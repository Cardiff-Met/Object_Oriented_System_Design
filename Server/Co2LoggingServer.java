package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Co2LoggingServer {

    private static final Logger logger = Logger.getLogger(Co2LoggingServer.class.getName());

    private final int port;
    private final int maxClients;
    private final Co2ReadingRepository repository;
    private final ExecutorService threadPool;

    /**
     * Create a CO2 logging server instance.
     *
     * @param port       TCP port to listen on
     * @param maxClients maximum number of concurrent client handler threads
     * @param repository repository used to persist readings
     */
    public Co2LoggingServer(int port, int maxClients, Co2ReadingRepository repository) {
        this.port = port;
        this.maxClients = maxClients;
        this.repository = repository;
        this.threadPool = Executors.newFixedThreadPool(maxClients);
    }

    /**
     * Start accepting client connections and dispatch handlers to a fixed
     * thread pool. This method blocks until the server socket is closed or an
     * unrecoverable I/O error occurs.
     */
    public void start() {
        logger.info("Starting CO2 logging server on port " + port + " (max clients: " + maxClients + ")...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server is listening on port " + port);

            // Use a shutdown-aware loop so static analysis can see an exit condition
            while (!threadPool.isShutdown()) {
                Socket clientSocket = serverSocket.accept();
                logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());

                threadPool.submit(new ClientHandler(clientSocket, repository));
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server error: " + e.getMessage(), e);
        } finally {
            shutdown();
        }
    }

    /**
     * Initiate an orderly shutdown of the server's thread pool. Already-
     * submitted tasks will continue to run.
     */
    public void shutdown() {
        threadPool.shutdown();
        logger.info("Server shutting down.");
    }
}
