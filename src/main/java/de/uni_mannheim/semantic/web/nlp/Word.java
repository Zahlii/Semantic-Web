package de.uni_mannheim.semantic.web.nlp;

import java.util.Arrays;
import java.util.List;
import de.uni_mannheim.semantic.web.helpers.TextHelper;
import opennlp.tools.util.Span;

public class Word {

	public Span getSpan() {
		return _span;
	}

	public void setSpan(Span _span) {
		this._span = _span;
		this._start = _span.getStart();
		this._end = _span.getEnd();
	}

	public String getText() {
		return _text;
	}

	public void setText(String _text) {
		this._text = _text;
	}

	public String getStem() {
		return _stem;
	}

	public void setStem(String _stem) {
		this._stem = _stem;
	}

	public String getPOSTag() {
		return _posTag;
	}

	public void setPOSTag(String _type) {
		this._posTag = _type;
	}

	public void setIndex(int i) {
		this._index = i;
	}

	public int getIndex() {
		return this._index;
	}

	public String getResource() {
		return _resource;
	}

	public void setResource(String _resource) {
		this._resource = _resource;
	}

	private String _sentence;
	private Span _span;
	private String _text;
	private String _stem = "";
	private String _posTag = "";
	private String _resource = "";
	private int _start;
	private int _end;
	private int _index;
	private double _probability = 0;

	public Word(Span span, String text, int index) {
		this._index = index;
		this.setSpan(span);
		setSentence(text);
	}

	// Clone it
	public Word(Word t, String completeText) {
		this(t.getSpan(), completeText, t.getIndex());
		setPOSTag(t.getPOSTag());
		setResource(t.getResource());
		setStem(t.getStem());
		setProbability(getProbability());
	}

	public double getProbability() {
		return _probability;
	}

	public void setProbability(double _probability) {
		this._probability = _probability;
	}

	@Override
	public String toString() {
		return _text + "(" + _posTag + " from " + _stem + " = " + this._resource + " [" + this._probability + "])";
	}

	public Word mergeWith(Word nextToken) {
		Word ret = new Word(new Span(this._start, nextToken.getSpan().getEnd()), _sentence, this._index);
		return ret;
	}

	public boolean isCapitalized() {
		return TextHelper.isCapitalized(this._text);
	}

	public boolean isNumber() {
		return this._posTag.equals("CN");
	}

	public boolean isEntityPreposition() {
		List<String> words = Arrays.asList(new String[] { "of","in" });

		return words.contains(this._text);
	}

	public boolean isQuestionWord() {
		List<String> words = Arrays.asList(new String[] { "Which","What","Who","Give","Where","Is","In","To"});

		return words.contains(this._text);
	}

	public void setSentence(String text) {
		this._sentence = text;
		this.setText((String) getSpan().getCoveredText(text));
	}

    public boolean isThe() {
		return _text.equals("the");
    }
}
