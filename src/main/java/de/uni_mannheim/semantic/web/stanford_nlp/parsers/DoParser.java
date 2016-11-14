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

public class DoParser extends GenericParser {

	private ArrayList<String> returnFalse(){
		ArrayList<String> ret = new ArrayList<>();
		ret.add("false");
		return ret;
	}
	
	private ArrayList<String> returnTrue(){
		ArrayList<String> ret = new ArrayList<>();
		ret.add("true");
		return ret;
	}
	
    @Override
    protected ArrayList<String> parseInternal() throws Exception {
        System.out.println("");
        Word adj1 = new Word("");
        Word noun1 = new Word("");
        Word adj2 = new Word("");
        Word noun2 = new Word("");
        Word verb = new Word("");
        
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
            
            LookupResult entity1 = null;
            if(!noun1.getText().equals("")){
	            for (int i = 0; i < entities.size(); i++) {
	            	if(entities.get(i).getSearchedTitle() == null) continue;
					if(entities.get(i).getSearchedTitle().contains(noun1.getText())){
						entity1 = entities.get(i);
						break;
					}
				}
            }
            if(entity1 != null){
            	noun1 = new Word(entity1.getSearchedTitle());
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
            if(entity2 != null){
            	noun2 = new Word(entity2.getSearchedTitle());
            }
            
            System.out.println("Found: " + adj1.getText() + "_JJ " + noun1.getText() + "_NN " + verb.getText() + "_VB " + adj2.getText() + "_JJ " + noun2.getText() + "_NN ");
            
            if(entity2 == null && entity1 == null)
            	return returnFalse();
            if(entity1 != null && noun2.getText().equals(""))
            	return returnFalse();
            if(entity2 != null && noun1.getText().equals(""))
            	return returnFalse();
            
            
//            LookupResult entity = _sentence.dbpediaResource.findOneIn(noun2.getText());

            if(entity1 != null){
            	ArrayList<String> res = new ArrayList<>();
            	ArrayList<String> usedResults = new ArrayList<>();
            	ArrayList<String> redirects = new ArrayList<>();
            	ArrayList<String> e2Syns = new ArrayList<>();
            	ArrayList<String> vbSyns = new ArrayList<>();
	            usedResults.add(entity1.getResult());
	            int counter = 0;
	            while(res.size() == 0 && counter < 5){
	            	counter++;
	            	DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence, entity1.getResult());
		            
	            	if(verb.getText().equals("")){
		            	res.addAll(pl.findPropertyForName(verb.getText()));
		            	for (int i = 0; i < res.size(); i++) {
							if(Levenshtein.normalized(noun2.getText(), res.get(i)) < 0.15)
								return returnTrue();
						}
		            	res.clear();
	            	}
	            	
		            if(pl.findPropertyForName(noun2.getText()).size() > 0)
		            	return returnTrue();
		            
		            if(res.size() == 0){
		            	if(e2Syns.size() == 0)	e2Syns.addAll(SynonymCrawler.findSynonyms(new Word(noun2.getText(), "NN")));
		            	if(verb.getText().equals("")){
		            		if(vbSyns.size() == 0)	vbSyns.addAll(SynonymCrawler.findSynonyms(verb));
		            	}
		            	for (int i = 0; i < e2Syns.size(); i++) {
		            		if(pl.findPropertyForName(e2Syns.get(i)).size() > 0)
		    	            	return returnTrue();
						}
		            	for (int i = 0; i < vbSyns.size(); i++) {
		            		res.addAll(pl.findPropertyForName(vbSyns.get(i)));
		                	for (int k = 0; k < res.size(); k++) {
		    					if(Levenshtein.normalized(noun1.getText(), res.get(k)) < 0.15)
		    						return returnTrue();
		    				}
		                	res.clear();
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
		            		entity1.setResult(redirects.get(0));
		            		usedResults.add(redirects.get(0));
		            		redirects.remove(0);
		            	}
		            }
	            }
            }
            
            if(entity2 != null){
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
		            
	            	if(verb.getText().equals("")){
		            	res.addAll(pl.findPropertyForName(verb.getText()));
		            	for (int i = 0; i < res.size(); i++) {
							if(Levenshtein.normalized(noun1.getText(), res.get(i)) < 0.15)
								return returnTrue();
						}
		            	res.clear();
	            	}
	            	
		            if(pl.findPropertyForName(noun1.getText()).size() > 0)
		            	return returnTrue();
		            
		            if(res.size() == 0){
		            	if(e1Syns.size() == 0)	e1Syns.addAll(SynonymCrawler.findSynonyms(new Word(noun1.getText(), "NN")));
		            	if(verb.getText().equals("")){
		            		if(vbSyns.size() == 0)	vbSyns.addAll(SynonymCrawler.findSynonyms(verb));
		            	}
		            	
		            	for (int i = 0; i < e1Syns.size(); i++) {
		            		if(pl.findPropertyForName(e1Syns.get(i)).size() > 0)
		    	            	return returnTrue();
						}
		            	for (int i = 0; i < vbSyns.size(); i++) {
		            		res.addAll(pl.findPropertyForName(vbSyns.get(i)));
		                	for (int k = 0; k < res.size(); k++) {
		    					if(Levenshtein.normalized(noun1.getText(), res.get(k)) < 0.15)
		    						return returnTrue();
		    				}
		                	res.clear();
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
            }
        }catch(Exception e){
        	e.printStackTrace();
        }
        return returnFalse();
    }
}
