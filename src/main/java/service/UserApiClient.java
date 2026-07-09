package service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;

import static io.restassured.RestAssured.given;

public class UserApiClient {

    private static final String USER_PATH = "/api/auth/user";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    @Step("Регистрация пользователя")
    public Response registerUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post(REGISTER_PATH);
    }

    @Step("Авторизация пользователя")
    public Response loginUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .body(user)
                .post(LOGIN_PATH);
    }

    @Step("Обновление данных пользователя с авторизацией")
    public Response updateUserWithAuth(String token, String body) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(token)
                .body(body)
                .patch(USER_PATH);
    }

    @Step("Обновление данных пользователя без авторизации")
    public Response updateUserWithoutAuth(String body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .patch(USER_PATH);
    }

    @Step("Удаление пользователя")
    public Response deleteUser(String token) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(token)
                .delete(USER_PATH);
    }
}