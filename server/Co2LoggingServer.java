package server;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Co2LoggingServer {

    private static final Logger logger = Logger.getLogger(Co2LoggingServer.class.getName());

    private static final class QueuedConnection {
        private final Socket socket;
        private final boolean queued;
        private final int queuePositionAtJoin;

        private QueuedConnection(Socket socket, boolean queued, int queuePositionAtJoin) {
            this.socket = socket;
            this.queued = queued;
            this.queuePositionAtJoin = queuePositionAtJoin;
        }
    }

    private final int port;
    private final int maxClients;
    private final Co2ReadingRepository repository;
    private volatile ServerSocket serverSocket;

    // Worker pool has exactly maxClients threads. They pull sockets from the queue and handle them.
    private final ExecutorService workerPool;
    private final BlockingQueue<QueuedConnection> waitingQueue;
    private final AtomicInteger activeClients;
    private final AtomicBoolean running;

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
        this.workerPool = Executors.newFixedThreadPool(maxClients);
        this.waitingQueue = new LinkedBlockingQueue<>();
        this.activeClients = new AtomicInteger(0);
        this.running = new AtomicBoolean(false);
    }

    /**
     * Start accepting client connections.
     *
     * The server will allow only {@code maxClients} active client sessions at once.
     * If more clients connect, they will be placed into an in-app waiting queue and
     * immediately receive a message telling them they are queued.
     */
    public void start() {
        logger.info("Starting CO2 logging server on port " + port + " (max clients: " + maxClients + ")...");

        if (!running.compareAndSet(false, true)) {
            logger.warning("Server already running; start() call ignored.");
            return;
        }

        // Start worker threads that consume connections from the queue.
        for (int i = 0; i < maxClients; i++) {
            workerPool.submit(this::workerLoop);
        }

        try (ServerSocket ss = new ServerSocket(port)) {
            this.serverSocket = ss;
            logger.info("Server is listening on port " + port);

            while (running.get() && !workerPool.isShutdown()) {
                Socket clientSocket = ss.accept();
                logger.info("Accepted connection from " + clientSocket.getRemoteSocketAddress());

                boolean willWait = activeClients.get() >= maxClients || !waitingQueue.isEmpty();
                int position = willWait ? waitingQueue.size() + 1 : 0;

                if (willWait) {
                    if (!sendQueuedMessage(clientSocket, position)) {
                        // Client disconnected / failed to write the queue message.
                        closeQuietly(clientSocket);
                        continue;
                    }
                }

                waitingQueue.offer(new QueuedConnection(clientSocket, willWait, position));
            }

        } catch (IOException e) {
            if (running.get()) {
                logger.log(Level.SEVERE, "Server error: " + e.getMessage(), e);
            } else {
                logger.info("Server socket closed; stopping accept loop.");
            }
        } finally {
            shutdownInternal();
        }
    }

    private void workerLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            QueuedConnection qc;
            try {
                qc = waitingQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            int nowActive = activeClients.incrementAndGet();
            try {
                if (qc.queued) {
                    sendNowServingMessage(qc.socket);
                    logger.info("Dequeued client (joined at position " + qc.queuePositionAtJoin + "). Active clients: " + nowActive);
                }

                new ClientHandler(qc.socket, repository).run();

            } finally {
                activeClients.decrementAndGet();
                closeQuietly(qc.socket);
            }
        }
    }

    private boolean sendQueuedMessage(Socket socket, int position) {
        try {
            ClientSession session = new ClientSession(
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    new PrintWriter(socket.getOutputStream(), true)
            );
            session.sendLine("Server is busy (max " + maxClients + " clients at a time). You are in the queue (position " + position + "). Please wait...");
            return true; // any I/O error will throw and be caught below
        } catch (IOException e) {
            return false;
        }
    }

    private void sendNowServingMessage(Socket socket) {
        try {
            ClientSession session = new ClientSession(
                    new BufferedReader(new InputStreamReader(socket.getInputStream())),
                    new PrintWriter(socket.getOutputStream(), true)
            );
            session.sendLine("A space is now available. You are now being served...");
        } catch (IOException ignored) {}
    }

    private void closeQuietly(Socket socket) {
        if (socket == null) return;
        try {
            socket.close();
        } catch (IOException ignored) {}
    }

    /**
     * Initiate shutdown from outside.
     */
    public void stop() {
        if (!running.compareAndSet(true, false)) {
            return; // already stopped or never started
        }
        closeServerSocket();
        shutdownInternal();
    }

    private void shutdownInternal() {
        running.set(false);
        closeServerSocket();
        workerPool.shutdownNow();

        // Best-effort cleanup of any queued sockets.
        List<QueuedConnection> remaining = new ArrayList<>();
        waitingQueue.drainTo(remaining);
        for (QueuedConnection qc : remaining) {
            closeQuietly(qc.socket);
        }

        try {
            workerPool.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        logger.info("Server shutting down.");
    }

    private void closeServerSocket() {
        ServerSocket ss = this.serverSocket;
        if (ss != null && !ss.isClosed()) {
            try {
                ss.close();
            } catch (IOException ignored) {}
        }
    }
}
