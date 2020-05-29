package fr.insalyon.p2i2_222b.sql;

import org.h2.tools.RunScript;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
}
