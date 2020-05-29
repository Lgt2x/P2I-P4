import java.sql.*;
import java.util.ArrayList;

public class LectureBase {

    private final String serveurBD = "localhost";
    private final String portBD = "3306";
    private final String nomBD = "p2i2p4";
    private final String loginBD = "root";
    private final String motdepasseBD = "root";
    private Connection connection = null;
    private PreparedStatement selectLastMesureStatement = null, selectCapteursDuneStationStatement = null, selectAllStationsStatement = null, selectStationFromNomStatement = null;

    public void connexionBD() throws Exception {

        try {
            //Enregistrement de la classe du driver par le driverManager
            //Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Driver trouvé...");
            //Création d'une connexion sur la base de donnée
            String urlJDBC = "jdbc:mysql://" + this.serveurBD + ":" + this.portBD + "/" + this.nomBD;
            urlJDBC += "?zeroDateTimeBehavior=convertToNull&serverTimezone=Europe/Paris";

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

            // FIXME trouver un truc plus propre que cette horreur de 2000 caractères
            this.selectLastMesureStatement = this.connection.prepareStatement("SELECT dateMesure, valeur FROM mesure, capteur WHERE dateMesure = (select MAX(dateMesure) from mesure, capteur where capteur.idCapteur = mesure.idCapteur and capteur.idStation = ? and capteur.idTypeCapteur = ?) and capteur.idCapteur = mesure.idCapteur and capteur.idStation = ? and capteur.idTypeCapteur = ?;");
            this.selectCapteursDuneStationStatement = this.connection.prepareStatement("select idCapteur,idTypeCapteur from capteur where idStation = ?");

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

    public ArrayList nomsStations() throws Exception {

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

    public Double[] derniereMesure(String nomStation) throws Exception {

        return derniereMesure(getIdStation(nomStation));
    }
    public Double[] derniereMesure(int idStation) throws Exception {

        try {
            Double[] valeurs = new Double[9];

            this.selectCapteursDuneStationStatement.setInt(1, idStation);
            ResultSet capteurs = this.selectCapteursDuneStationStatement.executeQuery();
            while (capteurs.next()) {

                int row = capteurs.getRow();
                selectLastMesureStatement.setInt(1, idStation);
                selectLastMesureStatement.setInt(3, idStation);
                selectLastMesureStatement.setInt(2, capteurs.getInt("idTypeCapteur"));
                selectLastMesureStatement.setInt(4, capteurs.getInt("idTypeCapteur"));
                ResultSet rs = selectLastMesureStatement.executeQuery();

                while (rs.next()) {
                    valeurs[row-1] = rs.getDouble("valeur");
                }
            }

            return valeurs;

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des valeurs actuelles");
        }
    }
}