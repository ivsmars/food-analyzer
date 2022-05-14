package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;


//At endpoint .../search, sometimes the same field(for example "ingredients") can be found twice
//(For example when searching with query broccoli the first result has two ingredient fields)
//This endpoint seems to have a lot of inconsistencies.
public class Food {

    @SerializedName("fdcId")
    @Expose
    private Integer fdcId;

    @SerializedName("dataType")
    @Expose
    private String dataType;

    @SerializedName("description")
    @Expose
    private String description;

    @SerializedName("foodNutrients")
    @Expose
    @JsonAdapter(CustomFoodNutrientListAdapter.class)
    private List<FoodNutrient> foodNutrients = null;

    @SerializedName("gtinUpc")
    @Expose
    private String gtinUpc;

    @SerializedName("ingredients")
    @Expose
    private String ingredients;

    @Expose
    @SerializedName("query")
    private String query;

    public Integer getFdcId() {
        return fdcId;
    }

    public String getDataType() {
        return dataType;
    }

    public String getDescription() {
        return description;
    }

    public List<FoodNutrient> getFoodNutrients() {
        return foodNutrients;
    }

    public String getGtinUpc() {
        return gtinUpc;
    }


    public String getIngredients() {
        return ingredients;
    }


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Food food = (Food) o;
        return fdcId.equals(food.fdcId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fdcId);
    }
}