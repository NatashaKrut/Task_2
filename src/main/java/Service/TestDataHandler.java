package Service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;

public class TestDataHandler {

    @Step("Создаю тестового пользователя")
    public String createTestUser(String email, String password, String name) {
        User userBody = new User(email, password, name);
        Response response = given()
                .header("Content-type", "application/json")
                .body(userBody)
                .post("/api/auth/register");
        response.then().statusCode(SC_OK);
        return response.then().extract()
                .jsonPath()
                .getString("accessToken")
                .replaceAll("Bearer ", "");
    }

    @Step("Удаляю тестового пользователя")
    public void deleteUser(String bearerToken) {
        if (bearerToken != null) {
            given()
                    .header("Content-type", "application/json")
                    .auth().oauth2(bearerToken)
                    .delete("/api/auth/user").then().statusCode(SC_ACCEPTED);
        }
    }

    @Step("Авторизую тестового пользователя")
    public String authorizeTestUser(String email, String password) {
        User userBody = new User(email, password);
        Response response = given()
                .header("Content-type", "application/json")
                .body(userBody)
                .post("/api/auth/login");
        response.then().statusCode(SC_OK);
        return response.then().extract()
                .jsonPath()
                .getString("accessToken")
                .replaceAll("Bearer ", "");
    }
}
