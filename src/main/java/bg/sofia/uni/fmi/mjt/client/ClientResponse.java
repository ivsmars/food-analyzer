package bg.sofia.uni.fmi.mjt.client;

import bg.sofia.uni.fmi.mjt.json.Food;
import bg.sofia.uni.fmi.mjt.result.ResultData;

import java.util.List;

public class ClientResponse {
    private final ResultData resultData;
    private static final String STATUS_OK = "OK";
    private static final String BRANDED_FOOD = "Branded";

    public ClientResponse(ResultData resultData) {
        if (resultData == null) {
            throw new IllegalArgumentException("Result data cannot be null");
        }
        this.resultData = resultData;
    }

    public String toHumanReadableString() {
        if (!resultData.getStatus().equals(STATUS_OK)) {
            return resultData.getMessage();
        }
        if (resultData.getFood() != null) {
            return humanReadableStringByFdcId();
        }
        return humanReadableStringByKeywords();
    }

    private String humanReadableStringByFdcId() {
        Food food = resultData.getFood();
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        builder.append(String.format("Name: %s", food.getDescription()))
                .append(System.lineSeparator())
                .append(String.format("Ingredients: %s", food.getIngredients() == null ? "N/A" : food.getIngredients()))
                .append(System.lineSeparator());
        for (var nutrient : food.getFoodNutrients()) {
            builder.append(nutrient.toString()).append(System.lineSeparator());
        }
        return builder.toString();
    }

    private String humanReadableStringByKeywords() {
        StringBuilder builder = new StringBuilder(System.lineSeparator());
        List<Food> foods = resultData.getFoods();
        for (Food food : foods) {
            builder.append(String.format("Name: %s", food.getDescription()))
                    .append(System.lineSeparator())
                    .append(String.format("FdcId: %s", food.getFdcId()))
                    .append(System.lineSeparator());
            if (food.getDataType().equals(BRANDED_FOOD)) {
                builder.append("GTIN/UPC: ")
                        .append(food.getGtinUpc())
                        .append(System.lineSeparator());
            }
            builder.append(System.lineSeparator());
        }
        builder.append(String.format("%d results shown", foods.size()))
                .append(System.lineSeparator());
        return builder.toString();
    }


}
