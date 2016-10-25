package de.uni_mannheim.semantic.web.crawl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import de.uni_mannheim.semantic.web.stanford_nlp.Search;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.uni_mannheim.semantic.web.helpers.TextHelper;
import de.uni_mannheim.semantic.web.info.DBPedia_MySQL;
import org.apache.lucene.queryparser.classic.ParseException;

public class TTLReader {

	public static void main(String[] args) throws IOException, SQLException, ParseException {
		Model model = ModelFactory.createDefaultModel();
		File f = new File("D:\\Downloads\\yagoTypes.ttl");


		Search db = new Search();

		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

		// wikicat_Moths_of_New_Zealand => WikicatMothsOfNewZealand
		// wordnet_road_104096066 => Road104096066

		HashSet<String> items = new HashSet<String>();

		String l = null;

		int i = 0;

		while ((l = in.readLine()) != null) {
			i++;


			if (!l.contains("rdf:type"))
				continue;

			if (l.contains("wordnet_")) {
				l = l.replaceAll("wordnet_", "");
			}

			String[] parts = l.split("\\s+");
			String type = parts[2].replace("<", "").replaceAll(">", "");

			String[] qParts = type.split("_");

			StringBuilder term = new StringBuilder();

			for (String p : qParts) {
				p = TextHelper.capitalize(p);
				term.append(p).append(" ");
			}

			items.add(term.toString().trim());
		}

		System.out.println(items.size());

		ArrayList<Tuple<String, String>> data = new ArrayList<Tuple<String, String>>();

		for (String x : items) {
			//System.out.println(x);
			db.addTerm(x.replace("Wikicat", ""),x.replace(" ",""));
			//data.add(new Tuple<String, String>(x, x.replace("Wikicat", "")));
		}

		System.out.println(new Date().toString() + " | " + i + " Finished parsing");

		//db.close();

		db.search("Politician");

		//DBPedia_MySQL.insertCategories(data);
		System.out.println(new Date().toString() + " | " + i + " Finished saving");

		/*
		 * model.read(in,null,"TTL");
		 * 
		 * Query query = QueryFactory.
		 * create("SELECT ?type WHERE { ?x rdfs:type ?type } LIMIT 1");
		 * 
		 * // Execute the query and obtain results QueryExecution qe =
		 * QueryExecutionFactory.create(query, model); ResultSet results =
		 * qe.execSelect();
		 * 
		 * if(results.hasNext()) { QuerySolution s = results.next();
		 * System.out.println(s); }
		 */
	}
}
