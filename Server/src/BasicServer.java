import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicServer {

    private static final int MAX_CLIENTS = 4;

    public static void main(String[] args) {
        int port = 8080;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        System.out.println("Starting server on port " + port + " (max clients: " + MAX_CLIENTS + ")...");

        ExecutorService threadPool = Executors.newFixedThreadPool(MAX_CLIENTS);

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                // Block until a client connects
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());

                // Handle each client in its own thread
                threadPool.submit(new ClientHandler(clientSocket));
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            threadPool.shutdown();
        }
    }
}
