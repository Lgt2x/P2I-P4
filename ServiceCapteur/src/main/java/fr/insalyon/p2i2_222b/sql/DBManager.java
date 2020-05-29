package fr.insalyon.p2i2_222b.sql;

import fr.insalyon.p2i2_222b.Main;
import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.*;

public class DBManager {

    Connection conn;
    private String connectionString;

    public DBManager(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setupDB(String setupScript) {
        try (Connection conn = DriverManager.getConnection(connectionString)) {
            RunScript.execute(conn, new InputStreamReader(getClass().getResourceAsStream(setupScript)));
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
