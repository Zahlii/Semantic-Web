package de.uni_mannheim.semantic.web.nlp;

import opennlp.tools.util.Span;

public class Token {

	public Span getSpan() {
		return _span;
	}
	public void setSpan(Span _span) {
		this._span = _span;
		this._start = _span.getStart();
		this._end = _span.getEnd();
	}
	public String getText() {
		return _text;
	}
	public void setText(String _text) {
		this._text = _text;
	}
	public String getStem() {
		return _stem;
	}
	public void setStem(String _stem) {
		this._stem = _stem;
	}
	public String getPOSTag() {
		return _posTag;
	}
	public void setPOSTag(String _type) {
		this._posTag = _type;
	}
	public void setIndex(int i) {
		this._index = i;
	}
	public int getIndex() {
		return this._index;
	}
	public String getResource() {
		return _resource;
	}
	public void setResource(String _resource) {
		this._resource = _resource;
	}
	
	private String _sentence;
	private Span _span;
	private String _text;
	private String _stem = "";
	private String _posTag = "";
	private String _resource = "";
	private int _start;
	private int _end;
	private int _index;
	private double _probability = 0;
	
	public Token(Span span,String text,int index) {
		this._sentence = text;
		this._index = index;
		this.setSpan(span);
		this.setText((String) span.getCoveredText(text));
	}
	
	// Clone it
    public Token(Token t,String completeText) {
    	this(t.getSpan(),completeText,t.getIndex());
    	setPOSTag(t.getPOSTag());
    	setResource(t.getResource());
    	setStem(t.getStem());
    	setProbability(getProbability());
	}
	public double getProbability() {
		return _probability;
	}
	public void setProbability(double _probability) {
		this._probability = _probability;
	}
	@Override
    public String toString() {
    	return _text + "("+_posTag+" from "+_stem+" = "+this._resource+" ["+this._probability+"])";
    }
    
    public Token mergeWith(Token nextToken) {
    	Token ret = new Token(new Span(this._start,nextToken.getSpan().getEnd()),_sentence,this._index);
    	return ret;
    }

}
