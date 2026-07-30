package com.revature.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ExpenseApiTest extends BaseApiTest {

    @BeforeEach
    void authenticateAsManager() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"username\": \"manager\", \"password\": \"admin123\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("Get pending expenses should return list of pending expenses")
    void getPendingExpensesShouldReturnListOfPendingExpenses() {
        given()
        .when()
            .get("/expenses/pending")
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get pending expenses should return unauthorized without session")
    void getPendingExpensesShouldReturnUnauthorizedWithoutSession() {
        // Clear cookies to simulate unauthenticated request
        RestAssured.reset();
        RestAssured.baseURI = BaseApiTest.BASE_URL;
        
        given()
        .when()
            .get("/expenses/pending")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }

    @Test
    @DisplayName("Decide expense should approve with valid request")
    void decideExpenseShouldApproveWithValidRequest() {
        int expenseId = 1;
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"approve\": true, \"comment\": \"Approved for testing\"}")
        .when()
            .post("/expenses/" + expenseId + "/decision")
        .then()
            .statusCode(anyOf(
                    equalTo(200),
                    equalTo(409)
            ))
            .body(notNullValue());
    }

    @Test
    @DisplayName("Decide expense should deny with valid request")
    void decideExpenseShouldDenyWithValidRequest() {
        int expenseId = 1;
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"approve\": false, \"comment\": \"Denied for testing\"}")
        .when()
            .post("/expenses/" + expenseId + "/decision")
        .then()
            .statusCode(anyOf(
                    equalTo(200),
                    equalTo(409)
            ))
            .body(notNullValue());
    }

    @Test
    @DisplayName("Decide expense should return bad request with invalid expense id")
    void decideExpenseShouldReturnBadRequestWithInvalidExpenseId() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"approve\": true, \"comment\": \"Approved\"}")
        .when()
            .post("/expenses/invalid/decision")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid expense id"));
    }

    @Test
    @DisplayName("Decide expense should require authentication")
    void decideExpenseShouldRequireAuthentication() {
        // Clear cookies to simulate unauthenticated request
        RestAssured.reset();
        RestAssured.baseURI = BaseApiTest.BASE_URL;
        
        given()
            .contentType(ContentType.JSON)
            .body("{\"approve\": true, \"comment\": \"Approved\"}")
        .when()
            .post("/expenses/1/decision")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }
}
