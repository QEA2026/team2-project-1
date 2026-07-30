import sqlite3
import os
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[2]
DB_PATH = Path(
    os.environ.get("EXPENSE_DB_PATH", PROJECT_ROOT / "expenses_system_db.db")
).resolve()

def get_connection():
    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    return conn

def init_db():
    """Create and seed the shared database the first time the app starts."""
    schema_path = PROJECT_ROOT / "database" / "schema.sql"
    seed_path = PROJECT_ROOT / "database" / "seed.sql"

    with get_connection() as conn:
        users_table = conn.execute(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'users'"
        ).fetchone()

        if users_table is None:
            conn.executescript(schema_path.read_text(encoding="utf-8"))
            conn.executescript(seed_path.read_text(encoding="utf-8"))
