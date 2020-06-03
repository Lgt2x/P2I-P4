import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LectureBase {

    private final String serveurBD = "localhost";
    private final String portBD = "3306";
    private final String nomBD = "p2i2p4";
    private final String loginBD = "root";
    private final String motdepasseBD = "root";
    private Connection connection = null;
    private PreparedStatement   selectLastMesureStatement = null,
                                selectCapteursDuneStationStatement = null,
                                selectAllStationsStatement = null,
                                selectStationFromNomStatement = null,
                                selectGrandeursStationStatement = null,
                                selectAllValuesFromStationOfType = null;

    public void connexionBD() throws Exception {

        try {
            //Enregistrement de la classe du driver par le driverManager
            //Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Driver trouvé...");
            //Création d'une connexion sur la base de donnée

            String urlJDBC = getDatabaseUrl();

            System.out.println("Connexion à " + urlJDBC);
            this.connection = DriverManager.getConnection(urlJDBC, this.loginBD, this.motdepasseBD);

            System.out.println("Connexion établie...");

            makeStatements();

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode connexionBD()");
        }
    }

    public void makeStatements() {

        try {
            this.selectAllStationsStatement = this.connection.prepareStatement("SELECT nomStation FROM station");
            this.selectStationFromNomStatement = this.connection.prepareStatement("SELECT idStation FROM station WHERE station.nomStation = ?");
            this.selectGrandeursStationStatement = this.connection.prepareStatement("SELECT libelleType, symbol FROM typecapteur, capteur, station WHERE typecapteur.idTypeCapteur=capteur.idTypeCapteur AND capteur.idStation=station.idStation AND station.idStation=? order by capteur.idTypeCapteur");

            // FIXME trouver un truc plus propre que cette horreur de 2000 caractères
            this.selectLastMesureStatement = this.connection.prepareStatement("SELECT dateMesure, valeur FROM mesure, capteur WHERE dateMesure = (select MAX(dateMesure) from mesure, capteur where capteur.idCapteur = mesure.idCapteur and capteur.idStation = ? and capteur.idTypeCapteur = ?) and capteur.idCapteur = mesure.idCapteur and capteur.idStation = ? and capteur.idTypeCapteur = ? order by capteur.idTypeCapteur;");

            this.selectCapteursDuneStationStatement = this.connection.prepareStatement("select idCapteur,idTypeCapteur from capteur where idStation = ? order by idTypeCapteur;");
            this.selectAllValuesFromStationOfType = this.connection.prepareStatement("select dateMesure, valeur from mesure, station, capteur, typecapteur where station.nomStation = ? and typecapteur.libelleType = ? and capteur.idTypeCapteur = typecapteur.idTypeCapteur and station.idStation = capteur.idStation and mesure.idCapteur = capteur.idCapteur order by dateMesure;");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int getIdStation(String nomStation) throws Exception {

        try {

            this.selectStationFromNomStatement.setString(1, nomStation);
            ResultSet stations = this.selectStationFromNomStatement.executeQuery();
            stations.next();

            return stations.getInt("idStation");

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération de la station \"" + nomStation + "\"");
        }
    }

    public ArrayList<String> nomsStations() throws Exception {

        try {
            // À compléter
            ResultSet rs = selectAllStationsStatement.executeQuery();
            ArrayList<String> noms = new ArrayList();
            while (rs.next()) {
                noms.add(rs.getString("nomStation"));
            }

            return noms;

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des stations");
        }
    }

    public ArrayList<String> grandeurStations(String nomStation) throws Exception {
        try {
            this.selectGrandeursStationStatement.setInt(1, getIdStation(nomStation));
            ResultSet rs = selectGrandeursStationStatement.executeQuery();
            ArrayList<String> grandeurs = new ArrayList();

            while (rs.next())
                grandeurs.add(rs.getString("libelleType") + "|" + rs.getString("symbol"));

            return grandeurs;

        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des grandeurs de la station");
        }
    }

    public Map<Long, Double> getValuesFromStationOfType(String nomStation, String type) {

        Map<Long, Double> values = new HashMap<>();
        try {
            this.selectAllValuesFromStationOfType.setString(1, nomStation);
            this.selectAllValuesFromStationOfType.setString(2, type);

            ResultSet results = this.selectAllValuesFromStationOfType.executeQuery();

            while (results.next())
                values.put(results.getTimestamp("dateMesure").getTime(), results.getDouble("valeur"));

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return values;
    }

    public ArrayList<Double> derniereMesure(String nomStation) throws Exception {

        return derniereMesure(getIdStation(nomStation));
    }
    public ArrayList<Double> derniereMesure(int idStation) throws Exception {

        try {

            this.selectCapteursDuneStationStatement.setInt(1, idStation);
            ResultSet capteurs = this.selectCapteursDuneStationStatement.executeQuery();

            ArrayList<Double> values = new ArrayList<>();

            while (capteurs.next()) {

                selectLastMesureStatement.setInt(1, idStation);
                selectLastMesureStatement.setInt(3, idStation);
                selectLastMesureStatement.setInt(2, capteurs.getInt("idTypeCapteur"));
                selectLastMesureStatement.setInt(4, capteurs.getInt("idTypeCapteur"));
                ResultSet rs = selectLastMesureStatement.executeQuery();

                while (rs.next())
                    values.add(rs.getDouble("valeur"));
            }

            return values;
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des valeurs actuelles");
        }
    }

    public double[][] getTimestampedDataset(String stationName, String selectedTypeX, String selectedTypeY, boolean xIsTimeAxis) {

        String selectedType = xIsTimeAxis ? selectedTypeY : selectedTypeX;

        Map<Long, Double> valuesMap;
        try {
            valuesMap = getValuesFromStationOfType(stationName, selectedType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        double[] timeValues, dataValues;

        if (valuesMap.isEmpty())
            return null;

        timeValues = valuesMap.keySet().stream().mapToDouble(Double::valueOf).toArray();
        dataValues = valuesMap.values().stream().mapToDouble(Double::valueOf).toArray();

        return new double[][] { xIsTimeAxis ? timeValues : dataValues, xIsTimeAxis ? dataValues : timeValues };
    }

    public double[][] getBiQuantityDataset(String stationName, String selectedTypeX, String selectedTypeY) {

        Map<Long, Double> valuesMapX, valuesMapY;
        try {
            valuesMapX = getValuesFromStationOfType(stationName, selectedTypeX);
            valuesMapY = getValuesFromStationOfType(stationName, selectedTypeY);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (valuesMapX.isEmpty() || valuesMapY.isEmpty() || valuesMapX.size() != valuesMapY.size())
            return null;

        double[][] result = new double[2][];
        result[0] = valuesMapX.values().stream().mapToDouble(Double::valueOf).toArray();
        result[1] = valuesMapY.values().stream().mapToDouble(Double::valueOf).toArray();

        return result;
    }

    public String getDatabaseUrl() {

        return "jdbc:mysql://" + this.serveurBD + ":" + this.portBD + "/" + this.nomBD + "?zeroDateTimeBehavior=convertToNull&serverTimezone=Europe/Paris";
    }
}