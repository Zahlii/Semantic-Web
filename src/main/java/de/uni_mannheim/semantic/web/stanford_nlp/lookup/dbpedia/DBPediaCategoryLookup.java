package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.StanfordNLP;
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

        StringBuilder b = new StringBuilder();
        for(String w : words) {
            if(w.contains("ariable"))
                return null;

            String s = StanfordNLP.getStem(w);
            if(s.length()>3) {
                s = s.substring(0, s.length() - 1) + "* ";
            } else {
                s= s +" ";
            }
            b.append(s);
        }
        return b.toString();
    }

    @Override
    protected List<LookupResult> performLookupInternal(List<String> words, String term) {
        List<LookupResult> res = new ArrayList<>();

        try {
            res = search.search(StringUtils.join(words," "),term);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return res;
    }
}
