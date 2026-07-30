from unittest.mock import MagicMock

from controllers import users
from models.users import User


def connection_with_cursor(monkeypatch, cursor):
    connection = MagicMock()
    connection.execute.return_value = cursor
    monkeypatch.setattr(users, "get_connection", lambda: connection)
    return connection


def test_create_inserts_user_and_assigns_id(monkeypatch):
    cursor = MagicMock(lastrowid=7)
    connection = connection_with_cursor(monkeypatch, cursor)
    user = User(username="alice", password="secret", role="Employee")

    result = users.create(user)

    assert result is user
    assert user.id == 7
    assert connection.execute.call_args.args[1] == (
        "alice",
        "secret",
        "Employee",
    )
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_get_all_returns_users(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = [
        (1, "alice", "secret", "Employee"),
        (2, "manager", "admin", "Manager"),
    ]
    connection = connection_with_cursor(monkeypatch, cursor)

    result = users.get_all()

    assert [user.__dict__ for user in result] == [
        {
            "id": 1,
            "username": "alice",
            "password": "secret",
            "role": "Employee",
        },
        {
            "id": 2,
            "username": "manager",
            "password": "admin",
            "role": "Manager",
        },
    ]
    connection.close.assert_called_once_with()


def test_get_all_returns_empty_list(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    connection_with_cursor(monkeypatch, cursor)

    assert users.get_all() == []


def test_get_from_id_returns_user(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = (3, "bob", "password", "Employee")
    connection = connection_with_cursor(monkeypatch, cursor)

    result = users.get_from_id(3)

    assert result.__dict__ == {
        "id": 3,
        "username": "bob",
        "password": "password",
        "role": "Employee",
    }
    assert connection.execute.call_args.args[1] == (3,)
    connection.close.assert_called_once_with()


def test_get_from_id_returns_none(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = None
    connection_with_cursor(monkeypatch, cursor)

    assert users.get_from_id(99) is None


def test_get_from_username_password_returns_user(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = (1, "alice", "secret", "Employee")
    connection = connection_with_cursor(monkeypatch, cursor)

    result = users.get_from_username_password("alice", "secret")

    assert result.username == "alice"
    assert connection.execute.call_args.args[1] == ("alice", "secret")
    connection.close.assert_called_once_with()


def test_get_from_username_password_returns_none(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = None
    connection_with_cursor(monkeypatch, cursor)

    assert users.get_from_username_password("alice", "wrong") is None
