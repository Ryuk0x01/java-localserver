# WIP: refining StaticHandler.md
# StaticHandler.java — Serving Files

📄 **File:** `src/StaticHandler.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`StaticHandler.java` handles everything related to files on disk:
- **GET** → Read and send a file to the browser
- **POST** → Save uploaded data as a file on disk
- **DELETE** → Remove a file from disk

It also handles **directory listing** (showing a folder's contents like a file explorer).

---

## Security: Path Traversal Protection

The first thing StaticHandler does is **verify the path is safe**. Without this, a hacker could request:
```
GET /../../../../etc/passwd
```
...and potentially read system files.

The fix: resolve the full path and check it still starts with the allowed root folder:

```java
Path fullPath = Paths.get(root, relativePath).normalize();

if (!fullPath.toAbsolutePath().startsWith(Paths.get(root).toAbsolutePath())) {
    Router.sendError(conn, key, 403, "Forbidden", server);
    return;
}
```

`normalize()` collapses `..` sequences. If after normalizing the path escapes the root folder, we refuse with **403 Forbidden**.

---

## Handling GET — Serving a File

```java
byte[] content = Files.readAllBytes(file.toPath());
```

Reads the whole file into memory, then sends it. The content type (MIME type) is determined by the file extension:

| Extension | Content-Type |
|-----------|-------------|
| `.html` | `text/html` |
| `.css` | `text/css` |
| `.js` | `application/javascript` |
| `.png` | `image/png` |
| `.jpg` | `image/jpeg` |
| `.txt` | `text/plain` |
| `.json` | `application/json` |
| anything else | `application/octet-stream` |

---

## Handling POST — Uploading a File

```bash
curl -X POST -d "hello world" http://localhost:8080/upload/myfile.txt
```

The body (`hello world`) is in `conn.body`. StaticHandler saves it:

```java
file.getParentFile().mkdirs();  // Create parent directories if needed
try (FileOutputStream fos = new FileOutputStream(file)) {
    fos.write(conn.body);        // Write the bytes to disk
}
```

Then responds with **201 Created**.

---

## Handling DELETE — Removing a File

```bash
curl -X DELETE http://localhost:8080/upload/myfile.txt
```

```java
if (file.exists() && file.delete()) {
    // 200 OK — "File deleted"
} else {
    // 404 Not Found
}
```

---

## Directory Listing

If a request hits a **folder** (not a file), and the route has `"directory_listing": true`, StaticHandler generates an HTML page showing all files in that folder:

```
Index of /dir_test/
../
file1.txt
file2.txt
```

If `directory_listing` is `false`, it returns **403 Forbidden** instead.

---

## Default File

If the route has a `"default_file": "index.html"`, and the request hits a directory, the server looks for `index.html` inside that directory and serves it automatically. This is the same behavior as Apache or Nginx.

```
GET / → looks for www/index.html → serves it
```
