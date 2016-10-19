package de.uni_mannheim.semantic.web.nlp;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.nlp.interpretation.DBResourceInterpretation;
import de.uni_mannheim.semantic.web.nlp.interpretation.YagoInterpretation;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.util.Span;

public class Sentence {
	enum QuestionType {
		Which("In which","To which","For which","Which","Through which"), // followed by CLASS
		Give_me_all("Give me all","Give me a list of all","Show me all","List the","List all"), // Followed by DESCRIPTION or CLASS
		Give_me_the("Give me"), // Followed by OBJECT
		Who_is("Who is","Who was","Who were"), // followed by DESCRIPTION or RESOURCE
		When_is("When is","When was","When were"), // followed by RESOURCE
		What_is("What is","What was","What were","What are"), // followed by PREDICATE? 
		Where_is("Where is","Where was","Where were"), // followed by PREDICATE? 
		Who("Who"), // followed by predicate
		When("When"), // Followed by 
		How_many("How many"), // followed by indicator for number
		Does("Does","Is","Do","Was","Did","Are"), // followed by RESOURCE
		How("How"); 
		
		private final String[] _alternatives;
		
		QuestionType(String... alternatives) {
			this._alternatives = alternatives;
		}
		public String[] getAlternatives() {
			return this._alternatives;
		}
		
		public boolean matches(String text) {
			for(String s : _alternatives) {
				if(text.startsWith(s))
					return true;
			}
			
			return false;
		}
		
		public String remove(String text) {
			for(String s : _alternatives) {
				if(text.startsWith(s))
					return text.replace(s, "").trim();
			}
			
			return text;
		}
	}
	
	private QuestionType type;
	private NGram _mainNGram;
	private String _originalText;

	public Sentence(String _text) {
		_originalText = _text;

		cleanText();
		try {
			extractQuestionType();
			constructTokens();

			POSTagTokens();
			// parseTokens();
			lemmatizeTokens();

			// DBResourceInterpretation dbr = new DBResourceInterpretation(this);

			// YagoInterpretation y = new YagoInterpretation(this);
			// InterpretationTest test = new InterpretationTest(this);

			// parseTokens(dbr.getSpans());
			System.out.println(this);
		} catch(Exception e) {
			System.err.println(e);
		}
		
		
	}

	private void extractQuestionType() throws Exception {
		for(QuestionType t : QuestionType.values()) {
			if(t.matches(_originalText)) {
				type = t;
				_originalText = t.remove(_originalText);
				return;
			}
		}
		
		throw new Exception("Malformed or unknown question " + this._originalText);
	}

	private void cleanText() {
		_originalText = _originalText.replaceAll("[^A-Za-z0-9\\s.]", "").replaceAll("[^A-Za-z0-9]$", "");
	}

	public void parseTokens(Span[] spans) {
		// Span[] spans = TextAnalyzer.Tokenizer.tokenizePos(_originalText);

		final Parse p = new Parse(_originalText, new Span(0, _originalText.length()), AbstractBottomUpParser.INC_NODE,
				1, 0);
		for (int idx = 0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses
			p.insert(new Parse(_originalText, span, AbstractBottomUpParser.TOK_NODE, 0, idx));
		}

		// https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html

		/*
		 * S -> Simple declarative clause, i.e. one that is not introduced by a
		 * (possible empty) subordinating conjunction or a wh-word and that does
		 * not exhibit subject-verb inversion. SBAR -> Clause introduced by a
		 * (possibly empty) subordinating conjunction. SBARQ -> Direct question
		 * introduced by a wh-word or a wh-phrase. Indirect questions and
		 * relative clauses should be bracketed as SBAR, not SBARQ. SINV ->
		 * Inverted declarative sentence, i.e. one in which the subject follows
		 * the tensed verb or modal. SQ -> Inverted yes/no question, or main
		 * clause of a wh-question, following the wh-phrase in SBARQ. ADJP ->
		 * Adjective Phrase. ADVP -> Adverb Phrase. CONJP -> Conjunction Phrase.
		 * FRAG -> Fragment. INTJ -> Interjection. Corresponds approximately to
		 * the part-of-speech tag UH. LST -> List marker. Includes surrounding
		 * punctuation. NAC -> Not a Constituent; used to show the scope of
		 * certain prenominal modifiers within an NP. NP -> Noun Phrase. NX ->
		 * Used within certain complex NPs to mark the head of the NP.
		 * Corresponds very roughly to N-bar PP -> Prepositional Phrase. PRN ->
		 * Parenthetical. PRT -> Particle. Category for words that should be
		 * tagged RP. QP -> Quantifier Phrase (i.e. complex measure/amount
		 * phrase); used within NP. RRC -> Reduced Relative Clause. UCP ->
		 * Unlike Coordinated Phrase. VP -> Verb Phrase. WHADJP -> Wh-adjective
		 * Phrase. Adjectival phrase containing a wh-adverb, as in how hot.
		 * WHAVP -> Wh-adverb Phrase. Introduces a clause with an NP gap. May be
		 * null (containing the 0 complementizer) or lexical, containing a
		 * wh-adverb such as how or why. WHNP -> Wh-noun Phrase. Introduces a
		 * clause with an NP gap. May be null (containing the 0 complementizer)
		 * or lexical, containing some wh-word, e.g. who, which book, whose
		 * daughter, none of which, or how many leopards. WHPP ->
		 * Wh-prepositional Phrase. Prepositional phrase containing a wh-noun
		 * phrase (such as of which or by whose authority) that either
		 * introduces a PP gap or is contained by a WHNP. X -> Unknown,
		 * uncertain, or unbracketable. X is often used for bracketing typos and
		 * in bracketing the...the-constructions.
		 */
		Parse x = TextAnalyzer.Parser.parse(p);
		showParse(x);

	}

	private void showParse(Parse p) {
		p.showCodeTree();
	}

	private void constructTokens() {
		Span[] _mainNGrams = TextAnalyzer.Tokenizer.tokenizePos(_originalText);

		int i = 0;
		ArrayList<Word> tokens = new ArrayList<Word>(_mainNGrams.length);

		for (Span _token : _mainNGrams) {
			tokens.add(new Word(_token, _originalText, i++));
		}

		_mainNGram = new NGram(tokens);
	}

	private void lemmatizeTokens() {
		for (Word _token : _mainNGram) {
			String lemma = TextAnalyzer.Lemmatizer.lemmatize(_token.getText(), _token.getPOSTag());
			_token.setStem(lemma);
		}
		return;
	}

	private void stemTokens() {
		for (Word _token : _mainNGram) {
			String pos = _token.getPOSTag();

			if (pos.startsWith("VB") || pos.equals("NNS")) {
				_token.setStem(TextAnalyzer.Stemmer.stem(_token.getText()));
				TextAnalyzer.Stemmer.reset();
			}
		}
		return;
	}

	private String[] getTokenizedStrings() {
		String[] _strings = new String[_mainNGram.size()];
		int i = 0;
		for (Word _token : _mainNGram) {
			_strings[i++] = _token.getText();
		}
		return _strings;
	}

	private void POSTagTokens() {
		String[] _pos = TextAnalyzer.Tagger.tag(getTokenizedStrings());
		int i = 0;
		for (String _tag : _pos) {
			_mainNGram.get(i++).setPOSTag(_tag);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Question "+type.toString() +" -> ");
		
		for (Word _token : _mainNGram) {
			sb.append(_token.toString()).append(",");
		}

		return sb.toString();
	}

	public NGram getMainNGram() {
		return _mainNGram;
	}

	public String getText() {
		return _originalText;
	}
}
