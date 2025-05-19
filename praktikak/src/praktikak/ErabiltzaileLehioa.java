package praktikak;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import datubasea.DB_Erabiltzaileak;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class ErabiltzaileLehioa extends JFrame
{

	private DefaultTableModel	modelo;
	private JTable				taula;
	private JTextField			bilatzailea;


	public ErabiltzaileLehioa()
	{
		setTitle("Kudeatzailea");
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
		edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(edukiontzia);

		// Goiburua + Bilatzailea
		JPanel	topPanel	= new JPanel(new BorderLayout(10, 10));
		JLabel	lbl			= new JLabel("Gidariak", JLabel.LEFT);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
		topPanel.add(lbl, BorderLayout.WEST);

		bilatzailea = new JTextField();
		bilatzailea.setToolTipText("Bilatu izena edo abizena...");
		topPanel.add(bilatzailea, BorderLayout.CENTER);
		edukiontzia.add(topPanel, BorderLayout.NORTH);

		// Taula
		modelo = new DefaultTableModel(new Object[]
		{
				"NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Pasahitza"
		}, 0);
		taula = new JTable(modelo);
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);
		// Datu baseko gidarien datuak bistaratzeko
		try
		{
			gidariakBistaratu();
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Botoiak behean
		JPanel	botoiPanela	= new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		JButton	btnGehitu	= new JButton("➕ Gehitu");
		JButton	btnEditatu	= new JButton("✏️ Editatu");
		JButton	btnEzabatu	= new JButton("❌ Ezabatu");

		botoiPanela.add(btnGehitu);
		botoiPanela.add(btnEditatu);
		botoiPanela.add(btnEzabatu);
		edukiontzia.add(botoiPanela, BorderLayout.SOUTH);
	}


	// Gehitu erabiltzailea taulan
	private void gidariakBistaratu() throws SQLException
	{
		
		
		modelo.setRowCount(0);
		
		ResultSet rs = DB_Erabiltzaileak.getDatuak();

		while (rs.next())
		{
			String	NAN			= rs.getString("NAN");
			String	izena		= rs.getString("Izena");
			String	abizena		= rs.getString("Abizena");
			String	posta		= rs.getString("Posta");
			String	tel_zenb	= rs.getString("Tel_zenb");
			String	pass		= rs.getString("Pasahitza");

			modelo.addRow(new Object[]
			{
					NAN, izena, abizena, posta, tel_zenb, pass
			});
		}

	}

	// Bilatu erabiltzaileak izenaren edo abizenaren arabera
	private void filtrar()
	{
		String								bilatu	= bilatzailea.getText().toLowerCase();
		TableRowSorter<DefaultTableModel>	sorter	= new TableRowSorter<>(modelo);
		taula.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));
	}


}