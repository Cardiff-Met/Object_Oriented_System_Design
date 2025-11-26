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

    public ClientHandler(Socket socket, Co2ReadingRepository repo) {
        this.socket = socket;
        this.repo = repo;
    }

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

    private static Optional<Double> parseCo2(String s) {
        try {
            double v = Double.parseDouble(s);
            return (v >= 0 && !Double.isInfinite(v)) ? Optional.of(v) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

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

    private void log(Level lvl, String msg) {
        logger.log(lvl, "[" + Thread.currentThread().getName() + "] " +
                socket.getRemoteSocketAddress() + " → " + msg);
    }
}
