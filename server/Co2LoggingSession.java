package server;

import java.io.IOException;
import java.util.Optional;

public final class Co2LoggingSession {

    private final ClientSession session;
    private final Co2ReadingRepository repository;
    private final Clock clock;

    public Co2LoggingSession(ClientSession session, Co2ReadingRepository repository, Clock clock) {
        this.session = session;
        this.repository = repository;
        this.clock = clock;
    }

    public void run() throws IOException {
        session.sendLine("Welcome to the CO2 logging server.");

        String userId = session.askUntilValid(
                "Enter your User ID:",
                s -> s.isEmpty() ? Optional.empty() : Optional.of(s),
                "User ID cannot be empty.");

        if (userId == null) return;

        Employee employee = EmployeeFactory.fromUserId(userId);

        String postcode = session.askUntilValid(
                "Enter the postcode:",
                s -> s.isEmpty() ? Optional.empty() : Optional.of(s),
                "Postcode cannot be empty.");

        if (postcode == null) return;

        Double co2 = session.askUntilValid(
                "Enter the CO2 concentration (ppm):",
                Co2LoggingSession::parseCo2,
                "Invalid value. Please enter a non-negative number.");

        if (co2 == null) return;

        Co2Reading reading = new Co2Reading(clock.now(), employee.userId(), postcode, co2);

        try {
            repository.append(reading);
            session.sendLine("Reading stored. Thank you.");
        } catch (IOException e) {
            session.sendLine("Failed to store reading.");
            throw e;
        }
    }

    private static Optional<Double> parseCo2(String s) {
        try {
            double v = Double.parseDouble(s);
            return (v >= 0 && !Double.isInfinite(v)) ? Optional.of(v) : Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
