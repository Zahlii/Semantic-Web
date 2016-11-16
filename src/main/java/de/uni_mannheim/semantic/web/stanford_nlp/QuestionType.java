package de.uni_mannheim.semantic.web.stanford_nlp;

import de.uni_mannheim.semantic.web.stanford_nlp.parsers.GiveMeParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.HowParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhatParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhenParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhereParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhichParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhoParser;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.stanford_nlp.parsers.DoParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.GenericParser;

public enum QuestionType {
	GiveMe(GiveMeParser.class,"Give all", "Show me", "Show a list", "Give me all","Give me a list of all","Show me all","List the","List all","Give me"), // Followed by DESCRIPTION or CLASS
	Who(WhoParser.class,".*whose.*", "Who is","Who was","Who were","Who"), // followed by DESCRIPTION or RESOURCE
	When(WhenParser.class, "When is","When was","When were","When did","When"), // followed by RESOURCE
	What(WhatParser.class, "What did", "What is","What was","What were","What are", "What does"), // followed by PREDICATE?
	Where(WhereParser.class,"Where", "Where is","Where was","Where were", "Where did", "Where do"), // followed by PREDICATE?
	Does(DoParser.class, "Does","Is","Do","Was","Did","Are"), // followed by RESOURCE
	Which(WhichParser.class,".*(w|W)hich.*", "At which", "Under which", "From which", "In which","To which","For which","Which","Through which"), // followed by CLASS
	How(HowParser.class, "How many","How"),
	UNDEFINED();
	
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
			if(!s.contains(".*")){
				if(text.startsWith(s))
					return true;
			} else {
				if(text.matches(s))
					return true;
			}
		}
		
		return false;
	}
	
	public String removeFromQuestion(String text) {
		for(String s : _alternatives) {
			if(text.startsWith(s))
				return text.replace(s, "").trim();
		}
		
		return text;
	}

	public ArrayList<String> startParsing(StanfordSentence s) throws Exception {
		if(_parse != null) {
			try {
				GenericParser p = _parse.newInstance();
				System.out.println("ANSWERING");
				return p.parse(s);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return new ArrayList<>();
	}
}