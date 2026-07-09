package service;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;

public abstract  class RestAssuredTests {
    @BeforeAll
    static void setUpBase() {
        RestAssured.baseURI = "https://stellarburgers.education-services.ru/";
    }
}
