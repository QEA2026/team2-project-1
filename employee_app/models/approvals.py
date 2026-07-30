class Approval:
    def __init__(self, expense_id:int, status:str="pending", reviewer:int=None, comment:str=None, review_date:str=None, id=None) -> None:
        self.id = id
        self.expense_id = expense_id
        self.status = status
        self.reviewer = reviewer
        self.comment = comment
        self.review_date = review_date
