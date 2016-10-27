package de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia;

import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;

public class DBPediaSpotlightNER {

	private static String url_prefiller =  "http://spotlight.sztaki.hu:2222/rest/annotate?text=";
	private static String url_postfiller = "&confidence=0.3";
	
	public static ArrayList<LookupResult> findEntities(String sentence){
		ArrayList<LookupResult> results = new ArrayList<>();
		String sentenceTmp = sentence.replace(" ", "%20");
		Document doc;
		try {
			doc = Jsoup.connect(url_prefiller+sentenceTmp+url_postfiller).get();
			
			Elements nes = doc.select("a");
			
			for(int i=0; i<nes.size(); i++){
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
	
	public static void main(String[] args) {
		ArrayList<LookupResult> list = DBPediaSpotlightNER.findEntities("Where was Bach born");
		
		for(int i=0; i<list.size(); i++){
			System.out.println(list.get(i).toString());
		}
	}
}
