package de.uni_mannheim.semantic.web.stanford_nlp.model;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class Word {

	private String _text;
	private String _posTag = "";

	
	public Word(CoreLabel w){
		this._text = w.get(CoreAnnotations.TextAnnotation.class);
		this._posTag = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
	}

	public Word (String text,String pos) {
		this._text = text;
		this._posTag = pos;
	}


	public String getText() {
		return _text;
	}

	public String getPOSTag() {
		return _posTag;
	}

	@Override
	public String toString() {
		return _text + "(" + _posTag + ")";
	}

}
