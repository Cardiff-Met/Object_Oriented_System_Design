package Client;

import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicClient {

    private static final Logger logger = Logger.getLogger(BasicClient.class.getName());

    /**
     * Simple interactive client that connects to the CO2 logging server using
     * an OOP-structured design.
     * Usage: BasicClient [host] [port]
     *
     * @param args optional host and port arguments
     */
     static void main(String[] args) {
        ClientConfig config;
        try {
            config = ClientConfig.fromArgs(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid arguments: " + e.getMessage());
            System.err.println("Usage: BasicClient [host] [port]");
            return;
        }

        ConsoleIO io = new ConsoleIO();
        try (ServerConnection conn = new ServerConnection(config.host(), config.port())) {
            conn.connect(Duration.ofSeconds(60));
            PromptProcessor processor = new PromptProcessor(io, conn);
            processor.run();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Client failed: " + e.getMessage(), e);
            try {
                io.writeLine("Client failed: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }
}
