# Router.java — Traffic Controller

📄 **File:** `src/Router.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Router.java` receives a fully-parsed HTTP request and **decides what to do with it**. It looks at the path, checks the config, and sends the request to the right handler.

Think of it as a **traffic controller at an airport** — every plane (request) comes in, and the controller decides which runway (handler) to send it to.

---

## The Decision Flow

```
Request arrives (e.g. GET /cgi-bin/hello.py)
  │
  ├── Which server? (virtual hosting via Host header)
  │     → Config.findServer()
  │
  ├── Which route matches? (longest prefix wins)
  │     /cgi-bin matches route { path: "/cgi-bin", cgi_extension: ".py" }
  │
  ├── Is the method allowed?
  │     GET is in ["GET", "POST"] → OK
  │
  └── Which handler?
        Has "cgi_extension"? → CGIHandler
        Has "redirect"?      → sendRedirect()
        Has "admin"?         → Metrics.toHtml()
        Default              → StaticHandler
```

---

## Virtual Hosting

Before routing, the Router figures out **which virtual server** this request belongs to. It reads the `Host:` header:

```java
Map<String, Object> server = Config.findServer(
    conn.headers.get("host"),   // e.g. "test.com"
    conn.localPort              // e.g. 8080
);
```

A request to `test.com:8080` gets the `test.com` config, while a request to `localhost:8080` gets the `localhost` config — even though they use the same port.

---

## Route Matching (Longest Prefix)

If you have these routes:
```
/          → serves www/
/upload    → serves www/uploads/
```

And the request is `GET /upload/myfile.txt`:
- `/` matches (length 1)
- `/upload` also matches (length 7) ✓ **winner** (longer = more specific)

---

## Method Checking

Each route specifies which HTTP methods are allowed:
```json
{ "path": "/", "methods": ["GET"] }
```

If someone tries `DELETE /`, the Router immediately sends back **405 Method Not Allowed**.

---

## Building and Sending Responses

The `sendResponse()` method builds a proper HTTP response:

```java
// The response looks like:
"HTTP/1.1 200 OK\r\n"
"Content-Type: text/html\r\n"
"Content-Length: 331\r\n"
"Set-Cookie: SESSIONID=abc123; Path=/; HttpOnly\r\n"
"\r\n"
<html>...</html>
```

All of this is packed into `conn.writeBuffer`, and the EventLoop is told to start writing it to the client.

---

## Sessions & Cookies

On every request, the Router:
1. Reads the `Cookie:` header to find an existing session ID
2. Looks up or creates a `Session` object
3. Sends back a `Set-Cookie` header with the session ID

This is how the server "remembers" returning visitors.

```java
Map<String, String> cookies = Cookie.parse(conn.headers.get("cookie"));
Session session = Session.getOrCreate(cookies.get("SESSIONID"));
// → adds Set-Cookie header to the response
```
