
import java.io.*;
import java.net.*;

public class MultiThreadedEchoServer {

    private final int port;
    private static final Logger LOGGER =
            new Logger(MultiThreadedEchoServer.class);

    static {
        new LoggingConfig();   // initialize logging
    }/*
    private static final Logger LOGGER;

    static {
        try {
            LOGGER = new Logger(MultiThreadedEchoServer.class);
            LOGGER.addConsoleLogger();
            LOGGER.addFileLogger("logs/server.log");
            LOGGER.addTimestampLogger("logs/server.log");
        }catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    public MultiThreadedEchoServer(int port) {
        this.port = port;
    }

    public void startServer() {
        LOGGER.info("Multi-threaded Echo Server starting...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            LOGGER.info("Server listening on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                LOGGER.info("Client connected: {}"+  socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket);
                new Thread(handler).start();
            }

        } catch (IOException e) {
            LOGGER.error("Server error: {}", e);
        }
    }

    public static void main(String[] args) {
        MultiThreadedEchoServer server = new MultiThreadedEchoServer(1234);
        server.startServer();
    }
}