package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

public class WhoParser extends GenericParser {

	@Override
	protected ArrayList<String> parseInternal() throws Exception {
//		System.out.println(_sentence);
		
//		if(_sentence.getWord(0).equals("is") || _sentence.getWord(0).equals("was"))
//			System.out.println(_sentence.removeWord(0));


		LookupResult w = _sentence.findEntity();
		

		if(w != null){
			DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence,w.getResult());

			ArrayList<String> prop = pl.findProperty();
			
			return prop;
		}else{
			System.out.println("Entity not found.");
		}
		return new ArrayList<>();
	}

	static ArrayList<String>auxilaryVerbs = new ArrayList<>();
	static{
		auxilaryVerbs.add("be");
		auxilaryVerbs.add("is");
		auxilaryVerbs.add("was");
		auxilaryVerbs.add("were");
		auxilaryVerbs.add("has");
		auxilaryVerbs.add("been");
	}
}
