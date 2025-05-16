package datubasea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class DB_Gidariak
{

	static Connection conn = DBGestorea.getConexion();

	public static void main(String[] args)
	{

	}

	public static void addGidariak(
			String nan, String izena, String abizena, String posta, String Tel_zenb, String pasahitza, String kokapena,
			String lan_lekua, String matrikula
	) throws SQLException
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

	public static ResultSet getDatuak() throws SQLException
	{

		String				sql_check	= "SELECT * FROM gidaria";
		PreparedStatement	stmt		= conn.prepareStatement(sql_check);

		ResultSet			rs			= stmt.executeQuery();

//	    String atributuak;
//	    while (rs.next());
//	    {
//	    	datuak.add(rs.getString(0));
//	    }

		return rs;
	}

}
