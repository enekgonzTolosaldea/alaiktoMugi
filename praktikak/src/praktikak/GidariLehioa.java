package praktikak;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import datubasea.DB_Gidariak;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class GidariLehioa extends JFrame
{

	private DefaultTableModel	modelo;
	private JTable				taula;
	private JTextField			bilatzailea;

	public GidariLehioa()
	{
		setTitle("Kudeatzailea");
		setSize(700, 500);
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
				"ID", "Izena", "Abizena"
		}, 0);
		taula = new JTable(modelo);
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);

		// Botoiak behean
		JPanel	botoiPanela	= new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		JButton	btnGehitu	= new JButton("➕ Gehitu");
		JButton	btnEditatu	= new JButton("✏️ Editatu");
		JButton	btnEzabatu	= new JButton("❌ Ezabatu");

		botoiPanela.add(btnGehitu);
		botoiPanela.add(btnEditatu);
		botoiPanela.add(btnEzabatu);
		edukiontzia.add(botoiPanela, BorderLayout.SOUTH);

		// Datuak gehitu hasieran
		// gehituErabiltzailea("1", "Ane", "Martínez");
		// gehituErabiltzailea("2", "Jon", "Etxeberria");
		// gehituErabiltzailea("3", "Maite", "Zabaleta");

		// Bilaketa funtzionalitatea
		bilatzailea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
		{

			public void insertUpdate(javax.swing.event.DocumentEvent e)
			{
				filtrar();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e)
			{
				filtrar();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e)
			{
				filtrar();
			}

		});

		// Gehitu
		btnGehitu.addActionListener(e -> {

			JTextField	nanField		= new JTextField();
			JTextField	izenaField		= new JTextField();
			JTextField	abizenaField	= new JTextField();
			JTextField	postaField		= new JTextField();
			JTextField	tel_zenbField	= new JTextField();
			JTextField	passField		= new JTextField();
			JTextField	kokapenaField	= new JTextField();
			JTextField	lekuaField		= new JTextField();
			JTextField	matrikulaField	= new JTextField();

			JPanel		panel			= new JPanel(new GridLayout(9, 2));
			panel.add(new JLabel("NAN:"));
			panel.add(nanField);
			panel.add(new JLabel("Izena:"));
			panel.add(izenaField);
			panel.add(new JLabel("Abizena:"));
			panel.add(abizenaField);
			panel.add(new JLabel("Posta:"));
			panel.add(postaField);
			panel.add(new JLabel("Telefono zenbakia:"));
			panel.add(tel_zenbField);
			panel.add(new JLabel("Pasahitza:"));
			panel.add(passField);
			panel.add(new JLabel("Kokapena:"));
			panel.add(kokapenaField);
			panel.add(new JLabel("Lan lekua:"));
			panel.add(lekuaField);
			panel.add(new JLabel("Matrikula:"));
			panel.add(matrikulaField);

			boolean zuzena = false;

			while (!zuzena)
			{

				int result = JOptionPane
						.showConfirmDialog(this, panel, "Erabiltzaile berria", JOptionPane.OK_CANCEL_OPTION);

				if (result != JOptionPane.OK_OPTION)
				{
					break;
				}

				boolean			error		= false;
				StringBuilder	errorMsg	= new StringBuilder("Erroreak:\n");
				Border			Gorria		= BorderFactory.createLineBorder(Color.RED, 2);

				// DATUAK BALIDATZEKO
				String			nan			= nanField.getText().trim();
				String			izena		= izenaField.getText().trim();
				String			abizena		= abizenaField.getText().trim();
				String			posta		= postaField.getText().trim();
				String			telefonoa	= tel_zenbField.getText().trim();
				String			pasahitza	= passField.getText().trim();
				String			kokapena	= kokapenaField.getText().trim();
				String			lekua		= lekuaField.getText().trim();
				String			matrikula	= matrikulaField.getText().trim();

				// if (nan.isEmpty() || izena.isEmpty() || abizena.isEmpty() ||
				// posta.isEmpty() || telefonoa.isEmpty() || pasahitza.isEmpty()
				// || kokapena.isEmpty() || lekua.isEmpty() ||
				// matrikula.isEmpty())
				// {
				// JOptionPane.showMessageDialog(this , "hutsik dago", "Errorea:
				// ", JOptionPane.ERROR_MESSAGE);
				// return;
				// }

				if (!isDNI(nan))
				{
					nanField.setBorder(Gorria);
					error = true;
					errorMsg.append("NAN \n");
				}
				if (!isIzena(izena))
				{
					izenaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Izena \n");
				}
				if (!isIzena(abizena))
				{
					abizenaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Abizena \n");
				}

				if (!isIzena(lekua))
				{
					lekuaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Lan Lekua \n");
				}

				if (!isIzena(kokapena))
				{
					kokapenaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Kokapena \n");
				}

				if (!isMatrikula(matrikula))
				{
					matrikulaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Matrikula \n");
				}
				if (!isTelf(telefonoa))
				{
					tel_zenbField.setBorder(Gorria);
					error = true;
					errorMsg.append("Telefono zenbakia \n");
				}
				if (!isPosta(posta))
				{
					postaField.setBorder(Gorria);
					error = true;
					errorMsg.append("Posta \n");
				}

				if (error)
				{
					JOptionPane.showMessageDialog(this, errorMsg.toString(), "Errorea: ", JOptionPane.ERROR_MESSAGE);
					continue;
				}
				try
				{
					DB_Gidariak.addGidariak(
							nanField.getText(), izenaField.getText(), abizenaField.getText(), postaField.getText(),
							tel_zenbField.getText(), passField.getText(), kokapenaField.getText(), lekuaField.getText(),
							matrikulaField.getText()
					);

					gehituErabiltzailea();
					zuzena = true;

				}
				catch (SQLException e1)
				{
					e1.printStackTrace();
					JOptionPane.showMessageDialog(
							this, "Errorea datu-basearekin: " + e1.getMessage(), "Errorea", JOptionPane.ERROR_MESSAGE
					);
				}

				break;
			}

		});

		// Editatu
		btnEditatu.addActionListener(e -> {
			int selectedRow = taula.getSelectedRow();
			if (selectedRow >= 0)
			{
				String		id				= (String) modelo.getValueAt(selectedRow, 0);
				String		izena			= (String) modelo.getValueAt(selectedRow, 1);
				String		abizena			= (String) modelo.getValueAt(selectedRow, 2);

				JTextField	idField			= new JTextField(id);
				JTextField	izenaField		= new JTextField(izena);
				JTextField	abizenaField	= new JTextField(abizena);

				JPanel		panel			= new JPanel(new GridLayout(3, 2));
				panel.add(new JLabel("ID:"));
				panel.add(idField);
				panel.add(new JLabel("Izena:"));
				panel.add(izenaField);
				panel.add(new JLabel("Abizena:"));
				panel.add(abizenaField);

				int result = JOptionPane
						.showConfirmDialog(this, panel, "Editatu erabiltzailea", JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION)
				{
					modelo.setValueAt(idField.getText(), selectedRow, 0);
					modelo.setValueAt(izenaField.getText(), selectedRow, 1);
					modelo.setValueAt(abizenaField.getText(), selectedRow, 2);
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Hautatu erabiltzaile bat editatzeko.");
			}
		});

		// Ezabatu
		btnEzabatu.addActionListener(e -> {
			int selectedRow = taula.getSelectedRow();
			if (selectedRow >= 0)
			{
				modelo.removeRow(selectedRow);
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Hautatu erabiltzaile bat ezabatzeko.");
			}
		});
	}

	// Gehitu erabiltzailea taulan
	private void gehituErabiltzailea() throws SQLException
	{
		ResultSet rs = DB_Gidariak.getDatuak();
		
			while (rs.next());
		    {
		    	String NAN = rs.getString("NAN");
		    	String izena = rs.getString("Izena");
		    	String abizena= rs.getString("Abizena");
		    	String posta = rs.getString("Posta");
		    	String tel_zenb = rs.getString("Tel_zenb");
		    	String pass = rs.getString("Pasahitza");
		    	String kokapena = rs.getString("Kokapena");
		    	String lan_lekua = rs.getString("Lan_Lekua");
		    	String Matrikula = rs.getString("Matrikula");
		    	
		    }
		
		
		
		modelo.addRow(new Object[]
		{
				
		});
	}

	// Bilatu erabiltzaileak izenaren edo abizenaren arabera
	private void filtrar()
	{
		String								bilatu	= bilatzailea.getText().toLowerCase();
		TableRowSorter<DefaultTableModel>	sorter	= new TableRowSorter<>(modelo);
		taula.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));
	}

	public static boolean isIzena(String testua)
	{
		return testua.matches("(?i)(?=.*[a-záéíóúñü])[a-záéíóúñü\\s\\-\\.']+");
	}

	public static boolean isTelf(String testua)
	{
		return testua.matches("[0-9]{9}");
	}

	public static boolean isPosta(String testua)
	{
		return testua.matches("^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$");
	}

	public static boolean isMatrikula(String testua)
	{
		return testua.matches("[0-9]{4}[A-Z]{3}");
	}

	public static boolean isDNI(String testua)
	{
		return testua.matches("[0-9]{8}[A-Z]{1}");
	}

}
