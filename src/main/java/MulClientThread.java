
import java.io.*;
import java.net.*;

class EchomClient {

    private final String host;
    private final int port;
    private static final Logger LOGGER =
            new Logger(EchomClient.class);

    static {
        new LoggingConfig();   // initialize logging
    }
    /*private static final Logger LOGGER;

    static {
        try {

            LOGGER = new Logger(EchomClient.class);
            LOGGER.addConsoleLogger();
            LOGGER.addFileLogger("logs/clients.log");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/

    public EchomClient(String host, int port) {
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
                        socket.getOutputStream(), true)
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

        } catch (IOException e) {
            LOGGER.error("Client error: " ,e);
        }
    }
}

public class MulClientThread {

    public static void main(String[] args) {
        EchomClient client = new EchomClient("localhost", 1234);
        client.startClient();
    }
}


































