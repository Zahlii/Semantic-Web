package de.uni_mannheim.semantic.web.crawl;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.info.DBPedia_MySQL;

public class YagoClassCrawler {

	public static void main(String[] args) throws IOException, SQLException {

		DBPedia_MySQL.createTables();

		for (int i = 35; i < 1000; i++) {

			String q = "SELECT DISTINCT ?type WHERE { \r\n" + "?x rdf:type ?type . \r\n"
					+ "FILTER(regex(str(?type),\"yago\")) \r\n" + "} \r\n" + "LIMIT 10000 \r\n" + "OFFSET "
					+ (i * 10000);

			ResultSet s = DBPedia.query(q);

			ArrayList<Tuple<String, String>> data = new ArrayList<Tuple<String, String>>();

			while (s.hasNext()) {
				QuerySolution x = s.next();
				String href = x.get("type").toString();
				String name = href.substring(href.lastIndexOf("/") + 1);
				String search = name.replace("Wikicat", "");
				// System.out.println(href);
				data.add(new Tuple<String, String>(name, search));
			}

			System.out.println(new Date().toString() + " | " + i + " Finished parsing");
			DBPedia_MySQL.insertCategories(data);
			System.out.println(new Date().toString() + " | " + i + " Finished Saving");
		}
	}

}
