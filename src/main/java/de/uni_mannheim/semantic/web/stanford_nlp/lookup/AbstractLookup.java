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
    private HashMap<String,E> lookupResults = new HashMap<>();

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

    protected void addVariable(int start, int end,E result) {
        String varName = "Variable"+lookupResults.size();

        StringBuilder search = new StringBuilder();

        for(int i=start;i<=end;i++) {
            search.append(currentTokens.get(i)).append(" ");
        }

        String s = search.toString().trim();
        this.currentText = this.currentText.replace(s,varName);
        lookupResults.put(varName,result);

        System.out.println("Setting " + search + " to " + result.toString());

        constructTokens();
    }

    public List<E> findAll() {
        List<E> r = new ArrayList<E>();

        for(int i=0;i<2;i++) {
            E res = findOneIn(0,this.currentTokens.size()-1);
            if(res.getStatus() == LookupStatus.NOT_FOUND) {
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

        E resNgram = null;
        for(int size=max;size>=1;size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i,i+size);

                String term = getSearchTermFromNGram(ngramWords.toArray(new String[] {}));

                if(term != null) {
                    E res = findByTitle(term);
                    addVariable(i,i+size-1,res);
//                    list.add(res);
                    resNgram = res;
                    break;
//                    return res;
                }
            }
            if(resNgram != null){
            	break;
            }
        }
        
        E resLookup = null;
        for(int size=max;size>=1;size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i,i+size);
                
                String charSeq = "";
                for(int k=0; k<ngramWords.size(); k++){
                	charSeq+=ngramWords.get(k)+" ";
                }

                
                ArrayList<LookupResult> lookupList = DBPediaResourceCrawler.findDBPediaResource(charSeq);
                if(lookupList.size()>0){
                	resLookup = (E) lookupList.get(0);
                	break;
                }
            }
            if(resLookup != null){
            	break;
            }
        }
        
        
        ArrayList<LookupResult> spotlightList = DBPediaSpotlightNER.findEntities(sentence.getCleanedText());
        
        int size = 0;
        LookupResult resSpotlight = null;
        for(int i=0; i<spotlightList.size(); i++){
        	int tmpSize = spotlightList.get(i).getSearchedTitle().split(" ").length;
        	if(tmpSize > size){
        		size = tmpSize; 
        		resSpotlight = spotlightList.get(i);
        	}
        }
        
        E winner = (E) resSpotlight;
        
        System.out.println("NGram: " + resNgram.toString());
        System.out.println("Lookup: " + resLookup.toString());
        System.out.println("Spotlight: " + resSpotlight.toString());
        if(resLookup != null && resNgram != null){
	        if(resLookup.getResultTitle().equals(resNgram.getResultTitle())){
	        	winner = resLookup;
	        }
        }
        if(resLookup != null && resSpotlight != null){
	        if(resLookup.getResultTitle().equals(resSpotlight.getResultTitle())){
	        	winner = resLookup;
	        }
        }
        if(resSpotlight != null && resNgram != null){
	        if(resSpotlight.getResultTitle().equals(resNgram.getResultTitle())){
	        	winner = (E) resSpotlight;
	        }
        }
        
        System.out.println("Winner: " +  winner);
        return winner;
    }

    protected abstract E findByTitle(String title);

    protected abstract String getSearchTermFromNGram(String[] words);

    public String getText() {
        return currentText;
    }

    public HashMap<String,E> getResults() {
        return lookupResults;
    }
}
