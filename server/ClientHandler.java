package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private static final int SOCKET_READ_TIMEOUT_MS = 60_000;

    private final Socket socket;
    private final Co2ReadingRepository repository;

    /**
     * Create a handler for a connected client socket.
     *
     * @param socket     the connected client socket
     * @param repository repository used to persist CO2 readings
     */
    public ClientHandler(Socket socket, Co2ReadingRepository repository) {
        this.socket = socket;
        this.repository = repository;
    }

    /**
     * Entry point for the handler thread. Manages the lifecycle of the
     * connection, reads input from the client, and ensures resources are
     * closed when the client disconnects or an error occurs.
     */
    @Override
    public void run() {
        log(Level.INFO, "New client connected.");

        try {
            socket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true))
            {
                ClientSession session = new ClientSession(in, out);
                Clock clock = new SystemClock();

                Co2LoggingSession loggingSession = new Co2LoggingSession(session, repository, clock);
                loggingSession.run();
            }

        } catch (IOException e) {
            log(Level.WARNING, "Socket error: " + e.getMessage());
        } finally {
            log(Level.INFO, "Client disconnected.");
        }
    }

    /**
     * Log a message together with thread and remote address information.
     *
     * @param lvl logging level to use
     * @param msg message to log
     */
    private void log(Level lvl, String msg) {
        logger.log(lvl, "[" + Thread.currentThread().getName() + "] " +
                socket.getRemoteSocketAddress() + " → " + msg);
    }
}
