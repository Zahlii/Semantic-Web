package de.uni_mannheim.semantic.web.stanford_nlp;

import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.DBPediaResourceLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
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


import java.util.*;

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
	private ArrayList<Word> words;
	private DBPediaResourceLookup dbpedia;

	private static StanfordCoreNLP pipelineBasic;

	public static void main(String[] args) throws Exception {

		String text = IOUtils.slurpFile("./data/questions.txt");
		String[] parts = text.split("\r\n");

		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize, ssplit, pos, lemma, depparse");

		props.setProperty("tokenize.options", "ptb3Escaping=false");

		pipelineBasic = new StanfordCoreNLP(props);

		StanfordSentence s = new StanfordSentence("How tall is Michael Jordan?");
		System.out.println(s.getAnswers());

		s = new StanfordSentence("Who produces Orangina?");
		System.out.println(s.getAnswers());

		/*
		int i=0;
		for(String s : parts) {
			if(i++ > 15)
				break;
			new StanfordSentence(s);
		}*/
	}


	public StanfordSentence(String text) throws Exception {
		this.basicText = text;
		this.cleanedText = basicText.replaceAll("(\\.|\\?)$","");

		extractQuestionType();

		dbpedia = new DBPediaResourceLookup(this);


		basicAnnotate();
	}

	public LookupResult<String> findEntity() {
		LookupResult<String> res = dbpedia.findOneIn(0,words.size()-1);
		return res;
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

		words = new ArrayList<>(annotatedWords.size());
		for(CoreLabel w : annotatedWords) {
			String text = w.get(CoreAnnotations.TextAnnotation.class);
			String pos = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
			words.add(new Word(text,pos));
		}

		graph = annotatedSentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
		System.out.println(graph.toString(SemanticGraph.OutputFormat.RECURSIVE));

		transformGraph();
	}

	public List<Word> getWords() {
		return words;
	}

	public List<Word> getVerbs() {
		return getByPOSTag("VB");
	}

	private ArrayList<Word> getByPOSTag(String tag) {
		ArrayList<Word> adjs = new ArrayList<Word>();

		for(Word w : words) {
			if(w.getPOSTag().matches(tag))
				adjs.add(w);
		}
		return adjs;
	}
	public ArrayList<Word> getAdjectives() {
		return getByPOSTag("JJ");
	}

	public ArrayList<Word> getAdverbs(){
		return getByPOSTag("RB");
	}

	public ArrayList<Word> getNouns(){
		return getByPOSTag("NNS?");
	}

	public ArrayList<Word> getProperNouns(){
		return getByPOSTag("NNPS?");
	}

	public Word getWord(int index) {
		return words.get(index);
	}

	public Word removeWord(int index) {
		Word w = getWord(index);
		cleanedText = cleanedText.replace(w.getText(),"").trim();
		annotatedWords.remove(index);
		words.remove(index);
		dbpedia.constructTokens();
		return w;
	}

	private void transformGraph() {
		IndexedWord w = graph.getFirstRoot();
		String pos = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		System.out.println("ROOT = " + pos + " / " + w.toString());
	}

	public String getCleanedText() {
		return this.cleanedText;
	}

	public ArrayList<String> getAnswers() throws Exception {
		return type.startParsing(this);
	}
}