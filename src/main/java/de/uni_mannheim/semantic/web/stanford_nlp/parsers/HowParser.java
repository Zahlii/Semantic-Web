package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
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

		LookupResult w = _sentence.findEntity();

		if(w == null)
			throw new Exception("Unable to find entity.");

		DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence,w.getResult());


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

		ArrayList<String> prop = pl.findPropertyForName(search.getText());

		return prop;
	}



}
