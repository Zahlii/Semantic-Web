package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.jena.vocabulary.DB;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.crawl.run_once.DBPediaOntologyCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.StanfordNLP;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.NGramLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup2;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaResourceLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import utils.Util;

public class WhichParser extends GenericParser {

    public static HashMap<String, String> comparators = new HashMap<>();
    static{
    	comparators.put("more", ">");
    	comparators.put("greater", ">");
    	comparators.put("higher", ">");
    	comparators.put("larger", ">");
    	comparators.put("taller", ">");
    	comparators.put("bigger", ">");
    	comparators.put("thicker", ">");
    	
    	comparators.put("less", "<");
    	comparators.put("fewer", "<");
    	comparators.put("lower", "<");
    	comparators.put("smaller", "<");
    	comparators.put("shorter", "<");
    	comparators.put("thinner", "<");
    }
    
    public static HashMap<String, String> hints = new HashMap<>();
    static{
    	hints.put("inhabitants", "population");
    }
	
    @Override
    protected ArrayList<String> parseInternal() throws Exception {
        System.out.println("");
        Word adj1 = new Word("");
        Word noun1 = new Word("");
        Word adj2 = new Word("");
        Word noun2 = new Word("");
        Word verb = new Word("");
        

        for(int i=0; i< _sentence.getWords().size(); i++){
        	Word word = _sentence.getWords().get(i);
        	if(word.getPOSTag().matches("JJR") || word.getPOSTag().matches("RBR")){
        		return parseJJR();
        	}else if(word.getPOSTag().matches("JJS") || word.getPOSTag().matches("RBS")){
        		return parseJJS();
        	}
        }
        
        try {
            boolean afterVerb = false;
            
            //splitting sentence into words
            for (int i = 0; i < _sentence.getWords().size(); i++) {    
                if (_sentence.getWords().get(i).getPOSTag().matches("JJ")) {
                    if (adj1.getText().equals("") && !afterVerb){
                    	adj1 = _sentence.getWords().get(i);
                    }
                    if(afterVerb && adj2.getText().equals("")){
                    	adj2 = _sentence.getWords().get(i);
                    }
                }
                if (_sentence.getWords().get(i).getPOSTag().matches("NN.*")) {
                    if (noun1.getText().equals("") && !afterVerb){
                    	noun1 = _sentence.getWords().get(i);
                    }
                    if(afterVerb && noun2.getText().equals("")){
                    	noun2 = _sentence.getWords().get(i);
                    }
                }
                if (_sentence.getWords().get(i).getPOSTag().matches("VB.*")) {
                    verb = _sentence.getWords().get(i);
                    afterVerb = true;
                }
            }
            

            ArrayList<LookupResult> entities = new ArrayList<>();
            entities.addAll(_sentence.findEntities());
            
            String entity1 = null;
            if(!noun1.getText().equals("")){
	            for (int i = 0; i < entities.size(); i++) {
	            	if(entities.get(i).getSearchedTitle() == null) continue;
					if(entities.get(i).getSearchedTitle().contains(noun1.getText())){
						entity1 = entities.get(i).getSearchedTitle();
						break;
					}
				}
	            if(entity1 == null){
	            	entity1 = noun1.getText();
	            }
            }
            
            LookupResult entity2 = null; 
            if(!noun2.getText().equals("")){
	            for (int i = 0; i < entities.size(); i++) {
	            	if(entities.get(i).getSearchedTitle() == null) continue;
					if(entities.get(i).getSearchedTitle().contains(noun2.getText())){
						entity2 = entities.get(i);
						break;
					}
				}
            }
            
            if(entity2 == null)
            	return new ArrayList<>();
            
            System.out.println("Found: " + adj1.getText() + "_JJ " + entity1 + "_NN " + verb.getText() + "_VB " + adj2.getText() + "_JJ " + entity2.getSearchedTitle() + "_NN ");
            
//            LookupResult entity = _sentence.dbpediaResource.findOneIn(noun2.getText());

            ArrayList<String> res = new ArrayList<>();
            ArrayList<String> usedResults = new ArrayList<>();
            ArrayList<String> redirects = new ArrayList<>();
            ArrayList<String> e1Syns = new ArrayList<>();
            ArrayList<String> vbSyns = new ArrayList<>();
            usedResults.add(entity2.getResult());
            int counter = 0;

            while(res.size() == 0 && counter < 5){
            	counter++;
            	DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence, entity2.getResult());
	            
            	res.addAll(pl.findPropertyForName(verb.getText()));
	            res.addAll(pl.findPropertyForName(noun1.getText()));
	            
	            if(res.size() == 0){
	            	if(e1Syns.size() == 0)	e1Syns.addAll(SynonymCrawler.findSynonyms(new Word(entity1, "NN")));
	            	if(vbSyns.size() == 0)	vbSyns.addAll(SynonymCrawler.findSynonyms(verb));
	            
	            	for (int i = 0; i < e1Syns.size(); i++) {
	    	            res.addAll(pl.findPropertyForName(e1Syns.get(i)));
					}
	            	for (int i = 0; i < vbSyns.size(); i++) {
	    	            res.addAll(pl.findPropertyForName(vbSyns.get(i)));
					}
	            }
	            
	            if(res.size() == 0){
	            	redirects.addAll(pl.findPropertyForName("wikiPageRedirects"));
	            	
	            	for (int i = 0; i < usedResults.size(); i++) {
						if(redirects.contains(usedResults.get(i))){
							redirects.remove(usedResults.get(i));
						}
					}
	            	
	            	if(redirects.size() == 0){
	            		break;
	            	} else {
	            		entity2.setResult(redirects.get(0));
	            		usedResults.add(redirects.get(0));
	            		redirects.remove(0);
	            	}
	            }
            }
            
//            ArrayList<String> ontologies = new ArrayList<>();
//            ArrayList<String> properties = new ArrayList<>();
//
//            for (int i = 0; i < res.size(); i++) {
//                if (res.get(i).matches(".*property.*")) {
//                    properties.add(res.get(i));
//                } else if (res.get(i).matches(".*ontology.*")) {
//                    ontologies.add(res.get(i));
//                }
//            }
            
//            Property p = new Property(properties.get(0), null, null, null, null, null);

//            ArrayList<String> finalRes = DBPediaWrapper.checkPropertyExists(results.get(0).getResult(), p);


//            ArrayList<LookupResult> lookupResults = new ArrayList<>();
//            for (int i = 0; i < finalRes.size(); i++) {
//                lookupResults.addAll(DBPediaWrapper.spotlightLookupSearch(finalRes.get(i)));
//            }

//            ArrayList<String> finalList = new ArrayList<>();
//            for (int i = 0; i < lookupResults.size(); i++) {
//                ArrayList<String> types = DBPediaWrapper.getTypeOfResource(lookupResults.get(i).getResult());
//                for (int j = 0; j < types.size(); j++) {
//                    types.set(j, types.get(j).toLowerCase());
//                }
//
//                for (int j = 0; j < ontologies.size(); j++) {
//                    String ont = ontologies.get(j).toLowerCase();
//                    if (types.contains(ont) && !finalList.contains(lookupResults.get(i).getResult())) {
//                        finalList.add(lookupResults.get(i).getResult());
//                    }
//                }
//            }
//            return prepareForReturn(finalList, validTypes);

            ArrayList<String> validTypes = new ArrayList<>();
            String type1 = null;
            ArrayList<LookupResult> tmp = DBPediaWrapper.lookupKeywordSearch(entity1);
            if(tmp.size() > 0){
            	ArrayList<String> tmp2 = DBPediaWrapper.getTypeOfResource(tmp.get(0).getResult());

            	if(tmp2.size() > 0)
            		type1 = tmp2.get(0);
            }
        	if(type1 != null){
            	validTypes.add(type1);
            }
            
            return prepareForReturn(res, validTypes);

//		DBPediaPropertyLookup prop = new DBPediaPropertyLookup(_sentence, results.get(0).getResult());		
//		ArrayList<String> props = prop.findPropertyForName(verb.getStem());
//		ArrayList<String> props2 = prop.findPropertyForName(noun1.getStem());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * parses JJRs (more, higher,...)
     * @return
     */
    public ArrayList<String> parseJJR(){
        Word adj1 = new Word("");
        Word noun1 = new Word("");
        Word adj2 = new Word("");
        Word noun2 = new Word("");
        Word verb = new Word("");
        Word jjr = new Word("");
        String tmpNumber = "";
        
        boolean afterNumber = false;
        
        
        //splitting sentence into words
        for (int i = 0; i < _sentence.getWords().size(); i++) {    
        	if(_sentence.getWords().get(i).getPOSTag().matches("JJR") || _sentence.getWords().get(i).getPOSTag().matches("RBR")){
        		jjr = _sentence.getWords().get(i);
        	}
        	if(_sentence.getWords().get(i).getPOSTag().matches("CD")){
        		tmpNumber += _sentence.getWords().get(i).getText() + " ";
        		afterNumber = true;
        	}
        	
            if (_sentence.getWords().get(i).getPOSTag().matches("JJ")) {
                if (adj1.getText().equals("") && !afterNumber){
                	adj1 = _sentence.getWords().get(i);
                }
                if(afterNumber && adj2.getText().equals("")){
                	adj2 = _sentence.getWords().get(i);
                }
            }
            if (_sentence.getWords().get(i).getPOSTag().matches("NN.*")) {
                if (noun1.getText().equals("") && !afterNumber){
                	noun1 = _sentence.getWords().get(i);
                }
                if(afterNumber && noun2.getText().equals("")){
                	noun2 = _sentence.getWords().get(i);
                }
            }
            if (_sentence.getWords().get(i).getPOSTag().matches("VB.*")) {
                verb = _sentence.getWords().get(i);
            }
        }
        
        //convert non-numeric numbers to numerics e.g. ten to 10
        if(!tmpNumber.matches("\\d+(\\s\\d*)*")){
        	tmpNumber = Util.replaceNumbers(tmpNumber);
        }
        
        Double number = null;
        try{
        	number = Double.parseDouble(tmpNumber);
        }catch(NumberFormatException e){
        	
        }
        
        System.out.println("Found: " + adj1.getText() + "_JJ " + noun1.getText() + "_NN " + verb.getText() + "_VB " + number + "_CD " + adj2.getText() + "_JJ " + noun2.getText() + "_NN ");
    	
        //find hint for second noun
        if(hints.containsKey(noun2.getText())){
        	noun2 = new Word(hints.get(noun2.getText()));
        }
        
        //find property for the second noun
        //if there is an adjective before the noun, search for both in combination
        ArrayList<Property> props = new ArrayList<>();
        if(!adj2.getText().equals("")){
            props = DBPediaOntologyCrawler.getOntologyPropertyByName(adj2.getText() + noun2.getText());
        }
        
        if(props.size() == 0){
        	props = DBPediaOntologyCrawler.getOntologyPropertyByName(noun2.getText());
        }

        //find best property according to Levensthein
        Property prop = getBestProperty(props, noun2.getText());
        //get the mathematical sign for the comparator e.g. "more" to >
        String comparator = parseComparator(jjr);
        
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
        if(prop == null || comparator == null || number == null || type == null){
        	return new ArrayList<String>();
        }
        
        //execute the query with the given parameters
        ArrayList<String> result = DBPediaWrapper.buildJJRQuery(type.replaceAll(".*\\/", ""), "http://dbpedia.org/property/"+prop.getName(), comparator, number, adj1.getText());
        
        //if no results with prop as a property, try to find one with prop as an ontology
        if(result.size() == 0){
        	result = DBPediaWrapper.buildJJRQuery(type.replaceAll(".*\\/", ""), "http://dbpedia.org/ontology/"+prop.getName(), comparator, number, adj1.getText());
        }
             
		return result;
    }
    
    public static Property getBestProperty(ArrayList<Property> props, String search){
    	double best = Double.MAX_VALUE;
    	Property prop = null;
    	for(int i=0; i<props.size(); i++){
    		double d = Levenshtein.normalized(props.get(i).getName(), search);
    		if(d<best){
    			best = d;
    			prop = props.get(i);
    		}
    	}
    	return prop;
    }
    
    public ArrayList<String> parseJJS(){
    	
    	return new ArrayList<String>();
    }
    
    public String parseComparator(Word jjr){
    	if(jjr == null){
    		return null;
    	}else{
    		if(comparators.containsKey(jjr.getText())){
    			return comparators.get(jjr.getText());
    		}else{
    			return null;
    		}
    	}
    }

}
