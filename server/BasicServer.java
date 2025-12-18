package server;

public class BasicServer {

    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_CLIENTS = 4;
    private static final String CSV_FILE_NAME = "co2_readings.csv";

    /**
     * Simple main to launch the CO2 logging server. Optionally accepts a
     * single argument to override the listening port.
     *
     * Usage: BasicServer [port]
     *
     * @param args optional command-line arguments
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        Co2ReadingRepository repository = new Co2ReadingCsvRepository(CSV_FILE_NAME);
        Co2LoggingServer server = new Co2LoggingServer(port, MAX_CLIENTS, repository);

        // Stop gracefully on JVM shutdown (e.g., Ctrl+C).
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }
}
