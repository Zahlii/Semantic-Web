package de.uni_mannheim.semantic.web.info;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

import de.uni_mannheim.semantic.web.nlp.finders.DBNERFinderResult;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.uni_mannheim.semantic.web.domain.OntologyClass;
import de.uni_mannheim.semantic.web.domain.Property;
import de.uni_mannheim.semantic.web.helpers.TextHelper;

public class DBPedia {
	private static final String endpoint = "http://dbpedia.org/sparql";

	private static String prefix;

	static {
		try {
			prefix = TextHelper.readFile("prefixes.txt", Charset.forName("UTF8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ResultSet query(String strQuery) {
		String x = prefix + "\r\n" + strQuery;
		// System.out.println(x);
		Query q = QueryFactory.create(x);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q);
		ResultSet RS = qexec.execSelect();

		return RS;
	}

	public static String findRessourceByTitle(String title) {

		if (title.length() > 5 && title.endsWith("s"))
			title = TextHelper.removeLast(title);

		String url = "http://lookup.dbpedia.org/api/search.asmx/PrefixSearch?QueryClass=&MaxHits=1&QueryString="
				+ title;
		Document doc;
		try {
			doc = Jsoup.connect(url).timeout(20000).get();
			Elements s = doc.select("Result URI");

			if (s.size() > 1)
				return s.get(0).html();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public static DBNERFinderResult checkTitleExists(String title) {

		// we dont want categories, but we want possible redirection targets. we
		// don't want disambiguations either!
		String se = ("SELECT ?x ?y WHERE { \r\n" + "{\r\n" + "?y rdfs:label \"XXX\"@en .\r\n"
				+ "?y dbo:wikiPageRedirects ?x.\r\n" + "?y dbo:wikiPageID ?id.\r\n" + "} UNION {\r\n"
				+ "?x rdfs:label \"XXX\"@en .\r\n" + "?x dbo:wikiPageID ?id.\r\n"
				+ "OPTIONAL { ?x dbo:wikiPageDisambiguates ?dis . }\r\n" + "FILTER(!BOUND(?dis))\r\n" + "} \r\n"
				+ "FILTER(!regex(?x,\"Category\"))\r\n" + "\r\n" + "} LIMIT 1").replaceAll("XXX", title.trim());

		ResultSet r = DBPedia.query(se);
		if (r.hasNext()) {
			QuerySolution s = r.next();
			String x = s.get("x").toString();
			String y = s.contains("y") ? s.get("y").toString() : null;
			return new DBNERFinderResult(x, y);
		}

		return null;
	}

	public static ArrayList<String> checkPropertyExists(String obj, Property prop) {
		ArrayList<String> properties = new ArrayList<>();

		String se = ("PREFIX obj:<OBJECT> \r\n" + " SELECT ?prop \r\n" + "	WHERE{  \r\n"
		// + " obj: dbo:PROPERTY ?prop . \r\n"
				+ "		obj: dbp:PROPERTY ?prop . \r\n"
				// + " ganges: ?p dbr:India . \r\n"
				+ "}").replaceAll("OBJECT", obj).replaceAll("PROPERTY", prop.getName());

		ResultSet r = DBPedia.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("prop").toString();
			properties.add(p);
		}

		return properties;
	}
	
	public static ArrayList<String> getTypeOfResource(String resource){
		ArrayList<String> ret = new ArrayList<>();

		String se = ("select ?type where{<RESOURCE> a ?type.}\r\n").replaceAll("RESOURCE", resource);

		ResultSet r = DBPedia.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("type").toString();
			ret.add(p);
		}

		return ret;
	}

	public static ArrayList<String> checkClassRelationExists(String obj1, OntologyClass obj2) {
		ArrayList<String> relations = new ArrayList<>();

		String se = ("PREFIX obj: <OBJECT1> \r\n" + " SELECT ?p \r\n" + "	WHERE{  \r\n"
		// + " obj: dbp:country ?(PROPERTY) . \r\n"
				+ "	    obj: ?p <OBJECT2> . \r\n" + "}").replaceAll("OBJECT1", obj1).replaceAll("OBJECT2",
						obj2.getLink());
		// System.out.println(se);

		ResultSet r = DBPedia.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String rel = s.get("p").toString();
			relations.add(rel);
		}

		return relations;
	}
}
