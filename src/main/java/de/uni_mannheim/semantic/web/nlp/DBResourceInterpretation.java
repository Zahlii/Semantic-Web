package de.uni_mannheim.semantic.web.nlp;

import java.util.Arrays;

import de.uni_mannheim.semantic.web.info.DBLookupResult;
import de.uni_mannheim.semantic.web.info.DBPedia;
import de.uni_mannheim.semantic.web.helpers.TextHelper;

public class DBResourceInterpretation extends SentenceInterpretation {

	public DBResourceInterpretation(Sentence s) {
		super(s);
		
		System.out.println(this);
	}

	protected boolean isCandidateNGram(NGram tokens) {
    	if(tokens.size() == 1 && tokens.get(0).getResource() != "")
    		return false;
    	
    	if(tokens.size()==0)
    		return false;
    	
    	String text = tokens.getText();
    	
    	String[] pos = new String[tokens.size()];
    	int i = 0;
    	for(Token t : tokens)
    		pos[i++] = t.getPOSTag();
    	
    	String posType = String.join(",", pos).replaceAll("NNPS","NNP").replaceAll("NNS", "NN");
    	
    	String[] allowedPosType = new String[] {
    			"NNP","NNPS",
    			"NNP,NNP","NNP,NNP,NNP","NNP,NNP,NN",
    			"NNP,CD","NNP,NNP,CD","NNP,CD,NNP","NNP,CD,NN",
    			"NN,NN","NN,NN,CD","NNP,NN",
    			"NN","NNP","NN",
    			"NNP,IN,NNP","NNP,PRP","NNP,NNP,PRP",
    			"NNP,NNP,IN,NNP","NN,IN,NN",
    	};

    	
    	boolean applicableStructure = Arrays.asList(allowedPosType).contains(posType);
    	
    	
    	if(applicableStructure || (TextHelper.isCapitalized(text) && tokens.size()==1 && tokens.get(0).getPOSTag().contains("NN")))
    		return true;

    	return false;
    }
	
	public void interpret() {
		scanForEntities(0);
	}
	
    private void scanForEntities(int depth) {
    	if(depth>=3)
    		return;

    	this._ngrams = this._mainNGram.getUpToNGrams(4);
    	
    	for(NGram ngram : _ngrams) {
    		if(isCandidateNGram(ngram)) {
    			int s = ngram.size();
    			String title = ngram.getStemmedText();

    			DBLookupResult dbTitle = DBPedia.checkTitleExists(title);
    			
    			if(dbTitle != null) {
    				double probability = TextHelper.similarity(title,dbTitle.getSimilarityRelevantCleanedPage())*s;
    			
    				if(probability>=0.9) {
	    				mergeNGramEntity(ngram,dbTitle.endPage,probability);
	    				if(s>1) {
	    					scanForEntities(++depth);
	    					return;
	    				}
    				}
    			}
    		}
    	}
    	
    }

}
