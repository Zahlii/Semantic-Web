package de.uni_mannheim.semantic.web;

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
	public String getType() {
		return _type;
	}
	public void setType(String _type) {
		this._type = _type;
	}
	private String _sentence;
	private Span _span;
	private String _text;
	private String _stem;
	private String _type;
	private int _start;
	private int _end;
	
	public Token(Span span,String text) {
		this._sentence = text;
		this.setSpan(span);
		this.setText((String) span.getCoveredText(text));
	}
	
    @Override
    public String toString() {
    	return _text + "("+_type+" from "+_stem+")";
    }
    
    public Token mergeWith(Token nextToken) {
    	Token ret = new Token(new Span(this._start,nextToken.getSpan().getEnd()),_sentence);
    	return ret;
    }
}
