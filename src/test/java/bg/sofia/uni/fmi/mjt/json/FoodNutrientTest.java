package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


//Keep in mind that only food numbers which we need will produce result, the others will return null
public class FoodNutrientTest {
    private final Gson gson = new Gson();

    @Test
    public void testFoodNutrientAbridged() {
        String json = """
                {"number": 303,
                  "name": "Iron, Fe",
                  "amount": 0.53,
                  "unitName": "mg",
                  "derivationCode": "LCCD",
                  "derivationDescription": "Calculated from a daily value percentage per serving size measure"}""";
        FoodNutrient foodNutrient = gson.fromJson(json, FoodNutrient.class);
        assertEquals(foodNutrient.getAmount(), 0.53);
        assertEquals(foodNutrient.getUnitName(), "mg");
        assertEquals(foodNutrient.getNumber(), "303");
        assertEquals(foodNutrient.getName(), "Iron, Fe");
    }

    @Test
    public void testFoodNutrientFull() {
        String json = """
                {
                  "nutrientId": 1003,
                  "nutrientName": "Protein",
                  "nutrientNumber": "203",
                  "unitName": "G",
                  "value": 4.25,
                  "rank": 600,
                  "indentLevel": 1,
                  "foodNutrientId": 13243963
                }""";
        FoodNutrient foodNutrient = gson.fromJson(json, FoodNutrient.class);
        assertEquals(foodNutrient.getAmount(), 4.25);
        assertEquals(foodNutrient.getUnitName(), "G");
        assertEquals(foodNutrient.getNumber(), "203");
        assertEquals(foodNutrient.getName(), "Protein");
    }

    @Test
    public void testGetUnnecessaryNutrientProducesNull() {
        String json = """
                {"nutrientId": 1018,
                  "nutrientName": "Alcohol, ethyl",
                  "nutrientNumber": "221",
                  "unitName": "G",
                  "value": 0,
                  "rank": 18200,
                  "indentLevel": 1,
                  "foodNutrientId": 13243967}""";
        assertNull(gson.fromJson(json, FoodNutrient.class));
    }
}
