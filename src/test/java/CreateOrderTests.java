import service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.Ingredients;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

public class CreateOrderTests {

    private static String email;
    private static String password;
    private String name;
    private static String userToken;
    static final String BUN_HASH = "61c0c5a71d1f82001bdaaa6d";
    static final String SAUCE_HASH = "61c0c5a71d1f82001bdaaa72";
    static final String WRONG_HAS = "63a7c8a52u1y44381bguiu9t";


    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
    }

    @BeforeEach
    void prepareTestData() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().firstName();
        tdh.createTestUser(email, password, name);
        userToken = tdh.authorizeTestUser(email, password);
    }

    @AfterAll
    static void clearTestData() {
        tdh.deleteUser(userToken);
    }

    @Test
    @DisplayName("Проверяю создание заказа с ингридиентами с авторизацией")
    void checkCreateOrderWithIngredientsWithAuthorize() {
        Ingredients body = new Ingredients(List.of(BUN_HASH, SAUCE_HASH));
        Response ordersResponse = sendOrdersPostRequestWithAuthorize(body);
        verifyOrdersPost(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю создание заказа без авторизации")
    void checkCreateOrderWithoutAuthorize() {
        Ingredients body = new Ingredients(List.of(BUN_HASH, SAUCE_HASH));
        Response ordersResponse = sendOrdersPostRequestWithoutAuthorize(body);
        verifyOrdersPost(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать заказ без ингридиентов")
    void checkCreateOrderWithoutIngredients() {
        Ingredients body = new Ingredients();
        Response ordersResponse = sendOrdersPostRequestWithAuthorize(body);
        verifyOrdersPostWithoutIngredients(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать заказ с неверным хешем ингредиентов")
    void checkCreateOrderWithWrongIngredients() {
        Ingredients body = new Ingredients(List.of(WRONG_HAS));
        Response ordersResponse = sendOrdersPostRequestWithAuthorize(body);
        verifyOrdersPostWithWrongIngredients(ordersResponse);
    }

    @Step("Отправка POST запроса на /api/orders с авторизацией")
    private Response sendOrdersPostRequestWithAuthorize(Ingredients body) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(userToken)
                .body(body)
                .post("/api/orders");
    }

    @Step("Отправка POST запроса на /api/orders без авторизации")
    private Response sendOrdersPostRequestWithoutAuthorize(Ingredients body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .post("/api/orders");
    }

    @Step("Проверка успешного создания заказа")
    private void verifyOrdersPost(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @Step("Проверка невозможности создания заказа без ингредиентов")
    private void verifyOrdersPostWithoutIngredients(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Проверка невозможности создания заказа с неверным хешем ингредиентов")
    private void verifyOrdersPostWithWrongIngredients(Response response) {
        response.then()
                .statusCode(SC_INTERNAL_SERVER_ERROR)
                .assertThat()
                .body(containsString("Internal Server Error"));
    }
}
