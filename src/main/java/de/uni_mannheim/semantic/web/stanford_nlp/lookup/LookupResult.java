package de.uni_mannheim.semantic.web.stanford_nlp.lookup;


import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;

public class LookupResult {
    private String result;
    private LookupStatus status;
    private double certainty;
    private String resultTitle;
    private String searchedTitle;

    @Override
    public String toString() {
        return "LookupResult{" +
                "result=" + result +
                ", status=" + status +
                ", certainty=" + certainty +
                ", resultTitle='" + resultTitle + '\'' +
                ", searchedTitle='" + searchedTitle + '\'' +
                '}';
    }

    public LookupResult(String searchedTitle, String resultTitle, String result) {
        this.searchedTitle = searchedTitle;
        this.resultTitle = resultTitle;
        this.result = result;
        this.status = LookupStatus.FOUND;
        this.certainty = calculateCertainty();
    }

    protected double calculateCertainty() {
        return 1-Levenshtein.normalized(this.searchedTitle,this.resultTitle);
    }

    public LookupResult(LookupStatus status) {
        this.status = status;
        this.certainty = -1;
    }

    public String getResult() {
        return result;
    }

    public double getCertainty() {
        return certainty;
    }

    public String getResultTitle() {
        return resultTitle;
    }


    public void setStatus(LookupStatus status) {
        this.status = status;
    }

    public void setCertainty(double certainty) {
        this.certainty = certainty;
    }
    
    public String getSearchedTitle(){
    	return searchedTitle;
    }

    public boolean found() {
        return this.status == LookupStatus.FOUND;
    }
}
