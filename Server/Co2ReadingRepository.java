package Server;

import java.io.IOException;

public interface Co2ReadingRepository {

    void append(Co2Reading reading) throws IOException;
}
