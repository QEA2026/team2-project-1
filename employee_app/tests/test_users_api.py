from models.users import User

def test_create_user(client, monkeypatch):
    def fake_create(user):
        user.id = 1
        return user

    monkeypatch.setattr("app.users.create", fake_create)

    response = client.post(
        "/users",
        json={"username": "alice", "password": "secret", "role": "Employee"},
    )

    assert response.status_code == 201
    assert response.get_json() == {
        "id": 1,
        "username": "alice",
        "password": "secret",
        "role": "Employee",
    }


def test_get_all_users(client, monkeypatch):
    expected_users = [
        User(id=1, username="alice", password="secret", role="Employee"),
        User(id=2, username="manager", password="admin", role="Manager"),
    ]
    monkeypatch.setattr("app.users.get_all", lambda: expected_users)

    response = client.get("/users")

    assert response.status_code == 200
    assert response.get_json() == [
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


def test_get_user_by_id(client, monkeypatch):
    user = User(id=1, username="alice", password="secret", role="Employee")
    monkeypatch.setattr("app.users.get_from_id", lambda user_id: user)

    response = client.get("/users/1")

    assert response.status_code == 200
    assert response.get_json()["id"] == 1


def test_get_user_by_id_not_found(client, monkeypatch):
    monkeypatch.setattr("app.users.get_from_id", lambda user_id: None)

    response = client.get("/users/999")

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "No user records found for user ID 999"
    }


def test_login(client, monkeypatch):
    user = User(id=1, username="alice", password="secret", role="Employee")
    monkeypatch.setattr(
        "app.users.get_from_username_password",
        lambda username, password: user,
    )

    response = client.post(
        "/users/login",
        json={"username": "alice", "password": "secret"},
    )

    assert response.status_code == 200
    assert response.get_json()["username"] == "alice"


def test_login_not_found(client, monkeypatch):
    monkeypatch.setattr(
        "app.users.get_from_username_password",
        lambda username, password: None,
    )

    response = client.post(
        "/users/login",
        json={"username": "unknown", "password": "wrong"},
    )

    assert response.status_code == 404
    assert response.get_json() == {
        "error": "No user records found with given login"
    }

