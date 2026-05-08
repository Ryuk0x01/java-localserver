import java.io.ByteArrayOutputStream;
import java.nio.channels.SelectionKey;
import java.util.Map;

public class HttpParser {

    public static void parse(Connection conn, SelectionKey key) {
        conn.readBuffer.flip();

        try {
            if (!conn.headersParsed) {
                int headerEnd = findHeaderEnd(conn.readBuffer);
                if (headerEnd == -1) {
                    conn.readBuffer.compact();
                    return;
                }

                byte[] headerBytes = new byte[headerEnd];
                conn.readBuffer.get(headerBytes);
                String headerString = new String(headerBytes);

                String[] lines = headerString.split("\r\n");
                if (lines.length > 0) {
                    String[] requestLine = lines[0].split(" ");
                    if (requestLine.length >= 2) {
                        conn.method = requestLine[0];
                        String fullPath = requestLine[1];
                        conn.protocol = requestLine.length > 2 ? requestLine[2] : "HTTP/1.1";

                        int qMark = fullPath.indexOf('?');
                        if (qMark >= 0) {
                            conn.path = fullPath.substring(0, qMark);
                            conn.queryString = fullPath.substring(qMark + 1);
                        } else {
                            conn.path = fullPath;
                            conn.queryString = "";
                        }
                    } else {
                        Router.sendError(conn, key, 400, "Bad Request");
                        return;
                    }

                    for (int i = 1; i < lines.length; i++) {
                        String line = lines[i];
                        if (line.isEmpty()) continue;
                        int colonIndex = line.indexOf(":");
                        if (colonIndex > 0) {
                            String name = line.substring(0, colonIndex).trim().toLowerCase();
                            String value = line.substring(colonIndex + 1).trim();
                            conn.headers.put(name, value);
                        }
                    }
                }

                // skip \r\n\r\n
                conn.readBuffer.get();
                conn.readBuffer.get();
                conn.readBuffer.get();
                conn.readBuffer.get();

                conn.headersParsed = true;

                String te = conn.headers.getOrDefault("transfer-encoding", "");
                if (te.toLowerCase().contains("chunked")) {
                    conn.chunked = true;
                    conn.chunkedBody = new ByteArrayOutputStream();
                }

                if (conn.headers.containsKey("content-length")) {
                    conn.contentLength = Integer.parseInt(conn.headers.get("content-length"));

                    // find correct server to check body limit
                    Map<String, Object> server = Config.findServer(conn.headers.get("host"), conn.localPort);
                    int maxBody = Config.getMaxBodySize(server);

                    if (conn.contentLength > maxBody) {
                        Router.sendError(conn, key, 413, "Payload Too Large");
}}}}}}