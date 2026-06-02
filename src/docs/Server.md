# Server.java — Opening the Ports

📄 **File:** `src/Server.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Server.java` is responsible for **opening the TCP ports** that the server listens on and handing control to the `EventLoop`. It uses Java's **NIO (Non-blocking I/O)** system, specifically `ServerSocketChannel` and `Selector`.

Think of it as the **front door of the restaurant** — it opens the doors, sets up tables, and then the waiters (EventLoop) take over.

---

## Key concept: What is a Port?

A **port** is like an apartment number in a building. The building's address is your IP (e.g. `127.0.0.1`), and the port number tells the computer which program to send the data to.

- Port `8080` → our Java server
- Port `443` → usually HTTPS websites
- Port `22` → usually SSH

---

## Key concept: What is a Selector?

A `Selector` is Java's way of watching **many connections at once with a single thread**. Instead of creating one thread per client (which would be slow and heavy), we register all connections with the Selector and ask it: *"Who has data for me right now?"*

```
Selector watches:
  ├── Port 8080 socket  (waiting for new clients)
  ├── Port 8081 socket  (waiting for new clients)
  ├── Client A          (reading their request)
  └── Client B          (writing a response)
```

---

## The Code Explained

```java
selector = Selector.open();  // Create the watcher

for (int port : Config.allPorts) {
    ServerSocketChannel serverChannel = ServerSocketChannel.open(); // Create a socket
    serverChannel.configureBlocking(false);                          // Non-blocking mode
    serverChannel.bind(new InetSocketAddress("0.0.0.0", port));     // Bind to the port
    serverChannel.register(selector, SelectionKey.OP_ACCEPT);        // Register with Selector
}

EventLoop loop = new EventLoop(selector);
loop.run();  // Hand off control — runs forever
```

**Step by step:**
1. Open a `Selector` (the watcher)
2. For each port in the config, open a `ServerSocketChannel` (the door)
3. Set it to **non-blocking** — so it never freezes waiting for data
4. Bind it to the port — claim that port number
5. Register it with the Selector — tell the Selector to watch it
6. Start the `EventLoop` and let it run forever

---

## Graceful Error Handling

If one port fails to open (e.g. already in use), the server **does not crash**. It just prints a warning and continues with the other ports:

```java
} catch (Exception e) {
    System.err.println("Failed to bind port " + port + ": " + e.getMessage());
    // continue with other ports, don't crash
}
```

---

## Real-world analogy

If you try to put two restaurants on the same street address and floor — only one can exist. If port 8080 is already taken, the server skips it and still opens on port 8081.
