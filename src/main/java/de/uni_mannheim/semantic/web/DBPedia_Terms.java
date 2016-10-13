package de.uni_mannheim.semantic.web;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.Function;

import Domain.OntologyClass;
import Domain.Property;

class Levenshtein extends Function {

	@Override
	protected void xFunc() throws SQLException {
		if (args() != 2) {
			throw new SQLException("Levenshtein(text1,text2): Invalid argument count. Requires 2, but found " + args());
		}

		String t1 = value_text(0);
		String t2 = value_text(1);
		result(computeLevenshteinDistance(t1, t2));
	}

	private static int minimum(int a, int b, int c) {
		return Math.min(Math.min(a, b), c);
	}

	public static int computeLevenshteinDistance(CharSequence lhs, CharSequence rhs) {
		int[][] distance = new int[lhs.length() + 1][rhs.length() + 1];

		for (int i = 0; i <= lhs.length(); i++)
			distance[i][0] = i;
		for (int j = 1; j <= rhs.length(); j++)
			distance[0][j] = j;

		for (int i = 1; i <= lhs.length(); i++)
			for (int j = 1; j <= rhs.length(); j++)
				distance[i][j] = minimum(distance[i - 1][j] + 1, distance[i][j - 1] + 1,
						distance[i - 1][j - 1] + ((lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1));

		return distance[lhs.length()][rhs.length()];
	}
}

public class DBPedia_Terms {

	public static final String CLASS_TABLE = "Ontology_Class";
	public static final String PROP_TABLE = "Property";

	private static Connection _connection;

	static {
		try {
			Class.forName("org.sqlite.JDBC");
			_connection = DriverManager.getConnection("jdbc:sqlite:DBPedia_Terms.db");
			Function.create(_connection, Levenshtein.class.getSimpleName(), new Levenshtein());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void getClasses() throws SQLException {
		Statement statement = _connection.createStatement();
		ResultSet rs = statement.executeQuery("select Levenshtein(\"Google\",\"Yahoo!\")");
		if (rs.next()) {
			System.out.println(rs.getString(1));
		}
	}

	public static void createTables() {

		// SQL statement for creating the Ontology Class table
		String sqlClassTable = "CREATE TABLE IF NOT EXISTS " + CLASS_TABLE + " (\n" 
				+ "	id integer PRIMARY KEY,\n"
				+ "	name text NOT NULL,\n" 
				+ "	link text NOT NULL,\n"
				+ " superclass integer \n"
				+ ");";

		String sqlPropertyTable = "CREATE TABLE IF NOT EXISTS " + PROP_TABLE 
				+ " (\n" + "	id integer PRIMARY KEY,\n"
				+ "	name text NOT NULL,\n" 
				+ " class integer NOT NULL,\n"
				+ "	label text NOT NULL,\n" 
				+ "	domain integer NOT NULL,\n"
				+ "	range text NOT NULL,\n" 
				+ "	description text,\n" 
				+ " FOREIGN KEY(class) REFERENCES " + CLASS_TABLE + "(id)\n" 
				+ ");";

		try {
			Statement stmt = _connection.createStatement();
			// create a new table
			stmt.execute(sqlClassTable);
			stmt.execute(sqlPropertyTable);
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void insertOntologyClass(OntologyClass oc) {
		String sql = "INSERT INTO " + CLASS_TABLE + "(name,link,superclass) VALUES(?,?,?)";

		try {
			PreparedStatement pstmt = _connection.prepareStatement(sql);
			pstmt.setString(1, oc.getName());
			pstmt.setString(2, oc.getLink());
			pstmt.setInt(3, getForeignKey(oc.getSuperclass()));
			pstmt.executeUpdate();
			
			ArrayList<Property> props = oc.getProperties();
			
			for(int i=0; i<props.size(); i++){
				Property p = props.get(i);
				insertProperty(p);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public static void insertProperty(Property p) {
		String sql = "INSERT INTO " + PROP_TABLE + "(name,class,label,domain,range,description) VALUES(?,?,?,?,?,?)";

		if (!contains(p)) {
			try {
				PreparedStatement pstmt = _connection.prepareStatement(sql);
				pstmt.setString(1, p.getName());
				pstmt.setInt(2, getForeignKey(p.getOntologyClass()));
				pstmt.setString(3, p.getLabel());
				pstmt.setInt(4, getForeignKey(p.getDomain()));
				pstmt.setString(5, p.getRange());
				pstmt.setString(6, p.getDescription());
				pstmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public static int getForeignKey(String domainLink) {
		String query = "SELECT id " + "FROM " + CLASS_TABLE + " " + "WHERE link = '" + domainLink + "';";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while(rs.next()){
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean contains(Property p) {
		String query = "SELECT id FROM " + PROP_TABLE + " WHERE name = '" + p.getName() 
		+ "' AND label = '" + p.getLabel() 
//		+ "' AND class = '" + p.getOntologyClass()
		+ "';";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static OntologyClass getOntologyClassByName(String name){
		String query = "SELECT * FROM " + CLASS_TABLE + " WHERE ((Levenshtein(name, '" + name
		+ "') * 1.0) / MIN(LENGTH(name), LENGTH('"+name+"'))) < 0.5" 
		+ ";";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while(rs.next()){
//				if((double)(new Levenshtein()).computeLevenshteinDistance(rs.getString("name"), name) / Math.min(name.length(), rs.getString("name").length()) < 0.2)
				System.out.println(rs.getString("name")+" "+name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void printClassTable() {
		String query = "SELECT * FROM " + CLASS_TABLE;

		Statement stmt;
		try {
			stmt = _connection.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			System.out.format("%-5s%-20s%-11s%-50s","id", "name", "superclass", "link");
			System.out.println();
			while (rs.next()) {
				System.out.format("%-5d%-20s%-11s%-50s",rs.getInt("id"), rs.getString("name"),  rs.getInt("superclass"), rs.getString("link"));
				System.out.println();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printPropertyTable(boolean sortByName) {
		String query;
		if(sortByName){
			query = "SELECT * FROM " + PROP_TABLE + " ORDER BY name ASC";
		}else{
			query = "SELECT * FROM " + PROP_TABLE;
		}

		Statement stmt;
		try {
			stmt = _connection.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			
			System.out.format("%-5s%-30s%-6s%-30s%-5s%-20s%-20s","id", "name", "class", "label", "domain", "range", "description");
			System.out.println();
			while (rs.next()) {
				System.out.format("%-5d%-30s%-6d%-40s%-5s%-50s%-50s",rs.getInt("id"), rs.getString("name"), rs.getInt("class"), 
						rs.getString("label"), rs.getString("domain"), rs.getString("range"), rs.getString("description"));
				System.out.println();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void dropTable(String table) {
		Statement stmt;
		String sql = "DROP TABLE IF EXISTS " + table;

		try {
			stmt = _connection.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void resetDatabase(){
		dropTable(CLASS_TABLE);
		dropTable(PROP_TABLE);

		createTables();
	}
	
	public static void crawlIntoDB(){
		System.out.println("Start crawling...");
		ArrayList<OntologyClass> list = ClassCrawler.crawlClasses();
		System.out.println("Inserting in DB...");
		for (int i = 0; i < list.size(); i++) {
			insertOntologyClass(list.get(i));
		}
		System.out.println("...inserting done.");
	}
	
	public static void printDB(){
		System.out.println(CLASS_TABLE + ": ");
		printClassTable();
		System.out.println();
		System.out.println(PROP_TABLE + ": ");
		printPropertyTable(true);
	}

	public static void main(String[] args) {

//		resetDatabase();

//		crawlIntoDB();
//		printDB();


	}

}
