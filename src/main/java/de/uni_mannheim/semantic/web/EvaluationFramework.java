package de.uni_mannheim.semantic.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.uni_mannheim.semantic.web.stanford_nlp.model.ExpectedAnswer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_mannheim.semantic.web.answerer.LinkedDataAnswerer;
import de.uni_mannheim.semantic.web.answerer.Siri;
import de.uni_mannheim.semantic.web.stanford_nlp.model.QASet;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Question;

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
						
						String hybrid = eElement.getAttribute("hybrid");
						if(hybrid.equals("true"))
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
							answerUris.add(answers.item(j).getTextContent());
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

	public static double computeFMeasureForOneQuestion(ArrayList<String> answers, ArrayList<String> expectedAnswers){
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
		return fmeasure;
	}

	public static void evaluateParser(LinkedDataAnswerer answerer){

		EvaluationFramework.loadDataSet();

		ArrayList<Double> fmeasuresTraining = new ArrayList<>();
		ArrayList<Double> fmeasuresTest = new ArrayList<>();

		System.out.println("Start training: ");
		for (int i = 0; i < trainingSet.size(); i++) {
			if(trainingSet.get(i).getQuestion().getQuestionText().contains("Who") && trainingSet.get(i).isAnswerable()){

			String q = trainingSet.get(i).getQuestion().getQuestionText();

			System.out.println("===============================");

			System.out.println("Question: " + q + " (answerable: " + trainingSet.get(i).isAnswerable()+")");
			System.out.println("Expected Answer: " + Arrays.toString(trainingSet.get(i).getExpectedAnswer().getQueryResult().toArray(new String[0])));

			ArrayList<String> answers = answerer.train(trainingSet.get(i).getQuestion(), trainingSet.get(i).getExpectedAnswer());

			fmeasuresTraining.add(computeFMeasureForOneQuestion(answers, trainingSet.get(i).getExpectedAnswer().getQueryResult()));
			}
		}

//		System.out.println("Start test: ");
//		for (int i = 0; i < testSet.size(); i++) {
//			String q = testSet.get(i).getQuestion().getQuestionText();
//
//
//			System.out.println("Question: " + q + " (answerable: " + testSet.get(i).isAnswerable()+")");
//			System.out.println("Expected Answer: " + Arrays.toString(testSet.get(i).getExpectedAnswer().getQueryResult().toArray(new String[0])));
//
//			ArrayList<String> answers = answerer.test(testSet.get(i).getQuestion());
//
//			fmeasuresTest.add(computeFMeasureForOneQuestion(answers, testSet.get(i).getExpectedAnswer().getQueryResult()));
//		}
//
		System.out.println("Training F-Measure Avg: "+getAvg(fmeasuresTraining));
		//System.out.println("Test F-Measure Avg: "+getAvg(fmeasuresTest));
	}

	public static void main(String[] args){
		EvaluationFramework.evaluateParser(new Siri());
	}
}
