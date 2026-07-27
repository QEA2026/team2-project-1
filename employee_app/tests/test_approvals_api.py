from models.approvals import Approval


def test_get_all_approvals(client, monkeypatch):
    expected = [
        Approval(
            id=1,
            expense_id=10,
            status="approved",
            reviewer=2,
            comment="Approved",
            review_date="2026-07-27",
        )
    ]
    monkeypatch.setattr("app.approvals.get_all", lambda: expected)

    response = client.get("/approvals")

    assert response.status_code == 200
    assert response.get_json() == [
        {
            "id": 1,
            "expense_id": 10,
            "status": "approved",
            "reviewer": 2,
            "comment": "Approved",
            "review_date": "2026-07-27",
        }
    ]


def test_create_approval(client, monkeypatch):
    def fake_create(approval):
        approval.id = 1
        return approval

    monkeypatch.setattr("app.approvals.create", fake_create)

    response = client.post(
        "/approvals",
        json={
            "expense_id": 10,
            "status": "approved",
            "reviewer": 2,
            "comment": "Approved",
            "review_date": "2026-07-27",
        },
    )

    assert response.status_code == 201
    assert response.get_json() == {
        "id": 1,
        "expense_id": 10,
        "status": "approved",
        "reviewer": 2,
        "comment": "Approved",
        "review_date": "2026-07-27",
    }


def test_get_approval_by_id(client, monkeypatch):
    approval = Approval(id=1, expense_id=10, status="pending")
    monkeypatch.setattr("app.approvals.get_from_id", lambda approval_id: approval)

    response = client.get("/approvals/1")

    assert response.status_code == 200
    assert response.get_json()["id"] == 1


def test_get_approval_by_id_not_found(client, monkeypatch):
    monkeypatch.setattr("app.approvals.get_from_id", lambda approval_id: None)

    response = client.get("/approvals/999")

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "Approval with ID 999 not found"
    }


def test_get_approval_by_expense_id(client, monkeypatch):
    approval = Approval(id=1, expense_id=10, status="pending")
    monkeypatch.setattr(
        "app.approvals.get_from_expenseid",
        lambda expense_id: approval,
    )

    response = client.get("/approvals/expense/10")

    assert response.status_code == 200
    assert response.get_json()["expense_id"] == 10


def test_get_approval_by_expense_id_not_found(client, monkeypatch):
    monkeypatch.setattr(
        "app.approvals.get_from_expenseid",
        lambda expense_id: None,
    )

    response = client.get("/approvals/expense/999")

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "No approval records found for expense ID 999"
    }

