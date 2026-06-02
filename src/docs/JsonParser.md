# WIP: refining JsonParser.md
# JsonParser.java — Parsing JSON Without Libraries

📄 **File:** `src/JsonParser.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`JsonParser.java` reads and understands **JSON text** — completely from scratch, without using any external library like Gson or Jackson.

This was a requirement of the project: **no external dependencies**.

---

## What is JSON?

JSON (JavaScript Object Notation) is a simple text format for storing structured data. `config.json` is written in JSON:

```json
{
    "timeout": 60,
    "servers": [
        {
            "server_name": "localhost",
            "ports": [8080, 8081]
        }
    ]
}
```

---

## How does the parser work?

It uses a technique called **recursive descent parsing**. The parser reads one character at a time and calls different methods depending on what it sees:

| First character | What it parses |
|-----------------|----------------|
| `{` | An object (key-value pairs) |
| `[` | An array (list of values) |
| `"` | A string |
| `0-9` or `-` | A number |
| `t` or `f` | `true` or `false` |
| `n` | `null` |

---

## Example walk-through

Input: `{"port": 8080}`

1. See `{` → call `parseObject()`
2. See `"` → call `parseString()` → returns `"port"`
3. See `:` → expect a value next
4. See `8` → call `parseNumber()` → returns `8080`
5. See `}` → object is complete
6. Return `Map { "port" → 8080 }`

---

## What it returns

- JSON `{}` objects → Java `Map<String, Object>`
- JSON `[]` arrays → Java `List<Object>`
- JSON strings → Java `String`
- JSON numbers → Java `Double` or `Long`
- JSON booleans → Java `Boolean`

Then `Config.java` reads these maps and lists to configure the server.

---

## Why build it from scratch?

The project rules say **no external libraries**. Real-world servers often have this constraint too — embedding a full JSON library in a small embedded device might not be practical. Building a parser teaches you exactly how data formats work under the hood.
