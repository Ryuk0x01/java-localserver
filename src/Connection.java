import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

public class Connection {
    public SocketChannel channel;
    public ByteBuffer readBuffer;
    public ByteBuffer writeBuffer;
    public long lastActive;
    public int localPort;

    public boolean headersParsed = false;
    public String method;
    public String path;
    public String queryString = "";
    public String protocol;
    public Map<String, String> headers = new HashMap<>();
    public byte[] body;
    public int bodyBytesRead = 0;
    public int contentLength = 0;
    public boolean chunked = false;
    public ByteArrayOutputStream chunkedBody;

    public Process cgiProcess;
    public File cgiOutputFile;
    public File cgiInputFile;

    public Connection(SocketChannel channel, int localPort) {
        this.channel = channel;
        this.localPort = localPort;
        this.readBuffer = ByteBuffer.allocate(16384);
        this.lastActive = System.currentTimeMillis();
    }

    public void updateActivity() {
        this.lastActive = System.currentTimeMillis();
    }

    public void reset() {
        headersParsed = false;
        method = null;
        path = null;
        queryString = "";
        protocol = null;
        headers.clear();
        body = null;
        bodyBytesRead = 0;
        contentLength = 0;
        chunked = false;
        chunkedBody = null;
        cgiProcess = null;
        cgiOutputFile = null;
        cgiInputFile = null;
        readBuffer.clear();
        writeBuffer = null;
    }
}




