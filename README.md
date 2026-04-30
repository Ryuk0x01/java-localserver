# Java HTTP Server

A lightweight HTTP/1.1 server built with Java NIO.

## How to build and run

```bash
javac -d out src/*.java src/utils/*.java
java -cp out Main
```

## Features

- Non-blocking I/O with java.nio Selector
- Listens on multiple ports (configured in config.json)
- Handles GET, POST, DELETE
- Static file serving with directory listing
- CGI script execution (Python)
- Cookie and session management
- Chunked transfer encoding support
- Custom error pages (400, 403, 404, 405, 413, 500)
- Connection timeouts
- Configurable via config.json

## Testing

```bash
# Basic test
curl http://localhost:8080/

# Upload a file
curl -X POST -d "hello" http://localhost:8080/upload/test.txt

# Delete a file
curl -X DELETE http://localhost:8080/upload/test.txt

# Run CGI
curl http://localhost:8080/cgi-bin/hello.py

# Test redirect
curl -v http://localhost:8080/redirect

# Stress test
siege -b -c 50 -t 30S http://localhost:8080/
```
