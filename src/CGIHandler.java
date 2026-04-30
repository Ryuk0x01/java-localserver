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
                }
            }

            File outputFile = File.createTempFile("cgi_out_", ".tmp");

            // pick interpreter based on extension
            String interpreter;
            if (cgiExt.equals(".py")) {
                interpreter = "python3";
            } else if (cgiExt.equals(".sh")) {
                interpreter = "/bin/sh";
            } else if (cgiExt.equals(".pl")) {
                interpreter = "perl";
            } else {
                interpreter = "python3";
            }

            ProcessBuilder pb = new ProcessBuilder(interpreter, scriptFile.getAbsolutePath());
            Map<String, String> env = pb.environment();
            env.put("REQUEST_METHOD", conn.method);
            env.put("PATH_INFO", conn.path);
            env.put("QUERY_STRING", conn.queryString != null ? conn.queryString : "");
            env.put("SERVER_NAME", "localhost");
            env.put("SERVER_PROTOCOL", "HTTP/1.1");
            if (conn.headers.containsKey("content-length"))
                env.put("CONTENT_LENGTH", conn.headers.get("content-length"));
            if (conn.headers.containsKey("content-type"))
                env.put("CONTENT_TYPE", conn.headers.get("content-type"));

            for (Map.Entry<String, String> h : conn.headers.entrySet()) {
                env.put("HTTP_" + h.getKey().toUpperCase().replace("-", "_"), h.getValue());
            }

            if (inputFile != null) pb.redirectInput(inputFile);
            pb.redirectOutput(outputFile);
            pb.redirectErrorStream(true);

            Process p = pb.start();

            conn.cgiProcess = p;
            conn.cgiOutputFile = outputFile;
            conn.cgiInputFile = inputFile;

            key.interestOps(0);
            loop.addPendingCGI(conn);

        } catch (Exception e) {
            e.printStackTrace();
            Router.sendError(conn, key, 500, "CGI Execution Error");
        }
    }
}

