package fr.insalyon.p2i2_222b;

import java.io.Closeable;
import java.io.IOException;

public interface ArduinoConnector extends Closeable {

    void start() throws IOException;
    void write(String data) throws IOException;
}
