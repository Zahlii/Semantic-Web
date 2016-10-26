package de.uni_mannheim.semantic.web.stanford_nlp.model.query;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;

public class QModel extends QVariable {

	private StringBuilder _appendOuter = new StringBuilder();

	public QModel() {
		super("search");

	}

	public QModel orderBy(QVariable var, SortOrder order) {
		String o = order == SortOrder.DESC ? "desc" : "asc";
		_appendOuter.append("ORDER BY " + o + "(" + var.asString() + ")\r\n");
		return this;
	}

	public QModel orderBy(SortOrder order) {
		return orderBy(this, order);
	}

	public ResultSet exec() {
		String text = "SELECT * WHERE {\r\n" + this.listRestrictions() + "}";
		text += "\r\n" + _appendOuter.toString();

		System.out.println(text);
		return DBPediaWrapper.query(text);
	}

	public static void main(String[] args) {
		QModel m = new QModel();

		QVariable capital = new QVariable("capital");
		capital.filterByType("dbo:Settlement").filterBy("regex", "L.*");

		m.filterByType("yago:WikicatAfricanCountries").filterByVariableProperty("dbo:capital", capital)
				.filterBy("regex", "A.*");

		m.orderBy(SortOrder.ASC);

		ResultSet s = m.exec();
		QuerySolution r;
		while (s.hasNext()) {
			r = s.next();
			System.out.println(r.toString());
		}

	}
}
