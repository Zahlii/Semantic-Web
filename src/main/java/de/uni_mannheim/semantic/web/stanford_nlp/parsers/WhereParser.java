package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

public class WhereParser extends GenericParser {

	@Override
	protected ArrayList<String> parseInternal() throws Exception {
//		System.out.println(_sentence);
		
//		if(_sentence.getWord(0).equals("is") || _sentence.getWord(0).equals("was"))
//			System.out.println(_sentence.removeWord(0));

//		System.out.println("WhereParser");
		
		LookupResult w = _sentence.findEntity();
		
		ArrayList<String> answers = new ArrayList<>();
		
		if(w != null){
			DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence,w.getResult());

			List<String> prop = pl.findProperty();
			
			ArrayList<String> validTypes = new ArrayList<>();
			validTypes.add("http://dbpedia.org/ontology/Location");

			for (int i = 0; i < prop.size(); i++) {
				if(!answers.contains(prop.get(i))){
					if(isValidType(prop.get(i), validTypes))
						answers.add(prop.get(i));
				}
			}
		}else{
			System.out.println("Entity not found.");
		}
		return answers;
	}

	static ArrayList<String>auxilaryVerbs = new ArrayList<>();
	static{
		auxilaryVerbs.add("be");
		auxilaryVerbs.add("is");
		auxilaryVerbs.add("was");
		auxilaryVerbs.add("were");
		auxilaryVerbs.add("has");
		auxilaryVerbs.add("been");
		auxilaryVerbs.add("did");
		auxilaryVerbs.add("do");
		auxilaryVerbs.add("have");
	}
}
