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

}