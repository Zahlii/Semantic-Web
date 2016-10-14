package de.uni_mannheim.semantic.web.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.info.DBPedia_Terms;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

public class Sentence {
	private NGram _mainNGram;
	private String _originalText;
	
    public Sentence(String _text) {
    	_originalText = _text;
    	
    	cleanText();
    	constructTokens();
    	POSTagTokens();
    	//parseTokens();
    	lemmatizeTokens();
    	
    	DBResourceInterpretation dbr = new DBResourceInterpretation(this);

    	System.out.println();
    }
    
    private void cleanText() {
    	_originalText = _originalText.replace("?", "").trim();
    }
    public void parseTokens() {
    	Span[] spans = TextAnalyzer.Tokenizer.tokenizePos(_originalText);
    	
		final Parse p = new Parse(_originalText,new Span(0, _originalText.length()),AbstractBottomUpParser.INC_NODE,1,0);
		for (int idx=0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses 
			p.insert(new Parse(_originalText,span,AbstractBottomUpParser.TOK_NODE,0,idx));
		}
		
		// https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		
		/*
		S -> Simple declarative clause, i.e. one that is not introduced by a (possible empty) subordinating conjunction or a wh-word and that does not exhibit subject-verb inversion.
		SBAR -> Clause introduced by a (possibly empty) subordinating conjunction.
		SBARQ -> Direct question introduced by a wh-word or a wh-phrase. Indirect questions and relative clauses should be bracketed as SBAR, not SBARQ.
		SINV -> Inverted declarative sentence, i.e. one in which the subject follows the tensed verb or modal.
		SQ -> Inverted yes/no question, or main clause of a wh-question, following the wh-phrase in SBARQ.
		ADJP -> Adjective Phrase.
		ADVP -> Adverb Phrase.
		CONJP -> Conjunction Phrase.
		FRAG -> Fragment.
		INTJ -> Interjection. Corresponds approximately to the part-of-speech tag UH.
		LST -> List marker. Includes surrounding punctuation.
		NAC -> Not a Constituent; used to show the scope of certain prenominal modifiers within an NP.
		NP -> Noun Phrase.
		NX -> Used within certain complex NPs to mark the head of the NP. Corresponds very roughly to N-bar
		PP -> Prepositional Phrase.
		PRN -> Parenthetical.
		PRT -> Particle. Category for words that should be tagged RP.
		QP -> Quantifier Phrase (i.e. complex measure/amount phrase); used within NP.
		RRC -> Reduced Relative Clause.
		UCP -> Unlike Coordinated Phrase.
		VP -> Verb Phrase.
		WHADJP -> Wh-adjective Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
		WHAVP -> Wh-adverb Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing a wh-adverb such as how or why.
		WHNP -> Wh-noun Phrase. Introduces a clause with an NP gap. May be null (containing the 0 complementizer) or lexical, containing some wh-word, e.g. who, which book, whose daughter, none of which, or how many leopards.
		WHPP -> Wh-prepositional Phrase. Prepositional phrase containing a wh-noun phrase (such as of which or by whose authority) that either introduces a PP gap or is contained by a WHNP.
		X -> Unknown, uncertain, or unbracketable. X is often used for bracketing typos and in bracketing the...the-constructions.
		 */
		Parse x = TextAnalyzer.Parser.parse(p);
		x.show();

		
    }
    
    private void constructTokens() {
    	Span[] _mainNGrams = TextAnalyzer.Tokenizer.tokenizePos(_originalText);
    	
    	int i=0;
    	ArrayList<Token> tokens = new ArrayList<Token>(_mainNGrams.length);
    	
    	for(Span _token : _mainNGrams) {
    		tokens.add(new Token(_token,_originalText,i++));
    	}
    	
    	_mainNGram = new NGram(tokens);
    }
    
    private void lemmatizeTokens() {
    	for(Token _token : _mainNGram) {  		
    		String lemma = TextAnalyzer.Lemmatizer.lemmatize(_token.getText(), _token.getPOSTag());
    		_token.setStem(lemma);
    	}
    	return;
    }
    
    private void stemTokens() {
    	for(Token _token : _mainNGram) {
    		String pos = _token.getPOSTag();
    		
    		if(pos.startsWith("VB") || pos.equals("NNS")) {
	    		_token.setStem(TextAnalyzer.Stemmer.stem(_token.getText()));
	    		TextAnalyzer.Stemmer.reset();
    		}
    	}
    	return;
    }
    
    private String[] getTokenizedStrings() {
    	String[] _strings = new String[_mainNGram.size()];
    	int i = 0;
    	for(Token _token : _mainNGram) {
    		_strings[i++] = _token.getText();
    	}
    	return _strings;
    }
    
    private void POSTagTokens() {
    	String[] _pos = TextAnalyzer.Tagger.tag(getTokenizedStrings());
    	int i = 0;
    	for(String _tag : _pos) {
    		_mainNGram.get(i++).setPOSTag(_tag);
    	}
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	for(Token _token : _mainNGram) {
    		sb.append(_token.toString()).append(",");
    	}
    	
    	return sb.toString();
    }
    

    

        
    private void scanForDboClasses(){
    	for (int i = 0; i < _mainNGram.size(); i++) {
//    		if(_mainNGram.get(i).getResource() == null) continue;
    		DBPedia_Terms.getOntologyClassByName(_mainNGram.get(i).getText());
//			DBPedia.scanForDboClasses(_mainNGram.get(i).getResource());
		}
    }

	public NGram getMainNGram() {
		return _mainNGram;
	}

	public String getText() {
		return _originalText;
	}
}
