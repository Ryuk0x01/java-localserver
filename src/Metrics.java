import java.util.HashMap;
import java.util.Map;

/**
 * TODO: Refactor and optimize connection handling and edge cases.
 */
public class Metrics {
    public static long totalRequests = 0;
    public static long startTime = System.currentTimeMillis();
    public static int activeConnections = 0;
    public static Map<Integer, Long> statusCodes = new HashMap<>();

    public static void recordRequest() {
        totalRequests++;
    }

    public static void recordStatus(int code) {
        statusCodes.merge(code, 1L, Long::sum);
    }

    public static String toJson() {
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"uptime_seconds\":").append(uptime);
        sb.append(",\"total_requests\":").append(totalRequests);
        sb.append(",\"active_connections\":").append(activeConnections);
        sb.append(",\"status_codes\":{");
        boolean first = true;
        for (Map.Entry<Integer, Long> e : statusCodes.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue());
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    public static String toHtml() {
        long uptime = (System.currentTimeMillis() - startTime) / 1000;
        long hours = uptime / 3600;
        long mins = (uptime % 3600) / 60;
        long secs = uptime % 60;

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><title>Server Metrics</title>");
        sb.append("<style>body{font-family:monospace;background:#1a1a2e;color:#e0e0e0;padding:40px;}");
        sb.append("h1{color:#00d4ff;}table{border-collapse:collapse;margin:20px 0;}");
        sb.append("td,th{border:1px solid #333;padding:8px 16px;text-align:left;}");
        sb.append("th{background:#16213e;color:#00d4ff;}</style></head><body>");
        sb.append("<h1>Server Dashboard</h1>");
        sb.append("<table><tr><th>Metric</th><th>Value</th></tr>");
        sb.append("<tr><td>Uptime</td><td>").append(hours).append("h ").append(mins).append("m ").append(secs).append("s</td></tr>");
        sb.append("<tr><td>Total Requests</td><td>").append(totalRequests).append("</td></tr>");
        sb.append("<tr><td>Active Connections</td><td>").append(activeConnections).append("</td></tr>");
        sb.append("</table>");
        sb.append("<h2>Status Codes</h2><table><tr><th>Code</th><th>Count</th></tr>");
        for (Map.Entry<Integer, Long> e : statusCodes.entrySet()) {
            sb.append("<tr><td>").append(e.getKey()).append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        sb.append("</table>");
        sb.append("<h2>Servers</h2><table><tr><th>Name</th><th>Ports</th></tr>");
        for (Map<String, Object> server : Config.servers) {
            sb.append("<tr><td>").append(server.get("server_name")).append("</td>");
            sb.append("<td>").append(server.get("ports")).append("</td></tr>");
        }
        sb.append("</table></body></html>");
        return sb.toString();
    }
}
