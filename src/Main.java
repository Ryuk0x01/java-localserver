/**
 * TODO: Refactor and optimize connection handling and edge cases.
 */
public class Main {
    public static void main(String[] args) {
        // Load the config first
        Config.loadConfig("config.json");
        
        // Start the server with the config
        Server server = new Server();
        server.start();
    }
}
 
 
