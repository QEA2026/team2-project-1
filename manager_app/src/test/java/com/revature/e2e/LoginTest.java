package com.revature.e2e;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LoginTest extends BaseSeleniumTest {

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "firefox", "edge" })
    void managerCanLogInSuccessfully(String browser) {
        setUpDriver(browser);

        driver.get(BASE_URL + "/index.html");

        WebElement usernameField = driver.findElement(By.id("username"));
        WebElement passwordField = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameField.sendKeys("manager");
        passwordField.sendKeys("admin123");
        loginButton.click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(webDriver -> webDriver.getCurrentUrl().endsWith("pending.html"));

        WebElement pageHeading = driver.findElement(By.tagName("h1"));

        assertEquals("Pending Expenses", pageHeading.getText());
        assertTrue(driver.getCurrentUrl().endsWith("pending.html"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "edge" })
    void loginFailsWithWrongPassword(String browser) {
        setUpDriver(browser);

        driver.get(BASE_URL + "/index.html");

        driver.findElement(By.id("username")).sendKeys("manager");
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.id("login-button")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        WebElement errorMessage = wait.until(webDriver -> {
           WebElement el = webDriver.findElement(By.id("error-message"));
           return el.getText().isEmpty() ? null : el;
        });

        assertEquals("Invalid username or password", errorMessage.getText());
        assertTrue(driver.getCurrentUrl().endsWith("index.html"));
    }
}