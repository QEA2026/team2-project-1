import os
from pathlib import Path
import psycopg


PROJECT_ROOT = Path(__file__).resolve().parents[2]


def _required_environment(name):
    value = os.environ.get(name, "").strip()
    if not value:
        raise RuntimeError(f"Required database environment variable {name} is not set")
    return value


def get_connection():
    """Open a PostgreSQL connection to the shared AWS RDS database."""
    return psycopg.connect(
        host=_required_environment("RDSHOST"),
        port=_required_environment("RDS_PORT"),
        dbname=_required_environment("RDS_DB_NAME"),
        user=_required_environment("RDS_USERNAME"),
        password=_required_environment("RDS_PASSWORD"),
        sslmode=os.environ.get("RDS_SSLMODE", "require"),
        connect_timeout=int(os.environ.get("RDS_CONNECT_TIMEOUT", "10")),
    )


def init_db(seed=False):
    """Create the PostgreSQL schema and optionally load development seed data."""
    schema_path = PROJECT_ROOT / "database" / "schema.sql"
    seed_path = PROJECT_ROOT / "database" / "seed.sql"

    with get_connection() as conn:
        conn.execute(schema_path.read_text(encoding="utf-8"))
        if seed:
            conn.execute(seed_path.read_text(encoding="utf-8"))
