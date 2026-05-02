# Metrics.java — Server Dashboard

📄 **File:** `src/Metrics.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Metrics.java` is a **bonus feature** — it tracks live statistics about the server and exposes them as a dashboard at `/admin`.

---

## What does it track?

| Metric | Description |
|--------|-------------|
| `totalRequests` | How many HTTP requests have been handled since startup |
| `activeConnections` | How many clients are connected right now |
| `startTime` | When the server started (used to calculate uptime) |
| `statusCodes` | Count of each HTTP status code served (200, 404, etc.) |

---

## How to view it

While the server is running, open your browser and go to:
```
http://localhost:8080/admin
```

Or via curl:
```bash
curl http://localhost:8080/admin
```

You'll see a dark-themed dashboard showing:
- Uptime (e.g. `0h 5m 23s`)
- Total requests served
- Active connections
- Status code breakdown
- List of configured servers and their ports

---

## How data is recorded

Every time a request comes in, `Router` calls:
```java
Metrics.recordRequest();   // increments totalRequests
Metrics.recordStatus(200); // increments statusCodes["200"]
```

Active connections are updated in `EventLoop` whenever clients connect or disconnect:
```java
Metrics.activeConnections = connections.size();
```

---

## Two output formats

**HTML** (for the browser at `/admin`):
```java
Metrics.toHtml() // → styled dark-theme HTML dashboard
```

**JSON** (for programmatic access):
```java
Metrics.toJson()
// → {"uptime_seconds":300,"total_requests":1250,"active_connections":3,...}
```

---

## Why is this useful?

In production, you'd use a dashboard like this to:
- See if the server is getting too many requests (potential attack)
- Identify which pages return the most errors (404s)
- Monitor if too many connections are piling up (memory leak detection)
