import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class CGIHandler {

    public static void handle(Connection conn, SelectionKey key, Map<String, Object> route, EventLoop loop) {
        String root = (String) route.get("root");
        if (root == null) root = ".";

        String routePath = (String) route.get("path");
        String cgiExt = (String) route.get("cgi_extension");
        String relativePath = conn.path.substring(routePath.length());
        if (relativePath.startsWith("/")) relativePath = relativePath.substring(1);
        if (relativePath.isEmpty()) {
            Router.sendError(conn, key, 404, "Not Found");
            return;
        }

        File scriptFile = Paths.get(root, relativePath).normalize().toFile();

        if (!scriptFile.exists() || !scriptFile.isFile()) {
            Router.sendError(conn, key, 404, "CGI Script Not Found");
            return;
        }

        try {
            File inputFile = null;
            if (conn.body != null && conn.body.length > 0) {
                inputFile = File.createTempFile("cgi_in_", ".tmp");
                try (FileOutputStream fos = new FileOutputStream(inputFile)) {
                    fos.write(conn.body);
}}}}}