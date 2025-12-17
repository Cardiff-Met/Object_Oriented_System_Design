package server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Co2ReadingCsvRepository implements Co2ReadingRepository {

    private final Path filePath;

    /**
     * Create a new CSV repository backed by the specified file name.
     * If the file or its parent directories do not exist they will be created.
     * The CSV file will be initialized with a header line when first created.
     *
     * @param fileName path to the CSV file to use for storage
     * @throws RuntimeException if the repository file or directories cannot be created
     */
    public Co2ReadingCsvRepository(String fileName) {
        this.filePath = Paths.get(fileName);

        // Ensure parent directory exists and create file with header if missing.
        try {
            Path parent = filePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(filePath)) {
                try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), false))) {
                    writer.println("timestamp,userId,postcode,co2Ppm");
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize CSV repository file: " + filePath, e);
        }
    }

    /**
     * Append a reading to the CSV file. This method is synchronized so that only
     * one thread writes to the file at a time. The method will append a single
     * line representing the reading in CSV format.
     *
     * @param reading the CO2 reading to append
     * @throws IOException if an I/O error occurs while writing
     */
    @Override
    public synchronized void append(Co2Reading reading) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), true))) {
            writer.println(reading.toCsvLine());
        }
    }
}
