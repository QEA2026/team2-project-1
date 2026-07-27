from models.expenses import Expense


def expense(
    expense_id=1,
    user_id=10,
    amount=25.5,
    description="Lunch",
    date="2026-07-27",
):
    return Expense(
        id=expense_id,
        user_id=user_id,
        amount=amount,
        description=description,
        date=date,
    )


def test_create_expense(client, monkeypatch):
    def fake_create(new_expense):
        new_expense.id = 1
        return new_expense

    monkeypatch.setattr("app.expenses.create", fake_create)

    response = client.post(
        "/expenses",
        json={
            "user_id": 10,
            "amount": 25.5,
            "description": "Lunch",
            "date": "2026-07-27",
        },
    )

    assert response.status_code == 201
    assert response.get_json() == {
        "id": 1,
        "user_id": 10,
        "amount": 25.5,
        "description": "Lunch",
        "date": "2026-07-27",
    }


def test_edit_expense(client, monkeypatch):
    monkeypatch.setattr("app.expenses.edit", lambda updated: updated)

    response = client.put(
        "/expenses/1",
        json={
            "user_id": 10,
            "amount": 30,
            "description": "Team lunch",
            "date": "2026-07-28",
        },
    )

    assert response.status_code == 200
    assert response.get_json() == {
        "id": 1,
        "user_id": 10,
        "amount": 30,
        "description": "Team lunch",
        "date": "2026-07-28",
    }


def test_edit_expense_not_found(client, monkeypatch):
    monkeypatch.setattr("app.expenses.edit", lambda updated: None)

    response = client.put(
        "/expenses/999",
        json={
            "amount": 30,
            "description": "Team lunch",
            "date": "2026-07-28",
        },
    )

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "Expense with ID 999 not found"
    }


def test_delete_expense(client, monkeypatch):
    removed_ids = []
    monkeypatch.setattr("app.expenses.get_from_id", lambda expense_id: expense())
    monkeypatch.setattr(
        "app.expenses.remove",
        lambda expense_id: removed_ids.append(expense_id),
    )

    response = client.delete("/expenses/1")

    assert response.status_code == 200
    assert response.get_json() == {
        "message": "Expense 1 successfully deleted"
    }
    assert removed_ids == [1]


def test_delete_expense_not_found(client, monkeypatch):
    monkeypatch.setattr("app.expenses.get_from_id", lambda expense_id: None)

    response = client.delete("/expenses/999")

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "Expense with ID 999 not found"
    }


def test_get_all_expenses(client, monkeypatch):
    monkeypatch.setattr("app.expenses.get_all", lambda: [expense()])

    response = client.get("/expenses")

    assert response.status_code == 200
    assert response.get_json() == [
        {
            "id": 1,
            "user_id": 10,
            "amount": 25.5,
            "description": "Lunch",
            "date": "2026-07-27",
        }
    ]


def test_get_expense_by_id(client, monkeypatch):
    monkeypatch.setattr(
        "app.expenses.get_from_id",
        lambda expense_id: expense(expense_id=expense_id),
    )

    response = client.get("/expenses/1")

    assert response.status_code == 200
    assert response.get_json()["id"] == 1


def test_get_expense_by_id_not_found(client, monkeypatch):
    monkeypatch.setattr("app.expenses.get_from_id", lambda expense_id: None)

    response = client.get("/expenses/999")

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "Expense with ID 999 not found"
    }


def test_get_expenses_by_user(client, monkeypatch):
    monkeypatch.setattr(
        "app.expenses.get_all_by_user",
        lambda user_id: [expense(user_id=user_id)],
    )

    response = client.get("/expenses/user/10")

    assert response.status_code == 200
    assert response.get_json()[0]["user_id"] == 10


def test_get_non_pending_user_expenses(client, monkeypatch):
    monkeypatch.setattr(
        "app.expenses.get_all_non_pending_user",
        lambda user_id: [expense(user_id=user_id)],
    )

    response = client.get("/expenses/user/10/history")

    assert response.status_code == 200
    assert response.get_json()[0]["user_id"] == 10

