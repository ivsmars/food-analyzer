package bg.sofia.uni.fmi.mjt.requests;

import bg.sofia.uni.fmi.mjt.exceptions.InvalidRequestException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.util.List;
import java.util.Properties;

public class Request {
    private static final String API_KEY;
    private static final int PAGE_SIZE = 10;

    private static final String SEARCH_ENDPOINT = "https://api.nal.usda.gov/fdc/v1/foods/search?";
    private static final String FOOD_ENDPOINT = "https://api.nal.usda.gov/fdc/v1/food/";

    private static final String FOOD_ENDPOINT_FORMAT = "%s%s?api_key=%s&format=abridged&nutrients=%d&nutrients=%d&nutrients=%d&nutrients=%d&nutrients=%d";
    private static final String SEARCH_ENDPOINT_FORMAT = "%sapi_key=%s&query=%s&requireAllWords=true&pageSize=%d&pageNumber=%d";

    private static final String URL_SPACE = "%20";

    private static final int CARBS_NUMBER = 205;
    private static final int KCAL_NUMBER = 208;
    private static final int FATS_NUMBER = 204;
    private static final int PROTEIN_NUMBER = 203;
    private static final int FIBERS_NUMBER = 291;

    private final String endpoint;
    private final List<String> keywords;
    private final int fdcId;
    private final String gtinUpc;
    private final SearchCriteria searchCriteria;
    private final int pageNumber;

    static {
        API_KEY = setApiKey();
    }

    private Request(RequestBuilder builder) {
        this.keywords = builder.keywords;
        this.fdcId = builder.fdcId;
        this.gtinUpc = builder.gtinUpc;
        this.searchCriteria = builder.searchCriteria;
        this.endpoint = builder.endpoint;
        this.pageNumber = builder.pageNumber;
    }

    private static String setApiKey(){
        try(InputStream in = new FileInputStream("./api.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            return properties.getProperty("api.key");
        }catch (IOException ex) {
            throw new RuntimeException("API key not found.");
        }
    }

    public static RequestBuilder newRequestBuilder() {
        return new RequestBuilder();
    }

    public static class RequestBuilder {
        private String endpoint;

        private int fdcId;
        private List<String> keywords;
        private String gtinUpc;

        private SearchCriteria searchCriteria;
        private int pageNumber = 1;


        private void setSearchCriteria(SearchCriteria criteria) {
            this.searchCriteria = criteria;
            if (criteria.equals(SearchCriteria.BY_FDCID)) {
                this.endpoint = FOOD_ENDPOINT;
            } else {
                this.endpoint = SEARCH_ENDPOINT;
            }
        }

        public RequestBuilder setKeywords(List<String> keywords) {
            this.keywords = keywords;
            setSearchCriteria(SearchCriteria.BY_KEYWORDS);
            return this;
        }

        public RequestBuilder setFdcId(int fdcId) {
            this.fdcId = fdcId;
            setSearchCriteria(SearchCriteria.BY_FDCID);
            return this;
        }

        public RequestBuilder setGtinUpc(String gtinUpc) {
            this.gtinUpc = gtinUpc;
            setSearchCriteria(SearchCriteria.BY_GTINUPC);
            return this;
        }

        public RequestBuilder setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }


        public Request build() throws InvalidRequestException {
            if (searchCriteria == null) {
                throw new InvalidRequestException("Cannot build request with missing search criteria");
            } else if (searchCriteria == SearchCriteria.BY_KEYWORDS && (keywords == null || keywords.isEmpty())) {
                throw new InvalidRequestException("Keywords missing");
            }
            return new Request(this);
        }


    }


    public HttpRequest toHttpRequest() {
        if (searchCriteria == SearchCriteria.BY_GTINUPC) {
            return null;
        }

        URI uri = null;
        if (this.searchCriteria == SearchCriteria.BY_FDCID) {

            uri = URI.create(String.format(FOOD_ENDPOINT_FORMAT,
                    this.endpoint, this.fdcId, API_KEY, CARBS_NUMBER,
                    FATS_NUMBER, FIBERS_NUMBER, KCAL_NUMBER, PROTEIN_NUMBER));

        } else if (this.searchCriteria == SearchCriteria.BY_KEYWORDS) {

            String query = String.join(URL_SPACE, keywords);
            uri = URI.create(String.format(SEARCH_ENDPOINT_FORMAT,
                    this.endpoint, API_KEY, query, PAGE_SIZE, pageNumber));

        }
        return HttpRequest.newBuilder()
                .uri(uri)
                .build();
    }


    public List<String> getKeywords() {
        return keywords;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }

    public int getFdcId() {
        return fdcId;
    }

    public SearchCriteria getSearchCriteria() {
        return searchCriteria;
    }

}
