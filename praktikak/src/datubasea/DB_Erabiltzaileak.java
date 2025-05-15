package datubasea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
public class DB_Erabiltzaileak {



    // Erabiltzailearen historialari buruzko kontsulta egitea
    public static void erabiltzailearenHistorialaIkusi(String erabiltzaileNan) {
        String kontsulta = "SELECT * FROM Bidai_historiala WHERE erabiltzaile_NAN = ?"; 
        
        try (PreparedStatement stmt = DBGestorea.getConexion().prepareStatement(kontsulta)) {
            // Parametroa jarri kontsultaren prestatuan
            stmt.setString(1, erabiltzaileNan);
            
            // Kontsulta exekutatu eta emaitzak lortu
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Emaitzak jaso eta inprimatu
                    int id = rs.getInt("Bidaia_id");
                    String gidari_nan = "Gidari_nan";
                    String erabiltzaile_nan = "erabiltzaile_nan";
                    Date date = new Date(2);	

                    System.out.println("ID: " + id );
                }
            }
        } catch (SQLException e) {
            System.err.println("Errore bat egon da kontsulta egitean: " + e.getMessage());
            e.printStackTrace();
        } 
    }

    public static void main(String[] args) {
        
        erabiltzailearenHistorialaIkusi("12345678A"); // Hemen sartu erabiltzailearen NAN-a
    }
}
