import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;
import utils.Cookie;
import utils.Session;

/**
 * TODO: Refactor and optimize connection handling and edge cases.
 */
public class Router {

    private static EventLoop eventLoop;

    public static void setEventLoop(EventLoop loop) {
        eventLoop = loop;
    }

    public static void route(Connection conn, SelectionKey key) {
        Metrics.recordRequest();

        // find the right server based on Host header
        Map<String, Object> server = Config.findServer(conn.headers.get("host"), conn.localPort);
        List<Map<String, Object>> routes = Config.getRoutes(server);

        System.out.println(conn.method + " " + conn.path + " [" + server.get("server_name") + "]");

        // cookies and sessions
        Map<String, String> cookies = Cookie.parse(conn.headers.get("cookie"));
        String sessionId = cookies.get("SESSIONID");
        Session session = Session.getOrCreate(sessionId);

        // find matching route (longest prefix)
        Map<String, Object> matchedRoute = null;
        int longestMatch = 0;

        for (Map<String, Object> route : routes) {
            String routePath = (String) route.get("path");
            if (conn.path.startsWith(routePath) && routePath.length() > longestMatch) {
                longestMatch = routePath.length();
                matchedRoute = route;
            }
        }

        if (matchedRoute == null) {
            sendError(conn, key, 404, "Not Found", server);
            return;
        }

        // check allowed methods
        List<String> methods = (List<String>) matchedRoute.get("methods");
        if (methods != null && !methods.contains(conn.method)) {
            sendError(conn, key, 405, "Method Not Allowed", server);
            return;
        }

        // admin dashboard
        if (matchedRoute.containsKey("admin")) {
            String html = Metrics.toHtml();
            sendResponse(conn, key, 200, "OK", "text/html", html.getBytes(), session);
            return;
        }

        // redirect
        if (matchedRoute.containsKey("redirect")) {
            sendRedirect(conn, key, (String) matchedRoute.get("redirect"),
                        ((Number) matchedRoute.getOrDefault("redirect_code", 302)).intValue());
            return;
        }

        // CGI
        if (matchedRoute.containsKey("cgi_extension")) {
            CGIHandler.handle(conn, key, matchedRoute, eventLoop);
            return;
        }

        // static files
        StaticHandler.handle(conn, key, matchedRoute, server);
    }

    public static void sendResponse(Connection conn, SelectionKey key, int statusCode, String statusText,
                                      String contentType, byte[] body, Session session) {
        Metrics.recordStatus(statusCode);

        StringBuilder response = new StringBuilder();
        response.append("HTTP/1.1 ").append(statusCode).append(" ").append(statusText).append("\r\n");
        response.append("Content-Type: ").append(contentType).append("\r\n");
        response.append("Content-Length: ").append(body != null ? body.length : 0).append("\r\n");
        response.append("Connection: close\r\n");

        if (session != null) {
            response.append("Set-Cookie: SESSIONID=").append(session.id).append("; Path=/; HttpOnly\r\n");
        }

        response.append("\r\n");

        byte[] headers = response.toString().getBytes();
        int totalLen = headers.length + (body != null ? body.length : 0);
        conn.writeBuffer = ByteBuffer.allocate(totalLen);
        conn.writeBuffer.put(headers);
        if (body != null) conn.writeBuffer.put(body);
        conn.writeBuffer.flip();

        key.interestOps(SelectionKey.OP_WRITE);
    }

    public static void sendError(Connection conn, SelectionKey key, int statusCode, String statusText) {
        sendError(conn, key, statusCode, statusText, null);
    }

    public static void sendError(Connection conn, SelectionKey key, int statusCode, String statusText, Map<String, Object> server) {
        Metrics.recordStatus(statusCode);
        byte[] body;

        if (server != null) {
            Map<String, Object> errorPages = Config.getErrorPages(server);
            String pagePath = (String) errorPages.get(String.valueOf(statusCode));
            if (pagePath != null) {
                try {
                    body = Files.readAllBytes(Paths.get(pagePath));
                    sendResponse(conn, key, statusCode, statusText, "text/html", body, null);
                    return;
                } catch (Exception e) {}
            }
        }

        body = ("<html><body><h1>" + statusCode + " " + statusText + "</h1></body></html>").getBytes();
        sendResponse(conn, key, statusCode, statusText, "text/html", body, null);
    }

    public static void sendRedirect(Connection conn, SelectionKey key, String location, int statusCode) {
        Metrics.recordStatus(statusCode);
        String response = "HTTP/1.1 " + statusCode + " Redirect\r\n" +
                          "Location: " + location + "\r\n" +
                          "Content-Length: 0\r\n" +
                          "Connection: close\r\n\r\n";
        conn.writeBuffer = ByteBuffer.wrap(response.getBytes());
        key.interestOps(SelectionKey.OP_WRITE);
    }
}




