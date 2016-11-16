package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.NGramLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


public class DBPediaResourceLookup extends NGramLookup {


    public DBPediaResourceLookup(StanfordSentence sentence) {
        super(sentence,"Resource");
    }

    @Override
    protected String getSearchTermFromNGram(List<String> words) {
        int s = words.size();

        String first = words.get(0);



        for(String word : words) {
            if(word.contains("Resource"))
                return null;
        }

        boolean startsWithThe = first.equals("the");

        StringBuilder search = new StringBuilder();
        String append = "";

        if(startsWithThe && s==1)
            return null;

        if(first.contains("Resource"))
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

    @Override
    protected List<LookupResult> performLookupInternal(List<String> words, String title) {
        List<LookupResult> candidates = new ArrayList<>();

        candidates.addAll(prefixLookup(title));
//        candidates.add(sparqlLookup(title));
        candidates.addAll(keywordLookup(title));
        candidates.addAll(spotlightLookup(title));

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

    private boolean isValidPart(String text) {
        return text.matches("(of|in|and)") || isValidLastPart(text) || isValidFirstPart(text);
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
}
