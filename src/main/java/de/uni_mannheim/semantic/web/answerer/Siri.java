package de.uni_mannheim.semantic.web.answerer;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.stanford_nlp.model.ExpectedAnswer;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Question;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;

public class Siri extends LinkedDataAnswerer{

	@Override
	public ArrayList<String> train(Question q, ExpectedAnswer a) {
		try {
			StanfordSentence s = new StanfordSentence(q.getQuestionText());
			s.setQuestion(q);
			s.setAnswer(a);
			return s.getAnswers();
		} catch(Exception e) {
			return new ArrayList<>();
		}

	}

	@Override
	public ArrayList<String> test(Question q) {
		try {
			StanfordSentence s = new StanfordSentence(q.getQuestionText());
			return s.getAnswers();
		} catch(Exception e) {
			return new ArrayList<>();
		}
	}

}
