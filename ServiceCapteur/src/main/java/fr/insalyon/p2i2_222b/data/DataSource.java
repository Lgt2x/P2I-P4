package fr.insalyon.p2i2_222b.data;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Représente une source de données capable de fournir les paquets de données des stations.
 */
public abstract class DataSource implements Closeable {

    protected Consumer<String[]> dataHandler;

    /**
     * Permet à la source de données de démarrer
     * sa/ses connexions et de commencer à recevoir des données.
     *
     * @throws IOException
     */
    public abstract void start() throws IOException;

    //public abstract void write(String data) throws IOException;

    /**
     * Inscrit le callback.
     * Cet handler sera appelé à chaque fois qu'un paquet de données arrivera.
     * Attention, le callback est freeze après l'appel de {@link #start()}.
     *
     * @param handler le callback
     */
    public void setDataHandler(Consumer<String[]> handler) {
        this.dataHandler = handler;
    }
}
