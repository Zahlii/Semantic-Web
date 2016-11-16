package de.uni_mannheim.semantic.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.uni_mannheim.semantic.web.stanford_nlp.QuestionType;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.model.ExpectedAnswer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_mannheim.semantic.web.answerer.LinkedDataAnswerer;
import de.uni_mannheim.semantic.web.answerer.Siri;
import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.model.EvaluationResult;
import de.uni_mannheim.semantic.web.stanford_nlp.model.QASet;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Question;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.GiveMeParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.HowParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhenParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhereParser;
import de.uni_mannheim.semantic.web.stanford_nlp.parsers.WhoParser;

public class EvaluationFramework {
	public static final String trainingFile = "/data/qald-5_train.xml";
	public static final String testFile = "/data/qald-5_test.xml";
	
	private static ArrayList<QASet> trainingSet = new ArrayList<>();
	private static ArrayList<QASet> testSet = new ArrayList<>();
	
	public static void loadDataSet(){
		try{
			for (int i = 0; i < 2; i++) {
				File fXmlFile = null;
				
				if(i == 0)	fXmlFile = new File(System.getProperty("user.dir") + trainingFile);
				else		fXmlFile = new File(System.getProperty("user.dir") + testFile);
					
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
				doc.getDocumentElement().normalize();
				
				NodeList nList = doc.getElementsByTagName("question");
				
				for (int temp = 0; temp < nList.getLength(); temp++) {
					Node nNode = nList.item(temp);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
	
						String id = eElement.getAttribute("id");

						String onlyDbo = eElement.getAttribute("onlydbo");
						if(onlyDbo.equals("false"))
							continue;

						String question = eElement.getElementsByTagName("string").item(0).getTextContent();
						boolean answerable = true;
						String query = "";
						try{
							query = eElement.getElementsByTagName("query").item(0).getTextContent();
						}catch(NullPointerException ex){
							query = eElement.getElementsByTagName("pseudoquery").item(0).getTextContent();
							answerable = false;
						}
						
						ArrayList<String> answerUris = new ArrayList<>();
						NodeList answers = eElement.getElementsByTagName("answer");
						for (int j = 0; j < answers.getLength(); j++) {
							answerUris.add(java.net.URLDecoder.decode(answers.item(j).getTextContent(), "UTF-8"));
						}
						
						Question q = new Question();
						q.setId(Integer.parseInt(id));
						q.setQuestion(question);
						
						ExpectedAnswer a = new ExpectedAnswer();
						a.setQuery(query);
						a.setQueryResult(answerUris);
						
						QASet qa = new QASet(q, a, answerable);
						
						if(i == 0) 	trainingSet.add(qa);
						else 		testSet.add(qa);
					}
				}
			}
		}catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Double getAvg(ArrayList<Double> values){
		Double sum = 0.0;
		for (int i = 0; i < values.size(); i++) {
			sum += values.get(i);
		}
		return sum / values.size();
	}

	public static EvaluationResult computeFMeasureForOneQuestion(ArrayList<String> answers, ExpectedAnswer expectedAnswer){
		ArrayList<String> expectedAnswers = new ArrayList<>();
		
		expectedAnswers = DBPediaWrapper.queryAnswerResults(expectedAnswer.getQuery());
		
		if(expectedAnswers.size() == 0)
			expectedAnswers = expectedAnswer.getQueryResult();
		
		System.out.println("Answers given: " + answers.size());
		System.out.println("Answers expected: " + expectedAnswers.size());

		double fmeasure = 0.0;
		double recall = 0.0;
		double precision = 0.0;
		double correct = 0.0;
		double expectedSize = (double) expectedAnswers.size();

		for (int j = 0; j < answers.size(); j++) {
			if(expectedAnswers.contains(answers.get(j))){
				expectedAnswers.remove(answers.get(j));
				correct++;
			}
		}

		System.out.println("Correct answers: " + correct);

		recall = correct / expectedSize;
		precision = (answers.size() > 0) ? correct / answers.size() : 0.0;

		System.out.println("Recall: " + recall);
		System.out.println("Precision: " + precision);

		fmeasure = (precision + recall > 0) ? (2 * precision * recall) / (precision + recall) : 0.0;

		System.out.println("F1: " + fmeasure);
		return new EvaluationResult(fmeasure, precision, recall, (answers.size() > 0));
	}

