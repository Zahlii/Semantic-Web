package de.uni_mannheim.semantic.web;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.uni_mannheim.semantic.web.nlp.Sentence;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;
import java.io.IOException;

public class ESWC2015Solver {

	public static void main(String[] args) throws Exception {

		// DBPedia_Terms.getClasses();

		ESWC2015Solver e = new ESWC2015Solver();
		// e.loadXMLData("test_set.xml");
		// e.loadXMLData("training_set.xml");
		e.loadXMLData(System.getProperty("user.dir") + "/data/qald-5_train.xml");

	}

	public void loadXMLData(String fileName) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		doc.getDocumentElement().normalize();
		// training_set.xml
		// NodeList nList = doc.getElementsByTagName("query");
		// qald-5_trian.xml
		NodeList nList = doc.getElementsByTagName("question");

		for (int temp = 0; temp < Math.min(10, nList.getLength()); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				// training_set.xml
				// String query =
				// eElement.getElementsByTagName("keyword_query").item(0).getTextContent();

				// qald-5_trian.xml
				String query = eElement.getElementsByTagName("string").item(0).getTextContent();

				Sentence s = new Sentence(query);
			}
		}
	}

}
