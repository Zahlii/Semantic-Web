package de.uni_mannheim.semantic.web;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ESWC2015Solver
{
	private List<String> queries = new ArrayList<String>();
	
    public static void main( String[] args ) 
    {
    	
    	try {
    		ESWC2015Solver e = new ESWC2015Solver();
			e.loadXMLData("test_set.xml");
			e.loadXMLData("training_set.xml");
		} catch (ParserConfigurationException | SAXException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
    }
    
    public void loadXMLData(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("query");

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				String query = eElement.getElementsByTagName("keyword_query").item(0).getTextContent();
				System.out.println(query);
				queries.add(query);
			}
		}
    }
}
