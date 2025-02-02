package de.uni_mannheim.semantic.web.crawl.run_once;

import java.io.*;
import java.sql.SQLException;
import java.util.HashSet;

import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.IndexedTextWriter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.TextHelper;
import org.apache.lucene.queryparser.classic.ParseException;

public class YagoCategoryCrawler {

    public static void main(String[] args) throws IOException, SQLException, ParseException {
        Model model = ModelFactory.createDefaultModel();
        File f = new File("D:\\Downloads\\yagoTypes.ttl");


        //IndexedTextWriter db = new IndexedTextWriter("yago");

        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF8"));

        // wikicat_Moths_of_New_Zealand => WikicatMothsOfNewZealand
        // wordnet_road_104096066 => Road104096066

        HashSet<String> items = new HashSet<String>();

        String l = null;

        int i = 0;

        while ((l = in.readLine()) != null) {
            i++;

            //if(i>1000)
            //	break;

            if (!l.contains("rdf:type"))
                continue;

            if (l.contains("wordnet_")) {
                l = l.replaceAll("wordnet_", "");
            }

            String[] parts = l.split("\\s+");
            String type = parts[2].replace("<", "").replaceAll(">", "");

            String[] qParts = type.split("_");

            StringBuilder term = new StringBuilder();

            for (String p : qParts) {
                p = TextHelper.capitalize(p);
                term.append(p).append(" ");
            }

            items.add(term.toString().trim());
        }

        System.out.println(items.size());

        try (FileWriter fw = new FileWriter("categories.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            for (String x : items) {
                out.println(x.replace("Wikicat", "").trim() + " | " + x.replace(" ", ""));
            }
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }


        //db.saveIndex();

        //db.search("German");
    }
}
