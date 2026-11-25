# Test Case & Bug Tracker — Spring Boot
Java 21, Spring Boot, PostgreSQL, Flyway, JWT, React and Kafka.

## Local development (PowerShell)
Generate a development JWT secret in the terminal:
```powershell
$bytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
docker compose up -d postgres kafka
cd backend
.\mvnw.cmd spring-boot:run
```
In another terminal: `cd frontend`, `npm ci`, `npm run dev`.
Full stack alternative from the root (with JWT_SECRET set): `docker compose --profile full up --build -d`. Open http://localhost:8080. Stop local backend first.

## API
Register and login at POST /api/register and /api/login. Send Bearer token for all project and test-case operations. Project routes are unchanged. Test-case routes: POST /api/projects/{id}/create-testcase, GET /api/projects/{id}/testcases, GET/PUT/DELETE /api/testcases/{id}. Owners and members may create/read/update cases; only owners may delete. Failed test cases represent defects in the original UI.

## Kafka
A committed status change publishes to test-case-events. The consumer inserts an idempotent audit record into test_case_events. This is a demonstration of asynchronous messaging, not a durable transactional outbox; a broker outage can lose an event. Audit records survive case deletion.

## Tests
`cd backend` then `.\mvnw.cmd test` (Docker required for the generated context integration test). Unit tests: `.\mvnw.cmd test "-Dtest=AuthServiceTest,ProjectServiceTest,TestCaseServiceTest"`.

## Migration notes
Existing V1/V2 migrations are preserved. V3 adds test cases, ordered steps and event audit. MongoDB data is not imported. Default status pending and priority medium. The local DB password is for development only. Never commit JWT_SECRET. Separate bug entity and Swagger are not included in this version.
