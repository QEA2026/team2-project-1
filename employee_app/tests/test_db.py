from unittest.mock import MagicMock

import pytest

from db import db


@pytest.fixture
def rds_environment(monkeypatch):
    values = {
        "RDSHOST": "example.cluster.us-east-1.rds.amazonaws.com",
        "RDS_PORT": "5432",
        "RDS_DB_NAME": "expenses",
        "RDS_USERNAME": "app_user",
        "RDS_PASSWORD": "secret",
    }
    for name, value in values.items():
        monkeypatch.setenv(name, value)
    return values


def test_get_connection_uses_rds_configuration(monkeypatch, rds_environment):
    expected_connection = MagicMock()
    connect = MagicMock(return_value=expected_connection)
    monkeypatch.setattr(db.psycopg, "connect", connect)

    assert db.get_connection() is expected_connection
    connect.assert_called_once_with(
        host=rds_environment["RDSHOST"],
        port=rds_environment["RDS_PORT"],
        dbname=rds_environment["RDS_DB_NAME"],
        user=rds_environment["RDS_USERNAME"],
        password=rds_environment["RDS_PASSWORD"],
        sslmode="require",
        connect_timeout=10,
    )


def test_get_connection_rejects_missing_required_configuration(monkeypatch):
    monkeypatch.delenv("RDSHOST", raising=False)

    with pytest.raises(RuntimeError, match="RDSHOST"):
        db.get_connection()


def test_init_db_applies_schema_without_development_seed(monkeypatch):
    connection = MagicMock()
    connection.__enter__.return_value = connection
    monkeypatch.setattr(db, "get_connection", lambda: connection)

    db.init_db()

    executed_sql = connection.execute.call_args.args[0]
    assert "CREATE TABLE IF NOT EXISTS users" in executed_sql
    assert connection.execute.call_count == 1


def test_init_db_can_optionally_apply_development_seed(monkeypatch):
    connection = MagicMock()
    connection.__enter__.return_value = connection
    monkeypatch.setattr(db, "get_connection", lambda: connection)

    db.init_db(seed=True)

    assert connection.execute.call_count == 2
    assert "INSERT INTO users" in connection.execute.call_args_list[1].args[0]
