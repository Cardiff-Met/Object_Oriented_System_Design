package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicClient {

    private static final Logger logger = Logger.getLogger(BasicClient.class.getName());

    static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        // Allow overriding host/port from command line: BasicClient 127.0.0.1 5000
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        logger.info("Connecting to server " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userIn = new BufferedReader(
                     new InputStreamReader(System.in))) {

            logger.info("Connected to server.");

            String line;
            // readLine() will return null when the server closes the connection
            while ((line = serverIn.readLine()) != null) {
                System.out.println("SERVER: " + line);

                // If the server line looks like a prompt (ends with ':'),
                // read one line from the user and send it.
                if (line.trim().endsWith(":")) {
                    System.out.print("YOU: ");
                    String userLine = userIn.readLine();
                    if (userLine == null) {
                        break;
                    }
                    serverOut.println(userLine);
                }
            }

            logger.info("Server closed the connection.");

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Client error: " + e.getMessage(), e);
        }
    }
}







