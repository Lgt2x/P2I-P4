package fr.insalyon.p2i2_222b.sql;

import fr.insalyon.p2i2_222b.Main;
import org.h2.tools.RunScript;

import java.io.Closeable;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.Instant;

/**
 * Gère les interactions avec la BDD.
 * <p>
 * TODO il serait judicieux d'utiliser un connection pool
 * au lieu d'ouvrir une connection pour chaque requête.
 */
public class DBManager implements Closeable {

    private static final String serveurBD = "localhost";
    private static final String portBD = "3306";
    private static final String nomBD = "P2I2P4";
    private static final String loginBD = "root";
    private static final String motdepasseBD = "root";
    private String connectionString;
    private Connection conn;
    private PreparedStatement insertMeasureStmt;

    public DBManager(String connectionString) throws SQLException {
        this(DriverManager.getConnection(getDatabaseUrl(connectionString), loginBD, motdepasseBD));
    }

    public DBManager(Connection conn) {
        this.conn = conn;
        if (!isDBSetup()) {
            Main.console.log("Mise en place de la BDD");
            setupDB("/sql/creationTables.sql", "/sql/insertionData.sql");
        }
        try {
            this.insertMeasureStmt = conn.prepareStatement("insert into P2I2P4.mesure (idCapteur, valeur, dateMesure) values ( ?, ?, ? )");
        } catch (SQLException e) {
            Main.console.err(e);
            System.exit(-2);
        }
    }

    public static String getDatabaseUrl(String base) {
        if (base.equals("mysql"))
            return "jdbc:mysql://" + serveurBD + ":" + portBD + "/" + nomBD + "?zeroDateTimeBehavior=convertToNull&serverTimezone=Europe/Paris";
        else
            return base;
    }

    /**
     * Crée les tables et ajoute les données de base
     *
     * @param setupScript  le chemin vers le script d'installation
     * @param insertScript le chemin vers le script des données
     */
    public void setupDB(String setupScript, String insertScript) {
        try {
            RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream(setupScript)));
            RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream(insertScript)));
        } catch (SQLException e) {
            Main.console.err(e);
        }
    }

    public boolean isDBSetup() {
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'P2I2P4' AND TABLE_NAME = 'MESURE'");
            rs.next();
            return rs.getInt(1) == 1;
        } catch (SQLException e) {
            Main.console.err(e);
            return false;
        }
    }

    public void saveMeasure(int idCapteur, double value) {
        try {
            insertMeasureStmt.setInt(1, idCapteur);
            insertMeasureStmt.setDouble(2, value);
            insertMeasureStmt.setTimestamp(3, Timestamp.from(Instant.now()));
            insertMeasureStmt.executeUpdate();
            ResultSet rs = insertMeasureStmt.getResultSet();
        } catch (SQLException e) {
            Main.console.err(e);
        }
    }

    @Override
    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
