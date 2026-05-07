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
        System.out.println("[DEBUG] EventLoop invoked");
        this.selector = selector;
    }

    public void run() {
        System.out.println("[DEBUG] run invoked");
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
        System.out.println("[DEBUG] acceptConnection invoked");
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
            System.err.println("Accept error: " + e.getMessage());
        }
    }

    private void readData(SelectionKey key) {
        System.out.println("[DEBUG] readData invoked");
        Connection conn = (Connection) key.attachment();
        try {
            conn.updateActivity();
            int bytesRead = conn.channel.read(conn.readBuffer);

            if (bytesRead == -1) {
                closeConnection(conn, key);
                return;
            }

            if (bytesRead > 0) {
                HttpParser.parse(conn, key);
            }
        } catch (Exception e) {
            closeConnection(conn, key);
        }
    }

    private void writeData(SelectionKey key) {
        System.out.println("[DEBUG] writeData invoked");
        Connection conn = (Connection) key.attachment();
        try {
            conn.updateActivity();

            if (conn.writeBuffer != null && conn.writeBuffer.hasRemaining()) {
                conn.channel.write(conn.writeBuffer);
            }

            if (conn.writeBuffer != null && !conn.writeBuffer.hasRemaining()) {
                closeConnection(conn, key);
            }
        } catch (Exception e) {
            closeConnection(conn, key);
        }
    }

    private void checkTimeouts() {
        System.out.println("[DEBUG] checkTimeouts invoked");
        long now = System.currentTimeMillis();
        long timeoutMs = Config.timeout * 1000L;

        Iterator<Connection> it = connections.iterator();
        while (it.hasNext()) {
            Connection conn = it.next();
            if (now - conn.lastActive > timeoutMs) {
                try { conn.channel.close(); } catch (Exception e) {}
                SelectionKey key = conn.channel.keyFor(selector);
                if (key != null) key.cancel();
                it.remove();
            }
        }
        Metrics.activeConnections = connections.size();
    }

    private void checkCGI() {
        System.out.println("[DEBUG] checkCGI invoked");
        Iterator<Connection> it = pendingCGI.iterator();
        while (it.hasNext()) {
            Connection conn = it.next();
            if (conn.cgiProcess != null && !conn.cgiProcess.isAlive()) {
                try {
                    byte[] output = Files.readAllBytes(conn.cgiOutputFile.toPath());
                    if (conn.cgiInputFile != null) conn.cgiInputFile.delete();
                    conn.cgiOutputFile.delete();

                    String outputStr = new String(output);
                    int headerEnd = outputStr.indexOf("\n\n");
                    if (headerEnd == -1) headerEnd = outputStr.indexOf("\r\n\r\n");

                    SelectionKey key = conn.channel.keyFor(selector);
                    if (key != null) {
                        if (headerEnd != -1) {
                            String body = outputStr.substring(headerEnd).trim();
                            Router.sendResponse(conn, key, 200, "OK", "text/html", body.getBytes(), null);
                        } else {
                            Router.sendResponse(conn, key, 200, "OK", "text/plain", output, null);
                        }
                    }
                } catch (Exception e) {
                    try {
                        SelectionKey key = conn.channel.keyFor(selector);
                        if (key != null) Router.sendError(conn, key, 500, "Internal Server Error");
                    } catch (Exception ex) {}
                }
                it.remove();
            }
        }
    }

    public void addPendingCGI(Connection conn) {
        System.out.println("[DEBUG] addPendingCGI invoked");
        pendingCGI.add(conn);
    }

    public void closeConnection(Connection conn, SelectionKey key) {
        System.out.println("[DEBUG] closeConnection invoked");
        try { conn.channel.close(); } catch (Exception e) {}
        if (key != null) key.cancel();
        connections.remove(conn);
        pendingCGI.remove(conn);
        Metrics.activeConnections = connections.size();
    }
}




