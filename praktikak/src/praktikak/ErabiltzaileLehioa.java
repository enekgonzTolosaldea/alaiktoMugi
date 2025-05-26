package praktikak;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
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

/**
 * Erabiltzaileak kudeatzeko leiho nagusia. Leiho honek erabiltzaileen zerrenda
 * bistaratzen du eta haien historiala ikusteko aukera ematen du.
 */
public class ErabiltzaileLehioa extends JFrame
{

	private DefaultTableModel					modelo;												// Taularen datuak
																									// gordetzeko modelo
	private JTable								taula;												// Erabiltzaileen
																									// taula
	private JTextField							bilatzailea;										// Bilaketa
																									// testu-kutxa
	private TableRowSorter<DefaultTableModel>	sorter;												// Taularen
																									// iragazkia eta
																									// ordenatzailea
	private Timer								historialRefreshTimer;								// Taula
																									// automatikoki
																									// eguneratzeko
																									// timer-a

	// Historial leihoaren instantzia bakarra gordetzeko (patroi singleton)
	private static JFrame						historialLehioaInstance	= null;

	// CardLayout-eko txartelen izenak
	private static final String					HUTSIK_EGOERA_TXARTELA	= "HutsikEgoeraTxartela";
	private static final String					TAULA_BISTA_TXARTELA	= "TaulaBistaTxartela";

	/**
	 * Leiho nagusiaren eraikitzailea.
	 */
	public ErabiltzaileLehioa()
	{
		// Leihoaren propietateak konfiguratu
		setTitle("Erabiltzaileen Kudeaketa");
		setSize(800, 600);
		setResizable(false);
		setLocationRelativeTo(null); // Zentratu pantailan
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Leiho hau ixterakoan, ez aplikazioa itxi

		// Edukiontzi nagusia konfiguratu (BorderLayout)
		JPanel edukiontzia = new JPanel(new BorderLayout(10, 10));
		edukiontzia.setBorder(new EmptyBorder(10, 10, 10, 10)); // Kanpo-marjina
		setContentPane(edukiontzia);

		// Goiko panela (izenburua + bilaketa)
		JPanel	goikoPanel	= new JPanel(new BorderLayout(10, 10));
		JLabel	lbl			= new JLabel("Erabiltzaileak", JLabel.LEFT);
		lbl.setFont(new Font("Segoe UI", Font.BOLD, 20));
		goikoPanel.add(lbl, BorderLayout.WEST);

		// Bilaketa testu-kutxaren konfigurazioa
		bilatzailea = new JTextField();
		bilatzailea.setToolTipText("Bilatu NAN, izena edo abizena...");
		goikoPanel.add(bilatzailea, BorderLayout.CENTER);
		edukiontzia.add(goikoPanel, BorderLayout.NORTH);

		// Bilaketa testua aldatzean, iragazi taula
		bilatzailea.getDocument().addDocumentListener(new DocumentListener()
		{

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				filtratu();
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				filtratu();
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				filtratu();
			}

		});

		// Taula automatikoki eguneratzeko timer-a (5 segundoro)
		int				delay			= 5000;
		ActionListener	refreshTable	= new ActionListener()
										{

											@Override
											public void actionPerformed(ActionEvent e)
											{
												try
												{
													erabiltzaileakBistaratu();
												}
												catch (SQLException ex)
												{
													ex.printStackTrace();
												}
											}

										};
		historialRefreshTimer = new Timer(delay, refreshTable);
		historialRefreshTimer.start();

		// Taularen modelo eta konfigurazioa
		modelo	= new DefaultTableModel(new Object[][] {}, new String[] {
				"NAN", "Izena", "Posta", "Telefono zenbakia"
		});
		taula	= new JTable(modelo);
		JScrollPane scrollPane = new JScrollPane(taula);
		edukiontzia.add(scrollPane, BorderLayout.CENTER);

		taula.setDefaultEditor(Object.class, null); // Taula ez editagarria
		taula.setAutoCreateRowSorter(true); // Zutabeen izenetan klik egitean ordenatzeko

		// Taulako testua zentratu eta padding-a gehitu
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		renderer.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Zutabe guztiei aplikatu errendatzailea
		for (int i = 0; i < taula.getColumnCount(); i++)
		{
			taula.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		// Erabiltzaileen datuak kargatu
		try
		{
			erabiltzaileakBistaratu();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea erabiltzaileak kargatzean: " + e.getMessage(), "Errorea",
					JOptionPane.ERROR_MESSAGE);
		}

