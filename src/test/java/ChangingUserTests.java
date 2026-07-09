import service.RestAssuredTests;
import service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;
import service.UserApiClient;

import java.util.Locale;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class ChangingUserTests extends RestAssuredTests {

    private static String email;
    private static String password;
    private String name;
    private static String userToken;

    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();
    UserApiClient userClient = new UserApiClient();

    @BeforeEach
    void prepareTestData() {
        email = faker.internet().emailAddress();
        password = faker.internet().password();
        name = faker.name().firstName();
        userToken = tdh.createTestUser(email, password, name);
    }

    @AfterAll
    static void clearTestData() {
        tdh.deleteUser(userToken);
    }

    @Test
    @DisplayName("Проверяю изменение данных EMAIL пользователя с авторизацией")
    void checkChangingUserEmailWithAuthorize() {
        userToken = tdh.authorizeTestUser(email, password);
        String newEmail = faker.internet().emailAddress();
        String newBody = String.format("{\"email\":\"%s\"}", newEmail);
        Response userPatchResponse = userClient.updateUserWithAuth(userToken, newBody);
        verifyUserPatchWithAuthorized(userPatchResponse);
        verifyUserPatchResponseContainsData(userPatchResponse, "email", newEmail);
    }

    @Test
    @DisplayName("Проверяю изменение данных NAME пользователя с авторизацией")
    void checkChangingUserNameWithAuthorize() {
        userToken = tdh.authorizeTestUser(email, password);
        String newName = faker.name().firstName();
        String newBody = String.format("{\"name\":\"%s\"}", newName);
        Response userPatchResponse = userClient.updateUserWithAuth(userToken, newBody);
        verifyUserPatchWithAuthorized(userPatchResponse);
        verifyUserPatchResponseContainsData(userPatchResponse, "name", newName);
    }

    @Test
    @DisplayName("Проверяю изменение данных EMAIL пользователя без авторизации")
    void checkChangingUserEmailWithoutAuthorize() {
        String newEmail = faker.internet().emailAddress();
        String newBody = String.format("{\"email\":\"%s\"}", newEmail);
        Response userPatchResponse = userClient.updateUserWithoutAuth(newBody);
        verifyUserPatchWithoutAuthorized(userPatchResponse);
    }

    @Test
    @DisplayName("Проверяю изменение данных NAME пользователя без авторизации")
    void checkChangingUserNameWithoutAuthorize() {
        String newName = faker.name().firstName();
        String newBody = String.format("{\"name\":\"%s\"}", newName);
        Response userPatchResponse = userClient.updateUserWithoutAuth(newBody);
        verifyUserPatchWithoutAuthorized(userPatchResponse);
    }

    @Step("Проверка успешного изменения данных пользователя с авторизацией: статус 200 и success=true")
    private void verifyUserPatchWithAuthorized(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @Step("Проверка наличия новых данных в ответе")
    private void verifyUserPatchResponseContainsData(Response response, String field, String value) {
        response.then()
                .assertThat()
                .body("user." + field, equalTo(value));
    }

    @Step("Проверка ошибки изменения данных пользователя без авторизации: статус 401 и success=false")
    private void verifyUserPatchWithoutAuthorized(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("You should be authorised"));
    }
}