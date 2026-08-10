class Expense:
    def __init__(self, user_id:int, amount:int, description:str, date:str, id=None) -> None:
        self.id = id
        self.user_id = user_id
        # PostgreSQL NUMERIC values are Decimals; keep the existing JSON API numeric.
        self.amount = float(amount)
        self.description = description
        # PostgreSQL DATE values are date objects; the frontend expects YYYY-MM-DD.
        self.date = date.isoformat() if hasattr(date, "isoformat") else date
