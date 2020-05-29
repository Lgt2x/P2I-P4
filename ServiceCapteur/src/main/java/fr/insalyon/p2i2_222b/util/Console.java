package fr.insalyon.p2i2_222b.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class Console {

    private final BufferedReader in;
    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    public Console() {
        this.in = new BufferedReader(new InputStreamReader(System.in));
    }

    public String readLine(String prompt) throws IOException {
        out.print(prompt);
        out.flush();
        return in.readLine();
    }

    public void err(String line) {
        err.println(line);
    }

    public void err(Throwable th) {
        th.printStackTrace(err);
    }

    public void log(String line) {
        out.println(line);
    }
}
