package bmv;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * This class prompts the user to enter the number of states for a node with a
 * combobox of available numbers 2-9
 * 
 * @author plvines
 * 
 */
public class StatePanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6033469452790732815L;
	private JComboBox stateBox;
	private BMVPanel context;

	public StatePanel(Point pLocation, BMVPanel pContext) {
		super(pLocation);
		context = pContext;

		JLabel label = new JLabel("Number of States");
		add(label);

		Object[] states = new Object[8];
		for (int i = 0; i < states.length; i++) {
			states[i] = i + 2;
		}
		stateBox = new JComboBox(states);
		stateBox.setPreferredSize(new Dimension(100, 25));
		this.add(stateBox);
		transferFocus();

		addHelp("Set the number of possible states this node can be in", 200,
				35);
		addOkCancel();
		setBounds(location.x, location.y, 160, 90);
	}

	protected void ok() {
		((EditPanel) context).chooseStates(stateBox.getSelectedIndex() + 2);
	}

	protected void cancel() {
		((EditPanel) context).chooseStates(-1);
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
