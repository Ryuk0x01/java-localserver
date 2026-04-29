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
                        return;
                    }
                    conn.body = new byte[conn.contentLength];
                }
            }

            if (conn.chunked) {
                if (!readChunkedBody(conn)) {
                    conn.readBuffer.compact();
                    return;
                }
                conn.body = conn.chunkedBody.toByteArray();

                Map<String, Object> server = Config.findServer(conn.headers.get("host"), conn.localPort);
                int maxBody = Config.getMaxBodySize(server);
                if (conn.body.length > maxBody) {
                    Router.sendError(conn, key, 413, "Payload Too Large");
                    return;
                }
                conn.readBuffer.compact();
                Router.route(conn, key);
                return;
            }

            if (conn.headersParsed && conn.contentLength > 0) {
                int available = conn.readBuffer.remaining();
                int needed = conn.contentLength - conn.bodyBytesRead;
                int toRead = Math.min(available, needed);

                conn.readBuffer.get(conn.body, conn.bodyBytesRead, toRead);
                conn.bodyBytesRead += toRead;

                if (conn.bodyBytesRead < conn.contentLength) {
                    conn.readBuffer.compact();
                    return;
                }
            }

            if (conn.headersParsed && conn.bodyBytesRead >= conn.contentLength) {
                conn.readBuffer.compact();
                Router.route(conn, key);
            } else {
                conn.readBuffer.compact();
            }

        } catch (Exception e) {
            System.err.println("Parse error: " + e.getMessage());
            Router.sendError(conn, key, 400, "Bad Request");
        }
    }

    private static boolean readChunkedBody(Connection conn) {
        try {
            while (conn.readBuffer.hasRemaining()) {
                int lineEnd = findLineEnd(conn.readBuffer, conn.readBuffer.position());
                if (lineEnd == -1) return false;

                int startPos = conn.readBuffer.position();
                byte[] sizeLineBytes = new byte[lineEnd - startPos];
                conn.readBuffer.get(sizeLineBytes);
                conn.readBuffer.get();
                conn.readBuffer.get();

                String sizeLine = new String(sizeLineBytes).trim();
                int chunkSize = Integer.parseInt(sizeLine, 16);

                if (chunkSize == 0) {
                    if (conn.readBuffer.remaining() >= 2) {
                        conn.readBuffer.get();
                        conn.readBuffer.get();
                    }
                    return true;
                }

                if (conn.readBuffer.remaining() < chunkSize + 2) {
                    conn.readBuffer.position(startPos);
                    return false;
                }

                byte[] chunkData = new byte[chunkSize];
                conn.readBuffer.get(chunkData);
                conn.readBuffer.get();
                conn.readBuffer.get();
                conn.chunkedBody.write(chunkData);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private static int findHeaderEnd(java.nio.ByteBuffer buffer) {
        int pos = buffer.position();
        for (int i = pos; i < buffer.limit() - 3; i++) {
            if (buffer.get(i) == '\r' && buffer.get(i + 1) == '\n' &&
                buffer.get(i + 2) == '\r' && buffer.get(i + 3) == '\n') {
                return i - pos;
            }
        }
        return -1;
    }

    private static int findLineEnd(java.nio.ByteBuffer buffer, int from) {
        for (int i = from; i < buffer.limit() - 1; i++) {
            if (buffer.get(i) == '\r' && buffer.get(i + 1) == '\n') {
                return i;
            }
        }
        return -1;
    }
}


