import model.IngredientType;
import service.OrderApiClient;
import service.RestAssuredTests;
import service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.Ingredients;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Locale;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class GettingOrdersTests extends RestAssuredTests {

    private static String email;
    private static String password;
    private String name;
    private static String userToken;

    final String BUN_HASH = IngredientType.BUN.getHash();
    final String SAUCE_HASH = IngredientType.SAUCE.getHash();
    static final String MEAT_HASH = IngredientType.MAIN.getHash();
    static final String STEAK_HASH =  IngredientType.MAIN.getHash();

    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();
    OrderApiClient orderClient = new OrderApiClient();

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

        orderClient.createOrderWithAuth(userToken, burgerOne);
        orderClient.createOrderWithAuth(userToken, burgerTwo);
        orderClient.createOrderWithAuth(userToken, burgerThree);

        Response response = orderClient.getOrdersWithAuth(userToken);
        verifyOrdersListGetWithAuthorized(response);
    }

    @Test
    @DisplayName("Проверяю получение заказов НЕавторизованного пользователя")
    void checkGetOrdersForUserWithoutAuthorize() {
        Response response = orderClient.getOrdersWithoutAuth();
        verifyOrdersListGetWithoutAuthorized(response);
    }

    @Step("Проверка успешного получения списка заказов авторизованного пользователя")
    private void verifyOrdersListGetWithAuthorized(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
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