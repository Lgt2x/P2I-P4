import java.sql.*;
import java.util.ArrayList;

public class LectureBase {

    private final String serveurBD = "localhost";
    private final String portBD = "3306";
    private final String nomBD = "p2i2p4";
    private final String loginBD = "root";
    private final String motdepasseBD = "root";
    private Connection connection = null;
    private PreparedStatement selectMesuresStatement = null;

    public void connexionBD() throws Exception {
        try {
            //Enregistrement de la classe du driver par le driverManager
            //Class.forName("com.mysql.jdbc.Driver");
            //System.out.println("Driver trouvé...");
            //Création d'une connexion sur la base de donnée
            String urlJDBC = "jdbc:mysql://" + this.serveurBD + ":" + this.portBD + "/" + this.nomBD;
            urlJDBC += "?zeroDateTimeBehavior=CONVERT_TO_NULL&serverTimezone=Europe/Paris";

            System.out.println("Connexion à " + urlJDBC);
            this.connection = DriverManager.getConnection(urlJDBC, this.loginBD, this.motdepasseBD);

            System.out.println("Connexion établie...");

            // Requête de test pour lister les tables existantes dans les BDs MySQL
            PreparedStatement statement = this.connection.prepareStatement(
                    "SELECT table_schema, table_name"
                            + " FROM information_schema.tables"
                            + " WHERE table_schema NOT LIKE '%_schema' AND table_schema != 'mysql'"
                            + " ORDER BY table_schema, table_name");
            ResultSet result = statement.executeQuery();

            System.out.println("Liste des tables:");
            while (result.next()) {
                System.out.println("- " + result.getString("table_schema") + "." + result.getString("table_name"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la méthode connexionBD()");
        }

    }

    public ArrayList nomsStations() throws Exception {
        try {
            // À compléter
            this.selectMesuresStatement = this.connection.prepareStatement("SELECT nomStation FROM station");
            ResultSet rs= selectMesuresStatement.executeQuery();
            ArrayList<String> noms = new ArrayList();
            while (rs.next()){
                noms.add(rs.getString("nomStation"));
            }

            return noms;

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des stations");
        }
    }

    public Double[] derniereMesure() throws Exception {
        try {
            Double[] valeurs = new Double[9];
            this.selectMesuresStatement = this.connection.prepareStatement("SELECT MAX(dateMesure),valeur FROM mesure, capteur WHERE mesure.idCapteur=capteur.idCapteur AND capteur.idTypeCapteur=? GROUP BY idMesure;");
            for(int i=1; i<9; i++) {
                selectMesuresStatement.setInt(1,i);
                ResultSet rs = selectMesuresStatement.executeQuery();

                while (rs.next()) {
                    valeurs[i]=rs.getDouble("valeur");
                }

            }

            return valeurs;

        } catch (SQLException ex) {
            ex.printStackTrace(System.err);
            throw new Exception("Erreur dans la récupération des valeurs actuelles");
        }
    }

}
