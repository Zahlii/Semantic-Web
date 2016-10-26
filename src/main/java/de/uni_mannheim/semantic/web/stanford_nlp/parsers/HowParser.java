package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyFetcher;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

import java.util.ArrayList;
import java.util.List;

//import com.github.andrewoma.dexx.collection.ArrayList;

public class HowParser extends GenericParser {

	enum HowParserType {
		Often_Verb,
		Property
	}
	
	private HowParserType subType;
	
	@Override
	protected ArrayList<String> parseInternal() throws Exception {

		
		Word w1 = _sentence.getWord(0);
		_sentence.removeWord(0); // Tall
		_sentence.removeWord(0); // Is, was
		
		switch(w1.getText()) {
		case "often":
			subType = HowParserType.Often_Verb;
			break;
		case "tall":
		case "high":
			subType = HowParserType.Property;
			break;
		}

		LookupResult<String> w = _sentence.findEntity();

		if(w == null)
			throw new Exception("Unable to find entity.");

		DBPediaPropertyFetcher pl = new DBPediaPropertyFetcher(w.getResult());

		pl.fetchProperties();

//		String search;
//
//		if(subType == HowParserType.Property) {
//			search = w1.getCleanedText();
//		} else {
//			search = _sentence.getVerbs().getWord(0).getStem();
//		}
		
		Word search;
		if(subType == HowParserType.Property) {
			search = w1;
		} else {
			search = _sentence.getVerbs().get(0);
		}

		List<String> prop = pl.findPropertyFor(search);
		
		ArrayList<String> answers = new ArrayList<>();
		ArrayList<String> validTypes = new ArrayList<>();
//		validTypes.add("http://dbpedia.org/ontology/Organisation");
//		validTypes.add("http://dbpedia.org/ontology/Person");

		for (int i = 0; i < prop.size(); i++) {
			prop.set(i, prop.get(i).replaceAll("\\^\\^.*", ""));
			if(!answers.contains(prop.get(i))){
				if(isValidType(prop.get(i), validTypes))
					answers.add(prop.get(i));
			}
		}
		
//		System.out.println(_sentence);

//		System.out.print("\t");
//		System.out.println(prop);

		return answers;
	}



}
