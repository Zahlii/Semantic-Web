package de.uni_mannheim.semantic.web.stanford_nlp.lookup;


import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public abstract class NGramLookup {

    public final Comparator<LookupResult> lookupResultComparator;
    private StanfordSentence sentence;
    private List<String> currentTokens;
    private HashMap<String, LookupResult> lookupResults = new HashMap<>();

    private String currentText;

    private String varPrefix;

    public NGramLookup(StanfordSentence sentence, String varPrefix) {
        this.varPrefix = varPrefix;

        this.lookupResultComparator = (o1, o2) -> {

            double c1 = o1.getCertainty();
            double c2 = o2.getCertainty();

            if(c1==c2)
                return 0;

            return c1 > c2 ? -1 : 1;
        };
        this.sentence = sentence;
        this.currentText = sentence.getCleanedText();
    }

    protected List<List<String>> getNGrams() {
        constructTokens();

        int max = 5;

        int start = 0;
        int end = this.currentTokens.size()-1;

        ArrayList<List<String>> ngrams = new ArrayList<>();

        for (int size = max; size >= 1; size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i, i + size);
                ngrams.add(ngramWords);
            }
        }

        return ngrams;
    }

    public void constructTokens() {
        this.currentTokens = sentence.tokenize(this.currentText);
    }

    public String getText() {
        return currentText;
    }

    public HashMap<String, LookupResult> getResults() {
        return lookupResults;
    }

    protected abstract String getSearchTermFromNGram(List<String> words);

    protected abstract List<LookupResult> performLookupInternal(List<String> words,String term);

    public List<LookupResult> findAll() {
        List<LookupResult> res = new ArrayList<>();
        for(int i=0;i<2;i++) {
            LookupResult next = findNext();
            res.add(next);
            if(!next.found())
                break;
        }

        return res;
    }

    public LookupResult findNext() {
        List<List<String>> ngrams = getNGrams();

        for(List<String> words : ngrams) {
            LookupResult r = findAndReplaceOneIn(words);

            if(r.found())
                return r;
        }

        return new LookupResult(LookupStatus.NOT_FOUND);
    }

    public LookupResult findAndReplaceOneIn(List<String> words) {
        LookupResult res = findOneIn(words);
        if(res.found()) {
            acceptSolution(res);
        }
        return res;
    }

    public LookupResult findOneIn(String word) {
        List<String> s = new ArrayList<String>();
        s.add(word);
        return findOneIn(s);
    }

    private LookupResult findOneIn(List<String> words) {
        String term = getSearchTermFromNGram(words);

        if(term == null)
            return new LookupResult(LookupStatus.NOT_FOUND);

        List<LookupResult> candidates = performLookupInternal(words, term);


        int s = candidates.size();

        if(s == 0)
            return new LookupResult(LookupStatus.NOT_FOUND);
        else if(s == 1 && candidates.get(0) != null)
            return candidates.get(0);

        return choseBestSolution(candidates);
    }

    private void acceptSolution(LookupResult result) {
        if(!result.found())
            return;

        String varName = varPrefix+lookupResults.size();
        this.currentText = this.currentText.replace(result.getSearchedTitle(),varName);
        System.out.println(this.getClass().getSimpleName() + " - Mapped " + result.getSearchedTitle() +" to " + result.getResult());

        result.setVarName(varName);
        lookupResults.put(varName,result);
    }

    private LookupResult choseBestSolution(List<LookupResult> results) {
        HashMap<String,LookupResult> calc = new HashMap<>();

        for(LookupResult r : results) {
            if(r == null || !r.found())
                continue;

            String resource = r.getResult();
            if(!calc.containsKey(resource)) {
                calc.put(resource, r);
            } else {
                LookupResult sum = calc.get(resource);

                sum.setCertainty(r.getCertainty()+sum.getCertainty());
            }
        }

        List<LookupResult> all = new ArrayList<>(calc.values());
        all.sort(this.lookupResultComparator);

        if(all.size() == 0)
            return new LookupResult(LookupStatus.NOT_FOUND);

        return all.get(0);
    }


}
