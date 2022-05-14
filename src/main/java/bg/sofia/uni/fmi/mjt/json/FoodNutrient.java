package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

//The returned value for the food nutrients is inconsistent with the API's documentation.
//When a request is sent to endpoint "/search/" the food nutrients should be in the abridged form
//as specified in the API's documentation, but instead it returns a food nutrient list which doesn't have a
//specified format in the documentation and is neither AbridgedFoodNutrient nor FoodNutrient.
//Another inconsistency is that the nutrient number is supposed to be an integer but is instead a string.
//That's why I've created a custom adapter. The two formats' first fields are differentiable, so it's easy to parse.
//

@JsonAdapter(CustomFoodNutrientAdapter.class)
public class FoodNutrient {
    @SerializedName("number")
    @Expose
    private String number;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("amount")
    @Expose
    private Double amount;

    @SerializedName("unitName")
    @Expose
    private String unitName;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public String getUnitName() {
        return unitName;
    }

    @Override
    public String toString() {
        return String.format("%s : %.2f %s", name, amount, unitName);
    }
}