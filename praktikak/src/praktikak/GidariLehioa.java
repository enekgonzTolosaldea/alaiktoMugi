package praktikak;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import datubasea.DB_Gidariak;

import java.awt.*;
import java.awt.event.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GidariLehioa extends JFrame
{

	private DefaultTableModel	modelo;
	private JTable				taula;
	private JTextField			bilatzailea;

	public enum Egoera
	{
		Gehitu, Editatu;
	}

	public enum Mota
	{
		DNI, Matrikula, posta, telf, izena;
	}

	public GidariLehioa()
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
				"NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Pasahitza", "Kokapena", "Lan lekua", "Matrikula"
		}, 0);
		taula = new JTable(modelo);
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);
		taula.setDefaultEditor(Object.class, null);
		taula.setRowHeight(30);
		
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER); // testua zentratzeko
		renderer.setBorder(new EmptyBorder(10, 10, 10, 10)); // Añade padding interno

		for (int i = 0; i < taula.getColumnCount(); i++) {
		    taula.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
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

			balidazioa(Egoera.Gehitu, nanField, izenaField, abizenaField, postaField, tel_zenbField, passField, kokapenaField, lekuaField, matrikulaField, panel);

		});

		// Editatu
		btnEditatu.addActionListener(e -> {
			int selectedRow = taula.getSelectedRow();
			System.out.println(selectedRow);
			if (selectedRow >= 0)
			{
				String		nan				= (String) modelo.getValueAt(selectedRow, 0);
				String		izena			= (String) modelo.getValueAt(selectedRow, 1);
				String		abizena			= (String) modelo.getValueAt(selectedRow, 2);
				String		posta			= (String) modelo.getValueAt(selectedRow, 3);
				String		tel_zenb		= (String) modelo.getValueAt(selectedRow, 4);
				String		pasahitza		= (String) modelo.getValueAt(selectedRow, 5);
				String		kokapena		= (String) modelo.getValueAt(selectedRow, 6);
				String		lan_lekua		= (String) modelo.getValueAt(selectedRow, 7);
				String		matrikula		= (String) modelo.getValueAt(selectedRow, 8);

				JTextField	nanField		= new JTextField(nan);
				JTextField	izenaField		= new JTextField(izena);
				JTextField	abizenaField	= new JTextField(abizena);
				JTextField	postaField		= new JTextField(posta);
				JTextField	tel_zenbField	= new JTextField(tel_zenb);
				JTextField	passField		= new JTextField(pasahitza);
				JTextField	kokapenaField	= new JTextField(kokapena);
				JTextField	lekuaField		= new JTextField(lan_lekua);
				JTextField	matrikulaField	= new JTextField(matrikula);

				nanField.setEditable(false);

				JPanel panel = new JPanel(new GridLayout(9, 2));
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

				balidazioa(Egoera.Editatu, nanField, izenaField, abizenaField, postaField, tel_zenbField, passField, kokapenaField, lekuaField, matrikulaField, panel);

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

				String nan = (String) modelo.getValueAt(selectedRow, 0);

				System.out.println(nan);
				try
				{
					DB_Gidariak.deleteGidaria(nan);
				}
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try
				{

					gidariakBistaratu();
				}
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Hautatu erabiltzaile bat ezabatzeko.");
			}
		});
	}

	/**
	 * @param egoera
	 * @param nanField
	 * @param izenaField
	 * @param abizenaField
	 * @param postaField
	 * @param tel_zenbField
	 * @param passField
	 * @param kokapenaField
	 * @param lekuaField
	 * @param matrikulaField
	 * @param panel
	 */
	public void balidazioa(Egoera mota, JTextField nanField, JTextField izenaField, JTextField abizenaField, JTextField postaField, JTextField tel_zenbField, JTextField passField, JTextField kokapenaField, JTextField lekuaField, JTextField matrikulaField, JPanel panel)
	{
		boolean zuzena = false;

		while (!zuzena)
		{

			int result = JOptionPane.showConfirmDialog(this, panel, "Erabiltzaile berria", JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION)
			{
				break;
			}
			
			Border defaultBorder = UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border");

			// Bordeak defektuz jartzeko
			nanField.setBorder(defaultBorder);
			izenaField.setBorder(defaultBorder);
			abizenaField.setBorder(defaultBorder);
			postaField.setBorder(defaultBorder);
			tel_zenbField.setBorder(defaultBorder);
			passField.setBorder(defaultBorder);
			kokapenaField.setBorder(defaultBorder);
			lekuaField.setBorder(defaultBorder);
			matrikulaField.setBorder(defaultBorder);

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

			if (!check(nan, Mota.DNI))
			{
				nanField.setBorder(Gorria);
				error = true;
				errorMsg.append("NAN ondo sartu \n");
			}
			if (!check(izena, Mota.izena))
			{
				izenaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Izenan bakarrik letrak sartu \n");
			}
			if (!check(abizena, Mota.izena))
			{
				abizenaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Abizenan bakarrik letrak sartu \n");
			}
			if (!check(lekua, Mota.izena))
			{
				lekuaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Lan lekua bakarrik letrak sartu \n");
			}
			if (!check(kokapena, Mota.izena))
			{
				kokapenaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Lan lekua \n");
			}
			if (!check(matrikula, Mota.Matrikula))
			{
				matrikulaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Matrikula \n");
			}
			if (!check(telefonoa, Mota.telf))
			{
				tel_zenbField.setBorder(Gorria);
				error = true;
				errorMsg.append("Telefono zenbakia \n");
			}
			if (!check(posta, Mota.posta))
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
				if (mota == Egoera.Gehitu)
				{
					DB_Gidariak.addGidariak(nanField.getText(), izenaField.getText(), abizenaField.getText(), postaField.getText(), tel_zenbField.getText(), passField.getText(), kokapenaField.getText(), lekuaField.getText(), matrikulaField.getText());
				}
				else if (mota == Egoera.Editatu)
				{
					DB_Gidariak.editGidariak(nanField.getText(), izenaField.getText(), abizenaField.getText(), postaField.getText(), tel_zenbField.getText(), passField.getText(), kokapenaField.getText(), lekuaField.getText(), matrikulaField.getText());
				}

				try
				{

					gidariakBistaratu();
				}
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				zuzena = true;

			}
			catch (SQLException e1)
			{
				JOptionPane.showMessageDialog(this, e1.getMessage(), "Errorea: ", JOptionPane.ERROR_MESSAGE);
				continue;
			}

			break;
		}
	}

	// Gehitu erabiltzailea taulan
	private void gidariakBistaratu() throws SQLException
	{

		modelo.setRowCount(0);

		ResultSet rs = DB_Gidariak.getDatuak();

		while (rs.next())
		{
			String	NAN			= rs.getString("NAN");
			String	izena		= rs.getString("Izena");
			String	abizena		= rs.getString("Abizena");
			String	posta		= rs.getString("Posta");
			String	tel_zenb	= rs.getString("Tel_zenb");
			String	pass		= rs.getString("Pasahitza");
			String	kokapena	= rs.getString("Kokapena");
			String	lan_lekua	= rs.getString("Lan_Lekua");
			String	Matrikula	= rs.getString("Matrikula");

			modelo.addRow(new Object[]
			{
					NAN, izena, abizena, posta, tel_zenb, pass, kokapena, lan_lekua, Matrikula
			});
		}

	}

	// Bilatu erabiltzaileak izenaren edo abizenaren arabera
	private void filtrar()
	{
		String								bilatu	= bilatzailea.getText().toLowerCase();
		String								regex	= Pattern.quote(bilatu);				// karaketere
																							// bereziak
																							// kentzeko
		TableRowSorter<DefaultTableModel>	sorter	= new TableRowSorter<>(modelo);
		taula.setRowSorter(sorter);
		sorter.setRowFilter(RowFilter.regexFilter("(?i)" + bilatu));
	}

	public static boolean check(String testua, Mota Mota)
	{

		String regex = null;

		if (Mota == Mota.DNI)
			regex = "[0-9]{8}[A-Z]{1}";
		if (Mota == Mota.Matrikula)
			regex = "[0-9]{4}[A-Z]{3}";
		if (Mota == Mota.posta)
			regex = "^[\\w\\.-]+@[\\w\\.-]+\\.[a-zA-Z]{2,}$";
		if (Mota == Mota.telf)
			regex = "[0-9]{9}";
		if (Mota == Mota.izena)
			regex = "^(?i)([a-záéíóúñü]+(?:[\\s'\\-][a-záéíóúñü]+)*)$";
		Pattern	pattern	= Pattern.compile(regex);
		Matcher	matcher	= pattern.matcher(testua);

		return matcher.matches();
	}

}