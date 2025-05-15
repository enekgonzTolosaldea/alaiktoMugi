package datubasea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBGestorea {

    // Konektatzeko datuak
    private static final String DB = "jdbc:mysql://10.23.25.23:3306/alaiktomugi?useSSL=false&serverTimezone=UTC";
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

    // Erabiltzailearen historialari buruzko kontsulta egitea
    public static void erabiltzailearenHistorialaIkusi(String erabiltzaileNan) {
        String kontsulta = "SELECT * FROM Bidai_historiala WHERE erabiltzaile_NAN = ?"; 

        try (PreparedStatement stmt = getConexion().prepareStatement(kontsulta)) {
            // Parametroa jarri kontsultaren prestatuan
            stmt.setString(1, erabiltzaileNan);

            // Kontsulta exekutatu eta emaitzak lortu
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Emaitzak jaso eta inprimatu
                    int id = rs.getInt("Bidaia_id");
   

                    System.out.println("ID: " + id );
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore bat egon da kontsulta egitean: " + e.getMessage());
            e.printStackTrace();
        } finally {
            itxiSesioa(); // Konektibitatea itxi
        }
    }

    public static void main(String[] args) {
        // Adibide bat: erabiltzailearen historialaren kontsulta egitea
        erabiltzailearenHistorialaIkusi("12345678A"); // Hemen sartu erabiltzailearen NAN-a
    }
}
