package de.uni_mannheim.semantic.web.crawl.model;

import java.util.ArrayList;

public class GeneralQuery {
	private String generalQuery;
	private ArrayList<String> posTags;
	private ArrayList<String> preconditions = new ArrayList<>();
	
	public GeneralQuery() {
		// TODO Auto-generated constructor stub
	}

	public String getGeneralQuery() {
		return generalQuery;
	}

	public void setGeneralQuery(String generalQuery) {
		this.generalQuery = generalQuery;
	}

	public ArrayList<String> getPosTags() {
		return posTags;
	}

	public void setPosTags(ArrayList<String> posTags) {
		this.posTags = posTags;
	}

	public ArrayList<String> getPreconditions() {
		return preconditions;
	}

	public void setPreconditions(ArrayList<String> preconditions) {
		this.preconditions = preconditions;
	}
	
	public void addPrecondition(String precondition){
		this.preconditions.add(precondition);
	}
}
