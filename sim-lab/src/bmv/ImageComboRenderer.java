package bmv;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * This class is a custom combobox renderer for rendering images used by the
 * shape edit panel to display the three shapes
 * 
 * @author plvines
 * 
 */
public class ImageComboRenderer extends JPanel implements ListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7446788667276913788L;
	protected ImageIcon displayed;

	public ImageComboRenderer() {
		super();
		setPreferredSize(new Dimension(30, 30));
	}

	public Component getListCellRendererComponent(JList list, Object obj,
			int row, boolean sel, boolean hasFocus) {
		if (obj instanceof ImageIcon)
			displayed = (ImageIcon) obj;
		return this;
	}

	public void paint(Graphics g) {
		g.drawImage(displayed.getImage(), 0, 0, getBounds().width,
				getBounds().height, null);
		// displayed.paintIcon(this, g, 0, 0);
	}
}