	public static void evaluateParser(LinkedDataAnswerer answerer){
		EvaluationFramework.loadDataSet();

		ArrayList<EvaluationResult> fmeasuresTraining = new ArrayList<>();
		ArrayList<EvaluationResult> fmeasuresTest = new ArrayList<>();

//		System.out.println("Start training: ");
//		for (int i = 0; i < trainingSet.size(); i++) {
////			if(trainingSet.get(i).getQuestion().getQuestionText().matches("(Do|Did).*") && trainingSet.get(i).isAnswerable()){
//			if(trainingSet.get(i).getQuestion().getQuestionText().matches(".*German.*") && trainingSet.get(i).isAnswerable()){
//
//				String q = trainingSet.get(i).getQuestion().getQuestionText();
//	
//	
//				System.out.println("Question: " + q + " (answerable: " + trainingSet.get(i).isAnswerable()+")");
//				System.out.println("Expected Answer: " + Arrays.toString(trainingSet.get(i).getExpectedAnswer().getQueryResult().toArray(new String[0])));
//	
//				ArrayList<String> answers = answerer.train(trainingSet.get(i).getQuestion(), trainingSet.get(i).getExpectedAnswer());
//				System.out.println("Given Answer: " + Arrays.toString(answers.toArray(new String[0])));
//
//				fmeasuresTraining.add(computeFMeasureForOneQuestion(answers, trainingSet.get(i).getExpectedAnswer().getQueryResult()));
////				fmeasuresTraining.add(computeFMeasureForOneQuestion(new ArrayList<>(), trainingSet.get(i).getExpectedAnswer().getQueryResult()));
//
//			}
//		}

		System.out.println("Start test: ");
		for (int i = 0; i < testSet.size(); i++) {
//			if(testSet.get(i).getQuestion().getQuestionText().matches(".*Ivy League university.*") && testSet.get(i).isAnswerable()){

			QASet q =  testSet.get(i);

			String qtext = q.getQuestion().getQuestionText();

			System.out.println("Question: " + qtext + " (answerable: " + q.isAnswerable()+")");
			System.out.println("Expected Answer: " + Arrays.toString(q.getExpectedAnswer().getQueryResult().toArray(new String[0])));

			QuestionType t = answerer.getQuestionType(qtext);

			ArrayList<String> answers = answerer.test(q.getQuestion());

			fmeasuresTest.add(computeFMeasureForOneQuestion(answers, q.getExpectedAnswer()));
//			fmeasuresTest.add(computeFMeasureForOneQuestion(new ArrayList<>(), testSet.get(i).getExpectedAnswer().getQueryResult()));
//			}
		}
		
		System.out.println("Training:");
		printResults(fmeasuresTraining);
		System.out.println("Test:");
		printResults(fmeasuresTest);
//		System.out.println(fmeasuresTraining.size()+" Training F-Measure Avg: "+getAvg(fmeasuresTraining));
//		System.out.println(fmeasuresTest.size()+" Test F-Measure Avg: "+getAvg(fmeasuresTest));
	}

	public static void printResults(ArrayList<EvaluationResult> results){
		ArrayList<Double> localMeasures = new ArrayList<>();
		ArrayList<Double> localPrecisions = new ArrayList<>();
		ArrayList<Double> localRecalls = new ArrayList<>();
		ArrayList<Double> globalMeasures = new ArrayList<>();
		ArrayList<Double> globalPrecisions = new ArrayList<>();
		ArrayList<Double> globalRecalls = new ArrayList<>();
		
		int countCorrect = 0;
		int countPartial = 0;
		int answered = 0;
		for (int i = 0; i < results.size(); i++) {
			globalMeasures.add(results.get(i).getFmeasure());
			globalPrecisions.add(results.get(i).getPrecision());
			globalRecalls.add(results.get(i).getRecall());
			
			if(results.get(i).isAnswered()){
				answered++;
				localMeasures.add(results.get(i).getFmeasure());
				localPrecisions.add(results.get(i).getPrecision());
				localRecalls.add(results.get(i).getRecall());
			}
			
			if(results.get(i).getFmeasure() == 1.0)		countCorrect++;
			else if(results.get(i).getFmeasure() > 0.0)	countPartial++;

		}
		
		System.out.printf("Questions: %d, Answered: %d, Correct: %d, PartialCorrect: %d\n", results.size(), answered, countCorrect, countPartial);
		System.out.printf("Global: F-M: %.3f, P: %.3f, R: %.3f\n", getAvg(globalMeasures), getAvg(globalPrecisions), getAvg(globalRecalls));
		System.out.printf("Local: F-M: %.3f, P: %.3f, R: %.3f\n", getAvg(localMeasures), getAvg(localPrecisions), getAvg(localRecalls));
	}
	
	public static void main(String[] args){
		EvaluationFramework.evaluateParser(new Siri());
	}
}
