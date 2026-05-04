public class Main {
    public static void main(String[] args) {
        System.out.println("[DEBUG] main invoked");
        // Load the config first
        Config.loadConfig("config.json");
        
        // Start the server with the config
        Server server = new Server();
        server.start();
    }
}
 
 
