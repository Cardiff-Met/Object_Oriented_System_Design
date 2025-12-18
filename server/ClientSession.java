package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.function.Function;

public final class ClientSession {

    private final BufferedReader in;
    private final PrintWriter out;

    public ClientSession(BufferedReader in, PrintWriter out) {
        this.in = in;
        this.out = out;
    }

    public void sendLine(String line) {
        out.println(line);
    }

    /**
     * Prompt the client repeatedly until a valid response is parsed or the
     * client disconnects/times out.
     *
     * @return the parsed value, or null if the client disconnected or timed out
     */
    public <T> T askUntilValid(String prompt,
                              Function<String, Optional<T>> parser,
                              String errorMsg) throws IOException {

        while (true) {
            sendLine(prompt);

            String line;
            try {
                line = in.readLine();
            } catch (SocketTimeoutException e) {
                sendLine("Timed out due to inactivity. Goodbye.");
                return null;
            }

            if (line == null) {
                return null;
            }

            Optional<T> parsed = parser.apply(line.trim());
            if (parsed.isPresent()) {
                return parsed.get();
            }

            sendLine(errorMsg);
        }
    }
}
