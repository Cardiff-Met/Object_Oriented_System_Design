import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Co2LoggingServer {

    private final int port;
    private final int maxClients;
    private final Co2ReadingRepository repository;
    private final ExecutorService threadPool;

    public Co2LoggingServer(int port, int maxClients, Co2ReadingRepository repository) {
        this.port = port;
        this.maxClients = maxClients;
        this.repository = repository;
        this.threadPool = Executors.newFixedThreadPool(maxClients);
    }

    public void start() {
        System.out.println("Starting CO2 logging server on port " + port + " (max clients: " + maxClients + ")...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress());

                threadPool.submit(new ClientHandler(clientSocket, repository));
            }

        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        threadPool.shutdown();
        System.out.println("Server shutting down.");
    }
}


