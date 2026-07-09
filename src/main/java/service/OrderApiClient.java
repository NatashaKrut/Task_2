package service;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Ingredients;

import static io.restassured.RestAssured.given;

public class OrderApiClient {

    private static final String ORDERS_PATH = "/api/orders";

    @Step("Создание заказа с авторизацией")
    public Response createOrderWithAuth(String token, Ingredients ingredients) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(token)
                .body(ingredients)
                .post(ORDERS_PATH);
    }

    @Step("Создание заказа без авторизации")
    public Response createOrderWithoutAuth(Ingredients ingredients) {
        return given()
                .header("Content-type", "application/json")
                .body(ingredients)
                .post(ORDERS_PATH);
    }

    @Step("Получение заказов с авторизацией")
    public Response getOrdersWithAuth(String token) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(token)
                .get(ORDERS_PATH);
    }

    @Step("Получение заказов без авторизации")
    public Response getOrdersWithoutAuth() {
        return given()
                .header("Content-type", "application/json")
                .get(ORDERS_PATH);
    }
}