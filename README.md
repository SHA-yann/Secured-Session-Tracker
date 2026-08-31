# Secured Session Tracker (SST)

![Java 17](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-green.svg)
![Angular](https://img.shields.io/badge/Angular-21-red.svg)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-v4-blue.svg)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-Supported-2496ED.svg)
![Grafana](https://img.shields.io/badge/Observability-Prometheus_%2F_Loki_%2F_Grafana-F46800.svg)

**Secured Session Tracker** est une application full-stack entreprise conçue pour l'authentification sécurisée, la gestion des rôles et le suivi de présence utilisateur en temps réel. 

Le système s'appuie sur une architecture réactive non-bloquante (**Spring WebFlux**), un cache in-memory (**Redis**), une persistance relationnelle (**PostgreSQL**) et une pile complète d'observabilité (**Prometheus, Loki, Promtail, Grafana**).


## Fonctionnalités Clés

* **Sécurité Applicative :** Authentification stateless via JWT (Access & Refresh Tokens), contrôle d'accès basé sur les rôles (RBAC : `ROLE_ADMIN`, `ROLE_USER`) et protection anti-brute force via Rate Limiting (Bucket4j).
* **Temps Réel & Performance :** Notification de présence et statut de connexion diffusés en temps réel au client Angular via Server-Sent Events (SSE).
* **Interface Réactive :** Dashboard dynamique sous Angular 21 (Architecture Standalone, Signals, RxJS) avec Tailwind CSS v4.
* **Observabilité :** 
  * Métriques JVM & WebFlux exposées via Micrometer et Prometheus.
  * Métriques de serveur Web Nginx via `nginx-exporter`.
  * Centralisation et agrégation des logs de conteneurs avec Promtail et Loki.
  * Dashboards et alertes unifiés sur Grafana.


## Architecture Globale

                      ┌───────────────────────────┐
                      │     Client Navigateur     │
                      └─────────────┬─────────────┘
                                    │ HTTP / SSE (Port 80)
                                    v
                      ┌───────────────────────────┐
                      │    Front-App (Nginx)      │
                      └──────┬─────────────┬──────┘
                             │             │
    http://backend-api:8080  │             │ /stub_status
                             v             v
         ┌──────────────────────┐   ┌─────────────────┐
         │     Backend API      │   │  Nginx Exporter │
         │  (Spring WebFlux)    │   └────────┬────────┘
         └───┬──────────────┬───┘            │
             │              │                │
    Postgres │        Redis │       Metrics  │
             v              v                v
    ┌────────────────┐  ┌───────┐   ┌─────────────────┐
    │ PostgreSQL 17  │  │ Redis │   │   Prometheus    │
    └────────────────┘  └───────┘   └────────┬────────┘
                                             │
    ┌────────────────┐    Logs               v
    │ Promtail/Loki  ├───────────► ┌─────────────────┐
    └────────────────┘             │    Grafana      │
                                   └─────────────────┘

## Stack Technique

* **Frontend :** Angular 21, RxJS, Fetch Event Source (SSE), Tailwind CSS v4.
* **Backend :** Java 17, Spring Boot 3.5.4 (Spring WebFlux, Spring Security).
* **Sécurité :** JWT, Bucket4j, BCrypt (Work Factor 12).
* **Persistance & Cache :** PostgreSQL 17, Redis 7 (Letuce reactive).
* **Infrastructure & Reverse Proxy :** Nginx, Docker, Docker Compose.
* **Observabilité :** Prometheus 3.10, Grafana 12.4, Loki 3.6, Promtail 3.5.


## Démarrage Rapide

### Prérequis
  * [Docker Desktop](https://www.docker.com/products/docker-desktop/) installé avec `docker compose` v2+.
  * Git.

### 1. Cloner le dépôt
  bash
  git clone https://github.com/SHA-yann/Secured-Session-Tracker.git
  cd Secured-Session-Tracker.
  
### 2. Configurer l'environnement
  Créez un fichier .env dans le repertoire backend en vous basant sur l'exemple
  bash
  cp .env.example ./backend/.env

### 3. Build d'images
  bash
  docker build -t sst-back:1.0 -f backend/Dockerfile ./backend
  docker build -t sst-front:1.0 -f frontend/Dockerfile .

### 4. Lancer la stack
  bash
  docker compose --env-file ./backend/.env up -d

### 5. Accéder aux services
  Application Frontend : http://localhost ( user : David/Emma/Bob/Charlie/Yann password : Password123! )

  API Backend (Swagger UI) : http://localhost:8080/swagger-ui.html
  Dashboard Grafana : http://localhost:3000 (Identifiants par défaut : admin / admin)
  Prometheus UI : http://localhost:9090


### 6. Documentation Complète
  Pour consulter le détail de l'architecture, le schéma des flux, référez-vous au fichier TECHNICAL_DOC.md