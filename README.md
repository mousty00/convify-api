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

### Configuration

Ensure your `application.properties` defines the download location:

```properties
app.download.dir=/tmp/yt-downloads
```

### Build & Execute

```bash
# Build the project
mvn clean install
```

### Now, you are ready to run the application!

