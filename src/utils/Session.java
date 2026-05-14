package utils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Session {
    private static Map<String, Session> sessions = new HashMap<>();

    public String id;
    public Map<String, Object> data;

}