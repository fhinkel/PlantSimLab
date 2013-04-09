package bmv;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * This class handles the panel for selecting terms for the states of a node.
 * 
 * @author plvines
 * 
 */
public class VocabPanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6285384583246170699L;
	private JLabel numToGo;
	private JTable termTable;
	private BMVPanel context;
	private Point location;
	private ArrayList<Term> termsSelected;
	private StateVocabulary vocab;
	private int numStates;
	private int numSelected;

	protected JScrollPane scrollPane;

	/**
	 * PRE: pLocation, pContext, pcVocab, and numberOfStates are defined
	 * POST: initializes this VocabPanel based on the parameters; using the
	 * vocabulary provided
	 * 
	 * @param pLocation
	 * @param pContext
	 * @param pVocab
	 * @param numberOfStates
	 */
	public VocabPanel(Point pLocation, BMVPanel pContext,
			StateVocabulary pVocab, int numberOfStates) {
		super(pLocation);
		termsSelected = new ArrayList<Term>();
		context = pContext;
		location = pLocation;
		vocab = pVocab;
		numStates = numberOfStates;
		numSelected = 0;
		if (numberOfStates > vocab.getTermSets().size()) {
			vocab.addTermSets(numberOfStates);
		}
		Term[] termsList = new Term[vocab.getTermSet(numberOfStates).size()];
		vocab.getTermSet(numberOfStates).toArray(termsList);
		Object[][] data = new Object[termsList.length + numberOfStates][2];
		for (int i = 0; i < termsList.length; i++) {
			data[i][0] = termsList[i];
			data[i][1] = new Boolean(false);
		}

		// Add extra terms for user-created ones
		for (int i = termsList.length; i < numberOfStates + termsList.length; i++) {
			data[i][0] = new Term("New Term");
			data[i][1] = new Boolean(false);
		}

		// setup table display
		termTable = new JTable(new TermTableModel(data));
		termTable.setPreferredScrollableViewportSize(new Dimension(200, Math
				.min(350, termTable.getRowCount() * 16)));
		termTable.setFillsViewportHeight(true);
		termTable.setShowVerticalLines(false);
		termTable.getColumnModel().getColumn(1).setPreferredWidth(20);

		setBounds(pLocation.x, pLocation.y, 250,
				Math.min(350, termTable.getRowCount() * 16) + 80);

		scrollPane = new JScrollPane(termTable);
		add(scrollPane);
		addHelp("Select terms to describe the states of the node. Enter your own terms by editing the cells labeled \"New Term\" at the bottom and then checking them.",
				200, 100);

		numToGo = new JLabel(numStates - numSelected + " selections left");
		numToGo.setPreferredSize(new Dimension(getBounds().width - 25, 10));
		add(numToGo);
		addOkCancel();
	}

	protected void ok() {
		applyTerms(true);
	}

	protected void cancel() {
		applyTerms(false);
	}

	/**
	 * Special table for displaying the check boxes in one column and the terms
	 * in the other, and allowing the "New Term" cells to be edited but none of
	 * the others
	 * 
	 * @author plvines
	 * 
	 */
	@SuppressWarnings("serial")
	class TermTableModel extends AbstractTableModel {
		private String[] columnNames = { "Term", "Include" };
		private Object[][] data;

		public TermTableModel(Object[][] data) {
			this.data = data;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/**
		 * Only returns true for the checkbox column and the "New Term" cells
		 */
		public boolean isCellEditable(int row, int col) {
			if (col < 1 && row < getRowCount() - numStates) {
				return false;
			} else {
				return true;
			}
		}

		/**
		 * Allows the check boxes to be checked, and handles adding and removing
		 * these terms from the terms selected list
		 */
		public void setValueAt(Object value, int row, int col) {
			if (col == 1) {
				Boolean oldValue = ((Boolean) data[row][col]);
				data[row][col] = value;
				if ((Boolean) value == true) {
					if (!oldValue) {
						termsSelected.add(((Term) data[row][0]));
						selectRelatedSet(row);
						numSelected++;
					}
				} else {
					termsSelected.remove((Term) data[row][0]);
					numSelected--;
				}
			} else {
				((Term) data[row][0]).setWord(((Term) value).getWord().replace(
						' ', '_'));
				setValueAt(new Boolean(true), row, 1);
			}
			if (numStates - numSelected != 1) {
				numToGo.setText((numStates - numSelected) + " selections left");
			} else {
				numToGo.setText((numStates - numSelected) + " selection left");
			}
			fireTableCellUpdated(row, col);
		}

		/**
		 * PRE: index is defined POST: related (off for on, active for inactive,
		 * etc) terms are set as selected in the table
		 * 
		 * @param index
		 */
		private void selectRelatedSet(int index) {
			for (int i = index - 1; i >= 0
					&& ((Term) data[i][0]).getValue() < ((Term) data[index][0])
							.getValue(); i++) {

				setValueAt(new Boolean(true), i, 1);
			}
			for (int i = index + 1; i < getRowCount()
					&& ((Term) data[i][0]).getValue() > ((Term) data[index][0])
							.getValue(); i++) {
				if (((Term) data[index][0]).getValue() < ((Term) data[index][0])
						.getValue()) {
					setValueAt(new Boolean(true), i, 1);
				}
			}
		}
	}

	/**
	 * \ PRE: accepted and termsSelected are defined POST: if accepted, then a
	 * deep-copy of termsSelected are passed to the EditTable method chooseTerms
	 * for integration into a node's vocabulary. Otherwise, null is passed
	 * 
	 * @param proper
	 */
	private void applyTerms(Boolean accepted) {
		if (accepted) {
			if (numSelected - numStates == 0) {
				ArrayList<Term> terms = null;
				if (accepted) {
					terms = new ArrayList<Term>();
					for (int i = 0; i < termsSelected.size(); i++) {
						terms.add(new Term(termsSelected.get(i)));
					}
					// Sort terms by ascending value order
					int sortedIndex = 0;
					for (int i = 0; i < terms.size(); i++) {
						int sortPosition = 0;
						for (sortPosition = 0; sortPosition < sortedIndex
								&& (terms.get(i).getValue() >= terms.get(
										sortPosition).getValue()); sortPosition++) {
						}
						terms.add(sortPosition, terms.remove(i));
						sortedIndex++;
					}

					// assign new values to duplicate-value terms
					for (int i = 0; i < terms.size() - 1; i++) {
						if (terms.get(i).getValue() == terms.get(i + 1)
								.getValue()) {
							// find the nearest empty value
							int k;
							for (k = 1; (i - k) >= 0; k++) {
								// if there's a gap in numbering
								if (terms.get(i - k).getValue() < terms.get(i)
										.getValue() - k) {
									terms.get(i).setValue(
											terms.get(i).getValue() - k);
								}
							}
							// if there was no available value
							if (k < 0) {
								// Shift values up to make room
								for (k = i + 1; k < terms.size(); k++) {
									terms.get(k).setValue(
											terms.get(k).getValue() + 1);
								}
							}
						}
					}

					// assign available values to user-created terms
					int nextValue = 0;
					while (terms.get(0).getValue() == -1) {
						for (int k = 0; k < terms.size(); k++) {
							if (terms.get(k).getValue() == nextValue) {
								nextValue++;
							}
							// If there's a hole
							else if (terms.get(k).getValue() > nextValue) {
								terms.add(k - 1, terms.remove(0));
								terms.get(k - 1).setValue(nextValue);
								vocab.addTermToFront(numStates,
										terms.get(k - 1));
								nextValue++;
							}
						}
						if (nextValue > terms.get(terms.size() - 1).getValue()) {
							terms.add(terms.remove(0));
							terms.get(terms.size() - 1).setValue(nextValue);
							vocab.addTermToFront(numStates,
									terms.get(terms.size() - 1));
							nextValue++;
						}
					}
				}

				((EditPanel) context).chooseVocab(terms);
			}
		} else {
			((EditPanel) context).chooseVocab(null);
		}
	}

	/**
	 * PRE: location is defined POST: RV = location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * PRE: location is defined POST: location = location
	 */
	public void setLocation(Point location) {
		this.location = location;
	}

}
