package de.uni_mannheim.semantic.web.nlp.parsers;

import de.uni_mannheim.semantic.web.nlp.QuestionType;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;

public class HowParser extends GenericParser {

	enum HowParserType {
		Often_Verb,
		Property
	}
	
	private HowParserType subType;
	
	@Override
	protected void parseInternal() {

		
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

		_sentence.findEntity();
		System.out.println(_sentence);
	}



}
