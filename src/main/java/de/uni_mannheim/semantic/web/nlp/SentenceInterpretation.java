package de.uni_mannheim.semantic.web.nlp;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;

public abstract class SentenceInterpretation {
	protected Sentence _sentence;
	protected NGram _mainNGram;
	protected String _originalText;
	protected List<NGram> _ngrams = new ArrayList<NGram>();

	public SentenceInterpretation(Sentence s) {
		this._sentence = s;
		this._originalText = s.getText();
		this._mainNGram = new NGram(this._sentence.getMainNGram());

		interpret();
	}

	protected abstract boolean isCandidateNGram(NGram tokens);

	public abstract void interpret();

	protected void mergeNGramEntity(NGram g, String resource, double prob) {
		Token newToken = mergeTokens(g.getStartTokenIndex(), g.getEndTokenIndex());
		newToken.setPOSTag("ENTITY");
		newToken.setResource(resource);
		newToken.setProbability(prob);
	}

	protected Token mergeTokens(int start, int end) {

		Token n = _mainNGram.get(start);
		if (end <= start)
			return n;

		for (int i = start + 1; i <= end; i++) {
			n = n.mergeWith(_mainNGram.get(i));
		}

		_mainNGram.subList(start, end + 1).clear();
		_mainNGram.add(start, n);
		int i = 0;
		for (Token t : _mainNGram) {
			t.setIndex(i++);
		}

		return n;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Token _token : _mainNGram) {
			sb.append(_token.toString()).append(",");
		}

		return sb.toString();
	}

	public Token[] getTokens() {
		return _mainNGram.toArray(new Token[_mainNGram.size()]);
	}

	public Span[] getSpans() {
		Span[] s = new Span[_mainNGram.size()];
		int i = 0;
		for (Token t : getTokens()) {
			s[i++] = t.getSpan();
		}

		return s;
	}
}
