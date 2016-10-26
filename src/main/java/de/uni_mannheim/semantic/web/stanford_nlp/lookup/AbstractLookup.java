package de.uni_mannheim.semantic.web.stanford_nlp.lookup;

import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;

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
        int max = 5;

        for(int size=max;size>=1;size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i,i+size);

                String term = getSearchTermFromNGram(ngramWords.toArray(new String[] {}));

                if(term != null) {
                    E res = findByTitle(term);
                    addVariable(i,i+size-1,res);
                    return res;
                }
            }
        }
        return null;
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
