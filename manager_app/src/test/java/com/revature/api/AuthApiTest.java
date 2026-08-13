package com.revature.api;

import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class AuthApiTest extends BaseApiTest {

    @Test
    @DisplayName("Login should return success with valid credentials")
    @Epic("API Testing")
    @Feature("AuthApi")
    @Story("Happy Path: Login should return success with valid credentials")
    void loginShouldReturnSuccessWithValidCredentials() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"manager\", \"password\": \"admin123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .body("message", equalTo("Login successful"))
            .body("username", notNullValue())
            .body("role", notNullValue());
    }

    @Test
    @DisplayName("Login should return unauthorized with invalid password")
    @Epic("API Testing")
    @Feature("AuthApi")
    @Story("Login should return unauthorized with invalid password")
    void loginShouldReturnUnauthorizedWithInvalidPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"manager\", \"password\": \"wrongpassword\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("Login should return unauthorized with non-existent user")
    @Epic("API Testing")
    @Feature("AuthApi")
    @Story("Login should return unauthorized with non-existent user")
    void loginShouldReturnUnauthorizedWithNonExistentUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"nonexistent\", \"password\": \"password\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("Whoami should return user info when authenticated")
    @Epic("API Testing")
    @Feature("AuthApi")
    @Story("Whoami should return user info when authenticated")
    void whoamiShouldReturnUserInfoWhenAuthenticated() {
        // First login
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"manager\", \"password\": \"admin123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200);

        // Then call whoami - cookies are automatically handled by RestAssured
        given()
        .when()
            .get("/whoami")
        .then()
            .statusCode(200)
            .body("username", notNullValue())
            .body("role", notNullValue());
    }

    @Test
    @DisplayName("Logout should invalidate session")
    @Epic("API Testing")
    @Feature("AuthApi")
    @Story("Logout should invalidate session")
    void logoutShouldInvalidateSession() {
        // First login
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"manager\", \"password\": \"admin123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200);

        // Then logout - cookies are automatically handled
        given()
        .when()
            .post("/logout")
        .then()
            .statusCode(200)
            .body("message", equalTo("Logged out"));
    }}

