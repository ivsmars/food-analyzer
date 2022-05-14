package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FoodTest {
    private final Gson gson = new Gson();

    @Test
    public void testJsonParseFood() {
        String json = "{\"fdcId\":2095236,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"269\",\"name\":\"Sugars, total including NLEA\",\"amount\":0.0,\"unitName\":\"G\"}],\"gtinUpc\":\"828653282457\",\"ingredients\":\"MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.\",\"query\":\"cheddar cheese\"}\n";

        Food food = gson.fromJson(json, Food.class);
        assertEquals(food.getQuery(), "cheddar cheese");
        assertEquals(food.getGtinUpc(), "828653282457");
        assertEquals(food.getDataType(), "Branded");
        assertEquals(food.getDescription(), "CHEDDAR CHEESE");
        assertEquals(food.getIngredients(), "MILK, CHEESE CULTURES, SALT, ENZYMES AND ANNATTO COLOR IF NECESSARY.");
        assertEquals(food.getFdcId(), 2095236);
    }


}
