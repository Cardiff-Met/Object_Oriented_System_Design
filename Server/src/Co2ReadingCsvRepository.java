import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Co2ReadingCsvRepository implements Co2ReadingRepository {

    private final Path filePath;

    public Co2ReadingCsvRepository(String fileName) {
        this.filePath = Paths.get(fileName);
    }

    /**
     * Append a reading to the CSV file. This method is synchronized so that only
     * one thread writes to the file at a time.
     */
    public synchronized void append(Co2Reading reading) throws IOException {
        boolean fileExists = Files.exists(filePath);

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath.toFile(), true))) {
            if (!fileExists) {
                writer.println("timestamp,userId,postcode,co2Ppm");
            }
            writer.println(reading.toCsvLine());
        }
    }
}
