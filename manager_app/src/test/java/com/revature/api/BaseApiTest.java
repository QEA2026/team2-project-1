package com.revature.api;

import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

public class BaseApiTest {

    protected static final String BASE_URL = "http://localhost:7000";

    @BeforeAll
    static void setUpRestAssured() {
        RestAssured.baseURI = BASE_URL;
    }

    @BeforeEach
    void resetCookies() {
        // Reset cookies before each test
        RestAssured.reset();
        RestAssured.baseURI = BASE_URL;

        // Preserve the Javalin session cookie between requests in the same test.
        // Tests that call RestAssured.reset() themselves remain unauthenticated.
        RestAssured.filters(new SessionFilter());
    }
}
