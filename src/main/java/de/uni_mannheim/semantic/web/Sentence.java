package de.uni_mannheim.semantic.web;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;

public class Sentence {
    private ArrayList<Token> _tokens = new ArrayList<Token>();
    private String _originalText;
    
    public Sentence(String _text) {
    	_originalText = _text.trim();
    	
    	constructTokens();
    	POSTagTokens();
    	//stemTokens();
    	lemmatizeTokens();
    }
    
    private void constructTokens() {
    	Span[] _tokenSpans = TextAnalyzer.Tokenizer.tokenizePos(_originalText);
    	
    	for(Span _token : _tokenSpans) {
    		_tokens.add(new Token(_token,_originalText));
    	}
    }
    
    private void lemmatizeTokens() {
    	for(Token _token : _tokens) {  		
    		String lemma = TextAnalyzer.Lemmatizer.lemmatize(_token.getText(), _token.getType());
    		_token.setStem(lemma);
    	}
    	return;
    }
    
    private void stemTokens() {
    	for(Token _token : _tokens) {
    		String pos = _token.getType();
    		
    		if(pos.startsWith("VB") || pos.equals("NNS")) {
	    		_token.setStem(TextAnalyzer.Stemmer.stem(_token.getText()));
	    		TextAnalyzer.Stemmer.reset();
    		}
    	}
    	return;
    }
    
    private String[] getTokenizedStrings() {
    	String[] _strings = new String[_tokens.size()];
    	int i = 0;
    	for(Token _token : _tokens) {
    		_strings[i++] = _token.getText();
    	}
    	return _strings;
    }
    
    private void POSTagTokens() {
    	String[] _pos = TextAnalyzer.Tagger.tag(getTokenizedStrings());
    	int i = 0;
    	for(String _tag : _pos) {
    		_tokens.get(i++).setType(_tag);
    	}
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(Token _token : _tokens) {
    		sb.append(_token.toString()).append(",");
    	}
    	
    	return sb.toString();
    }
    
    public void mergeTokens(int start,int end) {
    	Token n = _tokens.get(start);
    	
    	for(int i=start+1;i<end;i++) {
    		n = n.mergeWith(_tokens.get(i));
    	}
    	
    	_tokens.subList(start, end).clear();
    	_tokens.add(start, n);
    }
}
