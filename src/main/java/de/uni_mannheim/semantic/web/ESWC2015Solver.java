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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ESWC2015Solver
{

    public static void main( String[] args ) throws InvalidFormatException, IOException, ParserConfigurationException, SAXException 
    {
    	
    	String r = DBPedia.checkTitleExists("Lawrence of Arabia");
    	String r2 = DBPedia.checkTitleExists("Lawrence of");
    	
		ESWC2015Solver e = new ESWC2015Solver();
		//e.loadXMLData("test_set.xml");
		//e.loadXMLData("training_set.xml");
		
		e.parseSentence("number of times that Jane Fonda married");

    }
    
    private Tokenizer _tokenizer;
    private Parser _parser;
    private NameFinderME[] finders;
    
    public ESWC2015Solver() throws InvalidFormatException, IOException {
		InputStream modelInTokens = new FileInputStream("en-token.bin");
		final TokenizerModel tokenModel = new TokenizerModel(modelInTokens);
		modelInTokens.close();    	 
		_tokenizer = new TokenizerME(tokenModel);
		
		
		InputStream modelInParser = new FileInputStream("en-parser-chunking.bin");
		final ParserModel parseModel = new ParserModel(modelInParser);
		modelInParser.close();
		                
		_parser = ParserFactory.create(parseModel);
		
		
	    /*String[] names = {"person", "location", "organization"};
	    int l = names.length;
	    
	    finders = new NameFinderME[l];
	    for (int mi = 0; mi < l; mi++) {
	      finders[mi] = new NameFinderME(new TokenNameFinderModel(
	          new FileInputStream("en-ner-" + names[mi] + ".bin")));
	    }*/
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
			}
		}
    }
    

    
    public void parseSentence(String text) throws InvalidFormatException, IOException {
    			    	
		final Parse p = new Parse(text,new Span(0, text.length()),AbstractBottomUpParser.INC_NODE,1,0);
		 
		// first contains spans, second the text tokens		
		Span[] spans = _tokenizer.tokenizePos(text);
		String[] tokens = new String[spans.length];
		
		for(int i=0,l=spans.length;i<l;i++)
			tokens[i] = (String) spans[i].getCoveredText(text);
		
		Span[] newSpans = checkPossibleEntities(spans,tokens);
		 
		/*for (int idx=0; idx < spans.length; idx++) {
			final Span span = spans[idx];
			// flesh out the parse with individual token sub-parses 
			p.insert(new Parse(text,span,AbstractBottomUpParser.TOK_NODE,0,idx));
		}
		
		// https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
		Parse x =_parser.parse(p);
		x.show();*/
    }

	private Span[] checkPossibleEntities(Span[] spans, String[] tokens) {
		int maxLength = 4;
		
		return new Span[]{};
	}

}
