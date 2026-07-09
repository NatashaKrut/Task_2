package model;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngredientsResponse {

    private boolean success;

    private List<Ingredient> data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ingredient {

        @SerializedName("_id")
        private String id;

        private String name;
        private String type;
        private int proteins;
        private int fat;
        private int carbohydrates;
        private int calories;
        private int price;
        private String image;

        @SerializedName("image_mobile")
        private String imageMobile;

        @SerializedName("image_large")
        private String imageLarge;

        @SerializedName("__v")
        private int version;
    }
}