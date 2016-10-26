package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.AbstractLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;


public class DBPediaResourceLookup extends AbstractLookup {
    
    public DBPediaResourceLookup(StanfordSentence sentence) {
        super(sentence);
    }

    @Override
    protected LookupResult<String> findByTitle(String title) {
        LookupResult<String> res = normalLookup(title);

        if(res.getStatus() != LookupStatus.FOUND) {
            return sparqlLookup(title);
        }

        return res;
    }


    private LookupResult<String> normalLookup(String title) {
        String found = DBPediaWrapper.lookupSearch(title);

        LookupResult<String> res;

        if(found != null) {
            String ownTitle = found.replace("_"," ").replace("http://dbpedia.org/resource/","");
            if(ownTitle.contains("("))
                ownTitle = ownTitle.substring(0,ownTitle.indexOf("(")-1);
            res = new LookupResult<>(title, ownTitle , found);
        } else {
            res = new LookupResult<>(LookupStatus.NOT_FOUND);
        }

        return res;
    }

    private LookupResult<String> sparqlLookup(String title) {
        DBPediaSPARQLLookupResult found = DBPediaWrapper.sparqlSearch(title);

        LookupResult<String> res;

        if(found != null) {
            res = new LookupResult<>(title, found.getSimilarityRelevantCleanedPage(), found.getPage());
        } else {
            res = new LookupResult<>(LookupStatus.NOT_FOUND);
        }

        return res;
    }

    @Override
    protected String getSearchTermFromNGram(String[] words) {
        int s = words.length;

        String first = words[0];

        if(first.contains("Variable"))
            return null;

        boolean startsWithThe = first.equals("the");

        StringBuilder search = new StringBuilder();
        String append = "";

        if(startsWithThe && s==1)
            return null;

        String last = words[s-1];

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
}
