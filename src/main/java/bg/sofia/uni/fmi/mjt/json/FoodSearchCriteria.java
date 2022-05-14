package bg.sofia.uni.fmi.mjt.json;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class FoodSearchCriteria {

    @SerializedName("query")
    @Expose
    private String query;
    @SerializedName("pageSize")
    @Expose
    private Integer pageSize;

    @SerializedName("pageNumber")
    @Expose
    private Integer pageNumber;

    public String getQuery() {
        return query;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }
}