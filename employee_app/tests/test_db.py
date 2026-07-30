import sqlite3
import pytest
from db import db

@pytest.fixture
def temporary_database(tmp_path, monkeypatch):
    database_path = tmp_path / "test_expenses.db"
    monkeypatch.setattr(db, "DB_PATH", database_path)
    db.init_db()
    return database_path


def test_init_db_creates_expected_tables(temporary_database):
    with sqlite3.connect(temporary_database) as connection:
        rows = connection.execute(
            """
            SELECT name FROM sqlite_master
            WHERE type = 'table'
            """
        ).fetchall()

    table_names = {row[0] for row in rows}
    assert {"users", "expenses", "approvals"} <= table_names


def test_init_db_can_be_called_more_than_once(temporary_database):
    db.init_db()

    with sqlite3.connect(temporary_database) as connection:
        table_count = connection.execute(
            """
            SELECT COUNT(*) FROM sqlite_master
            WHERE type = 'table'
            AND name IN ('users', 'expenses', 'approvals')
            """
        ).fetchone()[0]

    assert table_count == 3


def test_get_connection_uses_row_factory(tmp_path, monkeypatch):
    monkeypatch.setattr(db, "DB_PATH", tmp_path / "rows.db")

    connection = db.get_connection()
    try:
        assert connection.row_factory is sqlite3.Row
    finally:
        connection.close()


def test_users_username_must_be_unique(temporary_database):
    with sqlite3.connect(temporary_database) as connection:
        connection.execute(
            "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
            ("mario", "secret", "EMPLOYEE"),
        )

        with pytest.raises(sqlite3.IntegrityError):
            connection.execute(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                ("mario", "different", "MANAGER"),
            )


def test_users_role_must_be_valid(temporary_database):
    with sqlite3.connect(temporary_database) as connection:
        with pytest.raises(sqlite3.IntegrityError):
            connection.execute(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)",
                ("alice", "secret", "Administrator"),
            )


def test_approval_status_must_be_valid(temporary_database):
    with sqlite3.connect(temporary_database) as connection:
        with pytest.raises(sqlite3.IntegrityError):
            connection.execute(
                """
                INSERT INTO approvals
                    (expense_id, status, reviewer_id, comment, review_date)
                VALUES (?, ?, ?, ?, ?)
                """,
                (1, "unknown", None, None, None),
            )