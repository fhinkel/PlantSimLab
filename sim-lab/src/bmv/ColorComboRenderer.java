package bmv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 * Custom combobox renderer to display color squares for a color-selection
 * combobox
 * 
 * @author plvines
 * 
 */
public class ColorComboRenderer extends JPanel implements ListCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4968857379699237065L;
	protected Color m_c = Color.black;

	public ColorComboRenderer() {
		super();
		setPreferredSize(new Dimension(20, 15));
	}

	public Component getListCellRendererComponent(JList list, Object obj,
			int row, boolean sel, boolean hasFocus) {
		if (obj instanceof Color)
			m_c = (Color) obj;
		return this;
	}

	public void paint(Graphics g) {
		setBackground(m_c);
		super.paint(g);
	}
}
