package praktikak;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import datubasea.DB_Erabiltzaileak;
import datubasea.DBGestorea;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter; 
import java.awt.event.WindowEvent;   
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.RowFilter;

public class ErabiltzaileLehioa extends JFrame {

    private DefaultTableModel modelo;
    private JTable taula;
    private JTextField bilatzailea;
    private TableRowSorter<DefaultTableModel> sorter;
    private Timer historialRefreshTimer;

    private static final String HUTSIK_EGOERA_TXARTELA = "HutsikEgoeraTxartela";
    private static final String TAULA_BISTA_TXARTELA = "TaulaBistaTxartela";

    public ErabiltzaileLehioa() {
        setTitle("Gidarien Kudeaketa");
        setSize(800, 600);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
        edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(edukiontzia);

        JPanel goikoPanel = new JPanel(new BorderLayout(10, 10));
        JLabel lbl = new JLabel("Gidariak", JLabel.LEFT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
        goikoPanel.add(lbl, BorderLayout.WEST);

        bilatzailea = new JTextField();
        bilatzailea.setToolTipText("Bilatu NAN, izena edo abizena...");
        goikoPanel.add(bilatzailea, BorderLayout.CENTER);
        edukiontzia.add(goikoPanel, BorderLayout.NORTH);

        bilatzailea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filtratu();
            }
            public void removeUpdate(DocumentEvent e) {
                filtratu();
            }
            public void changedUpdate(DocumentEvent e) {
                filtratu();
            }
        });

        int delay = 15000;
        ActionListener refreshTable = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    erabiltzaileakBistaratu();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        };
        historialRefreshTimer = new Timer(delay, refreshTable);
        historialRefreshTimer.start();

        modelo = new DefaultTableModel(new Object[][]{}, new String[] {
            "NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Pasahitza"
        });
        taula = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(taula);
        edukiontzia.add(scrollPane, BorderLayout.CENTER);

        taula.setDefaultEditor(Object.class, null);

        try {
            erabiltzaileakBistaratu();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errorea gidariak kargatzean: " + e.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE);
        }

        taula.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = taula.getSelectedRow();
                if (row != -1) {
                    int modeloLerroa = taula.convertRowIndexToModel(row);
                    String NAN = modelo.getValueAt(modeloLerroa, 0).toString();
                    ikusiHistoriala(NAN);
                }
            }
        });

        JPanel botoiPanela = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        edukiontzia.add(botoiPanela, BorderLayout.SOUTH);
    }

    private void erabiltzaileakBistaratu() throws SQLException {
        modelo.setRowCount(0);
        ResultSet rs = null;
        try {
            rs = DB_Erabiltzaileak.getDatuak();
            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("NAN"), rs.getString("Izena"), rs.getString("Abizena"), rs.getString("Posta"),
                    rs.getString("Tel_zenb"), rs.getString("Pasahitza")
                });
            }
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
        }
    }

    private void filtratu() {
        String bilatu = bilatzailea.getText().trim();
        sorter = new TableRowSorter<>(modelo);
        taula.setRowSorter(sorter);

        if (bilatu.isEmpty())
            sorter.setRowFilter(null);
        else
            sorter.setRowFilter(RowFilter.regexFilter("(?i)\\Q" + bilatu + "\\E"));
    }

    private void ikusiHistoriala(String NAN) {
        boolean hasTrips = false;
        ResultSet rsCheck = null;
        try {
            rsCheck = DBGestorea.getHistorikoak(NAN);
            if (rsCheck != null && rsCheck.next()) {
                hasTrips = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errorea historialeko datuak egiaztatzean: " + e.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            if (rsCheck != null) {
                try {
                    rsCheck.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!hasTrips) {
            JOptionPane.showMessageDialog(this, "Erabiltzaile honek ez du bidaia historialik erregistratuta.", "Ohartarazpena", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFrame historialLehioa = new JFrame("Erabiltzailearen Historiala: " + NAN);
        historialLehioa.setSize(800, 600);
        historialLehioa.setResizable(false);
        historialLehioa.setLocationRelativeTo(this);
        historialLehioa.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        CardLayout historialTxartelDiseinua = new CardLayout();
        JPanel historialTxartelPanela = new JPanel(historialTxartelDiseinua);
        historialTxartelPanela.setBorder(new EmptyBorder(10, 10, 10, 10));
        historialLehioa.setContentPane(historialTxartelPanela);

        JPanel hutsikEgoeraPanela = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel goiburuaEtiketa = new JLabel("Ez dago bidaia historialik");
        goiburuaEtiketa.setFont(new Font("Segoe UI", Font.BOLD, 24));
        goiburuaEtiketa.setHorizontalAlignment(SwingConstants.CENTER);
        hutsikEgoeraPanela.add(goiburuaEtiketa, gbc);
        gbc.gridy++;

        JTextArea deskribapenArea = new JTextArea("Zure bidaia historiala hemen agertuko da.\n" +
                "Ez dago daturik erakusteko.");
        deskribapenArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        deskribapenArea.setWrapStyleWord(true);
        deskribapenArea.setLineWrap(true);
        deskribapenArea.setOpaque(false);
        deskribapenArea.setEditable(false);
        deskribapenArea.setFocusable(false);
        deskribapenArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        deskribapenArea.setBorder(null);
        deskribapenArea.setForeground(Color.GRAY);
        hutsikEgoeraPanela.add(deskribapenArea, gbc);
        gbc.gridy++;

        JButton gehituBidaiaBotoia = new JButton("Gehitu Bidaia");
        gehituBidaiaBotoia.setFont(new Font("Segoe UI", Font.BOLD, 16));
        gehituBidaiaBotoia.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(historialLehioa, "Bidaia berria gehitzeko funtzionalitatea hemen joango litzateke.", "Ekintza", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        hutsikEgoeraPanela.add(gehituBidaiaBotoia, gbc);

        historialTxartelPanela.add(hutsikEgoeraPanela, HUTSIK_EGOERA_TXARTELA);

        DefaultTableModel historialModelo = new DefaultTableModel(new Object[][]{}, new String[]{
                "Bidaia ID", "Gidari NAN", "Erabiltzaile NAN", "Data", "Ordua", "Pertsona kopurua", "Hasiera", "Helmuga"
        });

        JPanel taulaBistaPanela = new JPanel(new BorderLayout(10, 10));
        taulaBistaPanela.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel historialGoikoPanel = new JPanel(new BorderLayout(10, 10));
        JLabel historialTitleLabel = new JLabel("Bidaia Historiala", JLabel.LEFT);
        historialTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        historialGoikoPanel.add(historialTitleLabel, BorderLayout.WEST);

        JTextField historialBilatzailea = new JTextField();
        historialBilatzailea.setToolTipText("Bilatu historialeko datuak (adibidez: data, hasiera, helmuga...)");
        historialGoikoPanel.add(historialBilatzailea, BorderLayout.CENTER);
        taulaBistaPanela.add(historialGoikoPanel, BorderLayout.NORTH);

        JTable historialTaula = new JTable(historialModelo);
        historialTaula.setFillsViewportHeight(true);
        historialTaula.setDefaultEditor(Object.class, null);
        JScrollPane scrollPane = new JScrollPane(historialTaula);
        taulaBistaPanela.add(scrollPane, BorderLayout.CENTER);

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

        historialTxartelPanela.add(taulaBistaPanela, TAULA_BISTA_TXARTELA);

        ActionListener datuakKargatuEtaEguneratu = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new SwingWorker<List<Object>, Void>() {
                    private final String driverNAN = NAN;
                    private final DefaultTableModel model = historialModelo;
                    private final JFrame parentFrame = historialLehioa;
                    private final CardLayout cardLayout = historialTxartelDiseinua;
                    private final JPanel cardPanel = historialTxartelPanela;

                    @Override
                    protected List<Object> doInBackground() throws Exception {
                        List<Object> data = new ArrayList<>();
                        ResultSet rs = null;
                        try {
                            rs = DBGestorea.getHistorikoak(driverNAN);
                            while (rs != null && rs.next()) {
                                Object[] row = new Object[]{
                                    rs.getInt("Bidaia_id"),
                                    rs.getString("Gidari_nan"),
                                    rs.getString("erabiltzaile_nan"),
                                    rs.getDate("data"),
                                    rs.getTime("ordua"),
                                    rs.getString("pertsona_kopurua"),
                                    rs.getString("hasiera"),
                                    rs.getString("helmuga")
                                };
                                data.add(row);
                            }
                        } finally {
                            if (rs != null) try { rs.close(); } catch (SQLException ex) { ex.printStackTrace(); }
                        }
                        return data;
                    }

                    @Override
                    protected void done() {
                        try {
                            List<Object> newData = get();
                            if (newData.isEmpty()) {
                                cardLayout.show(cardPanel, HUTSIK_EGOERA_TXARTELA);
                            } else {
                                model.setRowCount(0);
                                for (Object row : newData) {
                                    model.addRow((Object[]) row);
                                }
                                cardLayout.show(cardPanel, TAULA_BISTA_TXARTELA);
                            }
                        } catch (InterruptedException | ExecutionException ex) {
                            ex.printStackTrace();
                            Throwable cause = ex.getCause();
                            String errorMessage = "Errorea historialeko datuak eguneratzean.";
                            if (cause != null) errorMessage += " Kausa: " + cause.getMessage();
                            JOptionPane.showMessageDialog(parentFrame, errorMessage, "Errorea", JOptionPane.ERROR_MESSAGE);
                            cardLayout.show(cardPanel, HUTSIK_EGOERA_TXARTELA);
                        }
                    }
                }.execute();
            }
        };

        datuakKargatuEtaEguneratu.actionPerformed(null);

        final Timer historialLeihoRefreshTimer = new Timer(10000, datuakKargatuEtaEguneratu);
        historialLeihoRefreshTimer.start();

        historialLehioa.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                historialLeihoRefreshTimer.stop();
            }
        });

        historialLehioa.setVisible(true);
    }

    private void filtratuHistoriala(String text, TableRowSorter<DefaultTableModel> sorter) {
        if (text.trim().length() == 0)
            sorter.setRowFilter(null);
        else
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
    }

}
