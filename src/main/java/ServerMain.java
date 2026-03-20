import java.io.*;
import java.net.*;

class EchoServer {

    private final int port;
    private static final Logger LOGGER =
            new Logger(EchoServer.class);

    static {
        new LoggingConfig();   // initialize logging
    }/*
    private static final Logger LOGGER;
    static {
        try {

            LOGGER = new Logger(EchoServer.class);
            LOGGER.addConsoleLogger();
            LOGGER.addFileLogger("logs/server.log"); // file + timestamp


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/


    public EchoServer(int port) {
        this.port = port;
    }

    public void startServer() {
        LOGGER.info("Starting Echo Server...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            LOGGER.info("Server listening on port {}"+port);
            LOGGER.warn("server");

            while (true) {
                LOGGER.info("Waiting for client...");


                Socket socket = serverSocket.accept();
                LOGGER.info("Client connected: {}"+socket.getInetAddress());


                ClientHandler handler= new ClientHandler(socket);
                handler.run();
            }
        } catch (IOException e) {
            LOGGER.error("Error handling client: {}", e);
        }
    }
}


public class ServerMain {

    public static void main(String[] args) {
        EchoServer server = new EchoServer(1234);
        server.startServer();

    }
}