package datubasea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBGestorea {

    // Konektatzeko datuak
    private static final String DB = "jdbc:mysql://10.23.26.119:3306/alaiktomugi?useSSL=false&serverTimezone=UTC";
    private static final String ERABILTZAILEAK = "arkaitz";
    private static final String PASAHITZA = "pvlbtnse";

    private static Connection connection = null;

    // Konektibitatea lortzeko metodoa
    public static Connection getConexion() {
        if (connection == null) {
            try {
                // Konektibitatea sortu ez badago
                connection = DriverManager.getConnection(DB, ERABILTZAILEAK, PASAHITZA);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }	
        return connection;
    }

    // Konektibitatea itxi
    public static void itxiSesioa() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
