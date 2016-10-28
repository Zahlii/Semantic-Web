package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

import java.util.*;


public class DBPediaResourceLookup {


    private Comparator<LookupResult> comp;

    private StanfordSentence sentence;
    private List<String> currentTokens;
    private HashMap<String, LookupResult> lookupResults = new HashMap<>();

    private String currentText;

    public DBPediaResourceLookup(StanfordSentence sentence) {
        this.sentence = sentence;
        this.currentText = sentence.getCleanedText();
        this.comp = (o1, o2) -> {

            double c1 = o1.getCertainty();
            double c2 = o2.getCertainty();

            if(c1==c2)
                return 0;

            return c1 > c2 ? -1 : 1;
        };
        constructTokens();
    }

    public void constructTokens() {
        this.currentText = sentence.getCleanedText();
        this.currentTokens = Arrays.asList(this.currentText.split(" "));
    }

    private LookupResult findByTitle(String title) {

        List<LookupResult> candidates = new ArrayList<>();

        candidates.addAll(prefixLookup(title));
        candidates.add(sparqlLookup(title));
        candidates.addAll(keywordLookup(title));
        candidates.addAll(spotlightLookup(title));

        return voteFind(candidates);
    }

    private LookupResult voteFind(List<LookupResult> results) {
        HashMap<String,LookupResult> calc = new HashMap<>();
        for(LookupResult r : results) {
            String resource = r.getResult();
            if(!calc.containsKey(resource)) {
                calc.put(resource, r);
            } else {
                LookupResult sum = calc.get(resource);

                sum.setCertainty(r.getCertainty()+sum.getCertainty());
            }
        }

        List<LookupResult> all = new ArrayList<>(calc.values());
        all.sort(comp);

        return all.get(0);
    }


    public List<LookupResult> findAllIn(int start, int end) {
        int max = 5;

        List<LookupResult> candidates = new ArrayList<>();
        ArrayList<List<String>> ngrams = new ArrayList<>();

        for (int size = max; size >= 1; size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i, i + size);
                ngrams.add(ngramWords);
            }
        }

        for(List<String> ngram : ngrams) {
            String term = getSearchTermFromNGram(ngram);

            if(term != null) {
                LookupResult r = findByTitle(term);
                candidates.add(r);
            }
        }

        candidates.sort(comp);

        return candidates;
    }

    private List<LookupResult> prefixLookup(String title) {
        return DBPediaWrapper.lookupPrefixSearch(title);
    }

    private List<LookupResult> keywordLookup(String title) {
        return DBPediaWrapper.lookupKeywordSearch(title);
    }

    private LookupResult sparqlLookup(String title) {
        return DBPediaWrapper.sparqlSearch(title);
    }

    private List<LookupResult> spotlightLookup(String title) {
        return DBPediaWrapper.spotlightLookupSearch(title);
    }

    private String getSearchTermFromNGram(List<String> words) {
        int s = words.size();

        String first = words.get(0);

        if(first.contains("Variable"))
            return null;

        boolean startsWithThe = first.equals("the");

        StringBuilder search = new StringBuilder();
        String append = "";

        if(startsWithThe && s==1)
            return null;

        String last = words.get(s-1);

        for(String w : words) {
            if(!isValidPart(w)) {
                if(isClassification(w)) {
                    append = "("+w.replace("movie","film")+")";
                } else {
                    return null;
                }
            } else {
                search.append(w).append(" ");
            }
        }

        if(!isValidFirstPart(first))
            return null;

        if(!isValidLastPart(last))
            return null;

        return search.append(append).toString().replaceAll("^the","").trim();
    }

    private boolean isValidPart(String text) {
        return text.matches("(of|in)") || isValidLastPart(text) || isValidFirstPart(text);
    }

    private boolean isValidLastPart(String text) {
        return text.matches("^[A-Z].*") || text.matches("\\d+");
    }

    private boolean isClassification(String text) {
        return text.matches("(movie|book|film)");
    }

    private boolean isValidFirstPart(String text) {
        return (text.matches("^[A-Z].*") || text.matches("the"));
    }

    public String getText() {
        return currentText;
    }

    public HashMap<String, LookupResult> getResults() {
        return lookupResults;
    }

    public List<LookupResult> findAll() {

        return findAllIn(0,this.currentTokens.size()-1);
    }
}
