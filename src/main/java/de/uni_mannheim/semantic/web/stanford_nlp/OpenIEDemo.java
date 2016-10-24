package de.uni_mannheim.semantic.web.stanford_nlp;

import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ie.machinereading.structure.RelationMention;
import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * A demo illustrating how to call the OpenIE system programmatically. You can
 * call this code with:
 *
 * <pre>
 *   java -mx1g -cp stanford-openie.jar:stanford-openie-models.jar edu.stanford.nlp.naturalli.OpenIEDemo
 * </pre>
 *
 */
public class OpenIEDemo {

	private OpenIEDemo() {
	} // static main


	private static StanfordCoreNLP pipeline;

	public static void main(String[] args) throws Exception {
		// Create the Stanford CoreNLP pipeline

		// Annotate an example document.
		String text = IOUtils.slurpFile("questions.txt");
		String[] parts = text.split("\r\n");

		Properties props = PropertiesUtils.asProperties("annotators",
				"tokenize, ssplit, pos, lemma, ner, parse, depparse, mention, coref, natlog, relation, openie");

		pipeline = new StanfordCoreNLP(props);


		int i=0;
		for(String s : parts) {
			if(i++ > 20)
				break;
			annotate(s);
		}
	}

	private static void annotate(String text) {
		text = text.replaceAll("(Who|What|Which|How|Where|When)","").trim();
		Annotation document = new Annotation(text);
		pipeline.annotate(document);

		System.out.println("---");
		System.out.println(text);
		System.out.println("=>coref chains");
		for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
			System.out.println("\t"+cc);
		}
		for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
			System.out.println("=>mentions");
			for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
				System.out.println("\t"+m);
			}

			System.out.println("=>dependencies");
			System.out.println(sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));


			System.out.println("=>triples");
			// Get the OpenIE triples for the sentence
			Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

			// Print the triples
			for (RelationTriple triple : triples) {
				System.out.println(triple.confidence + "\t" +
						triple.subjectLemmaGloss() + "\t" +
						triple.relationLemmaGloss() + "\t" +
						triple.objectLemmaGloss());
			}

			System.out.println("=>relation mentions");
			List<RelationMention> rel = sentence.get(MachineReadingAnnotations.RelationMentionsAnnotation.class);
			for(RelationMention r : rel) {
				System.out.println(r.toString());
			}
		}

	}

}