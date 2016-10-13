package de.uni_mannheim.semantic.web;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;

public class NGram extends ArrayList<Token> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4840688175789406794L;

	public NGram(List<Token> subList) {
		super(subList);
	}
	
	public int getStartTokenIndex() {
		return this.get(0).getIndex();
	}
	
	public int getEndTokenIndex() {
		return this.get(this.size()-1).getIndex();
	}
	
	public Span getSpan() {
		return new Span(this.get(0).getSpan().getStart(),this.get(this.size()-1).getSpan().getEnd());
	}
	
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(Token _token : this) {
    		sb.append(_token.toString()).append(",");
    	}
    	
    	return sb.toString();
    }
    
    public String getText() {
    	StringBuilder sb = new StringBuilder();
    	for(Token _token : this) {
    		sb.append(_token.getText()).append(" ");
    	}
    	
    	return sb.toString().trim();
    }
    
    public String getStemmedText() {
    	StringBuilder sb = new StringBuilder();
    	for(Token _token : this) {
    		sb.append(_token.getStem()).append(" ");
    	}
    	
    	return sb.toString().trim();
    }
}
