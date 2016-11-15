package de.uni_mannheim.semantic.web.crawl;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.text.TextHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DBPediaWrapper {
	private static final String endpoint = "http://dbpedia.org/sparql";

	private static String prefix;

	static {
		try {
			prefix = TextHelper.readFile("prefixes.txt", Charset.forName("UTF8"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static ResultSet query(String strQuery) {
		String x = prefix + "\r\n" + strQuery;
//		 System.out.println(x);
		Query q = QueryFactory.create(x);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q);
		ResultSet RS = qexec.execSelect();

		return RS;
	}

	public static ArrayList<String> queryWOPrefix(String strQuery) {
//		 System.out.println(x);
		Query q = QueryFactory.create(strQuery);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q);
		ResultSet RS = qexec.execSelect();

		ArrayList<String> res = new ArrayList<>();
		while (RS.hasNext()) {
			QuerySolution s = RS.next();
			String p = s.get("uri").toString();
			res.add(p);
		}
		
		return res;
	}
	
	public static ArrayList<String> queryAnswerResults(String strQuery) {
		try{
//		 System.out.println(x);
			String[] split = strQuery.split("\n");
			ArrayList<String> search = new ArrayList<>();
			for (int i = 0; i < split.length; i++) {
				if(split[i].contains("SELECT")){
					String[] split2 = split[i].split("\\s");
					for (int j = 0; j < split2.length; j++) {
						if(split2[j].contains("?"))
							search.add(split2[j]);
					}
				}
			}
			
			if(search.size() == 0)
				return new ArrayList<>();
			
			strQuery = prefix + strQuery;
			Query q = QueryFactory.create(strQuery);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, q);
			ResultSet RS = qexec.execSelect();
	
			ArrayList<String> res = new ArrayList<>();
			while (RS.hasNext()) {
				QuerySolution s = RS.next();
				for (int i = 0; i < search.size(); i++) {
					String p = s.get(search.get(i)).toString();
					res.add(p);
				}
			}
			
			for (int i = 0; i < res.size(); i++) {
				res.set(i, res.get(i).replaceAll("\\^\\^http:.*", ""));
			}
			
			return res;
		}catch(Exception e){
//			e.printStackTrace();
		}
		return new ArrayList<>();
	}
	
	private static ArrayList<LookupResult> lookupSearch(String URL, String charSeq) {

		ArrayList<LookupResult> results = new ArrayList<>();

		String charSeqTmp = charSeq.replace(" ", "%20");
		try {
			String xml = Jsoup.connect(URL + charSeqTmp).timeout(1000 * 20).get().toString().replace("&nbsp", "&#160");

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			org.w3c.dom.Document doc = builder.parse(is);

			Element e = doc.getDocumentElement();
			NodeList nl = e.getElementsByTagName("result");

			if (nl != null) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element el = (Element) nl.item(i);
					String label = el.getElementsByTagName("label").item(0).getTextContent();
					String uri = el.getElementsByTagName("uri").item(0).getTextContent();

					if (!charSeq.equals("")) {
						if (charSeq.charAt(0) == ' ') {
							charSeq = charSeq.substring(1);
						}

						if (charSeq.charAt(charSeq.length() - 1) == ' ') {
							charSeq = charSeq.substring(0, charSeq.length() - 1);
						}
					}

					LookupResult lr = new LookupResult(charSeq, label, uri.replace("\n", "").replace(" ", ""));
					results.add(lr);
				}

			}

		} catch (IOException | ParserConfigurationException | SAXException e) {
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<LookupResult> lookupKeywordSearch(String charSeq) {
		return lookupSearch("http://lookup.dbpedia.org/api/search/KeywordSearch?QueryString=", charSeq);
	}

	public static ArrayList<LookupResult> lookupPrefixSearch(String charSeq) {
		return lookupSearch("http://lookup.dbpedia.org/api/search/PrefixSearch?QueryString=", charSeq);
	}

	public static LookupResult sparqlSearch(String title) {

		// we dont want categories, but we want possible redirection targets. we
		// don't want disambiguations either!
		String se = ("SELECT ?x ?y WHERE { \r\n" + "{\r\n" + "?y rdfs:label \"XXX\"@en .\r\n"
				+ "?y dbo:wikiPageRedirects ?x.\r\n" + "?y dbo:wikiPageID ?id.\r\n" + "} UNION {\r\n"
				+ "?x rdfs:label \"XXX\"@en .\r\n" + "?x dbo:wikiPageID ?id.\r\n"
				+ "OPTIONAL { ?x dbo:wikiPageDisambiguates ?dis . }\r\n" + "FILTER(!BOUND(?dis))\r\n" + "} \r\n"
				+ "FILTER(!regex(?x,\"Category\"))\r\n" + "\r\n" + "} LIMIT 1").replaceAll("XXX", title.trim());

		ResultSet r = DBPediaWrapper.query(se);
		if (r.hasNext()) {
			QuerySolution s = r.next();
			String x = s.get("x").toString();
			String y = s.contains("y") ? s.get("y").toString() : null;
			String match = y != null ? y : x;
			match = match.replace("http://dbpedia.org/resource/", "");
			return new LookupResult(title, match, x);
		}

		return null;
	}

	private static String url_prefiller = "http://spotlight.sztaki.hu:2222/rest/annotate?text=";
	private static String url_postfiller = "&confidence=0.3";

	public static ArrayList<LookupResult> spotlightLookupSearch(String sentence) {
		ArrayList<LookupResult> results = new ArrayList<>();
		String sentenceTmp = sentence.replace(" ", "%20");
		Document doc;
		try {
			doc = Jsoup.connect(url_prefiller + sentenceTmp + url_postfiller).timeout(10*1000).get();

			Elements nes = doc.select("a");

			for (int i = 0; i < nes.size(); i++) {
				String href = nes.get(i).attr("href");
				String ne = nes.get(i).text();
				LookupResult lr = new LookupResult(ne, ne, href);
				results.add(lr);
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	public static ArrayList<String> checkPropertyExists(String obj, Property prop) {
		ArrayList<String> properties = new ArrayList<>();

		String se = ("PREFIX obj:<OBJECT> \r\n" + " SELECT ?prop \r\n" + "	WHERE{  \r\n"
		// + " obj: dbo:PROPERTY ?prop . \r\n"
				+ "		obj: <PROPERTY> ?prop . \r\n"
				// + " ganges: ?p dbr:India . \r\n"
				+ "}").replaceAll("OBJECT", obj).replaceAll("PROPERTY", prop.getName());

		ResultSet r = DBPediaWrapper.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("prop").toString();
			properties.add(p);
		}

		return properties;
	}

	public static ArrayList<String> getTypeOfResource(String resource) {
		ArrayList<String> ret = new ArrayList<>();

		String se = ("select ?type where{<RESOURCE> a ?type.}\r\n").replaceAll("RESOURCE", resource);

		ResultSet r = DBPediaWrapper.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("type").toString();
			ret.add(p);
		}

		return ret;
	}

	public static ArrayList<String> checkClassRelationExists(String obj1, OntologyClass obj2) {
		ArrayList<String> relations = new ArrayList<>();

		String se = ("PREFIX obj: <OBJECT1> \r\n" + " SELECT ?p \r\n" + "	WHERE{  \r\n"
		// + " obj: dbp:country ?(PROPERTY) . \r\n"
				+ "	    obj: ?p <OBJECT2> . \r\n" + "}").replaceAll("OBJECT1", obj1).replaceAll("OBJECT2",
						obj2.getLink());
		// System.out.println(se);

		ResultSet r = DBPediaWrapper.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String rel = s.get("p").toString();
			relations.add(rel);
		}

		return relations;
	}

	public static ArrayList<String> buildJJRQuery(String resource, String prop, String comparator, double numeric, String adj1){
		ArrayList<String> properties = new ArrayList<>();
		
		String se = "SELECT DISTINCT ?obj \r\n" 
				+ "	WHERE{  \r\n"
				+ "?obj a dbo:<OBJECT> . \r\n"
				+ "?obj <PROPERTY> ?prop . \r\n"
				+ "FILTER(?prop <COMPARATOR> <NUMERIC>) . \r\n";
		if(!adj1.equals("")){
			se += "?obj ?p ?q . FILTER(regex(?q,\"<ADJECTIVE>\")) . \r\n";
		}
		
		se += "}";
		se = se.replaceAll("<OBJECT>", resource).replaceAll("PROPERTY", prop)
					.replaceAll("<COMPARATOR>", comparator).replaceAll("<NUMERIC>", String.valueOf(numeric)).replaceAll("<ADJECTIVE>", adj1);
		
//		System.out.println(se);
		
		ResultSet r = DBPediaWrapper.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("obj").toString();
			properties.add(p);
		}

		return properties;
	}
	
	public static ArrayList<String> buildGiveMeQuery(String resource, String adj1){
		ArrayList<String> properties = new ArrayList<>();
		
		String se = "SELECT DISTINCT ?obj \r\n" 
				+ "	WHERE{  \r\n"
				+ "?obj a dbo:<OBJECT> . \r\n";
		if(!adj1.equals("")){
			se += "?obj ?p ?q . FILTER(regex(?q,\"<ADJECTIVE>\")) . \r\n";
		}
		
		se += "}";
		se = se.replaceAll("<OBJECT>", resource).replaceAll("<ADJECTIVE>", adj1);
		
//		System.out.println(se);
		
		ResultSet r = DBPediaWrapper.query(se);
		while (r.hasNext()) {
			QuerySolution s = r.next();
			String p = s.get("obj").toString();
			properties.add(p);
		}

		return properties;
	}
}
