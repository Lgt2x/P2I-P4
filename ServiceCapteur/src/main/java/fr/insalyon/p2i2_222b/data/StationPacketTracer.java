package fr.insalyon.p2i2_222b.data;

import fr.insalyon.p2i2_222b.Main;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;

public class StationPacketTracer extends DataSource {

    private final int udpListeningPort;
    private final int udpSendingPort;
    private final DatagramSocket serverSocket;
    private final BlockingQueue<String> transferQueue;
    private final InetAddress localhostIpAddress;
    private Thread readingThread;
    private Thread handlerThread;
    private boolean readingThreadRunning;
    private boolean handlerThreadRunning;

    public StationPacketTracer(int udpListeningPort, int udpSendingPort) throws IOException {
        this.udpListeningPort = udpListeningPort;
        this.udpSendingPort = udpSendingPort;

        this.readingThreadRunning = false;

        this.localhostIpAddress = InetAddress.getByName("127.0.0.1"); // localhost
        this.serverSocket = new DatagramSocket(udpListeningPort);
        this.transferQueue = new LinkedTransferQueue<>();
    }

    public final void start() {

        handlerThread = new Thread(() -> {

            handlerThreadRunning = true;

            while (handlerThreadRunning) {
                try {
                    dataHandler.accept(transferQueue.take().split(":"));
                } catch (InterruptedException ignored) { }
            }

            handlerThreadRunning = false;
        });

        // Creation d'une tache qui va s'exécuter en parallèle du code séquentiel du main
        readingThread = new Thread(() -> {

            readingThreadRunning = true;

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                while (readingThreadRunning) {
                    serverSocket.receive(receivePacket);
                    String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), StandardCharsets.UTF_8);

                    try {
                        transferQueue.put(message);
                    } catch (InterruptedException ignored) { }
                }

            } catch (IOException ex) {
                if (readingThreadRunning) {
                    Main.console.err(ex);
                }
            }

            readingThreadRunning = false;
        });

        handlerThread.start();
        readingThread.start();
    }

    @Override
    public final void close() {

        readingThreadRunning = false;

        serverSocket.close();

        if (readingThread != null) {
            //Ordre d'interruption de la transmission en entrée
            readingThread.interrupt();
            try {
                //attente de la fin du thread (au plus 1000 ms)
                readingThread.join(1000);
            } catch (InterruptedException ex) {
                Main.console.err(ex);
            }
        }

        handlerThreadRunning = false;

        if (handlerThread != null) {
            //Ordre d'interruption de la transmission en entrée
            handlerThread.interrupt();
            try {
                //attente de la fin du thread (au plus 1000 ms)
                handlerThread.join(1000);
            } catch (InterruptedException ex) {
                Main.console.err(ex);
            }
        }
    }

    public void write(String line) throws IOException {
        byte[] data = line.getBytes(StandardCharsets.UTF_8);
        serverSocket.send(new DatagramPacket(data, data.length, localhostIpAddress, udpSendingPort));
    }
}
