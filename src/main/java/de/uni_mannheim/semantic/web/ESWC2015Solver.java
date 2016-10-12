package de.uni_mannheim.semantic.web;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
    
    private Tokenizer _tokenizer;
    private Parser _parser;
    
    public ESWC2015Solver() throws InvalidFormatException, IOException {
		InputStream modelInTokens = new FileInputStream("en-token.bin");
		final TokenizerModel tokenModel = new TokenizerModel(modelInTokens);
		modelInTokens.close();    	 
		_tokenizer = new TokenizerME(tokenModel);
		
		
		InputStream modelInParser = new FileInputStream("en-parser-chunking.bin");
		final ParserModel parseModel = new ParserModel(modelInParser);
		modelInParser.close();
		                
		_parser = ParserFactory.create(parseModel);
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
				parseSentence(query);
				queries.add(query);
			}
		}
    }
    

    
    private void parseSentence(String text) throws InvalidFormatException, IOException {
    			    	
		final Parse p = new Parse(text,new Span(0, text.length()),AbstractBottomUpParser.INC_NODE,1,0);
		 
		// make sure to initialize the _tokenizer correctly
		final Span[] spans = _tokenizer.tokenizePos(text);
		 
		for (int idx=0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses 
			p.insert(new Parse(text,span,AbstractBottomUpParser.TOK_NODE,0,idx));
		}
		
		// https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		Parse x =_parser.parse(p);
		x.show();
    }

}
