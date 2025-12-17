package Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());
    private static final int SOCKET_READ_TIMEOUT_MS = 60_000;

    private final Socket socket;
    private final Co2ReadingRepository repo;

    /**
     * Create a handler for a connected client socket.
     *
     * @param socket the connected client socket
     * @param repo   repository used to persist CO2 readings
     */
    public ClientHandler(Socket socket, Co2ReadingRepository repo) {
        this.socket = socket;
        this.repo = repo;
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
                handleClient(in, out);
            }

        } catch (IOException e) {
            log(Level.WARNING, "Socket error: " + e.getMessage());
        } finally {
            log(Level.INFO, "Client disconnected.");
        }
    }

    /**
     * Interact with the client to obtain user details and a CO2 reading,
     * then store the reading via the repository. If the client disconnects
     * or times out at any point, the method returns early.
     *
     * @param in  reader for client input
     * @param out writer for client output
     * @throws IOException if an I/O error occurs while communicating with the client
     */
    private void handleClient(BufferedReader in, PrintWriter out) throws IOException {
        out.println("Welcome to the CO2 logging server.");

        String userId = ask(in, out, "Enter your User ID:",
                s -> s.isEmpty() ? Optional.empty() : Optional.of(s),
                "User ID cannot be empty.");

        if (userId == null) return;

        String postcode = ask(in, out, "Enter the postcode:",
                s -> s.isEmpty() ? Optional.empty() : Optional.of(s),
                "Postcode cannot be empty.");

        if (postcode == null) return;

        Double co2 = ask(in, out, "Enter the CO2 concentration (ppm):",
                ClientHandler::parseCo2,
                "Invalid value. Please enter a non-negative number.");

        if (co2 == null) return;

        Co2Reading reading = new Co2Reading(
                java.time.LocalDateTime.now(), userId, postcode, co2);

        storeReading(reading, out);
    }

    /**
     * Prompt the client repeatedly until a valid response is parsed or the
     * client disconnects/times out. The provided parser maps the raw input
     * string to an Optional containing the parsed value on success.
     *
     * @param in      reader for client input
     * @param out     writer for client output
     * @param prompt  prompt message to send to the client
     * @param parser  function that converts a trimmed input string to Optional<T>
     * @param errorMsg message to send when parsing fails
     * @param <T>     type of the parsed value
     * @return the parsed value, or null if the client disconnected or timed out
     * @throws IOException if an I/O error occurs while reading from the client
     */
    private <T> T ask(BufferedReader in, PrintWriter out, String prompt,
                      Function<String, Optional<T>> parser, String errorMsg) throws IOException {

        while (true) {
            out.println(prompt);
            String line;

            try {
                line = in.readLine();
            } catch (SocketTimeoutException e) {
                out.println("Timed out due to inactivity. Goodbye.");
                log(Level.INFO, "Timed out.");
                return null;
            }

            if (line == null) return null;

            Optional<T> parsed = parser.apply(line.trim());
            if (parsed.isPresent()) return parsed.get();

            out.println(errorMsg);
        }
    }

    /**
     * Parse a string into a CO2 double value, returning an Optional containing
     * the value when it is a non-negative finite number.
     *
     * @param s input string to parse
     * @return Optional containing the parsed double, or empty if invalid
     */
    private static Optional<Double> parseCo2(String s) {
        try {
            double v = Double.parseDouble(s);
            return (v >= 0 && !Double.isInfinite(v)) ? Optional.of(v) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Persist the provided reading using the repository and inform the client
     * of the result.
     *
     * @param r   the CO2 reading to store
     * @param out writer used to send status messages to the client
     */
    private void storeReading(Co2Reading r, PrintWriter out) {
        try {
            repo.append(r);
            out.println("Reading stored. Thank you.");
            log(Level.INFO, "Stored reading: " + r);
        } catch (IOException e) {
            out.println("Failed to store reading.");
            log(Level.SEVERE, "Error storing reading: " + e.getMessage());
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
