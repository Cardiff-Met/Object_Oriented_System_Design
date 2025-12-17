package Server;

import java.io.IOException;

public interface Co2ReadingRepository {

    /**
     * Append a CO2 reading to the underlying repository.
     *
     * @param reading the CO2 reading to append (not null)
     * @throws IOException if an I/O error occurs while persisting the reading
     */
    void append(Co2Reading reading) throws IOException;
}
