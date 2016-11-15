package de.uni_mannheim.semantic.web.stanford_nlp.parsers;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

import org.ejml.alg.dense.misc.PermuteArray;

import de.uni_mannheim.semantic.web.crawl.DBPediaWrapper;
import de.uni_mannheim.semantic.web.crawl.SynonymCrawler;
import de.uni_mannheim.semantic.web.crawl.model.GeneralQuery;
import de.uni_mannheim.semantic.web.crawl.model.OntologyClass;
import de.uni_mannheim.semantic.web.crawl.model.Property;
import de.uni_mannheim.semantic.web.crawl.run_once.DBPediaOntologyCrawler;
import de.uni_mannheim.semantic.web.stanford_nlp.StanfordSentence;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Levenshtein;
import de.uni_mannheim.semantic.web.stanford_nlp.helpers.Permutation;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.LookupResult;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaPropertyLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.lookup.dbpedia.DBPediaResourceLookup;
import de.uni_mannheim.semantic.web.stanford_nlp.model.ExpectedAnswer;
import de.uni_mannheim.semantic.web.stanford_nlp.model.Word;

public abstract class GenericParser {
	protected StanfordSentence _sentence;
	private static ArrayList<GeneralQuery> generalQueries = new ArrayList<>();
	
	private boolean test = false;
	
	public StanfordSentence getSentence() {
		return _sentence;
	}

	public GenericParser() {
		
	}

