package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleIO implements IO {
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

    @Override
    public void writeLine(String line) {
        System.out.println(line);
    }

    @Override
    public void write(String text) {
        System.out.print(text);
    }

    @Override
    public String readLine() throws IOException {
        return in.readLine();
    }
}

