package de.uni_mannheim.semantic.web;



import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class DBPedia {
	private static final String endpoint = "http://dbpedia.org/sparql";
	
	private static final String prefix = "PREFIX p: <http://dbpedia.org/property/>"+
			"PREFIX dbpedia: <http://dbpedia.org/resource/>"+
			"PREFIX category: <http://dbpedia.org/resource/Category:>"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
			"PREFIX skos: <http://www.w3.org/2004/02/skos/core#>"+
			"PREFIX geo: <http://www.georss.org/georss/>";
	
	public static ResultSet query(String strQuery) {
		Query q = QueryFactory.create(prefix + "\r\n" + strQuery);
		QueryExecution qexec =
		QueryExecutionFactory.sparqlService(endpoint, q);
		ResultSet RS = qexec.execSelect();
		
		return RS;
	}
	
	public static String checkTitleExists(String title) {
		ResultSet r = DBPedia.query("SELECT ?x WHERE { ?x rdfs:label \""+title+"\"@en } LIMIT 1");
		if(r.hasNext()) {
			QuerySolution s =  r.next();
			return s.get("x").toString();
		}
		
		return null;
	}
}
