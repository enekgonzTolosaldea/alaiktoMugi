package praktikak;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

public class GidariLehioa extends JFrame {

    private DefaultTableModel modelo;
    private JTable taula;
    private JTextField bilatzailea;

    public GidariLehioa() {
        setTitle("Kudeatzailea");
        setSize(700, 500);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
        edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(edukiontzia);

        // Goiburua + Bilatzailea
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        JLabel lbl = new JLabel("Gidariak", JLabel.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        topPanel.add(lbl, BorderLayout.WEST);

        bilatzailea = new JTextField();
        bilatzailea.setToolTipText("Bilatu izena edo abizena...");
        topPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(topPanel, BorderLayout.NORTH);

        // Taula
        modelo = new DefaultTableModel(new Object[]{"ID", "Izena", "Abizena"}, 0);
        taula = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(taula);
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        // Botoiak behean
        JPanel botoiPanela = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JButton btnGehitu = new JButton("➕ Gehitu");
        JButton btnEditatu = new JButton("✏️ Editatu");
        JButton btnEzabatu = new JButton("❌ Ezabatu");

        botoiPanela.add(btnGehitu);
        botoiPanela.add(btnEditatu);
        botoiPanela.add(btnEzabatu);
        edukiontzia.add(botoiPanela, BorderLayout.SOUTH);

        // Datuak gehitu hasieran
        gehituErabiltzailea("1", "Ane", "Martínez");
        gehituErabiltzailea("2", "Jon", "Etxeberria");
        gehituErabiltzailea("3", "Maite", "Zabaleta");

        // Bilaketa funtzionalitatea
        bilatzailea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
        });

        // Gehitu
        btnGehitu.addActionListener(e -> {
            JTextField idField = new JTextField();
            JTextField izenaField = new JTextField();
            JTextField abizenaField = new JTextField();

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("ID:"));
            panel.add(idField);
            panel.add(new JLabel("Izena:"));
            panel.add(izenaField);
            panel.add(new JLabel("Abizena:"));
            panel.add(abizenaField);

            int result = JOptionPane.showConfirmDialog(this, panel, "Erabiltzaile berria", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                gehituErabiltzailea(idField.getText(), izenaField.getText(), abizenaField.getText());
            }
        });

        // Editatu
        btnEditatu.addActionListener(e -> {
            int selectedRow = taula.getSelectedRow();
            if (selectedRow >= 0) {
                String id = (String) modelo.getValueAt(selectedRow, 0);
                String izena = (String) modelo.getValueAt(selectedRow, 1);
                String abizena = (String) modelo.getValueAt(selectedRow, 2);

                JTextField idField = new JTextField(id);
                JTextField izenaField = new JTextField(izena);
                JTextField abizenaField = new JTextField(abizena);

                JPanel panel = new JPanel(new GridLayout(3, 2));
                panel.add(new JLabel("ID:"));
                panel.add(idField);
                panel.add(new JLabel("Izena:"));
                panel.add(izenaField);
                panel.add(new JLabel("Abizena:"));
                panel.add(abizenaField);

                int result = JOptionPane.showConfirmDialog(this, panel, "Editatu erabiltzailea", JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.OK_OPTION) {
                    modelo.setValueAt(idField.getText(), selectedRow, 0);
                    modelo.setValueAt(izenaField.getText(), selectedRow, 1);
                    modelo.setValueAt(abizenaField.getText(), selectedRow, 2);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Hautatu erabiltzaile bat editatzeko.");
            }
        });

        // Ezabatu
        btnEzabatu.addActionListener(e -> {
            int selectedRow = taula.getSelectedRow();
            if (selectedRow >= 0) {
                modelo.removeRow(selectedRow);
            } else {
                JOptionPane.showMessageDialog(this, "Hautatu erabiltzaile bat ezabatzeko.");
            }
        });
    }

    // Gehitu erabiltzailea taulan
    private void gehituErabiltzailea(String id, String izena, String abizena) {
        modelo.addRow(new Object[]{id, izena, abizena});
    }

    // Bilatu erabiltzaileak izenaren edo abizenaren arabera
    private void filtrar() {
        String bilatu = bilatzailea.getText().toLowerCase();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(modelo);
        taula.setRowSorter(sorter);
        sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));
    }
}
