package bg.sofia.uni.fmi.mjt.result;

import bg.sofia.uni.fmi.mjt.json.Food;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ResultData {
    public static final String STATUS_OK = "OK";
    public static final String STATUS_ERROR = "ERROR";
    @Expose
    @SerializedName("status")
    private String status;

    @Expose
    @SerializedName("message")
    private String message;

    @Expose
    @SerializedName("foods")
    private List<Food> foods;

    @Expose
    @SerializedName("food")
    private Food food;


    public ResultData(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public ResultData(String status, Food food) {
        this.status = status;
        this.food = food;
    }

    public ResultData(String status, List<Food> foods) {


        this.status = status;
        this.foods = foods;
    }


    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Food getFood() {
        return food;
    }

    public List<Food> getFoods() {
        return foods;
    }


}
