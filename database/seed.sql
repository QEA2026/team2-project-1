PRAGMA foreign_keys = ON;

BEGIN TRANSACTION;

INSERT INTO users (id, username, password, role)
VALUES
    (1, 'alice', 'password123', 'EMPLOYEE'),
    (2, 'bob', 'password123', 'EMPLOYEE'),
    (3, 'carla', 'password123', 'EMPLOYEE'),
    (4, 'manager', 'admin123', 'MANAGER');

INSERT INTO expenses (id, user_id, amount, description, date)
VALUES
    (1, 1, 18.40, 'Parking for client visit', '2026-07-01'),
    (2, 1, 96.75, 'Team dinner', '2026-07-02'),
    (3, 2, 245.00, 'Professional certification exam', '2026-07-03'),
    (4, 2, 34.99, 'Laptop charging adapter', '2026-07-05'),
    (5, 3, 72.30, 'Mileage reimbursement', '2026-07-06'),
    (6, 1, 510.00, 'Flight to regional office', '2026-07-08'),
    (7, 1, 14.25, 'Airport breakfast', '2026-07-09'),
    (8, 2, 129.50, 'Ergonomic keyboard', '2026-07-10'),
    (9, 2, 48.60, 'Customer presentation printing', '2026-07-12'),
    (10, 3, 210.00, 'Training workshop', '2026-07-13'),
    (11, 1, 27.80, 'Rideshare to partner office', '2026-07-15'),
    (12, 1, 64.15, 'Internet service while traveling', '2026-07-16'),
    (13, 2, 11.90, 'Shipping project documents', '2026-07-18'),
    (14, 2, 385.00, 'Two-night conference lodging', '2026-07-20'),
    (15, 3, 53.45, 'Client meeting refreshments', '2026-07-22');

INSERT INTO approvals (id, expense_id, status, reviewer_id, comment, review_date)
VALUES
    (1, 1, 'APPROVED', 4, 'Approved client travel cost.', '2026-07-02'),
    (2, 2, 'PENDING', NULL, NULL, NULL),
    (3, 3, 'DENIED', 4, 'Certification was not pre-approved.', '2026-07-04'),
    (4, 4, 'PENDING', NULL, NULL, NULL),
    (5, 5, 'APPROVED', 4, 'Mileage documentation verified.', '2026-07-07'),
    (6, 6, 'APPROVED', 4, 'Approved business travel.', '2026-07-09'),
    (7, 7, 'PENDING', NULL, NULL, NULL),
    (8, 8, 'DENIED', 4, 'Use the standard equipment request process.', '2026-07-11'),
    (9, 9, 'PENDING', NULL, NULL, NULL),
    (10, 10, 'APPROVED', 4, 'Workshop supports current project needs.', '2026-07-14'),
    (11, 11, 'APPROVED', 4, 'Approved local transportation.', '2026-07-16'),
    (12, 12, 'PENDING', NULL, NULL, NULL),
    (13, 13, 'DENIED', 4, 'Shipping receipt was not attached.', '2026-07-19'),
    (14, 14, 'PENDING', NULL, NULL, NULL),
    (15, 15, 'APPROVED', 4, 'Approved client meeting expense.', '2026-07-23');

COMMIT;
