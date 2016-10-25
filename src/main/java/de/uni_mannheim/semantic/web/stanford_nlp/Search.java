package de.uni_mannheim.semantic.web.stanford_nlp;


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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

public class Search {

    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;
    private IndexWriter writer;
    private IndexReader reader;
    private IndexSearcher searcher;

    public TopDocs search(String term) throws IOException {

        Query fuzzyQuery = new FuzzyQuery(new Term("title", term), 2);

        // 3. search
        int hitsPerPage = 10;
        reader = DirectoryReader.open(index);
        searcher = new IndexSearcher(reader);

        TopDocs docs = searcher.search(fuzzyQuery, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            System.out.println((i + 1) + ". " + hits[i].score + "\t" + d.get("yago_name") + "\t" + d.get("title"));
        }

        return docs;
    }

    public Search() throws IOException, ParseException {
        analyzer = new StandardAnalyzer();

        // 1. create the index
        index = new RAMDirectory();

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

    public void closeWriter() throws IOException {
        writer.close();
    }
}