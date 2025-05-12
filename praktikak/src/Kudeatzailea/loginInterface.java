package Kudeatzailea;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class loginInterface {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Menu nagusia");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);

        // Kolore eta diseinu dotorea
        JPanel panel = new JPanel();
        panel.setBackground(new Color(40, 44, 52));
        panel.setLayout(new GridBagLayout());
        frame.add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Tituloa
        JLabel title = new JLabel("Aukeratu aukera bat");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(title, gbc);

        // Botoiak estiloarekin
        JButton gidariaButton = estiloBotoia("Gidaria");
        JButton erabiltzaileaButton = estiloBotoia("Erabiltzailea");
        JButton historialaButton = estiloBotoia("Historiala");

        gbc.gridy = 1;
        panel.add(gidariaButton, gbc);
        gbc.gridy = 2;
        panel.add(erabiltzaileaButton, gbc);
        gbc.gridy = 3;
        panel.add(historialaButton, gbc);

        // Leiho desberdinak botoientzako
        gidariaButton.addActionListener(e -> irekiLeiho("Gidaria", "Gidariaren informazioa hemen."));
        erabiltzaileaButton.addActionListener(e -> irekiLeiho("Erabiltzailea", "Erabiltzailearen informazioa hemen."));
        historialaButton.addActionListener(e -> irekiLeiho("Historiala", "Historialaren datuak hemen agertuko dira."));

        frame.setVisible(true);
    }

    // Botoi estilizatua sortzen duen metodoa
    private static JButton estiloBotoia(String testua) {
        JButton botoia = new JButton(testua);
        botoia.setFocusPainted(false);
        botoia.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        botoia.setBackground(new Color(100, 149, 237)); // Kolore urdin argitsua
        botoia.setForeground(Color.WHITE);
        botoia.setPreferredSize(new Dimension(200, 40));
        botoia.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        return botoia;
    }

    // Leiho berria irekitzen duen metodoa
    private static void irekiLeiho(String titulua, String mezua) {
        JFrame leihoa = new JFrame(titulua);
        leihoa.setSize(300, 150);
        leihoa.setLocationRelativeTo(null);

        JPanel edukia = new JPanel();
        edukia.setBackground(new Color(60, 63, 65));
        edukia.setLayout(new BorderLayout());

        JLabel label = new JLabel(mezua, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        edukia.add(label, BorderLayout.CENTER);
        leihoa.add(edukia);
        leihoa.setVisible(true);
    }
}
