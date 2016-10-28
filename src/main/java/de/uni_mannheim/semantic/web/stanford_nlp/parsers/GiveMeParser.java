package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nik on 26.10.2016.
 */
public class GiveMeParser extends GenericParser {
    @Override
    protected ArrayList<String> parseInternal() throws Exception {

        List<LookupResult> results = _sentence.dbpediaCategory.findAllByTitle(_sentence.getCleanedText());


        ArrayList<String> responses = new ArrayList<>();

        for(LookupResult r : results) {
            ResultSet s = DBPediaWrapper.query("SELECT * WHERE { ?p rdf:type yago:"+r.getResult()+" .}");
            while(s.hasNext()) {
                QuerySolution sol = s.next();
                responses.add(sol.get("p").toString());
            }
        }

        return responses;
    }
}
