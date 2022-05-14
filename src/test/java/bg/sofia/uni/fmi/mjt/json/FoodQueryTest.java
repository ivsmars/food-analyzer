package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FoodQueryTest {
    private final Gson gson = new Gson();

    @Test
    public void testFoodQuery() {
        String json = """
                {
                  "totalHits": 3801,
                  "currentPage": 1,
                  "totalPages": 1901,
                  "pageList": [
                    1,
                    2,
                    3,
                    4,
                    5,
                    6,
                    7,
                    8,
                    9,
                    10
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
                        },
                        {
                          "nutrientId": 1089,
                          "nutrientName": "Iron, Fe",
                          "nutrientNumber": "303",
                          "unitName": "MG",
                          "derivationCode": "LCCD",
                          "derivationDescription": "Calculated from a daily value percentage per serving size measure",
                          "derivationId": 75,
                          "value": 0.73,
                          "foodNutrientSourceId": 9,
                          "foodNutrientSourceCode": "12",
                          "foodNutrientSourceDescription": "Manufacturer's analytical; partial documentation",
                          "rank": 5400,
                          "indentLevel": 1,
                          "foodNutrientId": 3194234,
                          "percentDailyValue": 6
                        }],"finalFoodInputFoods": [],
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
                        }], "finalFoodInputFoods": [],
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

        FoodQuery foodQuery = gson.fromJson(json, FoodQuery.class);
        assertNotNull(foodQuery);
        assertEquals(foodQuery.getCurrentPage(), 1);
        assertEquals(foodQuery.getTotalPages(), 1901);
        assertEquals(foodQuery.getTotalHits(), 3801);
        assertEquals(foodQuery.getFoods().size(),2);

    }
}
