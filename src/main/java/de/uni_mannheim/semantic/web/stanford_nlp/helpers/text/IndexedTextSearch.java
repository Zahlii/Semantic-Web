package de.uni_mannheim.semantic.web.stanford_nlp.helpers.text;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class IndexedTextSearch {

    private String name;
    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser qp;

    public TopDocs search(String term) throws IOException, ParseException {

        Query q = qp.parse(term);

        // 3. search
        int hitsPerPage = 100;
        reader = DirectoryReader.open(index);
        searcher = new IndexSearcher(reader);


        long startTime = System.nanoTime();


        TopDocs docs = searcher.search(q, hitsPerPage);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime)/1000000;

        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits ["+duration+"ms].");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);

            System.out.println((i + 1) + ". " + hits[i].score + "\t" + d.get("yago_name") + "\t" + d.get("title"));
        }

        return docs;
    }

    public IndexedTextSearch(String name) throws IOException, ParseException {
        this.name = name;
        analyzer = new StandardAnalyzer();

        index = new SimpleFSDirectory(Paths.get("./data/lucene/"+name));
        qp = new QueryParser("title",analyzer);
        qp.setDefaultOperator(QueryParser.Operator.AND);
    }
}