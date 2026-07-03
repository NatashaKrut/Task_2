import Service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Locale;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class LoginUserTests {

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
    @DisplayName("Проверяю, что можно авторизоваться под существующим пользователем")
    void checkUserCanBeLoggedIn() {
        User body = new User(email, password);
        Response loginResponse = sendLoginRequest(body);
        verifyUserAuthorized(loginResponse);
    }

    @Step("Отправка POST запроса на /api/auth/login")
    private Response sendLoginRequest(User body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .post("/api/auth/login");
    }

    @Step("Проверка успешной авторизации пользователя: статус 200 и success=true")
    private void verifyUserAuthorized(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @ParameterizedTest
    @MethodSource("userBodyProviderForLogin")
    @DisplayName("Проверяю, что нельзя авторизоваться с неверным логином и паролем")
    void checkUserCanNotBeLoggedInWithWrongData(String email, String password) {
        User body = new User(email, password);
        Response loginResponse = sendLoginRequest(body);
        verifyIncorrectUserDataUnauthorized(loginResponse);
    }

    @Step("Проверка ошибки при авторизации с неверным логином или паролем: статус 401 и success=false")
    private void verifyIncorrectUserDataUnauthorized(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("email or password are incorrect"));
    }

    private static Stream<Arguments> userBodyProviderForLogin() {
        Faker faker = new Faker(new Locale("ru"));
        return Stream.of(
                Arguments.of(faker.internet().emailAddress(), password),
                Arguments.of(email, faker.internet().password()),
                Arguments.of(email, ""),
                Arguments.of("", password)
        );
    }
}
