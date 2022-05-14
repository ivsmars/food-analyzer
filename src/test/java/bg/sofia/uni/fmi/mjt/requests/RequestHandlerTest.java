package bg.sofia.uni.fmi.mjt.requests;

import bg.sofia.uni.fmi.mjt.cache.FoodCache;
import bg.sofia.uni.fmi.mjt.exceptions.HttpRequestException;
import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;
import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.json.FoodQuery;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestHandlerTest {
    @Mock
    private HttpClient client;

    @Mock
    private FoodCache foodCache;


    private final Gson gson = new Gson();
    private RequestHandler requestHandler;

    @BeforeEach
    private void init() {
        requestHandler = new RequestHandler(client, foodCache);
    }


    @Test
    public void testSendRequestReceiveOneFoodItem() throws IOException, InterruptedException, InvalidRequestException {
        HttpResponse<String> httpResponse = mock(HttpResponse.class);
        when(httpResponse.statusCode()).thenReturn(200);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(httpResponse);

        String json = """
                {
                  "dataType": "Branded",
                  "description": "NUT 'N BERRY MIX",
                  "fdcId": 534358,
                  "foodNutrients": [
                    {
                      "number": 303,
                      "name": "Iron, Fe",
                      "amount": 0.53,
                      "unitName": "mg",
                      "derivationCode": "LCCD",
                      "derivationDescription": "Calculated from a daily value percentage per serving size measure"
                    }
                  ],
                  "publicationDate": "4/1/2019",
                  "brandOwner": "Kar Nut Products Company",
                  "gtinUpc": "077034085228",
                  "ndbNumber": 7954,
                  "foodCode": "27415110"
                }""";
        when(httpResponse.body()).thenReturn(json);
        when(foodCache.getByFdcId(anyInt())).thenReturn(null);

        Food result = requestHandler.fetchFood("get-food-report 534358").getFood();

        assertNotNull(result);
        assertEquals(result.getFdcId(), 534358);
        assertNull(result.getIngredients());
        assertEquals(result.getDescription(), "NUT 'N BERRY MIX");
        assertEquals(result.getDataType(), "Branded");
        assertEquals(result.getFoodNutrients().size(), 1);

    }

    @Test
    public void testGetMultiplePages() throws InvalidRequestException, IOException, InterruptedException {
        HttpResponse<String> firstPageHttpResponse = mock(HttpResponse.class);
        when(firstPageHttpResponse.statusCode()).thenReturn(200);
        assertDoesNotThrow(() -> gson.fromJson(firstPageJson, FoodQuery.class));

        when(firstPageHttpResponse.body()).thenReturn(firstPageJson);

        CompletableFuture<HttpResponse<String>> completableFutureMock = mock(CompletableFuture.class);

        CompletableFuture<FoodQuery> foodQueryCompletableFuture1 = new CompletableFuture<>();
        CompletableFuture<FoodQuery> foodQueryCompletableFuture2 = new CompletableFuture<>();

        foodQueryCompletableFuture1.complete(gson.fromJson(secondPageJson, FoodQuery.class));
        foodQueryCompletableFuture2.complete(gson.fromJson(thirdPageJson, FoodQuery.class));

        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(firstPageHttpResponse);
        when(client.sendAsync(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(completableFutureMock);
        when(completableFutureMock.thenApply(any(Function.class))).thenReturn(foodQueryCompletableFuture1, foodQueryCompletableFuture2);


        when(foodCache.getByKeywords(anyString())).thenReturn(null);
        ResultData result = requestHandler.fetchFood("get-food cheddar cheese");

        assertNotNull(result);
        assertNotNull(result.getFoods());
        assertEquals(result.getFoods().size(), 6);
        assertEquals(result.getStatus(), ResultData.STATUS_OK);
    }


    @Test
    public void testThrowsHttpRequestExceptionSendThrowsIOException() throws IOException, InterruptedException {
        when(foodCache.getByKeywords(anyString())).thenReturn(null);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(IOException.class);
        assertThrows(HttpRequestException.class, () -> new RequestHandler(client, foodCache).fetchFood("get-food cheddar cheese"));
    }

    @Test
    public void testThrowsHttpRequestExceptionSendThrowsInterruptedException() throws IOException, InterruptedException {
        when(foodCache.getByKeywords(anyString())).thenReturn(null);
        when(foodCache.getByFdcId(anyInt())).thenReturn(null);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenThrow(InterruptedException.class);
        assertThrows(HttpRequestException.class, () -> requestHandler.fetchFood("get-food cheddar cheese"));
        assertThrows(HttpRequestException.class, () -> requestHandler.fetchFood("get-food-report 1234123"));
    }

    @Test
    public void testGetFoodByGtinUpcNotFoundInCache() throws InvalidRequestException {
        when(foodCache.getByGtinUpc(anyString())).thenReturn(null);
        RequestHandler requestHandler = new RequestHandler(client, foodCache);
        assertNull(requestHandler.fetchFood("get-food-by-barcode 98123812"));
    }

    @Test
    public void testGetIsInCache() throws InvalidRequestException {
        String json = """
                {
                  "dataType": "Branded",
                  "description": "NUT 'N BERRY MIX",
                  "fdcId": 534358,
                  "foodNutrients": [
                    {
                      "number": 303,
                      "name": "Iron, Fe",
                      "amount": 0.53,
                      "unitName": "mg",
                      "derivationCode": "LCCD",
                      "derivationDescription": "Calculated from a daily value percentage per serving size measure"
                    }
                  ],
                  "publicationDate": "4/1/2019",
                  "brandOwner": "Kar Nut Products Company",
                  "gtinUpc": "077034085228",
                  "ndbNumber": 7954,
                  "foodCode": "27415110"
                }""";
        Food food = gson.fromJson(json, Food.class);
        when(foodCache.getByGtinUpc(anyString())).thenReturn(new ResultData(ResultData.STATUS_OK, food));

        Food res = requestHandler.fetchFood("get-food-by-barcode 077034085228").getFood();
        assertEquals(res.getFoodNutrients(), food.getFoodNutrients());
        assertEquals(res.getDataType(), food.getDataType());
        assertEquals(res.getQuery(), food.getQuery());
        assertEquals(res.getDescription(), food.getDescription());
        assertEquals(res.getFdcId(), food.getFdcId());
        assertEquals(res.getGtinUpc(), food.getGtinUpc());
        assertEquals(res.getIngredients(), food.getIngredients());

    }

    @Test
    public void testReturnsNullStatusCode404() throws IOException, InvalidRequestException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);
        assertNull(requestHandler.fetchFood("get-food-report 12341"));
    }

    @Test
    public void testThrowsHttpRequestExceptionStatusCode400() throws IOException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);
        assertThrows(HttpRequestException.class, () -> requestHandler.fetchFood("get-food-report 12341"));
    }

    @Test
    public void testThrowsHttpRequestExceptionStatusCodeUnknown() throws IOException, InterruptedException {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(client.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(response);
        assertThrows(HttpRequestException.class, () -> requestHandler.fetchFood("get-food-report 12341"));
    }

    @Test
    public void testIllegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> new RequestHandler(null, foodCache));
        assertThrows(IllegalArgumentException.class, () -> new RequestHandler(client, null));
        assertThrows(IllegalArgumentException.class, () -> new RequestHandler(client, foodCache).fetchFood(null));
        assertThrows(IllegalArgumentException.class, () -> new RequestHandler(client, foodCache).fetchFood("  "));
    }


    private static final String firstPageJson = """
            {
              "totalHits": 3801,
              "currentPage": 1,
              "totalPages": 3,
              "pageList": [
                1,
                2,
                3
              ],
              "foodSearchCriteria": {
                "dataType": [],
                "query": "broccoli",
                "generalSearchInput": "broccoli",
                "pageNumber": 1,
                "sortBy": "dataType.keyword",
                "sortOrder": "asc",
                "numberOfResultsPerPage": 50,
                "pageSize": 2,
                "requireAllWords": false
              },
              "foods": [
                {
                  "fdcId": 539572,
                  "description": "BROCCOLI",
                  "lowercaseDescription": "broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "000651700229",
                  "publishedDate": "2019-04-01",
                  "brandOwner": "OCEAN MIST",
                  "ingredients": "",
                  "marketCountry": "United States",
                  "foodCategory": "Pre-Packaged Fruit & Vegetables",
                  "modifiedDate": "2018-01-19",
                  "dataSource": "LI",
                  "servingSizeUnit": "g",
                  "servingSize": 148,
                  "householdServingFullText": "1 MEDIUM STALK",
                  "allHighlightFields": "",
                  "score": 1011.23737,
                  "foodNutrients": [
                    {
                      "nutrientId": 1087,
                      "nutrientName": "Calcium, Ca",
                      "nutrientNumber": "301",
                      "unitName": "MG",
                      "derivationCode": "LCCD",
                      "derivationDescription": "Calculated from a daily value percentage per serving size measure",
                      "derivationId": 75,
                      "value": 41,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 5300,
                      "indentLevel": 1,
                      "foodNutrientId": 3194233,
                      "percentDailyValue": 6
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                },
                {
                  "fdcId": 1935906,
                  "description": "BROCCOLI",
                  "lowercaseDescription": "broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "709351891335",
                  "publishedDate": "2021-07-29",
                  "brandOwner": "Curation Foods, Inc.",
                  "brandName": "EAT SMART",
                  "subbrandName": "BENEFORT'E",
                  "ingredients": "BROCCOLI",
                  "marketCountry": "United States",
                  "foodCategory": "Pre-Packaged Fruit & Vegetables",
                  "modifiedDate": "2021-03-13",
                  "dataSource": "LI",
                  "packageWeight": "32 oz/2 lbs/907 g",
                  "servingSizeUnit": "g",
                  "servingSize": 85,
                  "allHighlightFields": "<b>Ingredients</b>: <em>BROCCOLI</em>",
                  "score": 1011.23737,
                  "foodNutrients": [
                    {
                      "nutrientId": 1003,
                      "nutrientName": "Protein",
                      "nutrientNumber": "203",
                      "unitName": "G",
                      "derivationCode": "LCCS",
                      "derivationDescription": "Calculated from value per serving size measure",
                      "derivationId": 70,
                      "value": 3.53,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 600,
                      "indentLevel": 1,
                      "foodNutrientId": 23351156
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                }
              ],
              "aggregations": {
                "dataType": {
                  "Branded": 3502,
                  "Survey (FNDDS)": 278,
                  "SR Legacy": 20,
                  "Foundation": 1
                },
                "nutrients": {}
              }
            }""";

    private static final String secondPageJson = """
            {
              "totalHits": 3801,
              "currentPage": 2,
              "totalPages": 3,
              "pageList": [
                1,
                2,
                3
              ],
              "foodSearchCriteria": {
                "dataType": [],
                "query": "broccoli",
                "generalSearchInput": "broccoli",
                "pageNumber": 2,
                "sortBy": "dataType.keyword",
                "sortOrder": "asc",
                "numberOfResultsPerPage": 50,
                "pageSize": 2,
                "requireAllWords": false
              },
              "foods": [
                {
                  "fdcId": 1986874,
                  "description": "BROCCOLI",
                  "lowercaseDescription": "broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "085239119488",
                  "publishedDate": "2021-07-29",
                  "brandOwner": "Target Stores",
                  "brandName": "GOOD & GATHER",
                  "ingredients": "BROCCOLI.",
                  "marketCountry": "United States",
                  "foodCategory": "Frozen Vegetables",
                  "modifiedDate": "2021-01-25",
                  "dataSource": "LI",
                  "packageWeight": "12 oz/340 g",
                  "servingSizeUnit": "g",
                  "servingSize": 85,
                  "allHighlightFields": "<b>Ingredients</b>: <em>BROCCOLI</em>.",
                  "score": 1011.23737,
                  "foodNutrients": [
                    {
                      "nutrientId": 1003,
                      "nutrientName": "Protein",
                      "nutrientNumber": "203",
                      "unitName": "G",
                      "derivationCode": "LCCS",
                      "derivationDescription": "Calculated from value per serving size measure",
                      "derivationId": 70,
                      "value": 2.35,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 600,
                      "indentLevel": 1,
                      "foodNutrientId": 24207378
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                },
                {
                  "fdcId": 1993697,
                  "description": "BROCCOLI",
                  "lowercaseDescription": "broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "66507264306",
                  "publishedDate": "2021-07-29",
                  "brandOwner": "Quirch Foods Company",
                  "brandName": "MAMBO",
                  "ingredients": "BROCCOLI.",
                  "marketCountry": "United States",
                  "foodCategory": "Frozen Vegetables",
                  "modifiedDate": "2021-02-18",
                  "dataSource": "LI",
                  "packageWeight": "12 OZ/340 G",
                  "servingSizeUnit": "g",
                  "servingSize": 85,
                  "allHighlightFields": "<b>Ingredients</b>: <em>BROCCOLI</em>.",
                  "score": 1011.23737,
                  "foodNutrients": [
                    {
                      "nutrientId": 1003,
                      "nutrientName": "Protein",
                      "nutrientNumber": "203",
                      "unitName": "G",
                      "derivationCode": "LCCS",
                      "derivationDescription": "Calculated from value per serving size measure",
                      "derivationId": 70,
                      "value": 1.18,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 600,
                      "indentLevel": 1,
                      "foodNutrientId": 22753820
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                }
              ],
              "aggregations": {
                "dataType": {
                  "Branded": 3502,
                  "Survey (FNDDS)": 278,
                  "SR Legacy": 20,
                  "Foundation": 1
                },
                "nutrients": {}
              }
            }""";

    String thirdPageJson = """
            {
              "totalHits": 3801,
              "currentPage": 3,
              "totalPages": 3,
              "pageList": [
                1,
                2,
                3
              ],
              "foodSearchCriteria": {
                "dataType": [],
                "query": "broccoli",
                "generalSearchInput": "broccoli",
                "pageNumber": 3,
                "sortBy": "dataType.keyword",
                "sortOrder": "asc",
                "numberOfResultsPerPage": 50,
                "pageSize": 2,
                "requireAllWords": false
              },
              "foods": [
                {
                  "fdcId": 2014551,
                  "description": "BROCCOLI CAKES, BROCCOLI",
                  "lowercaseDescription": "broccoli cakes, broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "080868000565",
                  "publishedDate": "2021-10-28",
                  "brandOwner": "Dr. Praeger's Sensible Foods, Inc.",
                  "brandName": "DR. PRAEGER'S",
                  "ingredients": "BROCCOLI, POTATOES, ONIONS, POTATO FLAKES, EXPELLER PRESSED CANOLA OIL, OAT BRAN, EGG WHITES, ARROWROOT POWDER, SEA SALT, GARLIC, SPICE",
                  "marketCountry": "United States",
                  "foodCategory": "Frozen Prepared Sides",
                  "modifiedDate": "2021-06-19",
                  "dataSource": "LI",
                  "packageWeight": "10 oz/283 g",
                  "servingSizeUnit": "g",
                  "servingSize": 94,
                  "allHighlightFields": "<b>Ingredients</b>: <em>BROCCOLI</em>, POTATOES, ONIONS, POTATO FLAKES, EXPELLER PRESSED CANOLA OIL, OAT BRAN, EGG WHITES, ARROWROOT POWDER, SEA SALT, GARLIC, SPICE",
                  "score": -111.030495,
                  "foodNutrients": [
                    {
                      "nutrientId": 1003,
                      "nutrientName": "Protein",
                      "nutrientNumber": "203",
                      "unitName": "G",
                      "derivationCode": "LCCS",
                      "derivationDescription": "Calculated from value per serving size measure",
                      "derivationId": 70,
                      "value": 4.26,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 600,
                      "indentLevel": 1,
                      "foodNutrientId": 25014155
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                },
                {
                  "fdcId": 1888089,
                  "description": "BROCCOLI FLORETS, BROCCOLI",
                  "lowercaseDescription": "broccoli florets, broccoli",
                  "dataType": "Branded",
                  "gtinUpc": "07203671951",
                  "publishedDate": "2021-07-29",
                  "brandOwner": "Harris-Teeter Inc.",
                  "brandName": "HARRIS TEETER",
                  "subbrandName": "ORGANICS",
                  "ingredients": "ORGANIC BROCCOLI FLORETS.",
                  "marketCountry": "United States",
                  "foodCategory": "Pre-Packaged Fruit & Vegetables",
                  "modifiedDate": "2021-05-29",
                  "dataSource": "LI",
                  "packageWeight": "16 oz/1 lbs/454 g",
                  "servingSizeUnit": "g",
                  "servingSize": 85,
                  "allHighlightFields": "<b>Ingredients</b>: ORGANIC <em>BROCCOLI</em> FLORETS.",
                  "score": -111.030495,
                  "foodNutrients": [
                    {
                      "nutrientId": 1003,
                      "nutrientName": "Protein",
                      "nutrientNumber": "203",
                      "unitName": "G",
                      "derivationCode": "LCCS",
                      "derivationDescription": "Calculated from value per serving size measure",
                      "derivationId": 70,
                      "value": 1.18,
                      "foodNutrientSourceId": 9,
                      "foodNutrientSourceCode": "12",
                      "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                      "rank": 600,
                      "indentLevel": 1,
                      "foodNutrientId": 22440678
                    }],
                  "finalFoodInputFoods": [],
                  "foodMeasures": [],
                  "foodAttributes": [],
                  "foodAttributeTypes": [],
                  "foodVersionIds": []
                }
              ],
              "aggregations": {
                "dataType": {
                  "Branded": 3502,
                  "Survey (FNDDS)": 278,
                  "SR Legacy": 20,
                  "Foundation": 1
                },
                "nutrients": {}
              }
            }""";

}
