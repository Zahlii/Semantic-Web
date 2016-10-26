package de.uni_mannheim.semantic.web.stanford_nlp.helpers.text;


import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.*;

public class IndexedTextWriter {

    private String name;
    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;
    private IndexWriter writer;

    public IndexedTextWriter(String name) throws IOException, ParseException {
        this.name = name;

        analyzer = new StandardAnalyzer();

        index = new SimpleFSDirectory(Paths.get("./data/lucene/"+name));
        config = new IndexWriterConfig(analyzer);
        writer = new IndexWriter(index, config);
    }

    public void addTerm(String title, String indexed_field) throws IOException {

        title = title.trim();
        indexed_field = indexed_field.trim();

        //System.out.println(title+ " | " + indexed_field);

        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("yago_name", indexed_field, Field.Store.YES));
        writer.addDocument(doc);
    }

    public void saveIndex() throws IOException {
        writer.close();
    }
}