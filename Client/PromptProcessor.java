package Client;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PromptProcessor {
    private static final Logger logger = Logger.getLogger(PromptProcessor.class.getName());

    private final IO io;
    private final ServerConnection connection;

    public PromptProcessor(IO io, ServerConnection connection) {
        this.io = Objects.requireNonNull(io);
        this.connection = Objects.requireNonNull(connection);
    }

    public void run() {
        try {
            String line;
            while ((line = connection.readLine()) != null) {
                io.writeLine("SERVER: " + line);

                if (looksLikePrompt(line)) {
                    io.write("YOU: ");
                    String userInput = io.readLine();
                    if (userInput == null) {
                        io.writeLine("Input closed. Exiting.");
                        break;
                    }
                    userInput = sanitize(userInput);
                    if (!userInput.isEmpty()) {
                        connection.writeLine(userInput);
                    } else {
                        io.writeLine("Ignored empty input.");
                    }
                }
            }
            io.writeLine("Server closed the connection.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "I/O error: " + e.getMessage(), e);
            ioSafeWriteLine("Error: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.log(Level.SEVERE, "Connection error: " + e.getMessage(), e);
            ioSafeWriteLine("Connection error: " + e.getMessage());
        }
    }

    private boolean looksLikePrompt(String line) {
        return line != null && line.trim().endsWith(":");
    }

    private String sanitize(String s) {
        return s == null ? "" : s.trim();
    }

    private void ioSafeWriteLine(String msg) {
        try { io.writeLine(msg); } catch (IOException ignored) {}
    }
}

