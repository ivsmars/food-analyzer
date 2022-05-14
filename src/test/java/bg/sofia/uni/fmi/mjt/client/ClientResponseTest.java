package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.result.ResultData;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ClientResponseTest {
    private final Gson gson = new Gson();

    @Test
    public void testClientResponseSingleFood() {
        String json = "{\"fdcId\":539572,\"dataType\":\"Branded\",\"description\":\"BROCCOLI\",\"foodNutrients\":[{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":0.34,\"unitName\":\"G\"},{\"number\":\"203\",\"name\":\"Protein\",\"amount\":2.7,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":34.0,\"unitName\":\"KCAL\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":2.0,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":5.41,\"unitName\":\"G\"}],\"gtinUpc\":\"000651700229\"}\n";
        Food food = gson.fromJson(json, Food.class);
        ResultData resultData = new ResultData(ResultData.STATUS_OK, food);

        ClientResponse response = new ClientResponse(resultData);
        String expected = System.lineSeparator() +
                "Name: BROCCOLI" + System.lineSeparator() +
                "Ingredients: N/A" + System.lineSeparator() +
                "Total lipid (fat) : 0,34 G" + System.lineSeparator() +
                "Protein : 2,70 G" + System.lineSeparator() +
                "Energy : 34,00 KCAL" + System.lineSeparator() +
                "Fiber, total dietary : 2,00 G" + System.lineSeparator() +
                "Carbohydrate, by difference : 5,41 G" + System.lineSeparator();


        String actual = response.toHumanReadableString();
        assertEquals(expected, actual);
    }

    @Test
    public void testClientResponseListOfFoods() {
        String jsonFirstFood = "{\"fdcId\":2015943,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"203\",\"name\":\"Protein\",\"amount\":25.0,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":3.57,\"unitName\":\"G\"}],\"gtinUpc\":\"75925306223\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n";
        String jsonSecondFood = "{\"fdcId\":2118224,\"dataType\":\"Branded\",\"description\":\"CHEDDAR CHEESE\",\"foodNutrients\":[{\"number\":\"204\",\"name\":\"Total lipid (fat)\",\"amount\":32.1,\"unitName\":\"G\"},{\"number\":\"291\",\"name\":\"Fiber, total dietary\",\"amount\":0.0,\"unitName\":\"G\"},{\"number\":\"205\",\"name\":\"Carbohydrate, by difference\",\"amount\":7.14,\"unitName\":\"G\"},{\"number\":\"203\",\"name\":\"Protein\",\"amount\":21.4,\"unitName\":\"G\"},{\"number\":\"208\",\"name\":\"Energy\",\"amount\":393.0,\"unitName\":\"KCAL\"}],\"gtinUpc\":\"075925300009\",\"ingredients\":\"CHEDDAR CHEESE (PASTEURIZED MILK, CHEESE CULTURE, SALT, ENZYMES, ANNATTO COLOR), POTATO STARCH, STARCH AND CELLULOSE POWDER TO PREVENT CAKING, NATAMYCIN (MOLD INHIBITOR).\",\"query\":\"cheddar cheese\"}\n";
        Food food1 = gson.fromJson(jsonFirstFood, Food.class);
        Food food2 = gson.fromJson(jsonSecondFood, Food.class);

        List<Food> foodList = new ArrayList<>();
        foodList.add(food1);
        foodList.add(food2);

        ResultData resultData = new ResultData("OK", foodList);
        ClientResponse response = new ClientResponse(resultData);
        String expected = System.lineSeparator() +
                "Name: CHEDDAR CHEESE" + System.lineSeparator() +
                "FdcId: 2015943" + System.lineSeparator() +
                "GTIN/UPC: 75925306223" + System.lineSeparator() +
                System.lineSeparator() +
                "Name: CHEDDAR CHEESE" + System.lineSeparator() +
                "FdcId: 2118224" + System.lineSeparator() +
                "GTIN/UPC: 075925300009" + System.lineSeparator() +
                System.lineSeparator() +
                "2 results shown" + System.lineSeparator();
        String actual = response.toHumanReadableString();
        assertEquals(expected, actual);
    }

    @Test
    public void testClientResultError() {
        ResultData resultData = new ResultData("Error", "Food not found");
        ClientResponse response = new ClientResponse(resultData);
        String expected = "Food not found";
        String actual = response.toHumanReadableString();
        assertEquals(expected, actual);
    }

    @Test
    public void testClientResponseNullThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new ClientResponse(null));
    }
}
