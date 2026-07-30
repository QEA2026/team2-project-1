package com.revature.e2e;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PendingExpensesTest extends BaseSeleniumTest {

    private void loginAsManager() {
        driver.get(BASE_URL + "/index.html");
        driver.findElement(By.id("username")).sendKeys("manager");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("login-button")).click();

        new WebDriverWait(driver, Duration.ofSeconds(3))
                .until(webDriver -> webDriver.getCurrentUrl().endsWith("pending.html"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "firefox", "edge" })
    void pendingTableLoadsAfterLogin(String browser) {
        setUpDriver(browser);
        loginAsManager();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(webDriver ->
                !webDriver.findElements(By.cssSelector("#pending-table-body tr")).isEmpty());

        List<WebElement> rows = driver.findElements(By.cssSelector("#pending-table-body tr"));

        assertFalse(rows.isEmpty(), "Expected at least one row in the pending table");
    }
}