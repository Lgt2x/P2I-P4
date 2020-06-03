import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class MonitoringFrame extends JFrame {

    private JPanel mainPanel;
    private JPanel realtimeAndRefreshPanel;
    private JCheckBox realtimeCheckBox;
    private JButton refreshButton;
    private JComboBox choixStation;
    private JTable tableValeursStation;
    private JPanel panelGraph;
    private JSplitPane splitPane;
    private JPanel rightPanel;
    private JPanel leftPanel;
    private JPanel stationDataPanel;
    private JPanel graphSettingsPanel;
    private JComboBox xAxisChoice;
    private JComboBox yAxisChoice;
    private JPanel xAxisPanel;
    private JPanel yAxisPanel;
    private LectureBase bd;
    private Timer refreshTimer = new Timer(500, null);

    public MonitoringFrame() throws Exception {

        super("Monitoring de stations");

        setResizable(false);
        setSize(800, 500);
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

        ActionListener dataUpdate = e -> {
            try {
                majMesures();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        };

        choixStation.addActionListener(dataUpdate);
        refreshButton.addActionListener(dataUpdate);
        refreshTimer.addActionListener(dataUpdate);

        realtimeCheckBox.addActionListener(e -> {

            refreshButton.setVisible(!realtimeCheckBox.isSelected());

            if (realtimeCheckBox.isSelected())
                refreshTimer.start();
            else refreshTimer.stop();
        });

        recupNomsStations();
        majMesures();
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

            throw new Exception("Erreur dans les requêtes, nombre de valeurs et nombre de grandeurs différents");
        }

        TableModel tableModel = new DefaultTableModel(values.size(), 3);
        for (int i = 0; i < values.size(); ++i) {

            // "temperature|°C" => ["temperature", "°C"]
            String[] strSplit = grandeurs.get(i).split("\\|");
            tableModel.setValueAt(strSplit[0], i, 0);
            tableModel.setValueAt(values.get(i), i, 1);
            tableModel.setValueAt(strSplit[1], i, 2);
        }

        tableValeursStation.setModel(tableModel);

        // On essaye de redimensionner la SplitPane
        int location = splitPane.getDividerLocation();
        if (location < tableValeursStation.getWidth() || location < choixStation.getWidth())
            splitPane.setDividerLocation(-1);
    }
}