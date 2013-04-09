package bmv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;

/**
 * popup panel for selecting the color and shape of a node.
 * 
 * @author plvines
 * 
 */
public class ColorPanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7795570084271524778L;
	private JComboBox colorBox, shapeBox;
	private EditPanel context;
	private Point location;

	/**
	 * PRE: pLocation, pContext, and colors are defined.
	 * POST: this ColorPanel has been initialized
	 * 
	 * @param pLocation
	 * @param pContext
	 * @param colors
	 */
	public ColorPanel(Point pLocation, EditPanel pContext,
			ArrayList<Color> colors) {
		super();
		context = pContext;
		location = pLocation;
		setBounds(location.x, location.y, 150, 100);
		initializeGUI(colors);
	}

	/**
	 * PRE: the icons of node0-0, node1-0, and node2-0 exist in
	 * "Resources/Images/", colors is defined
	 * POST: the comboboxes for shape and color have been initialized, as well
	 * as the other GUIElements (OK, Cancel, and shape and color labels);
	 * 
	 * @param colors
	 */
	private void initializeGUI(ArrayList<Color> colors) {
		setBackground(Color.white);
		JLabel label = new JLabel("Color:");
		add(label);

		label = new JLabel("Shape:");
		add(label);

		addHelp("Set the color and shape of the node being edited", 200, 35);
		colorBox = new JComboBox(colors.toArray());
		colorBox.setRenderer(new ColorComboRenderer());
		colorBox.setPreferredSize(new Dimension(70, 30));
		add(colorBox);

		ImageIcon[] shapes = new ImageIcon[3];
		for (int i = 0; i < shapes.length; i++) {
			shapes[i] = new ImageIcon("Resources/Images/node" + i + "-0.png");
		}
		shapeBox = new JComboBox(shapes);
		shapeBox.setRenderer(new ImageComboRenderer());
		add(shapeBox);

		addOkCancel();
	}

	/**
	 * call the EditPanel method for selecting the current color and shape
	 */
	protected void ok() {
		context.chooseColor(colorBox.getSelectedIndex(),
				shapeBox.getSelectedIndex());
	}

	/**
	 * call the EditPanel method with parameters to not change the current color
	 * and shape
	 */
	protected void cancel() {
		context.chooseColor(-1, -1);
	}

	/**
	 * paint the components
	 */
	public void paint(Graphics g) {
		super.paint(g);
		paintChildren(g);
	}

	protected void panelMouseClicked(MouseEvent e) {
		super.panelMouseClicked(e);
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
