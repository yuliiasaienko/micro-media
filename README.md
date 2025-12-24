# Micro Media

## 1) Overview
Two Spring Boot microservices plus Eureka:
- `resource-service` accepts MP3 uploads, validates/reads tags, stores the binary in Postgres, and forwards extracted metadata to `song-service`.
- `song-service` persists and serves song metadata.
- `eureka-server` provides service discovery.

## 2) Technologies
- Java 17, Spring Boot 3.4.1 (Web, Validation, Data JPA)
- Spring Cloud 2024.0.0 (Netflix Eureka Client/Server, LoadBalancer)
- Springdoc OpenAPI 2.7.0
- Hibernate/JPA, HikariCP, Tomcat
- PostgreSQL 17
- Apache Tika (MP3 metadata parsing)
- Apache Commons Lang/Collections, Lombok
- Maven, Docker, Docker Compose

## 3) Local Run
Prereqs: Docker + Docker Compose.

1) Create `.env` in repo root (defaults shown):
   ```env
   RESOURCE_DB_NAME=resource_db
   RESOURCE_DB_USER=resource_user
   RESOURCE_DB_PASSWORD=resource_pass
   RESOURCE_DB_URL=jdbc:postgresql://resource-db:5432/resource_db

   SONG_DB_NAME=song_db
   SONG_DB_USER=song_user
   SONG_DB_PASSWORD=song_pass
   SONG_DB_URL=jdbc:postgresql://song-db:5432/song_db

   EUREKA_SERVER_PORT=8761
   RESOURCE_SERVICE_PORT=8081
   SONG_SERVICE_PORT=8082

   SONG_SERVICE_URL=http://song-service:8082
   SONG_SERVICE_PATH=/songs
   EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
   EUREKA_INSTANCE_PREFER_IP_ADDRESS=true
   ```
2) Run: `docker compose up --build`
3) Ports:
   - Eureka: `http://localhost:${EUREKA_SERVER_PORT}`
   - Resource Service: `http://localhost:${RESOURCE_SERVICE_PORT}`
   - Song Service: `http://localhost:${SONG_SERVICE_PORT}`
   - Host DB ports (compose defaults): resource-db `5433`, song-db `5434`.

## 4) Testing
- Postman collection: `introduction_to_microservices.postman_collection.json`
- Sample MP3 for manual upload: `mp3.zip`
- Quick curls:
  ```bash
  curl -X POST http://localhost:8081/resources \
       -H "Content-Type: audio/mpeg" \
       --data-binary @track.mp3

  curl http://localhost:8082/songs/1
  ```

## 5) API (Swagger snapshot in-repo)
Full OpenAPI specs live in `docs/`:
- `docs/resource-service-openapi.yaml`
- `docs/song-service-openapi.yaml`

### Resource Service (port 8081)
- `POST /resources`
  - Body: binary MP3 (`Content-Type: audio/mpeg` or `application/octet-stream`)
  - Response: `{"id":1}`
  - Errors: `400` for invalid/empty/non-MP3 or missing tags; `500` if metadata save fails
- `GET /resources/{id}`
  - Returns MP3 bytes with `Content-Type: audio/mpeg`
  - Errors: `404` if not found
- `DELETE /resources?id=1,2,3`
  - Cascades deletion of metadata in Song Service
  - Response: `{"ids":[1,2,3]}` (only actually deleted)
  - Errors: `400` on empty/invalid ids

### Song Service (port 8082)
- `POST /songs`
  - Body:
    ```json
    {
      "id": 1,
      "name": "We are the champions",
      "artist": "Queen",
      "album": "News of the world",
      "duration": "02:59",
      "year": "1977"
    }
    ```
  - Response: `{"id":1}`
  - Validation: id > 0; name/artist/album length 1–100; duration `mm:ss`; year `1900–2099`
  - Errors: `400` validation; `409` if id already exists
- `GET /songs/{id}`
  - Response:
    ```json
    {
      "id": 1,
      "name": "...",
      "artist": "...",
      "album": "...",
      "duration": "02:59",
      "year": "1977"
    }
    ```
  - Errors: `404` if not found; `400` if id invalid
- `DELETE /songs?id=1,2,3`
  - Response: `{"ids":[1,2,3]}` (only removed records)
  - Errors: `400` on invalid ids
