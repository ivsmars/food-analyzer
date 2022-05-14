package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomFoodNutrientListAdapter extends TypeAdapter<List<FoodNutrient>> {
    @Override
    public void write(JsonWriter jsonWriter, List<FoodNutrient> foodNutrients) throws IOException {
        jsonWriter.beginArray();
        for (FoodNutrient nutrient : foodNutrients) {
            CustomFoodNutrientAdapter customFoodNutrientAdapter = new CustomFoodNutrientAdapter();
            customFoodNutrientAdapter.write(jsonWriter, nutrient);
        }
        jsonWriter.endArray();
    }

    @Override
    public List<FoodNutrient> read(JsonReader jsonReader) throws IOException {
        List<FoodNutrient> res = new ArrayList<>();
        jsonReader.beginArray();
        while (jsonReader.hasNext()) {
            FoodNutrient nutrient;
            CustomFoodNutrientAdapter customFoodNutrientAdapter = new CustomFoodNutrientAdapter();
            nutrient = customFoodNutrientAdapter.read(jsonReader);
            if (nutrient != null) {
                res.add(nutrient);
            }
        }
        jsonReader.endArray();
        return res;
    }
}
