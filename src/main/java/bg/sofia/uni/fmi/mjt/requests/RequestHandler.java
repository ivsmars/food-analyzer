package bg.sofia.uni.fmi.mjt.requests;

import bg.sofia.uni.fmi.mjt.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.exceptions.HttpRequestException;
import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.json.FoodQuery;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class RequestHandler {
    private static final int MAX_PAGE_COUNT = 10;

    private static final int OK = 200;
    private static final int NOT_FOUND = 404;
    private static final int BAD_INPUT_PARAMETER = 400;

    private static final int COMMAND_INDEX = 0;
    private static final int KEY_INDEX = 1;

    private static final String GET_FOOD = "get-food";
    private static final String GET_FOOD_REPORT = "get-food-report";
    private static final String GET_FOOD_BY_BARCODE = "get-food-by-barcode";
    private static final String SEPARATOR = " ";

    private final Gson gson = new Gson();
    private final HttpClient client;
    private final FoodCache foodCache;

    private Request clientRequest;


    public RequestHandler(HttpClient client, FoodCache foodCache) {
        checkNull(client, "client");
        checkNull(foodCache, "food cache");
        this.foodCache = foodCache;
        this.client = client;
    }

    public ResultData fetchFood(String request) throws InvalidRequestException {
        checkNull(request, "request");
        checkEmpty(request, "request");
        clientRequest = parseRequest(request);

        ResultData res = getFromCache();

        if (res != null) {
            return res;
        } else {
            switch (clientRequest.getSearchCriteria()) {
                case BY_KEYWORDS -> res = fetchByKeywords();
                case BY_FDCID -> res = fetchByFcdID();
            }
        }
        return res;
    }

    private HttpResponse<String> fetch() {
        HttpRequest httpRequest = clientRequest.toHttpRequest();
        HttpResponse<String> response;
        try {
            response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new HttpRequestException("Error occurred while trying to fetch data from API", e);
        }

        int statusCode = response.statusCode();
        switch (statusCode) {
            case OK -> {
                return response;
            }
            case NOT_FOUND -> {
                return null;
            }
            case BAD_INPUT_PARAMETER -> throw new HttpRequestException(String.format("%d Bad input parameter", statusCode));
            default -> throw new HttpRequestException(String.format("%d Unknown HTTP request error", statusCode));
        }
    }

    private ResultData fetchByFcdID() {
        HttpResponse<String> response = fetch();
        if (response != null) {
            Food foodResult = gson.fromJson(response.body(), Food.class);
            foodCache.submit(foodResult);
            return new ResultData(ResultData.STATUS_OK, foodResult);
        } else {
            return null;
        }
    }


    private ResultData fetchByKeywords() throws InvalidRequestException {
        HttpResponse<String> response = fetch();
        if (response == null) {
            return null;
        }

        FoodQuery foodQuery = gson.fromJson(response.body(), FoodQuery.class);
        if (foodQuery.getTotalHits() == 0) {
            return null;
        }

        foodQuery.setKeywords();
        List<Food> result = new ArrayList<>(foodQuery.getFoods());

        List<CompletableFuture<FoodQuery>> futures = new ArrayList<>();
        int currPage = foodQuery.getCurrentPage();
        int totalPages = foodQuery.getTotalPages();

        while (currPage < totalPages && currPage < MAX_PAGE_COUNT) {
            clientRequest = Request.newRequestBuilder()
                    .setKeywords(clientRequest.getKeywords())
                    .setPageNumber(++currPage)
                    .build();

            futures.add(client.sendAsync(clientRequest.toHttpRequest(), HttpResponse.BodyHandlers.ofString())
                    .thenApply(future -> {
                        if (future.statusCode() != OK) {
                            //404 and 400 shouldn't be returned here
                            throw new HttpRequestException(String.format("%d Unknown HTTP request error", future.statusCode()));
                        }
                        FoodQuery query = gson.fromJson(future.body(), FoodQuery.class);
                        query.setKeywords();
                        return query;
                    }));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        for (var future : futures) {
            try {
                result.addAll(future.get().getFoods());
            } catch (InterruptedException e) {
                throw new HttpRequestException("Future thread was interrupted while waiting", e);
            } catch (ExecutionException e) {
                throw new HttpRequestException("Future thread completed exceptionally", e);
            }
        }
        foodCache.submitAll(result);
        return new ResultData(ResultData.STATUS_OK, result);
    }

    private ResultData getFromCache() {
        switch (clientRequest.getSearchCriteria()) {
            case BY_FDCID -> {
                return foodCache.getByFdcId(clientRequest.getFdcId());
            }
            case BY_GTINUPC -> {
                return foodCache.getByGtinUpc(clientRequest.getGtinUpc());
            }
            case BY_KEYWORDS -> {
                return foodCache.getByKeywords(String.join(SEPARATOR, clientRequest.getKeywords()));
            }
            default -> {
                return null;
            }
        }
    }

    private Request parseRequest(String request) throws InvalidRequestException {
        String[] tokens = request.split(SEPARATOR);
        Request.RequestBuilder builder = Request.newRequestBuilder();
        switch (tokens[COMMAND_INDEX]) {
            case GET_FOOD -> {
                List<String> keywords = new ArrayList<>(Arrays.asList(tokens).subList(KEY_INDEX, tokens.length));
                builder.setKeywords(keywords);
            }
            case GET_FOOD_REPORT -> {
                try {
                    builder.setFdcId(Integer.parseInt(tokens[KEY_INDEX]));
                } catch (NumberFormatException e) {
                    throw new InvalidRequestException("Invalid fdcId", e);
                }
            }
            case GET_FOOD_BY_BARCODE -> builder.setGtinUpc(tokens[KEY_INDEX]);
        }
        return builder.build();
    }

    private void checkNull(Object object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(String.format("%s can't be null", name));
        }
    }

    private void checkEmpty(String instance, String name) {
        if (instance.isBlank()) {
            throw new IllegalArgumentException(String.format("%s can't be blank", name));
        }
    }
}
