package bmv;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import bmv.Node.TABLE_STATUS;

/**
 * This class extends JTable to handle the update table for each node. It
 * contains X + columns, where X is the number of incoming edges to the node.
 * Only the rightmost column can be edited; it consists of a combobox selection
 * of the terms selected for use with this node.
 * The class also tracks which rows have been edited by the user so far, and
 * will attempt to change weighting of edges to produce a prediction rule that
 * matches the highest numbered state edited so far and all the ones below it,
 * and use this prediction to pre-fill-out higher states
 * 
 * @author plvines
 * 
 */
public class NodeTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final int NOT_CONVERGING = -1;
	private boolean[] userEdited;
	private int maxStates;
	private Model context;
	private Node node;
	private DefaultTableModel data;
	private int speed;
	private Thread adjustThread;
	protected int bestFit;
	protected double[] weights;
	protected double[] bestWeights;
	protected double[] originalWeights;
	protected boolean stopAdjustments;
	protected TableCellListener storedTCL;
	protected double[] originalTableWeights;

	/**
	 * PRE: node, tableString ,and context are defined
	 * POST: this NodeTable has been initialized based on the number of edges
	 * coming into node and the tableString, because the table was loaded from a
	 * file, it is assumed that all states have been edited by the user earlier
	 * 
	 * @param node
	 * @param tableString
	 * @param context
	 */
	public NodeTable(Node node, String tableString, Model context) {
		super();
		this.maxStates = context.getMaxStates();
		this.context = context;
		this.node = node;
		initializeTableFromString(tableString);
		userEdited = new boolean[getRowCount()];
		for (int i = 0; i < getRowCount(); i++) {
			userEdited[i] = true;
		}

		stopAdjustments = false;
	}

	/**
	 * PRE: orig is defined
	 * POST: this NodeTable is a realtively shallow copy of orig; the reference
	 * to data is copied
	 * 
	 * @param orig
	 */
	public NodeTable(NodeTable orig) {
		super();
		this.maxStates = orig.getMaxStates();
		this.context = orig.getContext();
		this.node = orig.getNode();
		this.data = orig.getData();
		this.setModel(data);
		this.speed = orig.getSpeed();

		userEdited = new boolean[getRowCount()];
		if (orig.getUserEdited() != null) {
			for (int i = 0; i < orig.getUserEdited().length; i++) {
				userEdited[i] = orig.getUserEdited()[i];
			}
		}

		stopAdjustments = false;
	}

	/**
	 * PRE: node, context, and display are defined
	 * POST: this NodeTable is initialized based on the details of node,
	 * context, and display. If display is true then GUIElements are created,
	 * otherwise they are not.
	 * The table itself is initialized based on the number of incoming nodes to
	 * node and their number of states; if node has a TABLE_STATUS of UP_TO_DATE
	 * then this NodeTable simply copies the node's NodeTable. Otherwise, the
	 * output column is filled via the prediction algorithm
	 * 
	 * @param node
	 * @param context
	 * @param display
	 */
	public NodeTable(Node node, Model context) {
		super();

		this.maxStates = context.getMaxStates();
		this.context = context;
		this.node = node;
		// If up to date, simply copy the node's table and then predict for
		// those values not considered user-edited
		if (node.getTableStatus() == TABLE_STATUS.UP_TO_DATE
				|| node.getStateChanged() != null) {
			NodeTable orig = node.getTable();

			this.data = orig.getData();
			this.setModel(data);

			speed = orig.getSpeed();
			userEdited = new boolean[getRowCount()];
			for (int i = 0; i < ((NodeTable) orig).getUserEdited().length; i++) {
				userEdited[i] = ((NodeTable) orig).getUserEdited()[i];
			}
			predictAllStates();
		}
		// Otherwise, initialize it based on incoming edges and then predict all
		// states
		else if (node.getStateChanged() == null) {
			initializeTableFromNode();
			speed = 4;
		} else {
			speed = node.getTable().getSpeed();
			initializeTableFromNodeWithEstimation(node.getTable(),
					node.getStateChanged());
		}

		@SuppressWarnings("serial")
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				TableCellListener tcl = (TableCellListener) e.getSource();
				if (valueIsProper(tcl.getOldValue(), tcl.getNewValue(),
						tcl.getRow())) {
					adjust(tcl);
				} else {
					setValueAt(tcl.getOldValue(), tcl.getRow(), tcl.getColumn());
				}
			}
		};
		addPropertyChangeListener(new TableCellListener(this, action));

		stopAdjustments = false;
	}

	/**
	 * PRE: node, context, and panel have been defined
	 * POST: this NodeTable has been defined and initialized based upon the
	 * NodeTable object held in Node, and then had display elements, such as the
	 * combobox cell editor, added for use in the panel
	 * 
	 * @param node
	 * @param context
	 * @param panel
	 */
	public NodeTable(Node node, Model context, TablePanel panel) {
		super();
		originalTableWeights = new double[node.getInEdges().size()];
		for (int i = 0; i < originalTableWeights.length; i++) {
			originalTableWeights[i] = node.getInEdges().get(i).getWeight();
		}
		this.maxStates = context.getMaxStates();
		this.context = context;
		this.node = node;
		// If up to date, simply copy the node's table and then predict for
		// those values not considered user-edited
		if (node.getTableStatus() == TABLE_STATUS.UP_TO_DATE) {
			NodeTable orig = node.getTable();

			this.data = orig.getData();
			this.setModel(data);
			this.speed = orig.getSpeed();

			userEdited = new boolean[getRowCount()];
			for (int i = 0; i < ((NodeTable) orig).getUserEdited().length; i++) {
				userEdited[i] = ((NodeTable) orig).getUserEdited()[i];
			}
			predictAllStates();
		}
		// Otherwise, initialize it based on incoming edges and then predict all
		// states
		else {
			if (node.getStateChanged() == null) {
				speed = 4;
				initializeTableFromNode();
			} else {
				speed = node.getTable().getSpeed();
				initializeTableFromNodeWithEstimation(node.getTable(),
						node.getStateChanged());
			}
		}

		@SuppressWarnings("serial")
		Action action = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				TableCellListener tcl = (TableCellListener) e.getSource();
				if (valueIsProper(tcl.getOldValue(), tcl.getNewValue(),
						tcl.getRow())) {
					adjust(tcl);
				} else {
					setValueAt(tcl.getOldValue(), tcl.getRow(), tcl.getColumn());
				}
			}
		};
		addPropertyChangeListener(new TableCellListener(this, action));

		requestFocusInWindow();
		transferFocus();
		setRowHeight(20);
		setFillsViewportHeight(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setPreferredScrollableViewportSize(new Dimension(Math.min(500,
				getColumnModel().getTotalColumnWidth()), Math.min(500,
				getRowCount() * 20)));
		getTableHeader().setReorderingAllowed(false);

		addComboBoxEditor();
		for (int i = 0; i < getRowCount(); i++) {
			((TermComboBoxRenderer) this.getCellRenderer(i,
					getColumnCount() - 1)).setSelectedIndex(((Term) this
					.getValueAt(i, getColumnCount() - 1)).getValue());
		}

		stopAdjustments = false;
	}

	/**
	 * This private class runs on a separate thread to attempt to exhaustively
	 * search and find the combination of weights for the incoming edges that
	 * produces a perfect fit with all states edited so far
	 * 
	 * @author plvines
	 * 
	 */
	private class WeightAdjuster implements Runnable {

		/**
		 * PRE: weights, originalWeight, bestWeights, bestFit, and this table
		 * are defined
		 * POST: goes through all permutations of weightings in .1 increments to
		 * find the best fit.
		 */
		@Override
		public void run() {
			for (int i = 0; i < weights.length; i++) {
				weights[i] = -2;
			}

			for (int i = 0; i < originalWeights.length; i++) {
				originalWeights[i] = node.getInEdges().get(i).getWeight();
			}

			int index = 0;

			while (bestFit > 0 && index >= 0 && !stopAdjustments) {

				for (int i = 0; i < index; i++) {
					weights[i] = -2;
				}
				for (int i = 0; i < weights.length; i++) {
					node.getInEdges().get(i).setWeight(weights[i]);
				}
				int fit = checkFit();
				if (fit < bestFit) {
					bestFit = fit;
					bestWeights = weights.clone();
				}
				index = permute(weights, 0, .1, 2);
				// index = -1 if permute is finished
			}
			if (stopAdjustments) {
				bestFit = Integer.MAX_VALUE;
			}
			makeAdjustment();

		}
	}

	/**
	 * PRE: this object is defined, the node.getTermsUsed is defined
	 * POST: the comboboxEditor has been added and populated with the terms in
	 * node.getTermsUsed
	 */
	private void addComboBoxEditor() {
		Term[] terms = new Term[node.getTermsUsed().size()];
		node.getTermsUsed().toArray(terms);
		TableColumn col = this.getColumnModel().getColumn(getColumnCount() - 1);
		TermComboBox editor = new TermComboBox(terms);
		col.setCellEditor(editor);
		col.setCellRenderer(new TermComboBoxRenderer(terms));
	}

	/**
	 * this private class is the customized renderer for displaying the combo
	 * box terms in the table
	 * 
	 * @author plvines
	 * 
	 */
	@SuppressWarnings("serial")
	private class TermComboBoxRenderer extends JComboBox implements
			TableCellRenderer {
		public TermComboBoxRenderer(Term[] items) {
			super(items);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				super.setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			// Select the current value

			setSelectedItem(((Term) value));
			return this;
		}
	}

	@SuppressWarnings("serial")
	public class TermComboBox extends DefaultCellEditor {
		public TermComboBox(Term[] items) {
			super(new JComboBox(items));
		}
	}

	/**
	 * PRE: newValue, oldValue, context.continuous and node.numStates are all
	 * defined
	 * POST: RV = true if newValue < node.numStates, and, if continuous, then
	 * |table[row][#cols -2] - newValue| <= 1 or if either oldValue or newValue
	 * are not
	 * integers
	 * 
	 * @return
	 */
	private boolean valueIsProper(Object oldValue, Object newValue, int row) {
		if (oldValue != null) {
			int newNum = ((Term) newValue).getValue();

			return ((newNum < node.getNumStates())
					&& (!context.isContinuous() || Math.abs(((Term) getValueAt(
							row, getColumnCount() - 2)).getValue() - newNum) <= 1) && newNum >= 0);
		} else {
			return true;
		}
	}

	/**
	 * PRE: change and all edges going into node are defined
	 * POST: the highest value activating edge and inhibiting edge at that
	 * cell's row are modified to increase or decrease the predicted value to
	 * meet the user's entry
	 * The fit of these new strengths is then checked, and then all
	 * user-unedited states are repredicted
	 * 
	 * @param change
	 */
	private void adjust(TableCellListener change) {
		userEdited[change.getRow()] = true;
		if (node.getInEdges().size() > 0
				&& (storedTCL == null || (storedTCL.getNewValue() != change
						.getNewValue() || storedTCL.getRow() != change.getRow()))) {
			if (adjustThread != null && adjustThread.isAlive()) {
				stopAdjustments = true;
				try {
					adjustThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			bestFit = Integer.MAX_VALUE;
			weights = new double[node.getInEdges().size()];
			bestWeights = weights.clone();
			originalWeights = new double[node.getInEdges().size()];

			stopAdjustments = false;
			adjustThread = new Thread(new WeightAdjuster());
			adjustThread.start();
			storedTCL = change;
		}
	}

	/**
	 * PRE: bestFit, bestWeights, weights, and originalWeights have all been
	 * defined
	 * POST: if bestFit == 0, then bestWeights match edited states perfectly,
	 * so set the edges to those weights and predict all the states so-far
	 * unedited. If bestFit != 0 then set edge weights back to their original
	 * values before permuting
	 */
	public void makeAdjustment() {
		if (bestFit == 0) {
			for (int i = 0; i < weights.length; i++) {
				node.getInEdges().get(i).setWeight(bestWeights[i]);
			}
			predictAllStates();
		} else {
			for (int i = 0; i < originalWeights.length; i++) {
				node.getInEdges().get(i).setWeight(originalWeights[i]);
			}
		}

	}

	/**
	 * PRE: node.getInEdges() is defined
	 * POST: refresh the column names to update upon another node's name being
	 * edited or state terms being changed
	 */
	public void refreshNames() {
		Node curNode;
		Term curTerm;
		for (int i = 0; i < getColumnCount() - 2; i++) {
			curNode = node.getInEdges().get(i).getStart();
			for (int k = 0; k < getRowCount(); k++) {
				curTerm = (Term) data.getValueAt(k, i);
				curTerm.setWord(curNode.getTermsUsed().get(curTerm.getValue())
						.getWord());
			}
		}

		for (int k = 0; k < getRowCount(); k++) {
			curTerm = (Term) data.getValueAt(k, getColumnCount() - 2);
			curTerm.setWord(node.getTermsUsed().get(curTerm.getValue())
					.getWord());
		}

		for (int k = 0; k < getRowCount(); k++) {
			curTerm = (Term) data.getValueAt(k, getColumnCount() - 1);
			curTerm.setWord(node.getTermsUsed().get(curTerm.getValue())
					.getWord());
		}
	}

	/**
	 * PRE: userEdited is defined
	 * POST: resets userEdited to false for all states and then predicts those
	 * states based on the current weightings
	 */
	public void resetEdits() {
		userEdited = new boolean[userEdited.length];
		predictAllStates();
	}

	/**
	 * PRE: all edges, userEdited, and this table are defined
	 * POST: RV = the number of incorrect predictions
	 * 
	 * @return
	 */
	private int checkFit() {
		int fit = 0;
		int furthestEdit = 0;
		for (int i = 0; i < userEdited.length; i++) {
			if (userEdited[i]) {
				furthestEdit = i;
			}
		}

		for (int i = 0; i <= furthestEdit; i++) {
			if (predictState(i) != ((Term) getValueAt(i, getColumnCount() - 1))
					.getValue()) {
				fit++;
			}
		}

		return fit;
	}

	/**
	 * PRE: permutedArray, startIndex, changeValue, and maxValue are all
	 * defined; startIndex < permutedArray.length and changeValue > 0
	 * POST: the value at permutedArray[startIndex] has been changed by
	 * changeValue amount, to a max of maxValue or min of 0. If
	 * permutedArray[startIndex] was already at maxValue or 0, then the next
	 * lowest index of permutedArray not at maxValue has been changed
	 * If no such index could be found, permutedArray is not changed and -1 is
	 * returned.
	 * Otherwise, the index of the variable actually changed is returned
	 * 
	 * @param permutedArray
	 * @param startIndex
	 * @param changeValue
	 * @param maxValue
	 * @return
	 */
	private int permute(double[] permutedArray, int startIndex,
			double changeValue, double maxValue) {
		int permutedIndex = startIndex;
		if (permutedArray[permutedIndex] < maxValue) {
			permutedArray[permutedIndex] += changeValue;
			if (permutedArray[permutedIndex] > maxValue) {
				permutedArray[permutedIndex] = maxValue;
			}
		} else if (permutedIndex < permutedArray.length - 1) {
			permutedIndex = permute(permutedArray, permutedIndex + 1,
					changeValue, maxValue);
		} else {
			permutedIndex = -1;
		}

		return permutedIndex;
	}

	/**
	 * PRE: the table is filled except for the output column, the node has all
	 * edges defined
	 * POST: the output column has been fully filled out based on the
	 * edge strengths and types coming into the node this table describes
	 */
	private void predictAllStates() {
		if (node.getTermsUsed() != null) {
			int[] nodeVals = new int[getColumnCount() - 1];
			ArrayList<Edge> edges = node.getInEdges();

			for (int row = 0; row < getRowCount(); row++) {

				if (row != 0) {
					nodeVals[nodeVals.length - 1] = (nodeVals[nodeVals.length - 1] + 1)
							% node.getNumStates();
					for (int i = nodeVals.length - 2; i >= 0
							&& nodeVals[i + 1] == 0; i--) {
						nodeVals[i] = (nodeVals[i] + 1)
								% edges.get(i).getStart().getNumStates();
					}
				}

				if (!userEdited[row]) {
					int predictedValue = predictState(nodeVals);
					setValueAt(node.getTermsUsed().get(predictedValue), row,
							getColumnCount() - 1);
				}
			}
		}
	}

	/**
	 * PRE: row is defined
	 * POST: row has been converted into an array of values based on the regular
	 * patterning of the non-editable cell values by row, this array has then
	 * been fed into predictState() and the result is returned
	 * 
	 * @param row
	 * @return
	 */
	private int predictState(int row) {
		int[] nodeVals = new int[getColumnCount() - 1];
		for (int i = 0; i < nodeVals.length; i++) {
			nodeVals[i] = ((Term) getValueAt(row, i)).getValue();
		}

		return (predictState(nodeVals));
	}

	/**
	 * PRE: inputs is defined as an array of ints of length = getColumnCount() -
	 * 1 initialized with numbers representing the input value for the state
	 * being predicted
	 * POST: the state is predicted based on edge weights and the input values
	 * and is returned as an int
	 * 
	 * @param inputs
	 * @return
	 */
	private int predictState(int[] inputs) {
		int output = 0;
		double sumActivation = 0, sumInhibition = 0, netChange;
		int desiredVal;
		ArrayList<Edge> edges = node.getInEdges();

		for (int col = 0; col < getColumnCount() - 2; col++) {
			Edge curEdge = edges.get(col);
			if (curEdge.getType() == Edge.ACTIVATING) {
				sumActivation += (((double) inputs[col] * curEdge.getWeight()) / curEdge
						.getStart().getNumStates());
				Math.max(sumActivation, 0);
			} else if (curEdge.getType() == Edge.INHIBITING) {
				sumInhibition += (((double) inputs[col] * (-curEdge.getWeight())) / curEdge
						.getStart().getNumStates());
				Math.max(sumInhibition, 0);
			}
		}
		netChange = ((sumActivation - sumInhibition)
				* (double) node.getNumStates() / 2.0);
		if (netChange < 0) {
			netChange = Math.round(netChange);
		} else {
			netChange = Math.round(netChange);
		}
		desiredVal = (node.getNumStates() / 2) + (int) netChange;
		if (node.getNumStates() == 2) {
			desiredVal = (int) netChange;
		}
		output = inputs[inputs.length - 1]
				+ Math.min(1,
						Math.max(-1, desiredVal - inputs[inputs.length - 1]));
		if (output < 0) {
			output = 0;
		} else if (output >= node.getNumStates()) {
			output = node.getNumStates() - 1;
		}
		return output;
	}

	/**
	 * PRE: userEdited is defined
	 * POST: RV = userEdited
	 */
	public boolean[] getUserEdited() {
		return userEdited;
	}

	/**
	 * PRE: userEdited is defined
	 * POST: userEdited = userEdited
	 */
	public void setUserEdited(boolean[] userEdited) {
		this.userEdited = userEdited;
	}

	/**
	 * PRE: tableHeader is defined and contains just the header of the table
	 * file (the names of the nodes)
	 * POST: the header has been parsed and the array of names returned
	 * 
	 * @param tableHeader
	 * @return
	 */
	private String[] parseHeader(String tableHeader) {
		Scanner scan = new Scanner(tableHeader);
		int numNames = 0;
		while (scan.hasNext()) {
			scan.next();
			numNames++;
		}
		scan = new Scanner(tableHeader);
		String[] names = new String[numNames];
		for (int i = 0; i < numNames; i++) {
			names[i] = scan.next();
		}

		return names;
	}

	/**
	 * PRE: fullTableString and termsUsed for each of the starts of the edges in
	 * node.getInEdges have been defined
	 * POST: fullTableString has been parsed to generate the rightmost column of
	 * the NodeTable, while all other columns have been filled in in the regular
	 * patterning using the terms for each node represented in that column
	 * 
	 * @param fullTableString
	 */
	protected void initializeTableFromString(String fullTableString) {

		Scanner scan = new Scanner(fullTableString);
		scan.next();
		speed = scan.nextInt();
		scan.nextLine();

		String[] columnNames = parseHeader(scan.nextLine());

		int numCols = columnNames.length;
		int numRows = 0;
		while (scan.hasNext()) {
			for (int i = 0; i < numCols; i++) {
				scan.next();
			}
			numRows++;
		}

		Term[][] input = new Term[numRows][numCols];
		int value;
		scan = new Scanner(fullTableString);
		scan.nextLine();
		scan.nextLine();

		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				value = scan.nextInt();
				if (col < numCols - 2) {
					input[row][col] = new Term(node.getInEdges().get(col)
							.getStart().getTermsUsed().get(value));
				} else {
					input[row][col] = new Term(node.getTermsUsed().get(value));
				}
			}
		}

		data = new DefaultTableModel(input, columnNames);
		this.setModel(data);
		userEdited = new boolean[numRows];
		for (int i = 0; i < numRows; i++) {
			userEdited[i] = true;
		}

	}

	/**
	 * PRE: node is defined
	 * POST:constructs the table for a node with the output column based solely
	 * on predicts from the weightings of the edges
	 */
	protected void initializeTableFromNode() {
		// Fill column names
		String[] columnNames = new String[node.getInEdges().size() + 2];
		for (int i = 0; i < node.getInEdges().size(); i++) {
			columnNames[i] = node.getInEdges().get(i).getStart().getAbrevName();
		}
		int numCols = columnNames.length;

		columnNames[numCols - 2] = node.getAbrevName();
		columnNames[numCols - 1] = node.getAbrevName();

		int numRows = node.getNumStates();
		for (Edge edge : node.getInEdges()) {
			numRows *= edge.getStart().getNumStates();
		}

		Term[][] input = new Term[numRows][numCols];

		// Fill input columns
		int blockSize;
		if (node.getInEdges().size() > 0) {
			blockSize = numRows;
		} else {
			blockSize = node.getNumStates();
		}
		int blocksInRow = numRows / blockSize;
		int value;
		for (int a = 0; a < numCols - 2; a++) {
			Node curNode = node.getInEdges().get(a).getStart();
			if (a + 1 < numCols) {
				blockSize /= node.getInEdges().get(a).getStart().getNumStates();
				blocksInRow *= node.getInEdges().get(a).getStart()
						.getNumStates();
			}
			for (int b = 0; b < blocksInRow; b++) {
				for (int c = 0; c < blockSize; c++) {
					value = b
							% node.getInEdges().get(a).getStart()
									.getNumStates();
					input[(b * blockSize) + c][a] = new Term(curNode
							.getTermsUsed().get(value));
				}
			}

		}

		// Self input column
		for (int i = 0; i < numRows; i++) {
			if (node.getTermsUsed() != null) {
				input[i][numCols - 2] = new Term(node.getTermsUsed().get(
						i % node.getNumStates()));
			}
		}

		data = new DefaultTableModel(input, columnNames);
		this.setModel(data);

		userEdited = new boolean[numRows];
		for (int i = 0; i < numRows; i++) {
			userEdited[i] = false;
		}

		predictAllStates();
	}

	/**
	 * PRE: node.getInEdges().getStart() is defined for all node.getInEdges()
	 * POST: the column names of this table have been updated
	 */
	public void updateColumnNames() {
		String[] columnNames = new String[node.getInEdges().size() + 2];
		for (int i = 0; i < node.getInEdges().size(); i++) {
			columnNames[i] = node.getInEdges().get(i).getStart().getAbrevName();
		}
		int numCols = columnNames.length;
		columnNames[numCols - 2] = node.getAbrevName();
		columnNames[numCols - 1] = node.getAbrevName();

		data.setColumnIdentifiers(columnNames);
	}

	/**
	 * PRE: origTable and stateChange are defined
	 * POST: attempts to predict desired states for this NodeTable after a
	 * change in the number of states in the node has changed based on the
	 * original table and how the states changed.
	 * For example: if A becomes med, high, high in 3 states, it will become
	 * low, med, high, very-high, very-high if changed to 5 states
	 * 
	 * @param origTable
	 * @param stateChange
	 */
	protected void initializeTableFromNodeWithEstimation(NodeTable origTable,
			Point stateChange) {
		// Fill column names
		String[] columnNames = new String[node.getInEdges().size() + 2];
		for (int i = 0; i < node.getInEdges().size(); i++) {
			columnNames[i] = node.getInEdges().get(i).getStart().getAbrevName();
		}
		int numCols = columnNames.length;

		columnNames[numCols - 2] = node.getAbrevName();
		columnNames[numCols - 1] = node.getAbrevName();

		int numRows = node.getNumStates();
		for (Edge edge : node.getInEdges()) {
			numRows *= edge.getStart().getNumStates();
		}

		Term[][] input = new Term[numRows][numCols];

		// Fill input columns
		int blockSize;
		if (node.getInEdges().size() > 0) {
			blockSize = numRows;
		} else {
			blockSize = node.getNumStates();
		}
		int blocksInRow = numRows / blockSize;
		int value;
		for (int a = 0; a < numCols - 2; a++) {
			Node curNode = node.getInEdges().get(a).getStart();
			if (a + 1 < numCols) {
				blockSize /= node.getInEdges().get(a).getStart().getNumStates();
				blocksInRow *= node.getInEdges().get(a).getStart()
						.getNumStates();
			}
			for (int b = 0; b < blocksInRow; b++) {
				for (int c = 0; c < blockSize; c++) {
					value = b
							% node.getInEdges().get(a).getStart()
									.getNumStates();
					input[(b * blockSize) + c][a] = new Term(curNode
							.getTermsUsed().get(value));
				}
			}

		}

		// Self input column
		for (int i = 0; i < numRows; i++) {
			if (node.getTermsUsed() != null) {
				input[i][numCols - 2] = new Term(node.getTermsUsed().get(
						i % node.getNumStates()));
			}
		}

		data = new DefaultTableModel(input, columnNames);
		this.setModel(data);

		userEdited = new boolean[numRows];
		for (int i = 0; i < numRows; i++) {
			userEdited[i] = true;
		}

		// if there was a decrease in the number of states
		if (stateChange.y > stateChange.x) {
			int[] origBlock = new int[stateChange.x];
			int[] newBlock = new int[stateChange.y];
			int origTableRow = 0;
			for (int newTableRow = 0; newTableRow < numRows; newTableRow += stateChange.y, origTableRow += stateChange.x) {
				origBlock = getValueBlock(origTable, origTableRow,
						stateChange.x);
				int convergingTowards = findPattern(origBlock);
				convergingTowards *= ((stateChange.y - 1) / (stateChange.x - 1));
				fillBlock(newBlock, convergingTowards);

				for (int i = 0; i < stateChange.y; i++) {
					setValueAt(node.getTermsUsed().get(newBlock[i]),
							newTableRow + i, numCols - 1);
				}
			}
		}

		// increase in number of states
		else {
			int[] origBlock = new int[stateChange.x];
			int[] newBlock = new int[stateChange.y];
			int origTableRow = 0;
			for (int newTableRow = 0; newTableRow < numRows; newTableRow += stateChange.y, origTableRow += stateChange.x) {
				origBlock = getValueBlock(origTable, origTableRow,
						stateChange.x);
				int convergingTowards = findPattern(origBlock);
				convergingTowards *= ((double) (stateChange.y - 1) / (double) (stateChange.x - 1));
				fillBlock(newBlock, convergingTowards);

				for (int i = 0; i < stateChange.y; i++) {
					setValueAt(node.getTermsUsed().get(newBlock[i]),
							newTableRow + i, numCols - 1);
				}
			}
		}
		node.setStateChanged(new Point(stateChange.y, stateChange.y));
	}

	/**
	 * PRE: blockToFill and convergingTowards are defined
	 * POST: if convergingTowards = NOT_CONVERGING then each state in the
	 * blockToFill is set to stay itself; otherwise, blockToFill is filled out
	 * as a series of states moving towards either the medium, maximum, or
	 * minimum state (i.e. very-low, very-low, low, med, high for 5 states)
	 * 
	 * @param blockToFill
	 * @param convergingTowards
	 */
	private void fillBlock(int[] blockToFill, int convergingTowards) {
		if (convergingTowards == NOT_CONVERGING) {
			for (int i = 0; i < blockToFill.length; i++) {
				blockToFill[i] = i;
			}
		} else {
			if (context.isContinuous()) {
				for (int i = 0; i < blockToFill.length; i++) {
					int difference = convergingTowards - i;
					if (difference > 1) {
						difference = 1;
					} else if (difference < -1) {
						difference = -1;
					}
					blockToFill[i] = i + difference;
				}
			} else {
				for (int i = 0; i < blockToFill.length; i++) {
					int difference = convergingTowards - i;
					blockToFill[i] = i + difference;
				}
			}
		}
	}

	/**
	 * PRE: valueBlock is defined
	 * POST: the convergence pattern of valueBlock is returned: NOT_CONVERGING
	 * if there is no pattern in the valueBlock. Or the value converged towards
	 * if there is a pattern
	 * 
	 * @param valueBlock
	 * @return
	 */
	private int findPattern(int[] valueBlock) {
		int convergingTowards = NOT_CONVERGING;

		for (int i = 0; i < valueBlock.length; i++) {
			if (valueBlock[i] > i) {
				convergingTowards = valueBlock[i];
			} else if (valueBlock[i] < i) {
				if (convergingTowards == -1
						|| valueBlock[i] < convergingTowards) {
					convergingTowards = valueBlock[i];
				}
			}
		}

		return convergingTowards;
	}

	/**
	 * PRE: table, startRow, and numState are defined
	 * POST: returns a block of the output column starting at startRow and
	 * moving numState down in the table; converts the term to a value to return
	 * in eahc index of the block
	 * 
	 * @param table
	 * @param startRow
	 * @param numState
	 * @return
	 */
	private int[] getValueBlock(NodeTable table, int startRow, int numState) {
		int[] returnBlock = new int[numState];
		for (int i = 0; i < numState; i++) {
			returnBlock[i] = ((Term) table.getValueAt(startRow + i,
					table.getColumnCount() - 1)).getValue();
		}

		return returnBlock;
	}

	/**
	 * PRE: this nodeTable is defined
	 * POST: returns the full table as a string, using the word for each term in
	 * each cell
	 * 
	 * @return
	 */
	public String toString() {
		String str = "";
		for (int i = 0; i < this.getColumnCount(); i++) {
			str += this.getColumnName(i) + '\t';
		}
		str += '\n';

		for (int i = 0; i < this.getRowCount(); i++) {
			for (int k = 0; k < this.getColumnCount(); k++) {
				str += ((Term) this.getValueAt(i, k)).getWord() + '\t';
			}
			str += '\n';
		}

		return str;
	}

	/**
	 * PRE: this NodeTable is defined
	 * POST: returns the full table with header with the int value of each term
	 * 
	 * @return
	 */
	public String valueString() {
		String str = "";

		str += "SPEED:\t" + speed + "\n";
		for (int i = 0; i < this.getColumnCount(); i++) {
			str += this.getColumnName(i) + '\t';
		}
		str += '\n';

		for (int i = 0; i < this.getRowCount(); i++) {
			for (int k = 0; k < this.getColumnCount(); k++) {
				str += ((Term) this.getValueAt(i, k)).getValue() + "\t";
			}
			str += '\n';
		}

		return str;
	}

	/**
	 * PRE: table and numStates are defined
	 * POST: RV = true if every entry in the final column of table is a number <
	 * numStates
	 * 
	 * @return
	 */
	public boolean isProper() {
		boolean filled = true;
		for (int i = 0; filled && i < getRowCount(); i++) {
			filled = getValueAt(i, getColumnCount() - 1) != null
					&& ((Term) getValueAt(i, getColumnCount() - 1)).getValue() < node
							.getNumStates();
		}

		return filled;
	}

	/**
	 * PRE: row and col are defined
	 * POST: returns true if col is the last column, false otherwise
	 * 
	 * @param row
	 * @param col
	 * @return
	 */
	public boolean isCellEditable(int row, int col) {
		if (col == getColumnCount() - 1) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * PRE: maxStates is defined
	 * POST: RV = maxStates
	 */
	public int getMaxStates() {
		return maxStates;
	}

	/**
	 * PRE: maxStates is defined
	 * POST: maxStates = maxStates
	 */
	public void setMaxStates(int maxStates) {
		this.maxStates = maxStates;
	}

	/**
	 * PRE: context is defined
	 * POST: RV = context
	 */
	public Model getContext() {
		return context;
	}

	/**
	 * PRE: context is defined
	 * POST: context = context
	 */
	public void setContext(Model context) {
		this.context = context;
	}

	/**
	 * PRE: node is defined
	 * POST: RV = node
	 */
	public Node getNode() {
		return node;
	}

	/**
	 * PRE: node is defined
	 * POST: node = node
	 */
	public void setNode(Node node) {
		this.node = node;
	}

	/**
	 * PRE: data is defined
	 * POST: RV = data
	 */
	public DefaultTableModel getData() {
		return data;
	}

	/**
	 * PRE: data is defined
	 * POST: data = data
	 */
	public void setData(DefaultTableModel data) {
		this.data = data;
	}

	/**
	 * PRE: speed is defined
	 * POST: RV = speed
	 */
	public int getSpeed() {
		return speed;
	}

	/**
	 * PRE: speed is defined
	 * POST: speed = speed
	 */
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	/**
	 * PRE: originalTableWeights is defined
	 * POST: RV = originalTableWeights
	 */
	public double[] getOriginalTableWeights() {
		return originalTableWeights;
	}

	/**
	 * PRE: originalTableWeights is defined
	 * POST: originalTableWeights = originalTableWeights
	 */
	public void setOriginalTableWeights(double[] originalTableWeights) {
		this.originalTableWeights = originalTableWeights;
	}

}
