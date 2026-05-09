import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;
import utils.Cookie;
import utils.Session;

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
}}}