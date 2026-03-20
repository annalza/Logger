import java.io.*;
import java.net.*;

class EchocClient {

    private final String host;
    private final int port;

    public EchocClient(String host, int port) {
        this.host = host;
        this.port = port;
    }
    public void error(String msg) {}

    public void startClient() {
        System.out.println("Connecting to server...");

        try (
                Socket socket = new Socket(host, port);
                BufferedReader serverInput = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in));
                PrintWriter output = new PrintWriter(
                        socket.getOutputStream(), true)
        ) {

            System.out.println("Connected to Echo Server.");
            System.out.println("Type 'exit' to stop.");
            String message;

            while (true) {
                message = userInput.readLine();

                if (message.equalsIgnoreCase("exit")) {
                    System.out.println("Disconnecting...");
                    break;
                }

                output.println(message);
                System.out.println("Server says: " + serverInput.readLine());
            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }
    }
}

public class SimpleClient {

    public static void main(String[] args) {
        EchocClient client = new EchocClient("localhost", 1234);
        client.startClient();
    }
}


































