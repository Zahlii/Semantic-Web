package de.uni_mannheim.semantic.web.v2;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.ext.com.google.common.io.Files;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.BasicDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;

public class SentenceV2 {
	enum QuestionType {
		ONE, ORDERED, ALL, NUMBER_OF
	}

	class SortOrder {
		String Text;
		int limit;
		int offset;

		SortOrder() {

		}
	}

	public static void main(String[] args) {
		/*
		 * 1,N or COUNT | TYPE Give me all | astronauts. // ALL
		 * 
		 * 1,N or COUNT | TYPE | COUNTRY Give me all | astronauts | from Russia.
		 * Give me all | organizations | from Munich.
		 * 
		 * 1,N or COUNT | TYPE | PREDICATE | RESOURCE Give me the number of |
		 * people | who were born in | Vienna. // NUMBER_OF Give me all | films
		 * | that were directed by | Steven Spielberg. Give me the | person |
		 * who is the spouse of | Amanda Palmer. // ONE Give me the number of |
		 * people | who were the spouse of | Jane Fonda.
		 * 
		 * 1 | ORDER_BY_OFFSET_LIMIT | TYPE // ORDERED Give me the | 10 highest
		 * | mountains. Give me the | 2nd highest | mountain | in Germany. Give
		 * me the | youngest | Formula One Racer. Give me the | 10 largest |
		 * Cities in Germany.
		 * 
		 * OPTIONAL [[1,N or COUNT | PREDICATE | RESOURCE // ONE Give me the |
		 * population of | Germany. Give me the | vice president of | John F.
		 * Kennedy]]
		 */

		SentenceV2 s0 = new SentenceV2("Who is the vice president of JohnF.Kennedy?");
		SentenceV2 s1 = new SentenceV2("Which battles did LawrenceofArabia participate in?");
		SentenceV2 s2 = new SentenceV2("How many students does the FreeUniversityofAmsterdam have?");
	}

	private String _text;
	private String _baseText;
	private QuestionType _type;
	private SortOrder order;

	public SentenceV2(String text) {
		this._text = text.substring(0, text.length() - 1);

		semanticGraph(text);
		// parseSentence();

		// System.out.println(_baseText);

	}

	private void semanticGraph(String text) {

		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(text);

		// run all Annotators on this text
		pipeline.annotate(document);

		// these are all the sentences in this document
		// a CoreMap is essentially a Map that uses class objects as keys and
		// has values with custom types
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// traversing the words in the current sentence
			// a CoreLabel is a CoreMap with additional token-specific methods
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// this is the text of the token
				String word = token.get(TextAnnotation.class);
				// this is the POS tag of the token
				String pos = token.get(PartOfSpeechAnnotation.class);
				// this is the NER label of the token
				String ne = token.get(NamedEntityTagAnnotation.class);

				System.out.println("word: " + word + " pos: " + pos + " ne:" + ne);
			}

			// this is the parse tree of the current sentence
			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println("parse tree:\n" + tree);

			// this is the Stanford dependency graph of the current sentence
			SemanticGraph dependencies1 = sentence.get(CollapsedDependenciesAnnotation.class);

			System.out.println(dependencies1);
		}
	}


	private void parseSentence() {
		if (_text.startsWith("Give me all ")) {
			_type = QuestionType.ALL;
			_baseText = _text.replace("Give me all ", "");
		} else if (_text.startsWith("Give me the number of ")) {
			_type = QuestionType.NUMBER_OF;
			_baseText = _text.replace("Give me the number of ", "");
		} else if (_text.startsWith("Give me the ")) {
			_baseText = _text.replace("Give me the ", "");
			if (extractOrdering()) {
				_type = QuestionType.ORDERED;
			} else {
				_type = QuestionType.ONE;
			}
		}
	}

	private boolean extractOrdering() {
		final String valid = "(youngest|oldest|largest|smallest|highest|lowest|fastest|slowest)";
		final String regex = "((\\d+(st|nd|rd|th)\\s)|(\\d+\\s+))?" + valid;

		Pattern r = Pattern.compile(regex);
		Matcher m = r.matcher(_baseText);

		if (m.find()) {
			order = new SortOrder();

			_baseText = _baseText.replace(m.group(0), "").trim();
			String t = m.group(0).replaceAll("\\d+(st|nd|rd|th)", "").replaceAll("\\d+", "").replaceAll("\\s+", "");
			order.Text = t;

			if (m.group(1) == null) { // youngest
				order.limit = 1;
				order.offset = 0;
			} else if (m.group(2) == null) {// 10 youngest
				order.limit = Integer.parseInt(m.group(1).replaceAll("\\D+", ""));
				order.offset = 0;
			} else {
				order.offset = Integer.parseInt(m.group(1).replaceAll("\\D+", "")) - 1;
				order.limit = 1;
			}

			return true;
		} else {
			return false;
		}
	}

}
