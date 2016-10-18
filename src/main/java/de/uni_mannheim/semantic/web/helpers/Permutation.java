package de.uni_mannheim.semantic.web.helpers;

import java.util.*;

class Permutation<E> implements Iterator<List<E>> {

	private List<E> arr;
	private int[] ind;
	private boolean has_next;

	public List<E> output;// next() returns this array, make it public

	Permutation(List<E> arr) {
		this.arr = new ArrayList<E>(arr);
		ind = new int[arr.size()];
		// convert an array of any elements into array of integers - first
		// occurrence is used to enumerate
		Map<E, Integer> hm = new HashMap<E, Integer>();
		for (int i = 0; i < arr.size(); i++) {
			Integer n = hm.get(arr.get(i));
			if (n == null) {
				hm.put(arr.get(i), i);
				n = i;
			}
			ind[i] = n.intValue();
		}
		Arrays.sort(ind);// start with ascending sequence of integers

		// output = new E[arr.length]; <-- cannot do in Java with generics, so
		// use reflection
		output = new ArrayList<E>(arr.size());
		has_next = true;
	}

	@Override
	public boolean hasNext() {
		return has_next;
	}

	/**
	 * Computes next permutations. Same array instance is returned every time!
	 * 
	 * @return
	 */
	@Override
	public List<E> next() {
		if (!has_next)
			throw new NoSuchElementException();

		output.clear();

		for (int i = 0; i < ind.length; i++) {
			output.add(i, arr.get(ind[i]));
		}

		// get next permutation
		has_next = false;
		for (int tail = ind.length - 1; tail > 0; tail--) {
			if (ind[tail - 1] < ind[tail]) {// still increasing

				// find last element which does not exceed ind[tail-1]
				int s = ind.length - 1;
				while (ind[tail - 1] >= ind[s])
					s--;

				swap(ind, tail - 1, s);

				// reverse order of elements in the tail
				for (int i = tail, j = ind.length - 1; i < j; i++, j--) {
					swap(ind, i, j);
				}
				has_next = true;
				break;
			}

		}
		return output;
	}

	private void swap(int[] arr, int i, int j) {
		int t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}

	@Override
	public void remove() {

	}
}