package datubasea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DB_Erabiltzaileak
{

	static Connection conn = DBGestorea.getConexion();

	public static ResultSet getDatuak() throws SQLException
	{

		String				sql_check	= "SELECT * FROM erabiltzailea";
		PreparedStatement	stmt		= conn.prepareStatement(sql_check);

		ResultSet			rs1			= stmt.executeQuery();

		return rs1;
	}

}