from unittest.mock import MagicMock

from controllers import approvals
from models.approvals import Approval


def connection_with_cursor(monkeypatch, cursor):
    connection = MagicMock()
    connection.execute.return_value = cursor
    monkeypatch.setattr(approvals, "get_connection", lambda: connection)
    return connection


def approval_row():
    return (1, 10, "approved", 2, "Looks good", "2026-07-27")


def test_create_inserts_approval_and_assigns_id(monkeypatch):
    cursor = MagicMock(lastrowid=5)
    connection = connection_with_cursor(monkeypatch, cursor)
    approval = Approval(
        expense_id=10,
        status="approved",
        reviewer=2,
        comment="Looks good",
        review_date="2026-07-27",
    )

    result = approvals.create(approval)

    assert result is approval
    assert approval.id == 5
    assert connection.execute.call_args.args[1] == (
        10,
        "approved",
        2,
        "Looks good",
        "2026-07-27",
    )
    connection.commit.assert_called_once_with()
    connection.close.assert_called_once_with()


def test_get_all_returns_approvals(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = [approval_row()]
    connection = connection_with_cursor(monkeypatch, cursor)

    result = approvals.get_all()

    assert [approval.__dict__ for approval in result] == [
        {
            "id": 1,
            "expense_id": 10,
            "status": "approved",
            "reviewer": 2,
            "comment": "Looks good",
            "review_date": "2026-07-27",
        }
    ]
    connection.close.assert_called_once_with()


def test_get_all_returns_empty_list(monkeypatch):
    cursor = MagicMock()
    cursor.fetchall.return_value = []
    connection_with_cursor(monkeypatch, cursor)

    assert approvals.get_all() == []


def test_get_from_id_returns_approval(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = approval_row()
    connection = connection_with_cursor(monkeypatch, cursor)

    result = approvals.get_from_id(1)

    assert result.id == 1
    assert result.expense_id == 10
    assert connection.execute.call_args.args[1] == (1,)
    connection.close.assert_called_once_with()


def test_get_from_id_returns_none(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = None
    connection_with_cursor(monkeypatch, cursor)

    assert approvals.get_from_id(99) is None


def test_get_from_expenseid_returns_approval(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = approval_row()
    connection = connection_with_cursor(monkeypatch, cursor)

    result = approvals.get_from_expenseid(10)

    assert result.expense_id == 10
    assert connection.execute.call_args.args[1] == (10,)
    connection.close.assert_called_once_with()


def test_get_from_expenseid_returns_none(monkeypatch):
    cursor = MagicMock()
    cursor.fetchone.return_value = None
    connection_with_cursor(monkeypatch, cursor)

    assert approvals.get_from_expenseid(99) is None
