package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.crawl.run_once.DBPediaOntologyCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.StanfordNLP;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import utils.Util;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nik on 26.10.2016.
 */
public class GiveMeParser extends GenericParser {
//    @Override
//    protected ArrayList<String> parseInternal() throws Exception {
//
//        List<LookupResult> results = _sentence.dbpediaCategory.findAll();
//
//
//        ArrayList<String> responses = new ArrayList<>();
//
//        for(LookupResult r : results) {
//            ResultSet s = DBPediaWrapper.query("SELECT * WHERE { ?p rdf:type yago:"+r.getResult()+" .}");
//            while(s.hasNext()) {
//                QuerySolution sol = s.next();
//                responses.add(sol.get("p").toString());
//            }
//        }
//
//        return responses;
//    }
	
	   @Override
	    protected ArrayList<String> parseInternal() throws Exception {
		   try{
		        Word adj1 = new Word("");
		        Word noun1 = new Word("");
		        Word noun2 = new Word("");
		        Word adj = new Word("");
		        //splitting sentence into words
		        for (int i = 0; i < _sentence.getWords().size(); i++) {    
		            if (_sentence.getWords().get(i).getPOSTag().matches("JJ")) {
		                if (adj1.getText().equals("")){
		                	adj1 = _sentence.getWords().get(i);
		                }
		            }
		            if (_sentence.getWords().get(i).getPOSTag().matches("NNP")) {
		            	if(noun2.getText().equals("")){
		            		noun2 = _sentence.getWords().get(i);
		            	}
		            }
		            if (_sentence.getWords().get(i).getPOSTag().matches("NNS?")) {
		                if (noun1.getText().equals("")){
		                	noun1 = _sentence.getWords().get(i);
		                }
		            }
		        }
		        
		        adj = (noun2.getText().equals("")) ? adj1 : noun2;
		        
		        //convert non-numeric numbers to numerics e.g. ten to 10
//		        if(!tmpNumber.matches("\\d+(\\s\\d*)*")){
//		        	tmpNumber = Util.replaceNumbers(tmpNumber);
//		        }
//		        Double number = null;
//		        try{
//		        	number = Double.parseDouble(tmpNumber);
//		        }catch(NumberFormatException e){
//		        	
//		        }
		        
		        System.out.println("Found: " + adj.getText() + "_JJ " + noun1.getText() + "_NN ");
		    	
		        //search for the expected type for the result
		        String type = null;
		        ArrayList<LookupResult> lookupResults = DBPediaWrapper.spotlightLookupSearch(noun1.getText());
		        
		        //if not found with spotlight, search in database
		        if(lookupResults.size() == 0){
		        	ArrayList<OntologyClass> list = DBPediaOntologyCrawler.getOntologyClassByName(StanfordNLP.getStem(noun1.getText()));
		        	if(list.size() > 0){
		        		type = getBestOntologyClass(list, StanfordNLP.getStem(noun1.getText())).getLink();
		        	}
		        }else{
		        	type = lookupResults.get(0).getResult();
		        }
		        
		        //check all query parameters for null
		        if(type == null){
		        	return new ArrayList<String>();
		        }
		        
		        //execute the query with the given parameters
		        ArrayList<String> result = DBPediaWrapper.buildGiveMeQuery(type.replaceAll(".*\\/", ""), adj.getText());
		        
				return prepareForReturn(result, new ArrayList<>());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return new ArrayList<>();
	    }
}
