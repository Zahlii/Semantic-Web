package de.uni_mannheim.semantic.web.nlp.finders;

import java.util.List;

import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.nlp.NGram;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.helpers.TextHelper;

public class DBNERFinder {

	private final Sentence _sentence;

	public DBNERFinder(Sentence s) {
		this._sentence = s;
	}

	private boolean isCandidateNGram(NGram tokens) {
		int l = tokens.size();
		
		if(l==0)
			return false;

		boolean startsWithThe = tokens.get(0).isThe();

		if(startsWithThe && l==1)
			return false;

		Word first = tokens.get(startsWithThe ? 1 : 0);
		
		if(!first.getResource().equals(""))
			return false;

		Word last = tokens.get(l - 1);

		for (Word w : tokens) {
			if (!w.isCapitalized() && !w.isNumber() && !w.isEntityPreposition() && !w.isThe())
				return false;
		}
		
		if (!first.isCapitalized())
			return false;

		if (!last.isCapitalized() && !last.isNumber())
			return false;

		return true;

	}

	private Word searchAndApply(NGram ngram, String title, boolean retry) {
		int s = ngram.size();


		DBNERFinderResult dbTitle = DBPedia.checkTitleExists(title);

		if (dbTitle != null) {
			double probability = TextHelper.similarity(title, dbTitle.getSimilarityRelevantCleanedPage()) * s;

			if (probability >= 0.9) {	
				//System.out.println("Found " + dbTitle.endPage + " with probability " + probability + "(Term: "+title+")");
				Word m = _sentence.mergeNGramEntity(ngram, dbTitle.endPage, probability);
				return m;
			}
		} 
		
		return retry ? searchAndApply(ngram, TextHelper.removeLastSignificant(title),false) : null;
	}


	public Word findNext() {
		List<NGram> _ngrams = _sentence.getMainNGram().getUpToNGrams(4);

		for (NGram ngram : _ngrams) {
			if (isCandidateNGram(ngram)) {
				String text = ngram.getText().replaceAll("^the","").trim();
				Word r = searchAndApply(ngram, text ,true);

				if(r != null) {
					return r;
				}
			}
		}

		return null;
	}

}
