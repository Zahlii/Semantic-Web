package de.uni_mannheim.semantic.web.stanford_nlp.model;

public class Question {

	private int id;
	private boolean onlydbo;
	private String answertype;
	private boolean aggregation;
	private boolean hybrid;
	private String question;
	
	public Question(){
		
	}
	
	public String getQuestionText() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public boolean isHybrid() {
		return hybrid;
	}
	public void setHybrid(boolean hybrid) {
		this.hybrid = hybrid;
	}
	public boolean isAggregation() {
		return aggregation;
	}
	public void setAggregation(boolean aggregation) {
		this.aggregation = aggregation;
	}
	public String getAnswertype() {
		return answertype;
	}
	public void setAnswertype(String answertype) {
		this.answertype = answertype;
	}
	public boolean isOnlydbo() {
		return onlydbo;
	}
	public void setOnlydbo(boolean onlydbo) {
		this.onlydbo = onlydbo;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

}
