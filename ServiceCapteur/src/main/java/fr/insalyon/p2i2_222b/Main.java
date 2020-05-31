package fr.insalyon.p2i2_222b;

import fr.insalyon.p2i2_222b.data.DataSource;
import fr.insalyon.p2i2_222b.data.StationFaker;
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
    static DBManager db = new DBManager("jdbc:h2:./../db/test");
    static DataSource arduino;

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
            // TODO create a station pool
            //arduino = new StationPacketTracer(20001, 20002);
            arduino = new StationFaker(1500);
            arduino.setDataHandler((data) -> {
                console.log(DATETIME_FORMAT.format(new Date()) + " >> " + Arrays.toString(data));
                db.saveMeasure(Integer.parseInt(data[1]), Double.parseDouble(data[2]));
            });

            console.log("Démarrage de la connexion au réseau de capteurs.");
            arduino.start();

            // Fait attendre ce thread jusqu'à un ctrl-c
            Thread.currentThread().join();
        } catch (IOException | InterruptedException ex) {
            // Ya une couille dans le pâté
            console.err(ex);
            System.exit(-1);
        }
    }
}
