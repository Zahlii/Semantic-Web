package de.uni_mannheim.semantic.web.stanford_nlp.model.query;

public class QueryLiteral implements QueryItem {
	private String _literal;

	public QueryLiteral(String _text) {
		this._literal = _text;
	}

	@Override
	public String asString() {
		return _literal;
	}
}
