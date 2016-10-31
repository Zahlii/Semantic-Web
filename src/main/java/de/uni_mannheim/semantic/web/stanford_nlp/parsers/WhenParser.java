package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.ArrayList;

public class WhenParser extends GenericParser {

	@Override
	protected ArrayList<String> parseInternal() throws Exception {
		//Lohnt sich gar nicht. Gibt nur eine frage im testset und die sieht nicht lösbar aus.
//		System.out.println("WhenParser: "+_sentence);
		return new ArrayList<>();
	}

}
