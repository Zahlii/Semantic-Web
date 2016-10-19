package de.uni_mannheim.semantic.web.nlp.parsers;

import de.uni_mannheim.semantic.web.nlp.QuestionType;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;

public class HowParser extends GenericParser {

	enum HowParserType {
		Often,
		Tall,
		High
	}
	
	private HowParserType subType;
	
	@Override
	protected void parseInternal() {
		System.out.println(_sentence);
		
		Word w1 = _sentence.get(0); 
		_sentence.removeWord(0); // Tall
		_sentence.removeWord(1); // Is, was
		
		switch(w1.getText()) {
		case "often":
			subType = HowParserType.Often;
			break;
		case "tall":
			subType = HowParserType.Tall;
			break;
		case "high":
			subType = HowParserType.High;
			break;
		}
	}



}
