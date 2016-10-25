package de.uni_mannheim.semantic.web.nlp.finders;

public class DBNERFinderResult {

	private String endPage;
	private String redirectionStartPage;

	public DBNERFinderResult(String x, String y) {
		endPage = x;
		redirectionStartPage = y;
	}

	public String getSimilarityRelevantPage() {
		return redirectionStartPage != null ? redirectionStartPage : endPage;
	}

	public String getSimilarityRelevantCleanedPage() {
		return this.getSimilarityRelevantPage().replaceAll("http://dbpedia.org/resource/", "");
	}

    public String getPage() {
        return endPage;
    }
}
