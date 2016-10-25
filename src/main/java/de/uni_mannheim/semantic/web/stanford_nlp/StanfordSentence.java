package de.uni_mannheim.semantic.web.stanford_nlp;

import de.uni_mannheim.semantic.web.helpers.Levenshtein;
import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.nlp.finders.DBNERFinderResult;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.Mention;
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
	private List<String> currentTokens;
	private String cleanedText;
	private Annotation annotatedDocument;
	private CoreMap annotatedSentence;
	private List<CoreLabel> annotatedWords;
	private SemanticGraph graph;


	private HashMap<String,String> resourceMap = new HashMap<String,String>();

	private StanfordSentence(String text) {
		this.basicText = text;
		this.cleanedText = basicText.replaceAll("(\\.|\\?)$","");

		replaceNextEntity(0);

		basicAnnotate();
	}


	private static StanfordCoreNLP pipelineBasic;
	private static StanfordCoreNLP pipelineDepparse;

	public static void main(String[] args) throws Exception {

		String text = IOUtils.slurpFile("questions.txt");
		String[] parts = text.split("\r\n");

		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize, ssplit, pos, lemma, depparse");

		props.setProperty("tokenize.options", "ptb3Escaping=false");

		pipelineBasic = new StanfordCoreNLP(props);

		//new StanfordSentence("Does Breaking Bad have more episodes than Game of Thrones?");

		int i=0;
		for(String s : parts) {
			if(i++ > 15)
				break;
			new StanfordSentence(s);
		}
	}

	private void basicAnnotate() {
		//basicText = basicText.replaceAll("(Who|What|Which|How|Where|When)","").trim();

		annotatedDocument = new Annotation(cleanedText);
		pipelineBasic.annotate(annotatedDocument);

		System.out.println("================================================");
		System.out.println(basicText);
		System.out.println("\t"+ cleanedText);

		System.out.println("\t"+ resourceMap);

		annotatedSentence = annotatedDocument.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		annotatedWords = annotatedSentence.get(CoreAnnotations.TokensAnnotation.class);


		graph = annotatedSentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
		System.out.println(graph.toString(SemanticGraph.OutputFormat.RECURSIVE));

		transformGraph();
	}

	private void replaceNextEntity(int found) {
		if(found >= 2)
			return;

		final int max = 5;
		this.currentTokens = Arrays.asList(this.cleanedText.split(" "));
		int l = currentTokens.size();

		for(int size=max;size>=1;size--) {
			for (int i = 0; i < l - size + 1; i++) {
				List<String> ngramWords = currentTokens.subList(i,i+size);

				boolean isFound = checkNGram(ngramWords);

				if(isFound) {
					replaceNextEntity(++found);
					return;
				}
			}
		}
	}


	private String getText(CoreLabel word) {
		return word.get(CoreAnnotations.TextAnnotation.class);
	}

	private String getPOS(CoreLabel word) {
		return word.get(CoreAnnotations.PartOfSpeechAnnotation.class);
	}

	private boolean checkNGram(List<String> ngramWords) {
		int s = ngramWords.size();

		String first = ngramWords.get(0);

		if(first.contains("http://"))
			return false;

		boolean startsWithThe = first.equals("the");

		StringBuilder content = new StringBuilder();
		StringBuilder search = new StringBuilder();
		String append = "";

		if(startsWithThe && s==1)
			return false;

		String last = ngramWords.get(s-1);

		for(String w : ngramWords) {
			if(!isValidPart(w)) {
				if(isClassification(w)) {
					append = "("+w.replace("movie","film")+")";
				} else {
					return false;
				}
			} else {
				search.append(w).append(" ");
			}
			content.append(w).append(" ");

		}

		if(!isValidFirstPart(first))
			return false;

		if(!isValidLastPart(last))
			return false;

		SearchResult sr = tryFindNGram(ngramWords, search.append(append).toString().replaceAll("^the","").trim());

		if(sr.wasFound()) {
			String var = "Variable" + resourceMap.size();
			cleanedText = cleanedText.replace(content.toString().trim(),var);
			resourceMap.put(var,sr.getRes());
			return true;
		}

		return false;
	}

	private SearchResult tryFindNGram(List<String> ngramWords, String search) {
		//System.out.println("Searching..." + search);

		SearchResult s = normalLookUp(search);
		if(!s.wasFound()) {
			DBNERFinderResult res = referencedLookUp(search);
			if(res == null)
				return new SearchResult(false,null);
			else
				return new SearchResult(true,res.getPage());
		}

		return s;
	}

	private DBNERFinderResult referencedLookUp(String search) {
		if(search.contains("("))
			search = search.substring(0,search.indexOf("(")-1);

		DBNERFinderResult res = DBPedia.checkTitleExists(search);
		if(res == null)
			return null;

		String relevant = res.getSimilarityRelevantCleanedPage();

		int length = search.length();
		int dist = Levenshtein.computeLevenshteinDistance(search,relevant);

		double normalized = ((double)dist/(double)length);
		if(normalized < 0.2) {
			//System.out.println(normalized + " | " + res.getPage());
			return res;
		}

		return null;
	}

	private SearchResult normalLookUp(String search) {
		String title = DBPedia.findRessourceByTitle(search);
		if(title != null) {
			String niceSearch = search;
			String niceTitle = title.replace("http://dbpedia.org/resource/","").replace("_"," ");

			if(niceSearch.contains("("))
				niceSearch = search.substring(0,search.indexOf("(")-1);

			if(niceTitle.contains("("))
				niceTitle = niceTitle.substring(0,niceTitle.indexOf("(")-1);

			int length = search.length();
			int dist = Levenshtein.computeLevenshteinDistance(niceSearch,niceTitle);

			double normalized = ((double)dist/(double)length);
			if(normalized < 0.2) {
				//System.out.println(normalized + " | " + title);
				return new SearchResult(true,title);
			}
		}
		return new SearchResult(false,null);
	}
	private boolean isValidPart(String text) {
		return text.matches("(of|in)") || isValidLastPart(text) || isValidFirstPart(text);
	}

	private boolean isValidLastPart(String text) {
		return text.matches("^[A-Z].*") || text.matches("\\d+");
	}

	private boolean isClassification(String text) {
		return text.matches("(movie|book|film)");
	}

	private boolean isValidFirstPart(String text) {
		return (text.matches("^[A-Z].*") || text.matches("the"));
	}

	private void transformGraph() {
		IndexedWord w = graph.getFirstRoot();
		String pos = w.get(CoreAnnotations.PartOfSpeechAnnotation.class);
		System.out.println("ROOT = " + pos + " / " + w.toString());
	}


	private class SearchResult {
		private String res;
		private boolean found;

		public SearchResult(boolean found, String res) {
			this.found = found;
			this.res = res;
		}

		public boolean wasFound() {
			return found;
		}

		public String getRes() {
			return res;
		}
	}
}