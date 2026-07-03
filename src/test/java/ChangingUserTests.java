import service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class ChangingUserTests {

    private static String email;
    private static String password;
    private String name;
    private static String userToken;

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
        String newEmailBodyPattern = "{\"email\":\"%s\"}";
        String newBody = String.format(newEmailBodyPattern, newEmail);
        Response userPatchResponse = sendUserPatchRequestWithAuthorize(newBody);
        verifyUserPatchWithAuthorized(userPatchResponse);
        verifyUserPatchResponseContainsData(userPatchResponse, "email", newEmail);
    }

    @Test
    @DisplayName("Проверяю изменение данных NAME пользователя с авторизацией")
    void checkChangingUserNameWithAuthorize() {
        userToken = tdh.authorizeTestUser(email, password);
        String newName = faker.name().firstName();
        String newNameBodyPattern = "{\"name\":\"%s\"}";
        String newBody = String.format(newNameBodyPattern, newName);
        Response userPatchResponse = sendUserPatchRequestWithAuthorize(newBody);
        verifyUserPatchWithAuthorized(userPatchResponse);
        verifyUserPatchResponseContainsData(userPatchResponse, "name", newName);
    }

    @Step("Отправка PATCH запроса на /api/auth/user с авторизацией")
    private Response sendUserPatchRequestWithAuthorize(String body) {
        return given()
                .header("Content-type", "application/json")
                .auth().oauth2(userToken)
                .body(body)
                .patch("/api/auth/user");
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

    @Test
    @DisplayName("Проверяю изменение данных EMAIL пользователя без авторизации")
    void checkChangingUserEmailWithoutAuthorize() {
        String newEmail = faker.internet().emailAddress();
        String newEmailBodyPattern = "{\"email\":\"%s\"}";
        String newBody = String.format(newEmailBodyPattern, newEmail);
        Response userPatchResponse = sendUserPatchRequestWithoutAuthorize(newBody);
        verifyUserPatchWithoutAuthorized(userPatchResponse);
    }

    @Test
    @DisplayName("Проверяю изменение данных NAME пользователя без авторизации")
    void checkChangingUserNameWithoutAuthorize() {
        String newName = faker.internet().emailAddress();
        String newNameBodyPattern = "{\"name\":\"%s\"}";
        String newBody = String.format(newNameBodyPattern, newName);
        Response userPatchResponse = sendUserPatchRequestWithoutAuthorize(newBody);
        verifyUserPatchWithoutAuthorized(userPatchResponse);
    }

    @Step("Отправка PATCH запроса на /api/auth/user без авторизации")
    private Response sendUserPatchRequestWithoutAuthorize(String body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .patch("/api/auth/user");
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
