package fr.insalyon.p2i2_222b;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

public abstract class ArduinoConnector implements Closeable {

    protected Consumer<String> dataHandler;

    public abstract void start() throws IOException;

    public abstract void write(String data) throws IOException;

    public void setDataHandler(Consumer<String> handler) {
        this.dataHandler = handler;
    }
}
