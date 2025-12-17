package Client;

import java.io.IOException;

public interface IO {
    void writeLine(String line) throws IOException;
    void write(String text) throws IOException;
    String readLine() throws IOException;
}

