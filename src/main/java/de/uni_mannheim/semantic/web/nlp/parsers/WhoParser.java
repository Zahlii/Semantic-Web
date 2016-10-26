package de.uni_mannheim.semantic.web.nlp.parsers;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.nlp.finders.DBPropertyList;

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
			
//			if(_sentence.getVerbs().size() == 0)
//				return answers;
				
			List<String> prop = new ArrayList<>();
			
			List<Word> verbs = _sentence.getVerbs();
			List<Word> adjs = _sentence.getAdjectives();
			List<Word> advs = _sentence.getAdverbs();
			List<Word> nouns = _sentence.getNouns();

	
//			for(int i = 0; i<verbs.size(); i++){
//				prop.addAll(pl.findPropertyFor(verbs.get(i)));
//			}
//			

			
//			List<String> prop = pl.findPropertyFor(verbs.get(0));
	
			Word verb = null;

			for(int i=0; i<verbs.size(); i++){
				if(!auxilaryVerbs.contains(verbs.get(i).getText())){
					verb = verbs.get(i);
				}
			}

			if(verb == null){
				
				for(int i = 0; i<nouns.size(); i++){
					prop.addAll(pl.findPropertyFor(nouns.get(i)));
				}
				
				for(int i = 0; i<adjs.size(); i++){
					prop.addAll(pl.findPropertyFor(adjs.get(i)));
				}
				
				for(int i = 0; i<advs.size(); i++){
					prop.addAll(pl.findPropertyFor(advs.get(i)));
				}
				
			}else{
				prop.addAll(pl.findPropertyFor(verb));
			}
			
			
			ArrayList<String> validTypes = new ArrayList<>();
			validTypes.add("http://dbpedia.org/ontology/Organisation");
			validTypes.add("http://dbpedia.org/ontology/Person");

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
	}
}
