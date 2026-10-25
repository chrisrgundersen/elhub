# Metering API service
Simple app developed with reference to interview @Elhub: Register and fetch hourly values for metering points (GSRN)

## Technologies
Kotlin, Spring Boot, PostgreSQL, Flyway, JUnit 5, Testcontainers, Docker

## Run service 
Prerequisites: JDK 25, Docker

1. Navigate to root project folder 
2. docker compose up -d 
3. \gradlew bootRun (windows)

## Dependencies
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Validation
- Flyway Migration
- Testcontainers

## API
Running on http://localhost:8080
- POST /metering-points
- GET /metering-points
- POST /metering-points/{gsrn}/readings 
- GET /metering-points/{gsrn}/readings?from=&to= 
- GET /metering-points/{gsrn}/consumption?from=&to=

E.g:
- http://localhost:8080/metering-points
- http://localhost:8080/metering-points/707057500000000001/readings?from=2026-10-23T00:00:00Z&to=2026-10-24T00:00:00Z

## Testing (unit + integration)
1. Navigate to root project folder
2. .\gradlew test (windows)
3. build/reports/tests/test/index.html

### Test-scripts
The scripts test-api.ps1 and test-api.sh should only be run after truncating all data from the database.
You can do this either by logging into the postgreSQL database:
```bash
docker compose exec postgres psql -U metering -d metering
```
Then execute
```sql
truncate metering_point cascade;  
```
Alternatively you can delete the docker volumes:
```bash
docker compose down -v
docker compose up -d
```
You need to restart the application afterward. 

## Design decisions
- Layered architecture (controller, service, repository)
- GSRN is used as the external identifier
- Timestamp is UTC+0!

## Use of AI
I used AI (Claude) mainly as a sparring partner: asking questions about
concepts (e.g. layered architecture, DTO vs. entity, JPA, Testcontainers)
and discussing alternatives. Some smaller code snippets are based on AI
suggestions, which I have reviewed, adapted and understand. The overall
design, structure and decisions are my own. The helper scripts
`test-api.ps1` and `test-api.sh` were generated with AI assistance.
