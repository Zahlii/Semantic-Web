package de.uni_mannheim.semantic.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.mysql.cj.core.exceptions.MysqlErrorNumbers;
import com.mysql.cj.fabric.exceptions.MySQLFabricException;

import de.uni_mannheim.semantic.web.answerer.LinkedDataAnswerer;
import de.uni_mannheim.semantic.web.answerer.Siri;
import de.uni_mannheim.semantic.web.domain.Answer;
import de.uni_mannheim.semantic.web.domain.QASet;
import de.uni_mannheim.semantic.web.domain.Question;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import edu.stanford.nlp.parser.metrics.Eval;

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
						NodeList answers = eElement.getElementsByTagName("answers");
						for (int j = 0; j < answers.getLength(); j++) {
							answerUris.add(answers.item(j).getTextContent());
						}
						
						Question q = new Question();
						q.setId(Integer.parseInt(id));
						q.setQuestion(question);
						
						Answer a = new Answer();
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
	
	public static void evaluateParser(LinkedDataAnswerer answerer){
		EvaluationFramework.loadDataSet();
		
		for (int i = 0; i < trainingSet.size(); i++) {
			ArrayList<String> answers = new ArrayList<String>();
			
			if(trainingSet.get(i).getQuestion().getQuestion().contains("Who"))
				answers = answerer.train(trainingSet.get(i).getQuestion(), trainingSet.get(i).getAnswer());
		
			if(answers != null){
				for (int j = 0; j < answers.size(); j++) {
					System.out.println(answers.get(j));
				}
			}
		}
		
		//Test schleife
	}
	
	public static void main(String[] args){
		EvaluationFramework.evaluateParser(new Siri());
	}
}
