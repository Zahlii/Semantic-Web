package de.uni_mannheim.semantic.web;

import java.util.ArrayList;
import java.util.Arrays;
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
    	scanForEntities(0);
    	System.out.println(this);
    	System.out.println();
    }
    
    private void constructTokens() {
    	Span[] _tokenSpans = TextAnalyzer.Tokenizer.tokenizePos(_originalText);
    	
    	int i=0;
    	for(Span _token : _tokenSpans) {
    		_tokens.add(new Token(_token,_originalText,i++));
    	}
    }
    
    private void lemmatizeTokens() {
    	for(Token _token : _tokens) {  		
    		String lemma = TextAnalyzer.Lemmatizer.lemmatize(_token.getText(), _token.getPOSTag());
    		_token.setStem(lemma);
    	}
    	return;
    }
    
    private void stemTokens() {
    	for(Token _token : _tokens) {
    		String pos = _token.getPOSTag();
    		
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
    		_tokens.get(i++).setPOSTag(_tag);
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
    
    public Token mergeTokens(int start,int end) {
    		
    	Token n = _tokens.get(start);
    	if(end<=start)
    		return n;
    	
    	for(int i=start+1;i<=end;i++) {
    		n = n.mergeWith(_tokens.get(i));
    	}
    	
    	_tokens.subList(start, end+1).clear();
    	_tokens.add(start,n);
    	int i = 0;
    	for(Token t : _tokens) {
    		t.setIndex(i++);
    	}
    	
    	return n;
    }
    
    private List<NGram> getUpToNGrams(int n) {
    	List<NGram> ret = new ArrayList<NGram>();
    	for(int i=n;i>=0;i--) {
    		ret.addAll(getNGrams(i));
    	}
    	return ret;
    }
    
    private List<NGram> getNGrams(int n) {
    	List<NGram> ret = new ArrayList<NGram>();
    	for(int i=0;i<_tokens.size()-n+1;i++) {
    		ret.add(new NGram(_tokens.subList(i, i+n)));
    	}
    	return ret;
    }
    
    private boolean isCandidateNGram(NGram tokens) {
    	if(tokens.size() == 1 && tokens.get(0).getResource() != null)
    		return false;
    	
    	String[] pos = new String[tokens.size()];
    	int i = 0;
    	for(Token t : tokens)
    		pos[i++] = t.getPOSTag();
    	
    	String posType = String.join(",", pos).replaceAll("S","");
    	
    	String[] allowedPosType = new String[] {
    			"NNP","NNPS",
    			"NNP,NNP","NNP,NNP,NNP","NNP,NNP,NN",
    			"NNP,CD","NNP,NNP,CD","NNP,CD,NNP","NNP,CD,NN",
    			"NN,NN","NN,NN,CD","NNP,NN",
    			"NN","NNP","NN",
    			"NNP,IN,NNP","NNP,PRP","NNP,NNP,PRP",
    			"NNP,NNP,IN,NNP","NN,IN,NN",
    			"JJ"
    	};

    	
    	boolean applicableStructure = Arrays.asList(allowedPosType).contains(posType);
    	
    	if(!applicableStructure)
    		return false;

    	return true;
    }
    
    private void mergeNGramEntity(NGram g, String resource) {
    	Token newToken = mergeTokens(g.getStartTokenIndex(),g.getEndTokenIndex());
    	newToken.setPOSTag("NNP");
    	newToken.setResource(resource);
    }
    
    private void scanForEntities(int depth) {
    	if(depth>=3)
    		return;
    	
    	
    	List<NGram> ngrams = getUpToNGrams(4);
    	for(NGram ngram : ngrams) {
    		if(isCandidateNGram(ngram)) {
    			String title = ngram.getStemmedText();
    			String dbTitle = DBPedia.checkTitleExists(title);
    			if(dbTitle != null) {
    				mergeNGramEntity(ngram,dbTitle);
    				scanForEntities(++depth);
    				return;
    			}
    		}
    	}
    	
    }
}
