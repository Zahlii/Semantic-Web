package de.uni_mannheim.semantic.web.crawl.model;

public class EvaluationResult {
	private double fmeasure;
	private double precision;
	private double recall;
	private boolean answered;
	
	public EvaluationResult(double fmeasure, double precision, double recall, boolean answered) {
		this.setFmeasure(fmeasure);
		this.setPrecision(precision);
		this.setRecall(recall);
		this.setAnswered(answered);
	}

	public double getFmeasure() {
		return fmeasure;
	}

	public void setFmeasure(double fmeasure) {
		this.fmeasure = fmeasure;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}

	public double getRecall() {
		return recall;
	}

	public void setRecall(double recall) {
		this.recall = recall;
	}

	public boolean isAnswered() {
		return answered;
	}

	public void setAnswered(boolean answered) {
		this.answered = answered;
	}
}
