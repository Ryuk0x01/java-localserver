import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import utils.Cookie;
import utils.Session;

public class EventLoop {

    private Selector selector;
    private List<Connection> connections = new ArrayList<>();
    private List<Connection> pendingCGI = new ArrayList<>();

    public EventLoop(Selector selector) {
        this.selector = selector;
    }

    public void run() {
        System.out.println("Starting event loop...");
        while (true) {
            try {
                selector.select(500);

                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            acceptConnection(key);
                        } else if (key.isReadable()) {
                            readData(key);
                        } else if (key.isWritable()) {
                            writeData(key);
                        }
                    } catch (Exception e) {
                        if (key.attachment() instanceof Connection) {
                            closeConnection((Connection) key.attachment(), key);
                        }
                    }
                }

                checkTimeouts();
                checkCGI();

            } catch (Exception e) {
                System.err.println("Event loop error: " + e.getMessage());
            }
        }
    }

    private void acceptConnection(SelectionKey key) {
        try {
            ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
            SocketChannel clientChannel = serverChannel.accept();
            if (clientChannel != null) {
                clientChannel.configureBlocking(false);
                int port = ((InetSocketAddress) serverChannel.getLocalAddress()).getPort();
                Connection conn = new Connection(clientChannel, port);
                clientChannel.register(selector, SelectionKey.OP_READ, conn);
                connections.add(conn);
                Metrics.activeConnections = connections.size();
            }
        } catch (Exception e) {
}}}