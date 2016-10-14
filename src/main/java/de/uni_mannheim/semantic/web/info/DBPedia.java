package de.uni_mannheim.semantic.web.info;



import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

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
	
	public static String checkTitleExists(String title) {

		// Capitalize!
		String first = title.substring(0, 1);
		String rest = title.substring(1);
		title = first.toUpperCase() + rest;
		
		// we dont want categories, but we want possible redirection targets. we don't want disambiguations either!
		String se = ("SELECT ?x WHERE { \r\n" + 
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
			return s.get("x").toString();
		}
		
		return null;
	}
}
