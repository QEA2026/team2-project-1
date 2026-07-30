package com.revature.e2e;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AuthRedirectTest extends BaseSeleniumTest {

    @ParameterizedTest
    @ValueSource(strings = { "chrome", "firefox", "edge" })
    void visitingPendingPageWithoutLoginRedirectsToIndex(String browser) {
        setUpDriver(browser);

        driver.get(BASE_URL + "/pending.html");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(webDriver -> webDriver.getCurrentUrl().endsWith("index.html"));

        assertTrue(driver.getCurrentUrl().endsWith("index.html"));
    }
}