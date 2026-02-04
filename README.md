# 🚀 Convify API: Next-Gen YouTube Converter

The Convify API is a high-performance microservice designed for seamless YouTube video conversion. 
Built on the bleeding edge of Java and Spring technology, it offers a rock-solid, clean architecture for handling file conversions and streaming.

## ✨ Core Technology Stack

| Component | Version / Role |
| :--- | :--- |
| **Framework** | **Spring Boot 4** |
| **Language** | **Java 25** |
| **External Tool** | **`yt-dlp`** |
| **Architecture** | Service-oriented, Global Exception Handling |

## ⚙️ How It Works

1.  A user sends a YouTube **URL** and target **`format`** (`mp3` or `mp4`) to the `/convert` endpoint.
2.  The **`YouTubeService`** uses the external command-line tool **`yt-dlp`** to fetch and convert the video on the server.
3.  Upon success, the API returns the server-side **`filePath`**.
4.  The client calls the `/download` endpoint with the `filePath` to stream the binary file back to the user.

## 🔗 Endpoints (Quick Reference)

| Type | Endpoint | Description | Request Body |
| :--- | :--- | :--- | :--- |
| `POST` | `/convert` | Initiates video conversion via `yt-dlp`. | `{ "url": "...", "format": "..." }` |
| `POST` | `/download` | Streams the converted file back to the client. | `{ "filepath": "..." }` |

## 🛡️ Key Architectural Principles

  * **Controllers:** Controllers are purely HTTP translators; all business logic is in the **Service Layer**.
  * **Zero Try-Catch:** All validation and runtime errors are managed centrally by the **`GlobalExceptionHandler`** (using a `ResponseEntity<ErrorMessageResponse>`), guaranteeing consistent and predictable error formats.
  * **Safety & Performance:** Uses `ProcessBuilder` and `try-with-resources` for safe execution of external shell commands.

## 💻 Setup and Run

### Requirements

  * **JDK 17+** (For compilation—targeting Java 25 runtime environment)
  * **Maven**
  * **`yt-dlp`** (Must be installed and in your system's PATH)
  * **`ffmpeg`** (Must be installed and in your system's PATH)

### Configuration

Ensure your `application.properties` is defined:

```properties

# Application Configuration
spring.application.name=convify-api
server.port=8080
server.url=http://localhost:8080

# YouTube API Configuration
youtube.api.key=${YOUTUBE_API_KEY}
youtube.api.application-name=Convify

# Download Configuration
app.download.dir=${DOWNLOAD_DIR}
youtube.download.max-concurrent=3
youtube.download.min-disk-space-gb=1
youtube.download.file-retention-hours=24
youtube.download.cleanup-cron=0 0 2 * * *

# Rate Limiting
youtube.rate-limit.capacity=10
youtube.rate-limit.refill-tokens=10
youtube.rate-limit.refill-duration-minutes=1

# Actuator Configuration (Health & Metrics)
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=when-authorized
management.health.defaults.enabled=true
management.prometheus.metrics.export.enabled=true

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=24h

# Thread Pool Configuration
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=10
spring.task.execution.pool.queue-capacity=100
spring.task.scheduling.pool.size=2

# Jackson Configuration
spring.jackson.serialization.indent-output=true

# Error Handling
spring.web.error.include-stacktrace=never
spring.web.error.include-exception=false

# Validation
spring.validation.method.adapt-constraint-violations=true

# Swagger/OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# Security
# spring.security.user.name=admin
# spring.security.user.password=${ADMIN_PASSWORD}

```

### Build & Execute

```bash
# Build the project
mvn clean install
```

### Now, you are ready to run the application!

