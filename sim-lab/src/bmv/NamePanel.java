package bmv;

import java.awt.Font;
import java.awt.Point;
import javax.swing.JTextField;

/**
 * This class is the name prompt popup for editing/setting node and edge names
 * 
 * @author plvines
 * 
 */
public class NamePanel extends MoveablePanel {

	protected static enum EDITING {
		NODE, EDGE
	};

	/**
	 * 
	 */
	private static final long serialVersionUID = 6573268411679939330L;
	private NameField abrevNameField, fullNameField;
	private EditPanel context;
	private Point location;
	private EDITING mode;

	/**
	 * PRE: pLocation and pContext are defined
	 * POST: this namePanel has been constructed based on pLocation, and
	 * pContext
	 * 
	 * @param pLocation
	 * @param pContext
	 */
	public NamePanel(Point pLocation, EditPanel pContext, EDITING mode) {
		super();
		this.mode = mode;
		context = pContext;
		location = pLocation;
		requestFocusInWindow();

		if (mode == EDITING.NODE) {
			abrevNameField = new NameField("Enter the short name");
			abrevNameField.setFont(new Font("arial", Font.BOLD, 14));
			abrevNameField.setOpaque(true);
			this.add(abrevNameField);

			fullNameField = new NameField("Enter the full name");
			fullNameField.setFont(new Font("arial", Font.BOLD, 14));
			fullNameField.setOpaque(true);
			this.add(fullNameField);
			addHelp("Set the abbreviated and full names of this node", 170, 20);

			setBounds(location.x, location.y, 190, 100);
		} else if (mode == EDITING.EDGE) {
			fullNameField = new NameField("Enter a name");
			fullNameField.setFont(new Font("arial", Font.BOLD, 16));
			fullNameField.setOpaque(true);
			this.add(fullNameField);
			addHelp("Set the name of this edge", 150, 20);
			setBounds(location.x, location.y, 170, 70);
		}
		transferFocus();

		addOkCancel();
	}

	/**
	 * Holds the text entered as the name in the name panel
	 * 
	 * @author plvines
	 * 
	 */
	private class NameField extends JTextField {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9050212391798454999L;

		public NameField(String pText) {
			super(pText);
			setFocusable(true);
			requestFocusInWindow();
			setSelectionStart(0);
			setSelectionEnd(getText().length());
		}

	}

	/**
	 * If a new name has been entered, choose it for the node/edge, otherwise
	 * send back ""
	 */
	protected void ok() {
		if (mode == EDITING.NODE) {
			if (abrevNameField.getText().equals("Enter the short name")) {
				context.chooseNodeName(null, null);
			} else {
				if (fullNameField.getText().equals("Enter the full name")) {
					context.chooseNodeName(abrevNameField.getText(),
							abrevNameField.getText());
				} else {
					context.chooseNodeName(abrevNameField.getText(),
							fullNameField.getText());
				}
			}
		} else if (mode == EDITING.EDGE) {
			if (fullNameField.getText().equals("Enter a name")) {
				context.chooseEdgeName(null);
			} else {
				context.chooseEdgeName(fullNameField.getText());
			}
		}
	}

	/**
	 * send a null name back to cancel changing it
	 */
	protected void cancel() {
		if (mode == EDITING.NODE) {
			context.chooseNodeName(null, null);
		} else if (mode == EDITING.EDGE) {
			context.chooseEdgeName(null);
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
