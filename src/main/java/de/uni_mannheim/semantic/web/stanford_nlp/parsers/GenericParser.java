package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;

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
	
}
