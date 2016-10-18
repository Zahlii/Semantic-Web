package de.uni_mannheim.semantic.web.nlp;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.Span;

public class NGram extends ArrayList<Token> implements Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4840688175789406794L;
	private String _originalText;

	public NGram(List<Token> subList) {
		super(subList);
		_originalText = getText();
	}

	// Clone it
	public NGram(NGram other) {
		this(new ArrayList<Token>(other.size()));
		_originalText = other.getText();
		for (Token t : other) {
			this.add(new Token(t, _originalText));
		}
	}

	public int getStartTokenIndex() {
		return this.get(0).getIndex();
	}

	public int getEndTokenIndex() {
		return this.get(this.size() - 1).getIndex();
	}

	public Span getSpan() {
		return new Span(this.get(0).getSpan().getStart(), this.get(this.size() - 1).getSpan().getEnd());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Token _token : this) {
			sb.append(_token.toString()).append(",");
		}

		return sb.toString();
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (Token _token : this) {
			sb.append(_token.getText()).append(" ");
		}

		return sb.toString().trim();
	}

	public String getStemmedText() {
		StringBuilder sb = new StringBuilder();
		for (Token _token : this) {
			sb.append(_token.getStem()).append(" ");
		}

		return sb.toString().trim();
	}

	public List<NGram> getUpToNGrams(int n) {
		List<NGram> ret = new ArrayList<NGram>();
		for (int i = n; i >= 0; i--) {
			ret.addAll(getNGrams(i));
		}
		return ret;
	}

	public List<NGram> getNGrams(int n) {
		List<NGram> ret = new ArrayList<NGram>();
		for (int i = 0; i < this.size() - n + 1; i++) {
			ret.add(new NGram(this.subList(i, i + n)));
		}
		return ret;
	}

}
