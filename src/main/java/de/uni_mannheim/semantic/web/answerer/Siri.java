package de.uni_mannheim.semantic.web.answerer;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.domain.Answer;
import de.uni_mannheim.semantic.web.domain.Question;
import de.uni_mannheim.semantic.web.nlp.Sentence;

public class Siri extends LinkedDataAnswerer{

	@Override
	public ArrayList<String> train(Question q, Answer a) {
		Sentence s = new Sentence(q.getQuestion());
		
//		System.out.println(s.getType() + " -> " + s.getText());

		return s.getAnswers();
	}

	@Override
	public ArrayList<String> test(Question q) {
		// TODO Auto-generated method stub
		return null;
	}

}
