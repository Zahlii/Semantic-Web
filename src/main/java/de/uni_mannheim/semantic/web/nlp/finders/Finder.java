package de.uni_mannheim.semantic.web.nlp.finders;

import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.nlp.NGram;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;
import opennlp.tools.util.Span;

public abstract class Finder {
	protected Sentence _sentence;
	protected NGram _mainNGram;
	protected String _originalText;
	protected List<NGram> _ngrams = new ArrayList<NGram>();

	public Finder(Sentence s) {
		this._sentence = s;
		this._originalText = s.getText();
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
}
