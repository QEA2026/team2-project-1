class User:
    def __init__(self, username:str, password:str, role:str, id=None) -> None:
        self.id = id
        self.username = username
        self.password = password
        self.role = role
