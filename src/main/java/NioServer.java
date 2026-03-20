import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NioServer {

    private final int port;
    private static final int BUFFER_SIZE = 1024;
    private static final Logger LOGGER = LogManager.getLogger(NioServer.class);

    public NioServer(int port) {this.port = port;}

    public void startServer() {

        LOGGER.info("NIO Echo Server starting");

        try (
                Selector selector = Selector.open();
                ServerSocketChannel serverChannel = ServerSocketChannel.open()
        ) {
            serverChannel.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LOGGER.info("Server listening on port {}", port);

            while (true) {
                selector.select();
                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

                while (keys.hasNext()) {
                    SelectionKey key = keys.next();
                    keys.remove();

                    if (key.isAcceptable()) {
                        handleAccept(key, selector);
                    }

                    if (key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Server error: {}", e.getMessage());
        }
    }

    private void handleAccept(SelectionKey key, Selector selector) throws IOException {

        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();

        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);

        LOGGER.info("Client connected: {}", clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {

        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            LOGGER.info("Client disconnected: {} ", clientChannel.getRemoteAddress());
            clientChannel.close();
            key.cancel();
            return;
        }

        buffer.flip();
        String message = new String(buffer.array(), 0, buffer.limit());

        LOGGER.info("Received from {}: {}", clientChannel.getRemoteAddress(), message);
        clientChannel.write(buffer);
    }

    public static void main(String[] args) {
        NioServer server = new NioServer(1234);
        server.startServer();
    }
}