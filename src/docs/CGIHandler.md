# CGIHandler.java — Running External Scripts

📄 **File:** `src/CGIHandler.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

**CGI** stands for **Common Gateway Interface**. It's a way for a web server to run an external program (like a Python or Shell script) and send its output back to the browser as an HTTP response.

`CGIHandler.java` launches these external scripts as separate processes, passing HTTP information to them via **environment variables**, then sends their output back to the client.

---

## Real-world analogy

Imagine a restaurant where most dishes are pre-made (static files). But for special orders, the waiter (server) goes to an external chef (CGI script) and asks them to cook something custom. The chef produces the dish, the waiter brings it back to the customer.

---

## Supported Script Types

| Extension | Interpreter |
|-----------|-------------|
| `.py` | `python3` |
| `.sh` | `/bin/sh` |
| `.pl` | `perl` |

The interpreter is chosen based on the `cgi_extension` field in the route config.

---

## How it works step by step

### 1. Find the script file
```java
File scriptFile = Paths.get(root, relativePath).normalize().toFile();
if (!scriptFile.exists()) {
    Router.sendError(conn, key, 404, "CGI Script Not Found");
    return;
}
```

### 2. Write request body to a temp file (for POST requests)
```java
File inputFile = File.createTempFile("cgi_in_", ".tmp");
fos.write(conn.body);  // e.g. form data
```

### 3. Launch the script with environment variables
```java
ProcessBuilder pb = new ProcessBuilder("python3", scriptFile.getAbsolutePath());
Map<String, String> env = pb.environment();
env.put("REQUEST_METHOD", conn.method);        // "GET" or "POST"
env.put("PATH_INFO", conn.path);               // "/cgi-bin/hello.py"
env.put("QUERY_STRING", conn.queryString);     // "name=John"
env.put("CONTENT_LENGTH", "...");
```

These environment variables are the standard way CGI scripts receive request data. In your Python script, you access them with:
```python
import os
method = os.environ.get("REQUEST_METHOD")
query = os.environ.get("QUERY_STRING")
```

### 4. Redirect output to a temp file
```java
pb.redirectOutput(outputFile);  // Script output goes here
Process p = pb.start();
```

### 5. Don't block! Register as pending.
```java
key.interestOps(0);           // Stop watching this connection temporarily
loop.addPendingCGI(conn);     // EventLoop will check when script finishes
```

This is the crucial part — we **don't wait** for the script to finish here. We hand it off to the EventLoop's `checkCGI()` method, which polls each loop iteration until the process exits.

---

## Example: hello.py

```python
import os, datetime
print("Content-Type: text/html")
print("")
print("<html><body>")
print("<p>Hello from Python CGI!</p>")
print("<p>Method:", os.environ.get("REQUEST_METHOD"), "</p>")
print("<p>Query:", os.environ.get("QUERY_STRING"), "</p>")
print("</body></html>")
```

Test it:
```bash
curl "http://localhost:8080/cgi-bin/hello.py?name=World"
```

---

## Example: hello.sh

```bash
#!/bin/sh
echo "Content-Type: text/html"
echo ""
echo "<html><body>"
echo "<h1>Hello from Shell CGI!</h1>"
echo "<p>Date: $(date)</p>"
echo "</body></html>"
```

Test it:
```bash
curl http://localhost:8080/sh-cgi/hello.sh
```
