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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;

public class CreateOrderTests extends RestAssuredTests {

    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();
    OrderApiClient orderClient = new OrderApiClient();

    private static String email;
    private static String password;
    private String name;
    private static String userToken;
    static final String TYPE_BUN = "bun";
    static final String TYPE_SAUCE = "sauce";
    final String BUN_HASH = tdh.getIngredientHashByType(TYPE_BUN);
    final String SAUCE_HASH = tdh.getIngredientHashByType(TYPE_SAUCE);
    final String WRONG_HASH = faker.crypto().sha1(); //похожее на hash случайное заведомо неверное значение


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
        Response ordersResponse = orderClient.createOrderWithAuth(userToken, body);
        verifyOrdersPost(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю создание заказа без авторизации")
    void checkCreateOrderWithoutAuthorize() {
        Ingredients body = new Ingredients(List.of(BUN_HASH, SAUCE_HASH));
        Response ordersResponse = orderClient.createOrderWithoutAuth(body);
        verifyOrdersPost(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать заказ без ингридиентов")
    void checkCreateOrderWithoutIngredients() {
        Ingredients body = new Ingredients();
        Response ordersResponse = orderClient.createOrderWithAuth(userToken, body);
        verifyOrdersPostWithoutIngredients(ordersResponse);
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать заказ с неверным хешем ингредиентов")
    void checkCreateOrderWithWrongIngredients() {
        Ingredients body = new Ingredients(List.of(WRONG_HASH));
        Response ordersResponse = orderClient.createOrderWithAuth(userToken, body);
        verifyOrdersPostWithWrongIngredients(ordersResponse);
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