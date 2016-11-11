package de.uni_mannheim.semantic.web.stanford_nlp.model;

import java.util.ArrayList;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

public class Word {

	private String _text;
	private String _posTag = "";
	private String _stem;
	
	private Integer tagPosition;
	private ArrayList<String> synonyms;
	private ArrayList<String> alias;
	private String resource;
	
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

	public Integer getTagPosition() {
		return tagPosition;
	}

	public void setTagPosition(Integer tagPosition) {
		this.tagPosition = tagPosition;
	}

	public ArrayList<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(ArrayList<String> synonyms) {
		this.synonyms = synonyms;
	}

	public ArrayList<String> getAlias() {
		return alias;
	}

	public void setAlias(ArrayList<String> alias) {
		this.alias = alias;
	}

	public String getResource() {
		return resource;
	}

	public void setResource(String resource) {
		this.resource = resource;
	}
	
	public void setPOSTag(String tag){
		this._posTag = tag;
	}
}
