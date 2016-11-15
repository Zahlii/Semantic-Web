package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.SynonymFinder;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

public class WhatParser extends GenericParser{

	   @Override
	    protected ArrayList<String> parseInternal() throws Exception {
		   try{
		        Word adj1 = new Word("");
		        Word verb = new Word("");
		        Word noun1 = new Word("");
		        Word noun2 = new Word("");
		        Word adj = new Word("");
		        //splitting sentence into words
		        for (int i = 0; i < _sentence.getWords().size(); i++) {   
		        	if (_sentence.getWords().get(i).getPOSTag().matches("VB")) {
		                if (verb.getText().equals("")){
		                	verb = _sentence.getWords().get(i);
		                }
		            }
		            if (_sentence.getWords().get(i).getPOSTag().matches("JJ")) {
		                if (adj1.getText().equals("")){
		                	adj1 = _sentence.getWords().get(i);
		                }
		            }
		            if (_sentence.getWords().get(i).getPOSTag().matches("NNP")) {
		            	if(noun1.getText().equals("")){
		            		noun1 = _sentence.getWords().get(i);
		            	}
		            }
		            if (_sentence.getWords().get(i).getPOSTag().matches("NNS?")) {
		                if (noun2.getText().equals("")){
		                	noun2 = _sentence.getWords().get(i);
		                }
		            }
		        }
		        
		        adj = (noun2.getText().equals("")) ? adj1 : noun2;
		        adj = (adj.getText().equals("")) ? verb : adj;
		        //convert non-numeric numbers to numerics e.g. ten to 10
//			        if(!tmpNumber.matches("\\d+(\\s\\d*)*")){
//			        	tmpNumber = Util.replaceNumbers(tmpNumber);
//			        }
//			        Double number = null;
//			        try{
//			        	number = Double.parseDouble(tmpNumber);
//			        }catch(NumberFormatException e){
//			        	
//			        }
		        
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
	            
	            if(entity1 == null)
	            	return new ArrayList<>();
		        
		        System.out.println("Found: " + adj.getText() + "_JJ " + noun1.getText() + "_NN ");
		    	
		        ArrayList<String> res = new ArrayList<>();
	            ArrayList<String> usedResults = new ArrayList<>();
	            ArrayList<String> redirects = new ArrayList<>();
	            ArrayList<String> vbSyns = new ArrayList<>();
	            usedResults.add(entity1.getResult());

	            while(res.size() == 0){
	            	DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence, entity1.getResult());
		            
	            	res.addAll(pl.findPropertyForName(adj.getText()));
		            
		            if(res.size() == 0){
		            	if(vbSyns.size() == 0)	vbSyns.addAll(SynonymFinder.findSynonyms(adj,true));
		            
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
		            		entity1.setResult(redirects.get(0));
		            		usedResults.add(redirects.get(0));
		            		redirects.remove(0);
		            	}
		            }
	            }
	            
				return prepareForReturn(res, new ArrayList<>());
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return new ArrayList<>();
	    }

}
