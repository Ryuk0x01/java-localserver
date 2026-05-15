package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TODO: Refactor and optimize connection handling and edge cases.
 */
public class Session {
    private static Map<String, Session> sessions = new HashMap<>();

    public String id;
    public Map<String, Object> data;

    private Session(String id) {
        this.id = id;
        this.data = new HashMap<>();
    }

    public static Session getOrCreate(String sessionId) {
        if (sessionId != null && sessions.containsKey(sessionId)) {
            return sessions.get(sessionId);
        }
        String newId = UUID.randomUUID().toString();
        Session s = new Session(newId);
        sessions.put(newId, s);
        return s;
    }

    public static Session get(String sessionId) {
        return sessions.get(sessionId);
    }
}
