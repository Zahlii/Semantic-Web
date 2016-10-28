package de.uni_mannheim.semantic.web.stanford_nlp.lookup;

import de.uni_mannheim.semantic.web.crawl.model.DBPediaResource;
import de.uni_mannheim.semantic.web.crawl.run_once.DBPediaResourceCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaSpotlightNER;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public abstract class AbstractLookup<E extends LookupResult> {

	private StanfordSentence sentence;
	private List<String> currentTokens;
	private HashMap<String, E> lookupResults = new HashMap<>();

	private String currentText;

	public AbstractLookup(StanfordSentence sentence) {
		this.sentence = sentence;
		this.currentText = sentence.getCleanedText();
		constructTokens();
	}

	public void constructTokens() {
		this.currentText = sentence.getCleanedText();
		this.currentTokens = Arrays.asList(this.currentText.split(" "));
	}

	protected void addVariable(int start, int end, E result) {
		String varName = "Variable" + lookupResults.size();

		StringBuilder search = new StringBuilder();

		for (int i = start; i <= end; i++) {
			search.append(currentTokens.get(i)).append(" ");
		}

		String s = search.toString().trim();
		this.currentText = this.currentText.replace(s, varName);
		lookupResults.put(varName, result);

		System.out.println("Setting " + search + " to " + result.toString());

		constructTokens();
	}

	public List<E> findAll() {
		List<E> r = new ArrayList<E>();

		for (int i = 0; i < 2; i++) {
			E res = findOneIn(0, this.currentTokens.size() - 1);
			if (res.getStatus() == LookupStatus.NOT_FOUND) {
				return r;
			} else {
				r.add(res);
			}
		}

		return r;
	}

	public E findOneIn(int start, int end) {
		ArrayList<LookupResult> list = new ArrayList<LookupResult>();
		int max = 5;

		//ngram NER
		E resNgram = null;
		for (int size = max; size >= 1; size--) {
			for (int i = start; i <= end - size + 1; i++) {
				List<String> ngramWords = currentTokens.subList(i, i + size);

				String term = getSearchTermFromNGram(ngramWords.toArray(new String[] {}));

				if (term != null) {
					E res = findByTitle(term);
					addVariable(i, i + size - 1, res);
					// list.add(res);
					resNgram = res;
					break;
					// return res;
				}
			}
			if (resNgram != null) {
				break;
			}
		}

		//dbpedia lookup NER
		E resLookup = null;
		ArrayList<LookupResult> lookups = new ArrayList<>();
		for (int size = max; size >= 1; size--) {
			for (int i = start; i <= end - size + 1; i++) {
				List<String> ngramWords = currentTokens.subList(i, i + size);

				String charSeq = "";
				for (int k = 0; k < ngramWords.size(); k++) {
					charSeq += ngramWords.get(k) + " ";
				}

				ArrayList<LookupResult> lookupList = DBPediaResourceCrawler.findDBPediaResource(charSeq);
				// is abbreviation?
				if (charSeq.matches("[A-Z]+") && lookupList.size() > 0) {
					resLookup = (E) lookupList.get(0);
					break;
				}

				lookups.addAll(lookupList);

			}
			if (resLookup != null) {
				break;
			}
		}

		if(resLookup == null){
			double certainty = 0;
			LookupResult lRes = null;
			if (lookups.size() > 0) {
				for (int k = 0; k < lookups.size(); k++) {
					double cert = lookups.get(k).getCertainty();
	
					// find the one with the highest certainty
					if (cert > certainty) {
						certainty = cert;
						lRes = lookups.get(k);
					}
				}
			}
	
			if (lRes != null) {
				resLookup = (E) lRes;
			}
		}

		//dbpedia spotlight NER
		ArrayList<LookupResult> spotlightList = DBPediaSpotlightNER.findEntities(sentence.getCleanedText());

		int size = 0;
		E resSpotlight = null;
		for (int i = 0; i < spotlightList.size(); i++)

		{
			int tmpSize = spotlightList.get(i).getSearchedTitle().split(" ").length;
			if (tmpSize > size) {
				size = tmpSize;
				resSpotlight = (E) spotlightList.get(i);
			}
		}

		E winner = resSpotlight;
		System.out.println("-------------------------------------------------------------------");
		System.out.println("NGram:     " + resNgram.toString());
		System.out.println("Lookup:    " + resLookup.toString());
		System.out.println("Spotlight: " + resSpotlight.toString());
		System.out.println("-------------------------------------------------------------------");
		if (resLookup != null && resNgram != null){
			if (resLookup.getResult().toString().equals(resNgram.getResult().toString())) {
				winner = resLookup;
			}
		}
		if (resLookup != null && resSpotlight != null){
			if (resLookup.getResult().toString().equals(resSpotlight.getResult().toString())) {
				winner = resLookup;
			}
		}
		if (resSpotlight != null && resNgram != null){
			if (resSpotlight.getResult().toString().equals(resNgram.getResult().toString())) {
				winner = (E) resSpotlight;
			}
		}
		
		if(resSpotlight != null && resLookup != null && resNgram != null){
			if(!resSpotlight.getResult().toString().equals(resLookup.getResult().toString()) 
					&& !resSpotlight.getResult().toString().equals(resNgram.getResult().toString())
					&& !resLookup.getResult().toString().equals(resNgram.getResult().toString())){
						double c1 = resNgram.getCertainty();
						double c2 = resLookup.getCertainty();
						double c3 = resSpotlight.getCertainty();
						
					    double maxC =  Math.max(Math.max(c1,c2),c3);
						
					    if(c1 == maxC){
					    	winner = resNgram;
					    }
					    if(c3 == maxC){
					    	winner = resSpotlight;
					    }
					    //usually the best one
					    if(c2 == maxC){
					    	winner = resLookup;
					    }
					}
		}

		System.out.println("Winner:    " + winner);
		return winner;

	}

	protected abstract E findByTitle(String title);

	protected abstract String getSearchTermFromNGram(String[] words);

	public String getText() {
		return currentText;
	}

	public HashMap<String, E> getResults() {
		return lookupResults;
	}
}
