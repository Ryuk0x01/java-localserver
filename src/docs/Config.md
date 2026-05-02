# Config.java — Reading the Configuration

📄 **File:** `src/Config.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Config.java` reads `config.json` at startup and stores all the server settings in memory. It also validates the configuration and **rejects bad settings** rather than crashing later at runtime.

Think of it as the **manager who reads the rulebook** before the restaurant opens.

---

## What does it load?

From `config.json`, it extracts:
- **timeout** — how long to wait before disconnecting idle clients
- **servers** — a list of server blocks, each with:
  - `server_name` — the hostname (e.g. `localhost` or `test.com`)
  - `ports` — which ports to listen on
  - `client_max_body_size` — max allowed request body size
  - `error_pages` — custom HTML pages for 404, 500, etc.
  - `routes` — URL path rules

---

## Validation — Catching Errors Early

Config validates each server block before accepting it:

**Duplicate ports in the same server:**
```json
"ports": [8080, 8080]  ← ERROR: duplicate!
```
```java
if (!seen.add(port)) {
    System.err.println("Error: duplicate port " + port);
    // skip this server, don't crash
}
```

**Invalid port number:**
```json
"ports": [99999]  ← ERROR: ports only go up to 65535
```

**No ports defined:**
```json
"ports": []  ← WARNING: nothing to listen on
```

If a server block is invalid, it's **skipped** — other server blocks still work. The server only exits if **no valid servers** are found at all.

---

## Finding the Right Server (Virtual Hosting)

`findServer()` is called on every request to figure out which server config applies:

```java
Config.findServer("test.com", 8080)
```

**Priority order:**
1. Exact match: `server_name` matches the `Host` header AND the port matches
2. Default server: has `"default_server": true` AND port matches
3. First server that has the matching port
4. Absolute fallback: the first server in the list

Example:
```
Request: Host: test.com, Port: 8080
→ Finds server_name="test.com" with port 8080 → returns test.com config

Request: Host: unknown.com, Port: 8080
→ No exact match
→ Falls back to default_server=true on port 8080 → returns localhost config
```

---

## Helper Methods

```java
// Get the max allowed body size for a server (defaults to 1MB)
Config.getMaxBodySize(server)  // → e.g. 10485760 (10MB)

// Get the error page paths
Config.getErrorPages(server)   // → {"404": "error_pages/404.html", ...}

// Get the route list
Config.getRoutes(server)       // → [{"path": "/", "methods": ["GET"], ...}, ...]
```
