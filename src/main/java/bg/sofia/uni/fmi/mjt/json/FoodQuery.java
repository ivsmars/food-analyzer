package bg.sofia.uni.fmi.mjt.json;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;


public class FoodQuery {

    @SerializedName("foodSearchCriteria")
    @Expose
    private FoodSearchCriteria foodSearchCriteria;

    @SerializedName("totalHits")
    @Expose
    private Integer totalHits;

    @SerializedName("currentPage")
    @Expose
    private Integer currentPage;

    @SerializedName("totalPages")
    @Expose
    private Integer totalPages;

    @SerializedName("foods")
    @Expose
    private List<Food> foods = null;

    public FoodSearchCriteria getFoodSearchCriteria() {
        return foodSearchCriteria;
    }

    public Integer getTotalHits() {
        return totalHits;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }


    public Integer getTotalPages() {
        return totalPages;
    }


    public List<Food> getFoods() {
        return foods;
    }

    public void setKeywords() {
        for (var food : foods) {
            food.setQuery(this.getFoodSearchCriteria().getQuery());
        }
    }

}
