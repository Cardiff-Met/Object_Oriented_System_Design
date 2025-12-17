package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerConnection implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(ServerConnection.class.getName());

    private final String host;
    private final int port;
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;

    public ServerConnection(String host, int port) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
    }

    public void connect(Duration timeout) throws IOException {
        logger.info("Connecting to server " + host + ":" + port + "...");
        this.socket = new Socket(host, port);
        if (timeout != null) {
            socket.setSoTimeout((int) Math.min(Integer.MAX_VALUE, timeout.toMillis()));
        }
        this.serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.serverOut = new PrintWriter(socket.getOutputStream(), true);
        logger.info("Connected.");
    }

    public String readLine() throws IOException {
        ensureOpen();
        return serverIn.readLine();
    }

    public void writeLine(String line) {
        ensureOpen();
        serverOut.println(line);
        if (serverOut.checkError()) {
            logger.log(Level.WARNING, "Error writing to server");
        }
    }

    private void ensureOpen() {
        if (socket == null || socket.isClosed()) {
            throw new IllegalStateException("Connection is not open");
        }
    }

    @Override
    public void close() {
        try { if (serverIn != null) serverIn.close(); } catch (IOException ignored) {}
        if (serverOut != null) serverOut.close();
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
        logger.info("Connection closed.");
    }
}

