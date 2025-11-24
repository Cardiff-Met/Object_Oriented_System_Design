import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class MultiClientTest {

    private static final int CLIENT_COUNT = 4;

    public static void main(String[] args) throws InterruptedException {
        String host;
        int port;

        if (args.length >= 1) {
            host = args[0];
        } else {
            host = "localhost";
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        } else {
            port = 8080;
        }

        Thread[] threads = new Thread[CLIENT_COUNT];

        for (int i = 0; i < CLIENT_COUNT; i++) {
            final int clientId = i + 1;
            threads[i] = new Thread(() -> runSingleClient(host, port, clientId), "Client-" + clientId);
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("All test clients finished.");
    }

    private static void runSingleClient(String host, int port, int clientId) {
        System.out.println("Client " + clientId + " connecting to " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = serverIn.readLine()) != null) {
                System.out.println("[Client-" + clientId + "] SERVER: " + line);

                if (line.contains("Please enter a line of text")) {
                    String message = "Hello from client " + clientId;
                    System.out.println("[Client-" + clientId + "] sending: " + message);
                    serverOut.println(message);
                }
            }

            System.out.println("Client " + clientId + " connection closed by server.");

        } catch (IOException e) {
            System.err.println("Client " + clientId + " error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
