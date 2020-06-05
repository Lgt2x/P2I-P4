import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

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

        setResizable(true);
        setSize(1000, 700);
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
                majMesures(0);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        };
        ActionListener chartUpdater = e -> buildChart(true);

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
        majMesures(0);
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

    public void majMesures(int timesUsed) throws Exception {

        ArrayList<Double> values = bd.derniereMesure(choixStation.getSelectedItem().toString());
        ArrayList<String> grandeurs = bd.grandeurStations(choixStation.getSelectedItem().toString());

        if (grandeurs.size() != values.size()) {

            System.out.println("gotten grandeurs: " + Arrays.toString(grandeurs.toArray()));
            System.out.println("gotten values: " + Arrays.toString(values.toArray()));

            // TODO supprimer les grandeurs sans valeurs, et si aucune grandeur n'a de valeur mettre un msg d'erreur
            JOptionPane.showMessageDialog(this, "<html>Mesure manquante sur un des capteurs de la station '" + choixStation.getSelectedItem() + "', il est possiblement défectueux !<html>", "Valeurs manquantes", JOptionPane.WARNING_MESSAGE);

            // on passe à la station suivante
            choixStation.setSelectedIndex((choixStation.getSelectedIndex() + 1) % choixStation.getItemCount());

            // on veut pas boucler à l'infini si toutes les stations n'ont pas de valeurs
            if (timesUsed < choixStation.getItemCount())
                majMesures(timesUsed + 1);
            else {
                JOptionPane.showMessageDialog(this, "<html>Toutes les stations sont défectueuses.<br> Arrêt de la mise à jour automatique</html>", "Totalité des stations défectueuses", JOptionPane.ERROR_MESSAGE);
                realtimeCheckBox.setSelected(false);
            }

            return;
        }

        TableModel tableModel = new DefaultTableModel(new String[] { "Type", "Valeur", "Unité" }, values.size());

        Object previousChoiceXAxis = xAxisChoice.getSelectedItem();
        Object previousChoiceYAxis = yAxisChoice.getSelectedItem();
        xAxisChoice.removeAllItems();
        xAxisChoice.addItem("Temps");
        yAxisChoice.removeAllItems();

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

        if (previousChoiceXAxis == null)
            yAxisChoice.setSelectedIndex(0);
        else
            yAxisChoice.setSelectedItem(previousChoiceYAxis);

        if (yAxisChoice.getSelectedIndex() == 0 && xAxisChoice.getSelectedIndex() == 0)
            yAxisChoice.setSelectedIndex(yAxisChoice.getItemCount() > 1 ? 1 : 0);

        tableValeursStation.setModel(tableModel);

        // On essaye de redimensionner la SplitPane
        int location = splitPane.getDividerLocation();
        if (location < tableValeursStation.getWidth() || location < choixStation.getWidth())
            splitPane.setDividerLocation(-1);
    }

    public void buildChart(boolean resetChart) {

        if (resetChart)
            chart = null;

        if (chart == null){
            updateChart();
            if(chart == null){
                chart = QuickChart.getChart("Graphique", "x-axe", "y-axe", "serie", new double[] {0}, new double[] {0});
            }
        }


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
        DataSet dataset;

        if (selectedXType.equalsIgnoreCase("undefined") || selectedYType.equalsIgnoreCase("undefined"))
            return;

        try {
            dataset = DataSet.buildDataSet(bd, station, selectedXType, selectedYType);
            chart = dataset.makeChart();

        } catch (Exception e) {
            System.err.println("Impossible de générer un DataSet correct. Le graph n'a pas pu être construit");
            e.printStackTrace();
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your
     * code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {

        mainPanel = new JPanel();
        mainPanel.setLayout(new GridLayoutManager(2, 1, new Insets(10, 5, 20, 5), -1, -1));
        mainPanel.setMinimumSize(new Dimension(600, 400));
        mainPanel.setPreferredSize(new Dimension(600, 400));
        mainPanel.setRequestFocusEnabled(true);
        mainPanel.setVerifyInputWhenFocusTarget(false);
        final JLabel label1 = new JLabel();
        Font label1Font = this.$$$getFont$$$("Consolas", Font.BOLD, 18, label1.getFont());
        if (label1Font != null) label1.setFont(label1Font);
        label1.setHorizontalAlignment(0);
        label1.setHorizontalTextPosition(0);
        label1.setText("Monitoring Stations");
        mainPanel.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        splitPane = new JSplitPane();
        mainPanel.add(splitPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(200, 200), null, 0, false));
        rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        splitPane.setRightComponent(rightPanel);
        final Spacer spacer1 = new Spacer();
        rightPanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        rightPanel.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        panelGraph = new JPanel();
        panelGraph.setLayout(new BorderLayout(0, 0));
        panelGraph.setForeground(new Color(-4521980));
        rightPanel.add(panelGraph, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayoutManager(6, 1, new Insets(5, 5, 20, 5), -1, -1));
        splitPane.setLeftComponent(leftPanel);
        realtimeAndRefreshPanel = new JPanel();
        realtimeAndRefreshPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        leftPanel.add(realtimeAndRefreshPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        realtimeCheckBox = new JCheckBox();
        realtimeCheckBox.setText("realtime");
        realtimeAndRefreshPanel.add(realtimeCheckBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        refreshButton = new JButton();
        refreshButton.setText("refresh");
        realtimeAndRefreshPanel.add(refreshButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        stationDataPanel = new JPanel();
        stationDataPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        leftPanel.add(stationDataPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        stationDataPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Station");
        panel1.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        choixStation = new JComboBox();
        panel1.add(choixStation, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        stationDataPanel.add(scrollPane1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tableValeursStation = new JTable();
        tableValeursStation.setAutoResizeMode(4);
        tableValeursStation.setPreferredScrollableViewportSize(new Dimension(225, 300));
        scrollPane1.setViewportView(tableValeursStation);
        graphSettingsPanel = new JPanel();
        graphSettingsPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        leftPanel.add(graphSettingsPanel, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        xAxisPanel = new JPanel();
        xAxisPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        graphSettingsPanel.add(xAxisPanel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("x-axis");
        xAxisPanel.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        xAxisChoice = new JComboBox();
        xAxisChoice.setEnabled(true);
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("undefined");
        xAxisChoice.setModel(defaultComboBoxModel1);
        xAxisPanel.add(xAxisChoice, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yAxisPanel = new JPanel();
        yAxisPanel.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        graphSettingsPanel.add(yAxisPanel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("y-axis");
        yAxisPanel.add(label4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        yAxisChoice = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel2 = new DefaultComboBoxModel();
        defaultComboBoxModel2.addElement("undefined");
        yAxisChoice.setModel(defaultComboBoxModel2);
        yAxisPanel.add(yAxisChoice, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator1 = new JSeparator();
        leftPanel.add(separator1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JSeparator separator2 = new JSeparator();
        leftPanel.add(separator2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {

        if (currentFont == null) return null;
        String resultName;
        if (fontName == null) {resultName = currentFont.getName();} else {
            Font testFont = new Font(fontName, Font.PLAIN, 10);
            if (testFont.canDisplay('a') && testFont.canDisplay('1')) {resultName = fontName;} else {
                resultName = currentFont.getName();
            }
        }
        return new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() { return mainPanel; }
}