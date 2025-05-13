package praktikak;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.event.*;

public class ErabiltzaileLehioa extends JFrame {  // "Ventana" = "Leihoa"

    private static final long serialVersionUID = 1L;
    private JPanel edukiontzia; // contentPane -> edukiontzia

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
            	MenuNagusia markoa = new MenuNagusia();
                markoa.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public ErabiltzaileLehioa() {
        setTitle("Kudeatzaile Nagusia Erabiltzaile");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(583, 426);
        setLocationRelativeTo(null); // Pantailaren erdian kokatu
        setResizable(false); 
    
        // Gradientea duen panel nagusia
        edukiontzia = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                Color kolorea1 = new Color(86, 171, 228); // goiko kolorea
                Color kolorea2 = new Color(238, 245, 249); // beheko kolorea
                GradientPaint gp = new GradientPaint(0, 0, kolorea1, getWidth(), getHeight(), kolorea2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(edukiontzia);

        // Botoien panela
        JPanel botoiPanela = new JPanel();
        botoiPanela.setLayout(new BoxLayout(botoiPanela, BoxLayout.Y_AXIS));
        botoiPanela.setOpaque(false); // Atzeko planoa gardena
        botoiPanela.setBorder(new EmptyBorder(20, 50, 20, 50));

        // Estiloak
        Font letra = new Font("Segoe UI", Font.BOLD, 32);
        Dimension botoiNeurriak = new Dimension(400, 100);
        Color urdinNormala = new Color(0, 153, 255);
        Color urdinHover = new Color(0, 123, 215);
        Color urdinKlik = new Color(0, 93, 175);

        // 1. botoia — Gidarientzako
        JButton btn1 = sortuBotoiLeuna("Gidariak", letra, botoiNeurriak, urdinNormala, urdinHover, urdinKlik);
        botoiPanela.add(btn1);
        botoiPanela.add(Box.createRigidArea(new Dimension(0, 20))); // Espazioa botoien artean
        btn1.addActionListener(e -> {
            GidariLehioa erabiltzaileLeihoa = new GidariLehioa();
            erabiltzaileLeihoa.setVisible(true);
        });

        // 2. botoia — erabiltzaileentzako
        JButton btn2 = sortuBotoiLeuna("Erabiltzaileak", letra, botoiNeurriak, urdinNormala, urdinHover, urdinKlik);
        botoiPanela.add(btn2);

        // Gehitu botoien panela edukiontzira
        edukiontzia.add(botoiPanela);
    }

    // Botoiak efektu leunarekin sortzeko metodoa
    private JButton sortuBotoiLeuna(String testua, Font letra, Dimension neurriak,
                                  Color normala, Color hover, Color klikatuta) {
        JButton botoia = new JButton(testua);
        botoia.setFont(letra);
        botoia.setForeground(Color.WHITE);
        botoia.setBackground(normala);
        botoia.setFocusPainted(false);
        botoia.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(255, 255, 255, 50), 2),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        botoia.setMaximumSize(neurriak);
        botoia.setAlignmentX(Component.CENTER_ALIGNMENT);
        botoia.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Animazio leuna hover eta klik efekturako
        Timer hoverTimer = new Timer(10, null);
        Timer pressTimer = new Timer(10, null);

        final Color[] unekoKolorea = {normala};

        hoverTimer.addActionListener(e -> {
            Color helburua = botoia.getModel().isRollover() ? hover : normala;
            unekoKolorea[0] = koloreaTrantsizioa(unekoKolorea[0], helburua, 0.2f);
            botoia.setBackground(unekoKolorea[0]);
            if (unekoKolorea[0].equals(helburua)) {
                hoverTimer.stop();
            }
        });

        pressTimer.addActionListener(e -> {
            Color helburua = botoia.getModel().isPressed() ? klikatuta :
                             botoia.getModel().isRollover() ? hover : normala;
            unekoKolorea[0] = koloreaTrantsizioa(unekoKolorea[0], helburua, 0.3f);
            botoia.setBackground(unekoKolorea[0]);
            if (unekoKolorea[0].equals(helburua)) {
                pressTimer.stop();
            }
        });

        botoia.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hoverTimer.start();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverTimer.start();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                pressTimer.start();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pressTimer.start();
            }
        });


        return botoia;
    }

     private Color koloreaTrantsizioa(Color hasiera, Color helmuga, float ratio) {
        int gorria = (int) (hasiera.getRed() + (helmuga.getRed() - hasiera.getRed()) * ratio);
        int berdea = (int) (hasiera.getGreen() + (helmuga.getGreen() - hasiera.getGreen()) * ratio);
        int urdina = (int) (hasiera.getBlue() + (helmuga.getBlue() - hasiera.getBlue()) * ratio);
        return new Color(gorria, berdea, urdina);
    }
}

