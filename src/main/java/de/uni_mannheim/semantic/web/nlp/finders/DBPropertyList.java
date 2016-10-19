package de.uni_mannheim.semantic.web.nlp.finders;

import de.uni_mannheim.semantic.web.info.DBPedia;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBPropertyList {
    private String _resource;
    private HashMap<String,List<String>> props = new HashMap<String,List<String>>();

    public DBPropertyList(String resource) {
        this._resource = resource;
    }

    public Map<String,List<String>> fetchProperties() {


        ResultSet s = DBPedia.query("SELECT ?p ?o WHERE {\n" +
                "<"+this._resource+"> ?p ?o.\n" +
                "}");

        while(s.hasNext()) {
            QuerySolution l = s.next();

            String k = l.get("p").toString();
            RDFNode r = l.get("o");

            String v = r.isResource() ? r.toString() : r.asLiteral().getString();

            if(!props.containsKey(k)) {
                props.put(k,new ArrayList<String>());
            }

            props.get(k).add(v);
        }

        return props;
    }

    public String findPropertyFor(String s) {
        if(findingHints.containsKey(s))
            s = findingHints.get(s);

        for(String k : props.keySet()) {
            if(k.contains(s))
                return props.get(k).toString();
        }

        return null;
    }

    public static Map<String,String> findingHints = new HashMap<String,String>();
    static {
        findingHints.put("tall","height");
        findingHints.put("high","elevation");
        findingHints.put("marry","spouse");
    }
}
