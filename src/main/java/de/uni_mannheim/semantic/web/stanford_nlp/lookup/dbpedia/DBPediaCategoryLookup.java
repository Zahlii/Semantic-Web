package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.IndexedTextSearch;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class DBPediaCategoryLookup  {

    private StanfordSentence sentence;
    private static IndexedTextSearch search = new IndexedTextSearch("yago");

    public DBPediaCategoryLookup(StanfordSentence sentence) {
        this.sentence = sentence;
    }

    public List<LookupResult> findAllByTitle(String title) {
        LookupResult r;
        try {
            List<LookupResult> res = search.search(title);
            return res;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected LookupResult findByTitle(String title) {
        LookupResult r;
        try {
            List<LookupResult> res = search.search(title);
            r = res.size() > 0 ? res.get(0) : new LookupResult(LookupStatus.NOT_FOUND);
        } catch(Exception e) {
            e.printStackTrace();
            return new LookupResult(LookupStatus.FAIL);
        }
        return r;
    }
    protected String getSearchTermFromNGram(String[] words) {
        return StringUtils.join(words,"~ ");
    }
}
