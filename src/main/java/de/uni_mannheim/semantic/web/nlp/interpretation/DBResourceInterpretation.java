package de.uni_mannheim.semantic.web.nlp.interpretation;

import de.uni_mannheim.semantic.web.info.DBLookupResult;
import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.nlp.NGram;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;
import de.uni_mannheim.semantic.web.helpers.TextHelper;

public class DBResourceInterpretation extends SentenceInterpretation {

	public DBResourceInterpretation(Sentence s) {
		super(s);

		//System.out.println(this);
	}

	@Override
	protected boolean isCandidateNGram(NGram tokens) {
		int l = tokens.size();
		
		if(l==0)
			return false;
		
		Word first = tokens.get(0);
		
		if(!first.getResource().equals(""))
			return false;
		
		if(first.isQuestionWord()) {
			return false;
		}
			
		Word last = tokens.get(l - 1);

		for (Word w : tokens) {
			if (!w.isCapitalized() && !w.isNumber() && !w.isEntityPreposition())
				return false;
		}
		
		if (!first.isCapitalized())
			return false;

		if (!last.isCapitalized() && !last.isNumber())
			return false;

		return true;

	}

	@Override
	public void interpret() {
		scanForEntities(0);
	}

	private boolean searchAndApply(NGram ngram, String title, boolean retry) {
		int s = ngram.size();
		
		DBLookupResult dbTitle = DBPedia.checkTitleExists(title);

		if (dbTitle != null) {
			double probability = TextHelper.similarity(title, dbTitle.getSimilarityRelevantCleanedPage()) * s;

			if (probability >= 0.9) {	
				System.out.println("Found " + dbTitle.endPage + " with probability " + probability + "(Term: "+title+")");	
				mergeNGramEntity(ngram, dbTitle.endPage, probability);
				return true;
			}
		} 
		
		return retry ? searchAndApply(ngram, TextHelper.removeLast(title),false) : false;
	}
	private void scanForEntities(int depth) {
		if (depth >= 3)
			return;

		this._ngrams = this._mainNGram.getUpToNGrams(4);

		for (NGram ngram : _ngrams) {
			if (isCandidateNGram(ngram)) {
				boolean success = searchAndApply(ngram, ngram.getText(),true);	
				
				if(success && ngram.size()>1) {
					scanForEntities(depth++);
					return;
				}
			}
		}

	}

}
