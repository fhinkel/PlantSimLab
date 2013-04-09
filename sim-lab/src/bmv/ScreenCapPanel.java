package bmv;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

/**
 * This class prompts the user for a file name to save a screencapture and gives
 * the option to save it as a basic B&W picture
 * 
 * @author plvines
 * 
 */
public class ScreenCapPanel extends MoveablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6210146460445791444L;
	private NameField nameField;
	private BMVPanel context;
	private Point location;
	private JCheckBox cb;

	public ScreenCapPanel(Point pLocation, BMVPanel pContext) {
		super();
		context = pContext;
		location = pLocation;
		requestFocusInWindow();

		cb = new JCheckBox("Black & White");
		cb.setBackground(Color.white);
		add(cb);

		Date date = new Date();
		DateFormat format = new SimpleDateFormat("-yyyy-MM-dd HH:mm:ss");
		nameField = new NameField(context.getModel().getModelName()
				+ format.format(date));
		nameField.setFont(new Font("arial", Font.BOLD, 16));
		nameField.setOpaque(true);
		this.add(nameField);
		transferFocus();

		addHelp("Set the name of this screenshot. It will be stored in ScreenCaptures/name.png",
				150, 20);

		addOkCancel();
		setBounds(location.x, location.y,
				nameField.getPreferredSize().width + 30, 100);
	}

	@SuppressWarnings("serial")
	private class NameField extends JTextField {
		public NameField(String pText) {
			super(pText);
			setFocusable(true);
			requestFocusInWindow();
			setSelectionStart(0);
			setSelectionEnd(getText().length());
		}

	}

	protected void ok() {
		context.screenCapture(nameField.getText(), cb.isSelected());
	}

	protected void cancel() {
		context.screenCapture(null, cb.isSelected());
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
