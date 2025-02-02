package de.uni_mannheim.semantic.web.stanford_nlp.model;

import java.util.ArrayList;

public class ExpectedAnswer {

	private int questionId;
	private String query;
	private ArrayList<String> queryResult;
	
	public ExpectedAnswer(){
		
	}
	
	public int getQuestionId() {
		return questionId;
	}
	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public ArrayList<String> getQueryResult() {
		return queryResult;
	}
	public void setQueryResult(ArrayList<String> queryResult) {
		this.queryResult = queryResult;
	}
}
