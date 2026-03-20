
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EchoServerThreadPool {

    private static final int PORT = 1234;
    private static final int MAX_THREADS = 2;

    private static final Logger LOGGER =
            LogManager.getLogger(EchoServerThreadPool.class);

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            LOGGER.info("Echo Server started on port: {} ", PORT);
            LOGGER.info("Maximum concurrent clients: {}", MAX_THREADS);


            while (true) {
                Socket clientSocket = serverSocket.accept();
                LOGGER.info("New client connected: {}", clientSocket.getInetAddress());

                threadPool.execute(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            LOGGER.error("Server error occurred", e);
        }
    }
}