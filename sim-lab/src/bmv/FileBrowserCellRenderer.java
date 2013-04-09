package bmv;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * This class is a small custom renderer for FileNodes which allows new results
 * to flash red until they are viewed
 * 
 * @author plvines
 * 
 */
public class FileBrowserCellRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8682500177449796996L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		Component result = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus);

		if (((FileNode) value).isNewResult()
				&& System.currentTimeMillis() % 1000 > 500) {
			((DefaultTreeCellRenderer) result).setForeground(Color.red);
		}
		return result;
	}
}
