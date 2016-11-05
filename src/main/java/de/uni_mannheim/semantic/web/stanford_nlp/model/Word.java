package de.uni_mannheim.semantic.web.stanford_nlp.model;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class Word {

	private String _text;
	private String _posTag = "";
	private String _stem;

	
	public Word(CoreLabel w){
		this._text = w.get(CoreAnnotations.TextAnnotation.class);
		this._posTag = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		this._stem = w.get(CoreAnnotations.StemAnnotation.class);
	}

	public Word (String text,String pos) {
		this._text = text;
		this._posTag = pos;
	}

    public Word(String w1) {
		this._text = w1;
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

    public String getStem() {
        return _stem;
    }
}
