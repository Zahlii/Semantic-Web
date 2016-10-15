package de.uni_mannheim.semantic.web.query;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import de.uni_mannheim.semantic.web.info.DBPedia;

public class QModel extends QVariable {
	
	private StringBuilder _appendOuter = new StringBuilder();
	
	public QModel() {
		super("search");

	}
		
	public QModel orderBy(QVariable var, SortOrder order) {
		String o = order == SortOrder.DESC ? "desc" : "asc";
		_appendOuter.append("ORDER BY "+o+"("+var.asString()+")\r\n");
		return this;
	}
	
	public QModel orderBy(SortOrder order) {
		return orderBy(this,order);
	}
	
	public ResultSet exec() {
		String text = "SELECT * WHERE {\r\n" + this.listRestrictions() + "}";
		text += "\r\n" + _appendOuter.toString();
		
		System.out.println(text);
		return DBPedia.query(text);
	}
	public static void main(String[] args) {
		QModel m = new QModel();
		
		QVariable capital = new QVariable("capital");
		capital.filterBy("regex", "L.*");
		
		m.filterByResourceProperty("rdf:type", "yago:WikicatAfricanCountries")
		 .filterByVariableProperty("dbo:capital", capital)
		 .filterBy("regex","A.*");
		
		m.orderBy(SortOrder.ASC);
		
		ResultSet s = m.exec();
		QuerySolution r;
		while(s.hasNext()) {
			r = s.next();
			System.out.println(r.toString());
		}
		
	}
}
