package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FoodSearchCriteriaTest {
    private final Gson gson = new Gson();

    @Test
    public void testFoodSearchCriteriaFromJson() {
        String json = """
                {
                    "dataType": [],
                    "query": "broccoli",
                    "generalSearchInput": "broccoli",
                    "pageNumber": 1,
                    "sortBy": "dataType.keyword",
                    "sortOrder": "asc",
                    "numberOfResultsPerPage": 50,
                    "pageSize": 2,
                    "requireAllWords": false
                  }""";
        FoodSearchCriteria foodSearchCriteria = gson.fromJson(json,FoodSearchCriteria.class);
        assertEquals(foodSearchCriteria.getQuery(),"broccoli");
        assertEquals(foodSearchCriteria.getPageNumber(),1);
        assertEquals(foodSearchCriteria.getPageSize(),2);
    }
}
