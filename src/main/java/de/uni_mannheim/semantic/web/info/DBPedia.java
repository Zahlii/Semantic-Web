package de.uni_mannheim.semantic.web.info;



import java.io.IOException;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.uni_mannheim.semantic.web.crawl.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.Property;
import de.uni_mannheim.semantic.web.nlp.helpers.TextHelper;

public class DBPedia {
	private static final String endpoint = "http://dbpedia.org/sparql";
	
	private static final String prefix = "PREFIX owl: <http://www.w3.org/2002/07/owl#>\r\n" + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\r\n" + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\r\n" + 
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"PREFIX foaf: <http://xmlns.com/foaf/0.1/>\r\n" + 
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>\r\n" + 
			"PREFIX : <http://dbpedia.org/resource/>\r\n" + 
			"PREFIX dbpedia2: <http://dbpedia.org/property/>\r\n" + 
			"PREFIX dbpedia: <http://dbpedia.org/>\r\n" + 
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>" + 
			"PREFIX dbo: <http://dbpedia.org/ontology/>\r\n" + 
			"PREFIX dbp: <http://dbpedia.org/property/>";
	
	public static ResultSet query(String strQuery) {
		Query q = QueryFactory.create(prefix + "\r\n" + strQuery);
		QueryExecution qexec =
		QueryExecutionFactory.sparqlService(endpoint, q);
		ResultSet RS = qexec.execSelect();
		
		return RS;
	}
	
	public static String findRessourceByTitle(String title) {
		
		if(title.length()>5 && TextHelper.endsWith(title,"s"))
			title = TextHelper.removeLast(title);
		
		String url = "http://lookup.dbpedia.org/api/search.asmx/PrefixSearch?QueryClass=&MaxHits=1&QueryString=" + title;
		Document doc;
		try {
			doc = Jsoup.connect(url).get();
			Elements s = doc.select("Result URI");
			
			if(s.size()>1)
				return s.get(0).html();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
		return null;
	}
	public static DBLookupResult checkTitleExists(String title) {

		// Capitalize!
		String first = title.substring(0, 1);
		String rest = title.substring(1);
		title = first.toUpperCase() + rest;
		
		// we dont want categories, but we want possible redirection targets. we don't want disambiguations either!
		String se = ("SELECT ?x ?y WHERE { \r\n" + 
				"{\r\n" + 
				"?y rdfs:label \"XXX\"@en .\r\n" + 
				"?y dbo:wikiPageRedirects ?x.\r\n" + 
				"?y dbo:wikiPageID ?id.\r\n" + 
				"} UNION {\r\n" + 
				"?x rdfs:label \"XXX\"@en .\r\n" + 
				"?x dbo:wikiPageID ?id.\r\n" + 
				"OPTIONAL { ?x dbo:wikiPageDisambiguates ?dis . }\r\n" + 
				"FILTER(!BOUND(?dis))\r\n" + 
				"} \r\n" + 
				"FILTER(!regex(?x,\"Category\"))\r\n" + 
				"\r\n" + 
				"} LIMIT 1").replaceAll("XXX", title.trim());

		ResultSet r = DBPedia.query(se);
		if(r.hasNext()) {
			QuerySolution s =  r.next();
			String x = s.get("x").toString();
			String y = s.contains("y") ? s.get("y").toString() : null;
			return new DBLookupResult(x,y);
		}
		
		return null;
	}
	
	public static ArrayList<String> checkPropertyExists(String obj, Property prop){
		ArrayList<String> properties = new ArrayList<>();
		
		String se = ("PREFIX obj:<OBJECT> \r\n"
				+ " SELECT ?prop \r\n"
				+ "	WHERE{  \r\n"
//				+ "		obj: dbo:PROPERTY ?prop . \r\n"
				+ "		obj: dbp:PROPERTY ?prop . \r\n"
//				+ "	    ganges: ?p dbr:India . \r\n"
				+ "}")
				.replaceAll("OBJECT", obj)
				.replaceAll("PROPERTY", prop.getName());

		ResultSet r = DBPedia.query(se);
		while(r.hasNext()) {
			QuerySolution s =  r.next();
			String p = s.get("prop").toString();
			properties.add(p);
		}
		
		return properties;
	}
	
	public static ArrayList<String> checkClassRelationExists(String obj1, OntologyClass obj2){
		ArrayList<String> relations = new ArrayList<>();

		String se = ("PREFIX obj: <OBJECT1> \r\n"
				+ " SELECT ?p \r\n"
				+ "	WHERE{  \r\n"
//				+ "		obj: dbp:country ?(PROPERTY) . \r\n"
				+ "	    obj: ?p <OBJECT2> . \r\n"
				+ "}")
				.replaceAll("OBJECT1", obj1)
				.replaceAll("OBJECT2", obj2.getLink());
//		System.out.println(se);
		
		ResultSet r = DBPedia.query(se);
		while(r.hasNext()) {
			QuerySolution s =  r.next();
			String rel = s.get("p").toString();
			relations.add(rel);
		}
		
		return relations;
	}
}
