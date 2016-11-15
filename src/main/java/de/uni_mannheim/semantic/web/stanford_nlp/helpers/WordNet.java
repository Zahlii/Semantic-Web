package de.uni_mannheim.semantic.web.stanford_nlp.helpers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerUtils;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.data.list.PointerTargetNode;
import net.didion.jwnl.data.list.PointerTargetNodeList;
import net.didion.jwnl.dictionary.Dictionary;

public class WordNet {
//	public static void main(String[] args) {
//		 getSynonyms("married", "VB");
//		getHypernyms("move along", "VB");
//		System.out.println(getInfinitive("was", "VB"));
//	}

	private static void initJWNL() {
		// initialize JWNL (this must be done before JWNL can be used)
		try {
			JWNL.initialize(new FileInputStream("src/main/resources/jwnl14-rc2/config/file_properties.xml"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JWNLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * return the infinitive of a word
	 * @param word
	 * @param posTag
	 * @return
	 */
	public static String getInfinitive(String word, String posTag){
		initJWNL();

		POS pos = getPOS(posTag);

		ArrayList<String> synonyms = new ArrayList<>();

		String result = "";
		
		if (pos != null) {
			try {
				IndexWord wordTmp = Dictionary.getInstance().lookupIndexWord(pos, word);
				result = wordTmp.getLemma();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}
	
	/**
	 * returns all synonyms of a word in WordNet 
	 * @param word
	 * @param posTag
	 * @return
	 */
	public static ArrayList<String> getSynonyms(String word, String posTag) {
		initJWNL();

		POS pos = getPOS(posTag);

		ArrayList<String> synonyms = new ArrayList<>();

		if (pos != null) {
			try {
				IndexWord wordTmp = Dictionary.getInstance().lookupIndexWord(pos, word);
				if (wordTmp != null) {
					Synset synset[] = wordTmp.getSenses();
					for (int i = 0; i < synset.length; i++) {
						// System.out.println(synset[i].toString());
						Word[] words = synset[i].getWords();
						for (int j = 0; j < words.length; j++) {
							if (!synonyms.contains(words[j].getLemma()))
								synonyms.add(words[j].getLemma());
						}
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

//		System.out.println(Arrays.toString(synonyms.toArray()));

		return synonyms;
	}

	/**
	 * Returns all hypernyms of word in WordNet
	 * @param word
	 * @param posTag
	 * @return
	 */
	public static ArrayList<String> getHypernyms(String word, String posTag) {
		initJWNL();
		POS pos = getPOS(posTag);

		ArrayList<String> hypernyms = new ArrayList<>();

		if (pos != null) {
			try {
				IndexWord wordTmp = Dictionary.getInstance().lookupIndexWord(pos, word);

				if (wordTmp != null) {
					for (int i = 1; i <= wordTmp.getSenseCount(); i++) {

						// Get all of the hypernyms (parents) of the first sense
						PointerTargetNodeList hyp = PointerUtils.getInstance().getDirectHypernyms(wordTmp.getSense(i));

						for (Iterator<?> iter = hyp.listIterator(); iter.hasNext();) {
							PointerTargetNode ptn = ((PointerTargetNode) iter.next());
							Synset s = ptn.getSynset();
							for (int j = 0; j < s.getWordsSize(); j++) {
								String lemma = s.getWord(j).getLemma();
								if (!hypernyms.contains(lemma)) {
									hypernyms.add(lemma);
								}
							}
						}
					}
				}
			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		System.out.println(Arrays.toString(hypernyms.toArray()));

		return hypernyms;
	}

	private static POS getPOS(String posTag) {
		POS pos = null;
		if (posTag.matches("VB.*")) {
			pos = POS.VERB;
		} else if (posTag.matches("NN.*")) {
			pos = POS.NOUN;
		} else if (posTag.matches("JJ.*")) {
			pos = POS.ADJECTIVE;
		} else if (posTag.matches("RB.*")) {
			pos = POS.ADVERB;
		}
		return pos;
	}

	// public ArrayList<String> getHypernym(PointerTargetTreeNodeList l,
	// ArrayList<String> p) {
	// ArrayList<String> parent = p;
	// for (Iterator<?> itr = l.iterator(); itr.hasNext();) {
	// PointerTargetNode node = (PointerTargetNode) itr.next();
	// Synset synset = node.getSynset();
	// String name = synset.getWord(0).getLemma();
	// parent.add(name);
	// PointerTargetNodeList targets;
	// try {
	// targets = new
	// PointerTargetNodeList(synset.getTargets(PointerType.HYPERNYM));
	//
	// if (targets.size() > 0) {
	// parent = getHypernymTerm(targets, parent, name);
	// }
	// } catch (JWNLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// return parent;
	// }
	//
	// private ArrayList<String> getHypernymTerm(PointerTargetNodeList l,
	// ArrayList<String> p, String s){
	// ArrayList<String> parent = p;
	// if (!s.equals("entity")) {
	// for (Iterator<?> itr = l.iterator(); itr.hasNext();) {
	// PointerTargetNode node = (PointerTargetNode) itr.next();
	// Synset synset = node.getSynset();
	// String term = synset.getWord(0).getLemma();
	// parent.add(term);
	// PointerTargetNodeList targets;
	// try {
	// targets = new
	// PointerTargetNodeList(synset.getTargets(PointerType.HYPERNYM));
	// if (targets.size() > 0) {
	// parent = getHypernymTerm(targets, parent, term);
	// }
	// } catch (JWNLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// }
	// return parent;
	// }
}
