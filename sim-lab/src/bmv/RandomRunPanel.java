package bmv;

import java.awt.Font;
import java.awt.Point;
import javax.swing.JTextField;

/**
 * Panel to prompt the user to enter the number of steps and number of runs for
 * a random run of an experiment
 * 
 * @author plvines
 * 
 */
public class RandomRunPanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9074380405152661182L;
	private NumberField runsField, stepsField;
	private BMVPanel context;
	private Point location;

	public RandomRunPanel(Point pLocation, BMVPanel pContext) {
		super();
		context = pContext;
		location = pLocation;
		requestFocusInWindow();
		runsField = new NumberField("Number of Runs");
		runsField.setFont(new Font("arial", Font.BOLD, 14));
		runsField.setOpaque(true);
		this.add(runsField);

		addHelp("This mode will run the experiment on the model, only updating a single variable (randomly determined) at a time.\n\nNumber of Runs: this is how many times the experiment will be rerun, the more runs the more consistent the results should become.\n\nNumber of Steps: this is how many steps each run will go through. More steps would represent observing the system for a greater period of time, so the more steps the longer term behavior will be observed.",
				200, 200);

		stepsField = new NumberField("Number of Steps");
		stepsField.setFont(new Font("arial", Font.BOLD, 14));
		stepsField.setOpaque(true);
		this.add(stepsField);
		transferFocus();

		addOkCancel();
		setBounds(location.x, location.y, 170, 95);
	}

	@SuppressWarnings("serial")
	private class NumberField extends JTextField {
		public NumberField(String pText) {
			super(pText);
			// addKeyListener(new KeyHandler());
			setFocusable(true);
			requestFocusInWindow();
			setSelectionStart(0);
			setSelectionEnd(getText().length());
		}
	}

	protected void ok() {
		try {
			int runs = Integer.parseInt(runsField.getText());
			int steps = Integer.parseInt(stepsField.getText());
			((TrajPanel) context).runRandomUpdateTraj(runs, steps);
		} catch (NumberFormatException ex) {
			((TrajPanel) context).runRandomUpdateTraj(-1, -1);
		}
	}

	protected void cancel() {
		((TrajPanel) context).runRandomUpdateTraj(-1, -1);

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
