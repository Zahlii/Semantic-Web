package de.uni_mannheim.semantic.web.crawl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import de.uni_mannheim.semantic.web.domain.DBPediaResource;

public class DBPediaLookup {

//	public static void main(String[] args) {
//		DBPediaLookup db = new DBPediaLookup();
//		ArrayList<DBPediaResource> r = db.findDBPediaResource("Facebook");
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
	public static ArrayList<DBPediaResource> findDBPediaResource(String charSeq) {
		ArrayList<DBPediaResource> dbpediaResources = new ArrayList<>();

		try {
			String xml = Jsoup.connect("http://lookup.dbpedia.org/api/search/KeywordSearch?QueryString=" + charSeq)
					.get().toString();


			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			Document doc = builder.parse(is);

			Element e = doc.getDocumentElement();
			NodeList nl = e.getElementsByTagName("result");
			System.out.println(nl.getLength());
			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					String label = el.getElementsByTagName("label").item(0).getTextContent();
					System.out.println(label);
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
					DBPediaResource d = new DBPediaResource();
					d.setLabel(label);
					d.setUri(uri);
					d.setDescription(desc);
					d.setClassUris(classUris);
					d.setCategoryUris(catUris);
					dbpediaResources.add(d);
				}

			}

		} catch (IOException | ParserConfigurationException | SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dbpediaResources;
	}

}
