import org.h2.message.DbException;
import org.h2.tools.Server;

import java.sql.*;
import java.util.*;

public class LectureBase {

    private static final boolean useH2 = false;
    private final String serveurBD = "localhost";
    private final String portBD = "3306";
    private final String nomBD = "P2I2P4";
    private final String loginBD = "root";
    private final String motdepasseBD = "root";
    private Connection connection = null;
    private PreparedStatement selectLastMesureStatement = null,
            selectCapteursDuneStationStatement = null,
            selectAllStationsStatement = null,
            selectStationFromNomStatement = null,
            selectGrandeursStationStatement = null,
            selectAllValuesFromStationOfType = null,
            selectUnitsAndThresholdsFromDataType = null,
            selectAllStationLocations = null;

    public void connexionBD() throws Exception {

        try {
            //Enregistrement de la classe du driver par le driverManager
            //Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Driver trouvé...");
            //Création d'une connexion sur la base de donnée

            if (useH2) {
                Server h2server = null;
                try {
                    h2server = Server.createTcpServer("-tcpPort", "9123", "-tcpAllowOthers", "-tcpDaemon", "-baseDir", "./../db", "-ifNotExists");
                    h2server.start();
                } catch (DbException e) {
                    // Ignored, the server is already in place
                }
            }

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
            this.selectAllStationsStatement = this.connection.prepareStatement("SELECT nomStation FROM station");
            this.selectStationFromNomStatement = this.connection.prepareStatement(
                    "SELECT idStation FROM station " +
                            "WHERE station.nomStation = ?");
            this.selectGrandeursStationStatement = this.connection.prepareStatement(
                    "SELECT libelleType, symbol " +
                            "FROM typeCapteur, capteur, station " +
                            "WHERE typeCapteur.idTypeCapteur=capteur.idTypeCapteur " +
                            "AND capteur.idStation=station.idStation " +
                            "AND station.idStation=? " +
                            "ORDER BY capteur.idTypeCapteur");

            // FIXME trouver un truc plus propre que cette horreur de 2000 caractères
            this.selectLastMesureStatement = this.connection.prepareStatement(
                    "SELECT dateMesure, valeur " +
                            "FROM mesure, capteur " +
                            "WHERE dateMesure = (" +
                            "SELECT MAX(dateMesure) " +
                            "FROM mesure, capteur " +
                            "WHERE capteur.idCapteur = mesure.idCapteur " +
                            "AND capteur.idStation = ? AND capteur.idTypeCapteur = ?)    " +
                            "AND capteur.idCapteur = mesure.idCapteur " +
                            "AND capteur.idStation = ? " +
                            "AND capteur.idTypeCapteur = ? " +
                            "ORDER BY capteur.idTypeCapteur;");

            this.selectCapteursDuneStationStatement = this.connection.prepareStatement(
                    "SELECT idCapteur,idTypeCapteur " +
                            "FROM capteur WHERE idStation = ? " +
                            "ORDER BY idTypeCapteur;");
            this.selectAllValuesFromStationOfType = this.connection.prepareStatement(
                    "SELECT dateMesure, valeur " +
                            "FROM mesure, station, capteur, typeCapteur " +
                            "WHERE station.nomStation = ? AND typeCapteur.libelleType = ? " +
                            "AND capteur.idTypeCapteur = typeCapteur.idTypeCapteur " +
                            "AND station.idStation = capteur.idStation " +
                            "AND mesure.idCapteur = capteur.idCapteur " +
                            "ORDER BY dateMesure;");

            this.selectUnitsAndThresholdsFromDataType = this.connection.prepareStatement("" +
                                                                                         "SELECT symbol, seuilAlerteBas, seuilAlerteHaut " +
                                                                                         "FROM typeCapteur WHERE libelleType = ?");

            this.selectAllStationLocations = this.connection.prepareStatement("SELECT nomStation, latitude, longitude, libelle\n" +
                                                                              "FROM station, localisation, installation\n" +
                                                                              "WHERE station.idStation = installation.idStation\n" +
                                                                              "  AND installation.idLocalisation = localisation.idLocalisation\n" +
                                                                              "  AND installation.dateDebut < now() AND now() < installation.dateFin");

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

    public LinkedHashMap<Long, Double> getValuesFromStationOfType(String nomStation, String type) {

        LinkedHashMap<Long, Double> values = new LinkedHashMap<>();
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

        return new double[][] { xIsTimeAxis ? timeValues : dataValues, xIsTimeAxis ? dataValues : timeValues };
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
            iterator.next();
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
            this.selectUnitsAndThresholdsFromDataType.setString(1, dataTypeName);
            ResultSet set = this.selectUnitsAndThresholdsFromDataType.executeQuery();
            set.next();

            Object[] result = new Object[] { set.getString("symbol"), null, null };

            int lowThresh = set.getInt("seuilAlerteBas");
            if (!set.wasNull())
                result[1] = lowThresh;

            int highThresh = set.getInt("seuilAlerteHaut");
            if (!set.wasNull())
                result[2] = highThresh;

            return result;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return null;
    }

    public HashMap<String, Location> getAllStationLocations() {

        HashMap<String, Location> stationLocationMap = new HashMap<>();

        try {

            ResultSet result = this.selectAllStationLocations.executeQuery();
            while(result.next())
                stationLocationMap.put(result.getString("nomStation"),
                                       new Location(result.getDouble("latitude"),
                                                    result.getDouble("longitude"),
                                                    result.getString("libelle"))
                                      );

        } catch (SQLException ex) {

            ex.printStackTrace();
        }

        return stationLocationMap;
    }

    public String getDatabaseUrl() {

        if (useH2)
            return "jdbc:h2:tcp://localhost:9123/../db/" + nomBD;
        else
            return "jdbc:mysql://" + serveurBD + ":" + portBD + "/" + nomBD + "?zeroDateTimeBehavior=convertToNull&serverTimezone=Europe/Paris";
    }
}