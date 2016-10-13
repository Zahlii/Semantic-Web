package de.uni_mannheim.semantic.web;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class TextAnalyzer {
    public static Tokenizer Tokenizer;
    public static Parser Parser;
    public static POSTaggerME Tagger;
    public static PorterStemmer Stemmer;
    public static SimpleLemmatizer Lemmatizer;
    //private static NameFinderME[] finders;
    

    static {
		try {
			InputStream modelInTokens = new FileInputStream("en-token.bin");
			final TokenizerModel tokenModel = new TokenizerModel(modelInTokens);
			modelInTokens.close();    	 
			Tokenizer = new TokenizerME(tokenModel);
			
			
			InputStream modelInParser = new FileInputStream("en-parser-chunking.bin");
			final ParserModel parseModel = new ParserModel(modelInParser);
			modelInParser.close();
			
			InputStream modelInPos = new FileInputStream("en-pos-maxent.bin");
			final POSModel posModel = new POSModel(modelInPos);
			Tagger = new POSTaggerME(posModel);
			modelInPos.close();
			                
			InputStream is = new FileInputStream("en-lemmatizer.dict");
		    Lemmatizer = new SimpleLemmatizer(is);
		    is.close();
		    
			Parser = ParserFactory.create(parseModel);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		Stemmer = new PorterStemmer();
		
	    /*String[] names = {"person", "location", "organization"};
	    int l = names.length;
	    
	    finders = new NameFinderME[l];
	    for (int mi = 0; mi < l; mi++) {
	      finders[mi] = new NameFinderME(new TokenNameFinderModel(
	          new FileInputStream("en-ner-" + names[mi] + ".bin")));
	    }*/
    }
    
    public TextAnalyzer(String text) {
    	this._currentText = text;
    	this._originalText = _currentText;
    	
    }
    

    private String _currentText;
    private String _originalText;
    private HashMap<Integer,String> _variables = new HashMap<Integer,String>();
    
    public void parseSentence() {

		
		Span[] newSpans = replaceNamedEntities();
		ArrayList<String> tokens = tokenizeText(newSpans);
		
		System.out.println(_originalText);
		System.out.println(tokens);
		System.out.println(_variables);
		System.out.println();
		 
		
    }
    
    public void tagSentence() {
    	Span[] spans = Tokenizer.tokenizePos(_currentText);
    	
		final Parse p = new Parse(_currentText,new Span(0, _currentText.length()),AbstractBottomUpParser.INC_NODE,1,0);
		for (int idx=0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses 
			p.insert(new Parse(_currentText,span,AbstractBottomUpParser.TOK_NODE,0,idx));
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
		Parse x =Parser.parse(p);
		

		
    }
    
    private void analyzeParsedTree(Parse tree) {
    	System.out.println(_originalText);
		tree.show();
		System.out.println();
    }
    
    private boolean isCandidateForNamedEntity(String t) {
    	if(t.contains(" ") || t.contains("_"))
    		return false;
    	String s = t.substring(0,1);
    	return s.toUpperCase().equals(s);
    }

    

	private Span[] replaceNamedEntities() {
		Span[] spans = Tokenizer.tokenizePos(_currentText);
		
		ArrayList<String> tokens = tokenizeText(spans);
	
		
		int l = tokens.size();
		
		String title;
		
		for(int i=0;i<l;i++) {
			String t = tokens.get(i);
			
			
			String lookup = "";
			String successfulLookup = null;
			String successfulLookupResult = null;
			
			// current token a noun?
			if(isCandidateForNamedEntity(t)) {			
						
				// create up to maxLength Lookups
				for(int j=i;j<Math.min(l,i+4);j++) {
					String nextItem = tokens.get(j);
					lookup = (lookup + " " + nextItem).trim();
					
					// if we found it, it's the new title
					if((title = DBPedia.checkTitleExists(lookup)) != null) {
						successfulLookup = lookup;
						successfulLookupResult = title;
					}
				}
				
				if(successfulLookup != null) {
					int var = _variables.size()+1;
					_variables.put(var, successfulLookupResult);
					_currentText = _currentText.replace(successfulLookup, "_"+var);
					return replaceNamedEntities();
				}
			}
		}

		return spans;
	}
	
	private ArrayList<String> tokenizeText(Span[] spans) {
		ArrayList<String> tokens = new ArrayList<String>(spans.length);
		
		for(int i=0,l=spans.length;i<l;i++)
			tokens.add((String) spans[i].getCoveredText(_currentText));
		
		return tokens;
	}
}
