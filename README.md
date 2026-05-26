# Java HTTP Server — Complete Documentation

Welcome! This is a custom HTTP/1.1 web server written from scratch in Java — no frameworks, no external libraries, just pure Java.

Think of it like building a restaurant from the ground up: the kitchen, the waiters, the menus, the tables — everything made by hand.

---

## Table of Contents

- [What Does This Server Do?](#what-does-this-server-do)
- [How to Build & Run](#how-to-build--run)
- [Project Structure](#project-structure)
- [Source Files — Explained](#source-files--explained)
- [Configuration File](#configuration-file)
- [Testing](#testing)
- [How HTTP Works (Quick Summary)](#how-http-works-quick-summary)

---

## What Does This Server Do?

When you open a website in your browser, your browser sends a **request** to a server, and the server sends back a **response** (the webpage). This project *is* that server — built entirely by hand.

**Features:**
- ✅ Listens on multiple ports (e.g. 8080 and 8081 at the same time)
- ✅ Serves static files (HTML, CSS, images, etc.)
- ✅ Handles file uploads and deletions
- ✅ Runs external scripts (CGI — Python, Shell)
- ✅ Manages user sessions with cookies
- ✅ Supports multiple websites on the same port (virtual hosting)
- ✅ Shows an admin dashboard at `/admin`
- ✅ Never crashes — all errors are handled gracefully

---

## How to Build & Run

**Step 1 — Compile the Java source code:**
```bash
javac -d out src/*.java src/utils/*.java
```
This takes all `.java` files and compiles them into runnable `.class` files inside the `out/` folder.

**Step 2 — Start the server:**
```bash
java -cp out Main
```
You should see:
```
Config loaded: 2 server(s), ports: [8080, 8081]
Listening on port 8080
Listening on port 8081
Starting event loop...
```

**Step 3 — Open your browser and go to:**
```
http://localhost:8080
```

---

## Project Structure

```
java-localserver/
│
├── config.json          ← Server settings (ports, routes, error pages)
│
├── src/                 ← All Java source code
│   ├── Main.java        ← Entry point — where the program starts
│   ├── Server.java      ← Opens the ports and starts listening
│   ├── EventLoop.java   ← The heart — handles ALL connections in one loop
│   ├── Connection.java  ← Stores the state of ONE client connection
│   ├── HttpParser.java  ← Reads and understands the client's request
│   ├── Router.java      ← Decides what to do with each request
│   ├── StaticHandler.java ← Serves files (GET, POST, DELETE)
│   ├── CGIHandler.java  ← Runs external scripts (Python, Shell)
│   ├── Config.java      ← Reads config.json and validates it
│   ├── JsonParser.java  ← Parses JSON manually (no libraries)
│   ├── Metrics.java     ← Tracks server stats (uptime, requests, etc.)
│   └── utils/
│       ├── Cookie.java  ← Parses cookie headers
│       └── Session.java ← Manages user sessions in memory
│
├── cgi-bin/             ← Scripts the server can run
│   ├── hello.py         ← Python CGI example
│   └── hello.sh         ← Shell CGI example
│
├── www/                 ← Files served to the browser
│   ├── index.html       ← Homepage
│   └── uploads/         ← Where uploaded files go
│
├── www_test/            ← Files for the "test.com" virtual host
│   └── index.html
│
├── error_pages/         ← Custom HTML pages for errors
│   ├── 400.html
│   ├── 403.html
│   ├── 404.html
│   ├── 405.html
│   ├── 413.html
│   └── 500.html
│
├── test_all.sh          ← Automated test script
└── test_siege.sh        ← Stress test script
```

---

## Source Files — Explained

Each source file has its own detailed README. Click to read:

| File | What It Does |
|------|-------------|
| [Main.java](src/docs/Main.md) | Starts the whole program |
| [Server.java](src/docs/Server.md) | Opens ports and sets up the NIO Selector |
| [EventLoop.java](src/docs/EventLoop.md) | The single-threaded event loop — the core of everything |
| [Connection.java](src/docs/Connection.md) | Represents one client connected to the server |
| [HttpParser.java](src/docs/HttpParser.md) | Parses raw HTTP requests into usable data |
| [Router.java](src/docs/Router.md) | Decides which handler deals with each request |
| [StaticHandler.java](src/docs/StaticHandler.md) | Serves, uploads, and deletes files |
| [CGIHandler.java](src/docs/CGIHandler.md) | Runs external scripts (Python, Shell) |
| [Config.java](src/docs/Config.md) | Reads and validates config.json |
| [JsonParser.java](src/docs/JsonParser.md) | Parses JSON by hand without libraries |
| [Metrics.java](src/docs/Metrics.md) | Counts requests, tracks uptime, shows the dashboard |
| [Cookie.java](src/docs/Cookie.md) | Reads cookies from request headers |
| [Session.java](src/docs/Session.md) | Keeps track of individual users across requests |

---

## Configuration File

The server is fully controlled by `config.json`. See the [config.json documentation](docs/config.md).

---

## Testing

**Run all functional tests:**
```bash
./test_all.sh
```

**Run the stress test (requires `siege`):**
```bash
./test_siege.sh
```

**Manual curl examples:**
```bash
# Get the homepage
curl http://localhost:8080/

# Upload a file
curl -X POST -d "hello world" http://localhost:8080/upload/myfile.txt

# Download the file back
curl http://localhost:8080/upload/myfile.txt

# Delete the file
curl -X DELETE http://localhost:8080/upload/myfile.txt

# Test CGI (Python script)
curl http://localhost:8080/cgi-bin/hello.py

# Test CGI (Shell script)
curl http://localhost:8080/sh-cgi/hello.sh

# Test virtual hosting
curl --resolve test.com:8080:127.0.0.1 http://test.com:8080/

# View admin dashboard
curl http://localhost:8080/admin
```

---

## How HTTP Works (Quick Summary)

HTTP is a simple conversation between a **browser (client)** and a **server**:

1. **You type** `http://localhost:8080/` in your browser
2. **Browser sends a request:**
   ```
   GET / HTTP/1.1
   Host: localhost:8080
   ```
3. **Server reads it**, finds the right file, and sends back:
   ```
   HTTP/1.1 200 OK
   Content-Type: text/html
   Content-Length: 331

   <html>...</html>
   ```
4. **Browser displays** the HTML as a webpage

This project handles steps 2, 3 — reading the request and building the response — entirely from scratch using Java's low-level networking APIs.
