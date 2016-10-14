package de.uni_mannheim.semantic.web.nlp.helpers;

import info.debatty.java.stringsimilarity.MetricLCS;
import info.debatty.java.stringsimilarity.NGram;
import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

public class TextHelper {
	public static boolean isCapitalized(String text) {
		String first = text.substring(0,1);
    	return first.toUpperCase().equals(first);
	}
	
	public static boolean endsWith(String s, String end) {
		// Hallo, lo
		
		// 3
		int i = s.indexOf(end);
		
		if(i == -1)
			return false;
		
		// 3
		int l = end.length();
		
		// 5-3 +1
		return i == (s.length() - l + 1);

	}

	public static String removeLast(String title) {
		return title.substring(0,title.length()-1);
	}
	
	
	public static double similarity(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
		
		
		NormalizedLevenshtein l = new NormalizedLevenshtein();

        return l.distance(s1, s2);
		//int le = Levenshtein.computeLevenshteinDistance(s1,s2);
	}
	
	
}
