package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;

public abstract class GenericParser {
	protected StanfordSentence _sentence;
	
	public StanfordSentence getSentence() {
		return _sentence;
	}

	public GenericParser() {
		
	}

	public ArrayList<String> parse(StanfordSentence s) throws Exception {
		this._sentence = s;
		
		return parseInternal();
	}
	
	protected abstract ArrayList<String> parseInternal() throws Exception;

	protected boolean isValidType(String resource, ArrayList<String> validTypes){
		if(validTypes.size() == 0) return true;
		
		if(resource.matches("http:.*")){
			ArrayList<String> types = DBPediaWrapper.getTypeOfResource(resource);
			
			boolean validType = false;
			for (String type : types) {
				if(validTypes.contains(type))
					return true;
			}
		}
		return false;
	}
	
	protected ArrayList<String> prepareForReturn(ArrayList<String> result, ArrayList<String> validTypes){
		ArrayList<String> ret = new ArrayList<>();
		for (int i = 0; i < result.size(); i++) {
			result.set(i, result.get(i).replaceAll("\\^\\^http:.*", ""));
			
			if(!ret.contains(result.get(i))&& !result.get(i).matches("http:.*:.*")){
				if(isValidType(result.get(i), validTypes))
					ret.add(result.get(i));
			}
		}
		return ret;
	}
	
    public static OntologyClass getBestOntologyClass(ArrayList<OntologyClass> props, String search){
    	double best = Double.MAX_VALUE;
    	OntologyClass prop = null;
    	for(int i=0; i<props.size(); i++){
    		double d = Levenshtein.normalized(props.get(i).getName(), search);
    		if(d<best){
    			best = d;
    			prop = props.get(i);
    		}
    	}
    	return prop;
    }
}
