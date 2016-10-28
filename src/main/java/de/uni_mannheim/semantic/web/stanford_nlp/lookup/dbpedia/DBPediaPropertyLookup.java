package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupStatus;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.RDFNode;

import java.util.*;

public class DBPediaPropertyLookup {
    private StanfordSentence sentence;
    private String _resource;
    private HashMap<String,ArrayList<String>> props = new HashMap<String,ArrayList<String>>();

    private HashMap<String,LookupResult> propertyResults = new HashMap<String, LookupResult>();
    private String currentText;
    private List<String> currentTokens;

    public DBPediaPropertyLookup(StanfordSentence sentence, String resource) {
        this._resource = resource;
        this.sentence = sentence;
        this.currentText = sentence.getTextWithoutEntities();

        this.fetchProperties();
        this.constructTokens();
    }

    public void setText(String text) {
        this.currentText = text;

    }
    public void constructTokens() {
        this.currentTokens = sentence.tokenize(this.currentText);
    }

    private void fetchProperties() {
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
    }

    public ArrayList<String> findProperty() {
        String propertyName = findPropertyName();
        return props.containsKey(propertyName) ? props.get(propertyName) : new ArrayList<>();
    }

    public ArrayList<String> findPropertyForName(String search) {
        LookupResult r =  findPropertyByName(search);
        if(r.getStatus() == LookupStatus.FOUND) {
            return props.get(r.getResult());
        }

        return new ArrayList<>();
    }

    private String findPropertyName() {
        int max = 5;

        int start = 0;
        int end = this.currentTokens.size()-1;

        ArrayList<LookupResult> results = new ArrayList<>();

        ArrayList<List<String>> ngrams = new ArrayList<>();

        for (int size = max; size >= 1; size--) {
            for (int i = start; i <= end - size + 1; i++) {
                List<String> ngramWords = currentTokens.subList(i, i + size);
                ngrams.add(ngramWords);
            }
        }

        for(List<String> ngram : ngrams) {
            List<String> terms = getPropertySearchTermsForNGram(ngram);

            for(String term : terms) {
                LookupResult r =  findPropertyByName(term);
                if(r.getStatus() == LookupStatus.FOUND) {
                    System.out.println("Mapping " + term + " to " + r.getResult());
                    String propName = "Property" + propertyResults.size();
                    this.currentText = this.currentText.replace(term,propName);
                    this.constructTokens();
                    propertyResults.put(propName,r);

                    return r.getResult();
                }
            }
        }
        return null;
    }

    private LookupResult findPropertyByName(String ngram) {
        double maxcertainty = 0.0;
        LookupResult res = new LookupResult(LookupStatus.NOT_FOUND);

        for(String property : props.keySet()) {
            String search = property.substring(property.lastIndexOf("/")+1);
            double certainty = 1-Levenshtein.normalized(search,ngram);

            if(certainty >= maxcertainty) {
                res = new LookupResult(ngram,search,property);
                maxcertainty = certainty;
            }
        }

        return res;
    }

    private List<String> getPropertySearchTermsForNGram(List<String> ngram) {
        List<String> ret = new ArrayList<>();
        for(String w : ngram) {
            if (w.contains("Variable"))
                return ret;
        }
        if(ngram.size() == 1) {
            String w1 = ngram.get(0);
            ret.add(w1);
            ret.add(w1+"By");
            ret.addAll(SynonymCrawler.findSynonyms(new Word(w1)));
            if(findingHints.containsKey(w1))
                ret.addAll(Arrays.asList(findingHints.get(w1)));
        } else {
            ret.add(StringUtils.join(ngram, " "));
        }
        return ret;
    }

    public static Map<String,String[]> findingHints = new HashMap<String,String[]>();
    static {
        findingHints.put("tall",new String[] {"elevation","height"});
        findingHints.put("high",new String[] {"altitude","elevation","height"});
        findingHints.put("marry",new String[] {"spouse","husband","wife"});
        findingHints.put("married",new String[] {"spouse","husband","wife"});
        findingHints.put("parents",new String[] {"father","mother"});
        findingHints.put("mayor",new String[] { "leader","leaderName" });
        findingHints.put("wrote",new String[] {"notableWork","author"});
        findingHints.put("owner",new String[] {"founder"});
        findingHints.put("created",new String[] {"author"});
        findingHints.put("designed",new String[] {"architect"});
    }
}
