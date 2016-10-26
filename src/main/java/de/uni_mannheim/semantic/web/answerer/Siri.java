package de.uni_mannheim.semantic.web.answerer;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.domain.Answer;
import de.uni_mannheim.semantic.web.domain.Question;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;

public class Siri extends LinkedDataAnswerer{

	@Override
	public ArrayList<String> train(Question q, Answer a) {
		try {
			StanfordSentence s = new StanfordSentence(q.getQuestion());
			return s.getAnswers();
		} catch(Exception e) {
			return new ArrayList<>();
		}

	}

	@Override
	public ArrayList<String> test(Question q) {
		// TODO Auto-generated method stub
		return null;
	}

}
