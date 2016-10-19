package de.uni_mannheim.semantic.web.nlp.interpretation;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.nlp.NGram;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;
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
		Word newToken = mergeTokens(g.getStartTokenIndex(), g.getEndTokenIndex());
		newToken.setPOSTag("ENTITY");
		newToken.setResource(resource);
		newToken.setProbability(prob);
	}

	protected Word mergeTokens(int start, int end) {

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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Word _token : _mainNGram) {
			sb.append(_token.toString()).append(",");
		}

		return sb.toString();
	}

	public Word[] getTokens() {
		return _mainNGram.toArray(new Word[_mainNGram.size()]);
	}

	public Span[] getSpans() {
		Span[] s = new Span[_mainNGram.size()];
		int i = 0;
		for (Word t : getTokens()) {
			s[i++] = t.getSpan();
		}

		return s;
	}
}
