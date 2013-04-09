package bmv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * This class holds all the terms available for selection as state names, sorted
 * into arrays based on the number of states. The terms are grouped into blocks
 * of related terms with enumerated values (i.e. "low", "medium", "high", or
 * "Off" and "On"), and each block is of a certain size, which groups it into
 * the array for that many states
 * 
 * @author plvines
 * 
 */
public class StateVocabulary {

	ArrayList<ArrayList<Term>> termSets;
	ArrayList<Integer> indexByLastUse;
	Model model;

	/**
	 * PRE: this object is undefined
	 * POST: this object has default vocabulary terms loaded in according to a
	 * file "Resources/vocab.cfg"
	 */
	public StateVocabulary() {
		termSets = new ArrayList<ArrayList<Term>>();
		readConfig();
	}

	/**
	 * PRE: this object is undefined
	 * POST: this object has default vocabulary terms loaded in according to a
	 * file "Resources/vocab.cfg" as well as those in a file
	 * "Models/MODELNAME/vocab.csv"
	 */
	public StateVocabulary(Model model) {
		this.model = model;
		termSets = new ArrayList<ArrayList<Term>>();
		readConfig();
	}

	private void readConfig() {
		File loadFile = new File("Resources/vocab.cfg");
		try {
			Scanner scan = new Scanner(loadFile);
			scan.useDelimiter(Pattern.compile("(\t|\n)+"));
			int numberOfStates;
			int value;
			String next = scan.next();
			while (scan.hasNext()) {
				numberOfStates = Integer.parseInt(next);
				termSets.add(new ArrayList<Term>());
				next = scan.next();
				while (scan.hasNext()
						&& !next.equals("" + (numberOfStates + 1))) {
					value = scan.nextInt();
					termSets.get(numberOfStates - 2).add(new Term(next, value));
					if (scan.hasNext()) {
						next = scan.next();
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * PRE: numberOfStates is defined
	 * POST: an empty array of terms has been added to the termSets array at
	 * index numberOfStates -2
	 * 
	 * @param numberOfStates
	 */
	public void addTermSets(int numberOfStates) {
		for (int i = termSets.size() - 1; i < numberOfStates - 2; i++) {
			termSets.add(new ArrayList<Term>());
		}
	}

	/**
	 * PRE: numStates and newTerm are defined
	 * POST: newTerm has been added to the front of the array of terms for
	 * numStates
	 * 
	 * @param numStates
	 * @param newTerm
	 */
	public void addTermToFront(int numStates, Term newTerm) {
		ArrayList<Term> set = termSets.get(numStates - 2);

		if (!set.contains(newTerm)) {
			set.add(0, newTerm);
		}
	}

	/**
	 * PRE: numberOfStates is defined
	 * POST: returns the arrayList of terms at numberOfStates -2 index of
	 * termSets
	 * 
	 * @param numberOfStates
	 * @return
	 */
	public ArrayList<Term> getTermSet(int numberOfStates) {
		return termSets.get(numberOfStates - 2);
	}

	/**
	 * PRE: numberOfStates and word are defined
	 * POST: returns the term with term.word = word in the array of
	 * numberOfStates-2 in termSets
	 * 
	 * @param numberOfStates
	 * @param word
	 * @return
	 */
	public Term getTerm(int numberOfStates, String word) {
		Term result = null;
		ArrayList<Term> termSet = termSets.get(numberOfStates - 2);
		for (int i = 0; i < termSet.size(); i++) {
			if (word.equals(termSet.get(i).getWord())) {
				result = termSet.get(i);
			}
		}
		return result;
	}

	/**
	 * PRE: numberOfStates and value are defined
	 * POST: returns the term with term.value = value from the array in termSets
	 * at numberOfStates -2
	 * 
	 * @param numberOfStates
	 * @param value
	 * @return
	 */
	public Term getTerm(int numberOfStates, int value) {
		Term result = null;
		ArrayList<Term> termSet = termSets.get(numberOfStates - 2);
		for (int i = 0; i < termSet.size(); i++) {
			if (termSet.get(i).getValue() == value) {
				result = termSet.get(i);
			}
		}
		return result;
	}

	/**
	 * PRE: numberOfStates and index are defined
	 * POST: moves the term at index to the front of its array
	 * 
	 * @param numberOfStates
	 * @param index
	 */
	public void updateLastUsed(int numberOfStates, int index) {
		termSets.get(numberOfStates).add(
				termSets.get(numberOfStates).remove(index));
	}

	/**
	 * PRE: termSets is defined
	 * POST: RV = termSets
	 */
	public ArrayList<ArrayList<Term>> getTermSets() {
		return termSets;
	}

	/**
	 * PRE: termSets is defined
	 * POST: termSets = termSets
	 */
	public void setTermSets(ArrayList<ArrayList<Term>> termSets) {
		this.termSets = termSets;
	}

}
