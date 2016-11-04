package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.IndexedTextSearch;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.NGramLookup;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBPediaCategoryLookup extends NGramLookup {

    private static IndexedTextSearch search = new IndexedTextSearch("yago");

    public DBPediaCategoryLookup(StanfordSentence sentence) {
        super(sentence);
    }

    @Override
    protected String getSearchTermFromNGram(List<String> words) {
        return StringUtils.join(words,"~ ") + "~";
    }

    @Override
    protected List<LookupResult> performLookupInternal(List<String> words, String term) {
        List<LookupResult> res = new ArrayList<>();

        try {
            res = search.search(term);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return res;
    }
}
