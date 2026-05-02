# Connection.java — One Client's State

📄 **File:** `src/Connection.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Connection.java` is a **data container**. It holds everything the server needs to know about **one specific client** currently connected.

Since the server handles many clients at once with a single thread, it needs a way to remember "what stage is Client A at?" and "what has Client B sent so far?". That's exactly what `Connection` does.

---

## Real-world analogy

Think of a **notepad** a waiter carries. For each customer at the table, the waiter writes:
- Table number (which port they came from)
- What they've ordered so far (HTTP headers/body)
- Whether the order is complete (has all data arrived?)
- What's being delivered back (the response buffer)

---

## The Fields Explained

```java
public SocketChannel channel;     // The actual network pipe to this client
public ByteBuffer readBuffer;     // Where we store incoming bytes as they arrive
public ByteBuffer writeBuffer;    // Where we put the response bytes to send out
public long lastActive;           // When did this client last send something?
public int localPort;             // Which port did they connect to? (for virtual hosting)
```

**HTTP parsing state:**
```java
public String method;        // "GET", "POST", "DELETE"
public String path;          // "/index.html", "/upload/file.txt"
public String queryString;   // "name=John&age=20" (the part after ?)
public Map<String,String> headers; // All HTTP headers e.g. {"host": "localhost"}
public byte[] body;          // The request body (e.g. uploaded file data)
public boolean chunked;      // Is the body sent in chunks?
```

**CGI script state:**
```java
public Process cgiProcess;   // The Python/Shell process currently running
public File cgiOutputFile;   // Temp file where the script writes its output
public File cgiInputFile;    // Temp file with the request body (for POST)
```

---

## Why track `localPort`?

The server supports **virtual hosting** — multiple websites on the same IP but different domain names. To figure out which website a client wants, we need to know:
1. Which port they connected on
2. What `Host:` header they sent

For example:
```
curl --resolve test.com:8080:127.0.0.1 http://test.com:8080/
```
The browser connects to port 8080 and sends `Host: test.com`. With both pieces of info, `Config.findServer()` knows to serve the `test.com` website, not `localhost`.

---

## The Buffer System

The `readBuffer` is a 16KB chunk of memory. As bytes arrive from the network, they're written here. Once a complete HTTP request is assembled, `HttpParser` processes it.

```
[...bytes arriving from network...] → readBuffer → HttpParser
```

The `writeBuffer` works in reverse:
```
Router builds a response → writeBuffer → [...bytes sent to browser...]
```

---

## `updateActivity()`

Called every time a client sends data. Resets their idle timer:
```java
public void updateActivity() {
    this.lastActive = System.currentTimeMillis();
}
```
If this timestamp gets too old, `EventLoop.checkTimeouts()` will disconnect them.
