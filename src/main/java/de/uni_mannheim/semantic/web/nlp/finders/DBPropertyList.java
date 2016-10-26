package de.uni_mannheim.semantic.web.nlp.finders;

import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.nlp.TextAnalyzer;
import de.uni_mannheim.semantic.web.nlp.Word;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;

public class DBPropertyList {
    private String _resource;
    private HashMap<String,List<String>> props = new HashMap<String,List<String>>();

    public DBPropertyList(String resource) {
        this._resource = resource;
    }

    public Map<String,List<String>> fetchProperties() {


        ResultSet s = DBPediaWrapper.query("SELECT ?p ?o WHERE {\n" +
                "{<"+this._resource+"> ?p ?o.}\n" +
                "UNION {?o ?p <"+this._resource+"> .}\n" +
                "}");

        while(s.hasNext()) {
            QuerySolution l = s.next();

            String k = l.get("p").toString();
            RDFNode r = l.get("o");


            String v = r.toString();

            if(!props.containsKey(k)) {
                props.put(k,new ArrayList<String>());
            }

            props.get(k).add(v);
        }

        return props;
    }

    public List<String> findPropertyFor(Word s) {
        List<String> tries = new ArrayList<>();
        List<String> res = new ArrayList<>();

//        if(findingHints.containsKey(s))
//            tries = new ArrayList<String>(Arrays.asList(findingHints.get(s)));

        //crawls Synonyms
        tries = SynonymCrawler.findSynonyms(s);
        
        tries.add(s.getText());

        for(String prop : props.keySet()) {
            for(String search: tries) {
                if(prop.contains(TextAnalyzer.Stemmer.stem(search))) {
                    List<String> result = props.get(prop);
                    res.addAll(result);
                }
            }
        }

        return res;
    }

    public static Map<String,String[]> findingHints = new HashMap<String,String[]>();
    static {
        findingHints.put("tall",new String[] {"elevation","height"});
        findingHints.put("high",new String[] {"altitude","elevation","height"});
        findingHints.put("marry",new String[] {"spouse","husband","wife"});
    }
}
