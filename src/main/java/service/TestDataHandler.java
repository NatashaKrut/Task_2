package service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.IngredientsResponse;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class TestDataHandler {

    private UserApiClient userClient = new UserApiClient();
    private IngredientsApiClient ingredientsClient = new IngredientsApiClient();

    @Step("Создаю тестового пользователя")
    public String createTestUser(String email, String password, String name) {
        User userBody = new User(email, password, name);
        Response response = userClient.registerUser(userBody);
        response.then().statusCode(SC_OK);
        return extractToken(response);
    }

    @Step("Удаляю тестового пользователя")
    public void deleteUser(String bearerToken) {
        if (bearerToken != null && !bearerToken.isEmpty()) {
            userClient.deleteUser(bearerToken)
                    .then().statusCode(SC_ACCEPTED);
        }
    }

    @Step("Авторизую тестового пользователя")
    public String authorizeTestUser(String email, String password) {
        User userBody = new User(email, password);
        Response response = userClient.loginUser(userBody);
        response.then().statusCode(SC_OK);
        return extractToken(response);
    }

    private String extractToken(Response response) {
        return response.then().extract()
                .jsonPath()
                .getString("accessToken")
                .replaceAll("Bearer ", "");
    }

    public String getIngredientHashByType(String type) {
        Response response = ingredientsClient.getIngredients();
        response.then().statusCode(SC_OK);
        IngredientsResponse ingredientsResponse = response.as(IngredientsResponse.class);
        List<IngredientsResponse.Ingredient> ingredients = ingredientsResponse.getData();

        List<IngredientsResponse.Ingredient> filteredIngredients = new ArrayList<>();
        for (IngredientsResponse.Ingredient ingredient : ingredients) {
            if (ingredient.getType().equals(type)) {
                filteredIngredients.add(ingredient);
            }
        }

        if (filteredIngredients.isEmpty()) {
            throw new IllegalArgumentException("Не найден ни один ингредиент с типом " + type);
        }

        Random random = new Random();
        IngredientsResponse.Ingredient randomIngredient = filteredIngredients.get(random.nextInt(filteredIngredients.size()));

        return randomIngredient.getId();
    }

}