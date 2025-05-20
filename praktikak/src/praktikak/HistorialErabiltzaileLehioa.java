package praktikak;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;

import datubasea.DBGestorea;

/**
 * HistorialUsuarioLehioa, erabiltzailearen bidaiaren historian bistaratzen duen leihoa.
 */
public class HistorialErabiltzaileLehioa extends JFrame {

    private DefaultTableModel modelo; // Taula modeloa
    private JTable taula;             // Taula non bidaiak erakutsiko diren
    private JTextField bilatzailea;           // Bilatzailea erabiltzailearen izen edo abizena bilatzeko
    
    /**
     * Eraikitzailea. Historiala bistaratzen duen leihoa sortzen du.
     * 
     * @param NAN Erabiltzailearen NAN
     */
    public HistorialErabiltzaileLehioa(String NAN) {
        setTitle("Erabiltzailearen Historiala"); // Leihoaren izena
        setSize(800, 600); // Leihoaren tamaina
        setResizable(false); // Leihoa ez da aldatzen tamainan
        setLocationRelativeTo(null); // Leihoa pantailaren erdian kokatzen du
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Leihoa ixterakoan aplikazioa itxi

        JPanel edukiontzia = new JPanel(new BorderLayout(10, 10)); // Edukiontzia panel-a
        setContentPane(edukiontzia);

        // Goiburua
        JLabel lbl = new JLabel("Erabiltzailearen Bidaiak", JLabel.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(lbl, BorderLayout.WEST);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        
        bilatzailea = new JTextField(); // Bilatzailea saguaren bidez testu bat sartzeko
        bilatzailea.setToolTipText("Bilatu izena edo abizena..."); // Tooltip-a
        topPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        
        // Taula modeloa
        modelo = new DefaultTableModel(new Object[] {
            "bidaia_Id", "gidari_nan"
        }, 0);

        taula = new JTable(modelo); // Taula sortzen da
        JScrollPane scrollPane = new JScrollPane(taula); // Taula scrollable egiteko
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        // Historiala lortu eta taulan erakutsi
        try {
            bidaiakBistaratu(NAN);
        } catch (SQLException e) {
            e.printStackTrace(); // Historiala kargatzean errore bat egon bada
        }
    }

    /**
     * Erabiltzailearen bidaiak datu-basean bilatzen ditu eta taulan erakusten ditu.
     * 
     * @param NAN Erabiltzailearen NAN
     * @throws SQLException SQL errorea gertatzen bada
     */
    private void bidaiakBistaratu(String NAN) throws SQLException {
        // Datu-baseko bidaiak eskuratu
        ResultSet rs = DBGestorea.getHistorikoak(NAN); // Llamamos a la función getHistorikoak

        // Taula hustu
        modelo.setRowCount(0);

        // Datuak taulan gehitu
        while (rs.next()) {
            String Bidaia_Id = rs.getString("Bidaia_Id");
            String Gidari_nan = rs.getString("Gidari_nan");
            String Erabiltzaile_nan = rs.getString("Erabiltzaile_nan");
            Date data = rs.getDate("data");
            Time ordua = rs.getTime("ordua");
            String pertsona_kopurua = rs.getString("pertsona_kopurua");
            String hasiera = rs.getString("hasiera");
            String helmuga = rs.getString("helmuga");
            String egoera = rs.getString("egoera");




            modelo.addRow(new Object[] {
            		Bidaia_Id, Gidari_nan,Erabiltzaile_nan,data,ordua,pertsona_kopurua, hasiera, helmuga, egoera
            });
        }
    }
    private void filtratu()
    {
        // Bilatzailea lortu eta lortutako testua txiki-txiki bihurtu (case insensitive)
        String bilatu = bilatzailea.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);  // Taulako row-ak ordenatzeko
        taula.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));  // Bilatzailea regex bidez aplikatzen da
    }
}
