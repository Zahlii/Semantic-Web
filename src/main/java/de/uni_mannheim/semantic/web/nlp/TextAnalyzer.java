package de.uni_mannheim.semantic.web.nlp;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import opennlp.tools.lemmatizer.SimpleLemmatizer;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.stemmer.PorterStemmer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

public class TextAnalyzer {
	public static Tokenizer Tokenizer;
	public static Parser Parser;
	public static POSTaggerME Tagger;
	public static PorterStemmer Stemmer;
	public static SimpleLemmatizer Lemmatizer;
	// private static NameFinderME[] finders;

	static {
		try {
			InputStream modelInTokens = new FileInputStream("en-token.bin");
			final TokenizerModel tokenModel = new TokenizerModel(modelInTokens);
			modelInTokens.close();
			Tokenizer = new TokenizerME(tokenModel);

			InputStream modelInParser = new FileInputStream("en-parser-chunking.bin");
			final ParserModel parseModel = new ParserModel(modelInParser);
			modelInParser.close();

			InputStream modelInPos = new FileInputStream("en-pos-maxent.bin");
			final POSModel posModel = new POSModel(modelInPos);
			Tagger = new POSTaggerME(posModel);
			modelInPos.close();

			InputStream is = new FileInputStream("en-lemmatizer.dict");
			Lemmatizer = new SimpleLemmatizer(is);
			is.close();

			Parser = ParserFactory.create(parseModel);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Stemmer = new PorterStemmer();

		/*
		 * String[] names = {"person", "location", "organization"}; int l =
		 * names.length;
		 * 
		 * finders = new NameFinderME[l]; for (int mi = 0; mi < l; mi++) {
		 * finders[mi] = new NameFinderME(new TokenNameFinderModel( new
		 * FileInputStream("en-ner-" + names[mi] + ".bin"))); }
		 */
	}

	public TextAnalyzer(String text) {
		this._currentText = text;
		this._originalText = _currentText;

	}

	private String _currentText;
	private String _originalText;
	private HashMap<Integer, String> _variables = new HashMap<Integer, String>();

	private boolean isCandidateForNamedEntity(String t) {
		if (t.contains(" ") || t.contains("_"))
			return false;
		String s = t.substring(0, 1);
		return s.toUpperCase().equals(s);
	}

	private ArrayList<String> tokenizeText(Span[] spans) {
		ArrayList<String> tokens = new ArrayList<String>(spans.length);

		for (int i = 0, l = spans.length; i < l; i++)
			tokens.add((String) spans[i].getCoveredText(_currentText));

		return tokens;
	}
}
