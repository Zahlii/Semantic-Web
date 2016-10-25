package de.uni_mannheim.semantic.web.nlp;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.nlp.finders.DBNERFinder;
import opennlp.tools.util.Span;

public class Sentence implements Comparable<Sentence> {
	private QuestionType type;
	private NGram _mainNGram;
	private String _originalText;
	private ArrayList<String> answers;
	
	public Sentence(String _text) {
		_originalText = _text;

		cleanText();
		try {
			extractQuestionType();
			
			constructTokens();
			
			POSTagTokens();
			lemmatizeTokens();
			
			this.setAnswers(type.startParsing(this));

		} catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public QuestionType getType() {
		return type;
	}

	private void extractQuestionType() throws Exception {
		for(QuestionType t : QuestionType.values()) {
			if(t.matches(_originalText)) {
				type = t;
				_originalText = t.removeFromQuestion(_originalText);
				return;
			}
		}
		
		throw new Exception("Malformed or unknown question " + this._originalText);
	}

	public Word get(int i) {
		return _mainNGram.get(i);
		
	}
	private void cleanText() {
		_originalText = _originalText.replaceAll("[^A-Za-z0-9\\s.]", "").replaceAll("[^A-Za-z0-9]$", "");
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
		StringBuilder sb = new StringBuilder("Question "+type.toString() +" -> " + _originalText +"\r\n\t");
		
		for (Word _token : _mainNGram) {
			sb.append(_token.toString()).append(",");
		}

		return sb.toString();
	}

	public String getText() {
		return _mainNGram.getText();
	}

	@Override
	public int compareTo(Sentence arg0) {
		int c = this.getType().compareTo(arg0.getType());
		if(c == 0)
			return this.getText().compareTo(arg0.getText());
		
		return c;
	}


	public Word removeToken(int index) {
		Word w = _mainNGram.remove(index);

		Span s = w.getSpan();

		int rem = s.length()+1;

		int i = 0;

		String txt = _mainNGram.getText();

		for (Word t : _mainNGram) {
			Span ts = t.getSpan();

			t.setSpan(new Span(ts.getStart()-rem,ts.getEnd()-rem));
			t.setSentence(txt);
			t.setIndex(i++);
		}


		return w;
	}

	public Word findEntity() {
		DBNERFinder f = new DBNERFinder(this);

		return f.findNext();
	}

	public Word mergeNGramEntity(NGram g, String resource, double prob) {
		Word newToken = mergeTokens(g.getStartTokenIndex(), g.getEndTokenIndex());
		newToken.setPOSTag("ENTITY");
		newToken.setResource(resource);
		newToken.setProbability(prob);

		return newToken;
	}

	public Word mergeTokens(int start, int end) {

		Word n = _mainNGram.get(start);
		if (end <= start)
			return n;

		for (int i = start + 1; i <= end; i++) {
			n = n.mergeWith(_mainNGram.get(i));
		}

		_mainNGram.subList(start, end + 1).clear();
		_mainNGram.add(start, n);
		int i = 0;
		for (Word t : _mainNGram) {
			t.setIndex(i++);
		}

		return n;
	}

	public NGram getMainNGram() {
		return this._mainNGram;
	}

	public List<Word> getVerbs() {
		ArrayList<Word> v = new ArrayList<Word>();

		for(Word w : _mainNGram) {
			if(w.getPOSTag().startsWith("VB"))
				v.add(w);
		}
		return v;
	}

	public ArrayList<String> getAnswers() {
		return answers;
	}

	public void setAnswers(ArrayList<String> answers) {
		this.answers = answers;
	}

	public ArrayList<Word> getAdjectives() {
		ArrayList<Word> adjs = new ArrayList<Word>();

		for(Word w : _mainNGram) {
			if(w.getPOSTag().startsWith("JJ"))
				adjs.add(w);
		}
		return adjs;
	}
	
	public ArrayList<Word> getAdverbs(){
		ArrayList<Word> advs = new ArrayList<Word>();

		for(Word w : _mainNGram) {
			if(w.getPOSTag().startsWith("RB"))
				advs.add(w);
		}
		return advs;
	}
	
	public ArrayList<Word> getNouns(){
		ArrayList<Word> advs = new ArrayList<Word>();

		for(Word w : _mainNGram) {
			if(w.getPOSTag().matches("NNS?"))
				advs.add(w);
		}
		return advs;
	}
	
	public ArrayList<Word> getProperNouns(){
		ArrayList<Word> propNouns = new ArrayList<Word>();
		
		for(Word w : _mainNGram){
			if(w.getPOSTag().matches("NNPS?")){
				propNouns.add(w);
			}
		}
		return propNouns;
	}
}
