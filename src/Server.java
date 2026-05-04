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
        try {
            selector = Selector.open();

            for (int port : Config.allPorts) {
                try {
}}}}}