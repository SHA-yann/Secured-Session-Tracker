# Access management demo Api

## Project Purpose
Demonstrate through a simple, authentication via credentials, authorization by JWT token with refresh token and access role based. Exposes secured REST endpoints
and documented with OpenApi
**Methodology:** Test-Driven Development (TDD) for one feature
**Version Control:** Git (feature-branch workflow)

## Technologies
Java 17
Maven 3.9
Git 2.49
Spring Boot 3.5.4
PostgreSQL 17
JUnit 5

## Development Workflow
Create feature branch
Writing one feature with TDD
Commit frequently with meaningful messages
Push branch and create Pull Request (PR)
Merge into `dev` after review
Merge into `main` after feature completion

## Branching Strategy
main → stable releases
dev → integration branch
feature/* → individual tasks

## Requirements
java 17
Git 2.49
Maven 3.9+
PostgreSQL 17 / H2(in-memory) for tests
Postman / Swagger UI via http://localhost:8080/swagger-ui/index.html

## Setup Instructions
 ### 1. clone repository
 git clone https://github.com/SHA-yann/taskmanager
 cd taskmanager
 ### 2. configure database
 install PostgreSQL (runtime), H2(tests)
 create database with a user and password, and configure backend/src/main/resources/application.properties

 ### 3. Run
 cd backend
 ./mvnw spring-boot:run for Linux/macOS or mvn spring-boot:run for Windows (if maven is installed, otherwise mvn.cmd)

 ## API Documentation

 ### Swagger UI
 http://localhost:8080/swagger-ui/index.html

 ### OpenAPI JSON
 http://localhost:8080/v3/api-docs

 ## Main Endpoints
 
 | Méthode | Endpoint             | Description                    | Rôle        |   requirements                                |
| ------- | --------------------- | ------------------------------ | ----------- | ---------------------------------------------
| GET     | `/users`              |         List all users         | ADMIN/USER  |                                               |
| POST    | `/users`              |         Create new user        | ADMIN       | uername, password, email,role, status         |
| GET     | `/users/{id}`         |         Find a user            | ADMIN       | user id                                       |
| PUT     | `/users/{username}`   |         Update a user          | ADMIN/USER  | username and body(attributs except username)  |
| DELETE  | `/users/{id}`         |         Delete a user          | ADMIN       | user id                                       |
| GET     | `/users/search`       | Recherche des utilisateurs avec pagination | ADMIN |`query` (username/role), `page`, `size` |

### Paginated User Search
Endpoint to search for users by a **keyword** (`username` or `role`) and retrieve results **page by page**.

**Parameters:**
- `query` (required): keyword to search for
- `page` (optional): page number (default = 0)
- `size` (optional): number of results per page (default = 10)

**Example curl:**
curl -X GET "http://localhost:8080/users/search?query=john&page=0&size=5" \
  -H "Authorization: Bearer <your_jwt_token>"


## Authentication / refresh

| Méthode | Endpoint          | Description                                                      |   requirements                        |
| ------- | ----------------- | ---------------------------------------------------------------- | -------------------------------------
| POST    |   `/auth/login`   |                Authenticate a user and return a JWT              | username, password
| POST    | `/auth/refresh`   | Refresh jwt(expired) with a refreh token a user and return a JWT | expired jwt and cookie(refresh token)


## sample requests
### create a user (curl)
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{"username":"john","password":"pass123","email":"john@example.com","role":"USER","status":"ACTIVE"}'

###Authenticatio(get a jwt)
 curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"John","password":"pass123"}'

### Access a secured endpoint with jwt
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer <your_token_jwt>"

## Advanced Security / Roles
The API manages Roles and Permissions (e.g., ADMIN, USER), prevent from role escalation / status modification
Sensitive endpoints (such as user creation or deletion) are restricted to ADMIN users only.
To test with different roles:
Create users with role: USER or role: ADMIN.
Authenticate each user to obtain the corresponding token.
Include the token in the request header: Authorization: Bearer <token>.

## Monitoring
The API exposes a management interface on port 8087
Sytem health:/actuator/health
Prometheus metrics:/actuator/prometheus

### Example of a restricted endpoint:
DELETE /users/{id} → accessible only by an ADMIN user.
GET /users/mail/{email } → accessible only by an ADMIN user.

## Tests
Run unit and integration tests:
./mvnw test  for Linux/macOS or mvn test for Windows (if maven is installed, otherwise mvn.cmd)
./mvnw verify for Linux/macOS or mvn test for Windows (if maven is installed, otherwise mvn.cmd)

## Contribution
1. Fork the project.
2. Create a branch: git checkout -b feature/my-feature
3. Commit your changes: git commit -m "Add a new feature"
4. Push to your branch: git push origin feature/my-feature
5. Open a Pull Request against the main repository.

## License

MIT License – see file [LICENSE](LICENSE)
