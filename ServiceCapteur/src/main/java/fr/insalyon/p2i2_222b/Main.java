package fr.insalyon.p2i2_222b;

import fr.insalyon.p2i2_222b.arduino.ArduinoConnector;
import fr.insalyon.p2i2_222b.arduino.PacketTracerArduino;
import fr.insalyon.p2i2_222b.sql.DBManager;
import fr.insalyon.p2i2_222b.util.Console;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Point d'entrée.
 */
public class Main {

    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static Console console = new Console();
    static DBManager db = new DBManager("jdbc:h2:./temp/test");
    static int udpListeningPort = 20001;
    static int udpSendingPort = 20002;
    static ArduinoConnector arduino;

    public static void main(String[] args) {

        // Permet de garantir la fermeture des connexions
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (arduino != null)
                    arduino.close();
                console.log("ByeBye");
            } catch (IOException e) {
                console.err(e);
            }
        }));

        console.log("ServiceCapteur v0.1.0");

        try {
            arduino = new PacketTracerArduino(udpListeningPort, udpSendingPort);
            arduino.setDataHandler((line) -> {
                String[] data = line.split(":");
                console.log("ARDUINO @ " + DATETIME_FORMAT.format(new Date()) + " >> " + Arrays.toString(data));
            });

            if (!db.isDBSetup()) {
                console.log("Mise en place de la BDD");
                db.setupDB("/sql/creationTables.sql");
            }

            console.log("DÉMARRAGE de la connexion au simulateur");
            arduino.start();

            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            // Ya une couille dans le pâté
            console.err(ex);
            System.exit(-1);
        }
    }
}
