import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        System.out.println("Handling client on thread " + Thread.currentThread().getName()
                + " from " + clientSocket.getRemoteSocketAddress());

        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println("Welcome to the CO2 logging server (multi-client version).");
            out.println("Please enter a line of text, then press Enter:");

            String clientInput = in.readLine();
            System.out.println("[" + Thread.currentThread().getName() + "] Received from client "
                    + socket.getRemoteSocketAddress() + ": " + clientInput);

            out.println("Server received: " + clientInput);

            System.out.println("[" + Thread.currentThread().getName() + "] Client "
                    + socket.getRemoteSocketAddress() + " disconnected.");

        } catch (IOException e) {
            System.err.println("Error handling client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
