package datubasea;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBGestorea
{

	// Konektatzeko datuak
	private static final String	DB				= "jdbc:mysql://10.23.26.119:3306/alaiktomugi?useSSL=false&serverTimezone=UTC";
	private static final String	ERABILTZAILEAK	= "arkaitz";
	private static final String	PASAHITZA		= "pvlbtnse";

	private static Connection	connection		= null;

	// Konektibitatea lortzeko metodoa
	public static Connection getConexion()
	{
		if (connection == null)
		{
			try
			{
				// Konektibitatea sortu ez badago
				connection = DriverManager.getConnection(DB, ERABILTZAILEAK, PASAHITZA);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
		}
		return connection;
	}

	// Konektibitatea itxi
	public static void itxiSesioa()
	{
		try
		{
			if (connection != null && !connection.isClosed())
			{
				connection.close();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Erabiltzailearen historialeko bidaiak lortzeko funtzioa.
	 * 
	 * @param  NAN          Erabiltzailearen NAN
	 * @return              ResultSet Bidaiaren historioko datuak
	 * @throws SQLException SQL errorea gertatzen bada
	 */
	public static ResultSet getHistorikoak(String NAN) throws SQLException
	{
		// Datu-basearekin konektatzea
		Connection	conn	= getConexion();
		// SQL kontsulta prestatzea
		String		sql		= "SELECT bh.Bidaia_id, E.nan, E.Izena, E.Abizena, bh.Gidari_nan, g.Izena," + " g.Abizena, bh.Data, bh.Hasiera_ordua, bh.Amaiera_ordua, Pertsona_kopurua," + " bh.Hasiera, bh.Helmuga  " + "FROM erabiltzailea E " + "JOIN bidai_historiala bh on E.NAN = bh.Erabiltzaile_nan "
				+ "JOIN gidaria g on g.NAN = bh.Gidari_nan " + "where Erabiltzaile_nan = ?;";
		System.out.println("SQL Query: " + sql); // Add this line
		System.out.println("NAN parameter value: " + NAN); // Add this line
		// PreparedStatement-a prestatzea
		PreparedStatement stmt = conn.prepareStatement(sql);
		stmt.setString(1, NAN); // NAN parametroa jartzen da kontsultan
		// Kontsulta exekutatzea eta emaitzak lortzea
		return stmt.executeQuery(); // ResultSet-a itzultzen da, non historialeko bidaiak dauden
	}

}
