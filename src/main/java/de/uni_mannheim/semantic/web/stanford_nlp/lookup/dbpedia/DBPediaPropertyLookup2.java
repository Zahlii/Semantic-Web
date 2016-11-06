package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import java.util.ArrayList;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;

public class DBPediaPropertyLookup2 {

	
	public static ArrayList<String> fetchProperties(String resource){
        ResultSet s = DBPediaWrapper.query("SELECT ?p WHERE {\n" +
                "{<"+resource+"> ?p ?o.}\n" +
                "UNION {?o ?p <"+resource+"> .}\n" +
                "}");
        
        ArrayList<String> props = new ArrayList<>();
        
        while(s.hasNext()) {
            QuerySolution l = s.next();

            String k = l.get("p").toString();

            if(!props.contains(k)) {
                props.add(k);
            }
        }
        return props;
	}
	
	public static ArrayList<String> findProperties(String searchString, ArrayList<String> fetchedProperties){
		double threshold = 0.15d;
		ArrayList<String> results = new ArrayList<>();
		
		for(int i=0; i<fetchedProperties.size(); i++){
			double d = Levenshtein.normalized(fetchedProperties.get(i).replaceAll(".*\\/", ""), searchString);
			if(d<=threshold){
				results.add(fetchedProperties.get(i));
			}
		}
		
		return results;
	}
}
