package de.uni_mannheim.semantic.web.crawl.run_once;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.sqlite.Function;

import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;

public class DBPediaOntologyCrawler {

	public static final String CATEGORY_TABLE = "Category";
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
		String sqlClassTable = "CREATE TABLE IF NOT EXISTS " + CLASS_TABLE + " (\n" + "	id integer PRIMARY KEY,\n"
				+ "	name text NOT NULL,\n" + "	link text NOT NULL,\n" + " superclass integer \n" + ");";

		String sqlPropertyTable = "CREATE TABLE IF NOT EXISTS " + PROP_TABLE + " (\n" + "	id integer PRIMARY KEY,\n"
				+ "	name text NOT NULL,\n" + " class integer NOT NULL,\n" + "	label text NOT NULL,\n"
				+ "	domain integer NOT NULL,\n" + "	range text NOT NULL,\n" + "	description text,\n"
				+ " FOREIGN KEY(class) REFERENCES " + CLASS_TABLE + "(id)\n" + ");";

		String sqlCategoryTable = "CREATE TABLE IF NOT EXISTS " + CATEGORY_TABLE + " (\n"
				+ "	id integer PRIMARY KEY,\n" + "	name text NOT NULL,\n" + " search text NOT NULL \n" + ");";

		try {
			Statement stmt = _connection.createStatement();
			// create a new table
			stmt.execute(sqlClassTable);
			stmt.execute(sqlPropertyTable);
			stmt.execute(sqlCategoryTable);
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

			for (int i = 0; i < props.size(); i++) {
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

	public static void insertCategory(String fullname, String searchName) {
		String sql = "INSERT INTO " + CATEGORY_TABLE + "(name,search) VALUES(?,?)";

		try {
			PreparedStatement pstmt = _connection.prepareStatement(sql);
			pstmt.setString(1, fullname);
			pstmt.setString(2, searchName);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static int getForeignKey(String domainLink) {
		String query = "SELECT id " + "FROM " + CLASS_TABLE + " " + "WHERE link = '" + domainLink + "';";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				return rs.getInt("id");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean contains(Property p) {
		String query = "SELECT id FROM " + PROP_TABLE + " WHERE name = '" + p.getName() + "' AND label = '"
				+ p.getLabel()
				// + "' AND class = '" + p.getOntologyClass()
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

	public static ArrayList<OntologyClass> getOntologyClassByName(String name) {
		return getOntologyClassByName(name, 0.25);
	}
	
	public static ArrayList<OntologyClass> getOntologyClassByName(String name, Double threshold) {
		ArrayList<OntologyClass> classes = new ArrayList<>();

		String query = "SELECT * FROM " + CLASS_TABLE + " WHERE " + "((Levenshtein(name, '" + name
				+ "') * 1.0) / MIN(LENGTH(name), LENGTH('" + name + "'))) < " + threshold + ";";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				// if((double)(new
				// Levenshtein()).distance(rs.getString("name"),
				// name) / Math.min(name.length(),
				// rs.getString("name").length()) < 0.2)
				OntologyClass oc = new OntologyClass("dbo:" + rs.getString("name"), rs.getString("link"));
				classes.add(oc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return classes;
	}

	public static ArrayList<Property> getOntologyPropertyByName(String name) {
		return getOntologyPropertyByName(name, 0.3);
	}
	
	public static ArrayList<Property> getOntologyPropertyByName(String name, Double threshold) {
		ArrayList<Property> properties = new ArrayList<>();

		String query = "SELECT * FROM " + PROP_TABLE + " WHERE " + "((Levenshtein(name, '" + name
				+ "') * 1.0) / MIN(LENGTH(name), LENGTH('" + name + "'))) < " + threshold + ";";

		try {
			Statement stmt = _connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);

			while (rs.next()) {
				Property prop = new Property(rs.getString("name"), String.valueOf(rs.getInt("class")),
						rs.getString("label"), String.valueOf(rs.getInt("domain")), rs.getString("range"),
						rs.getString("description"));
				properties.add(prop);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return properties;
	}

	public static void printClassTable() {
		String query = "SELECT * FROM " + CLASS_TABLE;

		Statement stmt;
		try {
			stmt = _connection.createStatement();

			ResultSet rs = stmt.executeQuery(query);
			System.out.format("%-5s%-20s%-11s%-50s", "id", "name", "superclass", "link");
			System.out.println();
			while (rs.next()) {
				System.out.format("%-5d%-20s%-11s%-50s", rs.getInt("id"), rs.getString("name"), rs.getInt("superclass"),
						rs.getString("link"));
				System.out.println();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void printPropertyTable(boolean sortByName) {
		String query;
		if (sortByName) {
			query = "SELECT * FROM " + PROP_TABLE + " ORDER BY name ASC";
		} else {
			query = "SELECT * FROM " + PROP_TABLE;
		}

		Statement stmt;
		try {
			stmt = _connection.createStatement();

			ResultSet rs = stmt.executeQuery(query);

			System.out.format("%-5s%-30s%-6s%-30s%-5s%-20s%-20s", "id", "name", "class", "label", "domain", "range",
					"description");
			System.out.println();
			while (rs.next()) {
				System.out.format("%-5d%-30s%-6d%-40s%-5s%-50s%-50s", rs.getInt("id"), rs.getString("name"),
						rs.getInt("class"), rs.getString("label"), rs.getString("domain"), rs.getString("range"),
						rs.getString("description"));
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

	public static void resetDatabase() {
		dropTable(CLASS_TABLE);
		dropTable(PROP_TABLE);

		createTables();
	}

	public static void crawlIntoDB() {
		System.out.println("Start crawling...");
		ArrayList<OntologyClass> list = OntologyClassCrawler.crawlClasses();
		System.out.println("Inserting in DB...");
		for (int i = 0; i < list.size(); i++) {
			insertOntologyClass(list.get(i));
		}
		System.out.println("...inserting done.");
	}

	public static void printDB() {
		System.out.println(CLASS_TABLE + ": ");
		printClassTable();
		System.out.println();
		System.out.println(PROP_TABLE + ": ");
		printPropertyTable(true);
	}

	public static void main(String[] args) {

		// resetDatabase();

		// crawlIntoDB();
		// printDB();

	}

}
