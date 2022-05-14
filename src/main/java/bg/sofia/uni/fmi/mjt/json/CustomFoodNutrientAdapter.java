package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CustomFoodNutrientAdapter extends TypeAdapter<FoodNutrient> {
    //we only need carbs,fibers,calories,proteins and fats, the others should be skipped
    //because they only bloat the buffer and take unnecessary space in the cache
    private static final String CARBS_NUMBER = "205";
    private static final String KCAL_NUMBER = "208";
    private static final String FATS_NUMBER = "204";
    private static final String PROTEIN_NUMBER = "203";
    private static final String FIBERS_NUMBER = "291";
    private static final Set<String> NUTRIENT_NUMBERS = new HashSet<>(Arrays.asList(CARBS_NUMBER,
            KCAL_NUMBER,
            FATS_NUMBER,
            PROTEIN_NUMBER,
            FIBERS_NUMBER));


    //shared fields
    private static final String UNIT_NAME = "unitName";

    //abridged field names
    private static final String NUMBER = "number";
    private static final String NAME = "name";
    private static final String AMOUNT = "amount";


    //not abridged field names
    private static final String NUTRIENT_ID = "nutrientId";
    private static final String NUTRIENT_NAME = "nutrientName";
    private static final String NUTRIENT_NUMBER = "nutrientNumber";
    private static final String VALUE = "value";

    @Override
    public void write(JsonWriter jsonWriter, FoodNutrient foodNutrient) throws IOException {
        jsonWriter.beginObject();
        jsonWriter.name(NUMBER);
        jsonWriter.value(foodNutrient.getNumber());
        jsonWriter.name(NAME);
        jsonWriter.value(foodNutrient.getName());
        jsonWriter.name(AMOUNT);
        jsonWriter.value(foodNutrient.getAmount());
        jsonWriter.name(UNIT_NAME);
        jsonWriter.value(foodNutrient.getUnitName());
        jsonWriter.endObject();
    }

    @Override
    public FoodNutrient read(JsonReader jsonReader) throws IOException {
        FoodNutrient res;
        jsonReader.beginObject();
        String name = jsonReader.nextName();
        switch (name) {
            case NUMBER -> res = readAbridgedFoodNutrient(jsonReader);
            case NUTRIENT_ID -> res = readFullFoodNutrient(jsonReader);
            default -> throw new IllegalStateException();
        }
        jsonReader.endObject();
        return res;
    }

    private FoodNutrient readAbridgedFoodNutrient(JsonReader reader) throws IOException {
        FoodNutrient res = new FoodNutrient();
        res.setNumber(reader.nextString());

        String name;
        while (reader.hasNext()) {
            name = reader.nextName();
            switch (name) {
                case NUMBER -> res.setNumber(reader.nextString());
                case NAME -> res.setName(reader.nextString());
                case AMOUNT -> res.setAmount(reader.nextDouble());
                case UNIT_NAME -> res.setUnitName(reader.nextString());
                default -> reader.skipValue();
            }
        }
        return res;
    }

    private FoodNutrient readFullFoodNutrient(JsonReader reader) throws IOException {
        boolean returnNull = false;
        FoodNutrient res = new FoodNutrient();
        String name;
        reader.skipValue();
        while (reader.hasNext()) {
            name = reader.nextName();
            switch (name) {
                case NUTRIENT_NAME -> res.setName(reader.nextString());
                case NUTRIENT_NUMBER -> {
                    String number = reader.nextString();
                    if (NUTRIENT_NUMBERS.contains(number)) {
                        res.setNumber(number);
                    } else {
                        returnNull = true;
                    }
                }
                case UNIT_NAME -> res.setUnitName(reader.nextString());
                case VALUE -> res.setAmount(reader.nextDouble());
                default -> reader.skipValue();
            }
        }
        return returnNull ? null : res;
    }
}
