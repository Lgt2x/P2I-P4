package fr.insalyon.p2i2_222b.usb;

import fr.insalyon.p2i2_222b.util.Console;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ArduinoSimulatorManager {

    protected final Integer udpListeningPort;
    protected final Integer udpSendingPort;

    protected Thread readingThread;
    protected Thread handlerThread;
    protected boolean readingThreadRunning;
    protected boolean handlerThreadRunning;

    protected DatagramSocket serverSocket;
    protected BlockingQueue<String> bufferQueue;

    protected InetAddress localhostIpAddress;

    public ArduinoSimulatorManager(Integer udpListeningPort, Integer udpSendingPort) throws IOException {
        this.udpListeningPort = udpListeningPort;
        this.udpSendingPort = udpSendingPort;

        this.readingThreadRunning = false;

        this.localhostIpAddress = InetAddress.getByName("127.0.0.1"); // localhost
        this.serverSocket = new DatagramSocket(udpListeningPort);

    }

    public static void test() {

        // Objet matérialisant la console d'exécution (Affichage Écran / Lecture Clavier)
        final Console console = new Console();

        // Affichage sur la console
        console.log("DÉBUT du programme de Test de ArduinoSimulatorManager");

        // Spécification des Ports UDP
        Integer udpListeningPort = 20001;
        Integer udpSendingPort = 20002;

        try {

            ArduinoSimulatorManager arduino = new ArduinoSimulatorManager(udpListeningPort, udpSendingPort) {
                @Override
                protected void onData(String line) {

                    // Cette méthode est appelée AUTOMATIQUEMENT lorsque l'Arduino envoie des données
                    // Affichage sur la Console de la ligne transmise par l'Arduino
                    console.println("ARDUINO >> " + line);

                    // À vous de jouer ;-)
                    // Par exemple:
                    //   String[] data = line.split(";");
                    //   int sensorid = Integer.parseInt(data[0]);
                    //   double value = Double.parseDouble(data[1]);
                    //   ...
                }
            };

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
            arduino.stop();

        } catch (IOException ex) {
            // Si un problème a eu lieu...
            console.log(ex);
        }

    }

    public final void start() throws IOException {

        this.bufferQueue = new LinkedBlockingQueue<>();

        this.handlerThread = new Thread(() -> {

            ArduinoSimulatorManager.this.handlerThreadRunning = true;

            String queueData;

            while (ArduinoSimulatorManager.this.handlerThreadRunning) {
                try {
                    queueData = ArduinoSimulatorManager.this.bufferQueue.take();
                    ArduinoSimulatorManager.this.onData(queueData);
                } catch (InterruptedException ex) {
                    // Ignore...
                }
            }

            ArduinoSimulatorManager.this.handlerThreadRunning = false;
        });

        // Creation d'une tache qui va s'exécuter en parallèle du code séquentiel du main
        this.readingThread = new Thread(() -> {

            ArduinoSimulatorManager.this.readingThreadRunning = true;

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                while (ArduinoSimulatorManager.this.readingThreadRunning) {
                    ArduinoSimulatorManager.this.serverSocket.receive(receivePacket);
                    InetAddress ipAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), StandardCharsets.UTF_8);
                    //System.out.println("RECEIVED from " + ipAddress.getHostAddress() + ":" + port + " >> " + message);

                    try {
                        ArduinoSimulatorManager.this.bufferQueue.put(message);
                    } catch (InterruptedException ex) {
                        // Ignore...
                    }
                }

            } catch (IOException ex) {
                if (ArduinoSimulatorManager.this.readingThreadRunning) {
                    ex.printStackTrace(System.err);
                }
            }

            ArduinoSimulatorManager.this.readingThreadRunning = false;
        });

        this.handlerThread.start();
        this.readingThread.start();
    }

    public final void stop() throws IOException {

        this.readingThreadRunning = false;

        this.serverSocket.close();

        if (this.readingThread != null) {
            //Ordre d'interruption de la transmission en entrée
            this.readingThread.interrupt();
            try {
                //attente de la fin du thread (au plus 1000 ms)
                this.readingThread.join(1000);
            } catch (InterruptedException ex) {
                // Ignore
                ex.printStackTrace(System.err);
            }
        }

        this.handlerThreadRunning = false;

        if (this.handlerThread != null) {
            //Ordre d'interruption de la transmission en entrée
            this.handlerThread.interrupt();
            try {
                //attente de la fin du thread (au plus 1000 ms)
                this.handlerThread.join(1000);
            } catch (InterruptedException ex) {
                // Ignore
                ex.printStackTrace(System.err);
            }
        }
    }

    protected void onData(String line) {
        // Cette méthode est à surcharger dans une classe qui hérite de cette classe

        // Affichage de la ligne transmise par l'Arduino
        // System.err.println("Data from Arduino: " + line);
    }

    public final void write(String line) throws IOException {
        if (this.udpSendingPort != null) {
            byte[] data = line.getBytes(StandardCharsets.UTF_8);
            this.serverSocket.send(new DatagramPacket(data, data.length, this.localhostIpAddress, this.udpSendingPort));
        } else {
            throw new IOException("No Port defined to send UDP Datagram");
        }
    }
}
