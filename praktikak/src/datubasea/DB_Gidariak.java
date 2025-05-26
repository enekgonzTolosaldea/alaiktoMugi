package datubasea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DB_Gidariak
{

	static Connection conn = DBGestorea.getConexion();

	public static void addGidariak(String nan, String izena, String abizena, String posta, String Tel_zenb,
			String pasahitza, String kokapena, String lan_lekua, String matrikula) throws SQLException
	{
		String				sql_insert	= "INSERT INTO gidaria(NAN, Izena, Abizena, Posta, Tel_Zenb, Pasahitza, Kokapena, Lan_lekua, Matrikula) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement	stmt		= conn.prepareStatement(sql_insert);

		stmt.setString(1, nan);
		stmt.setString(2, izena);
		stmt.setString(3, abizena);
		stmt.setString(4, posta);
		stmt.setString(5, Tel_zenb);
		stmt.setString(6, pasahitza);
		stmt.setString(7, kokapena);
		stmt.setString(8, lan_lekua);
		stmt.setString(9, matrikula);

		stmt.executeUpdate();

	}

	/**
	 * 
	 * @param nan editatzeko
	 * @param izena editatzeko
	 * @param abizena editatzeko
	 * @param posta editatzeko
	 * @param Tel_zenb editatzeko
	 * @param pasahitza editatzeko
	 * @param kokapena editatzeko
	 * @param lan_lekua editatzeko
	 * @param matrikula editatzeko
	 * @throws SQLException Datu basearekin erroreak egon badira
	 */
	public static void editGidariak(String nan, String izena, String abizena, String posta, String Tel_zenb,
			String pasahitza, String kokapena, String lan_lekua, String matrikula) throws SQLException
	{
		String				sql_update	= "UPDATE gidaria SET Izena = ?, Abizena = ?, Posta = ?, Tel_zenb = ?, Pasahitza = ?, Kokapena = ?, Lan_lekua = ?, Matrikula = ? WHERE NAN = ?";
		PreparedStatement	stmt		= conn.prepareStatement(sql_update);

		stmt.setString(1, izena);
		stmt.setString(2, abizena);
		stmt.setString(3, posta);
		stmt.setString(4, Tel_zenb);
		stmt.setString(5, pasahitza);
		stmt.setString(6, kokapena);
		stmt.setString(7, lan_lekua);
		stmt.setString(8, matrikula);

		// WHERE
		stmt.setString(9, nan);

		stmt.executeUpdate();

	}

	/**
	 * 
	 * @param nan aukeratutako gidariaren dni pasatzeko
	 * @throws SQLException Datu basearekin erroreak egon badira
	 */
	public static void deleteGidaria(String nan) throws SQLException
	{
		String				delete_sql	= "DELETE FROM gidaria WHERE NAN = ?";
		PreparedStatement	stmt		= conn.prepareStatement(delete_sql);

		stmt.setString(1, nan);

		stmt.executeUpdate();

	}

	/**
	 * 
	 * @return sql kontsultaren bidez ateratako datuak pasatzeko
	 * @throws SQLException Datu basearekin erroreak egon badira
	 */
	public static ResultSet getDatuak() throws SQLException
	{

		String				sql_check	= "SELECT * FROM gidaria";
		PreparedStatement	stmt		= conn.prepareStatement(sql_check);

		ResultSet			rs1			= stmt.executeQuery();

		return rs1;
	}
	
	/**
	 * 
	 * @param nan aukeratutako gidariaren NAN-a
	 * @return sql kontsultaren bidez ateratako datuak pasatzeko
	 * @throws SQLException Datu basearekin erroreak egon badira
	 */
	public static ResultSet getGidariaNAN(String nan) throws SQLException
	{

		String				sql_check	= "SELECT * FROM gidaria WHERE NAN = ?";
		PreparedStatement	stmt		= conn.prepareStatement(sql_check);
		
		stmt.setString(1, nan);

		ResultSet			rs1			= stmt.executeQuery();

		return rs1;
	}
	
	/**
	 * 
	 * @param nan aukeratutako gidariaren NAN-a
	 * @return sql kontsultaren bidez ateratako datuak pasatzeko
	 * @throws SQLException Datu basearekin erroreak egon badira
	 */
	public static ResultSet getDatuakBidaiak(String nan) throws SQLException
	{

		String				sql_check	= "SELECT bh.Gidari_nan, bh.Data, bh.Hasiera_ordua, bh.Hasiera, bh.Helmuga, g.izena AS gidari_izena, g.Abizena AS gidari_abizena, "
										+ "e.NAN AS erabiltzaile_nan, e.Izena AS erabiltzaile_izena, e.Abizena AS erabiltzaile_abizena "
										+ "FROM bidai_historiala bh INNER JOIN gidaria g ON bh.Gidari_nan = g.NAN INNER JOIN erabiltzailea e ON bh.Erabiltzaile_nan = e.NAN WHERE bh.Gidari_nan = ? ";
		PreparedStatement	stmt		= conn.prepareStatement(sql_check);

		stmt.setString(1, nan);
		ResultSet			rs2			= stmt.executeQuery();
		return rs2;
	}

}
