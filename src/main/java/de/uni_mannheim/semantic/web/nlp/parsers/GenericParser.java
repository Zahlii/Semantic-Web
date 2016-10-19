package de.uni_mannheim.semantic.web.nlp.parsers;

import de.uni_mannheim.semantic.web.nlp.QuestionType;
import de.uni_mannheim.semantic.web.nlp.Sentence;

public abstract class GenericParser {
	protected Sentence _sentence;

	public Sentence getSentence() {
		return _sentence;
	}

	public GenericParser() {
		
	}

	public void parse(Sentence s) throws Exception {
		this._sentence = s;
		
		parseInternal();
	}
	
	protected abstract void parseInternal() throws Exception;

}
