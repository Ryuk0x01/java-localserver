import java.util.*;

public class JsonParser {

    private String json;
    private int pos;

    public JsonParser(String json) {
        System.out.println("[DEBUG] JsonParser invoked");
        this.json = json.trim();
        this.pos = 0;
    }

    public Object parse() {
        System.out.println("[DEBUG] parse invoked");
        return readValue();
    }

    private Object readValue() {
        System.out.println("[DEBUG] readValue invoked");
        skipSpaces();
        char c = peek();
        if (c == '{') return readObject();
        if (c == '[') return readArray();
        if (c == '"') return readString();
        if (c == 't' || c == 'f') return readBoolean();
        if (c == 'n') return readNull();
        return readNumber();
    }

    private char peek() { return json.charAt(pos); }
        System.out.println("[DEBUG] peek invoked");
    private char next() { return json.charAt(pos++); }
        System.out.println("[DEBUG] next invoked");

    private void skipSpaces() {
        System.out.println("[DEBUG] skipSpaces invoked");
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
    }

    private Map<String, Object> readObject() {
        System.out.println("[DEBUG] readObject invoked");
        Map<String, Object> map = new LinkedHashMap<>();
        next();
        skipSpaces();
        if (peek() == '}') { next(); return map; }
        while (true) {
            skipSpaces();
            String key = readString();
            skipSpaces();
            next(); // :
            Object value = readValue();
            map.put(key, value);
            skipSpaces();
            if (peek() == ',') next();
            else break;
        }
        skipSpaces();
        next();
        return map;
    }

    private List<Object> readArray() {
        System.out.println("[DEBUG] readArray invoked");
        List<Object> list = new ArrayList<>();
        next();
        skipSpaces();
        if (peek() == ']') { next(); return list; }
        while (true) {
            list.add(readValue());
            skipSpaces();
            if (peek() == ',') next();
            else break;
        }
        skipSpaces();
        next();
        return list;
    }

    private String readString() {
        System.out.println("[DEBUG] readString invoked");
        next();
        StringBuilder sb = new StringBuilder();
        while (peek() != '"') {
            char c = next();
            if (c == '\\') {
                char esc = next();
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case 'n': sb.append('\n'); break;
                    case 't': sb.append('\t'); break;
                    case 'r': sb.append('\r'); break;
                    case '/': sb.append('/'); break;
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        next();
        return sb.toString();
    }

    private Number readNumber() {
        System.out.println("[DEBUG] readNumber invoked");
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < json.length() && (Character.isDigit(peek()) || peek() == '.')) pos++;
        String num = json.substring(start, pos);
        if (num.contains(".")) return Double.parseDouble(num);
        return Long.parseLong(num);
    }

    private Boolean readBoolean() {
        System.out.println("[DEBUG] readBoolean invoked");
        if (json.startsWith("true", pos)) { pos += 4; return true; }
        pos += 5;
        return false;
    }

    private Object readNull() {
        System.out.println("[DEBUG] readNull invoked");
        pos += 4;
        return null;
    }
}
 
 
