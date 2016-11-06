package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.vocabulary.DB;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.StanfordNLP;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.NGramLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup2;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaResourceLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

public class WhichParser extends GenericParser {

	@Override
	protected ArrayList<String> parseInternal() throws Exception {
		System.out.println("");
		Word adj1 = new Word("");
		Word noun1 = new Word("");
		Word adj2 = new Word("");
		Word noun2 = new Word("");
		Word verb = new Word("");
		
		try{
		boolean second = false;
		for (int i = 0; i < _sentence.getWords().size(); i++) {
			if(_sentence.getWords().get(i).getPOSTag().matches("JJ.*")){
				if(!second)	adj1 = _sentence.getWords().get(i);
				else		adj2 = _sentence.getWords().get(i);
			}
			if(_sentence.getWords().get(i).getPOSTag().matches("NN.*")){
				if(!second)	noun1 = _sentence.getWords().get(i);
				else		noun2 = _sentence.getWords().get(i);
				second = true;
			}
			if(_sentence.getWords().get(i).getPOSTag().matches("VB.*")){
				verb = _sentence.getWords().get(i);
			}
		}
		
		System.out.println("Found: "+adj1.getText()+" "+noun1.getText()+" "+verb.getText()+" "+adj2.getText()+" "+noun2.getText()+" ");
		
		DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence,noun2.getText());
//		findPropertyForName()
		ArrayList<LookupResult> results = DBPediaWrapper.spotlightLookupSearch(noun2.getText());
		
		ArrayList<String> props = DBPediaPropertyLookup2.fetchProperties(results.get(0).getResult());
		ArrayList<String> res = new ArrayList<>();
		res.addAll(DBPediaPropertyLookup2.findProperties(StanfordNLP.getStem(verb.getText()), props));
		res.addAll(DBPediaPropertyLookup2.findProperties(StanfordNLP.getStem(noun1.getText()), props));
		
		ArrayList<String> ontologies = new ArrayList<>();
		ArrayList<String> properties = new ArrayList<>();
		
		for(int i=0; i<res.size();i ++){
			if(res.get(i).matches(".*property.*")){
				properties.add(res.get(i));
			}else if(res.get(i).matches(".*ontology.*")){
				ontologies.add(res.get(i));
			}
		}
		
		Property p = new Property(properties.get(0), null, null, null, null, null);
		
		ArrayList<String> finalRes = DBPediaWrapper.checkPropertyExists(results.get(0).getResult(), p);
		
		
		ArrayList<LookupResult> lookupResults = new ArrayList<>();
		for(int i=0; i<finalRes.size(); i++){
			lookupResults.addAll(DBPediaWrapper.spotlightLookupSearch(finalRes.get(i)));
		}
		
		ArrayList<String> finalList = new ArrayList<>(); 
		for (int i = 0; i < lookupResults.size(); i++) {
			ArrayList<String> types = DBPediaWrapper.getTypeOfResource(lookupResults.get(i).getResult());
			for (int j = 0; j < types.size(); j++) {
				types.set(j, types.get(j).toLowerCase());
			}
			
			for (int j = 0; j < ontologies.size(); j++) {
				String ont = ontologies.get(j).toLowerCase();
				if(types.contains(ont) && !finalList.contains(lookupResults.get(i).getResult())){
					finalList.add(lookupResults.get(i).getResult());
				}
			}
		}
		
		return finalList;
		
//		DBPediaPropertyLookup prop = new DBPediaPropertyLookup(_sentence, results.get(0).getResult());		
//		ArrayList<String> props = prop.findPropertyForName(verb.getStem());
//		ArrayList<String> props2 = prop.findPropertyForName(noun1.getStem());

		}catch(Exception e){
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
