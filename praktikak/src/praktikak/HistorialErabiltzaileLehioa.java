package praktikak;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;
import datubasea.DBGestorea;

public class HistorialErabiltzaileLehioa extends JFrame {

    private DefaultTableModel taulaModeloa;
    private JTable taula;
    private JTextField bilatzailea;
    private TableRowSorter<DefaultTableModel> iragazkia;
    private JComboBox<String> zutabeCombo;

    public HistorialErabiltzaileLehioa(String NAN) {
        setTitle("Erabiltzailearen Bidai Historiala");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Panel nagusia
        JPanel edukiontziNagusia = new JPanel(new BorderLayout(10, 10));
        setContentPane(edukiontziNagusia);

        // Goiko panela (izenburua, bilaketa eta iragazkia)
        JPanel goikoPanela = new JPanel(new BorderLayout(10, 10));

        // Izenburua
        JLabel izenburua = new JLabel("Erabiltzailearen Bidaiak", JLabel.LEFT);
        izenburua.setFont(new Font("Arial", Font.BOLD, 20));
        goikoPanela.add(izenburua, BorderLayout.WEST);

        // Bilaketa testu-eremua
        bilatzailea = new JTextField();
        bilatzailea.setToolTipText("Bilatu testua idatzita...");

        // Zutabeen hautatzailea
        String[] zutabeAukerak = {
            "Guztiak", "Bidaia ID", "Gidari NAN", "Erabiltzaile NAN", 
            "Data", "Ordua", "Pertsona Kopurua", "Hasiera", "Helmuga", "Egoera"
        };
        zutabeCombo = new JComboBox<>(zutabeAukerak);
        zutabeCombo.setToolTipText("Hautatu zein zutabetan bilatu");

        // Bilaketa eta hautaketa batera
        JPanel bilaketaPanela = new JPanel(new BorderLayout(5, 5));
        bilaketaPanela.add(bilatzailea, BorderLayout.CENTER);
        bilaketaPanela.add(zutabeCombo, BorderLayout.EAST);
        goikoPanela.add(bilaketaPanela, BorderLayout.CENTER);

        edukiontziNagusia.add(goikoPanela, BorderLayout.NORTH);

        // Taula modeloa sortu
        taulaModeloa = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int zutabeIndizea) {
                return String.class; // Datu guztiak kate bezala tratatu
            }
        };

        // Taula konfiguratu
        taula = new JTable(taulaModeloa);
        iragazkia = new TableRowSorter<>(taulaModeloa);
        taula.setRowSorter(iragazkia);
        JScrollPane korritzePanela = new JScrollPane(taula);
        edukiontziNagusia.add(korritzePanela, BorderLayout.CENTER);

        // Bilaketa automatikoki eguneratzeko
        bilatzailea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { eguneratuIragazkia(); }

            @Override
            public void removeUpdate(DocumentEvent e) { eguneratuIragazkia(); }

            @Override
            public void changedUpdate(DocumentEvent e) { eguneratuIragazkia(); }
        });

        // Datuak kargatu
        try {
            kargatuBidaiak(NAN);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errorea datuak kargatzean: " + e.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void kargatuBidaiak(String NAN) throws SQLException {
        ResultSet emaitzak = DBGestorea.getHistorikoak(NAN);
        taulaModeloa.setRowCount(0);

        // Zutabe izenak definitu
        String[] zutabeIzenak = {
            "Bidaia ID", "Gidari NAN", "Erabiltzaile NAN", "Data", 
            "Ordua", "Pertsona Kopurua", "Hasiera", "Helmuga", "Egoera"
        };
        taulaModeloa.setColumnIdentifiers(zutabeIzenak);

        // Datuak gehitu
        while (emaitzak.next()) {
            Object[] lerroa = {
                emaitzak.getString("Bidaia_Id"),
                emaitzak.getString("Gidari_nan"),
                emaitzak.getString("Erabiltzaile_nan"),
                emaitzak.getDate("data"),
                emaitzak.getTime("ordua"),
                emaitzak.getString("pertsona_kopurua"),
                emaitzak.getString("hasiera"),
                emaitzak.getString("helmuga"),
                emaitzak.getString("egoera")
            };
            taulaModeloa.addRow(lerroa);
        }
    }

    private void eguneratuIragazkia() {
        String bilaketaTestua = bilatzailea.getText().trim();
        int zutabea = zutabeCombo.getSelectedIndex() - 1; // "Guztiak" = -1

        if (bilaketaTestua.length() == 0) {
            iragazkia.setRowFilter(null);
        } else if (zutabea < 0) {
            // Guztiak: zutabe guztietan bilatu
            iragazkia.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(bilaketaTestua)));
        } else {
            // Zutabe jakin batean bilatu
            iragazkia.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(bilaketaTestua), zutabea));
        }
    }

    public static void abiarazi(String NAN) {
        SwingUtilities.invokeLater(() -> new HistorialErabiltzaileLehioa(NAN).setVisible(true));
    }
}
