package de.uni_mannheim.semantic.web.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PowerSet {	
	private static <T> List<List<T>> generatePerm(List<T> original) {
	  	if (original.size() == 0) { 
    	 	List<List<T>> result = new ArrayList<List<T>>();
       		result.add(new ArrayList<T>());
       		return result;
     	}
     	T firstElement = original.remove(0);
     	List<List<T>> returnValue = new ArrayList<List<T>>();
	 	List<List<T>> permutations = generatePerm(original);
	 	for (List<T> smallerPermutated : permutations) {
		 	for (int index=0; index <= smallerPermutated.size(); index++) {
			 	List<T> temp = new ArrayList<T>(smallerPermutated);
		     	temp.add(index, firstElement);
		     	returnValue.add(temp);
		 	}
	 	}
	 	return returnValue;
   	}
	
	private static <T> List<List<T>> powerSet(List<T> originalSet) {
		List<List<T>> sets = new ArrayList<List<T>>();
	    if (originalSet.isEmpty()) {
	    	sets.add(new ArrayList<T>());
	    	return sets;
	    }
	    List<T> list = new ArrayList<T>(originalSet);
	    T head = list.get(0);
	    List<T> rest = new ArrayList<T>(list.subList(1, list.size())); 
	    for (List<T> set : powerSet(rest)) {
	    	List<T> newSet = new ArrayList<T>();
	    	newSet.add(head);
	    	newSet.addAll(set);
	    	sets.add(newSet);
	    	sets.add(set);
	    }		
	    return sets;
	}
	
	public static List<List<String>> createPowerSet(ArrayList<String> strings){
		List<List<String>> ret = new ArrayList<>();
		for (List<String> s : PowerSet.powerSet(strings)) {
			List<List<String>> str = generatePerm(s);
			ret.addAll(str);
		 }
		return ret;
	}
	
	public static void main(String[] args) {
		 ArrayList<String> mySet = new ArrayList<String>();
		 mySet.add("t1");
		 mySet.add("t2");
		 mySet.add("t3");
		 System.out.println(createPowerSet(mySet));
	}
}
