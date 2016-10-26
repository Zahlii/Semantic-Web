package de.uni_mannheim.semantic.web.stanford_nlp.model;

public class QASet {
	private Question question;
	private ExpectedAnswer expectedAnswer;
	private boolean answerable;

	public QASet(Question question, ExpectedAnswer expectedAnswer, boolean answerable){
		this.setQuestion(question);
		this.setExpectedAnswer(expectedAnswer);
		this.setAnswerable(answerable);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public ExpectedAnswer getExpectedAnswer() {
		return expectedAnswer;
	}

	public void setExpectedAnswer(ExpectedAnswer expectedAnswer) {
		this.expectedAnswer = expectedAnswer;
	}

	public boolean isAnswerable() {
		return answerable;
	}

	public void setAnswerable(boolean answerable) {
		this.answerable = answerable;
	}
	
}
