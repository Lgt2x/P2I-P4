package fr.insalyon.p2i2_222b.data;

/**
 * Agit comme une station en fournissant des données à intervalle régulier.
 */
public class StationFaker extends DataSource {

    private Thread stationThread;
    private int intervalleMs;

    public StationFaker(int intervalleMs) {
        this.intervalleMs = intervalleMs;
    }

    @Override
    public void start() {
        stationThread = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(intervalleMs);
                    // Genere des valeurs au pif
                    int idStation = (int) Math.ceil(Math.random() * 4);
                    int idCapteur = ((int) Math.ceil(Math.random() * 7)) * idStation;
                    double value = Math.random() * 400;
                    dataHandler.accept(new String[] {
                            Integer.toString(idStation),
                            Integer.toString(idCapteur),
                            Double.toString(value) });
                }
            } catch (InterruptedException e) {

            }
        });
        stationThread.setDaemon(true);
        stationThread.start();
    }

    @Override
    public void close() {
        stationThread.interrupt();
    }
}
