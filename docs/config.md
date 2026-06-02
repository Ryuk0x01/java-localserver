# WIP: refining config.md
# config.json — Server Configuration

📄 **File:** `config.json`
🔗 **Back to:** [Main README](README.md)

---

## What is this file?

`config.json` is the **control panel** of the server. You edit this file to change how the server behaves — no need to touch any Java code.

---

## Full Example

```json
{
    "timeout": 60,
    "servers": [
        {
            "server_name": "localhost",
            "host": "0.0.0.0",
            "ports": [8080, 8081],
            "default_server": true,
            "client_max_body_size": 10485760,
            "error_pages": {
                "404": "error_pages/404.html",
                "500": "error_pages/500.html"
            },
            "routes": [
                {
                    "path": "/",
                    "methods": ["GET"],
                    "root": "www",
                    "default_file": "index.html"
                },
                {
                    "path": "/upload",
                    "methods": ["GET", "POST", "DELETE"],
                    "root": "www/uploads",
                    "directory_listing": true
                },
                {
                    "path": "/cgi-bin",
                    "methods": ["GET", "POST"],
                    "root": "cgi-bin",
                    "cgi_extension": ".py"
                },
                {
                    "path": "/redirect",
                    "methods": ["GET"],
                    "redirect": "/",
                    "redirect_code": 301
                }
            ]
        }
    ]
}
```

---

## Top-Level Fields

| Field | Type | Description |
|-------|------|-------------|
| `timeout` | number | Seconds before idle connections are closed |
| `servers` | array | List of server blocks (virtual hosts) |

---

## Server Block Fields

| Field | Type | Description |
|-------|------|-------------|
| `server_name` | string | The hostname this server responds to (e.g. `localhost`, `test.com`) |
| `host` | string | The IP to bind on. Use `"0.0.0.0"` to accept connections from anywhere |
| `ports` | array | List of port numbers to listen on |
| `default_server` | boolean | If `true`, this server handles requests whose `Host` header doesn't match any other server |
| `client_max_body_size` | number | Max size of request body in **bytes**. `10485760` = 10MB |
| `error_pages` | object | Map of error codes to custom HTML page paths |

---

## Route Fields

Each object in `routes` defines one URL rule:

| Field | Type | Description |
|-------|------|-------------|
| `path` | string | URL prefix this route matches (e.g. `/upload`) |
| `methods` | array | Allowed HTTP methods: `"GET"`, `"POST"`, `"DELETE"` |
| `root` | string | Folder on disk to serve files from |
| `default_file` | string | File to serve when the request hits a folder (like `index.html`) |
| `directory_listing` | boolean | If `true`, shows folder contents when no `default_file` is found |
| `cgi_extension` | string | If set, treats requests as CGI scripts. E.g. `".py"` runs Python |
| `redirect` | string | If set, redirects requests to this URL |
| `redirect_code` | number | HTTP status for redirect: `301` (permanent) or `302` (temporary) |
| `admin` | boolean | If `true`, serves the metrics dashboard |

---

## Common Recipes

**Serve a static website:**
```json
{
    "path": "/",
    "methods": ["GET"],
    "root": "www",
    "default_file": "index.html"
}
```

**Allow file uploads:**
```json
{
    "path": "/files",
    "methods": ["GET", "POST", "DELETE"],
    "root": "www/files",
    "directory_listing": true
}
```

**Run Python CGI scripts:**
```json
{
    "path": "/scripts",
    "methods": ["GET", "POST"],
    "root": "cgi-bin",
    "cgi_extension": ".py"
}
```

**Permanent redirect:**
```json
{
    "path": "/old-page",
    "methods": ["GET"],
    "redirect": "/new-page",
    "redirect_code": 301
}
```

**Add a second virtual host on the same port:**
```json
{
    "server_name": "mysite.com",
    "ports": [8080],
    "routes": [...]
}
```
Then test it with:
```bash
curl --resolve mysite.com:8080:127.0.0.1 http://mysite.com:8080/
```

---

## Error: Duplicate Ports

If you accidentally set the same port twice in one server block:
```json
"ports": [8080, 8080]
```
The server will print an error and **skip that server block**, but continue running with all other valid servers.
