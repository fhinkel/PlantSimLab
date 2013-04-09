package bmv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class represents a trajectory result (as opposed to a cycle result)
 * created by running an Experiment.
 * 
 * @author plvines
 * 
 */
public class TrajResult extends Result {
	String trajName;

	/**
	 * PRE: file and model are defined
	 * POST: this TrajResult is initialized based on the input model and
	 * scanning the file as a trajectory result file
	 * 
	 * @param file
	 * @param model
	 */
	public TrajResult(File file, Model model) {
		Scanner scan;
		this.model = model;
		try {
			scan = new Scanner(file);
			modelName = scan.next();
			trajName = scan.next();

			scan.findWithinHorizon("Length:", 0);
			pathLength = scan.nextInt();
			scan.findWithinHorizon("Length:", 0);
			cycleLength = scan.nextInt();
			totalLength = pathLength + cycleLength;
			result = new double[totalLength][model.getNodes().size()];

			scan.close();
			scan = new Scanner(file);
			scan.findWithinHorizon("Length:", 0);
			scan.nextLine();
			for (int i = 0; i < pathLength; i++) {
				result[i] = parseState(scan);
			}
			scan.findWithinHorizon("Length:", 0);
			scan.nextLine();
			for (int i = pathLength; i < totalLength; i++) {
				result[i] = parseState(scan);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * PRE: states and model are defined
	 * POST: this TrajResult is initialized to the states passed in and model,
	 * the cycleLength = 0, and pathLength = states.length. This is used for
	 * holding randomResults which have no limit cycle component
	 * 
	 * @param states
	 * @param model
	 */
	public TrajResult(double[][] states, Model model) {
		this.model = model;
		modelName = model.getModelName();

		pathLength = states.length;
		cycleLength = 0;
		totalLength = pathLength;
		result = new double[totalLength][states[0].length];
		for (int i = 0; i < totalLength; i++) {
			for (int k = 0; k < result[0].length; k++) {
				result[i][k] = states[i][k];
			}
		}
	}

	/**
	 * PRE: input and model are defined
	 * POST: this TrajResult is initialized based on model and the string input
	 * treated as a trajectory result
	 * 
	 * @param input
	 * @param model
	 */
	public TrajResult(String input, Model model) {
		this.model = model;
		Scanner scan = new Scanner(input);

		modelName = scan.next();
		trajName = scan.next();

		scan.findWithinHorizon("Length:", 0);
		pathLength = scan.nextInt();
		scan.findWithinHorizon("Length:", 0);
		cycleLength = scan.nextInt();
		totalLength = pathLength + cycleLength;
		result = new double[totalLength][model.getNodes().size()];

		scan.close();
		scan = new Scanner(input);
		scan.findWithinHorizon("Length:", 0);
		for (int i = 0; i < pathLength; i++) {
			result[i] = parseState(scan);
		}
		scan.findWithinHorizon("Length:", 0);
		for (int i = pathLength; i < totalLength; i++) {
			result[i] = parseState(scan);
		}
	}

	/**
	 * PRE: orig is defined
	 * POST: this TrajResult is a deep copy of orig
	 * 
	 * @param orig
	 */
	public TrajResult(TrajResult orig) {
		model = orig.getModel();
		modelName = orig.getModelName();
		trajName = orig.getTrajName();
		cycleLength = orig.getCycleLength();
		pathLength = orig.getPathLength();
		totalLength = orig.getTotalLength();
		result = new double[totalLength][model.getNodes().size()];
		for (int i = 0; i < totalLength; i++) {
			for (int k = 0; k < result[0].length; k++) {
				result[i][k] = orig.result[i][k];
			}
		}
	}

	/**
	 * PRE: scan is defined and contains a file or string that conforms to the
	 * trajectory result standards
	 * POST: the states of the result contained in scan are returned
	 * 
	 * @param scan
	 * @return
	 */
	private double[] parseState(Scanner scan) {
		double[] state = new double[model.getNodes().size()];
		String token = scan.next();
		while (!token.equals("[")) {
			token = scan.next();
		}

		for (int i = 0; i < state.length; i++) {
			state[i] = scan.nextDouble();
		}
		return state;
	}

	/**
	 * PRE: other is defined, this is defined
	 * POST: RV = true if samePath(other) and sameCycle(other) are true
	 * 
	 * @param other
	 * @return
	 */
	public boolean sameTraj(TrajResult other) {
		boolean same = true;
		same = samePath(other) && sameCycle(other);
		return same;
	}

	/**
	 * PRE: other and this are defined
	 * POST: RV = true if every state in path of other and this are the same
	 * 
	 * @param other
	 * @return
	 */
	public boolean samePath(TrajResult other) {
		boolean same = other.getPathLength() == this.pathLength;
		for (int i = 0; same && i < this.pathLength; i++) {
			same = sameState(result[i], other.result[i]);
		}

		return same;
	}

	/**
	 * PRE: this and other are defined
	 * POST: RV = true if every state in cycle of this and other are the same,
	 * the order does not need to be the same
	 * 
	 * @param other
	 * @return
	 */
	public boolean sameCycle(TrajResult other) {
		boolean same = other.getCycleLength() == this.cycleLength;

		for (int i = 0; same && i < cycleLength; i++) {
			same = false;
			for (int k = 0; !same && k < other.getCycleLength(); k++) {
				same = same || sameState(result[i], other.result[i]);
			}
		}

		return same;
	}

	/**
	 * PRE: state1 and state2 are defined
	 * POST: RV = true if state1 = state2, false otherwise
	 * 
	 * @param state1
	 * @param state2
	 * @return
	 */
	public boolean sameState(double[] state1, double[] state2) {
		boolean same = true;

		for (int i = 0; same && i < state1[i]; i++) {
			same = Double.valueOf(state1[i]).intValue() == Double.valueOf(
					state2[i]).intValue();
		}
		return same;
	}

	/**
	 * PRE: trajName is defined
	 * POST: RV = trajName
	 */
	public String getTrajName() {
		return trajName;
	}

	/**
	 * PRE: trajName is defined
	 * POST: trajName = trajName
	 */
	public void setTrajName(String trajName) {
		this.trajName = trajName;
	}

	public String toString() {
		return modelName + " " + trajName + " " + pathLength + " "
				+ cycleLength + " " + result;
	}
}
