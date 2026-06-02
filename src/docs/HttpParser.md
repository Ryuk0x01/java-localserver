# WIP: refining HttpParser.md
# HttpParser.java — Reading the Client's Request

📄 **File:** `src/HttpParser.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`HttpParser.java` reads the **raw bytes** that arrive from the browser and turns them into something the server can understand — the HTTP method, path, headers, and body.

---

## What does a raw HTTP request look like?

When you visit `http://localhost:8080/upload/test.txt` and POST some data, the browser sends this raw text over the network:

```
POST /upload/test.txt HTTP/1.1\r\n
Host: localhost:8080\r\n
Content-Length: 11\r\n
\r\n
hello world
```

This arrives as raw **bytes**. `HttpParser` has to manually decode all of this.

---

## Step-by-Step Parsing

### Step 1 — Find where the headers end
HTTP headers always end with `\r\n\r\n` (two blank lines). We scan the bytes looking for that pattern:

```java
// Scan for \r\n\r\n
if (buffer.get(i)=='\r' && buffer.get(i+1)=='\n' &&
    buffer.get(i+2)=='\r' && buffer.get(i+3)=='\n') {
    return i; // found it!
}
```

### Step 2 — Parse the first line
The first line is always: `METHOD PATH PROTOCOL`
```
POST /upload/test.txt HTTP/1.1
```
We split by space and extract:
- `conn.method = "POST"`
- `conn.path = "/upload/test.txt"`
- `conn.protocol = "HTTP/1.1"`

### Step 3 — Parse the query string
If the path contains `?`, we split it:
```
GET /cgi-bin/hello.py?name=John&lang=java
→ conn.path = "/cgi-bin/hello.py"
→ conn.queryString = "name=John&lang=java"
```

### Step 4 — Parse headers
Each header line is `Key: Value`. We split on `:` and store them all:
```java
conn.headers.put("host", "localhost:8080");
conn.headers.put("content-length", "11");
```

### Step 5 — Read the body
For normal requests with `Content-Length`, we read exactly that many bytes:
```java
conn.body = new byte[conn.contentLength]; // e.g. "hello world"
```

---

## Chunked Transfer Encoding

Some clients send the body in **chunks** instead of all at once. Each chunk has a size number in hexadecimal, then the data:

```
5\r\n
Hello\r\n
6\r\n
 World\r\n
0\r\n
\r\n
```
- `5` = 5 bytes → "Hello"
- `6` = 6 bytes → " World"
- `0` = end of chunks

The `readChunkedBody()` method handles this, assembling all chunks into one complete body.

---

## Body Size Check

Before fully reading the body, we check if it exceeds the limit set in `config.json`:

```java
if (conn.contentLength > maxBody) {
    Router.sendError(conn, key, 413, "Payload Too Large");
    return;
}
```

If it's too big, the server immediately responds with **413 Payload Too Large** without reading any more data.

---

## Why is parsing complex?

Because data arrives **in pieces**. TCP (the network protocol) doesn't guarantee that a full HTTP request arrives in one chunk. The headers might arrive first, then the body in parts. The parser must handle all of these cases without blocking.
