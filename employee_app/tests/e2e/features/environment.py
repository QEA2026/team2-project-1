import os
import shutil
import socket
import sqlite3
import subprocess
import sys
import tempfile
import time
from http.client import HTTPConnection
from pathlib import Path

from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.edge.options import Options as EdgeOptions
from selenium.webdriver.firefox.options import Options as FirefoxOptions


EMPLOYEE_APP_DIR = Path(__file__).resolve().parents[3]


def _available_port():
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as listener:
        listener.bind(("127.0.0.1", 0))
        return listener.getsockname()[1]


def _wait_for_server(port, process, timeout=15):
    deadline = time.monotonic() + timeout
    while time.monotonic() < deadline:
        if process.poll() is not None:
            output = process.stdout.read() if process.stdout else ""
            raise RuntimeError(f"Employee app stopped during startup:\n{output}")
        try:
            connection = HTTPConnection("127.0.0.1", port, timeout=0.5)
            connection.request("GET", "/health")
            response = connection.getresponse()
            if response.status < 500:
                connection.close()
                return
            connection.close()
        except OSError:
            time.sleep(0.2)
    raise RuntimeError(f"Employee app did not start within {timeout} seconds.")


def _create_driver():
    browser = os.environ.get("E2E_BROWSER", "chrome").lower()
    headless = os.environ.get("E2E_HEADLESS", "1") != "0"

    if browser == "firefox":
        options = FirefoxOptions()
        if headless:
            options.add_argument("-headless")
        return webdriver.Firefox(options=options)

    if browser == "edge":
        options = EdgeOptions()
    elif browser == "chrome":
        options = ChromeOptions()
    else:
        raise ValueError(
            f'Unsupported E2E_BROWSER "{browser}". Use edge, chrome, or firefox.'
        )

    options.page_load_strategy = "eager"
    if headless:
        options.add_argument("--headless=new")
    options.add_argument("--window-size=1440,1000")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--no-sandbox")
    if browser == "edge":
        return webdriver.Edge(options=options)
    return webdriver.Chrome(options=options)


def before_all(context):
    context.temp_dir = Path(tempfile.mkdtemp(prefix="expenseflow-e2e-"))
    context.db_path = context.temp_dir / "expenses_system_db.db"
    context.port = _available_port()
    context.base_url = f"http://127.0.0.1:{context.port}"

    server_environment = os.environ.copy()
    server_environment.update(
        {
            "EXPENSE_DB_PATH": str(context.db_path),
            "FLASK_USE_RELOADER": "0",
            "EMPLOYEE_APP_PORT": str(context.port),
            "PYTHONUNBUFFERED": "1",
        }
    )
    context.server = subprocess.Popen(
        [sys.executable, "app.py"],
        cwd=EMPLOYEE_APP_DIR,
        env=server_environment,
        # A PIPE must be continuously drained. Leaving Flask's request logs in
        # an unread pipe eventually fills its Windows buffer and blocks the
        # server, which surfaces in Selenium as a renderer timeout.
        stdout=subprocess.DEVNULL,
        stderr=subprocess.DEVNULL,
    )

    try:
        _wait_for_server(context.port, context.server)
        context.driver = _create_driver()
        context.driver.set_page_load_timeout(30)
        context.update_expense_status = lambda description, status: update_expense_status(
            context, description, status
        )
    except Exception:
        context.server.terminate()
        context.server.wait(timeout=5)
        shutil.rmtree(context.temp_dir, ignore_errors=True)
        raise


def before_scenario(context, _scenario):
    context.expense_description = None


def after_scenario(context, scenario):
    if scenario.status == "failed":
        screenshot_path = context.temp_dir / f"{scenario.name.replace(' ', '_')}.png"
        context.driver.save_screenshot(str(screenshot_path))
        print(f"Failure screenshot: {screenshot_path}")


def after_all(context):
    if hasattr(context, "driver"):
        context.driver.quit()
    if hasattr(context, "server"):
        context.server.terminate()
        try:
            context.server.wait(timeout=5)
        except subprocess.TimeoutExpired:
            context.server.kill()
            context.server.wait(timeout=5)
    if hasattr(context, "temp_dir"):
        shutil.rmtree(context.temp_dir, ignore_errors=True)


def update_expense_status(context, description, status):
    with sqlite3.connect(context.db_path) as connection:
        expense = connection.execute(
            "SELECT id FROM expenses WHERE description = ? ORDER BY id DESC LIMIT 1",
            (description,),
        ).fetchone()
        if expense is None:
            raise AssertionError(f'Expense "{description}" was not found in the test database.')
        connection.execute(
            """
            UPDATE approvals
            SET status = ?, reviewer_id = 4, comment = ?, review_date = date('now')
            WHERE expense_id = ?
            """,
            (status.upper(), "Approved by the E2E manager fixture.", expense[0]),
        )
