package fr.insalyon.p2i2_222b;

import fr.insalyon.p2i2_222b.data.DataSource;
import fr.insalyon.p2i2_222b.data.StationFaker;
import fr.insalyon.p2i2_222b.data.StationPacketTracer;
import fr.insalyon.p2i2_222b.sql.DBManager;
import fr.insalyon.p2i2_222b.util.Console;
import org.h2.tools.Server;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Point d'entrée. On relie les données reçues à la base de donnée.
 */
public class Main {

    private static final boolean useH2 = false;
    private static final boolean fakeData = false;
    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    public static Console console = new Console();
    private static DBManager db;
    private static DataSource arduino;
    private static Server h2server;

    public static void main(String[] args) {

        console.log("ServiceCapteur v0.1.0");

        try {
            // Le premier permet l'utilisation de mysql, le second utilise une bdd intégrée
            if (useH2) {
                h2server = Server.createTcpServer("-tcpPort", "9123", "-tcpAllowOthers", "-tcpDaemon", "-baseDir", "./../db", "-ifNotExists");
                h2server.start();
                db = new DBManager("jdbc:h2:tcp://localhost:9123/../db/P2I2P4");
            } else
                db = new DBManager("mysql");

            if (fakeData)
                arduino = new StationFaker(500);
            else
                arduino = new StationPacketTracer(20001, 20002);

            arduino.setDataHandler((data) -> {
                console.log(DATETIME_FORMAT.format(new Date()) + " >> " + Arrays.toString(data));
                db.saveMeasure(Integer.parseInt(data[1]), Double.parseDouble(data[2]));
            });

            // Permet de garantir la fermeture des connexions
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    if (arduino != null)
                        arduino.close();
                    console.log("ByeBye");
                    db.close();
                } catch (IOException e) {
                    console.err(e);
                }
            }));

            console.log("Démarrage de la connexion au réseau de capteurs.");
            arduino.start();

            // Fait attendre ce thread jusqu'à un ctrl-c
            Thread.currentThread().join();

        } catch (IOException | InterruptedException | SQLException ex) {
            // Ya une couille dans le pâté
            console.err(ex);
            System.exit(-1);
        }
    }
}
