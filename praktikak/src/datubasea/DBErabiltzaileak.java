package datubasea;

import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;

public class DBErabiltzaileak {
    private static final SimpleDateFormat DATA_FORMATUA = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ORDU_FORMATUA = new SimpleDateFormat("HH:mm:ss");

    /**
     * Erabiltzaileen bidai historial guztia kargatzen du taula eredura (hilo seguro)
     * @param modelo Taularen eredua datuekin betetzeko
     */
    public static void erabiltzaileGuztienHistorialaIkusi(DefaultTableModel modelo) {
        new Thread(() -> {
            try {
                String kontsulta = "SELECT Bidaia_id, gidari_nan, erabiltzaile_nan, "
                                + "data, Ordua, Pertsona_kopurua, hasiera, helmuga "
                                + "FROM Bidai_historiala";

                try (Connection con = DBGestorea.getConexion();
                     PreparedStatement stmt = con.prepareStatement(kontsulta);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    // Datuak bildu
                    java.util.List<Object[]> datuak = new java.util.ArrayList<>();
                    while (rs.next()) {
                        Object[] lerroa = {
                            rs.getInt("Bidaia_id"),
                            rs.getString("gidari_nan"),
                            rs.getString("erabiltzaile_nan"),
                            rs.getDate("data") != null ? DATA_FORMATUA.format(rs.getDate("data")) : "",
                            rs.getTime("Ordua") != null ? ORDU_FORMATUA.format(rs.getTime("Ordua")) : "",
                            rs.getInt("Pertsona_kopurua"),
                            rs.getString("hasiera"),
                            rs.getString("helmuga")
                        };
                        datuak.add(lerroa);
                    }

                    // Swing-en hiloan eguneratu
                    SwingUtilities.invokeLater(() -> {
                        modelo.setRowCount(0);
                        for (Object[] lerroa : datuak) {
                            modelo.addRow(lerroa);
                        }
                    });
                }
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(null, 
                        "Errorea datuak kargatzean: " + e.getMessage(), 
                        "Errorea", JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            }
        }).start();
    }
}