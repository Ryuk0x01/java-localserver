import java.util.HashMap;
import java.util.Map;

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
}}}}}