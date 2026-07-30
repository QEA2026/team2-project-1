from unittest.mock import MagicMock

from controllers import expenses
from models.expenses import Expense


def connection_with_cursor(monkeypatch, cursor):
    connection = MagicMock()
    connection.execute.return_value = cursor
    monkeypatch.setattr(expenses, "get_connection", lambda: connection)
    return connection


def make_expense(expense_id=1):
    return Expense(
        id=expense_id,
        user_id=10,
        amount=25.5,
        description="Lunch",
        date="2026-07-27",
    )


def test_create_inserts_expense_and_assigns_id(monkeypatch):
    cursor = MagicMock(lastrowid=4)
    connection = connection_with_cursor(monkeypatch, cursor)
    expense = make_expense(expense_id=None)

    result = expenses.create(expense)

    assert result is expense
    assert expense.id == 4
    assert connection.execute.call_args.args[1] == (
        10,
        25.5,
        "Lunch",
        "2026-07-27",
    )
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_edit_updates_and_returns_expense(monkeypatch):
    cursor = MagicMock(rowcount=1)
    connection = connection_with_cursor(monkeypatch, cursor)
    expense = make_expense()

    result = expenses.edit(expense)

    assert result is expense
    assert connection.execute.call_args.args[1] == (
        25.5,
        "Lunch",
        "2026-07-27",
        1,
    )
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_edit_returns_none_when_no_row_is_updated(monkeypatch):
    cursor = MagicMock(rowcount=0)
    connection = connection_with_cursor(monkeypatch, cursor)

    assert expenses.edit(make_expense(expense_id=99)) is None
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_remove_deletes_requested_expense(monkeypatch):
    connection = MagicMock()
    monkeypatch.setattr(expenses, "get_connection", lambda: connection)

    result = expenses.remove(8)

    assert result is None
    assert connection.execute.call_args.args[1] == (8,)
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_get_all_returns_expenses(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = [
        (1, 10, 25.5, "Lunch", "2026-07-27"),
        (2, 10, 12.0, "Taxi", "2026-07-28"),
    ]
    connection_with_cursor(monkeypatch, cursor)

    result = expenses.get_all()

    assert [expense.__dict__ for expense in result] == [
        {
            "id": 1,
            "user_id": 10,
            "amount": 25.5,
            "description": "Lunch",
            "date": "2026-07-27",
        },
        {
            "id": 2,
            "user_id": 10,
            "amount": 12.0,
            "description": "Taxi",
            "date": "2026-07-28",
        },
    ]


def test_get_all_returns_empty_list(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    connection_with_cursor(monkeypatch, cursor)

    assert expenses.get_all() == []


def test_get_all_by_user_filters_by_user_id(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = [(1, 10, 25.5, "Lunch", "2026-07-27")]
    connection = connection_with_cursor(monkeypatch, cursor)

    result = expenses.get_all_by_user(10)

    assert len(result) == 1
    assert result[0].user_id == 10
    assert connection.execute.call_args.args[1] == (10,)
    connection.close.assert_called_once_with()


def test_get_all_by_user_returns_empty_list(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    connection_with_cursor(monkeypatch, cursor)

    assert expenses.get_all_by_user(99) == []


def test_get_all_non_pending_user_returns_expenses(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = [
        {
            "id": 1,
            "user_id": 10,
            "amount": 25.5,
            "description": "Lunch",
            "date": "2026-07-27",
        }
    ]
    connection = connection_with_cursor(monkeypatch, cursor)

    result = expenses.get_all_non_pending_user(10)

    assert result[0].id == 1
    assert connection.execute.call_args.args[1] == (10,)
    query = " ".join(connection.execute.call_args.args[0].split()).upper()
    assert "UPPER(A.STATUS) != 'PENDING'" in query
    connection.close.assert_called_once_with()


def test_get_all_non_pending_user_returns_empty_list(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    connection_with_cursor(monkeypatch, cursor)

    assert expenses.get_all_non_pending_user(10) == []


def test_get_from_id_returns_expense(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = (1, 10, 25.5, "Lunch", "2026-07-27")
    connection = connection_with_cursor(monkeypatch, cursor)

    result = expenses.get_from_id(1)

    assert result.__dict__ == make_expense().__dict__
    assert connection.execute.call_args.args[1] == (1,)
    connection.close.assert_called_once_with()


def test_get_from_id_returns_none(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = None
    connection_with_cursor(monkeypatch, cursor)

    assert expenses.get_from_id(99) is None
