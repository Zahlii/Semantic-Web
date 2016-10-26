package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

public class DBPediaSPARQLLookupResult {

	private String endPage;
	private String redirectionStartPage;

	public DBPediaSPARQLLookupResult(String x, String y) {
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
