package bmv;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * This class provides the panel to view and edit the table for a node,
 * including the speed of update
 * NOTE: resizeability of moveablepanel has been disabled due to problem
 * resizing the table
 * 
 * @author plvines
 * 
 */
public class TablePanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8460147471810413195L;
	protected Rectangle maxSize;
	protected NodeTable table;
	protected int maxStates;
	protected BMVPanel context;
	protected Node node;
	protected JLabel speedMsg;
	protected JScrollPane scrollPane;
	protected Term[] backup;
	protected FullNamePanel namePanel;

	/**
	 * PRE: node, loc, and model are defined
	 * POST: this TablePanel has been initialized based on the node and at the
	 * loc provided
	 * 
	 * @param loc
	 * @param node
	 * @param model
	 */
	public TablePanel(Point loc, Node node, BMVPanel model) {
		super();
		context = model;
		this.node = node;
		maxStates = model.getMaxStates();
		table = new NodeTable(node, node.getModel(), this);
		backup = new Term[table.getRowCount()];
		for (int i = 0; i < table.getRowCount(); i++) {
			backup[i] = (Term) table.getValueAt(i, table.getColumnCount() - 1);
		}

		initializeGUI();

		namePanel = new FullNamePanel(node, new Point(loc.x, loc.y - 32));
		context.parent.layeredPanes.add(namePanel, new Integer(3));
	}

	/**
	 * PRE:
	 * POST: add the table and speed and button elements
	 */
	private void initializeGUI() {

		// SPEED COMBOBOX/LABEL
		speedMsg = new JLabel("Timescale:");
		speedMsg.setForeground(BMVPanel.TEXT_TITLE_COLOR);
		add(speedMsg);
		Term[] speedTerms = new Term[5];
		speedTerms[0] = (new Term("Very Slow", 1));
		speedTerms[1] = (new Term("Slow", 2));
		speedTerms[2] = (new Term("Normal", 4));
		speedTerms[3] = (new Term("Fast", 8));
		speedTerms[4] = (new Term("Very Fast", 16));
		JComboBox speedBox = new JComboBox(speedTerms);

		switch (table.getSpeed()) {
		case 1:
			speedBox.setSelectedIndex(0);
			break;
		case 2:
			speedBox.setSelectedIndex(1);
			break;
		case 4:
			speedBox.setSelectedIndex(2);
			break;
		case 8:
			speedBox.setSelectedIndex(3);
			break;
		case 16:
			speedBox.setSelectedIndex(4);
			break;
		}

		speedBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				changeSpeed(arg0);
			}
		});
		add(speedBox);
		scrollPane = new JScrollPane(table);
		addHelp("Timescale: changes the speed the node is updated; faster nodes will update multiple times for each time a slower node is updated (speeds are 1, 2, 4, 8, and 16)\n\nTABLE: select what state this node enters when the combination of states shown occurs. If \"Enforce Continuity\" under \"Options\" is checked then the chosen state must be within one of the previous state (if this node was \"Low\" it could only go to \"Low\" or \"High\")",
				200, 200);

		// TABLE
		add(scrollPane, BorderLayout.CENTER);
		if (scrollPane.getBounds().width < 200) {
			JPanel spacer = new JPanel();
			spacer.setPreferredSize(new Dimension(Math.max(340,
					table.getPreferredScrollableViewportSize().width + 30), 10));
			add(spacer, BorderLayout.CENTER);
			spacer.setBackground(getBackground());
		}

		// BUTTONS
		addOkCancel();

		JButton button = new JButton("Reset");
		button.setMnemonic(KeyEvent.VK_R);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetTable();
			}
		});
		button.setEnabled(true);
		add(button, BorderLayout.SOUTH);

		button = new JButton("Predict All");
		button.setMnemonic(KeyEvent.VK_P);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				table.resetEdits();
			}
		});
		button.setEnabled(true);
		add(button, BorderLayout.SOUTH);

		setBounds(node.getPixPos().x, node.getPixPos().y, Math.max(350,
				table.getPreferredScrollableViewportSize().width + 40),
				table.getPreferredScrollableViewportSize().height + 120);
		maxSize = getBounds();

	}

	/**
	 * Resize is overridden to do nothing to keep the spacing the scrollpane for
	 * the table proper
	 */
	public void resize(Point dragPoint) {

	}

	@Override
	public void move(Point dragPoint) {
		super.move(dragPoint);
		namePanel.setLocation(this.getBounds().x, this.getBounds().y - 32);
	}

	protected void ok() {
		((EditPanel) context).chooseTable(table);
		namePanel.getParent().remove(namePanel);
	}

	protected void cancel() {
		resetTable();
		((EditPanel) context).chooseTable(null);
		namePanel.getParent().remove(namePanel);
	}

	private void resetTable() {
		double[] originalWeights = table.getOriginalTableWeights();
		for (int i = 0; i < originalWeights.length; i++) {
			node.getInEdges().get(i).setWeight(originalWeights[i]);
		}
		for (int i = 0; i < backup.length; i++) {
			table.setValueAt(backup[i], i, table.getColumnCount() - 1);
		}
	}

	protected void applyTable() {
		if (table.isProper()) {
			((EditPanel) context).chooseTable(table);
		}
	}

	private void changeSpeed(ItemEvent arg0) {
		table.setSpeed(((Term) arg0.getItemSelectable().getSelectedObjects()[0])
				.getValue());
	}

	protected void exit() {
		((EditPanel) context).chooseTable(null);
	}

	/**
	 * PRE: table is defined POST: RV = table
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * PRE: node is defined POST: node = node
	 */
	public void setNode(Node node) {
		this.node = node;
	}

}
