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
}}}}