package de.uni_mannheim.semantic.web.info;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.mysql.cj.jdbc.MysqlDataSource;

import de.uni_mannheim.semantic.web.crawl.Tuple;
import de.uni_mannheim.semantic.web.nlp.NGram;

public class DBPedia_MySQL {

	public static final String CATEGORY_TABLE = "Category";

	private static Connection _connection;

	static {
		try {
			MysqlDataSource dataSource = new MysqlDataSource();
			dataSource.setUser("root");
			dataSource.setPassword("");
			dataSource.setServerName("localhost");
			dataSource.setDatabaseName("test");

			_connection = dataSource.getConnection();
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

	public static void insertCategories(List<Tuple<String, String>> data) throws SQLException {
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

	public static List<String> findCategoriesForNGram(NGram n) {
		/*
		 * StringBuilder search = new StringBuilder("%");
		 * 
		 * for(Token t : n) { String tx = t.getText(); if(tx.endsWith("n")) tx =
		 * TextHelper.removeLast(tx);
		 * 
		 * else if(tx.endsWith("s")) tx = TextHelper.removeLast(tx);
		 * 
		 * search.append(tx+"%"); }
		 */

		List<String> results = new ArrayList<String>();

		try {
			PreparedStatement stmt = _connection.prepareStatement(
					"SELECT `search`,Levenshtein(?,`search`) AS dist FROM `category` ORDER BY dist ASC LIMIT 1");
			stmt.setString(1, n.getText());

			ResultSet s = stmt.executeQuery();

			while (s.next()) {
				results.add(s.getString(1));
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return results;
	}

}
