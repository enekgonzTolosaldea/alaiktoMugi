package praktikak;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import datubasea.DB_Erabiltzaileak;
import datubasea.DBGestorea;
import java.awt.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 * ErabiltzaileLehioa klasea, erabiltzaileen datuak kudeatzeko leihoa da. Taula
 * batean erabiltzaileen informazioa erakusten du, eta bilatze funtzionalitatea
 * ere eskaintzen du.
 */
public class ErabiltzaileLehioa extends JFrame {
    private DefaultTableModel modelo;         // Taularako modeloa
    private JTable taula;                      // Taula non datuak erakutsiko diren
    private JTextField bilatzailea;           // Bilatzailea erabiltzailearen izen edo abizena bilatzeko

    /**
     * Eraikitzailea. Leihoa sortzen du, konfiguratzen du eta datuak kargatzen ditu.
     */
    public ErabiltzaileLehioa() {
        setTitle("Kudeatzailea"); // Leihoaren izena
        setSize(800, 600); // Leihoaren tamaina
        setResizable(false); // Leihoa ez da aldatzen tamainan
        setLocationRelativeTo(null); // Leihoa pantailaren erdian kokatzen du
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Leihoa ixterakoan aplikazioa itxi

        // Edukiontzia (panel) sortzen da, non goiburua, bilatzailea eta taula egon
        // diren
        JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
        edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(edukiontzia);

        // Goiburua + Bilatzailea
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel lbl = new JLabel("Erabiltzaileak", JLabel.LEFT);  
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lbl, BorderLayout.WEST);

