import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private Selector selector;
    private List<ServerSocketChannel> serverChannels = new ArrayList<>();

    public void start() {
        System.out.println("[DEBUG] start invoked");
        try {
            selector = Selector.open();

            for (int port : Config.allPorts) {
                try {
                    ServerSocketChannel serverChannel = ServerSocketChannel.open();
                    serverChannel.configureBlocking(false);
                    serverChannel.bind(new InetSocketAddress("0.0.0.0", port));
                    serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                    serverChannels.add(serverChannel);
                    System.out.println("Listening on port " + port);
                } catch (Exception e) {
                    System.err.println("Failed to bind port " + port + ": " + e.getMessage());
                    // continue with other ports, don't crash
                }
            }

            if (serverChannels.isEmpty()) {
                System.err.println("No ports could be opened, exiting");
                System.exit(1);
            }

            EventLoop loop = new EventLoop(selector);
            Router.setEventLoop(loop);
            loop.run();

        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

