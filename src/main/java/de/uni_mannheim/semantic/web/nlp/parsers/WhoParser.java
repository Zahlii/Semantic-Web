package de.uni_mannheim.semantic.web.nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.nlp.TextAnalyzer;
import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.nlp.finders.DBPropertyList;
import de.uni_mannheim.semantic.web.nlp.parsers.HowParser.HowParserType;

public class WhoParser extends GenericParser {

	@Override
	protected ArrayList<String> parseInternal() throws Exception {
//		System.out.println(_sentence);
		
//		if(_sentence.get(0).equals("is") || _sentence.get(0).equals("was"))
//			System.out.println(_sentence.removeToken(0));
		
		Word w = _sentence.findEntity();
		
		ArrayList<String> answers = new ArrayList<>();
		if(w != null){
			DBPropertyList pl = new DBPropertyList(w.getResource());
			pl.fetchProperties();
			
			if(_sentence.getVerbs().size() == 0)
				return answers;
				
			Word search = _sentence.getVerbs().get(0);
	
			List<String> prop = pl.findPropertyFor(search);
	
			ArrayList<String> validTypes = new ArrayList<>();
			validTypes.add("http://dbpedia.org/ontology/Organisation");
			validTypes.add("http://dbpedia.org/ontology/Person");

			for (int i = 0; i < prop.size(); i++) {
				if(!answers.contains(prop.get(i))){
					if(isValidType(prop.get(i), validTypes))
						answers.add(prop.get(i));
				}
			}
		}
		return answers;
	}

}
