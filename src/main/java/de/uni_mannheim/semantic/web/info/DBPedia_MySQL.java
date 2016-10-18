package de.uni_mannheim.semantic.web.info;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import de.uni_mannheim.semantic.web.crawl.Tuple;

public class DBPedia_MySQL {

	public static final String CATEGORY_TABLE = "Category";

	private static Connection _connection;

	static {
		try {
			_connection = DriverManager.getConnection("jdbc:mysql://localhost/test?rewriteBatchedStatements=true");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void createTables() {

		String sqlCategoryTable = "CREATE TABLE IF NOT EXISTS " + CATEGORY_TABLE + " (\n"
				+ "	id integer PRIMARY KEY,\n" + "	name text NOT NULL,\n" + " search text NOT NULL \n" + ");";

		try {
			Statement stmt = _connection.createStatement();
			// create a new table

			stmt.execute(sqlCategoryTable);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void insertCategory(List<Tuple<String, String>> data) throws SQLException {
		String sql = "INSERT INTO " + CATEGORY_TABLE + "(name,search) VALUES (?,?)";

		// _connection.setAutoCommit(false);

		PreparedStatement statement = _connection.prepareStatement(sql);

		for (Tuple<String, String> t : data) {
			statement.setString(1, t.x);
			statement.setString(2, t.y);

			statement.addBatch();
		}

		statement.executeBatch();
	}

}
