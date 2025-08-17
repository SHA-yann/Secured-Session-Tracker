# portfolio App

A full-stack portfolio project using:
**Backend:** Java 17, Spring Boot
**Frontend:** Angular 19 (standalone components)
**Database:** PostgreSQL
**Methodology:** Test-Driven Development (TDD) for one feature
**Version Control:** Git (feature-branch workflow)

## Project Purpose
Demonstrate the ability to design, develop, test, and document a complete for job postings and applications.

## Technologies
Java 17
Spring Boot
Angular 19
PostgreSQL
JUnit 5
Jasmine/Karma

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

## Feature register-user:POST (TDD)
- UserServiceTest tests the creation of a user
- UserRepositoryTest tests the persistence of a user in the db
- UserControllerTest tests registration of a user via the REST API 
- implementing User, UserController, UserService, UserRepository

## Feature search-user:GET (Agile)
- getting Allusers(when empty list), finding a user by Id(when not found), or by mail(when not found)
- implementing all GET methods from controller to repository layer(if needed) with tests

## Feature update-user:PUT (Agile)
- implementing on the service layer and tests
- implementing on the controller layer and tests
