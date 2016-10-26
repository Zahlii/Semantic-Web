package de.uni_mannheim.semantic.web.stanford_nlp.model.query;

public class QueryProperty implements QueryItem {
	private String _property;
	private QueryItem _subject;
	private QueryItem _object;

	public QueryProperty(QVariable start, String property, String literal) {
		this._subject = start;
		this._property = property;
		this._object = new QueryLiteral(literal);
	}

	public QueryProperty(QVariable start, String property, QVariable end) {
		this._subject = start;
		this._property = property;
		this._object = end;
	}

	@Override
	public String asString() {
		String text = _subject.asString() + " " + _property + " " + _object.asString() + " .";

		if (_object instanceof QVariable)
			text += "\r\n" + ((QVariable) _object).listRestrictions();

		return text;
	}

}
