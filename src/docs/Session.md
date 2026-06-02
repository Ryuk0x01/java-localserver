# WIP: refining Session.md
# Session.java — Remembering Users

📄 **File:** `src/utils/Session.java`
🔗 **Back to:** [Main README](../../../README.md)

---

## What is this file?

`Session.java` keeps track of **individual users** across multiple requests. HTTP is "stateless" — it doesn't remember you between requests. Sessions solve this problem.

---

## The Problem Sessions Solve

Imagine you log into a website:
1. `POST /login` — you send username + password
2. `GET /dashboard` — you want to see your profile

Between step 1 and step 2, the server has no memory of who you are (HTTP is stateless). Sessions fix this by giving you a unique ID that ties your requests together.

---

## How it works

**First visit (no session):**
1. Browser sends a request with no cookie
2. `Session.getOrCreate(null)` creates a brand new session with a random UUID
3. Server sends back `Set-Cookie: SESSIONID=550e8400-e29b-41d4...`

**Second visit (with session):**
1. Browser sends `Cookie: SESSIONID=550e8400-e29b-41d4...`
2. `Session.getOrCreate("550e8400-...")` finds the existing session
3. Same session object — can store user data between requests

---

## The Code

```java
private static Map<String, Session> sessions = new HashMap<>();

public static Session getOrCreate(String sessionId) {
    if (sessionId != null && sessions.containsKey(sessionId)) {
        return sessions.get(sessionId);  // Return existing session
    }
    String newId = UUID.randomUUID().toString();  // e.g. "550e8400-e29b-41d4-a716-446655440000"
    Session s = new Session(newId);
    sessions.put(newId, s);
    return s;  // Return new session
}
```

`UUID.randomUUID()` generates a globally unique ID — practically impossible to guess.

---

## Session Data

Each session can store arbitrary data:
```java
session.data.put("username", "Alice");
session.data.put("isLoggedIn", true);
```

This data lives in memory. If the server restarts, all sessions are lost (this is acceptable for a school project — production servers would use a database).

---

## Where the ID comes from

It's a **UUID** (Universally Unique IDentifier), a 128-bit random number formatted like:
```
550e8400-e29b-41d4-a716-446655440000
```
There are so many possible values (2^128) that generating the same one twice is astronomically unlikely.
