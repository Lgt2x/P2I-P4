import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class LectureBase {

    private static final boolean useH2 = false;
    private final String serveurBD = "localhost";
    private final String portBD = "3306";
    private final String nomBD = "P2I2P4";
    private final String loginBD = "root";
    private final String motdepasseBD = "root";
    private Connection connection = null;
    private PreparedStatement SELECTLastMesureStatement = null,
            SELECTCapteursDuneStationStatement = null,
            SELECTAllStationsStatement = null,
            SELECTStationFromNomStatement = null,
            SELECTgrandeursStationStatement = null,
            SELECTAllValuesFromStationOfType = null,
            SELECTUnitsAndThresholdsFromDataType = null;

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

            if (useH2)
                connection.createStatement().execute("use p2i2p4;");

            makeStatements();

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode connexionBD()");
        }
    }

    public void makeStatements() {

        try {
            this.SELECTAllStationsStatement = this.connection.prepareStatement("SELECT nomStation FROM station");
            this.SELECTStationFromNomStatement = this.connection.prepareStatement(
                    "SELECT idStation FROM station " +
                            "WHERE station.nomStation = ?");
            this.SELECTgrandeursStationStatement = this.connection.prepareStatement(
                    "SELECT libelleType, symbol " +
                            "FROM typeCapteur, capteur, station " +
                            "WHERE typeCapteur.idTypeCapteur=capteur.idTypeCapteur " +
                            "AND capteur.idStation=station.idStation " +
                            "AND station.idStation=? " +
                            "ORDER BY capteur.idTypeCapteur");

            // FIXME trouver un truc plus propre que cette horreur de 2000 caractères
            this.SELECTLastMesureStatement = this.connection.prepareStatement(
                    "SELECT dateMesure, valeur " +
                            "FROM mesure, capteur " +
                            "WHERE dateMesure = (" +
                                "SELECT MAX(dateMesure) " +
                                "FROM mesure, capteur " +
                                "WHERE capteur.idCapteur = mesure.idCapteur " +
                                "AND capteur.idStation = ? AND capteur.idTypeCapteur = ?) " +
                            "AND capteur.idCapteur = mesure.idCapteur " +
                            "AND capteur.idStation = ? " +
                            "AND capteur.idTypeCapteur = ? " +
                            "ORDER BY capteur.idTypeCapteur;");

            this.SELECTCapteursDuneStationStatement = this.connection.prepareStatement(
                    "SELECT idCapteur,idTypeCapteur " +
                            "FROM capteur WHERE idStation = ? " +
                            "ORDER BY idTypeCapteur;");
            this.SELECTAllValuesFromStationOfType = this.connection.prepareStatement(
                    "SELECT dateMesure, valeur " +
                            "FROM mesure, station, capteur, typeCapteur " +
                            "WHERE station.nomStation = ? AND typeCapteur.libelleType = ? " +
                            "AND capteur.idTypeCapteur = typeCapteur.idTypeCapteur " +
                            "AND station.idStation = capteur.idStation " +
                            "AND mesure.idCapteur = capteur.idCapteur " +
                            "ORDER BY dateMesure;");

            this.SELECTUnitsAndThresholdsFromDataType = this.connection.prepareStatement("" +
                    "SELECT symbol, seuilAlerteBas, seuilAlerteHaut " +
                    "FROM typeCapteur WHERE libelleType = ?");

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public int getIdStation(String nomStation) throws Exception {

        try {

            this.SELECTStationFromNomStatement.setString(1, nomStation);
            ResultSet stations = this.SELECTStationFromNomStatement.executeQuery();
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
            ResultSet rs = SELECTAllStationsStatement.executeQuery();
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
            this.SELECTgrandeursStationStatement.setInt(1, getIdStation(nomStation));
            ResultSet rs = SELECTgrandeursStationStatement.executeQuery();
            ArrayList<String> grandeurs = new ArrayList();

            while (rs.next())
                grandeurs.add(rs.getString("libelleType") + "|" + rs.getString("symbol"));

            return grandeurs;

        } catch (Exception exception) {
            exception.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des grandeurs de la station");
        }
    }

    public LinkedHashMap<Long, Double> getValuesFromStationOfType(String nomStation, String type) {

        LinkedHashMap<Long, Double> values = new LinkedHashMap<>();
        try {
            this.SELECTAllValuesFromStationOfType.setString(1, nomStation);
            this.SELECTAllValuesFromStationOfType.setString(2, type);

            ResultSet results = this.SELECTAllValuesFromStationOfType.executeQuery();

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

            this.SELECTCapteursDuneStationStatement.setInt(1, idStation);
            ResultSet capteurs = this.SELECTCapteursDuneStationStatement.executeQuery();

            ArrayList<Double> values = new ArrayList<>();

            while (capteurs.next()) {

                SELECTLastMesureStatement.setInt(1, idStation);
                SELECTLastMesureStatement.setInt(3, idStation);
                SELECTLastMesureStatement.setInt(2, capteurs.getInt("idTypeCapteur"));
                SELECTLastMesureStatement.setInt(4, capteurs.getInt("idTypeCapteur"));
                ResultSet rs = SELECTLastMesureStatement.executeQuery();

                while (rs.next())
                    values.add(rs.getDouble("valeur"));
            }

            return values;
        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des valeurs actuelles");
        }
    }

    public double[][] getTimestampedDataset(String stationName, String SELECTedTypeX, String SELECTedTypeY, boolean xIsTimeAxis) {

        String SELECTedType = xIsTimeAxis ? SELECTedTypeY : SELECTedTypeX;

        Map<Long, Double> valuesMap;
        try {
            valuesMap = getValuesFromStationOfType(stationName, SELECTedType);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        double[] timeValues, dataValues;

        if (valuesMap.isEmpty())
            return null;

        timeValues = valuesMap.keySet().stream().mapToDouble(Double::valueOf).toArray();
        dataValues = valuesMap.values().stream().mapToDouble(Double::valueOf).toArray();

        return new double[][]{xIsTimeAxis ? timeValues : dataValues, xIsTimeAxis ? dataValues : timeValues};
    }

    public double[][] getBiQuantityDataset(String stationName, String SELECTedTypeX, String SELECTedTypeY) {

        LinkedHashMap<Long, Double> valuesMapX, valuesMapY;
        try {
            valuesMapX = getValuesFromStationOfType(stationName, SELECTedTypeX);
            valuesMapY = getValuesFromStationOfType(stationName, SELECTedTypeY);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        if (valuesMapX.isEmpty() || valuesMapY.isEmpty() || valuesMapX.size() != valuesMapY.size()) {

            // FIXME when dataset is done, re-query data with LIMIT = min(valuesMapX.size(), valuesMaxY.size())
            System.out.println("maps sizes different, removing some values");

            LinkedHashMap<Long, Double> tooMuchInfoMap = valuesMapX.size() > valuesMapY.size() ? valuesMapX : valuesMapY;
            LinkedHashMap<Long, Double> otherMap = tooMuchInfoMap == valuesMapX ? valuesMapY : valuesMapX;

            Iterator<Map.Entry<Long, Double>> iterator = tooMuchInfoMap.entrySet().iterator();
            while (tooMuchInfoMap.size() > otherMap.size()) {
                iterator.remove();
                iterator.next();
            }
        }

        double[][] result = new double[2][];
        result[0] = valuesMapX.values().stream().mapToDouble(Double::valueOf).toArray();
        result[1] = valuesMapY.values().stream().mapToDouble(Double::valueOf).toArray();

        return result;
    }

    public Object[] getDataTypeInfo(String dataTypeName) {

        try {
            this.SELECTUnitsAndThresholdsFromDataType.setString(1, dataTypeName);
            ResultSet set = this.SELECTUnitsAndThresholdsFromDataType.executeQuery();
            set.next();
            return new Object[]{set.getString("symbol"), set.getInt("seuilAlerteBas"), set.getInt("seuilAlerteHaut")};

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public String getDatabaseUrl() {

        if (useH2)
            return "jdbc:h2:tcp://localhost:9123/../db/" + nomBD;
        else
            return "jdbc:mysql://" + serveurBD + ":" + portBD + "/" + nomBD + "?zeroDateTimeBehavior=convertToNull&serverTimezone=Europe/Paris";
    }
}