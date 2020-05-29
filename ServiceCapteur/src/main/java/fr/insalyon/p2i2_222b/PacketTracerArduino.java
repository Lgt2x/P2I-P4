package fr.insalyon.p2i2_222b;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PacketTracerArduino extends ArduinoConnector {

    protected final Integer udpListeningPort;
    protected final Integer udpSendingPort;

    protected Thread readingThread;
    protected Thread handlerThread;
    protected boolean readingThreadRunning;
    protected boolean handlerThreadRunning;

    protected DatagramSocket serverSocket;
    protected BlockingQueue<String> bufferQueue;

    protected InetAddress localhostIpAddress;

    public PacketTracerArduino(Integer udpListeningPort, Integer udpSendingPort) throws IOException {
        this.udpListeningPort = udpListeningPort;
        this.udpSendingPort = udpSendingPort;

        this.readingThreadRunning = false;

        this.localhostIpAddress = InetAddress.getByName("127.0.0.1"); // localhost
        this.serverSocket = new DatagramSocket(udpListeningPort);
    }

    public final void start() {

        this.bufferQueue = new LinkedBlockingQueue<>();

        this.handlerThread = new Thread(() -> {

            PacketTracerArduino.this.handlerThreadRunning = true;

            String queueData;

            while (PacketTracerArduino.this.handlerThreadRunning) {
                try {
                    queueData = PacketTracerArduino.this.bufferQueue.take();
                    PacketTracerArduino.this.dataHandler.accept(queueData);
                } catch (InterruptedException ex) {
                    // Ignore...
                }
            }

            PacketTracerArduino.this.handlerThreadRunning = false;
        });

        // Creation d'une tache qui va s'exécuter en parallèle du code séquentiel du main
        this.readingThread = new Thread(() -> {

            PacketTracerArduino.this.readingThreadRunning = true;

            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                while (PacketTracerArduino.this.readingThreadRunning) {
                    PacketTracerArduino.this.serverSocket.receive(receivePacket);
                    InetAddress ipAddress = receivePacket.getAddress();
                    int port = receivePacket.getPort();
                    String message = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength(), StandardCharsets.UTF_8);
                    //System.out.println("RECEIVED from " + ipAddress.getHostAddress() + ":" + port + " >> " + message);

                    try {
                        PacketTracerArduino.this.bufferQueue.put(message);
                    } catch (InterruptedException ex) {
                        // Ignore...
                    }
                }

            } catch (IOException ex) {
                if (PacketTracerArduino.this.readingThreadRunning) {
                    ex.printStackTrace(System.err);
                }
            }

            PacketTracerArduino.this.readingThreadRunning = false;
        });

        this.handlerThread.start();
        this.readingThread.start();
    }

    @Override
    public final void close() {

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

    public final void write(String line) throws IOException {
        if (this.udpSendingPort != null) {
            byte[] data = line.getBytes(StandardCharsets.UTF_8);
            this.serverSocket.send(new DatagramPacket(data, data.length, this.localhostIpAddress, this.udpSendingPort));
        } else {
            throw new IOException("No Port defined to send UDP Datagram");
        }
    }
}
