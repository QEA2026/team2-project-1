import sys
import pytest
from pathlib import Path

EMPLOYEE_APP_DIR = Path(__file__).resolve().parents[1]
if str(EMPLOYEE_APP_DIR) not in sys.path:
    sys.path.insert(0, str(EMPLOYEE_APP_DIR))

from app import app as flask_app


@pytest.fixture
def app():
    flask_app.config.update(
        TESTING=True,
    )
    yield flask_app


@pytest.fixture
def client(app):
    return app.test_client()