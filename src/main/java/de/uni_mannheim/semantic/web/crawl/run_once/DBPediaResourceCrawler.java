package de.uni_mannheim.semantic.web.crawl.run_once;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import de.uni_mannheim.semantic.web.crawl.model.DBPediaResource;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DBPediaResourceCrawler {

//	public static void main(String[] args) {
//		DBPediaResourceCrawler db = new DBPediaResourceCrawler();
//		ArrayList<DBPediaResource> r = db.findDBPediaResource("formula one racer");
//		for (int i = 0; i < r.size(); i++) {
//			System.out.println(r.get(i));
//		}
//	}

	/**
	 * searches in dbpedia lookup for resources and returns them
	 * 
	 * @param charSeq
	 * @return
	 */
	public static ArrayList<LookupResult> findDBPediaResource(String charSeq) {
//		ArrayList<DBPediaResource> dbpediaResources = new ArrayList<>();
		ArrayList<LookupResult> results = new ArrayList<>();

		String charSeqTmp = charSeq.replace(" ", "%20");
		try {
			String xml = Jsoup.connect("http://lookup.dbpedia.org/api/search/KeywordSearch?QueryString=" + charSeqTmp)
					.get().toString().replace("&nbsp", "&#160");


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document doc = builder.parse(is);

			Element e = doc.getDocumentElement();
			NodeList nl = e.getElementsByTagName("result");
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					String label = el.getElementsByTagName("label").item(0).getTextContent();
					String uri = el.getElementsByTagName("uri").item(0).getTextContent();
					String desc = el.getElementsByTagName("description").item(0).getTextContent();

					NodeList classes = el.getElementsByTagName("class");
					ArrayList<String> classUris = new ArrayList<>();
					if (classes != null) {
						for (int j = 0; j < classes.getLength(); j++) {
							Node tmp = ((Element) classes.item(j)).getElementsByTagName("uri").item(0);
							if (tmp != null) {
								String c = tmp.getTextContent();
								classUris.add(c);
							}
						}
					}

					NodeList categories = el.getElementsByTagName("category");
					ArrayList<String> catUris = new ArrayList<>();
					if (categories != null) {
						for (int j = 0; j < categories.getLength(); j++) {
							Node tmp = ((Element) categories.item(j)).getElementsByTagName("uri").item(0);
							if (tmp != null) {
								String c = tmp.getTextContent();
								catUris.add(c);
							}
						}
					}
//					DBPediaResource d = new DBPediaResource();
//					d.setLabel(label);
//					d.setUri(uri);
//					d.setDescription(desc);
//					d.setClassUris(classUris);
//					d.setCategoryUris(catUris);
//					dbpediaResources.add(d);

					if(!charSeq.equals("")){
						if(charSeq.charAt(0) == ' '){
							charSeq = charSeq.substring(1);
						}
						
						if(charSeq.charAt(charSeq.length()-1) == ' '){
							charSeq = charSeq.substring(0, charSeq.length()-1);
						}
					}
					
					LookupResult lr = new LookupResult(charSeq, label, uri.replace("\n", "").replace(" ", ""));
					results.add(lr);
				}

			}

		} catch (IOException | ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//			System.out.println(e.getLocalizedMessage());
		}
//		return dbpediaResources;
		return results;
	}

}
