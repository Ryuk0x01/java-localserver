# WIP: refining EventLoop.md
# EventLoop.java — The Heart of the Server

📄 **File:** `src/EventLoop.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`EventLoop.java` is the **core engine** of the server. It runs in an **infinite loop**, constantly checking if any client has sent data, needs a response, or has timed out.

This is what makes the server **non-blocking and single-threaded** — instead of creating a new thread for each client (like most simple servers do), one loop handles everyone.

---

## Real-world analogy

Imagine a **bank teller** who is incredibly fast:
- Instead of each customer getting their own dedicated teller (threads), there's ONE super-fast teller
- The teller constantly scans the room: *"Who's ready to talk? Who needs a response? Who's been waiting too long?"*
- They handle whoever is ready, then scan again

That teller is the EventLoop.

---

## The Main Loop

```java
while (true) {
    selector.select(500);  // Wait up to 500ms for something to happen

    for (SelectionKey key : selector.selectedKeys()) {
        if (key.isAcceptable()) acceptConnection(key);  // New client arrived
        if (key.isReadable())   readData(key);          // Client sent data
        if (key.isWritable())   writeData(key);         // Client ready for response
    }

    checkTimeouts();  // Kick out clients who've been idle too long
    checkCGI();       // Check if any CGI scripts have finished running
}
```

---

## The Four Methods

### 1. `acceptConnection()` — Welcoming a new client
When a browser first connects, `acceptConnection` is called:
- Takes the new client's socket channel
- Creates a `Connection` object to track their state
- Registers them with the Selector so we can read their request

### 2. `readData()` — Reading the client's request
When a client sends data:
- Reads bytes from their socket into a buffer
- Passes the buffer to `HttpParser` to decode the HTTP request
- If the connection closed (returns -1), the client is removed

```java
int bytesRead = conn.channel.read(conn.readBuffer);
if (bytesRead == -1) { closeConnection(conn, key); return; }
HttpParser.parse(conn, key);
```

### 3. `writeData()` — Sending the response
When a response is ready to be sent:
- Writes bytes from the `writeBuffer` to the client's socket
- When the buffer is empty, the connection is closed

### 4. `checkCGI()` — Non-blocking script execution
CGI scripts run as external processes (Python, Shell). Since we can't block the loop waiting for them, we poll every cycle:
- *"Is the CGI process done yet?"*
- If yes: read its output, send it as the HTTP response
- If no: check again next loop iteration

---

## Why Only One Thread?

The project rules say: **single process, single thread**. This is actually a famous design pattern used by Node.js, Nginx, and Redis. The trick is:
- Never do anything that **blocks** (waits for I/O)
- Use `selector.select()` which tells you who is ready WITHOUT waiting for each one

---

## Timeout Protection

The server tracks when each client last sent data. If they've been idle longer than the `timeout` setting in `config.json`, they get disconnected:

```java
if (now - conn.lastActive > timeoutMs) {
    conn.channel.close();  // Disconnect them
}
```

This prevents slow clients from hogging resources.
