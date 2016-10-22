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
		System.out.println(_sentence);
		
		if(_sentence.get(0).equals("is") || _sentence.get(0).equals("was"))
			System.out.println(_sentence.removeToken(0));
		
		Word w = _sentence.findEntity();
		
		ArrayList<String> answers = new ArrayList<>();
		if(w != null){
			DBPropertyList pl = new DBPropertyList(w.getResource());
			pl.fetchProperties();
			
			if(_sentence.getVerbs().size() == 0)
				return answers;
				
			String search = _sentence.getVerbs().get(0).getStem();
	
			List<String> prop = pl.findPropertyFor(search);
	
			for (int i = 0; i < prop.size(); i++) {
				if(prop.get(i).matches("http:.*")){
					ArrayList<String> types = DBPedia.getTypeOfResource(prop.get(i));
					
					boolean validType = false;
					for (String type : types) {
						if(type.equals("http://dbpedia.org/ontology/Organisation")){
							validType = true;
							break;
						}
						else if(type.equals("http://dbpedia.org/ontology/Person")){
							validType = true;
							break;
						}
					}
					
					if(validType)
						answers.add(prop.get(i));
				}
			}
		}
		return answers;
	}

}
