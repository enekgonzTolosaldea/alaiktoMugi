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
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GidariLehioa extends JFrame
{

	private DefaultTableModel					modelo;
	private DefaultTableModel					historialaModeloa;
	private JTable								taula;
	private JTable								historialTaula;
	private JTextField							bilatzailea;
	private JTextField							bilatzaileHistoriala;
	private TableRowSorter<DefaultTableModel>	sorter;
	private TableRowSorter<DefaultTableModel>	historialaSorter;
	private Timer								historialTimer;					// Timer único para
																				// refresco
	private String								historialaNAN			= null;
	private JFrame								historialLehioaAktiboa	= null;

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
		sorter = new TableRowSorter<>(modelo);
		taula.setRowSorter(sorter);
		sorter.setSortable(5, false);
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);
		taula.setDefaultEditor(Object.class, null);
		taula.setRowHeight(30);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER); // testua zentratzeko
		renderer.setBorder(new EmptyBorder(10, 10, 10, 10)); // Añade padding interno

		for (int i = 0; i < taula.getColumnCount(); i++)
		{
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

		this.addWindowFocusListener(new WindowAdapter()
		{

			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				if (historialaNAN != null && historialaModeloa != null)
				{
					try
					{
						gidarienBidaiakBistaratu(historialaNAN);
					}
					catch (SQLException ex)
					{
						ex.printStackTrace();
					}
				}
			}

		});

		taula.addMouseListener(new MouseAdapter()
		{

			@Override
			public void mouseClicked(MouseEvent evt)
			{
				if (evt.getClickCount() == 2)
				{
					if (historialLehioaAktiboa != null && historialLehioaAktiboa.isDisplayable())
					{
						JOptionPane.showMessageDialog(GidariLehioa.this, "Dagoeneko historialaren leiho bat irekita dago.", "Abisua", JOptionPane.WARNING_MESSAGE);
						return;
					}
					// bi aldiz clickatzerakoan
					int row = taula.getSelectedRow();

					if (row < 0)
						return;
					int modeloRow = taula.convertRowIndexToModel(row);
					historialaNAN = modelo.getValueAt(modeloRow, 0).toString();
//						System.out.println(NAN);

					historialLehioaAktiboa = new JFrame("Erabiltzailearen Historiala (" + historialaNAN + ")");
					historialLehioaAktiboa.setSize(800, 500);
					historialLehioaAktiboa.setLocationRelativeTo(null);
					historialLehioaAktiboa.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

					historialLehioaAktiboa.addWindowListener(new WindowAdapter()
					{

						@Override
						public void windowClosed(WindowEvent e)
						{
							if (historialTimer != null)
							{
								historialTimer.stop();
							}
							historialLehioaAktiboa = null;
						}

					});

					JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
					edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10));
					historialLehioaAktiboa.setContentPane(edukiontzia);

					// Panel superior con título y campo de búsqueda
					JPanel	topPanel	= new JPanel(new BorderLayout(10, 10));
					JLabel	lbl			= new JLabel("Bidaia Historiala", JLabel.LEFT);
					lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
					topPanel.add(lbl, BorderLayout.WEST);

					bilatzaileHistoriala = new JTextField();
					bilatzaileHistoriala.setToolTipText("Bilatu izena edo abizena...");
					topPanel.add(bilatzaileHistoriala, BorderLayout.CENTER);

					edukiontzia.add(topPanel, BorderLayout.NORTH);

					historialaModeloa = new DefaultTableModel(new Object[]
					{
							"Gidaria NAN", "Gidari Izena", "Erabiltzaile NAN", "Erabiltzaile Izena", "Data", "Ordua", "Hasiera", "Helmuga"
					}, 0);
					historialTaula = new JTable(historialaModeloa);
					historialaSorter = new TableRowSorter<>(historialaModeloa);
					historialTaula.setRowSorter(historialaSorter);
					historialTaula.setDefaultEditor(Object.class, null);
					historialTaula.setRowHeight(30);
					JScrollPane histScroll = new JScrollPane(historialTaula);
					edukiontzia.add(histScroll, BorderLayout.CENTER);

					try
					{
						gidarienBidaiakBistaratu(historialaNAN);
					}
					catch (SQLException e)
					{
						e.printStackTrace();
					}

					bilatzaileHistoriala.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
					{

						public void insertUpdate(javax.swing.event.DocumentEvent e)
						{
							filtratu2();
						}

						public void removeUpdate(javax.swing.event.DocumentEvent e)
						{
							filtratu2();
						}

						public void changedUpdate(javax.swing.event.DocumentEvent e)
						{
							filtratu2();
						}

					});

					historialLehioaAktiboa.setVisible(true);

					if (historialTimer != null && historialTimer.isRunning())
					{
						historialTimer.stop(); // Detener si ya había un Timer corriendo
					}

					historialTimer = new Timer(10000, new ActionListener()
					{

						public void actionPerformed(ActionEvent evt)
						{
							try
							{
								gidarienBidaiakBistaratu(historialaNAN);
							}
							catch (SQLException ex)
							{
								ex.printStackTrace();
							}
						}

					});
					historialTimer.start();
				}
			}

		});
		// Botoiak behean
		JPanel	botoiPanela	= new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		JButton	btnGehitu	= new JButton("➕ Gehitu");
		JButton	btnEditatu	= new JButton("✏️ Editatu");
		JButton	btnEzabatu	= new JButton("❌ Ezabatu");

		botoiPanela.add(btnGehitu);
		botoiPanela.add(btnEditatu);
		botoiPanela.add(btnEzabatu);
		edukiontzia.add(botoiPanela, BorderLayout.SOUTH);

		// Bilaketa funtzionalitatea
		bilatzailea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener()
		{

			public void insertUpdate(javax.swing.event.DocumentEvent e)
			{
				filtratu();
			}

			public void removeUpdate(javax.swing.event.DocumentEvent e)
			{
				filtratu();
			}

			public void changedUpdate(javax.swing.event.DocumentEvent e)
			{
				filtratu();
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
	 * @param egoera         Egoera pasatzeko(Egoera.Gehitu, Egoera.Ezabatu)
	 * @param nanField       Nan inputean sartutako testua pasatzeko
	 * @param izenaField     Izena inputean sartutako testua pasatzeko
	 * @param abizenaField   Abziena inputean sartutako testua pasatzeko
	 * @param postaField     Posta inputean sartutako testua pasatzeko
	 * @param tel_zenbField  Telefono zenbakia inputean sartutako testua pasatzeko
	 * @param passField      Pasahitza inputean sartutako testua pasatzeko
	 * @param kokapenaField  Kokapena inputean sartutako testua pasatzeko
	 * @param lekuaField     Lan Lekua inputean sartutako testua pasatzeko
	 * @param matrikulaField Martikula inputean sartutako testua pasatzeko
	 * @param panel          panela pasatzeko
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
				errorMsg.append("Kokapena ondo sartu \n");
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

	/**
	 * @param  NAN          aukeratutako errenkadaren NAN pasatzeko
	 * @throws SQLException erroreak badaude saltatzeko
	 */
	private void gidarienBidaiakBistaratu(String NAN) throws SQLException
	{
		historialaModeloa.setRowCount(0);

		ResultSet rs = DB_Gidariak.getDatuakBidaiak(NAN);
		while (rs.next())
		{
			;

			String	gidariaNan			= rs.getString("Gidari_nan");
			String	erabNAN				= rs.getString("erabiltzaile_nan");
			Date	data				= rs.getDate("Data");
			Time	ordua				= rs.getTime("hasiera_ordua");
			String	hasiera				= rs.getString("hasiera");
			String	helmuga				= rs.getString("helmuga");
			String	gidariIzena			= rs.getString("gidari_izena");
			String	gidariAbizena		= rs.getString("gidari_abizena");
			String	erabiltzaileIzena	= rs.getString("erabiltzaile_izena");
			String	erabiltzaileAbizena	= rs.getString("erabiltzaile_abizena");

			String	erabIzena			= erabiltzaileIzena + " " + erabiltzaileAbizena;
			String	gidIzena			= gidariIzena + " " + gidariAbizena;
			historialaModeloa.addRow(new Object[]
			{
					gidariaNan, gidIzena, erabNAN, erabIzena, data, ordua, hasiera, helmuga
			});
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
	private void filtratu()
	{
		String									bilatu	= bilatzailea.getText().toLowerCase();
		RowFilter<DefaultTableModel, Object>	rf		= RowFilter.regexFilter("(?i)" + Pattern.quote(bilatu));
		sorter.setRowFilter(rf);
	}

	private void filtratu2()
	{
		String									bilatu	= bilatzaileHistoriala.getText().toLowerCase();
		RowFilter<DefaultTableModel, Object>	rf		= RowFilter.regexFilter("(?i)" + Pattern.quote(bilatu));
		historialaSorter.setRowFilter(rf);
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