package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.IndexedTextSearch;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.AbstractLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.TopDocs;

import java.util.ArrayList;
import java.util.List;

public class DBPediaCategoryLookup extends AbstractLookup {

    private static IndexedTextSearch search = new IndexedTextSearch("yago");

    public DBPediaCategoryLookup(StanfordSentence sentence) {
        super(sentence);
    }

    public List<LookupResult<String>> findAllByTitle(String title) {
        LookupResult r;
        try {
            List<LookupResult<String>> res = search.search(title);
            return res;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected LookupResult findByTitle(String title) {
        LookupResult r;
        try {
            List<LookupResult<String>> res = search.search(title);
            r = res.size() > 0 ? res.get(0) : new LookupResult(LookupStatus.NOT_FOUND);
        } catch(Exception e) {
            e.printStackTrace();
            return new LookupResult(LookupStatus.FAIL);
        }
        return r;
    }

    @Override
    protected String getSearchTermFromNGram(String[] words) {
        return StringUtils.join(words,"~ ");
    }
}
