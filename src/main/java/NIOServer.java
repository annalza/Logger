import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NIOServer {

    private static final int PORT = 1234;
    private static final int BUFFER_SIZE = 1024;

    private static final Logger LOGGER = LogManager.getLogger(NIOServer.class);

    public static void main(String[] args) {
        try (
                Selector selector = Selector.open();
                ServerSocketChannel serverChannel = ServerSocketChannel.open()
        ) {
            serverChannel.bind(new InetSocketAddress(PORT));
            serverChannel.configureBlocking(false);

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            LOGGER.info("NIO Echo Server started on port {}", PORT);

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
            LOGGER.error("ERROR {}", e.getMessage());
        }
    }

    private static void handleAccept(SelectionKey key, Selector selector) throws IOException {

        ServerSocketChannel client = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel= client.accept();
        clientChannel.configureBlocking(false);

        clientChannel.register(selector, SelectionKey.OP_READ);

        LOGGER.info("Client connected: {}", clientChannel.getRemoteAddress());
    }

    private static void handleRead(SelectionKey key) throws IOException {

        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

        int bytesRead = clientChannel.read(buffer);

        if (bytesRead == -1) {
            LOGGER.info("Client disconnected: {} ", clientChannel.getRemoteAddress());
            clientChannel.close();
            key.cancel();
            return;
        }

        //It sets the limit to the current position, read operation only accesses the data that was actually
        // written, not the entire buffer's capacity. sets the position back to 0.
        // This prepares the buffer to be read from the beginning of the newly written data
        buffer.flip();

        clientChannel.write(buffer);
    }
}
