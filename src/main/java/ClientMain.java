
import java.io.*;
import java.net.*;
import java.util.*;


class EchoClient {

    private final String host;
    private final int port;
    private static final Logger LOGGER =
            new Logger(EchoClient.class);

    static {
        new LoggingConfig();   // initialize logging
    }/*
    private static final Logger LOGGER;

    static {
        try {

            LOGGER = new Logger(EchoClient.class);
            LOGGER.addConsoleLogger();
            LOGGER.addFileLogger("logs/clients.log"); // file + timestamp

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startClient() {
        LOGGER.info("Connecting to server...");

        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverInput = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in));
                PrintWriter output = new PrintWriter(
                        socket.getOutputStream(), true) //automatically forces any data held in an internal
                //memory buffer to be written to the final destination
        ) {

            LOGGER.info("Connected to Echo Server.");
            LOGGER.info("Type 'exit' to stop.");
            String message;

            while (true) {
                message = userInput.readLine();

                if (message.equalsIgnoreCase("exit")) {
                    LOGGER.info("Disconnecting...");
                    break;
                }

                output.println(message);
                LOGGER.info("Server says: " + serverInput.readLine());
            }

        } catch (Exception e) {
            LOGGER.error("error: " , e);
        }
    }
}

public class ClientMain {

    public static void main(String[] args) {
        EchoClient client = new EchoClient("localhost", 1234);
        client.startClient();
    }
}


































