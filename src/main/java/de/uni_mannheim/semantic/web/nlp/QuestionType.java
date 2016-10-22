package de.uni_mannheim.semantic.web.nlp;

import de.uni_mannheim.semantic.web.nlp.parsers.HowParser;
import de.uni_mannheim.semantic.web.nlp.parsers.WhoParser;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.nlp.parsers.GenericParser;

public enum QuestionType {
	Which("In which","To which","For which","Which","Through which"), // followed by CLASS
	Give_me_all("Give me all","Give me a list of all","Show me all","List the","List all"), // Followed by DESCRIPTION or CLASS
	Give_me_the("Give me"), // Followed by OBJECT
	Who_is("Who is","Who was","Who were"), // followed by DESCRIPTION or RESOURCE
	When_is("When is","When was","When were","When did"), // followed by RESOURCE
	What_is("What is","What was","What were","What are"), // followed by PREDICATE? 
	Where_is("Where is","Where was","Where were"), // followed by PREDICATE? 
//	Who("Who"), // followed by predicate
	When("When"), // Followed by 
	How_many("How many"), // followed by indicator for number
	Does("Does","Is","Do","Was","Did","Are"), // followed by RESOURCE
	How(HowParser.class, "How"),
//	Who_is(WhoParser.class, "Who is","Who was","Who were"), // followed by DESCRIPTION or RESOURCE
	Who(WhoParser.class, "Who"); 
	
	private final String[] _alternatives;
	private final Class<? extends GenericParser> _parse;
	
	QuestionType(Class<? extends GenericParser> parse, String... alternatives) {
		this._alternatives = alternatives;
		this._parse = parse;
	}
	
	QuestionType(String... alternatives) {
		this._alternatives = alternatives;
		this._parse = null;
	}
	
	public String[] getAlternatives() {
		return this._alternatives;
	}
	
	public boolean matches(String text) {
		for(String s : _alternatives) {
			if(text.startsWith(s))
				return true;
		}
		
		return false;
	}
	
	public String remove(String text) {
		for(String s : _alternatives) {
			if(text.startsWith(s))
				return text.replace(s, "").trim();
		}
		
		return text;
	}

	public ArrayList<String> startParsing(Sentence s) throws Exception {
		if(_parse != null) {
			try {
				GenericParser p = _parse.newInstance();
				return p.parse(s);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}