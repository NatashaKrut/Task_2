package service;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class IngredientsApiClient {
    private static final String INGREDIENTS_PATH = "/api/ingredients";

    public Response getIngredients() {
        return given()
                .header("Content-type", "application/json")
                .get(INGREDIENTS_PATH);
    }
}
