package de.uni_mannheim.semantic.web.stanford_nlp.model.query;

import java.util.ArrayList;

public class QVariable implements QueryItem {
	private String _varName;
	private ArrayList<QueryProperty> _properties = new ArrayList<QueryProperty>();

	private StringBuilder _append = new StringBuilder();

	public QVariable(String varName) {
		this._varName = varName;
	}

	public QVariable filterByResourceProperty(String property, String literal) {
		_properties.add(new QueryProperty(this, property, literal));
		return this;
	}

	public QVariable filterByVariableProperty(String property, QVariable end) {
		_properties.add(new QueryProperty(this, property, end));
		return this;
	}

	public QVariable filterByType(String type) {
		_properties.add(new QueryProperty(this, "rdf:type", type));
		return this;
	}

	public QVariable filterBy(String filter, String arg) {
		_append.append("FILTER(" + filter + "(" + this.asString() + ",\"" + arg + "\"))\r\n");
		return this;
	}

	@Override
	public String asString() {
		return "?" + _varName;
	}

	public String listRestrictions() {
		StringBuilder sb = new StringBuilder();
		for (QueryProperty p : _properties) {
			sb.append(p.asString() + "\r\n");
		}
		sb.append(_append);
		return sb.toString();
	}

	public void filterByLiteralProperty(String property, String literal) {
		_properties.add(new QueryProperty(this, property, "\"" + literal + "\"@en"));
	}
}
