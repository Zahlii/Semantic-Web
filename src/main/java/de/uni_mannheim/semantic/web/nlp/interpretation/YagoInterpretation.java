package de.uni_mannheim.semantic.web.nlp.interpretation;

import java.util.Arrays;
import java.util.List;

import de.uni_mannheim.semantic.web.info.DBPedia_MySQL;
import de.uni_mannheim.semantic.web.nlp.NGram;
import de.uni_mannheim.semantic.web.nlp.Sentence;
import de.uni_mannheim.semantic.web.nlp.Word;

public class YagoInterpretation extends SentenceInterpretation {

	public YagoInterpretation(Sentence s) {
		super(s);

		System.out.println(this);
	}

	@Override
	protected boolean isCandidateNGram(NGram tokens) {
		if (tokens.size() == 1 && tokens.get(0).getResource() != "")
			return false;

		if (tokens.size() == 0)
			return false;

		String text = tokens.getText();

		String[] pos = new String[tokens.size()];
		int i = 0;
		for (Word t : tokens)
			pos[i++] = t.getPOSTag();

		String posType = String.join(",", pos).replaceAll("NNPS", "NNP").replaceAll("NNS", "NN");

		String[] allowedPosType = new String[] { "NNP", "NNPS", "NNP,NNP", "NNP,NNP,NNP", "NNP,NNP,NN", "NNP,CD",
				"NNP,NNP,CD", "NNP,CD,NNP", "NNP,CD,NN", "NN,NN", "NN,NN,CD", "NNP,NN", "NN", "NNP", "NN", "NNP,IN,NNP",
				"NNP,PRP", "NNP,NNP,PRP", "NNP,NNP,IN,NNP", "NN,IN,NN", };

		boolean applicableStructure = Arrays.asList(allowedPosType).contains(posType);

		if (applicableStructure)
			return true;

		return false;
	}

	@Override
	public void interpret() {
		scanForEntities(0);
	}

	private void scanForEntities(int depth) {
		if (depth >= 3)
			return;

		this._ngrams = this._mainNGram.getUpToNGrams(4);

		for (NGram ngram : _ngrams) {
			if (isCandidateNGram(ngram)) {
				List<String> results = DBPedia_MySQL.findCategoriesForNGram(ngram);
				if (results.size() > 0) {
					System.out.println(results);
				}
			}
		}

	}

}
