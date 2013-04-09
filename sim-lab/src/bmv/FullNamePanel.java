package bmv;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;

import javax.swing.JLabel;

/**
 * This class is the subclass of moveable panel used to display the full name of
 * a node when it is clicked on
 * 
 * @author plvines
 * 
 */
public class FullNamePanel extends MoveablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4658128328521488791L;

	public FullNamePanel(Node node, Point pLocation) {
		super(pLocation);
		JLabel name = new JLabel(node.getFullName());
		name.setFont(new Font("arial", Font.BOLD, 14));
		name.setForeground(Color.red);
		add(name);
		setBounds(pLocation.x, pLocation.y,
				node.getFullName().length() * 10 + 10, 30);
	}
}
