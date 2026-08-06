from datetime import date
from behave import given, then, when
from selenium.webdriver.common.by import By
from selenium.webdriver.support import expected_conditions as conditions
from selenium.webdriver.support.ui import WebDriverWait

TIMEOUT = 10


def wait(context):
    return WebDriverWait(context.driver, TIMEOUT)


def expense_row(context, description):
    return wait(context).until(
        conditions.presence_of_element_located(
            (
                By.XPATH,
                f"//tbody[@id='expense-rows']/tr[.//strong[normalize-space()={description!r}]]",
            )
        )
    )


@given("I am on the employee login page")
def open_login(context):
    context.driver.get(context.base_url)
    wait(context).until(conditions.visibility_of_element_located((By.ID, "login-form")))


@when('I log in as employee "{username}" with password "{password}"')
def log_in(context, username, password):
    context.driver.find_element(By.ID, "username").send_keys(username)
    context.driver.find_element(By.ID, "password").send_keys(password)
    context.driver.find_element(By.CSS_SELECTOR, "#login-form button[type='submit']").click()


@then("I should see the employee expense dashboard")
def see_dashboard(context):
    wait(context).until(conditions.url_contains("dashboard.html"))
    wait(context).until(conditions.visibility_of_element_located((By.ID, "expense-rows")))
    assert "My expenses" in context.driver.page_source


@then("I should remain on the employee login page")
def remain_on_login(context):
    wait(context).until(conditions.visibility_of_element_located((By.ID, "login-form")))
    assert "dashboard.html" not in context.driver.current_url


@then("I should see a login error")
def see_login_error(context):
    error = wait(context).until(
        conditions.visibility_of_element_located((By.ID, "login-error"))
    )
    assert error.text.strip()


@when("I open the new expense form")
def open_expense_form(context):
    wait(context).until(
        conditions.element_to_be_clickable((By.ID, "new-expense-button"))
    ).click()
    wait(context).until(
        conditions.visibility_of_element_located((By.ID, "expense-dialog"))
    )


@when('I submit an expense for "{description}" costing "{amount}"')
def submit_expense(context, description, amount):
    open_expense_form(context)
    context.expense_description = description
    context.driver.find_element(By.ID, "expense-description").send_keys(description)
    context.driver.find_element(By.ID, "expense-amount").send_keys(amount)
    date_field = context.driver.find_element(By.ID, "expense-date")
    context.driver.execute_script(
        "arguments[0].value = arguments[1];"
        "arguments[0].dispatchEvent(new Event('change', {bubbles: true}));",
        date_field,
        date.today().isoformat(),
    )
    context.driver.find_element(By.ID, "save-expense-button").click()
    wait(context).until(
        conditions.invisibility_of_element_located((By.ID, "expense-dialog"))
    )


@then('I should see "{description}" with status "{status}"')
def see_expense_status(context, description, status):
    row = expense_row(context, description)
    badge = row.find_element(By.CSS_SELECTOR, ".status")
    assert badge.text.lower() == status.lower()


@when('a manager approves "{description}"')
def manager_approves(context, description):
    context.update_expense_status(description, "approved")


@when("I refresh the expense dashboard")
def refresh_dashboard(context):
    context.driver.refresh()
    wait(context).until(conditions.visibility_of_element_located((By.ID, "expense-rows")))


@when("I submit the expense form without an amount")
def submit_without_amount(context):
    context.driver.find_element(By.ID, "expense-description").send_keys(
        "Incomplete E2E expense"
    )
    context.driver.find_element(By.ID, "save-expense-button").click()


@then("I should see the expense validation message")
def see_expense_validation(context):
    validation = wait(context).until(
        conditions.visibility_of_element_located((By.ID, "expense-form-message"))
    )
    assert "Complete every field" in validation.text


@then("the expense form should remain open")
def expense_form_open(context):
    dialog = context.driver.find_element(By.ID, "expense-dialog")
    assert dialog.get_attribute("open") is not None


@when("I clear my employee session")
def clear_session(context):
    context.driver.execute_script("window.sessionStorage.clear();")


@when("I open the expense dashboard directly")
def open_dashboard_directly(context):
    context.driver.get(f"{context.base_url}/dashboard.html")


@then("I should be redirected to the employee login page")
def redirected_to_login(context):
    wait(context).until(conditions.visibility_of_element_located((By.ID, "login-form")))
    assert context.driver.current_url.rstrip("/") == context.base_url
