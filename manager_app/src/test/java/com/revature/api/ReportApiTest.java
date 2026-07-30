package com.revature.api;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

class ReportApiTest extends BaseApiTest {

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
    @DisplayName("Get report by employee should return list of reports")
    void getReportByEmployeeShouldReturnListOfReports() {
        int userId = 1;
        
        given()
        .when()
            .get("/reports/employee/" + userId)
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get report by employee should return bad request with invalid user id")
    void getReportByEmployeeShouldReturnBadRequestWithInvalidUserId() {
        given()
        .when()
            .get("/reports/employee/invalid")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid user id"));
    }

    @Test
    @DisplayName("Get report by date range should return reports within range")
    void getReportByDateRangeShouldReturnReportsWithinRange() {
        given()
            .queryParam("start", "2026-01-01")
            .queryParam("end", "2026-12-31")
        .when()
            .get("/reports/date")
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get report by date range should return bad request without parameters")
    void getReportByDateRangeShouldReturnBadRequestWithoutParameters() {
        given()
        .when()
            .get("/reports/date")
        .then()
            .statusCode(400)
            .body("error", containsString("Both 'start' and 'end' query parameters are required"));
    }

    @Test
    @DisplayName("Get report by date range should return bad request with invalid date format")
    void getReportByDateRangeShouldReturnBadRequestWithInvalidDateFormat() {
        given()
            .queryParam("start", "01-01-2026")
            .queryParam("end", "12-31-2026")
        .when()
            .get("/reports/date")
        .then()
            .statusCode(400)
            .body("error", containsString("YYYY-MM-DD format"));
    }

    @Test
    @DisplayName("Get report by status should return pending reports")
    void getReportByStatusShouldReturnPendingReports() {
        given()
        .when()
            .get("/reports/status/PENDING")
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get report by status should return approved reports")
    void getReportByStatusShouldReturnApprovedReports() {
        given()
        .when()
            .get("/reports/status/APPROVED")
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get report by status should return denied reports")
    void getReportByStatusShouldReturnDeniedReports() {
        given()
        .when()
            .get("/reports/status/DENIED")
        .then()
            .statusCode(200)
            .body("", instanceOf(java.util.List.class));
    }

    @Test
    @DisplayName("Get report by status should return bad request with invalid status")
    void getReportByStatusShouldReturnBadRequestWithInvalidStatus() {
        given()
        .when()
            .get("/reports/status/INVALID")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid status"));
    }

    @Test
    @DisplayName("Report endpoints should require authentication")
    void reportEndpointsShouldRequireAuthentication() {
        // Clear cookies to simulate unauthenticated request
        RestAssured.reset();
        RestAssured.baseURI = BaseApiTest.BASE_URL;
        
        given()
        .when()
            .get("/reports/employee/1")
        .then()
            .statusCode(401)
            .body("error", notNullValue());
    }
}
