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

/**
 * ErabiltzaileLehioa klasea, erabiltzaileen datuak kudeatzeko leihoa da. Taula
 * batean erabiltzaileen informazioa erakusten du, eta bilatze funtzionalitatea
 * ere eskaintzen du.
 */
public class ErabiltzaileLehioa extends JFrame
{
    private DefaultTableModel modelo;         // Taularako modeloa
    private JTable taula;                      // Taula non datuak erakutsiko diren
    private JTextField bilatzailea;           // Bilatzailea erabiltzailearen izen edo abizena bilatzeko

    /**
     * Eraikitzailea. Leihoa sortzen du, konfiguratzen du eta datuak kargatzen ditu.
     */
    public ErabiltzaileLehioa()
    {
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
        JLabel lbl = new JLabel("Gidariak", JLabel.LEFT);  // Goiburua: "Gidariak"
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lbl, BorderLayout.WEST);

        bilatzailea = new JTextField(); // Bilatzailea saguaren bidez testu bat sartzeko
        bilatzailea.setToolTipText("Bilatu izena edo abizena..."); // Tooltip-a
        topPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        // Bilaketa funtzionalitatea: erabiltzaileak idazten duen bakoitzean, taula
        // filtra daiteke
        bilatzailea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
        {
            public void insertUpdate(javax.swing.event.DocumentEvent e)
            {
                filtratu();  // Bilatzailea aldatu denean taula filtratzea
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e)
            {
                filtratu();  // Bilatzailea ezabatu denean taula filtratzea
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e)
            {
                filtratu();  // Bilatzailea aldatu denean taula filtratzea
            }
        });

        // Taula eta datuak erakusteko ScrollPane
        modelo = new DefaultTableModel(new Object[] {
            "NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Pasahitza"
        }, 0);  // Taulako zutabeak definitzen ditu
        taula = new JTable(modelo);  // Taula sortzen da
        JScrollPane scrollPane = new JScrollPane(taula);  // Taula scrollable egiteko
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        // Taula editatzea debekatu egiten da (zuzenean aldatzea ezinezkoa da)
        taula.setDefaultEditor(Object.class, null);

        // Datuak kargatu (datubasetik)
        try
        {
            erabiltzaileakBistaratu();  // Erabiltzaileak taulan erakusteko funtzioa deitzen da
        }
        catch (SQLException e)
        {
            e.printStackTrace();  // Datuak kargatzean errore bat egon bada, mezua inprimatu
        }
        
        // Taula klikarekin historiala ikusteko aukera gehitzen da
        taula.addMouseListener(new java.awt.event.MouseAdapter()
        {
            public void mouseClicked(java.awt.event.MouseEvent evt)
            {
                int row = taula.getSelectedRow();
                if (row != -1)
                {
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
    private void erabiltzaileakBistaratu() throws SQLException
    {
        modelo.setRowCount(0);  // Taula hustu

        // Datu-baseko datuak eskuratu
        ResultSet rs = DB_Erabiltzaileak.getDatuak();

        // Datu-baseko emaitzak taulan sartu
        while (rs.next())
        {
            String NAN = rs.getString("NAN");
            String izena = rs.getString("Izena");
            String abizena = rs.getString("Abizena");
            String posta = rs.getString("Posta");
            String tel_zenb = rs.getString("Tel_zenb");
            String pass = rs.getString("Pasahitza");

            modelo.addRow(new Object[] {
                NAN, izena, abizena, posta, tel_zenb, pass // Datuak gehitzen dira taulako row batean
            });
        }
    }

    /**
     * Taula filtratzeko funtzioa. Bilatzailean sartutako testuaren arabera taula
     * filtratzen du.
     */
    private void filtratu()
    {
        // Bilatzailea lortu eta lortutako testua txiki-txiki bihurtu (case insensitive)
        String bilatu = bilatzailea.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);  // Taulako row-ak ordenatzeko
        taula.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));  // Bilatzailea regex bidez aplikatzen da
    }

    /**
     * Erabiltzailearen NAN-a erabiliz, bere historialeko bidaiak erakusten ditu.
     * 
     * @param NAN Erabiltzailearen NAN-a
     */
    
    
    private void ikusiHistoriala(String NAN)
    {
        // Historikoa erakusteko leihoa
        JFrame historialLehioa = new JFrame("Erabiltzailearen Historiala");
        historialLehioa.setSize(600, 400);

        DefaultTableModel historialModelo = new DefaultTableModel(new Object[] {
            "Bidaia ID", "gidari_nan", "erabiltzaile_nan","data","ordua","pertsona_kopurua","hasiera", "helmuga"
        }, 0);

        JTable historialTaula = new JTable(historialModelo);
        JScrollPane scrollPane = new JScrollPane(historialTaula);
        historialLehioa.add(scrollPane, BorderLayout.CENTER);

        try
        {
            // Historikoaren datuak eskuratu
            ResultSet rs = DBGestorea.getHistorikoak(NAN);

            while (rs.next())
            {
                int bidaia_ID = rs.getInt("Bidaia_id");
                String Gidari_nan = rs.getString("Gidari_nan");
                String erabiltzaile_nan = rs.getString("erabiltzaile_nan");
                Date data = rs.getDate("data");
                Time ordua = rs.getTime("ordua");
                String pertsona_kopurua = rs.getString("pertsona_kopurua");
                String hasiera = rs.getString("hasiera");
                String helmuga = rs.getString("helmuga");


                

                historialModelo.addRow(new Object[] { bidaia_ID, Gidari_nan, erabiltzaile_nan,data, ordua, pertsona_kopurua, hasiera, helmuga});
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();  // Errorea egon bada, inprimatu
        }

        historialLehioa.setVisible(true); // Historikoaren leihoa ikusi
    }
    
}
