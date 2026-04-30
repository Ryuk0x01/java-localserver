import java.util.*;

public class JsonParser {

    private String json;
    private int pos;

    public JsonParser(String json) {
        this.json = json.trim();
        this.pos = 0;
    }

    public Object parse() {
        return readValue();
    }

    private Object readValue() {
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
    private char next() { return json.charAt(pos++); }

    private void skipSpaces() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
    }

    private Map<String, Object> readObject() {
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
}}}