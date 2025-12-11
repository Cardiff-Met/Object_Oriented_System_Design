package Client;

import java.util.Objects;

public class ClientConfig {
    private final String host;
    private final int port;

    public ClientConfig(String host, int port) {
        this.host = Objects.requireNonNull(host, "host cannot be null").trim();
        if (this.host.isEmpty()) {
            throw new IllegalArgumentException("Host cannot be empty");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
    }

    public static ClientConfig fromArgs(String[] args) {
        String host = (args != null && args.length >= 1) ? args[0] : "localhost";
        int port = (args != null && args.length >= 2) ? parsePort(args[1]) : 8080;
        return new ClientConfig(host, port);
    }

    private static int parsePort(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port: " + raw, e);
        }
    }

    public String host() { return host; }
    public int port() { return port; }
}

