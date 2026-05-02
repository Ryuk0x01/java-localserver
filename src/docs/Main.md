# Main.java — The Entry Point

📄 **File:** `src/Main.java`
🔗 **Back to:** [Main README](../../README.md)

---

## What is this file?

`Main.java` is where the entire program starts. In Java, every program needs one special method called `main()` — when you run `java -cp out Main`, Java looks for this method and begins executing from there.

Think of it as the **light switch** — it doesn't do much on its own, but it starts everything else.

---

## What does it actually do?

```java
public class Main {
    public static void main(String[] args) {
        // Step 1: Read the config file
        Config.loadConfig("config.json");

        // Step 2: Create and start the server
        Server server = new Server();
        server.start();
    }
}
```

**Two steps, that's it:**

1. **Load the config** — reads `config.json` to know which ports to listen on, what routes to set up, etc.
2. **Start the server** — hands control over to `Server.java`, which opens the ports and runs forever.

---

## Real-world analogy

Imagine you're opening a restaurant. `Main.java` is the **manager who shows up first in the morning**, reads the day's instructions (the config), then tells the staff (Server) to open the doors and start serving customers.

---

## How to run it

```bash
# Compile everything first
javac -d out src/*.java src/utils/*.java

# Then run Main
java -cp out Main
```

---

## Where does it go next?

```
Main.java
  └── calls Config.loadConfig()   → reads config.json
  └── calls Server.start()        → opens ports, starts the EventLoop
```
