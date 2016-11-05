package de.uni_mannheim.semantic.web.stanford_nlp;

import de.uni_mannheim.semantic.web.stanford_nlp.helpers.StanfordNLP;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaCategoryLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaResourceLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;


import java.util.*;


public class StanfordSentence {

	private String basicText;
	private String cleanedText;
	private CoreMap annotatedSentence;
	private List<CoreLabel> annotatedWords;
	private SemanticGraph graph;
	private QuestionType type;
	private ArrayList<Word> words;

	public DBPediaResourceLookup dbpediaResource;
	public DBPediaCategoryLookup dbpediaCategory;



	public static void main(String[] args) throws Exception {

		//StanfordSentence s = new StanfordSentence("Who was John F. Kennedy's vice president?");
		//System.out.println("Answers: " + s.getAnswers());

		StanfordSentence s = new StanfordSentence("Give me all countries in Africa.");
		s.findEntity();
		s.findCategory();
	}

	public StanfordSentence(String text) throws Exception {
		this.basicText = text;
		this.cleanedText = basicText.replaceAll("(\\.|\\?)$","");

		extractQuestionType();

		dbpediaResource = new DBPediaResourceLookup(this);
		dbpediaCategory = new DBPediaCategoryLookup(this);


		basicAnnotate();
	}

	public LookupResult findEntity() {
		List<LookupResult> res = dbpediaResource.findAll();
		return res.get(0);
	}

	public LookupResult findCategory() {
		List<LookupResult> res = dbpediaCategory.findAll();
		return res.get(0);
	}

	public String getTextWithoutEntities() {
		return dbpediaResource.getText();
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

		annotatedSentence = StanfordNLP.handle(cleanedText);

		System.out.println(basicText);
		System.out.println("\t"+ dbpediaResource.getText());
		System.out.println("\t"+ dbpediaResource.getResults());

		annotatedWords = annotatedSentence.get(CoreAnnotations.TokensAnnotation.class);

		words = new ArrayList<>(annotatedWords.size());
		for(CoreLabel w : annotatedWords) {
			words.add(new Word(w));
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
		dbpediaResource.constructTokens();
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

	public List<String> tokenize(String currentText) {
		CoreMap s = StanfordNLP.handle(currentText);

		ArrayList<String> words = new ArrayList<>();

		for(CoreLabel w : s.get(CoreAnnotations.TokensAnnotation.class)) {
			words.add(w.get(CoreAnnotations.TextAnnotation.class));
		}

		return words;
	}
	
	public SemanticGraph getSemanticGraph(){
		return this.graph;
	}
}