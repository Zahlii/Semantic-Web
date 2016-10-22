package de.uni_mannheim.semantic.web.nlp.parsers;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.nlp.QuestionType;
import de.uni_mannheim.semantic.web.nlp.Sentence;

public abstract class GenericParser {
	protected Sentence _sentence;
	
	public Sentence getSentence() {
		return _sentence;
	}

	public GenericParser() {
		
	}

	public ArrayList<String> parse(Sentence s) throws Exception {
		this._sentence = s;
		
		return parseInternal();
	}
	
	protected abstract ArrayList<String> parseInternal() throws Exception;

}
