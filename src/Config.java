import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Config {
    public static int timeout = 60;
    public static List<Map<String, Object>> servers = new ArrayList<>();
    public static Set<Integer> allPorts = new LinkedHashSet<>();

    public static void loadConfig(String path) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            JsonParser parser = new JsonParser(content);
            Map<String, Object> root = (Map<String, Object>) parser.parse();

            if (root.containsKey("timeout")) {
                timeout = ((Number) root.get("timeout")).intValue();
            }

            List<Object> serverList = (List<Object>) root.get("servers");
            if (serverList == null || serverList.isEmpty()) {
                System.err.println("Error: no servers defined in config");
                System.exit(1);
            }

            for (Object s : serverList) {
                Map<String, Object> server = (Map<String, Object>) s;

                // validate ports - check for duplicates within same server
                List<Object> ports = (List<Object>) server.get("ports");
                if (ports == null || ports.isEmpty()) {
                    System.err.println("Warning: server '" + server.get("server_name") + "' has no ports, skipping");
                    continue;
                }

                Set<Integer> seen = new HashSet<>();
                boolean hasDupe = false;
                for (Object p : ports) {
                    int port = ((Number) p).intValue();
                    if (port < 1 || port > 65535) {
                        System.err.println("Error: invalid port " + port + " in server '" + server.get("server_name") + "'");
                        hasDupe = true;
                        break;
                    }
                    if (!seen.add(port)) {
                        System.err.println("Error: duplicate port " + port + " in server '" + server.get("server_name") + "'");
                        hasDupe = true;
                        break;
                    }
                }

                if (hasDupe) {
                    System.err.println("Skipping server '" + server.get("server_name") + "' due to config error");
}}}}}