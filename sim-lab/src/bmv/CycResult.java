package bmv;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * A subclass of result for a cycle resulting from a model simulation, rather
 * than a trajectory.
 * 
 * @author plvines
 * 
 */
public class CycResult extends Result {

	double componentSize;

	/**
	 * FILE-BASED CONSTRUCTOR
	 * PRE: file, cycNum, and model are defined, cycNum < number of cycles in
	 * file
	 * POST: this CycResult has been initialized based on the cycNum-th cycle
	 * listed in the file.
	 * 
	 * @param file
	 * @param cycNum
	 */
	public CycResult(File file, int cycNum, Model model) {
		Scanner scan;
		this.model = model;
		try {
			scan = new Scanner(file);
			modelName = scan.next();

			pathLength = 0;

			// scan through cycNum number of cycles in the file
			for (int i = 0; i <= cycNum; i++) {
				scan.findWithinHorizon("=", 0);
			}
			componentSize = scan.nextDouble();

			scan.findWithinHorizon("Length:", 0);
			cycleLength = scan.nextInt();
			totalLength = cycleLength;
			result = new double[totalLength][model.getNodes().size()];
			scan.close();

			// scan through cycNum number of cycles in the file
			scan = new Scanner(file);
			for (int i = 0; i <= cycNum; i++) {
				scan.findWithinHorizon("Length:", 0);
			}

			// scan each state in the cycle and add it to the result
			scan.nextLine();
			for (int i = 0; i < cycleLength; i++) {
				result[i] = parseState(scan);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * STRING-BASED CONSTRUCTOR
	 * PRE: input, cycNum, and model are defined, cycNum < number of cycles
	 * contained in input
	 * POST: this CycResult has been initialized to the cycNum-th cycle in input
	 * 
	 * @param input
	 */
	public CycResult(String input, int cycNum, Model model) {
		this.model = model;
		Scanner scan = new Scanner(input);
		modelName = scan.next();

		pathLength = 0;

		// scan through cycNum number of cycles in the input
		for (int i = 0; i < cycNum; i++) {
			scan.findWithinHorizon("=", 0);
		}
		componentSize = scan.nextDouble();

		scan.findWithinHorizon("Length:", 0);
		cycleLength = scan.nextInt();
		totalLength = cycleLength;
		result = new double[totalLength][model.getNodes().size()];
		scan.close();

		// scan through cycNum number of cycles in the input
		scan = new Scanner(input);
		for (int i = 0; i < cycNum; i++) {
			scan.findWithinHorizon("Length:", 0);
		}
		// scan each state in the cycle and add it to the result
		scan.nextLine();
		for (int i = 0; i < cycleLength; i++) {
			result[i] = parseState(scan);
		}
	}

	/**
	 * PRE: orig is defined
	 * POST: this is a deep copy of orig
	 * 
	 * @param orig
	 */
	public CycResult(CycResult orig) {
		model = orig.getModel();
		modelName = orig.getModelName();
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
	 * PRE: scan is defined and contains a string "[ #.# #.# #.# ..." with a
	 * number of numbers at least equal to the number of nodes in the model
	 * POST: scan has been parsed to put the list of numbers into the double[]
	 * state, which is returned
	 * 
	 * @param scan
	 * @return
	 */
	private double[] parseState(Scanner scan) {
		double[] state = new double[model.getNodes().size()];
		while (!scan.next().equals("[")) {
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
	public boolean sameTraj(CycResult other) {
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
	public boolean samePath(CycResult other) {
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
	public boolean sameCycle(CycResult other) {
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
	 * PRE: componentSize is defined
	 * POST: RV = componentSize
	 */
	public double getComponentSize() {
		return componentSize;
	}

	/**
	 * PRE: componentSize is defined
	 * POST: componentSize = componentSize
	 */
	public void setComponentSize(double componentSize) {
		this.componentSize = componentSize;
	}

	public String toString() {
		return modelName + " " + componentSize + "%" + " " + pathLength + " "
				+ cycleLength + " " + result;
	}
}
