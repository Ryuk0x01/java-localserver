import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.nio.channels.SelectionKey;
import java.io.FileOutputStream;
import utils.Session;
import utils.Cookie;

public class StaticHandler {

    public static void handle(Connection conn, SelectionKey key, Map<String, Object> route, Map<String, Object> server) {
        String root = (String) route.get("root");
        if (root == null) root = ".";

        String routePath = (String) route.get("path");
        String relativePath = conn.path.substring(routePath.length());
        if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);

        Path fullPath = Paths.get(root, relativePath).normalize();

        if (!fullPath.toAbsolutePath().startsWith(Paths.get(root).toAbsolutePath().normalize())) {
            Router.sendError(conn, key, 403, "Forbidden", server);
            return;
        }

        File file = fullPath.toFile();

        // session for response
        Map<String, String> cookies = Cookie.parse(conn.headers.get("cookie"));
        Session session = Session.getOrCreate(cookies.get("SESSIONID"));

        if (conn.method.equals("POST")) {
            try {
                if (conn.body != null) {
                    file.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(conn.body);
                    }
                    Router.sendResponse(conn, key, 201, "Created", "text/plain", "File uploaded".getBytes(), session);
                    return;
                }
            } catch (Exception e) {
                Router.sendError(conn, key, 500, "Internal Server Error", server);
                return;
            }
        }

        if (conn.method.equals("DELETE")) {
            if (file.exists() && file.delete()) {
                Router.sendResponse(conn, key, 200, "OK", "text/plain", "File deleted".getBytes(), session);
            } else {
                Router.sendError(conn, key, 404, "Not Found", server);
            }
            return;
        }

        if (!file.exists()) {
            Router.sendError(conn, key, 404, "Not Found", server);
            return;
        }

        if (file.isDirectory()) {
            String defaultFile = (String) route.get("default_file");
            if (defaultFile != null) {
                File index = new File(file, defaultFile);
                if (index.exists()) {
                    serveFile(conn, key, index, session);
                    return;
                }
            }

            Boolean dirListing = (Boolean) route.get("directory_listing");
            if (dirListing != null && dirListing) {
                serveDirectoryListing(conn, key, file, conn.path, session);
            } else {
                Router.sendError(conn, key, 403, "Forbidden", server);
            }
            return;
        }

        serveFile(conn, key, file, session);
    }

    private static void serveFile(Connection conn, SelectionKey key, File file, Session session) {
        try {
            byte[] content = Files.readAllBytes(file.toPath());
            String contentType = "application/octet-stream";
            String name = file.getName().toLowerCase();
            if (name.endsWith(".html")) contentType = "text/html";
            else if (name.endsWith(".css")) contentType = "text/css";
            else if (name.endsWith(".js")) contentType = "application/javascript";
            else if (name.endsWith(".png")) contentType = "image/png";
            else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) contentType = "image/jpeg";
            else if (name.endsWith(".gif")) contentType = "image/gif";
            else if (name.endsWith(".txt")) contentType = "text/plain";
            else if (name.endsWith(".json")) contentType = "application/json";
            else if (name.endsWith(".ico")) contentType = "image/x-icon";

            Router.sendResponse(conn, key, 200, "OK", contentType, content, session);
        } catch (Exception e) {
            Router.sendError(conn, key, 500, "Internal Server Error");
        }
    }

    private static void serveDirectoryListing(Connection conn, SelectionKey key, File dir, String path, Session session) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Index of ").append(path).append("</title></head><body>");
        html.append("<h1>Index of ").append(path).append("</h1><hr><pre>");

        if (!path.equals("/")) {
            html.append("<a href=\"../\">../</a>\n");
        }

        File[] files = dir.listFiles();
        if (files != null) {
            java.util.Arrays.sort(files);
            for (File f : files) {
                html.append("<a href=\"").append(f.getName()).append(f.isDirectory() ? "/" : "").append("\">")
                    .append(f.getName()).append(f.isDirectory() ? "/" : "").append("</a>\n");
            }
        }

        html.append("</pre><hr></body></html>");
        Router.sendResponse(conn, key, 200, "OK", "text/html", html.toString().getBytes(), session);
    }
}

