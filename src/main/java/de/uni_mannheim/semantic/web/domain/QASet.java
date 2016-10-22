package de.uni_mannheim.semantic.web.domain;

public class QASet {
	private Question question;
	private Answer answer;
	private boolean answerable;

	public QASet(Question question, Answer answer, boolean answerable){
		this.setQuestion(question);
		this.setAnswer(answer);
		this.setAnswerable(answerable);
	}

	public Question getQuestion() {
		return question;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public Answer getAnswer() {
		return answer;
	}

	public void setAnswer(Answer answer) {
		this.answer = answer;
	}

	public boolean isAnswerable() {
		return answerable;
	}

	public void setAnswerable(boolean answerable) {
		this.answerable = answerable;
	}
	
}
