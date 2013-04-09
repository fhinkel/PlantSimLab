package bmv;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * This class handles the file hierarchy displayed on the side of PlantSimLab.
 * This class includes methods for file manipulation including copying and
 * renaming models and experiments
 * 
 * @author plvines
 * 
 */
public class FileBrowser extends JPanel implements TreeSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -317509224649506293L;
	private JTree fileStruct;
	private BMVManager context;
	private JPopupMenu rightClickMenu;
	private TreePath curPath;
	private DefaultTreeModel treeModel;

	/**
	 * PRE: rootName and context are defined
	 * POST: this FileBrowser has been initialized using rootName as the root of
	 * the file hierarchy
	 * 
	 * @param rootName
	 * @param context
	 */
	public FileBrowser(String rootName, BMVManager context) {
		super(new GridLayout(1, 0));
		this.context = context;
		FileNode root = new FileNode(new File(rootName));

		createNodes(root);

		// Create a tree that allows one selection at a time.
		treeModel = new DefaultTreeModel(root);
		treeModel.addTreeModelListener(new FileStructListener());
		fileStruct = new JTree(treeModel);
		fileStruct.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		fileStruct.setEditable(true);

		fileStruct.setCellEditor(new FileNodeEditor(fileStruct,
				new DefaultTreeCellRenderer()));
		fileStruct.addTreeSelectionListener(this);
		fileStruct.getCellEditor().addCellEditorListener(
				new FileNodeEditorListener());
		// Create the scroll pane and add the tree to it.
		JScrollPane treeView = new JScrollPane(fileStruct);
		MouseListener ml = new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int selRow = fileStruct.getRowForLocation(e.getX(), e.getY());
				curPath = fileStruct.getPathForLocation(e.getX(), e.getY());
				if (!fileStruct.getSelectionModel().isPathSelected(curPath)) {
					fileStruct.getSelectionModel().setSelectionPath(curPath);
				}
				if (selRow != -1) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						rightClick(e.getPoint());
					} else if (e.getClickCount() >= 2) {
						doubleClick();
					}
				}
			}
		};
		fileStruct.addMouseListener(ml);

		setPreferredSize(new Dimension(200, 100));
		add(treeView);
		curPath = fileStruct.getSelectionPath();
		initializeRightClickMenu();

		fileStruct.setCellRenderer(new FileBrowserCellRenderer());
	}

	/**
	 * PRE:
	 * POST: the popup menu for right clicking on a node has been initialized
	 */
	private void initializeRightClickMenu() {
		rightClickMenu = new JPopupMenu();

		// New Model
		JMenuItem item = new JMenuItem("New Model");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				addModel();
			}
		});
		rightClickMenu.add(item);

		// New Experiment
		item = new JMenuItem("New Experiment");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				newExperiment();
			}
		});
		rightClickMenu.add(item);

		// Rename
		item = new JMenuItem("Rename");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fileStruct.startEditingAtPath(curPath);
			}
		});
		rightClickMenu.add(item);

		// Copy
		item = new JMenuItem("Copy");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				copyNode();
			}
		});
		rightClickMenu.add(item);

		// Delete
		item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				deleteSelectedNode();
			}
		});
		rightClickMenu.add(item);

	}

	/**
	 * PRE: click is defined
	 * POST: open the right click menu at the point click
	 * 
	 * @param click
	 */
	private void rightClick(Point click) {
		rightClickMenu.show(this, click.x, click.y);
	}

	/**
	 * PRE: a node has been double-clicked
	 * POST: the file of the node double-clicked has been sent to be opened
	 * in the active panel unless the node is "Experiments" or "Models"
	 */
	private void doubleClick() {
		if (!((FileNode) curPath.getLastPathComponent()).getFile().getName()
				.equals("Experiments")
				&& !((FileNode) curPath.getLastPathComponent()).getFile()
						.getName().equals("Models")) {
			context.newFile(((FileNode) curPath.getLastPathComponent())
					.getFile());
			((FileNode) curPath.getLastPathComponent()).setNewResult(false);
		}
	}

	/**
	 * PRE: newName is defined
	 * POST: RV = newName with any extension stripped off and replace by .exp
	 * 
	 * @param newName
	 * @return
	 */
	private String cleanTrajName(final String newName) {
		String cleanedName = newName;
		int dotIndex = cleanedName.indexOf('.');
		// No extension provided
		if (dotIndex < 0) {
			cleanedName += ".exp";
		} else if (!cleanedName.substring(dotIndex, cleanedName.length())
				.equals(".exp")) {
			cleanedName = cleanedName.substring(0, dotIndex) + ".exp";
		}

		return cleanedName;
	}

	/**
	 * PRE: nodeToChange and newFile are defined
	 * POST: if nodeToChange is an experiment, it has been renamed, both inside
	 * its file and its file name and the filebrowser has been updated
	 * If the node was a model, then entire model folder has been renamed, along
	 * with all references to the model within files within the folder
	 * 
	 * @param nodeToChange
	 * @param newFile
	 * @return
	 */
	protected boolean renameNode(FileNode nodeToChange, File newFile) {
		Boolean result = true;
		// renaming a trajectory
		if (((FileNode) nodeToChange.getParent()).getFile().getName()
				.equals("Experiments")) {
			String oldTrajName = nodeToChange
					.getFile()
					.getName()
					.substring(0, nodeToChange.getFile().getName().indexOf('.'));
			String newTrajName = cleanTrajName(newFile.getName());
			newFile = new File(newFile.getParent() + "/" + newTrajName);

			copyTrajectoryInFile(
					null,
					newFile.getName().substring(0,
							newFile.getName().indexOf('.')),
					nodeToChange.getFile(), newFile);
			if (context.trajPanel.getTrajectory().getTrajName()
					.equals(oldTrajName)) {
				context.trajPanel.getTrajectory().setTrajName(
						newFile.getName().substring(0,
								newFile.getName().indexOf('.')));
				context.updatePanels();
				context.trajPanel.getTrajectory().saveTraj(true);
			}
			nodeToChange.getFile().delete();
			treeModel.valueForPathChanged(curPath, newFile);
		}
		// renaming a model
		else {
			copyModelInFile(newFile.getName(), new File(nodeToChange.getFile()
					.getPath()
					+ "/"
					+ nodeToChange.getFile().getName()
					+ ".csv"), newFile);
			treeModel.removeNodeFromParent(nodeToChange);
			FileNode newModel = new FileNode(newFile);
			treeModel.insertNodeInto(newModel, (FileNode) curPath
					.getParentPath().getLastPathComponent(), 0);
			createNodes(newModel);
			treeModel.reload();
			deleteFile(nodeToChange.getFile());
			// treeModel.valueForPathChanged(curPath, newFile);
		}
		return result;
	}

	/**
	 * PRE: newName and node have been defined and node is an experiment
	 * POST: the current trajectory has had its named changed to newName and a
	 * new file created by saving it. The active panel should now be the new
	 * experiment name
	 * 
	 * @param node
	 * @param newName
	 */
	protected void saveExpAs(FileNode node, String newName) {
		if (node != null) {
			fileStruct.setSelectionPath(new TreePath(treeModel
					.getPathToRoot(node)));
			curPath = fileStruct.getSelectionPath();

			copyNode();
			renameNode(node, new File("Models/" + context.model.getModelName()
					+ "/Experiments/" + newName + ".exp"));
			System.out.println("RENAMED 1");
			renameNode(getNodeByName("Copy_of_" + node.getFile().getName()),
					new File("Models/" + context.model.getModelName()
							+ "/Experiments/" + node.getFile().getName()));

			System.out.println(node.getFile());
			context.newFile(new File("Models/" + context.model.getModelName()
					+ "/Experiments/" + newName + ".exp"));
		} else {
			System.out.println("NULL");
		}
	}

	/**
	 * PRE: curPath != null
	 * POST: the node specified by curPath is copied. If it is a model all the
	 * experiments have also been copied. The new model or experiment is named
	 * "Copy of ___" and set to be edited
	 */
	protected void copyNode() {
		// Copy the model
		if (curPath.getPathCount() == 2) {
			File newModel = new File(((FileNode) curPath.getPathComponent(1))
					.getFile().getParent()
					+ "/Copy_of_"
					+ ((FileNode) curPath.getPathComponent(1)).getFile()
							.getName());
			File origModel = new File(((FileNode) curPath.getPathComponent(1))
					.getFile().getPath()
					+ "/"
					+ ((FileNode) curPath.getPathComponent(1)).getFile()
							.getName() + ".csv");
			copyModelInFile(
					"Copy_of_"
							+ ((FileNode) curPath.getPathComponent(1))
									.getFile().getName(), origModel, newModel);
			FileNode newModelNode = new FileNode(newModel);
			treeModel.insertNodeInto(newModelNode,
					(FileNode) treeModel.getRoot(), 0);
			createNodes(newModelNode);
			treeModel.reload();
		}
		// Copy the experiment
		else if (curPath.getPathCount() == 4
				&& ((FileNode) curPath.getPathComponent(2)).getFile().getName()
						.equals("Experiments")) {
			File newExp = new File(((FileNode) curPath.getLastPathComponent())
					.getFile().getParent()
					+ "/Copy_of_"
					+ ((FileNode) curPath.getLastPathComponent()).getFile()
							.getName());

			File origExp = ((FileNode) curPath.getLastPathComponent())
					.getFile();
			copyTrajectoryInFile(null,
					newExp.getName()
							.substring(0, newExp.getName().indexOf('.')),
					origExp, newExp);
			FileNode newExpNode = new FileNode(newExp);
			treeModel.insertNodeInto(newExpNode,
					(FileNode) curPath.getPathComponent(2), 0);
		}
		fileStruct.startEditingAtPath(curPath);

	}

	/**
	 * PRE: newModel, oldFile, and newFile are defined
	 * POST: the model file specified by oldFile has been copied to newFile with
	 * the model's name changed to newModel
	 * 
	 * @param newModel
	 * @param oldFile
	 * @param newFile
	 */
	private void copyModelInFile(String newModel, File oldFile, File newFile) {
		newFile.mkdir();
		File newModelFile = new File(newFile.getPath() + "/" + newModel
				+ ".csv");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(
					newModelFile));
			String script = newModel;
			Scanner oldScript = new Scanner(oldFile);
			oldScript.next(); // skip model name

			while (oldScript.hasNextLine()) {
				script += "\n" + oldScript.nextLine();
			}
			writer.write(script);
			writer.close();
			copySimFiles(newModel, oldFile.getParentFile(), newFile);
			copyTableFiles(new File(oldFile.getParentFile() + "/Tables"),
					newFile);
			copyExperiments(new File(oldFile.getParentFile() + "/Experiments"),
					newFile);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * PRE: oldExpFolder and newModelFolder are defined
	 * POST: all experiments in oldExpFolder have been copied to
	 * newModelFolder/Experiments/ and the internal file reference to the old
	 * model have been replaced by references to the new model
	 * 
	 * @param oldExpFolder
	 * @param newModelFolder
	 * @throws IOException
	 */
	protected void copyExperiments(File oldExpFolder, File newModelFolder)
			throws IOException {
		File newExperimentsFolder = new File(newModelFolder.getPath()
				+ "/Experiments");
		newExperimentsFolder.mkdir();
		File[] experiments = oldExpFolder.listFiles();
		for (int i = 0; i < experiments.length; i++) {
			copyTrajectoryInFile(newModelFolder.getName(), null,
					experiments[i], new File(newExperimentsFolder + "/"
							+ experiments[i].getName()));
		}

	}

	/**
	 * PRE: oldTablesFolder and newModelFolder are defined
	 * POST: all tables files in oldTablesFolder have been copied into
	 * newModelFolder/Tables/
	 * 
	 * @param oldTablesFolder
	 * @param newModelFolder
	 * @throws IOException
	 */
	private void copyTableFiles(File oldTablesFolder, File newModelFolder)
			throws IOException {
		File tableFolder = new File(newModelFolder.getPath() + "/Tables");
		tableFolder.mkdir();
		BufferedWriter writer;
		String[] oldTables = oldTablesFolder.list();
		for (int i = 0; i < oldTables.length; i++) {
			writer = new BufferedWriter(new FileWriter(new File(
					tableFolder.getPath() + "/" + oldTables[i])));
			String script = "";
			Scanner oldTable = new Scanner(new File(oldTablesFolder + "/"
					+ oldTables[i]));
			while (oldTable.hasNextLine()) {
				script += oldTable.nextLine() + "\n";
			}

			writer.write(script);
			writer.close();
		}
	}

	/**
	 * PRE: newModel, oldFile, and newFile are defined
	 * POST: the simluation.txt and simulation.res files (if they exist) in
	 * oldFile have been copied to newFile and their internal references to the
	 * old model replaced by references to the new model
	 * 
	 * @param newModel
	 * @param oldFile
	 * @param newFile
	 * @throws IOException
	 */
	private void copySimFiles(String newModel, File oldFile, File newFile)
			throws IOException {
		File[] simFile = (oldFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("simulation.txt");
			}
		}));

		if (simFile != null && simFile.length > 0) {
			File newSimFile = new File(newFile.getPath() + "/simulation.txt");
			Scanner scan = new Scanner(simFile[0]);
			scan.next();
			String script = newModel;
			script += scan.nextLine();
			BufferedWriter simwriter = new BufferedWriter(new FileWriter(
					newSimFile));
			while (scan.hasNextLine()) {
				script += "\n" + scan.nextLine();
			}
			simwriter.write(script);
			simwriter.close();
		}
		File[] simResultsFile = (oldFile.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("simulation.res");
			}
		}));

		if (simResultsFile != null && simResultsFile.length > 0) {
			File newSimResultsFile = new File(newFile.getPath()
					+ "/simulation.res");
			Scanner scan = new Scanner(simResultsFile[0]);
			scan.next();
			String script = newModel;
			BufferedWriter simwriter = new BufferedWriter(new FileWriter(
					newSimResultsFile));
			while (scan.hasNextLine()) {
				script += "\n" + scan.nextLine();
			}
			simwriter.write(script);
			simwriter.close();
		}
	}

	/**
	 * PRE: oldFile and newFile are defined, oldFile contains a trajectory POST:
	 * the contents of oldFile have been written to a file at the path specified
	 * by newFile, with the modelName and trajectoryName specified
	 * 
	 * @param oldFile
	 * @param newFile
	 */
	private void copyTrajectoryInFile(String newModel, String newTrajectory,
			File oldFile, File newFile) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
			String script = "";
			Scanner oldScript = new Scanner(oldFile);
			if (newModel != null) {
				script += newModel + "\n";
				String oldModel = oldScript.next();
				if (context.model.getModelName().equals(oldModel)) {
					context.model.setModelName(newModel);
					context.updatePanels();
				}
			} else {
				script += oldScript.next() + "\n";
			}
			if (newTrajectory != null) {
				script += newTrajectory + "\n";

			} else {
				script += oldScript.next() + "\n";
			}

			while (oldScript.hasNextLine()) {
				script += oldScript.nextLine() + "\n";
			}

			writer.write(script);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: name is defined, this treemodel is defined
	 * POST: if there is a model node with name = name, it is returned.
	 * Otherwise null is returned
	 * 
	 * @param name
	 * @return
	 */
	protected FileNode getModelNodeByName(String name) {
		FileNode aNode = null;
		FileNode root = ((FileNode) treeModel.getRoot());
		boolean found = false;
		for (int i = 0; i < root.getChildCount() && !found; i++) {
			aNode = (FileNode) root.getChildAt(i);
			found = aNode.getFile().getName().equals(name);
		}
		if (!found) {
			aNode = null;
		}
		return aNode;
	}

	/**
	 * PRE: name is defined this treemodel is defined
	 * POST: if there is a node with name = name it is returned, otherwise null
	 * has been returned
	 * 
	 * @param name
	 * @return
	 */
	protected FileNode getNodeByName(String name) {
		FileNode aNode = (FileNode) treeModel.getRoot();
		boolean found = false;
		for (int i = 0; i < ((FileNode) treeModel.getRoot()).getChildCount()
				&& !found; i++) {
			aNode = getNodeByName(name,
					(FileNode) ((FileNode) treeModel.getRoot()).getChildAt(i));
			found = aNode != null;
		}
		if (!found) {
			aNode = null;
		}
		return aNode;
	}

	/**
	 * PRE: name and startNode are defined
	 * POST: if startNode or one of its children has name = name, it is
	 * returned, otherwise null is returned
	 * 
	 * @param name
	 * @param startNode
	 * @return
	 */
	protected FileNode getNodeByName(String name, FileNode startNode) {
		FileNode aNode = startNode;
		if (startNode != null) {
			boolean found = false;
			found = aNode.getFile().getName().equals(name);
			for (int i = 0; i < startNode.getChildCount() && !found; i++) {
				aNode = getNodeByName(name, (FileNode) startNode.getChildAt(i));
				found = aNode != null;
			}
			if (!found) {
				aNode = null;
			}
		}
		return aNode;
	}

	/**
	 * PRE: currentModel is defined
	 * POST: a new experiment is added to the file hierarchy of the folder
	 * defined by currentModel/Experiments/, the selection path is set to that,
	 * and the next experiment's name is set to edit
	 * 
	 * @param currentModel
	 */
	protected void newExperiment(String currentModel) {
		curPath = new TreePath(
				treeModel.getPathToRoot(getNodeByName(currentModel)));
		fileStruct.setSelectionPath(curPath);
		newExperiment();
	}

	/**
	 * PRE: curPath is defined
	 * POST: a new experiment is added to the Experiments folder of the curPath
	 * model
	 */
	private void newExperiment() {
		if (curPath.getPathCount() >= 3) {
			addExperiment((FileNode) curPath.getPathComponent(2));
		} else if (curPath.getPathCount() == 2) {
			addExperiment(((FileNode) curPath.getPathComponent(1))
					.getChild(new FileNode(new File("Experiments"))));
		}
	}

	/**
	 * PRE: parent is defined
	 * POST: a new experiment is added as parent's child and is set to being
	 * edited
	 * 
	 * @param parent
	 */
	private void addExperiment(FileNode parent) {
		Trajectory defaultTraj = new Trajectory(context.model.getCurModel());
		defaultTraj.setTrajName("new");
		defaultTraj.saveTraj(false);
		treeModel.insertNodeInto(new FileNode(new File(parent.getFile()
				.getPath() + "/" + defaultTraj.getTrajName() + ".exp")),
				parent, 0);
		fileStruct.startEditingAtPath(curPath);

	}

	/**
	 * PRE:
	 * POST: a new model folder/file is added to Models/ and set to its name
	 * being edited
	 */
	protected void addModel() {
		FileNode newModel = new FileNode(new File(
				((FileNode) treeModel.getRoot()).getFile().getPath()
						+ "/new_model"));
		Model defaultModel = new Model(context, new Viewport());
		defaultModel.setModelName("new_model");
		defaultModel.saveModel();
		treeModel.insertNodeInto(newModel, ((FileNode) treeModel.getRoot()), 0);
		createNodes(((FileNode) treeModel.getRoot()));
		treeModel.reload(((FileNode) treeModel.getRoot()));
		fileStruct.startEditingAtPath(curPath);

	}

	/**
	 * PRE:
	 * POST: the currently selected node is deleted. If this is a directory, all
	 * its contents are deleted as well
	 */
	private void deleteSelectedNode() {
		if (fileStruct.getSelectionPath() != null) {
			FileNode nodeToDelete = (FileNode) fileStruct.getSelectionPath()
					.getLastPathComponent();
			if (nodeToDelete.getFile().isDirectory()) {
				deleteDirectory(nodeToDelete.getFile());
			} else {
				nodeToDelete.getFile().delete();
			}
			treeModel.removeNodeFromParent(nodeToDelete);
		}
	}

	/**
	 * PRE: fileToDelete is defined
	 * POST: fileToDelete is deleted
	 * 
	 * @param fileToDelete
	 */
	private void deleteFile(File fileToDelete) {
		if (fileToDelete.isDirectory()) {
			deleteDirectory(fileToDelete);
		} else {
			fileToDelete.delete();
		}
	}

	/**
	 * PRE: directoryToDelete is defined
	 * POST: directoryToDelete and all its contents have been deleted
	 * 
	 * @param directoryToDelete
	 */
	private void deleteDirectory(File directoryToDelete) {
		if (directoryToDelete.getParentFile().getName().equals("Models")) {
			context.model.removeModelByName(directoryToDelete.getName());
		}
		if (directoryToDelete.list().length > 0) {
			File[] subfiles = directoryToDelete.listFiles();
			for (int i = 0; i < subfiles.length; i++) {
				if (subfiles[i].isDirectory()) {
					deleteDirectory(subfiles[i]);
				} else {
					subfiles[i].delete();
				}
			}
		}
		directoryToDelete.delete();
	}

	private class FileStructListener implements TreeModelListener {

		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			// System.out.println("Changed");
		}

		/**
		 * immediately set new nodes as the selection path and open them as the
		 * active panel
		 */
		@Override
		public void treeNodesInserted(TreeModelEvent e) {
			// System.out.println("Inserted");
			curPath = e.getTreePath().pathByAddingChild(e.getChildren()[0]);
			fileStruct.setSelectionPath(curPath);
			context.newFile(((FileNode) e.getChildren()[0]).getFile());
		}

		@Override
		public void treeNodesRemoved(TreeModelEvent e) {
			// System.out.println("Removed");

		}

		@Override
		public void treeStructureChanged(TreeModelEvent e) {
			// System.out.println("Struc Changed");
		}
	}

	/**
	 * PRE: curNode is defined
	 * POST: add all the files and directories under curNode to the tree except
	 * for the Tables folder and anything ending in .txt
	 * 
	 * @param curNode
	 */
	private void createNodes(FileNode curNode) {
		File curNodeFile = curNode.getFile();
		if (curNodeFile.isDirectory()) {
			FileNode newNode;
			File newFile;
			// For each file/dir in this directory, add them and recursively add
			// their children
			// Add directories first
			for (int i = 0; i < curNodeFile.list().length; i++) {
				newFile = new File(curNodeFile.getPath() + "/"
						+ curNodeFile.list()[i]);
				newNode = new FileNode(newFile);
				if (newFile.isDirectory()) {
					FileNode duplicate = curNode.getChild(newNode);
					if (!newNode.toString().equals("Tables")
							&& (newNode.toString().charAt(
									newNode.toString().length() - 1) != '~')) {
						if (duplicate == null) {
							curNode.add(newNode);
							createNodes(newNode);
						} else {
							createNodes(duplicate);
						}
					}
				}
			}
			// Add files last
			for (int i = 0; i < curNodeFile.list().length; i++) {
				newFile = new File(curNodeFile.getPath() + "/"
						+ curNodeFile.list()[i]);
				newNode = new FileNode(newFile);
				if (!newFile.isDirectory()
						&& !curNode.contains(newNode)
						&& (newNode.toString().charAt(
								newNode.toString().length() - 1) != '~')
						&& !newNode.toString().endsWith(".txt")
						&& !newNode.toString().startsWith(".")) {
					curNode.add(newNode);
				}
			}
		}
	}

	/**
	 * PRE:
	 * POST: update the filebrowser in case anything has changed such as files
	 * added or deleted
	 */
	protected void update() {
		TreePath selectionPath = fileStruct.getSelectionPath();
		FileNode root = ((FileNode) fileStruct.getModel().getRoot());
		createNodes(root);
		treeModel.reload();
		fileStruct.setSelectionPath(selectionPath);
	}

	@Override
	public void valueChanged(TreeSelectionEvent arg0) {
		// System.out.println("VALUECHANGED");

	}

	private class FileNodeEditor extends DefaultTreeCellEditor {

		public FileNodeEditor(JTree tree, DefaultTreeCellRenderer renderer) {
			super(tree, renderer);
		}

		@Override
		public Object getCellEditorValue() {
			String newFileName = (String) super.getCellEditorValue();
			if (lastPath == null) {
				return "BLANK";
			}
			if (((FileNode) ((FileNode) lastPath.getLastPathComponent())
					.getParent()).getFile().getName().equals("Experiments")) {
				newFileName = cleanTrajName(newFileName);
			}
			File temp = new File(((FileNode) lastPath.getLastPathComponent())
					.getFile().getParent() + "/" + newFileName);
			return temp;
		}
	}

	private class FileNodeEditorListener implements CellEditorListener {

		@Override
		public void editingCanceled(ChangeEvent e) {
		}

		@Override
		public void editingStopped(ChangeEvent e) {
			// System.out.println("STOPPED");
			renameNode((FileNode) curPath.getLastPathComponent(),
					(File) fileStruct.getCellEditor().getCellEditorValue());

		}
	}
}
