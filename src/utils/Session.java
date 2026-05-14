package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private static Map<String, Session> sessions = new HashMap<>();

    public String id;
    public Map<String, Object> data;

    private Session(String id) {
        System.out.println("[DEBUG] Session invoked");
        this.id = id;
        this.data = new HashMap<>();
    }

    public static Session getOrCreate(String sessionId) {
        System.out.println("[DEBUG] getOrCreate invoked");
        if (sessionId != null && sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        }
        String newId = UUID.randomUUID().toString();
        Session s = new Session(newId);
        sessions.put(newId, s);
        return s;
    }

    public static Session get(String sessionId) {
        System.out.println("[DEBUG] get invoked");
        return sessions.get(sessionId);
    }
}
