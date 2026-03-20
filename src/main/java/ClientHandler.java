import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientHandler implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String clientAddress = socket.getInetAddress().toString();

        try (BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {

            String message;
            while ((message = input.readLine()) != null) {
                LOGGER.info("Client {} says: {}", clientAddress, message);

                output.println("Echo: " + message);
            }

        } catch (IOException e) {
            LOGGER.warn("Connection lost with client {}: {}", clientAddress, e.getMessage());
            LOGGER.info("Client disconnected: {} ", clientAddress);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                LOGGER.error("Failed to close client socket for {}", clientAddress, e);
            }
            LOGGER.info("Client {} connection closed", clientAddress);
        }
    }
}