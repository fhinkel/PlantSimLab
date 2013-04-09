package bmv;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * This class is used for the node objects in the FileBrowser. They provde a
 * boolean to mark if they are unviewed results so they can be flashed red by
 * the FileBrowserCellRenderer
 * 
 * @author plvines
 * 
 */
public class FileNode extends DefaultMutableTreeNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1431839813902075351L;
	private boolean newResult;

	public FileNode(File newFile) {
		super(newFile);
		newResult = false;
	}

	public FileNode(File newFile, boolean newResult) {
		super(newFile);
		this.newResult = newResult;
	}

	public File getFile() {
		return ((File) userObject);
	}

	public void setFile(File newFile) {
		setUserObject(newFile);
	}

	public boolean isNewResult() {
		return newResult;
	}

	public void setNewResult(boolean newResult) {
		this.newResult = newResult;
	}

	public void setFile(String newFile) {
		setUserObject(new File(getFile().getParent() + "/" + newFile));
	}

	/**
	 * PRE: other is defined
	 * POST: returns true if the file name of both this and other are equal,
	 * false otherwise
	 * 
	 * @param other
	 * @return
	 */
	public boolean equals(FileNode other) {
		return (other.toString().equals(this.toString()));
	}

	/**
	 * PRE: other is defined
	 * POST: returns true if there is a child such that child = other, false
	 * otherwise;
	 * 
	 * @param other
	 * @return
	 */
	public boolean contains(FileNode other) {
		boolean result = false;
		if (children != null) {
			for (int i = 0; result == false && i < children.size(); i++) {
				result = other.equals((FileNode) children.get(i));
			}
		}
		return result;
	}

	/**
	 * PRE: other is defined
	 * POST: returns the the child that equals other if there is one, null
	 * otherwise
	 * 
	 * @param other
	 * @return
	 */
	public FileNode getChild(FileNode other) {
		FileNode result = null;
		if (children != null) {
			for (int i = 0; result == null && i < children.size(); i++) {
				if (other.equals((FileNode) children.get(i))) {
					result = (FileNode) children.get(i);
				}
			}
		}
		return result;
	}

	public String toString() {
		return ((File) userObject).getName();
	}
}