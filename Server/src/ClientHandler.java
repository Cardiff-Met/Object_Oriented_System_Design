import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Co2ReadingRepository repository;

    public ClientHandler(Socket clientSocket, Co2ReadingRepository repository) {
        this.clientSocket = clientSocket;
        this.repository = repository;
    }

    @Override
    public void run() {
        System.out.println("Handling client on thread " + Thread.currentThread().getName()
                + " from " + clientSocket.getRemoteSocketAddress());

        try (Socket socket = clientSocket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            handleClientConversation(socket, in, out);

        } catch (IOException e) {
            System.err.println("Error handling client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleClientConversation(Socket socket, BufferedReader in, PrintWriter out) throws IOException {
        out.println("Welcome to the CO2 logging server (multi-client version).");

        String userId = promptForText(in, out, "Please enter your User ID:");
        String postcode = promptForText(in, out, "Please enter the postcode where the reading was taken:");
        Double co2Ppm = promptForCo2Ppm(in, out, socket);

        if (co2Ppm == null) {
            // Error message already sent to client in promptForCo2Ppm
            return;
        }

        Co2Reading reading = new Co2Reading(java.time.LocalDateTime.now(), userId, postcode, co2Ppm);
        storeReading(reading, out, socket);

        System.out.println("[" + Thread.currentThread().getName() + "] Client "
                + socket.getRemoteSocketAddress() + " disconnected.");
    }

    private String promptForText(BufferedReader in, PrintWriter out, String prompt) throws IOException {
        out.println(prompt);
        return in.readLine();
    }

    private Double promptForCo2Ppm(BufferedReader in, PrintWriter out, Socket socket) throws IOException {
        out.println("Please enter the CO2 concentration in ppm:");
        String ppmInput = in.readLine();

        try {
            return Double.parseDouble(ppmInput);
        } catch (NumberFormatException e) {
            out.println("Invalid CO2 value. Connection will be closed.");
            System.err.println("[" + Thread.currentThread().getName() + "] Invalid CO2 value from "
                    + socket.getRemoteSocketAddress() + ": '" + ppmInput + "'");
            return null;
        }
    }

    private void storeReading(Co2Reading reading, PrintWriter out, Socket socket) {
        try {
            repository.append(reading);
            System.out.println("[" + Thread.currentThread().getName() + "] Stored reading for user '"
                    + reading.userId() + "' at postcode '" + reading.postcode()
                    + "' with CO2=" + reading.co2Ppm() + " ppm.");
            out.println("Reading received and stored. Thank you.");
        } catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Failed to store reading for user '"
                    + reading.userId() + "' at postcode '" + reading.postcode()
                    + "': " + e.getMessage());
            out.println("Reading received but failed to store on server.");
        }
    }
}
