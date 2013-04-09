package bmv;

/**
 * Class for terms, basically a struct that contains the string and int for the
 * term
 * 
 * @author plvines
 * 
 */
public class Term {
	private String word;
	private int value;

	public Term() {
		word = "";
		value = 0;
	}

	public Term(String word) {
		this.word = word;
		this.value = -1;
	}

	public Term(String word, int value) {
		this.word = word;
		this.value = value;
	}

	public Term(Term orig) {
		this.word = "" + orig.getWord();
		this.value = orig.getValue();
	}

	/**
	 * PRE: word is defined
	 * POST: RV = word
	 */
	public String getWord() {
		return word;
	}

	/**
	 * PRE: word is defined
	 * POST: word = word
	 */
	public void setWord(String word) {
		this.word = word;
	}

	/**
	 * PRE: value is defined
	 * POST: RV = value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * PRE: value is defined
	 * POST: value = value
	 */
	public void setValue(int value) {
		this.value = value;

	}

	public boolean equals(Object obj) {
		boolean result = false;

		if (obj.getClass() == Term.class) {
			result = (((Term) obj).getValue() == this.getValue() && ((Term) obj)
					.getWord().equals(this.getWord()));
		}

		return result;
	}

	public int hashCode() {
		int hash = 0;

		for (int i = 0; i < word.length(); i++) {
			hash = word.charAt(i) + (hash << 6) + (hash << 16) - hash;
		}

		return hash;
	}

	public String toString() {
		return word;
	}
}
