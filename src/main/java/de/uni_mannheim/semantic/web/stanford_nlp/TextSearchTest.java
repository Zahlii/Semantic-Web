package de.uni_mannheim.semantic.web.stanford_nlp;

import org.apache.lucene.queryparser.classic.ParseException;

import java.io.IOException;

public class TextSearchTest {
    public static void main(String[] args) throws IOException, ParseException {
        TextSearch s = new TextSearch("yago");

        s.search("German~ cities~");

    }
}
