package de.uni_mannheim.semantic.web.stanford_nlp.helpers;

import java.util.ArrayList;

import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

public class SynonymFinder {

	
	public static ArrayList<String> findSynonyms(Word w, boolean includeHypernyms){
		ArrayList<String> syns = WordNet.getSynonyms(w.getText(), w.getPOSTag());
		
		ArrayList<String> syns2 = SynonymCrawler.findSynonyms(w);
		
		for(int i=0; i<syns2.size(); i++){
			if(!syns.contains(syns2.get(i))){
				syns.add(syns2.get(i));
			}
		}
		
		if(includeHypernyms){
			ArrayList<String> hyp = WordNet.getHypernyms(w.getText(), w.getPOSTag());
			for(int i=0; i<hyp.size(); i++){
				if(!hyp.contains(hyp.get(i))){
					syns.add(hyp.get(i));
				}
			}
		}
		
		return syns;
	}
}
