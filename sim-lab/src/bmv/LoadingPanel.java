package bmv;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 * This is the subclass of moveable panel that should be displayed when loading
 * a model
 * 
 * @author plvines
 * 
 */
public class LoadingPanel extends MoveablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9139674497305884030L;
	private final static int WIDTH = 250, HEIGHT = 30;

	protected static enum STATUS {
		STARTING, LOADING_MODEL, LOADING_NODES, LOADING_EDGES, LOADING_DRAWINGS, LOADING_TABLES
	};

	STATUS status;
	private long oldTime;
	private static long timeStep = 500;
	JLabel statusMsg;

	public LoadingPanel(JComponent parentContainer) {
		super();
		setBounds(parentContainer.getWidth() / 2 - WIDTH / 2,
				parentContainer.getHeight() / 2 - HEIGHT / 2, WIDTH, HEIGHT);
		status = STATUS.STARTING;
		statusMsg = new JLabel("" + status.toString());
		statusMsg.setForeground(BMVPanel.BORDER_COLOR);
		statusMsg.setFont(new Font("arial", Font.BOLD, 16));
		add(statusMsg);
	}

	/**
	 * paints with increasing number of '.' until 4 dots are reached, then
	 * resets to 1
	 */
	public void paint(Graphics g) {
		super.paintComponent(g);
		super.paintChildren(g);
		super.paintBorder(g);
		if (System.currentTimeMillis() - oldTime < timeStep) {
			statusMsg.setText(status.toString() + ".");
		} else if (System.currentTimeMillis() - oldTime < 2 * timeStep) {
			statusMsg.setText(status.toString() + ". .");
		} else if (System.currentTimeMillis() - oldTime < 3 * timeStep) {
			statusMsg.setText(status.toString() + ". . .");
		} else if (System.currentTimeMillis() - oldTime < 4 * timeStep) {
			statusMsg.setText(status.toString() + ". . . .");
		} else {
			oldTime = System.currentTimeMillis();
		}
	}
}
