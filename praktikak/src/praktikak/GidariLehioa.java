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
	private Timer								historialTimer;					// Historial
																				// taularentzako
																				// timer-a
	private Timer								mainTableTimer;					// Taula
																				// nagusiarentzako
																				// timer-a
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
//		modelo = new DefaultTableModel(new Object[]
//		{
//				"NAN", "Izena", "Abizena", "Posta", "Telefono zenbakia", "Kokapena", "Lan lekua", "Matrikula"
//		}, 0);
		modelo = new DefaultTableModel(new Object[]
		{
				"NAN", "Izen abizena", "Posta", "Telefono zenbakia", "Kokapena", "Lan lekua", "Matrikula"
		}, 0);
		taula = new JTable(modelo);
		sorter = new TableRowSorter<>(modelo);
		taula.setRowSorter(sorter);
		sorter.setSortable(5, false); // No sortable for password column
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);
		taula.setDefaultEditor(Object.class, null);
		taula.setRowHeight(30);

		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER); // testua zentratzeko
		renderer.setBorder(new EmptyBorder(10, 10, 10, 10)); // padding-a jartzeko

		for (int i = 0; i < taula.getColumnCount(); i++)
		{
			taula.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		// 5 segundu pasatzerakoan eguneratzeko
		mainTableTimer = new Timer(5000, new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					gidariakBistaratu();
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(GidariLehioa.this, "Errorea gidarien datuak eguneratzean: " + ex.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
				}
			}

		});
		mainTableTimer.start(); // timer-a hasteko

		// Datu baseko gidarien datuak bistaratzeko
		try
		{
			gidariakBistaratu();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea gidarien datuak kargatzean: " + e.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
		}

		this.addWindowFocusListener(new WindowAdapter()
		{

			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				// lehio sagusi focus dagoenean eguneratzeko
				try
				{
					gidariakBistaratu();
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
				}

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

			@Override
			public void windowClosed(WindowEvent e)
			{
				// leihoa isterakoan timerrak gelditzeko
				if (mainTableTimer != null && mainTableTimer.isRunning())
				{
					mainTableTimer.stop();
				}
				if (historialTimer != null && historialTimer.isRunning())
				{
					historialTimer.stop();
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

					try (ResultSet rs = DB_Gidariak.getDatuakBidaiak(historialaNAN))
					{
						// Gidariaren historiala hutsik badago
						if (!rs.isBeforeFirst())
						{
							JOptionPane.showMessageDialog(GidariLehioa.this, "Ez dago bidai historiarik gidari honentzat.", "Informazioa", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					}
					catch (SQLException e)
					{
						e.printStackTrace();
						JOptionPane.showMessageDialog(GidariLehioa.this, "Errorea historialaren datuak kargatzean: " + e.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
						return;
					}

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
					bilatzaileHistoriala.setToolTipText("Bilatu...");
					topPanel.add(bilatzaileHistoriala, BorderLayout.CENTER);

					edukiontzia.add(topPanel, BorderLayout.NORTH);

					historialaModeloa = new DefaultTableModel(new Object[]
					{
							"Gidaria NAN", "Gidari Izena", "Erabiltzaile NAN", "Erabiltzaile Izena", "Data", "Hasiera", "Helmuga"
					}, 0);
					historialTaula = new JTable(historialaModeloa);
					historialaSorter = new TableRowSorter<>(historialaModeloa);
					historialTaula.setRowSorter(historialaSorter);
					historialTaula.setDefaultEditor(Object.class, null);
					historialTaula.setRowHeight(30);
					JScrollPane histScroll = new JScrollPane(historialTaula);
					edukiontzia.add(histScroll, BorderLayout.CENTER);

					DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
					renderer.setHorizontalAlignment(JLabel.CENTER); // testua zentratzeko
					renderer.setBorder(new EmptyBorder(10, 10, 10, 10)); // padding-a jartzeko

					for (int i = 0; i < historialTaula.getColumnCount(); i++)
					{
						historialTaula.getColumnModel().getColumn(i).setCellRenderer(renderer);
					}

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
						historialTimer.stop(); // timer bat martzan badago amaitzeko
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
								JOptionPane.showMessageDialog(historialLehioaAktiboa, "Errorea historialaren datuak eguneratzean: " + ex.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
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
			panel.add(new JLabel("Kokapena:"));
			panel.add(kokapenaField);
			panel.add(new JLabel("Lan lekua:"));
			panel.add(lekuaField);
			panel.add(new JLabel("Matrikula:"));
			panel.add(matrikulaField);

			balidazioa(Egoera.Gehitu, nanField, izenaField, abizenaField, postaField, tel_zenbField, kokapenaField, lekuaField, matrikulaField, panel);
		});

		// Editatu
		btnEditatu.addActionListener(e -> {
			int selectedRow = taula.getSelectedRow();
			if (selectedRow >= 0)
			{
				// filtroa badago aukeratutako lerroa modelora gehitzeko
				int		modelRow	= taula.convertRowIndexToModel(selectedRow);
				String	nanAukera	= (String) modelo.getValueAt(modelRow, 0);

				try (ResultSet rs = DB_Gidariak.getGidariaNAN(nanAukera))
				{
					if (rs.next())
					{
						String		nan				= rs.getString("NAN");
						String		izena			= rs.getString("Izena");
						String		abizena			= rs.getString("Abizena");
						String		posta			= rs.getString("Posta");
						String		tel_zenb		= rs.getString("Tel_zenb");
						String		kokapena		= rs.getString("Kokapena");
						String		lan_lekua		= rs.getString("Lan_Lekua");
						String		matrikula		= rs.getString("Matrikula");

						JTextField	nanField		= new JTextField(nan);
						JTextField	izenaField		= new JTextField(izena);
						JTextField	abizenaField	= new JTextField(abizena);
						JTextField	postaField		= new JTextField(posta);
						JTextField	tel_zenbField	= new JTextField(tel_zenb);
						JTextField	kokapenaField	= new JTextField(kokapena);
						JTextField	lekuaField		= new JTextField(lan_lekua);
						JTextField	matrikulaField	= new JTextField(matrikula);

						nanField.setEditable(false); // nan-a editatzeko aukera kentzeko

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
						panel.add(new JLabel("Kokapena:"));
						panel.add(kokapenaField);
						panel.add(new JLabel("Lan lekua:"));
						panel.add(lekuaField);
						panel.add(new JLabel("Matrikula:"));
						panel.add(matrikulaField);

						balidazioa(Egoera.Editatu, nanField, izenaField, abizenaField, postaField, tel_zenbField, kokapenaField, lekuaField, matrikulaField, panel);

					}
					else
					{
						JOptionPane.showMessageDialog(this, "Aukeratutako gidaria ez da aurkitu datu-basean. Agian ezabatu egin da.", "Errorea", JOptionPane.ERROR_MESSAGE);
						gidariakBistaratu(); // ez bada existitzen datu basea eguneratzeko
					}
				}
				catch (SQLException ex)
				{
					ex.printStackTrace();
					JOptionPane.showMessageDialog(this, "Errorea gidariaren datuak kargatzean: " + ex.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
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
				// filtroa badago aukeratutako lerroa modelora gehitzeko
				int		modelRow	= taula.convertRowIndexToModel(selectedRow);
				String	nan			= (String) modelo.getValueAt(modelRow, 0);

				int		confirm		= JOptionPane.showConfirmDialog(this, "Ziur zaude " + nan + " gidaria ezabatu nahi duzula?", "Ezabatu gidaria", JOptionPane.YES_NO_OPTION);

				if (confirm == JOptionPane.YES_OPTION)
				{
					try
					{
						DB_Gidariak.deleteGidaria(nan);
						JOptionPane.showMessageDialog(this, "Gidaria ondo ezabatu da.", "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
						gidariakBistaratu(); // datuak borratu ondoren taula eguneratzeko
					}
					catch (SQLException e1)
					{
						e1.printStackTrace();
						JOptionPane.showMessageDialog(this, "Errorea gidaria ezabatzean: " + e1.getMessage(), "Datu-base errorea", JOptionPane.ERROR_MESSAGE);
					}
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
	 * @param kokapenaField  Kokapena inputean sartutako testua pasatzeko
	 * @param lekuaField     Lan Lekua inputean sartutako testua pasatzeko
	 * @param matrikulaField Martikula inputean sartutako testua pasatzeko
	 * @param panel          panela pasatzeko
	 */
	public void balidazioa(Egoera mota, JTextField nanField, JTextField izenaField, JTextField abizenaField, JTextField postaField, JTextField tel_zenbField, JTextField kokapenaField, JTextField lekuaField, JTextField matrikulaField, JPanel panel)
	{
		boolean zuzena = false;

		while (!zuzena)
		{
			int result = JOptionPane.showConfirmDialog(this, panel, (mota == Egoera.Gehitu ? "Gidari berria" : "Editatu gidaria"), JOptionPane.OK_CANCEL_OPTION);

			if (result != JOptionPane.OK_OPTION)
			{
				break; // erabiltzaileak ezestatzen duenean
			}

			Border defaultBorder = UIManager.getLookAndFeel().getDefaults().getBorder("TextField.border");

			// Bordeak defektuz jartzeko
			nanField.setBorder(defaultBorder);
			izenaField.setBorder(defaultBorder);
			abizenaField.setBorder(defaultBorder);
			postaField.setBorder(defaultBorder);
			tel_zenbField.setBorder(defaultBorder);
			kokapenaField.setBorder(defaultBorder);
			lekuaField.setBorder(defaultBorder);
			matrikulaField.setBorder(defaultBorder);

			boolean			error		= false;
			StringBuilder	errorMsg	= new StringBuilder("Erroreak:\n");
			Border			Gorria		= BorderFactory.createLineBorder(Color.RED, 2);

			// DATUAK BALIDATZEKO
			String			nan			= nanField.getText().trim().toUpperCase();
			String			izena		= izenaField.getText().trim();
			String			abizena		= abizenaField.getText().trim();
			String			posta		= postaField.getText().trim().toLowerCase();
			String			telefonoa	= tel_zenbField.getText().trim();
			String			pasahitza	= nanField.getText().trim().toUpperCase();
			String			kokapena	= kokapenaField.getText().trim();
			String			lekua		= lekuaField.getText().trim();
			String			matrikula	= matrikulaField.getText().trim();

			if (!check(nan, Mota.DNI))
			{
				nanField.setBorder(Gorria);
				error = true;
				errorMsg.append("NAN ondo sartu (8 digitu + 1 letra) \n");
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
				errorMsg.append("Matrikula formatua ez da zuzena (4 digitu + 3 letra) \n");
			}
			if (!check(telefonoa, Mota.telf))
			{
				tel_zenbField.setBorder(Gorria);
				error = true;
				errorMsg.append("Telefono zenbakia ez da zuzena (9 digitu) \n");
			}
			if (!check(posta, Mota.posta))
			{
				postaField.setBorder(Gorria);
				error = true;
				errorMsg.append("Posta elektroniko formatua ez da zuzena \n");
			}

			if (error)
			{
				JOptionPane.showMessageDialog(this, errorMsg.toString(), "Errorea: ", JOptionPane.ERROR_MESSAGE);
				continue; // Buklea berriro hasteko
			}

			try
			{
				if (mota == Egoera.Gehitu)
				{
					DB_Gidariak.addGidariak(nan, izena, abizena, posta, telefonoa, pasahitza, kokapena, lekua, matrikula);
					JOptionPane.showMessageDialog(this, "Gidaria ondo gehitu da.", "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
				}
				else if (mota == Egoera.Editatu)
				{
					DB_Gidariak.editGidariak(nan, izena, abizena, posta, telefonoa, pasahitza, kokapena, lekua, matrikula);
					JOptionPane.showMessageDialog(this, "Gidaria ondo editatu da.", "Arrakasta", JOptionPane.INFORMATION_MESSAGE);
				}
				gidariakBistaratu(); // datuak sartu edo eguneratu odnoren eguneratzeko
				zuzena = true; // bukletik ateratzeko
			}
			catch (SQLException e1)
			{
				JOptionPane.showMessageDialog(this, "Datu-base errorea: " + e1.getMessage(), "Errorea: ", JOptionPane.ERROR_MESSAGE);
				// SQL-an eerrore bat egon bada berriro egiteko adibidez NAN bikoiztua
				continue;
			}
		}
	}

	/**
	 * @param  NAN          aukeratutako errenkadaren NAN pasatzeko
	 * @throws SQLException erroreak badaude saltatzeko
	 */
	private void gidarienBidaiakBistaratu(String NAN) throws SQLException
	{
		// aukera eta bista gordetzeko
		int	selectedRow		= historialTaula.getSelectedRow();
		int	firstVisibleRow	= historialTaula.getVisibleRect().y / historialTaula.getRowHeight();

		historialaModeloa.setRowCount(0); // datu berriak gehitu aurretik taula garbitzeko

		try (ResultSet rs = DB_Gidariak.getDatuakBidaiak(NAN))
		{
			while (rs.next())
			{
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
				String	data2				= data + " " + ordua;
				historialaModeloa.addRow(new Object[]
				{
						gidariaNan, gidIzena, erabNAN, erabIzena, data2, hasiera, helmuga
				});

			}
		}

		// aukera eta bista reiniziatzeko
		if (selectedRow != -1 && selectedRow < historialTaula.getRowCount())
		{
			historialTaula.setRowSelectionInterval(selectedRow, selectedRow);
			if (firstVisibleRow < historialTaula.getRowCount())
			{
				historialTaula.scrollRectToVisible(new Rectangle(0, firstVisibleRow * historialTaula.getRowHeight(), historialTaula.getWidth(), historialTaula.getRowHeight()));
			}
		}
	}

	/**
	 * Gidariak bistaratu eta taulan gehitzeko
	 *
	 * @throws SQLException
	 */
	private void gidariakBistaratu() throws SQLException
	{
		// aukera eta bista gordetzeko
		int	selectedRow		= taula.getSelectedRow();
		int	firstVisibleRow	= taula.getVisibleRect().y / taula.getRowHeight();

		modelo.setRowCount(0); // datu berriak gehitu aurretik taula garbitzeko

		try (ResultSet rs = DB_Gidariak.getDatuak())
		{
			while (rs.next())
			{
				String	NAN			= rs.getString("NAN");
				String	izena		= rs.getString("Izena");
				String	abizena		= rs.getString("Abizena");
				String	posta		= rs.getString("Posta");
				String	tel_zenb	= rs.getString("Tel_zenb");
				String	kokapena	= rs.getString("Kokapena");
				String	lan_lekua	= rs.getString("Lan_Lekua");
				String	Matrikula	= rs.getString("Matrikula");
				
				String izenAbizena = izena + " " + abizena;

				modelo.addRow(new Object[]
				{
						NAN, izenAbizena, posta, tel_zenb, kokapena, lan_lekua, Matrikula
				});
			}
		}

		// aukera eta bista reiniziatzeko
		if (selectedRow != -1 && selectedRow < taula.getRowCount())
		{
			taula.setRowSelectionInterval(selectedRow, selectedRow);
			if (firstVisibleRow < taula.getRowCount())
			{
				taula.scrollRectToVisible(new Rectangle(0, firstVisibleRow * taula.getRowHeight(), taula.getWidth(), taula.getRowHeight()));
			}
		}
		filtratu(); // filtrorenbat badago berriro aplikatzeko
	}

	/**
	 * Bilatzaile nagusia funtzionatzeko
	 */
	private void filtratu()
	{
		String									bilatu	= bilatzailea.getText().toLowerCase();
		// letra larria, xeheak eta Pattern.quote karakter bereziak ignoratzeko
		RowFilter<DefaultTableModel, Object>	rf		= RowFilter.regexFilter("(?i)" + Pattern.quote(bilatu));
		sorter.setRowFilter(rf);
	}

	/**
	 * Bigarren bilatzailea funtzionatzeko
	 */
	private void filtratu2()
	{
		String									bilatu	= bilatzaileHistoriala.getText().toLowerCase();
		RowFilter<DefaultTableModel, Object>	rf		= RowFilter.regexFilter("(?i)" + Pattern.quote(bilatu));
		historialaSorter.setRowFilter(rf);
	}

	/**
	 * @param  testua egiaztatu nahi duzun testua pasatzeko
	 * @param  Mota   egiaztatze testu mota (Mota.DNI, Mota.Matrikula, Mota.posta, Mota.telf,
	 *                Mota.izena)
	 * @return        egiaztapena betezen bada true bestela false
	 */
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