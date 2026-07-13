PRAGMA foreign_keys = ON;

CREATE TABLE users (
    id INTEGER PRIMARY KEY,
    username TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL,
    role TEXT NOT NULL CHECK(role IN ('EMPLOYEE', 'MANAGER'))
);

CREATE TABLE expenses (
    id INTEGER PRIMARY KEY,
    user_id INTEGER NOT NULL,
    amount REAL NOT NULL,
    description TEXT,
    date TEXT NOT NULL,

    FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE RESTRICT
);

CREATE TABLE approvals (
    id INTEGER PRIMARY KEY,
    expense_id INTEGER NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    reviewer_id INTEGER,
    comment TEXT,
    review_date TEXT,

    FOREIGN KEY (expense_id)
        REFERENCES expenses(id)
        ON DELETE CASCADE,
    
    FOREIGN KEY (reviewer_id)
        REFERENCES users(id)
        ON DELETE SET NULL,

    CHECK(status IN ('PENDING', 'APPROVED', 'DENIED'))
);