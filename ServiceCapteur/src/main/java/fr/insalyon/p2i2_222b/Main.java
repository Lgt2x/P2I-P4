package fr.insalyon.p2i2_222b;

import fr.insalyon.p2i2_222b.util.Console;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Point d'entrée.
 */
public class Main {

    public static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static void main(String[] args) {

        // Objet matérialisant la console d'exécution (Affichage Écran / Lecture Clavier)
        final Console console = new Console();

        // Affichage sur la console
        console.log("ServiceCapteur v0.1.0");

        // Spécification des Ports UDP
        Integer udpListeningPort = 20001;
        Integer udpSendingPort = 20002;

        try {
            ArduinoConnector arduino = new PacketTracerArduino(udpListeningPort, udpSendingPort);
            arduino.setDataHandler((data) -> {
                console.println("ARDUINO @ " + DATETIME_FORMAT.format(new Date()) + " >> " + data);
            });

            console.log("DÉMARRAGE de la connexion (au simulateur)");
            // Connexion à l'Arduino
            arduino.start();

            console.log("BOUCLE infinie en attente du Clavier");
            // Boucle d'écriture sur l'Arduino (exécution concurrente au Thread qui écoute)
            boolean exit = false;

            while (!exit) {

                // Lecture Clavier de la ligne saisie par l'Utilisateur
                String line = console.readLine("Envoyer une ligne (ou 'stop') > ");

                if (line.length() != 0) {

                    // Affichage sur l'écran
                    console.log("CLAVIER >> " + line);

                    // Test de sortie de boucle
                    exit = line.equalsIgnoreCase("stop");

                    if (!exit) {
                        // Envoi sur l'Arduino du texte saisi au Clavier
                        arduino.write(line);
                    }
                }
            }

            console.log("ARRÊT de la connexion");
            // Fin de la connexion à l'Arduino
            arduino.close();

        } catch (IOException ex) {
            // Si un problème a eu lieu...
            console.log(ex);
        }
    }
}
