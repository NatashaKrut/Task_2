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
import static org.hamcrest.core.IsEqual.equalTo;

public class GettingOrdersTests {

    private static String email;
    private static String password;
    private String name;
    private static String userToken;
    static final String BUN_HASH = "61c0c5a71d1f82001bdaaa6d";
    static final String SAUCE_HASH = "61c0c5a71d1f82001bdaaa72";
    static final String MEAT_HASH = "61c0c5a71d1f82001bdaaa6f";
    static final String STEAK_HASH = "61c0c5a71d1f82001bdaaa70";

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
    @DisplayName("Проверяю получение заказов авторизованного пользователя")
    void checkGetOrdersForUserWithAuthorize() {
        Ingredients burgerOne = new Ingredients(List.of(BUN_HASH, SAUCE_HASH));
        Ingredients burgerTwo = new Ingredients(List.of(BUN_HASH, MEAT_HASH));
        Ingredients burgerThree = new Ingredients(List.of(STEAK_HASH, BUN_HASH, SAUCE_HASH));
        sendOrdersPostRequestWithAuthorize(burgerOne);
        sendOrdersPostRequestWithAuthorize(burgerTwo);
        sendOrdersPostRequestWithAuthorize(burgerThree);
        Response response = sendOrdersGetRequestWithAuthorize();
        verifyOrdersListGetWithAuthorized(response);
    }

    @Test
    @DisplayName("Проверяю получение заказов НЕавторизованного пользователя")
    void checkGetOrdersForUserWithoutAuthorize() {
        Response response = sendOrdersGetRequestWithoutAuthorize();
        verifyOrdersListGetWithoutAuthorized(response);
    }

    @Step("Отправка POST запроса на /api/orders с авторизацией (создание заказов)")
    private void sendOrdersPostRequestWithAuthorize(Ingredients body) {
        given()
                .header("Content-type", "application/json")
                .auth().oauth2(userToken)
                .body(body)
                .post("/api/orders");
    }

    @Step("Отправка GET запроса на /api/orders с авторизацией (получение заказов)")
    private Response sendOrdersGetRequestWithAuthorize() {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(userToken)
                .get("/api/orders");
    }

    @Step("Проверка успешного получения списка заказов авторизованного пользователя")
    private void verifyOrdersListGetWithAuthorized(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @Step("Отправка GET запроса на /api/orders без авторизации (получение заказов)")
    private Response sendOrdersGetRequestWithoutAuthorize() {
        return given()
                .header("Content-type", "application/json")
                .get("/api/orders");
    }

    @Step("Проверка получения списка заказов НЕавторизованного пользователя")
    private void verifyOrdersListGetWithoutAuthorized(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }

}