		// Taulan doble klik egitean, erabiltzailearen historiala ireki
		taula.addMouseListener(new java.awt.event.MouseAdapter()
		{

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt)
			{
				if (evt.getClickCount() == 2)
				{ // Doble klik
					int row = taula.getSelectedRow();
					if (row != -1)
					{ // Fila hautatu badu
						int		modeloLerroa	= taula.convertRowIndexToModel(row);			// Ordenatuta badago,
																								// modeloaren indizea
																								// lortu
						String	NAN				= modelo.getValueAt(modeloLerroa, 0).toString();
						ikusiHistoriala(NAN);
					}
				}
			}

		});

		// Beheko panela (botoientzat, hutsik oraingoz)
		JPanel botoiPanela = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
		edukiontzia.add(botoiPanela, BorderLayout.SOUTH);
	}

	/**
	 * Erabiltzaileen datuak kargatu taulan (datu-baseatik).
	 * 
	 * @throws SQLException Datuak atzitzerakoan errorea gertatzen bada
	 */
	private void erabiltzaileakBistaratu() throws SQLException
	{
		modelo.setRowCount(0); // Taula garbitu
		ResultSet rs = null;
		try
		{
			rs = DB_Erabiltzaileak.getDatuak(); // Datu-baseko kontsulta
			while (rs.next())
			{
				String izena = rs.getString("Izena") + " " + rs.getString("Abizena");
				modelo.addRow(new Object[] {
						rs.getString("NAN"), izena, rs.getString("Posta"), rs.getString("Tel_zenb")
				});
			}
		}
		finally
		{
			if (rs != null)
			{
				try
				{
					rs.close(); // Itxi ResultSet
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Taulako datuak iragazi bilaketa-testuaren arabera.
	 */
	private void filtratu()
	{
		String bilatu = bilatzailea.getText().trim();
		if (sorter == null)
		{
			sorter = new TableRowSorter<>(modelo);
			taula.setRowSorter(sorter);
		}
		if (bilatu.isEmpty())
		{
			sorter.setRowFilter(null); // Ez iragazi
		}
		else
		{
			// Maiuskulak/minuskulak ez kontuan hartu eta karaktere bereziak iragazi
			sorter.setRowFilter(RowFilter.regexFilter("(?i)\\Q" + bilatu + "\\E"));
		}
	}

	/**
	 * Erabiltzaile baten bidaia historiala bistaratzen du.
	 * 
	 * @param NAN Ikusi nahi den erabiltzailearen NAN
	 */
	private void ikusiHistoriala(String NAN)
	{
		// Egiaztatu ea historial leiho bat irekita dagoen jada
		if (historialLehioaInstance != null && historialLehioaInstance.isVisible())
		{
			JOptionPane.showMessageDialog(this, "Historial leiho bat irekita dago jada.", "Oharra",
					JOptionPane.WARNING_MESSAGE);
			historialLehioaInstance.toFront(); // Ekarri aurrera
			return;
		}

		// Egiaztatu ea erabiltzaileak bidaiarik duen
		boolean		hasTrips	= false;
		ResultSet	rsCheck		= null;
		try
		{
			rsCheck = DBGestorea.getHistorikoak(NAN);
			if (rsCheck != null && rsCheck.next())
			{
				hasTrips = true;
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Errorea historialeko datuak egiaztatzean: " + e.getMessage(),
					"Errorea", JOptionPane.ERROR_MESSAGE);
			return;
		}
		finally
		{
			if (rsCheck != null)
			{
				try
				{
					rsCheck.close();
				}
				catch (SQLException e)
				{
					e.printStackTrace();
				}
			}
		}

		if (!hasTrips)
		{
			JOptionPane.showMessageDialog(this, "Erabiltzaile honek ez du bidaia historialik erregistratuta.",
					"Ohartarazpena", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Historial leiho berria sortu
		JFrame historialLehioaBerria = new JFrame("Erabiltzailearen Historiala: " + NAN);
		historialLehioaBerria.setSize(1200, 800);
		historialLehioaBerria.setResizable(false);
		historialLehioaBerria.setLocationRelativeTo(this); // Leiho nagusiarekiko zentratu
		historialLehioaBerria.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// CardLayout erabiliz bi egoera: hutsik edo taula
		CardLayout	historialTxartelDiseinua	= new CardLayout();
		JPanel		historialTxartelPanela		= new JPanel(historialTxartelDiseinua);
		historialTxartelPanela.setBorder(new EmptyBorder(10, 10, 10, 10));
		historialLehioaBerria.setContentPane(historialTxartelPanela);

		// 1. Hutsik egoera (ez dago bidaiarik)
		JPanel	hutsikEgoeraPanela	= new JPanel(new GridBagLayout());
		JLabel	goiburuaEtiketa		= new JLabel("Ez dago bidaia historialik");
		goiburuaEtiketa.setFont(new Font("Segoe UI", Font.BOLD, 24));
		hutsikEgoeraPanela.add(goiburuaEtiketa);
		historialTxartelPanela.add(hutsikEgoeraPanela, HUTSIK_EGOERA_TXARTELA);

		// 2. Taula egoera (bidaiak daude)
		DefaultTableModel	historialModelo	= new DefaultTableModel(new Object[][] {}, new String[] {
				"Bidaia ID", "Erabiltzaile NAN", "Erabiltzaile izena", "Gidari NAN", "Gidari izena", "Data",
				"Hasiera_ordua", "Amaiera_ordua", "Pertsona kopurua", "Hasiera", "Helmuga"
		});

		JTable				historialTaula	= new JTable(historialModelo);
		historialTaula.setFillsViewportHeight(true);
		historialTaula.setDefaultEditor(Object.class, null); // Ez editagarria

		// Taulako testua zentratu
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		renderer.setBorder(new EmptyBorder(10, 10, 10, 10));

		for (int i = 0; i < historialTaula.getColumnCount(); i++)
		{
			historialTaula.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		// Historial taularen panela
		JPanel		taulaBistaPanela	= new JPanel(new BorderLayout(10, 10));
		JScrollPane	historialScrollPane	= new JScrollPane(historialTaula);
		taulaBistaPanela.add(historialScrollPane, BorderLayout.CENTER);

		// Goiko panela (izenburua + bilaketa)
		JPanel	historialGoikoPanel	= new JPanel(new BorderLayout(10, 10));
		JLabel	historialTitleLabel	= new JLabel("Bidaia Historiala (" + NAN + ")", JLabel.LEFT);
		historialTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
		historialGoikoPanel.add(historialTitleLabel, BorderLayout.WEST);
		JTextField historialBilatzailea = new JTextField();
		historialBilatzailea.setToolTipText("Bilatu...");
		historialGoikoPanel.add(historialBilatzailea, BorderLayout.CENTER);
		taulaBistaPanela.add(historialGoikoPanel, BorderLayout.NORTH);
		historialTxartelPanela.add(taulaBistaPanela, TAULA_BISTA_TXARTELA);

		// Iragazkia konfiguratu
		TableRowSorter<DefaultTableModel> historialSorter = new TableRowSorter<>(historialModelo);
		historialTaula.setRowSorter(historialSorter);

		// Bilaketa testu-kutxaren listener-a
		historialBilatzailea.getDocument().addDocumentListener(new DocumentListener()
		{

			@Override
			public void insertUpdate(DocumentEvent e)
			{
				filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
			}

			@Override
			public void removeUpdate(DocumentEvent e)
			{
				filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
			}

			@Override
			public void changedUpdate(DocumentEvent e)
			{
				filtratuHistoriala(historialBilatzailea.getText(), historialSorter);
			}

		});

		// Datuak eguneratzeko timer-a (10 segundoro)
		final Timer		leihoarenRefreshTimer		= new Timer(10000, null);
		ActionListener	datuakKargatuEtaEguneratu	= new ActionListener()
													{

														@Override
														public void actionPerformed(ActionEvent e)
														{
															new SwingWorker<List<Object[]>, Void>()
															{

																@Override
																protected List<Object[]> doInBackground()
																		throws Exception
																{
																	List<Object[]>	data	= new ArrayList<>();
																	ResultSet		rs		= null;
																	try
																	{
																		rs = DBGestorea.getHistorikoak(NAN);
																		while (rs != null && rs.next())
																		{
																			// Datuak eratu
																			int		bidaia				= rs
																					.getInt("bh.Bidaia_id");
																			String	nan					= rs
																					.getString("E.nan");
																			String	izena				= rs
																					.getString("e.izena") + " "
																					+ rs.getString("e.abizena");
																			String	gidariNan			= rs
																					.getString("bh.Gidari_nan");
																			String	gidariIzena			= rs
																					.getString("g.izena") + " "
																					+ rs.getString("g.abizena");
																			Date	data2				= rs
																					.getDate("bh.data");
																			Time	hasiera_ordua		= rs
																					.getTime("bh.hasiera_ordua");
																			Time	amaiera_ordua		= rs
																					.getTime("bh.amaiera_ordua");
																			int		pertsona_kopurua	= rs
																					.getInt("bh.pertsona_kopurua");
																			String	hasiera				= rs
																					.getString("bh.hasiera");
																			String	helmuga				= rs
																					.getString("bh.helmuga");

																			data.add(new Object[] {
																					bidaia, nan, izena, gidariNan,
																					gidariIzena, data2, hasiera_ordua,
																					amaiera_ordua, pertsona_kopurua,
																					hasiera, helmuga
																			});
																		}
																	}
																	finally
																	{
																		if (rs != null)
																		{
																			try
																			{
																				rs.close();
																			}
																			catch (SQLException ex)
																			{
																				ex.printStackTrace();
																			}
																		}
																	}
																	return data;
																}

																@Override
																protected void done()
																{
																	try
																	{
																		List<Object[]> newData = get();
																		if (newData.isEmpty())
																		{
																			// Ez dago bidaiarik: hutsik egoera erakutsi
																			historialTxartelDiseinua.show(
																					historialTxartelPanela,
																					HUTSIK_EGOERA_TXARTELA);
																		}
																		else
																		{
																			// Bidaiak daude: taula eguneratu
																			historialModelo.setRowCount(0);
																			for (Object[] row : newData)
																			{
																				historialModelo.addRow(row);
																			}
																			historialTxartelDiseinua.show(
																					historialTxartelPanela,
																					TAULA_BISTA_TXARTELA);
																		}
																	}
																	catch (InterruptedException | ExecutionException ex)
																	{
																		ex.printStackTrace();
																		JOptionPane.showMessageDialog(
																				historialLehioaBerria,
																				"Errorea historialeko datuak eguneratzean.",
																				"Errorea", JOptionPane.ERROR_MESSAGE);
																		historialTxartelDiseinua.show(
																				historialTxartelPanela,
																				HUTSIK_EGOERA_TXARTELA);
																	}
																}

															}.execute();
														}

													};

		leihoarenRefreshTimer.addActionListener(datuakKargatuEtaEguneratu);
		datuakKargatuEtaEguneratu.actionPerformed(null); // Lehenengo karga egin

		// Gorde instantzia static aldagaian
		historialLehioaInstance = historialLehioaBerria;

		// Leihoaren eventuak kudeatu
		historialLehioaBerria.addWindowListener(new WindowAdapter()
		{

			@Override
			public void windowOpened(WindowEvent e)
			{
				if (!leihoarenRefreshTimer.isRunning())
				{
					leihoarenRefreshTimer.start(); // Timer-a aktibatu
				}
			}

			@Override
			public void windowClosed(WindowEvent e)
			{
				leihoarenRefreshTimer.stop(); // Timer-a gelditu
				historialLehioaInstance = null; // Instantzia ezabatu
			}

		});

		historialLehioaBerria.setVisible(true); // Leihoa erakutsi
	}

	/**
	 * Historial taulako datuak iragazi bilaketa-testuaren arabera.
	 * 
	 * @param text   Bilatzeko testua
	 * @param sorter Taularen iragazkia
	 */
	private void filtratuHistoriala(String text, TableRowSorter<DefaultTableModel> sorter)
	{
		if (text.trim().isEmpty())
		{
			sorter.setRowFilter(null); // Ez iragazi
		}
		else
		{
			// Maiuskulak/minuskulak ez kontuan hartu eta karaktere bereziak iragazi
			sorter.setRowFilter(RowFilter.regexFilter("(?i)\\Q" + text + "\\E"));
		}
	}


}