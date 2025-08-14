# task manager Project

A full-stack portfolio project using:
**Backend:** Java 17, Spring Boot
**Frontend:** Angular 19 (standalone components)
**Database:** PostgreSQL
**Methodology:** Test-Driven Development (TDD)
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

## Feature User-registration (TDD)
- UserServiceTest tests the creation of a user
- UserRepositoryTest tests the persistence of a user in the db
- UserControllerTest tests registration of a user via the REST API 

a complete user will need a password and role, this implies the refactoring of user, service and repository and tests