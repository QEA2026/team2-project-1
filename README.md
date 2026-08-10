# Expense reimbursement system

The employee (Flask/Python) and manager (Javalin/Java) applications share one
PostgreSQL database. The connection is configured for AWS RDS entirely through
environment variables; no credentials are committed to this repository.

## Database configuration

Copy `.env.example` to `.env` for your local environment and fill in:

- `RDSHOST`: the RDS endpoint, without `https://`
- `RDS_PORT`: the PostgreSQL port configured on the RDS instance
- `RDS_DB_NAME`: the database name
- `RDS_USERNAME`: the RDS database user
- `RDS_PASSWORD`: the RDS database password
- `RDS_SSLMODE`: defaults to `require`

Export these variables into the shell or deployment environment that starts
either application. Java and Python use the same variable names.

Before the first deployment, apply `database/schema.sql` to the RDS database.
`database/seed.sql` contains development-only sample accounts and expenses and
must not be loaded in production. As an alternative for local development, the
employee app can apply the schema at startup with `DB_INIT_ON_STARTUP=true`; add
`DB_SEED_ON_STARTUP=true` only when sample data is wanted.

The RDS security group must allow PostgreSQL traffic from the applications'
security group (or their deployment hosts). Do not make the database publicly
accessible just to connect these applications.

The employee E2E tests use these same variables and modify database rows. Point
them only at a disposable test database initialized with the schema and seed,
never at production.

## Run the employee application

```bash
cd employee_app
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
python app.py
```

## Run the manager application

```bash
cd manager_app
mvn clean package
mvn exec:java -Dexec.mainClass=com.revature.Main
```