	private void analyzeQuestion(){
		try{
			ExpectedAnswer answer = this._sentence.getAnswer();
			
			if(answer == null){
				this.test = true;
				return;
			}
			
			ArrayList<Word> words = new ArrayList<>();
	        ArrayList<LookupResult> entities = new ArrayList<>();
	        entities.addAll(_sentence.findEntities());
	        for (int j = 0; j < _sentence.getWords().size(); j++) {
	        	if(_sentence.getWords().get(j).getPOSTag().matches("NN.*")){
	        		boolean found = false;
		    		for (int i = 0; i < entities.size(); i++) {
		    			if(entities.get(i).getSearchedTitle() == null) continue;
		    			if(entities.get(i).getSearchedTitle().contains(_sentence.getWords().get(j).getText())){
		    				words.add(new Word(entities.get(i).getSearchedTitle(), _sentence.getWords().get(j).getPOSTag()));
		    				words.get(words.size()-1).setResource(entities.get(i).getResult());
		    				String[] tmp = entities.get(i).getSearchedTitle().split("\\s");
		    				j += tmp.length-1;
		    				found = true;
		    				break;
		    			}
		    		}
		    		if(!found)
		    			words.add(_sentence.getWords().get(j));
	        	} else {
	        		words.add(_sentence.getWords().get(j));
	        	}
			}
	        
	        HashMap<String, Integer> positionMap = new HashMap<>();
	        for (int i = 0; i < words.size(); i++) {
	        	if(words.get(i).getPOSTag().equals("NNS"))
	        		words.get(i).setPOSTag("NN");
	        	if(words.get(i).getPOSTag().matches("VB.*"))
	        		words.get(i).setPOSTag("VB");
	        	
				if(!positionMap.containsKey(words.get(i).getPOSTag())){
					positionMap.put(words.get(i).getPOSTag(), 0);
				}
				words.get(i).setTagPosition(positionMap.get(words.get(i).getPOSTag()));
				positionMap.put(words.get(i).getPOSTag(), positionMap.get(words.get(i).getPOSTag()) + 1);
			
				words.get(i).setSynonyms(SynonymCrawler.findSynonyms(words.get(i)));
				
	        	if(words.get(i).getPOSTag().matches("NN.*") && words.get(i).getResource() != null){
	        		DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence, words.get(i).getResource());
	        		words.get(i).setAlias(pl.findPropertyForName("wikiPageRedirects"));
	        	}
	        }
	        
	        GeneralQuery generalQuery = new GeneralQuery();
	        String[] query = answer.getQuery().split("\\s");
	        for (int i = 0; i < query.length; i++) {
	        	String prefix = "";
	        	if(!query[i].contains("http") && query[i].contains(":")){
	        		prefix = query[i].replaceAll(":.*", "")+":";
	        		query[i] = query[i].replaceAll(".*:", "");
	        	}
	        	
	        	if(query[i].equals("")){
	        		if(!prefix.equals(":"))
	        			query[i] = prefix;
	        		continue;
	        	}
	        	
	        	for (int j = 0; j < words.size(); j++) {
	        		System.out.println(query[i]+" "+words.get(j).getText()+" "+Levenshtein.normalized(query[i], words.get(j).getText()));
	        		if(Levenshtein.normalized(query[i], words.get(j).getText()) < 0.3d){
	        			query[i] = prefix+"<"+words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition())+">";
	        			generalQuery.addPrecondition(words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition()));
//	        			System.out.println(query[i]+" "+words.get(j).getText());
	        		}
	        		if(words.get(j).getSynonyms() != null){
		        		for (int k = 0; k < words.get(j).getSynonyms().size(); k++) {
		        			if(Levenshtein.normalized(query[i], words.get(j).getSynonyms().get(k)) < 0.3d){
			        			query[i] = prefix+"<"+words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition())+">";
			        			generalQuery.addPrecondition(words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition()));
//		        				System.out.println(query[i]+" "+words.get(j).getSynonyms().get(k));
			        		}
						}
	        		}
	        		if(words.get(j).getAlias() != null){
		        		for (int k = 0; k < words.get(j).getAlias().size(); k++) {
		        			if(Levenshtein.normalized(query[i], words.get(j).getAlias().get(k)) < 0.3d){
			        			query[i] = prefix+"<"+words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition())+">";
			        			generalQuery.addPrecondition(words.get(j).getPOSTag()+String.valueOf(words.get(j).getTagPosition()));
//		        				System.out.println(query[i]+" "+words.get(j).getAlias().get(k));
			        		}
						}
	        		}
				}
			}
	        
	        ArrayList<String> tags = new ArrayList<>();
	        for (int i = 0; i < words.size(); i++) {
	        	tags.add(words.get(i).getPOSTag());
			}
	        generalQuery.setPosTags(tags);
	        
	        String gq = "";
	        for (int i = 0; i < query.length; i++) {
				gq += query[i]+" ";
			}
	        generalQuery.setGeneralQuery(gq);
	        generalQueries.add(generalQuery);
	        System.out.println(Arrays.toString(words.toArray()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> answerQuestion(){
		ArrayList<String> res = new ArrayList<>();
		try{
			ArrayList<Word> words = new ArrayList<>();
	        ArrayList<LookupResult> entities = new ArrayList<>();
	        entities.addAll(_sentence.findEntities());
	        for (int j = 0; j < _sentence.getWords().size(); j++) {
	        	if(_sentence.getWords().get(j).getPOSTag().matches("NN.*")){
	        		boolean found = false;
		    		for (int i = 0; i < entities.size(); i++) {
		    			if(entities.get(i).getSearchedTitle() == null) continue;
		    			if(entities.get(i).getSearchedTitle().contains(_sentence.getWords().get(j).getText())){
		    				words.add(new Word(entities.get(i).getSearchedTitle(), _sentence.getWords().get(j).getPOSTag()));
		    				words.get(words.size()-1).setResource(entities.get(i).getResult());
		    				String[] tmp = entities.get(i).getSearchedTitle().split("\\s");
		    				j += tmp.length-1;
		    				found = true;
		    				break;
		    			}
		    		}
		    		if(!found)
		    			words.add(_sentence.getWords().get(j));
	        	} else {
	        		words.add(_sentence.getWords().get(j));
	        	}
			}
	        
	        HashMap<String, Integer> positionMap = new HashMap<>();
	        for (int i = 0; i < words.size(); i++) {
	        	if(words.get(i).getPOSTag().equals("NNS"))
	        		words.get(i).setPOSTag("NN");
	        	if(words.get(i).getPOSTag().matches("VB.*"))
	        		words.get(i).setPOSTag("VB");
	        	
				if(!positionMap.containsKey(words.get(i).getPOSTag())){
					positionMap.put(words.get(i).getPOSTag(), 0);
				}
				words.get(i).setTagPosition(positionMap.get(words.get(i).getPOSTag()));
				positionMap.put(words.get(i).getPOSTag(), positionMap.get(words.get(i).getPOSTag()) + 1);
			
				words.get(i).setSynonyms(SynonymCrawler.findSynonyms(words.get(i)));
				
	        	if(words.get(i).getPOSTag().matches("NN.*") && words.get(i).getResource() != null){
	        		DBPediaPropertyLookup pl = new DBPediaPropertyLookup(_sentence, words.get(i).getResource());
	        		words.get(i).setAlias(pl.findPropertyForName("wikiPageRedirects"));
	        	}
	        }
	        
	        HashMap<String, Character> symbolMap = new HashMap<>();
	        Integer counter = 65;
	        
	        String posStr2 = "";
	        for (int j = 0; j < words.size(); j++) {
				if(!symbolMap.containsKey(words.get(j).getPOSTag())){
					symbolMap.put(words.get(j).getPOSTag(), Character.toChars(counter++)[0]);
				}
				posStr2 += symbolMap.get(words.get(j).getPOSTag());
			}
	        
	        double bestSim = Double.MAX_VALUE;
	        GeneralQuery bestQuery = null;
        	ArrayList<Word> importantWords = new ArrayList<>();

	        for (int i = 0; i < generalQueries.size(); i++) {
	        	boolean allPreconditions = true;
	        	ArrayList<Word> importantWordstmp = new ArrayList<>();
	        	for (int j = 0; j < generalQueries.get(i).getPreconditions().size(); j++) {
	        		boolean match = false;
					for (int k = 0; k < words.size(); k++) {
						if(generalQueries.get(i).getPreconditions().get(j).equals(words.get(k).getPOSTag()+String.valueOf(words.get(k).getTagPosition()))){
							match = true;
							importantWordstmp.add(words.get(k));
							break;
						}
					}
					if(!match){
						allPreconditions = false;
						break;
					}
				}
	        	if(!allPreconditions)	continue;
	        	
	        	String posStr1 = "";
	
	        	for (int j = 0; j < generalQueries.get(i).getPosTags().size(); j++) {
					if(!symbolMap.containsKey(generalQueries.get(i).getPosTags().get(j))){
						symbolMap.put(generalQueries.get(i).getPosTags().get(j), Character.toChars(counter++)[0]);
					}
					posStr1 += symbolMap.get(generalQueries.get(i).getPosTags().get(j));
				}
	        	
	        	if(bestSim > Levenshtein.normalized(posStr1, posStr2)){
	        		bestSim = Levenshtein.normalized(posStr1, posStr2);
	        		bestQuery = generalQueries.get(i);
	        		importantWords = (ArrayList<Word>) importantWordstmp.clone();
	        	}
			}
	        
	        if(bestQuery != null){
	        	String basicQuery = new String(bestQuery.getGeneralQuery());
	        	
	        	HashMap<String, String> replaceMap = new HashMap<>();
	        	ArrayList<ArrayList<String>> fds = new ArrayList<>();
	        	for (int j = 0; j < importantWords.size(); j++) {
	        		replaceMap.put(importantWords.get(j).getPOSTag()+String.valueOf(importantWords.get(j).getTagPosition()), importantWords.get(j).getText().replace(" ", "_"));
	        		fds.add(new ArrayList<>());
					fds.get(fds.size() - 1).add(";"+importantWords.get(j).getText()+"++<"+importantWords.get(j).getPOSTag()+String.valueOf(importantWords.get(j).getTagPosition())+">"+";");
	        		if(importantWords.get(j).getAlias() != null){
						for (int i = 0; i < importantWords.get(j).getAlias().size(); i++) {
							fds.get(fds.size() - 1).add(";"+importantWords.get(j).getAlias().get(i).replaceAll(".*/", "")+"++<"+importantWords.get(j).getPOSTag()+String.valueOf(importantWords.get(j).getTagPosition())+">"+";");
						}
	        		}
	        		if(importantWords.get(j).getSynonyms() != null){
		        		for (int i = 0; i < importantWords.get(j).getSynonyms().size(); i++) {
		        			ArrayList<OntologyClass> tmp1 = DBPediaOntologyCrawler.getOntologyClassByName(importantWords.get(j).getSynonyms().get(i), 0.1);
		        			ArrayList<Property> tmp2 = DBPediaOntologyCrawler.getOntologyPropertyByName(importantWords.get(j).getSynonyms().get(i), 0.1);

		        			for (int k = 0; k < tmp1.size(); k++) {
		        				fds.get(fds.size() - 1).add(";"+tmp1.get(k).getName().replaceAll(".*:", "")+"++<"+importantWords.get(j).getPOSTag()+String.valueOf(importantWords.get(j).getTagPosition())+">"+";");
							}
		        			for (int k = 0; k < tmp2.size(); k++) {
		        				fds.get(fds.size() - 1).add(";"+tmp2.get(k).getName().replaceAll(".*:", "")+"++<"+importantWords.get(j).getPOSTag()+String.valueOf(importantWords.get(j).getTagPosition())+">"+";");
							}
						}
	        		}
	        	}
	        	
	        	ArrayList<String> permutations = new ArrayList();
	        	Permutation.GeneratePermutations(fds, permutations, 0, "");
	        	
	        	for (int i = 0; i < Math.min(permutations.size(), 20); i++) {
	        		try{
					String[] matches = permutations.get(i).split(";;");
					String currentQuery = new String(basicQuery);
					
					for (int j = 0; j < matches.length; j++) {
						matches[j] = matches[j].replace(";", "");
						String[] split = matches[j].split("\\+\\+");
						
						currentQuery = currentQuery.replace(split[1], split[0].replace(" ", "_"));
					}

					res = DBPediaWrapper.queryWOPrefix(currentQuery);
	        		}catch(Exception e){
	        		}

	        		if(res.size() > 0)
	        			return res;
				}
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
		return res;
	}
	
	public ArrayList<String> parse(StanfordSentence s) throws Exception {
		this._sentence = s;
		
//		if(this._sentence.getAnswer() == null)	return this.answerQuestion();
//		else 									this.analyzeQuestion();
//		return new ArrayList<>();
		return parseInternal();
	}
	
	protected abstract ArrayList<String> parseInternal() throws Exception;

	protected boolean isValidType(String resource, ArrayList<String> validTypes){
		if(validTypes.size() == 0) return true;
		
		if(resource.matches("http:.*")){
			ArrayList<String> types = DBPediaWrapper.getTypeOfResource(resource);
			
			boolean validType = false;
			for (String type : types) {
				if(validTypes.contains(type))
					return true;
			}
		}
		return false;
	}
	
	protected ArrayList<String> prepareForReturn(ArrayList<String> result, ArrayList<String> validTypes){
		ArrayList<String> ret = new ArrayList<>();
		for (int i = 0; i < Math.min(result.size(), 10); i++) {
			result.set(i, result.get(i).replaceAll("\\^\\^http:.*", ""));
			
			if(!ret.contains(result.get(i))&& !result.get(i).matches("http:.*:.*")){
				if(isValidType(result.get(i), validTypes))
					ret.add(result.get(i));
			}
		}
		return ret;
	}
	
    public static OntologyClass getBestOntologyClass(ArrayList<OntologyClass> props, String search){
    	double best = Double.MAX_VALUE;
    	OntologyClass prop = null;
    	for(int i=0; i<props.size(); i++){
    		double d = Levenshtein.normalized(props.get(i).getName(), search);
    		if(d<best){
    			best = d;
    			prop = props.get(i);
    		}
    	}
    	return prop;
    }
}
