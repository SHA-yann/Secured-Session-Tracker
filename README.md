# portfolio App

A full-stack portfolio project using:
**Backend:** Java 17, Spring Boot
**Frontend:** Angular 19 (standalone components)
**Database:** PostgreSQL
**Methodology:** Test-Driven Development, Agile
**Version Control:** Git (feature-branch workflow)

## Project Purpose
Demonstrate the ability to design, develop, test, and document a complete application for job postings and applications.

## Technologies
Java 17
Spring Boot
Angular 19
PostgreSQL
JUnit 5
Jasmine/Karma

## Development Workflow
Create feature branch
Writing one feature in TDD
Commit frequently with meaningful messages
Push branch and create Pull Request (PR)
Merge into `dev` after review
Merge into `main` after feature completion

## Branching Strategy
main → stable releases
dev → integration branch
feature/* → individual tasks

## Requirements
JDK 17
Git 
Node.js 20+
Angular CLI 15+
Maven 3.9+
PostgreSQL 15+

## Setup Instructions
 ### 1. clone repository
 git clone https://github.com/SHA-yann/taskmanager
 cd taskmanager
 ### 2. configure database
 install PostgreSQL (runtime), H2(tests)
 create database
 configure credentials in backend/src/main/resources/application.properties

 ### 3. backend
 cd backend
 mvn clean install
 - Run tests: mvn test
 - Run app: mvn spring-boot:run

 ### 4. frontend
 cd../frontend
 npm install

## Features
User CRUD (create, read, update, delete)
Authentication and authorization with JWT(access + refresh token)
Role-Based security (ADMIN, USER)
Password hashing with BCrypt
Pagination and sorting for GET /users
Search and filtering by username/role
Method-level security (@PreAuthorize)

## API Endpoints
### Auth
 POST /auth/register to register new user
 POST /auth/login  to authenticate, get JWT tokens
 POST /auth/refresh to refresh access token

### Users(secured)
 GET /users?page=0&suze=5&sort=username, asc to list users with pagination
 GET/users/{id} to get user by ID
 POST/users to create user (ADMIN only)
 PUT/users/{id} to update user (ADMIN or owner)
 DELETE/users/{id} to delete user
 GET/users/search?username=yann&role=USER to search/filter users

 ## Testing
  UserServiceTest for Business logic tests
  UserRepositoryTest for repository queries
  UserControllerTest, UserControllerIT, AuthControllerIT for API layer tests