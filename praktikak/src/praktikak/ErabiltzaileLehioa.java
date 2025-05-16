package praktikak;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;

import datubasea.DBErabiltzaileak;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ErabiltzaileLehioa klasea erabiltzaileen informazioa erakusten duen GUIa da.
 */
public class ErabiltzaileLehioa extends JFrame {

    private static final long serialVersionUID = 1L;
    private DefaultTableModel modelo;
    private JTable taula;
    private JTextField bilatzailea;

    public ErabiltzaileLehioa() {
        setTitle("Kudeatzailea");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
        edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(edukiontzia);

        // Goiburua + Bilatzailea
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel lbl = new JLabel("Erabiltzaileak", JLabel.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lbl, BorderLayout.WEST);

        bilatzailea = new JTextField();
        bilatzailea.setToolTipText("Bilatu izena edo abizena...");
        topPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        // Taula
        modelo = new DefaultTableModel(new Object[]{
            "ID", "Gidari NAN", "Erabiltzaile NAN", "Data", "Ordua", 
            "Pertsona Kopurua", "Jatorria", "Helmuga"
        }, 0);
        
        taula = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(taula);
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        // Datuak kargatu
        kargatuDatuak();

        // Bilaketa funtzionalitatea
        bilatzailea.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtratu();
            }
        });
    }

    private void kargatuDatuak() {
        DBErabiltzaileak.erabiltzaileGuztienHistorialaIkusi(modelo);
    }

    private void filtratu() {
        String bilatu = bilatzailea.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        taula.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ErabiltzaileLehioa leihoa = new ErabiltzaileLehioa();
            leihoa.setVisible(true);
        });
    }
}