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

public class RegisterUserTests {

    private String uniqueEmail;
    private String uniquePassword;
    private String uniqueName;
    private static String uniqueUserToken;

    private String existedEmail;
    private String existedPassword;
    private String existedName;
    private static String existedUserToken;

    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();

    @BeforeAll
    static void setUp() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
    }

    @BeforeEach
    void prepareTestData() {
        uniqueEmail = faker.internet().emailAddress();
        uniquePassword = faker.internet().password();
        uniqueName = faker.name().firstName();

        existedEmail = faker.internet().emailAddress();
        existedPassword = faker.internet().password();
        existedName = faker.name().firstName();
    }

    @AfterAll
    static void clearTestData() {
        tdh.deleteUser(uniqueUserToken);
        tdh.deleteUser(existedUserToken);
    }

    @Test
    @DisplayName("Проверяю, что можно создать уникального пользователя")
    void checkUniqueUserCanBeRegistered() {
        User uniqueUser = new User(uniqueEmail, uniquePassword, uniqueName);
        Response registerResponse = sendRegisterRequest(uniqueUser);
        verifyUserRegistered(registerResponse);
        uniqueUserToken = registerResponse
                .then()
                .extract()
                .jsonPath()
                .getString("accessToken")
                .replaceAll("Bearer ", "");
    }


    @Step("Отправка POST запрос на /api/auth/register")
    private Response sendRegisterRequest(User body) {
        return given()
                .header("Content-type", "application/json")
                .body(body)
                .post("/api/auth/register");
    }

    @Step("Проверка успешного создания уникального пользователя: статус 200 и success=true")
    private void verifyUserRegistered(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать пользователя, который уже зарегистрирован")
    void checkExistedUserCanNotBeRegistered() {
        existedUserToken = tdh.createTestUser(existedEmail, existedPassword, existedName);
        User existedUser = new User(existedEmail, existedPassword, existedName);
        Response registerResponse = sendRegisterRequest(existedUser);
        verifyUserRegistrationForbidden(registerResponse);
    }

    @Step("Проверка ошибки создания существующего пользователя: статус 403 и success=false")
    private void verifyUserRegistrationForbidden(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @ParameterizedTest
    @MethodSource("userBodyProviderForCreate")
    @DisplayName("Проверяю, что нельзя создать пользователя и не заполнить одно из обязательных полей")
    void checkUserCanNotBeRegisteredWithoutRequiredField(String email, String password, String name) {
        User malformedUser = new User(email, password, name);
        Response registerResponse = sendRegisterRequest(malformedUser);
        verifyMalformedUserRegistrationForbidden(registerResponse);
    }

    @Step("Проверка ошибки создания пользователя без заполнения одного обязательного поля: статус 403 и success=false")
    private void verifyMalformedUserRegistrationForbidden(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

    private static Stream<Arguments> userBodyProviderForCreate() {
        Faker faker = new Faker(new Locale("ru"));
        return Stream.of(
                Arguments.of("", faker.internet().password(), faker.name().firstName()),
                Arguments.of(faker.internet().emailAddress(), "", faker.name().firstName()),
                Arguments.of(faker.internet().emailAddress(), faker.internet().password(), "")
        );
    }
}
