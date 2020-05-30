package fr.insalyon.p2i2_222b.sql;

import fr.insalyon.p2i2_222b.Main;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;

/**
 * Gère les interactions avec la BDD.
 * <p>
 * TODO il serait judicieux d'utiliser un connection pool
 * au lieu d'ouvrir une connection pour chaque requête.
 */
public class DBManager {

    private String connectionString;

    public DBManager(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setupDB(String setupScript, String insertScript) {
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream(setupScript)));
            RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream(insertScript)));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isDBSetup() {
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            try (Statement st = conn.createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*)\n" +
                                                       "   FROM INFORMATION_SCHEMA.TABLES\n" +
                                                       "   WHERE TABLE_SCHEMA = 'P2I2P4'\n" +
                                                       "   AND TABLE_NAME = 'mesure'");
                rs.next();
                int value = rs.getInt(1);
                Main.console.log("" + value);
                return value == 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
