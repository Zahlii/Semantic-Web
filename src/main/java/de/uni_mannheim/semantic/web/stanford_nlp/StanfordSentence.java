package de.uni_mannheim.semantic.web.stanford_nlp;

import de.uni_mannheim.semantic.web.helpers.Levenshtein;
import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.nlp.QuestionType;
import de.uni_mannheim.semantic.web.nlp.finders.DBNERFinderResult;

import de.uni_mannheim.semantic.web.stanford_nlp.lookup.DBPediaResourceLookup;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

/**
 * A demo illustrating how to call the OpenIE system programmatically. You can
 * call this code with:
 *
 * <pre>
 *   java -mx1g -cp stanford-openie.jar:stanford-openie-models.jar edu.stanford.nlp.naturalli.StanfordSentence
 * </pre>
 *
 */
public class StanfordSentence {

	private String basicText;
	private String cleanedText;
	private Annotation annotatedDocument;
	private CoreMap annotatedSentence;
	private List<CoreLabel> annotatedWords;
	private SemanticGraph graph;
	private QuestionType type;

	private DBPediaResourceLookup dbpedia;

	private static StanfordCoreNLP pipelineBasic;

	public static void main(String[] args) throws Exception {

		String text = IOUtils.slurpFile("./data/questions.txt");
		String[] parts = text.split("\r\n");

		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize, ssplit, pos, lemma, depparse");

		props.setProperty("tokenize.options", "ptb3Escaping=false");

		pipelineBasic = new StanfordCoreNLP(props);

		new StanfordSentence("Does Breaking Bad have more episodes than Game of Thrones?");

		/*
		int i=0;
		for(String s : parts) {
			if(i++ > 15)
				break;
			new StanfordSentence(s);
		}*/
	}


	private StanfordSentence(String text) throws Exception {
		this.basicText = text;
		this.cleanedText = basicText.replaceAll("(\\.|\\?)$","");

		extractQuestionType();

		dbpedia = new DBPediaResourceLookup(this);
		dbpedia.findAll();

		basicAnnotate();
	}


	private void extractQuestionType() throws Exception {
		for(QuestionType t : QuestionType.values()) {
			if(t.matches(cleanedText)) {
				type = t;
				cleanedText = t.removeFromQuestion(cleanedText);
				return;
			}
		}

		throw new Exception("Malformed or unknown question " + this.cleanedText);
	}

	private void basicAnnotate() {
		//basicText = basicText.replaceAll("(Who|What|Which|How|Where|When)","").trim();

		annotatedDocument = new Annotation(cleanedText);
		pipelineBasic.annotate(annotatedDocument);

		System.out.println("================================================");
		System.out.println(basicText);
		System.out.println("\t"+ dbpedia.getText());
		System.out.println("\t"+dbpedia.getResults());

		annotatedSentence = annotatedDocument.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		annotatedWords = annotatedSentence.get(CoreAnnotations.TokensAnnotation.class);


		graph = annotatedSentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
		System.out.println(graph.toString(SemanticGraph.OutputFormat.RECURSIVE));

		transformGraph();
	}

	private void transformGraph() {
		IndexedWord w = graph.getFirstRoot();
		String pos = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		System.out.println("ROOT = " + pos + " / " + w.toString());
	}

	public String getCleanedText() {
		return this.cleanedText;
	}
}