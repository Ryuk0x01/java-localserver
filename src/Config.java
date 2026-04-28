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
                    continue;
                }

                // validate body size
                if (!server.containsKey("client_max_body_size")) {
                    server.put("client_max_body_size", 1048576L);
                }

                servers.add(server);
                for (Object p : ports) {
                    allPorts.add(((Number) p).intValue());
                }
            }

            if (servers.isEmpty()) {
                System.err.println("Error: no valid servers after config validation");
                System.exit(1);
            }

            System.out.println("Config loaded: " + servers.size() + " server(s), ports: " + allPorts);

        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Map<String, Object> findServer(String hostHeader, int port) {
        String hostname = "";
        if (hostHeader != null) {
            hostname = hostHeader.contains(":") ? hostHeader.substring(0, hostHeader.indexOf(":")) : hostHeader;
        }

        // exact match: server_name + port
        for (Map<String, Object> server : servers) {
            String name = (String) server.getOrDefault("server_name", "");
            List<Object> ports = (List<Object>) server.get("ports");
            boolean hasPort = false;
            for (Object p : ports) if (((Number) p).intValue() == port) hasPort = true;

            if (hasPort && name.equalsIgnoreCase(hostname)) {
                return server;
            }
        }

        // fallback: default_server that has this port
        for (Map<String, Object> server : servers) {
            Boolean isDefault = (Boolean) server.getOrDefault("default_server", false);
            List<Object> ports = (List<Object>) server.get("ports");
            boolean hasPort = false;
            for (Object p : ports) if (((Number) p).intValue() == port) hasPort = true;

            if (hasPort && isDefault) return server;
        }

        // fallback: first server with this port
        for (Map<String, Object> server : servers) {
            List<Object> ports = (List<Object>) server.get("ports");
            for (Object p : ports) {
                if (((Number) p).intValue() == port) return server;
            }
        }

        return servers.get(0);
    }

    public static int getMaxBodySize(Map<String, Object> server) {
        return ((Number) server.getOrDefault("client_max_body_size", 1048576)).intValue();
    }

    public static Map<String, Object> getErrorPages(Map<String, Object> server) {
        return (Map<String, Object>) server.getOrDefault("error_pages", new HashMap<>());
    }

    public static List<Map<String, Object>> getRoutes(Map<String, Object> server) {
        return (List<Map<String, Object>>) server.getOrDefault("routes", new ArrayList<>());
    }
}
