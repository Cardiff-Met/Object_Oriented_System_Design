import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class BasicClient {

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        // Allow overriding host/port from command line: BasicClient 127.0.0.1 5000
        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            port = Integer.parseInt(args[1]);
        }

        System.out.println("Connecting to server " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(
                     new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userIn = new BufferedReader(
                     new InputStreamReader(System.in))) {

            System.out.println("Connected to server.");

            // Read and print the two greeting lines from the server
            String line;
            // readLine() will return null when the server closes the connection
            while ((line = serverIn.readLine()) != null) {
                System.out.println("SERVER: " + line);

                // For this basic test, after the server asks for input,
                // we read one line from the user and send it.
                if (line.contains("Please enter a line of text")) {
                    System.out.print("YOU: ");
                    String userLine = userIn.readLine();
                    serverOut.println(userLine);
                }
            }

            System.out.println("Server closed the connection.");

        } catch (IOException e) {
            System.err.println("Client error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}