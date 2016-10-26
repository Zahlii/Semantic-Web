package de.uni_mannheim.semantic.web.stanford_nlp.helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Lists;

public class PowerSet {
	public static <T> Set<List<T>> powerSet(List<T> originalSet) {
		Set<List<T>> sets = new HashSet<List<T>>();
		if (originalSet.isEmpty()) {
			sets.add(new ArrayList<T>());
			return sets;
		}
		List<T> list = new ArrayList<T>(originalSet);
		T head = list.get(0);
		List<T> rest = list.subList(1, list.size());
		for (List<T> set : powerSet(rest)) {
			List<T> newSet = new ArrayList<T>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	public static void main(String[] args) {

		List<String> mySet = Lists.newArrayList("Who was John F. Kennedys vice president".split(" "));

		Permutation<String> p = new Permutation<String>(mySet);

		for (List<String> s : PowerSet.powerSet(mySet)) {
			System.out.println(s);
		}

		while (p.hasNext()) {
			List<String> perm = p.next();
			System.out.println(perm);
		}
	}
}
