import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FenetreVisualisation extends JFrame {

    private JComboBox listeStations;
    private JComboBox selectStation;
    private JComboBox selectGrandeur;
    private JLabel affTemp;
    private JLabel affHum;
    private JLabel affParticules;
    private JLabel affCO2;
    private JLabel affO2;
    private JLabel affNO2;
    private JLabel affBruit;
    private JLabel affLum;
    private JLabel affPression;
    private LectureBase bd;


    public FenetreVisualisation() throws Exception {

        super("Visualisation des données");
        this.setSize(1500, 700);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel affInfoDirect = new JPanel();
        affInfoDirect.setBounds(0, 0, 1500, 700);
        affInfoDirect.setLayout(null);

        Font police = new Font("Arial", Font.BOLD, 25);

        listeStations = new JComboBox();
        listeStations.setBounds(850, 15, 150, 50);
        listeStations.addActionListener(e -> {
            try {
                majMesures();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });


        JLabel titre = new JLabel("Données actuelles de la station");
        titre.setFont(police);
        titre.setBounds(475, 5, 1000, 70);

        JLabel titreGraph = new JLabel("Visualiser les enregistrements sur un graphique");
        titreGraph.setFont(police);
        titreGraph.setBounds(475,300,1000,70);

        Font policeValeurs = new Font("Arial", Font.PLAIN, 17);

        affTemp = new JLabel("Temperature : --.- °C");
        affTemp.setFont(policeValeurs);
        affTemp.setBounds(50, 100, 300, 50);

        affHum = new JLabel("Humidite : -- %");
        affHum.setFont(policeValeurs);
        affHum.setBounds(350, 100, 300, 50);

        affBruit = new JLabel("Bruit : -- dB");
        affBruit.setFont(policeValeurs);
        affBruit.setBounds(650, 100, 300, 50);

        affLum = new JLabel("Lumière : --- lux");
        affLum.setFont(policeValeurs);
        affLum.setBounds(950, 100, 300, 50);

        affPression = new JLabel("Pression : ---- hPa");
        affPression.setFont(policeValeurs);
        affPression.setBounds(50, 150, 300, 50);

        affParticules = new JLabel("PM10 : -- ppm");
        affParticules.setFont(policeValeurs);
        affParticules.setBounds(350, 150, 300, 50);

        affCO2 = new JLabel("C02 : -- ppm");
        affCO2.setFont(policeValeurs);
        affCO2.setBounds(650, 150, 300, 50);

        affNO2 = new JLabel("Oxyde d'Azote : -- ppm");
        affNO2.setFont(policeValeurs);
        affNO2.setBounds(950, 150, 300, 50);

        affO2 = new JLabel("Dioxygène : -- %");
        affO2.setFont(policeValeurs);
        affO2.setBackground(Color.green);
        affO2.setBounds(1250,150,300,50);

        JButton refresh = new JButton("Rafraîchir");
        refresh.setBounds(1100,15,100,50);
        refresh.setBackground(Color.green);
        refresh.addActionListener(e -> {
            try {
                majMesures();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });

        JButton generer = new JButton ("Générer le graphique");
        generer.setBounds(650,600,200,50);
        generer.addActionListener(e ->{
            try{
                //à compléter
            }catch(Exception exception){
                exception.printStackTrace();
            }
        });

        selectGrandeur = new JComboBox();
        selectGrandeur.setBounds(800,400,150,50);

        selectStation = new JComboBox();
        selectStation.setBounds(550,400,150,50);
        selectStation.addActionListener(e -> {
            try{
                recupGrandeurStations();
            }catch(Exception exception){
                exception.printStackTrace();
            }
        });

        affInfoDirect.add(selectGrandeur);
        affInfoDirect.add(generer);
        affInfoDirect.add(selectStation);
        affInfoDirect.add(titre);
        affInfoDirect.add(refresh);
        affInfoDirect.add(affO2);
        affInfoDirect.add(affBruit);
        affInfoDirect.add(affHum);
        affInfoDirect.add(affLum);
        affInfoDirect.add(affNO2);
        affInfoDirect.add(affCO2);
        affInfoDirect.add(affParticules);
        affInfoDirect.add(affPression);
        affInfoDirect.add(affTemp);
        affInfoDirect.add(listeStations);
        affInfoDirect.add(titreGraph);

        this.add(affInfoDirect);
        this.setLayout(null);
        this.setVisible(true);

        bd = new LectureBase();
        bd.connexionBD();
        recupNomsStations();
        recupGrandeurStations();
        majMesures();

        /* ESSAI XCHART
        double[] xData = new double[] { 0.0, 1.0, 2.0 };
        double[] yData = new double[] { 2.0, 1.0, 0.0 };

        // Create Chart
        XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", xData, yData);

        // Show it
        new SwingWrapper(chart).displayChart();

         */
    }

    public void recupNomsStations() throws Exception {

        ArrayList<String> resultat = bd.nomsStations();

        for (int i = 0; i < resultat.size(); i++) {
            listeStations.addItem(resultat.get(i));
            selectStation.addItem(resultat.get(i));
        }

        listeStations.setSelectedIndex(0);
    }

    public void recupGrandeurStations() throws Exception{
        selectGrandeur.removeAllItems();
        ArrayList<String> rs = bd.grandeurStations(selectStation.getSelectedItem().toString());
        for (int j=0; j<rs.size(); j++){
            selectGrandeur.addItem(rs.get(j));
        }
    }

    public void majMesures() throws Exception {

        Double[] rs;
        rs = bd.derniereMesure(bd.getIdStation(listeStations.getSelectedItem().toString()));

        affNO2.setText("Oxyde d'Azote : " + rs[4] + " ppm");
        affCO2.setText("CO2 : " + rs[3] + " ppm");
        affParticules.setText("PM10 : " + rs[6] + " ppm");
        affPression.setText("Pression : " + rs[7] + " hPa");
        affLum.setText("Lumière : " + rs[2] + " lux");
        affBruit.setText("Bruit : " + rs[5] + " dB");
        affHum.setText("Humidite : " + rs[1] + " %");
        affTemp.setText("Temperature : " + rs[0] + " °C");
        affO2.setText("Dioxygène : "+rs[8]+" %");

        this.repaint();
    }
}
