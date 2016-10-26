package de.uni_mannheim.semantic.web.stanford_nlp.helpers;

import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.List;
import java.util.Properties;

public class StanfordNLP {

	private static StanfordCoreNLP pipelineBasic;

	static {
		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize, ssplit, pos, lemma, depparse");

		props.setProperty("tokenize.options", "ptb3Escaping=false");

		pipelineBasic = new StanfordCoreNLP(props);
	}

	public static CoreMap handle(String sentence) {
		Annotation annotatedDocument = new Annotation(sentence);
		pipelineBasic.annotate(annotatedDocument);

		return annotatedDocument.get(CoreAnnotations.SentencesAnnotation.class).get(0);
	}

	public static String getPOSTag(String word) {
		CoreMap sentence = handle(word);
		List<CoreLabel> l = sentence.get(CoreAnnotations.TokensAnnotation.class);

		return new Word(l.get(0)).getPOSTag();
	}

	public static String getStem(String word) {
		CoreMap sentence = handle(word);
		List<CoreLabel> l = sentence.get(CoreAnnotations.TokensAnnotation.class);

		return new Word(l.get(0)).getPOSTag();
	}



}
