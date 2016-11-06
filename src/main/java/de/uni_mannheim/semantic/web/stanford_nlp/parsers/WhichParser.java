package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.NGramLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
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
		DBPediaPropertyLookup prop = new DBPediaPropertyLookup(_sentence, results.get(0).getResult());		
		ArrayList<String> props = prop.findPropertyForName(verb.getStem());
		ArrayList<String> props2 = prop.findPropertyForName(noun1.getStem());

		System.out.println("");
		}catch(Exception e){
			e.printStackTrace();
		}
		return new ArrayList<>();
	}

}
