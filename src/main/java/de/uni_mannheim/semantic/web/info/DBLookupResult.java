package de.uni_mannheim.semantic.web.info;

public class DBLookupResult {

	public String endPage;
	public String redirectionStartPage;

	public DBLookupResult(String x, String y) {
		endPage = x;
		redirectionStartPage = y;
	}

	public String getSimilarityRelevantPage() {
		return redirectionStartPage != null ? redirectionStartPage : endPage;
	}

	public String getSimilarityRelevantCleanedPage() {
		return this.getSimilarityRelevantPage().replaceAll("http://dbpedia.org/resource/", "");
	}

}
