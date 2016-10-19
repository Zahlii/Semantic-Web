package de.uni_mannheim.semantic.web.nlp.parsers;

import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.nlp.finders.DBPropertyList;

import java.util.List;
import java.util.Map;

public class HowParser extends GenericParser {

	enum HowParserType {
		Often_Verb,
		Property
	}
	
	private HowParserType subType;
	
	@Override
	protected void parseInternal() throws Exception {

		
		Word w1 = _sentence.get(0); 
		_sentence.removeToken(0); // Tall
		_sentence.removeToken(0); // Is, was
		
		switch(w1.getText()) {
		case "often":
			subType = HowParserType.Often_Verb;
			break;
		case "tall":
		case "high":
			subType = HowParserType.Property;
			break;
		}

		Word w = _sentence.findEntity();

		if(w == null)
			throw new Exception("Unable to find entity.");

		DBPropertyList pl = new DBPropertyList(w.getResource());

		pl.fetchProperties();

		String search;

		if(subType == HowParserType.Property) {
			search = w1.getText();
		} else {
			search = _sentence.getVerbs().get(0).getStem();
		}

		List<String> prop = pl.findPropertyFor(search);

		System.out.println(_sentence);

		System.out.print("\t");
		System.out.println(prop);


	}



}
