package de.uni_mannheim.semantic.web.crawl;

import java.io.IOException;
import java.util.ArrayList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.uni_mannheim.semantic.web.nlp.Word;

public class SynonymCrawler {

	private static final String NOUN = "(n.)";
	private static final String VERB = "(v.)";
	private static final String ADJ = "(adj.)";
	private static final String ADV = "(adv.)";

	private static final String URL = "http://www.synonym.com/";

	public static void main(String[] args) {
		Word w = new Word("tall", "JJ");
		ArrayList<Word> list = SynonymCrawler.findSynonyms(w);

		for (int i = 0; i < list.size(); i++) {
			System.out.println(list.get(i).toString());
		}
	}

	/**
	 * crawls synonym.com for synonyms of the word w
	 * 
	 * @param w
	 * @return list with synonyms
	 */
	public static ArrayList<Word> findSynonyms(Word w) {

		ArrayList<Word> synonyms = new ArrayList<>();

		String searchW = w.getText().replace(" ", "_");
		Document doc;
		try {
			doc = Jsoup.connect(URL + "synonyms/" + w.getText()).timeout(10 * 1000).get();

			String wordform = getWordformFromPOSTag(w.getPOSTag());

			// word is noun, verb, adj. or adv.
			if (wordform != null) {

				Elements boxes = doc.select(".row.result");

				ArrayList<Element> mBoxes = findMatchingBoxes(boxes, wordform);

				for (int i = 0; i < mBoxes.size(); i++) {
					Element box = mBoxes.get(i);
					if (box != null) {
						Elements syns = box.select("li.syn");

						for (int j = 0; j < syns.size(); j++) {
							String link = syns.get(j).select("a").attr("href");
							String text = syns.get(j).select("a").text();

							String posTag = getPOStag(link, w.getText());
							//pos tag "" if "text" is synonym of w but w is no synonym of "text"
							if(!posTag.equals("")){
								Word syn = new Word(text, posTag);
								synonyms.add(syn);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return synonyms;
	}

	/**
	 * determines the POS tag of a synonym by checking the synonym results of
	 * this words for the word the original query word
	 * 
	 * @param link
	 *            link to the synonym
	 * @param word
	 *            original synonym query word
	 * @return
	 */
	private static String getPOStag(String link, String word) {
		Document doc = null;
		try {
			doc = Jsoup.connect(URL + link).timeout(10 * 1000).get();

			Elements boxes = doc.select(".row.result");

			for (int i = 0; i < boxes.size(); i++) {
				String term = boxes.get(i).select(".term").text();

				Elements es = boxes.get(i).select("li.syn");
				for (int j = 0; j < es.size(); j++) {
					String synRes = es.get(j).select("a").text();
					if (synRes.equals(word)) {
						return getPOSTag(getWordformFromStringSeq(term));
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * finds matching box from the search results
	 * 
	 * @param boxes
	 * @param wordform
	 * @return
	 */
	private static ArrayList<Element> findMatchingBoxes(Elements boxes, String wordform) {
		ArrayList<Element> matching = new ArrayList<>();
		// find the first box with the matching wordform
		for (int i = 0; i < boxes.size(); i++) {
			Elements e = boxes.get(i).select(".term");
			if (e.size() > 0) {
				String head = e.get(0).text();
				if (head.matches(".*" + wordform + ".*")) {
					matching.add(boxes.get(i));
				}
			}
		}
		return matching;
	}

	private static String getWordformFromPOSTag(String posTag) {
		switch (posTag) {
		case "NN":
		case "NNS":
		case "NNP":
		case "NNPS":
			return NOUN;

		case "RB":
		case "RBR":
		case "RBS":
			return ADV;

		case "VB":
		case "VBD":
		case "VBG":
		case "VBN":
		case "VBP":
		case "VBZ":
			return VERB;

		case "JJ":
		case "JJR":
		case "JJS":
			return ADJ;

		default:
			return null;
		}
	}

	private static String getPOSTag(String wordform) {
		switch (wordform) {
		case NOUN:
			return "NN";
		case VERB:
			return "VB";
		case ADJ:
			return "JJ";
		case ADV:
			return "RB";
		default:
			return "";
		}
	}

	private static String getWordformFromStringSeq(String seq) {
		if (seq.matches(".*" + NOUN + ".*")) {
			return NOUN;
		} else if (seq.matches(".*" + VERB + ".*")) {
			return VERB;
		} else if (seq.matches(".*" + ADJ + ".*")) {
			return ADJ;
		} else if (seq.matches(".*" + ADV + ".*")) {
			return ADV;
		} else {
			return "";
		}
	}
}
