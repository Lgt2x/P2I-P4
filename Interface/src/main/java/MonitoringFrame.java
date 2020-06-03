import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

public class MonitoringFrame extends JFrame {

    private JPanel mainPanel;
    private JCheckBox realtimeCheckBox;
    private JButton refreshButton;
    private JComboBox choixStation;
    private JTable tableValeursStation;

    // Automatically-generated component references, used for clarity in GUI Designer component tree
    private JPanel realtimeAndRefreshPanel;
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel stationDataPanel;
    private JPanel graphSettingsPanel;
    private JPanel xAxisPanel;
    private JPanel yAxisPanel;

    // Chart related
    private JPanel panelGraph;
    private Chart chart;
    private XChartPanel<XYChart> chartPanel;
    private JComboBox xAxisChoice;
    private JComboBox yAxisChoice;


    private JSplitPane splitPane;
    private LectureBase bd;
    private Timer refreshTimer = new Timer(500, null);

    public MonitoringFrame() throws Exception {

        super("Monitoring de stations");

        setResizable(false);
        setSize(800, 600);
        setContentPane(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);

        this.bd = new LectureBase();
        try {
            bd.connexionBD();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de connexion à la base de données : " + bd.getDatabaseUrl(), "Problèmes de connexion", JOptionPane.ERROR_MESSAGE);
            throw e;
        }

        ActionListener dataUpdater = e -> {
            try {
                majMesures();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        };
        ActionListener chartUpdater = e -> updateChart();

        choixStation.addActionListener(dataUpdater);
        refreshButton.addActionListener(dataUpdater);
        refreshTimer.addActionListener(dataUpdater);

        realtimeCheckBox.addActionListener(e -> {

            refreshButton.setVisible(!realtimeCheckBox.isSelected());

            if (realtimeCheckBox.isSelected())
                refreshTimer.start();
            else refreshTimer.stop();
        });
        xAxisChoice.addActionListener(chartUpdater);
        yAxisChoice.addActionListener(chartUpdater);

        recupNomsStations();
        majMesures();
        buildChart(true);

        splitPane.setDividerLocation(-1);
    }

    public void recupNomsStations() throws Exception {

        ArrayList<String> resultat = bd.nomsStations();

        choixStation.removeAllItems();

        for (int i = 0; i < resultat.size(); i++)
            choixStation.addItem(resultat.get(i));

        choixStation.setSelectedIndex(0);
    }

    public void majMesures() throws Exception {

        ArrayList<Double> values = bd.derniereMesure(choixStation.getSelectedItem().toString());
        ArrayList<String> grandeurs = bd.grandeurStations(choixStation.getSelectedItem().toString());

        if (grandeurs.size() != values.size()) {

            System.out.println("gotten grandeurs: " + Arrays.toString(grandeurs.toArray()));
            System.out.println("gotten values: " + Arrays.toString(values.toArray()));

            // la ligne d'après fait une erreur...
            //throw new Exception("Erreur dans les requêtes, nombre de valeurs et nombre de grandeurs différents");
        }

        TableModel tableModel = new DefaultTableModel(new String[] {"Type", "Valeur", "Unité"}, values.size());

        Object previousChoiceXAxis = xAxisChoice.getSelectedItem();
        Object previousChoiceYAxis = yAxisChoice.getSelectedItem();
        xAxisChoice.removeAllItems();
        xAxisChoice.addItem("Temps");
        yAxisChoice.removeAllItems();
        yAxisChoice.addItem("Temps");

        for (int i = 0; i < values.size(); ++i) {

            // "temperature|°C" => ["temperature", "°C"]
            String[] strSplit = grandeurs.get(i).split("\\|");
            tableModel.setValueAt(strSplit[0], i, 0);
            tableModel.setValueAt(values.get(i), i, 1);
            tableModel.setValueAt(strSplit[1], i, 2);

            xAxisChoice.addItem(strSplit[0]);
            yAxisChoice.addItem(strSplit[0]);
        }

        xAxisChoice.setSelectedItem(previousChoiceXAxis);
        if (xAxisChoice.getSelectedItem() == null)
            xAxisChoice.setSelectedIndex(0);

        yAxisChoice.setSelectedItem(previousChoiceYAxis);
        if (yAxisChoice.getSelectedItem() == null)
            yAxisChoice.setSelectedIndex(0);

        tableValeursStation.setModel(tableModel);

        // On essaye de redimensionner la SplitPane
        int location = splitPane.getDividerLocation();
        if (location < tableValeursStation.getWidth() || location < choixStation.getWidth())
            splitPane.setDividerLocation(-1);
    }

    public void buildChart(boolean resetChart) {

        if (resetChart)
            chart = null;

        if (chart == null)
            chart = QuickChart.getChart("Graphique", "axe-x", "axe-y", "station", new double[] {0, 0}, new double[] {0, 0});

        if (chartPanel != null)
            panelGraph.remove(chartPanel);

        chartPanel = new XChartPanel(chart);
        panelGraph.add(chartPanel);

        panelGraph.updateUI();
    }

    public void updateChart() {

        String station = choixStation.getSelectedItem() != null ? choixStation.getSelectedItem().toString() : null;
        String selectedXType = xAxisChoice.getSelectedItem() != null ? xAxisChoice.getSelectedItem().toString() : null;
        String selectedYType = yAxisChoice.getSelectedItem() != null ? yAxisChoice.getSelectedItem().toString() : null;

        if (station == null || selectedXType == null || selectedYType == null || selectedXType.equalsIgnoreCase(selectedYType))
            return;

        // {xAxisValues, yAxisValues}
        double[][] dataSet = null;
        if (xAxisChoice.getSelectedIndex() == 0 || yAxisChoice.getSelectedIndex() == 0)
            dataSet = bd.getTimestampedDataset(station, xAxisChoice.getSelectedIndex() == 0 ? selectedYType : selectedXType);
        else
            dataSet = bd.getBiQuantityDataset(station, selectedXType, selectedYType);

        if (dataSet != null && dataSet[0] != null && dataSet[1] != null && dataSet[0].length == dataSet[1].length && dataSet[0].length != 0) {
            chart = QuickChart.getChart("Graphique", xAxisChoice.getSelectedItem().toString(), yAxisChoice.getSelectedItem().toString(), choixStation.getSelectedItem().toString(), dataSet[0], dataSet[1]);
            buildChart(false);
        }
    }
}