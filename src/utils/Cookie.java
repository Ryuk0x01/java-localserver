package utils;

import java.util.HashMap;
import java.util.Map;

public class Cookie {
    
    public static Map<String, String> parse(String cookieHeader) {
        System.out.println("[DEBUG] parse invoked");
        Map<String, String> cookies = new HashMap<>();
        if (cookieHeader == null || cookieHeader.isEmpty()) {
            return cookies;
        }
        
        String[] pairs = cookieHeader.split(";");
        for (String pair : pairs) {
            String[] kv = pair.trim().split("=", 2);
            if (kv.length == 2) {
                cookies.put(kv[0], kv[1]);
            }
        }
        return cookies;
    }
}