        bilatzailea = new JTextField(); // Bilatzailea saguaren bidez testu bat sartzeko
        bilatzailea.setToolTipText("Bilatu izena edo abizena..."); // Tooltip-a
        topPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        // Bilaketa funtzionalitatea: erabiltzaileak idazten duen bakoitzean, taula
        // filtra daiteke
        bilatzailea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtratu();  // Bilatzailea aldatu denean taula filtratzea
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtratu();  // Bilatzailea ezabatu denean taula filtratzea
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtratu();  // Bilatzailea aldatu denean taula filtratzea
            }
        });

        // Taula eta datuak erakusteko ScrollPane
        modelo = new DefaultTableModel(new Object[]{
            "NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Pasahitza"
        }, 0);  // Taulako zutabeak definitzen ditu
        taula = new JTable(modelo);  // Taula sortzen da
        JScrollPane scrollPane = new JScrollPane(taula);  // Taula scrollable egiteko
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        // Taula editatzea debekatu egiten da (zuzenean aldatzea ezinezkoa da)
        taula.setDefaultEditor(Object.class, null);

        // Datuak kargatu (datubasetik)
        try {
            erabiltzaileakBistaratu();  // Erabiltzaileak taulan erakusteko funtzioa deitzen da
        } catch (SQLException e) {
            e.printStackTrace();  // Datuak kargatzean errore bat egon bada, mezua inprimatu
            JOptionPane.showMessageDialog(this, "Errorea erabiltzaileak kargatzean: " + e.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE);
        }

        // Taula klikarekin historiala ikusteko aukera gehitzen da
        taula.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = taula.getSelectedRow();
                if (row != -1) {
                    String NAN = modelo.getValueAt(row, 0).toString(); // NAN lortzen da (taulako lehenengo zutabea)
                    ikusiHistoriala(NAN); // NAN hori erabiliz historialeko datuak lortzen dira
                }
            }
        });

        // Botoiak behean
        JPanel botoiPanela = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        edukiontzia.add(botoiPanela, BorderLayout.SOUTH);
    }

    /**
     * Datu-baseko erabiltzaileen datuak taulan bistaratzen ditu.
     *
     * @throws SQLException SQL errorea gertatzen bada
     */
    private void erabiltzaileakBistaratu() throws SQLException {
        modelo.setRowCount(0);  // Taula hustu

        // Datu-baseko datuak eskuratu
        ResultSet rs = DB_Erabiltzaileak.getDatuak();

        // Datu-baseko emaitzak taulan sartu
        while (rs.next()) {
            String NAN = rs.getString("NAN");
            String izena = rs.getString("Izena");
            String abizena = rs.getString("Abizena");
            String posta = rs.getString("Posta");
            String tel_zenb = rs.getString("Tel_zenb");
            String pass = rs.getString("Pasahitza");

            modelo.addRow(new Object[]{
                NAN, izena, abizena, posta, tel_zenb, pass // Datuak gehitzen dira taulako row batean
            });
        }
        // Itxi ResultSet
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Taula filtratzeko funtzioa. Bilatzailean sartutako testuaren arabera taula
     * filtratzen du.
     */
    private void filtratu() {
        // Bilatzailea lortu eta lortutako testua txiki-txiki bihurtu (case insensitive)
        String bilatu = bilatzailea.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);  // Taulako row-ak ordenatzeko
        taula.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));  // Bilatzailea regex bidez aplikatzen da
    }

    /**
     * Erabiltzailearen NAN-a erabiliz, bere historialeko bidaiak erakusten ditu.
     * Historiala hutsik badago, ez du leihoa irekiko.
     *
     * @param NAN Erabiltzailearen NAN-a
     */
    private void ikusiHistoriala(String NAN) {
        DefaultTableModel historialModelo = new DefaultTableModel(new Object[]{
            "Bidaia ID", "Gidari NAN", "Erabiltzaile NAN", "Data", "Ordua", "Pertsona kopurua", "Hasiera", "Helmuga"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Taula ez editagarri bihurtu
            }
        };

        ResultSet rs = null;
        boolean datuakAurkituDira = false;

        try {
            System.out.println("Historiala bilatzen NAN honentzat: " + NAN);
            rs = DBGestorea.getHistorikoak(NAN);

            if (rs.isBeforeFirst()) {
                datuakAurkituDira = true;
                while (rs.next()) {
                    int bidaia_ID = rs.getInt("Bidaia_id");
                    String Gidari_nan = rs.getString("Gidari_nan");
                    String erabiltzaile_nan = rs.getString("erabiltzaile_nan");
                    Date data = rs.getDate("data");
                    Time ordua = rs.getTime("ordua");
                    String pertsona_kopurua = rs.getString("pertsona_kopurua");
                    String hasiera = rs.getString("hasiera");
                    String helmuga = rs.getString("helmuga");

                    historialModelo.addRow(new Object[]{
                        bidaia_ID, Gidari_nan, erabiltzaile_nan, data, ordua, pertsona_kopurua, hasiera, helmuga
                    });
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Ez da historialik aurkitu erabiltzaile honentzat.",
                    "Historiala hutsik",
                    JOptionPane.INFORMATION_MESSAGE);
                return; // Funtziotik irten, leihoa ez irekitzeko
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Errorea gertatu da historialeko datuak kargatzean: " + e.getMessage(),
                "Errorea",
                JOptionPane.ERROR_MESSAGE);
            return; // Errore bat badago, funtziotik irten, leihoa ez irekitzeko
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (datuakAurkituDira) {
            // --- Creación de la ventana del historial ---
            JFrame historialLehioa = new JFrame("Erabiltzailearen Historiala: " + NAN);
            historialLehioa.setSize(800, 600); // Tamaño similar a la ventana principal
            historialLehioa.setResizable(false); // No redimensionable, como la principal
            historialLehioa.setLocationRelativeTo(this);
            historialLehioa.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Contenedor principal de la ventana del historial
            JPanel historialEdukiontzia = new JPanel(new BorderLayout(10, 10));
            historialEdukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10)); // Mismos márgenes que la principal
            historialLehioa.setContentPane(historialEdukiontzia);

            // --- Panel Superior (Título + Buscador) ---
            JPanel historialTopPanel = new JPanel(new BorderLayout(10, 10)); // Mismos espaciados que el principal
            
            // Título de la ventana de historial
            JLabel historialTitleLabel = new JLabel("Historiala", JLabel.LEFT);
            historialTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
            historialTopPanel.add(historialTitleLabel, BorderLayout.WEST);

            // Buscador del historial
            JTextField historialBilatzailea = new JTextField();
            historialBilatzailea.setToolTipText("Bilatu historialeko datuak (adibidez: data, hasiera, helmuga...)");
            historialTopPanel.add(historialBilatzailea, BorderLayout.CENTER); // Buscador en el centro

            historialEdukiontzia.add(historialTopPanel, BorderLayout.NORTH); // Añadir el panel de cabecera al NORTE de la ventana

            // --- Tabla de Historial ---
            JTable historialTaula = new JTable(historialModelo);
            historialTaula.setFillsViewportHeight(true);
            historialTaula.setDefaultEditor(Object.class, null); // Asegurar que no sea editable
            JScrollPane scrollPane = new JScrollPane(historialTaula);
            historialEdukiontzia.add(scrollPane, BorderLayout.CENTER); // Tabla en el centro

            // Filtrado del historial
            TableRowSorter<DefaultTableModel> historialSorter = new TableRowSorter<>(historialModelo);
            historialTaula.setRowSorter(historialSorter);

            historialBilatzailea.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
                }

                public void removeUpdate(DocumentEvent e) {
                    filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
                }

                public void changedUpdate(DocumentEvent e) {
                    filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
                }
            });

            historialLehioa.setVisible(true);
        }
    }

    /**
     * Historialeko taula filtratzeko funtzioa.
     * @param testua Bilatzailean sartutako testua
     * @param sorter Taulako RowSorter-a
     */
    private void filtratuHistoriala(String testua, TableRowSorter<DefaultTableModel> sorter) {
        String bilatu = testua.toLowerCase();
        if (bilatu.trim().length() == 0) {
            sorter.setRowFilter(null); // Ez dago testurik, ezabatu iragazkia
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu)); // Iragazki insensitive
        }
    }
}