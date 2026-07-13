from db.db import get_connection
from models.expenses import Expense

def create(expense:Expense):
    conn = get_connection()
    cursor = conn.execute(
        """
        INSERT INTO expenses (user_id, amount, description, date)
        VALUES (?, ?, ?, ?)
        """,
        (expense.user_id, expense.amount, expense.description, expense.date)
    )

    expense.id = cursor.lastrowid
    conn.commit()
    conn.close()

    return expense


def edit(expense):
    conn = get_connection()
    cursor = conn.execute(
        """
        UPDATE expenses
        SET amount = ?, description = ?, date = ?
        WHERE id = ?
        """,
        (expense.amount, expense.description, expense.date, expense.id)
    )

    rows_updated = cursor.rowcount
    conn.commit()
    conn.close()    

    if rows_updated == 0:
        return None

    return expense


def remove(id:int):
    conn = get_connection()
    conn.execute(
        """
        DELETE FROM expenses WHERE id = ?
        """,
        (id,)
    )

    conn.commit()
    conn.close()

def get_all():
    conn = get_connection()
    cursor = conn.execute(
        """
        SELECT * FROM expenses
        """
    )

    rows = cursor.fetchall()
    expenses = []

    for row in rows:
        expenses.append(Expense(
            id=row[0],
            user_id=row[1],
            amount=row[2],
            description=row[3],
            date=row[4]
        ))

    conn.close()

    return expenses

def get_all_by_user(id:int):
    conn = get_connection()
    cursor = conn.execute(
        """
        SELECT * FROM expenses where user_id = ?
        """,
        (id,)
    )

    rows = cursor.fetchall()
    expenses = []

    for row in rows:
        expenses.append(Expense(
            id=row[0],
            user_id=row[1],
            amount=row[2],
            description=row[3],
            date=row[4]
        ))

    conn.close()

    return expenses

def get_all_non_pending_user(id:int):
    conn = get_connection()

    cursor = conn.execute(
        """
        SELECT e.* FROM expenses e
        JOIN approvals a ON e.id = a.expense_id
        WHERE e.user_id = ?
        AND a.status != 'pending'
        """,
        (id,)
    )

    rows = cursor.fetchall()
    expenses = []

    for row in rows:
        expenses.append(Expense(
            id=row['id'],
            user_id=row['user_id'],
            amount=row['amount'],
            description=row['description'],
            date=row['date']
        ))
    
    conn.close()
    return expenses

def get_from_id(id:int):
    conn = get_connection()
    cursor = conn.execute(
    """
    SELECT * FROM expenses WHERE id = ?
    """, 
    (id,)
    )

    row = cursor.fetchone()
    conn.close()


    if row is None:
        return None

    return Expense(
            id=row[0],
            user_id=row[1],
            amount=row[2],
            description=row[3],
            date=row[4]
    )

