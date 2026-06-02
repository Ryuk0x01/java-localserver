# WIP: refining Cookie.md
# Cookie.java — Reading Cookies

📄 **File:** `src/utils/Cookie.java`
🔗 **Back to:** [Main README](../../../README.md)

---

## What is this file?

`Cookie.java` has one job: **parse the `Cookie:` header** from an HTTP request into a simple key-value map.

---

## What is a Cookie?

A cookie is a small piece of data that the server sends to the browser, and the browser sends back on every future request. It's how websites "remember" you.

**Server sends (in response):**
```
Set-Cookie: SESSIONID=abc123; Path=/; HttpOnly
```

**Browser sends back (in next request):**
```
Cookie: SESSIONID=abc123
```

---

## The Code

```java
public static Map<String, String> parse(String cookieHeader) {
    Map<String, String> cookies = new HashMap<>();
    if (cookieHeader == null) return cookies;

    String[] pairs = cookieHeader.split(";");
    for (String pair : pairs) {
        String[] kv = pair.trim().split("=", 2);
        if (kv.length == 2) {
            cookies.put(kv[0], kv[1]);
        }
    }
    return cookies;
}
```

**Example:**
```
Input:  "SESSIONID=abc123; theme=dark; lang=en"
Output: { "SESSIONID" → "abc123", "theme" → "dark", "lang" → "en" }
```

The header is split by `;` (multiple cookies are separated by semicolons), then each cookie is split by `=` to get the key and value.

---

## How it's used

In `Router.java` and `StaticHandler.java`:
```java
Map<String, String> cookies = Cookie.parse(conn.headers.get("cookie"));
String sessionId = cookies.get("SESSIONID");
```

This extracts the session ID so we can look up the user's session.
