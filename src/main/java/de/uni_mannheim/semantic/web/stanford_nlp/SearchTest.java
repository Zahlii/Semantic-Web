package de.uni_mannheim.semantic.web.stanford_nlp;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class SearchTest {
    public static void main(String[] args) throws IOException, ParseException {
        Search s = new Search();

        s.search("Politician");

    }
}
