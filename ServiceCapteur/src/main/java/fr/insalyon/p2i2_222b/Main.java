package fr.insalyon.p2i2_222b;

import fr.insalyon.p2i2_222b.arduino.ArduinoConnector;
import fr.insalyon.p2i2_222b.arduino.PacketTracerArduino;
import fr.insalyon.p2i2_222b.sql.DBManager;
import fr.insalyon.p2i2_222b.util.Console;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Point d'entrée.
 */
public class Main {

    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    static Console console = new Console();
    static DBManager db = new DBManager("jdbc:h2:./test");
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

        console.err("ServiceCapteur v0.1.0");

        try {
            arduino = new PacketTracerArduino(udpListeningPort, udpSendingPort);
        } catch (IOException e) {
            console.err(e);
            System.exit(-1);
        }

        try {
            arduino.setDataHandler((data) -> {
                console.log("ARDUINO @ " + DATETIME_FORMAT.format(new Date()) + " >> " + data);
            });

            if (true) {
                console.log("Mise en place de la BDD");
                db.setupDB("../SQL/creationTables.sql");
            }

            console.err("DÉMARRAGE de la connexion (au simulateur)");
            // Connexion à l'Arduino
            arduino.start();

            console.err("BOUCLE infinie en attente du Clavier");

            // Boucle d'écriture sur l'Arduino (exécution concurrente au Thread qui écoute)
            boolean exit = false;
            while (!exit) {

            }
        } catch (IOException ex) {
            // Si un problème a eu lieu...
            console.err(ex);
        }
    }
}
