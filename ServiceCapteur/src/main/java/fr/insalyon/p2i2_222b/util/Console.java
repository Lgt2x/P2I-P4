package fr.insalyon.p2i2_222b.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Console {

    private final BufferedReader input;
    private final PrintStream output = System.out;
    private final PrintStream log = System.err;

    public Console() {
        this.input = new BufferedReader(new InputStreamReader(System.in));
    }

    public String readLine(String prompt) throws IOException {
        output.print(prompt);
        output.flush();
        return input.readLine();
    }

    public void log(String line) {
        log.println(line);
    }

    public void log(Throwable th) {
        th.printStackTrace(log);
    }

    public void println(String line) {
        output.println(line);
    }
}
