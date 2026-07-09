import service.TestDataHandler;
import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import model.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import service.UserApiClient;

import java.util.Locale;
import java.util.stream.Stream;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.core.IsEqual.equalTo;

public class RegisterUserTests extends RestAssuredTests {

    private String uniqueEmail;
    private String uniquePassword;
    private String uniqueName;

    private String existedEmail;
    private String existedPassword;
    private String existedName;

    private String userTokenForCleanup;

    Faker faker = new Faker(new Locale("ru"));
    static TestDataHandler tdh = new TestDataHandler();
    UserApiClient userClient = new UserApiClient();

    @BeforeEach
    void prepareTestData() {
        uniqueEmail = faker.internet().emailAddress();
        uniquePassword = faker.internet().password();
        uniqueName = faker.name().firstName();

        existedEmail = faker.internet().emailAddress();
        existedPassword = faker.internet().password();
        existedName = faker.name().firstName();

        userTokenForCleanup = null;
    }

    @AfterEach
    void clearTestData() {
        tdh.deleteUser(userTokenForCleanup);
    }

    @Test
    @DisplayName("Проверяю, что можно создать уникального пользователя")
    void checkUniqueUserCanBeRegistered() {
        User uniqueUser = new User(uniqueEmail, uniquePassword, uniqueName);
        Response registerResponse = userClient.registerUser(uniqueUser);
        verifyUserRegistered(registerResponse);

        userTokenForCleanup = registerResponse
                .then()
                .extract()
                .jsonPath()
                .getString("accessToken")
                .replaceAll("Bearer ", "");
    }

    @Test
    @DisplayName("Проверяю, что нельзя создать пользователя, который уже зарегистрирован")
    void checkExistedUserCanNotBeRegistered() {
        userTokenForCleanup = tdh.createTestUser(existedEmail, existedPassword, existedName);
        User existedUser = new User(existedEmail, existedPassword, existedName);
        Response registerResponse = userClient.registerUser(existedUser);
        verifyUserRegistrationForbidden(registerResponse);
    }

    @ParameterizedTest
    @MethodSource("userBodyProviderForCreate")
    @DisplayName("Проверяю, что нельзя создать пользователя и не заполнить одно из обязательных полей")
    void checkUserCanNotBeRegisteredWithoutRequiredField(String email, String password, String name) {
        User malformedUser = new User(email, password, name);
        Response registerResponse = userClient.registerUser(malformedUser);
        verifyMalformedUserRegistrationForbidden(registerResponse);
    }

    @Step("Проверка успешного создания уникального пользователя: статус 200 и success=true")
    private void verifyUserRegistered(Response response) {
        response.then()
                .statusCode(SC_OK)
                .assertThat().body("success", equalTo(true));
    }

    @Step("Проверка ошибки создания существующего пользователя: статус 403 и success=false")
    private void verifyUserRegistrationForbidden(Response response) {
        response.then()
                .statusCode(SC_FORBIDDEN)
                .assertThat()
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
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