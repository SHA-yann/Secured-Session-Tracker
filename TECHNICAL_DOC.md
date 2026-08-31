# Fiche d'Exploitation et Documentation Technique

    Ce document fournit la description synthétique du fonctionnement interne, de la topologie réseau et des guides d'exploitation pour le projet **Secured Session Tracker (SST)**.

## 1. Topologie Réseau & Modèle de Saisie (Docker Networks)

L'application est isolée en trois réseaux virtuels Docker distincts (`bridge`) pour garantir une étanchéité stricte des accès :

| Réseau Docker  | Services Membres           | Rôle & Isolation |
|                |                            |                  |
| `frontend-net` | `front-app`, `backend-api` | Routage du trafic utilisateur et proxying des requêtes API/SSE. |
| `backend-net`  | `backend-api`, `postgre-db`, `redis-cache` | Isolation totale des bases de données de la couche publique. |
| `monitoring-net` | `backend-api`, `nginx-exporter`, `prometheus`, `loki`, `promtail`, `grafana` | Réseau dédié à la collecte des métriques et à la centralisation des logs. |


## 2. Flux de Traitement & Routage Nginx

    Le conteneur `front-app` embarque Nginx, qui agit à la fois comme serveur de fichiers statiques pour Angular et comme Reverse Proxy :

    * **Routage SPA Angular :** Toutes les routes frontend inconnues sont réécrites vers `/index.html` via `try_files $uri $uri/ /index.html;`.
    * **Routage API & SSE :** Les préfixes `/auth`, `/users`, `/notifications` sont transférés au backend (`http://backend-api:8080`).
    * **Optimisation SSE (Server-Sent Events) :**
    ```nginx
    proxy_http_version 1.1;
    proxy_set_header Connection "";
    proxy_read_timeout 1h;
    proxy_send_timeout 1h;
    proxy_buffering off;
    proxy_cache off;
    proxy_set_header Cache-Control "no-cache";
    chunked_transfer_encoding on;

## 3. Architecture d'Observabilité
### Collecte des Métriques (Prometheus)
    Spring Boot Actuator : Expose les métriques JVM, HikariCP, Netty et HTTP sur le port dédié 8087 via /actuator/prometheus.

    Nginx Exporter : Lit le module /stub_status de Nginx sur le port 80 et le convertit au format Prometheus sur le port 9113.

    Prometheus : Scrape ces deux endpoints toutes les 15 secondes.

### Collecte des Logs (Loki & Promtail)
    Promtail : Est monté directement sur la socket Docker hôte (/var/run/docker.sock).

    Ingestion : Captures automatiques des flux stdout/stderr des conteneurs et étiquetage via les labels Docker (container="backend-api", container="front-app").

    Parsing : Promtail extrait le niveau de sévérité (INFO, WARN, ERROR) du backend pour accélérer le filtrage LogQL.

## 4. Commandes d'Exploitation Courantes
  - Consulter les logs d'un service en direct :
  Bash
  docker compose logs -f nom_service

  - Vérifier l'état de santé des conteneurs :
  Bash
  docker compose ps

  - Tester la réponse du stub_status Nginx :
  Bash
  docker exec -it front-app curl http://localhost/stub_status

  - Vérifier l'accès Prometheus aux métriques Backend :
  Bash
  docker exec -it prometheus curl http://backend-api:8087/actuator/prometheus

