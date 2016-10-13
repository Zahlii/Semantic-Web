package de.uni_mannheim.semantic.web;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.apache.jena.query.ResultSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.parser.AbstractBottomUpParser;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ESWC2015Solver
{

    public static void main( String[] args ) throws Exception 
    {
    	
    	
    	//DBPedia_Terms.getClasses();
    	
		ESWC2015Solver e = new ESWC2015Solver();
		e.loadXMLData("test_set.xml");
		e.loadXMLData("training_set.xml");
		
		

    }
    

    
    public void loadXMLData(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("query");

		for (int temp = 0; temp < Math.min(2000,nList.getLength()); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String query = eElement.getElementsByTagName("keyword_query").item(0).getTextContent();
				
				Sentence s = new Sentence(query);

			}
		}
    }
    



}
