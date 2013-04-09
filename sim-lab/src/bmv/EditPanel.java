package bmv;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;

import bmv.Node.SHAPE;
import bmv.BMVManager.ARCH;
import bmv.BMVManager.OS;

/**
 * This subclass of BMVPanel handles editing of the model
 * 
 * @author plvines
 * 
 */
public class EditPanel extends BMVPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private enum EDIT_MODE {
		NONE, ADDING_EDGE, ADDING_NODE, DELETING, DRAWING, DRAG_DRAWING
	};

	private final static int MODEL_NAME = 0;
	private final static int NUM_STATIC_ELEMS = 1;

	private int edgeSelected, nodeSelected, drawingSelected;
	private Edge partialEdge;
	private Node partialNode;
	private Drawing partialDrawing;
	private int edgeType;
	private ColorPanel colorPanel;
	private TablePanel tablePanel;
	private TablePanel tempTablePanel;
	private NamePanel namePanel;
	private StatePanel statePanel;
	private VocabPanel vocabPanel;
	private int nodeDrag;
	private EDIT_MODE editMode;
	private boolean multiJointed;
	private int drawingColor;
	private JPopupMenu rightClickNodeMenu, rightClickEdgeMenu;

	/**
	 * PRE: model, port, trajectory, and pLayer are defined POST: this object
	 * has been initialized
	 * 
	 * @param model
	 * @param port
	 * @param trajectory
	 * @param pLayer
	 */
	public EditPanel(ModelHolder model, Viewport port, Trajectory trajectory,
			BMVManager pLayer) {
		super(model, port, trajectory, pLayer);
	}

	/**
	 * initialize class variables to default values
	 */
	protected void initialize() {

		super.initialize();
		initializeRightClickMenus();
		bg = new ImageIcon("Resources/Images/bgEdit.png");
		setPreferredSize(START_SIZE);
		editMode = EDIT_MODE.NONE;
		edgeSelected = NOTHING;
		nodeSelected = NOTHING;
		drawingSelected = NOTHING;
		nodeDrag = NOTHING;
		partialEdge = null;
		edgeType = 0;
		multiJointed = false;
		drawingColor = 0;

	}

	/**
	 * PRE: POST: the popup menu for right clicking on a node has been
	 * initialized
	 */
	private void initializeRightClickMenus() {
		rightClickNodeMenu = new JPopupMenu();

		// Edit Name
		JMenuItem item = new JMenuItem("Edit Name");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				editName(model.getNodes().get(nodeSelected).getPixPos());
			}
		});
		rightClickNodeMenu.add(item);

		// Edit Color
		item = new JMenuItem("Edit Color");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				editColor(model.getNodes().get(nodeSelected).getPixPos());
			}
		});
		rightClickNodeMenu.add(item);

		// Edit States
		item = new JMenuItem("Edit States");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				editStates(model.getNodes().get(nodeSelected).getPixPos());
			}
		});
		rightClickNodeMenu.add(item);

		// Edit Table
		item = new JMenuItem("Edit Table");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				layer.remove(nameDisplayPanel);
				nameDisplayPanel = null;
				editTable();
			}
		});
		rightClickNodeMenu.add(item);

		// Delete
		item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				deleteNode(model.getNodes().get(nodeSelected));
			}
		});
		rightClickNodeMenu.add(item);

		rightClickEdgeMenu = new JPopupMenu();

		// Edit Name
		item = new JMenuItem("Edit Name");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				editName(model.getEdges().get(edgeSelected).getStart()
						.getPixPos());
			}
		});
		rightClickEdgeMenu.add(item);

		// Show name
		JCheckBoxMenuItem citem = new JCheckBoxMenuItem("Show Name");
		citem.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				model.getEdges().get(edgeSelected).setDrawName(
						arg0.getStateChange() == ItemEvent.SELECTED);
			}
		});
		rightClickEdgeMenu.add(citem);

		// Flip the type of edge: activator/inhibitor
		item = new JMenuItem("Flip Type");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				model.getEdges().get(edgeSelected).setType(
						-model.getEdges().get(edgeSelected).getType(), true);
			}
		});
		rightClickEdgeMenu.add(item);

		// Delete edge
		item = new JMenuItem("Delete");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				resetToggles();
				deleteEdge(model.getEdges().get(edgeSelected));
			}
		});
		rightClickEdgeMenu.add(item);
	}

	/**
	 * add the additional buttons to the toolbar
	 */
	protected void initializeMenu() {
		super.initializeMenu();

		// Add Buttons!
		ImageIcon icon = new ImageIcon("Resources/icons/16/158.png");
		JToggleButton tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Add Node (Q)");
		tbutton.setMnemonic(KeyEvent.VK_Q);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addNodeClicked();
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/105.png");
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Add activating edge (W)");
		tbutton.setMnemonic(KeyEvent.VK_W);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addActEdgeClicked();
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/201.png");
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Add inhibiting edge (E)");
		tbutton.setMnemonic(KeyEvent.VK_W);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addInhEdgeClicked();
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/101.png");
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Delete a node or edge (D)");
		tbutton.setMnemonic(KeyEvent.VK_D);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				deleteClicked();
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/090.png");
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Draw a box to help organize the model (B)");
		tbutton.setMnemonic(KeyEvent.VK_B);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				toggle(DRAW_BUTTON);
				editMode = EDIT_MODE.DRAWING;
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		Object[] colors = model.getColors().toArray();
		JComboBox cb = new JComboBox(colors);
		cb.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				drawingColor = model.getColors()
						.indexOf((Color) arg0.getItem());
			}
		});
		cb.setRenderer(new ColorComboRenderer());

		toolbar.add(cb);

		icon = new ImageIcon("Resources/icons/16/146.png");
		JButton button = new JButton(icon);
		button
				.setToolTipText("Run a full simulation of the model to find equilibrium points and cycles (R)");
		button.setMnemonic(KeyEvent.VK_R);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				resetToggles();
				runSim();
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);

		// ADD HELP
		addHelp(
				"This mode is used to change the model by adding/removing nodes and edges, editing the state-tables of nodes, moving nodes, and editing the appearance of nodes. Boxes can also be drawn to add to the visual organization of the model.\nThis mode also provides the capability to analyze the full model to find all stable equilibrium and oscillation points.",
				300, 150);

		JMenu spacer = new JMenu();
		spacer.setPreferredSize(new Dimension(5000, 10));
		toolbar.add(spacer);

		JLabel modeName = new JLabel("Model Editor");
		modeName.setHorizontalAlignment(JLabel.LEFT);
		modeName.setVerticalAlignment(JLabel.TOP);
		modeName.setFont(new Font("helvetica", Font.BOLD, 16));
		this.add(modeName);
	}

	/**
	 * PRE: multiJointed is defined POST: RV = multiJointed
	 */
	public boolean isMultiJointed() {
		return multiJointed;
	}

	/**
	 * PRE: multiJointed is defined POST: multiJointed = multiJointed
	 */
	public void setMultiJointed(boolean multiJointed) {
		this.multiJointed = multiJointed;
	}

	/**
	 * initializes the GUI elements, in this case only the model name
	 */
	protected void initializeGUI() {
		super.initializeGUI();

		for (int i = 0; i < NUM_STATIC_ELEMS; i++) {
			staticGUI.add(null);
		}

		Font commonFont = new Font("arial", Font.BOLD, 12);
		Color buttonColor = Color.blue;
		Color activeColor = Color.pink;

		// Display model name
		staticGUI.set(MODEL_NAME, new GUIElement(new int[] { 500, 20, 10, 16 },
				"", buttonColor, activeColor, commonFont, false));

	}

	/**
	 * nudges any existing popup panels and updates the name position
	 */
	protected void resize() {
		super.resize();
		updateNames();
		if (tablePanel != null) {
			nudgePanel(tablePanel);
		}
		if (colorPanel != null) {
			nudgePanel(colorPanel);
		}
		if (namePanel != null) {
			nudgePanel(namePanel);
		}

	}

	/**
	 * the partial edge and node, if they exist
	 */
	public void paint(Graphics g) {
		super.paint(g);

		if (partialEdge != null) {
			partialEdge.paint(g);
		}
		if (partialDrawing != null) {
			partialDrawing.paint((Graphics2D) g);
		}

		g.setColor(Color.black);
		g.setFont(new Font("arial", Font.PLAIN, 12));
	}

	/**
	 * PRE: POST: this model has been saved, if it was not already, and cyclone
	 * has been run on it
	 */
	private void runSim() {
		if (model.getModelName().equals("Unsaved")) {
			model.saveModel();
		}

		runCyclone(model.getCurModel().writeSimFile());
	}

	/**
	 * PRE: simFile is defined as the file the simulation input file was saved
	 * to POST: Cyclone has been run on simFile and its output saved as
	 * simFile.res
	 * 
	 * @param simFile
	 */
	private void runCyclone(File simFile) {

		String outputName = "Models/"
				+ model.getModelName()
				+ "/"
				+ simFile.getName()
						.substring(0, simFile.getName().indexOf('.')) + ".res";

		System.out.println(simFile.getAbsolutePath());
		String[] cmd = { "ERROR: This should point to a Cyclone program",
				simFile.getAbsolutePath(), "-c", "1", "-v", "-f", outputName,
				"-it" };
		if (parent.os == OS.WINDOWS) {
			cmd[0] = "Resources/Cyclone32.exe";
		} else if (parent.os == OS.LINUX) {
			if (parent.arch == ARCH.BIT64) {
				cmd[0] = "Resources/Cyclone64";

			} else {
				cmd[0] = "Resources/Cyclone32";
			}
		} else if (parent.os == OS.MAC) {
			if (parent.arch == ARCH.BIT64) {
				cmd[0] = "Resources/Cyclone64";
			} else {
				cmd[0] = "Resources/Cyclone32";
			}
		}

		try {
			Process proc = Runtime.getRuntime().exec(cmd);

			InputStream stderr = proc.getInputStream();
			// PrintWriter out = new PrintWriter(new OutputStreamWriter(
			// proc.getOutputStream()), true);

			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}

			// int exitVal = proc.waitFor();

			proc.destroy();

			parent.updateBrowser();
			parent.setNewResult(model.getModelName(), outputName
					.substring(outputName.lastIndexOf('/') + 1), true);

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}

	}

	/**
	 * update the modelName and position
	 */
	protected void updateNames() {
		staticGUI.get(MODEL_NAME).setMessage(model.getModelName());
		staticGUI.get(MODEL_NAME).setPos(
				new int[] {
						getSize().width
								- (parent.mainPane.getDividerLocation())
								- (model.getModelName().length() * 4 + 100),
						20, staticGUI.get(MODEL_NAME).getPos()[2],
						staticGUI.get(MODEL_NAME).getPos()[3] });
	}

	/**
	 * PRE: click is defined POST: if partialEdge is null and click is not on a
	 * node, then nothing happens. If click is on a node, then partialEdge is a
	 * new edge with a start at the node click is on If partialEdge is already
	 * defined and click is not on a node, then a new anchor is added to
	 * partialEdge If partialEdge is already defined and click is on a node,
	 * then that node is set as its end and partialEdge is added to edges
	 * 
	 * @param click
	 */
	private void buildEdge(MouseEvent click) {
		if (partialEdge == null) {
			int nodeClicked = model.getCurModel().getNodeClicked(
					click.getPoint());

			if (nodeClicked != NOTHING) {
				partialEdge = new Edge(model.getNodes().get(nodeClicked), port
						.frameToRealCoord(model.getNodes().get(nodeClicked)
								.getPixPos()), model.getColors().get(
						model.getNodes().get(nodeClicked).getColorChoice()),
						model.getNodes().get(nodeClicked).getColorChoice(),
						edgeType, model.getCurModel(), multiJointed);
			}
		} else {
			int nodeClicked = model.getCurModel().getNodeClicked(
					port.realToFrameCoord(partialEdge.getAnchors().get(
							partialEdge.getAnchors().size() - 1)));

			if (nodeClicked == NOTHING) {
				partialEdge.addAnchor(port.frameToRealCoord(click.getPoint()));
			} else if (model.getNodes().get(nodeClicked) != partialEdge
					.getStart()) {
				addEdge(model.getNodes().get(nodeClicked));
				edgeSelected = model.getEdges().size() - 1;
				editMode = EDIT_MODE.NONE;
			} else {
				partialEdge = null;
				editMode = EDIT_MODE.NONE;
			}
		}

	}

	/**
	 * PRE: POST: if partialEdge is defined and of length > 1, then its last
	 * anchor has been removed If it is of length = 1, then partialEdge has been
	 * set to null If it is null then nothing happens
	 */
	private void removeEdgeAnchor() {
		if (partialEdge != null) {
			if (partialEdge.getAnchors().size() > 1) {
				partialEdge.removeLastAnchor();
			} else {
				partialEdge = null;
			}
		}
	}

	/**
	 * PRE: edgeToDelete and edges have been defined POST: edgeToDelete has been
	 * removed from edges and from its start and end outEdges and inEdges lists
	 * 
	 * @param edgeToDelete
	 */
	private void deleteEdge(Edge edgeToDelete) {
		edgeToDelete.getStart().removeOut(edgeToDelete);
		edgeToDelete.getEnd().removeIn(edgeToDelete);
		model.getEdges().remove(edgeToDelete);
		resetToggles();
		edgeSelected = NOTHING;
	}

	/**
	 * PRE: drawingToDelete is defined POST: the drawingToDelete has been
	 * removed from the model, button toggles have been reset, and
	 * drawingSelected has been set to nothing
	 * 
	 * @param drawingToDelete
	 */
	private void deleteDrawing(Drawing drawingToDelete) {
		model.getDrawings().remove(drawingToDelete);
		resetToggles();
		drawingSelected = NOTHING;
	}

	/**
	 * PRE: end is defined as a node, partialEdge is defined, edges is defined
	 * POST: partialEdge has been added to edges with its end set to end and
	 * partialEdge is now a null point, and all toggle buttons have been reset
	 * 
	 * @param end
	 */
	private void addEdge(Node end) {
		partialEdge.setEnd(end);
		model.getEdges().add(partialEdge);
		selectEdge(model.getEdges().size() - 1);
		partialEdge.getStart().addOutEdge(partialEdge);
		partialEdge.getEnd().addInEdge(partialEdge);
		partialEdge = null;
		editName(new Point(end.getPixPos()));
		model.setChangesMade(true);
		resetToggles();
	}

	/**
	 * PRE: coordinate is defined, nodes is defined POST: a new node has been
	 * added to nodes with name and color based on user input
	 */
	private void addNode(MouseEvent click) {
		try {
			partialNode = new Node(port.frameToRealCoord(click.getPoint()), 20,
					model.getCurModel());
			partialNode.resize();
			editName(click.getPoint());
			editStates(new Point(click.getPoint().x + 30, click.getPoint().y));
			editColor(new Point(click.getPoint().x, click.getPoint().y + 30));
			model.getNodes().add(partialNode);
			selectNode(model.getNodes().size() - 1);
			partialNode = null;
			model.setChangesMade(true);
			editMode = EDIT_MODE.NONE;
			resetToggles();

		} catch (InvalidTableException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * PRE: click is defined POST: sets the partial drawing's corner to the
	 * click, and adds the drawing to the model
	 * 
	 * @param click
	 */
	private void addDrawing(MouseEvent click) {
		model.getDrawings().add(partialDrawing);
		selectDrawing(model.getDrawings().size() - 1);
		partialDrawing = null;
		editMode = EDIT_MODE.NONE;
		model.setChangesMade(true);
		resetToggles();
	}

	/**
	 * PRE: nodeToDelete and nodes are defined POST: nodeToDelete is removed
	 * from nodes, all edges with nodeToDelete as their start or end are removed
	 * 
	 * @param nodeToDelete
	 */
	private void deleteNode(Node nodeToDelete) {
		ArrayList<Edge> edgesToDelete = nodeToDelete.getInEdges();
		int size = edgesToDelete.size();
		for (int i = 0; i < size; i++) {
			deleteEdge(edgesToDelete.get(0));
		}
		edgesToDelete = nodeToDelete.getOutEdges();
		size = edgesToDelete.size();
		for (int i = 0; i < size; i++) {
			deleteEdge(edgesToDelete.get(0));
		}
		model.getNodes().remove(nodeToDelete);

		editMode = EDIT_MODE.NONE;
		resetToggles();
		deselect();
		model.setChangesMade(true);
	}

	/**
	 * PRE: nodes[nodeSelected] is defined POST: nodes[nodeSelected]'s color is
	 * changed based on user input
	 */
	private void editColor(Point loc) {
		popupUp = true;
		colorPanel = new ColorPanel(new Point(loc.x - 165, loc.y - 35), this,
				model.getColors());
		nudgePanel(colorPanel);
		colorPanel.setOpaque(true);
		layer.add(colorPanel, new Integer(3));
		transferFocus();
	}

	/**
	 * PRE: loc is defined POST: popup the NamePanel next to the node or edge
	 * selected to edit
	 * 
	 * @param loc
	 */
	private void editName(Point loc) {
		popupUp = true;
		if (nodeSelected != NOTHING || partialNode != null) {
			namePanel = new NamePanel(new Point(loc.x - 165, loc.y - 110),
					this, NamePanel.EDITING.NODE);
		} else if (edgeSelected != NOTHING || partialEdge != null) {
			namePanel = new NamePanel(new Point(loc.x - 165, loc.y - 110),
					this, NamePanel.EDITING.EDGE);
		}
		nudgePanel(namePanel);
		namePanel.setOpaque(true);
		layer.add(namePanel, new Integer(3));
		transferFocus();
	}

	/**
	 * PRE: loc is defined POST: popup the StatesPanel next to the node selected
	 * to edit
	 * 
	 * @param loc
	 */
	private void editStates(Point loc) {
		statePanel = new StatePanel(new Point(loc.x, loc.y - 110), this);
		nudgePanel(statePanel);
		statePanel.setOpaque(true);
		layer.add(statePanel, new Integer(3));
		transferFocus();
		popupUp = true;
	}

	/**
	 * PRE: loc is defined POST: pops up the editVocab panel at loc for the
	 * number of states of nodeSelected
	 * 
	 * @param loc
	 */
	private void editVocab(Point loc) {
		if (partialNode != null) {
			vocabPanel = new VocabPanel(new Point(loc.x, loc.y), this, model
					.getVocab(), partialNode.getNumStates());
		} else if (nodeSelected != NOTHING
				&& nodeSelected < model.getNodes().size()) {
			vocabPanel = new VocabPanel(new Point(loc.x, loc.y), this, model
					.getVocab(), model.getNodes().get(nodeSelected)
					.getNumStates());
		}
		nudgePanel(vocabPanel);
		vocabPanel.setOpaque(true);
		layer.add(vocabPanel, new Integer(3));
		transferFocus();
		popupUp = true;
	}

	/**
	 * PRE: node[nodeSelected] is defined POST: node[nodeSelected]'s table is
	 * changed based on user input
	 */
	private void editTable() {
		tablePanel = new TablePanel(model.getNodes().get(nodeSelected)
				.getPixPos(), model.getNodes().get(nodeSelected), this);
		nudgePanel(tablePanel);
		tablePanel.setOpaque(true);
		layer.add(tablePanel, new Integer(3));

		transferFocus();
		popupUp = true;
	}

	/**
	 * PRE: nodes[nodeSelected] is defined POST: nodes[nodeSelected]'s name is
	 * changed based on user input
	 */
	protected void chooseNodeName(String newAbrevName, String newFullName) {
		if (editMode != EDIT_MODE.ADDING_EDGE) {
			if (partialNode != null) {
				if (newAbrevName != null) {
					partialNode.setNames(newAbrevName.replace(' ', '_'),
							newFullName.replace(' ', '_'));
				}
			} else if (nodeSelected < model.getNodes().size()) {
				if (newAbrevName != null) {
					if (newAbrevName.equalsIgnoreCase("")) {
						model.getNodes().get(nodeSelected).setNames("?",
								"Unnamed");
					} else {
						model.getNodes().get(nodeSelected).setNames(
								newAbrevName.replace(' ', '_'),
								newFullName.replace(' ', '_'));
					}
				}
			}
		}

		layer.remove(namePanel);
		namePanel = null;
		popupDown();

		model.setChangesMade(true);
	}

	/**
	 * PRE: nodes[nodeSelected] is defined POST: nodes[nodeSelected]'s name is
	 * changed based on user input
	 */
	protected void chooseEdgeName(String newName) {
		if (edgeSelected != NOTHING) {
			if (newName != null) {
				System.out.println(newName);
				if (newName.equalsIgnoreCase("")) {
					model.getEdges().get(edgeSelected).setName("regulates");
				} else {
					model.getEdges().get(edgeSelected).setName(
							newName.replace(' ', '_'));
				}
			}
			resetToggles();
		}
		layer.remove(namePanel);
		namePanel = null;
		popupDown();

		deselect();
		model.setChangesMade(true);
	}

	/**
	 * PRE: colorChoice and colorPanel are defined POST: if there is a node
	 * selected its color has been changed to colorChoice. If there was a
	 * partially added node (partialNode) then its color has been changed to
	 * colorChoice. colorPanel has been deleted and popupUp set to false;
	 * 
	 * @param colorChoice
	 */
	protected void chooseColor(int colorChoice, int shapeChoice) {

		if (partialNode != null) {
			if (colorChoice != -1) {
				partialNode.setColorChoice(colorChoice);
			} else {
				partialNode.setColorChoice(0);
			}
		} else if (nodeSelected < model.getNodes().size()
				&& nodeSelected != NOTHING) {
			if (colorChoice != -1) {
				model.getNodes().get(nodeSelected).setColorChoice(colorChoice);
				model.getNodes().get(nodeSelected).setShape(
						SHAPE.values()[shapeChoice]);
			}
		}
		layer.remove(colorPanel);
		colorPanel = null;
		popupDown();

		model.setChangesMade(true);

	}

	/**
	 * PRE: nodeSelected < model.getNodes().size() POST: if newTable is defined
	 * then nodeSelected has had its table updated to be newTable
	 * 
	 * @param newTable
	 */
	protected void chooseTable(NodeTable newTable) {
		if (newTable != null && newTable.isProper()
				&& nodeSelected < model.getNodes().size()
				&& nodeSelected != NOTHING) {
			model.getNodes().get(nodeSelected).setTable(newTable);
		} else if (newTable == null) {

		}

		layer.remove(tablePanel);
		tablePanel = null;
		popupDown();

		model.setChangesMade(true);
	}

	/**
	 * PRE: numberOfStates and nodeSelected are defined and nodeSelected is a
	 * valid node index POST: if numberOfStates != -1, nodeSelected has been set
	 * to the number of states specified by NumberOfStates and the editVocab
	 * panel has been popped up Otherwise, nothing changes. The statepanel is
	 * popped down
	 * 
	 * @param numberOfStates
	 */
	protected void chooseStates(int numberOfStates) {

		if (numberOfStates != -1) {
			if (partialNode != null) {
				partialNode.setNumStates(numberOfStates);
			} else if (nodeSelected < model.getNodes().size()
					&& nodeSelected != NOTHING) {
				model.getNodes().get(nodeSelected).setNumStates(numberOfStates);
				editVocab(statePanel.getLocation());
			}
			if (numberOfStates > model.getMaxStates()) {
				model.setMaxStates(numberOfStates);
			}
		}
		layer.remove(statePanel);
		statePanel = null;
		popupDown();

		model.setChangesMade(true);

	}

	/**
	 * PRE: newVocab is defined POST: if newVocab is null nothing happens but
	 * the panel is popped down. Otherwise, nodeSelected or partial node has its
	 * termsUsed changed to newVocab
	 * 
	 * @param newVocab
	 */
	protected void chooseVocab(ArrayList<Term> newVocab) {

		if (newVocab != null) {
			if (partialNode != null) {
				partialNode.setTermsUsed(newVocab);
			} else if (nodeSelected < model.getNodes().size()
					&& nodeSelected != NOTHING) {
				model.getNodes().get(nodeSelected).setTermsUsed(newVocab);
			}
		}
		layer.remove(vocabPanel);
		vocabPanel = null;
		popupDown();

		model.setChangesMade(true);
	}

	/**
	 * PRE: POST: RV = true if all popup panels are null, false otherwise
	 */
	private void popupDown() {
		popupUp = (colorPanel != null || statePanel != null
				|| namePanel != null || vocabPanel != null);
	}

	/**
	 * PRE: POST: sets all _Selected variables to NOTHING and sets Selected to
	 * false for any node, edge, or drawing that was selected
	 */
	private void deselect() {
		if (nodeSelected != NOTHING && nodeSelected < model.getNodes().size()) {
			model.getNodes().get(nodeSelected).setSelected(false);
			nodeSelected = NOTHING;
		}
		if (edgeSelected != NOTHING && edgeSelected < model.getEdges().size()) {
			model.getEdges().get(edgeSelected).setSelected(false);
			edgeSelected = NOTHING;
		}
		if (drawingSelected != NOTHING
				&& drawingSelected < model.getDrawings().size()) {
			model.getDrawings().get(drawingSelected).setSelected(false);
			drawingSelected = NOTHING;
		}
	}

	/**
	 * PRE: toggle buttons are defined POST: all toggle buttons have been set to
	 * off
	 */
	private void resetToggles() {
		partialNode = null;
		partialEdge = null;
		zoomPort = null;
		zoomingIn = false;
		partialDrawing = null;

		editMode = EDIT_MODE.NONE;
		if (partialNode != null) {
			partialNode = null;
		}
		if (partialEdge != null) {
			partialEdge = null;
		}
		if (zoomPort != null) {
			zoomPort = null;
			zoomingIn = false;
		}

		Component curComp;
		for (int i = 0; i < toolbar.getComponentCount(); i++) {
			curComp = toolbar.getComponent(i);
			if (curComp.getClass() == JToggleButton.class
					&& ((JToggleButton) curComp).isSelected()) {
				((JToggleButton) curComp).setSelected(false);
			}
		}
	}

	/**
	 * PRE: buttonPressed is defined POST: if buttonPressed is the index of a
	 * toggleButton in this component, it is toggled, all other toggle buttons
	 * are switched to off. All partial variables are set to null (drawings,
	 * nodes, edges), and zoom is reset
	 * 
	 * @param buttonPressed
	 */
	private void toggle(int buttonPressed) {
		partialNode = null;
		partialEdge = null;
		zoomPort = null;
		zoomingIn = false;
		partialDrawing = null;

		Component curComp;
		for (int i = 0; i < toolbar.getComponentCount(); i++) {
			curComp = toolbar.getComponent(i);
			if (curComp.getClass() == JToggleButton.class
					&& ((JToggleButton) curComp).isSelected()) {
				if (i == buttonPressed) {
					((JToggleButton) curComp)
							.setSelected(((JToggleButton) curComp).isSelected());
				} else {
					((JToggleButton) curComp).setSelected(false);
				}
			}
		}
	}

	/**
	 * this panel is updated to remove any knockout artifacts of a result/traj
	 * panel in the model, selectings are erased, and names are updated
	 */
	protected void update() {
		super.update();
		for (int i = 0; i < model.getNodes().size(); i++) {
			model.getNodes().get(i).setScale(1);
			if (model.getNodes().get(i).isKnockedOut()) {
				model.getNodes().get(i).setKnockedOut(false);
			}
		}
		deselect();
		updateNames();
		requestFocus();
	}

	/**
	 * PRE: nodeToSelect is defined and < |nodes| POST: nodeSelected =
	 * nodeToSelect, nodes[nodeToSelect] is set to selected, and everything else
	 * has been deselected
	 * 
	 * @param nodeToSelect
	 */
	private void selectNode(int nodeToSelect) {
		deselect();
		nodeSelected = nodeToSelect;
		model.getNodes().get(nodeToSelect).setSelected(true);
	}

	/**
	 * PRE: edgeToSelect is defined and < |edges| POST: edgeSelected =
	 * edgeToSelect, edges[edgeToSelect] is set to selected, and everything else
	 * has been deselected
	 * 
	 * @param edgeToSelect
	 */
	private void selectEdge(int edgeToSelect) {
		deselect();
		edgeSelected = edgeToSelect;
		model.getEdges().get(edgeToSelect).setSelected(true);
	}

	/**
	 * PRE: drawingToSelect is defined and < |drawings| POST: drawingSelected =
	 * drawingToSelect, drawings[drawingToSelect] is set to selected, and
	 * everything else has been deselected
	 * 
	 * @param drawingToSelect
	 */
	private void selectDrawing(int drawingToSelect) {
		deselect();
		drawingSelected = drawingToSelect;
		model.getDrawings().get(drawingToSelect).setSelected(true);
	}

	/**
	 * PRE: click has been defined, edges has been defined POST: if click fell
	 * within any edge then that edge has been set as the selected edge
	 * 
	 * @param click
	 * @return
	 */
	private int checkEdges(MouseEvent click) {
		int clicked = NOTHING;
		for (int i = 0; i < model.getEdges().size(); i++) {
			if (model.getEdges().get(i).clicked(click)) {
				deselect();
				clicked = i;
				selectEdge(clicked);
				if (click.getButton() == MouseEvent.BUTTON1) {
					if (editMode == EDIT_MODE.DELETING) {
						deleteEdge(model.getEdges().get(clicked));
					}
				} else if (click.getButton() == MouseEvent.BUTTON3) {
					((JCheckBoxMenuItem) rightClickEdgeMenu.getComponent(1))
							.setSelected(model.getEdges().get(edgeSelected)
									.isDrawName());
					rightClickEdgeMenu.show(this, click.getPoint().x, click
							.getPoint().y);
				}
			}
		}

		return clicked;
	}

	/**
	 * PRE: click has been defined, drawings has been defined POST: if click
	 * fell within any drawing then that drawing has been set as the selected
	 * drawing
	 * 
	 * @param click
	 * @return
	 */
	private int checkDrawings(MouseEvent click) {
		int clicked = NOTHING;
		for (int i = 0; i < model.getDrawings().size(); i++) {
			if (model.getDrawings().get(i).clicked(click.getPoint())) {
				deselect();
				clicked = i;
				selectDrawing(clicked);
				if (editMode == EDIT_MODE.DELETING) {
					deleteDrawing(model.getDrawings().get(clicked));
				}
			}
		}

		return clicked;
	}

	/**
	 * PRE: click is defined, nodes is defined POST: if the click fell within
	 * any node, that node is now the node selected
	 * 
	 * @param click
	 * @return
	 */
	private int checkNodes(MouseEvent click) {
		int clicked = NOTHING;
		deselect();
		clicked = getNodeClicked(click);
		if (clicked != NOTHING) {
			selectNode(clicked);
			if (click.getButton() == MouseEvent.BUTTON1) {
				if (editMode == EDIT_MODE.DELETING) {
					deleteNode(model.getNodes().get(clicked));
					nodeSelected = NOTHING;
				} else if (click.getButton() == MouseEvent.BUTTON1) {
					editTable();
				}
			} else if (click.getButton() == MouseEvent.BUTTON3) {
				rightClickNodeMenu.show(this, click.getPoint().x, click
						.getPoint().y);
			}
		}
		return clicked;
	}

	/**
	 * PRE: click is defined POST: all clickable GUI items have been checked and
	 * any functions pertaining to one clicked have been executed
	 * 
	 * @param click
	 */
	protected void checkClick(MouseEvent click) {
		int clicked = NOTHING;

		// if (checkButtons(click) == NOTHING) {
		if (editMode == EDIT_MODE.ADDING_NODE) {
			addNode(click);
		} else if (editMode == EDIT_MODE.ADDING_EDGE) {
			if (click.getButton() == MouseEvent.BUTTON1)
				buildEdge(click);
			else if (click.getButton() == MouseEvent.BUTTON3) {
				removeEdgeAnchor();
			}
		} else if (editMode == EDIT_MODE.DRAWING) {
			Point realCoords = port.frameToRealCoord(click.getPoint());
			partialDrawing = new Drawing(new Rectangle(realCoords.x,
					realCoords.y, 0, 0), drawingColor, model.getCurModel(),
					port);
			editMode = EDIT_MODE.DRAG_DRAWING;
		} else if (editMode == EDIT_MODE.DRAG_DRAWING) {
			addDrawing(click);
		} else {
			clicked = checkNodes(click);
			if (clicked == NOTHING) {
				clicked = checkEdges(click);
				if (clicked == NOTHING) {
					clicked = checkDrawings(click);
					if (clicked == NOTHING) {
						deselect();
					}
				}
			}
		}
		// }
	}

	/**
	 * PRE: the addNode button has been defined POST: the button has been
	 * toggled and, if on, mode has been set to ADDING_NODE
	 */
	public void addNodeClicked() {
		toggle(ADD_NODE_BUTTON);
		if (editMode != EDIT_MODE.ADDING_NODE) {
			editMode = EDIT_MODE.ADDING_NODE;
		} else {
			editMode = EDIT_MODE.NONE;
		}
	}

	/**
	 * PRE: the addActEdge button has been defined POST: the button has been
	 * toggled and, if on, mode has been set to ADDING_EDGE and edgeType set to
	 * ACTIVATING
	 */
	public void addActEdgeClicked() {
		if (edgeSelected != NOTHING) {
			model.getEdges().get(edgeSelected).setType(Edge.ACTIVATING, true);
			toggle(NOTHING);
		} else {
			toggle(ADD_ACT_EDGE_BUTTON);
			if (editMode != EDIT_MODE.ADDING_EDGE
					|| edgeType != Edge.ACTIVATING) {
				editMode = EDIT_MODE.ADDING_EDGE;
			} else {
				editMode = EDIT_MODE.NONE;
			}
			edgeType = Edge.ACTIVATING;
			if (partialEdge != null) {
				partialEdge.setType(Edge.ACTIVATING, false);
			}
		}
	}

	/**
	 * PRE: the addAInhEdge button has been defined POST: the button has been
	 * toggled and, if on, mode has been set to ADDING_EDGE and edgeType set to
	 * INHIBITING
	 */
	public void addInhEdgeClicked() {
		if (edgeSelected != NOTHING) {
			model.getEdges().get(edgeSelected).setType(Edge.INHIBITING, true);
			toggle(NOTHING);
		} else {
			toggle(ADD_INH_EDGE_BUTTON);
			if (editMode != EDIT_MODE.ADDING_EDGE
					|| edgeType != Edge.INHIBITING) {
				editMode = EDIT_MODE.ADDING_EDGE;
			} else {
				editMode = EDIT_MODE.NONE;
			}
			edgeType = Edge.INHIBITING;
			if (partialEdge != null) {
				partialEdge.setType(Edge.INHIBITING, false);
			}
		}
	}

	/**
	 * PRE: the delete button has been defined POST: the button has been toggled
	 * and, if on, mode has been set to DELETE, if there was something selected
	 * then it has been deleted and mode set back to NONE
	 */
	public void deleteClicked() {
		toggle(DELETE_BUTTON);
		if (editMode != EDIT_MODE.DELETING) {
			editMode = EDIT_MODE.DELETING;
			if (edgeSelected != NOTHING) {
				deleteEdge(model.getEdges().get(edgeSelected));
			} else if (nodeSelected != NOTHING) {
				deleteNode(model.getNodes().get(nodeSelected));
			} else if (drawingSelected != NOTHING) {
				deleteDrawing(model.getDrawings().get(drawingSelected));
			}
		} else {
			editMode = EDIT_MODE.NONE;
		}
	}

	/**
	 * PRE: the editName button has been defined POST: the popup for editing a
	 * name has been popped up if there is an edge or node selected
	 */
	public void editNameClicked() {
		resetToggles();
		if (nodeSelected != NOTHING) {
			editName(model.getNodes().get(nodeSelected).getPixPos());
		} else if (edgeSelected != NOTHING) {
			editName(model.getEdges().get(edgeSelected).getStart().getPixPos());
		}
	}

	/**
	 * PRE: the editColor button has been defined POST: the popup for editing a
	 * color/shape has been popped up if there is a node selected
	 */
	public void editColorClicked() {
		resetToggles();
		editColor(model.getNodes().get(nodeSelected).getPixPos());
	}

	/**
	 * PRE: the editStates button has been defined POST: the popup for editing
	 * states has been popped up if there is a node selected
	 */
	public void editStatesClicked() {
		resetToggles();
		editStates(model.getNodes().get(nodeSelected).getPixPos());
	}

	/**
	 * PRE: the draw button has been defined POST: mode has been switched to
	 * DRAWING
	 */
	public void drawClicked() {
		toggle(DRAW_BUTTON);
		if (editMode != EDIT_MODE.DRAWING) {
			editMode = EDIT_MODE.DRAWING;
		} else {
			editMode = EDIT_MODE.NONE;
		}
	}

	/**
	 * PRE: the simulateModel button has been clicked POST: the model has been
	 * simulated
	 */
	public void simulateModelClicked() {
		resetToggles();
		runSim();
	}

	protected boolean dragged(MouseEvent e) {
		boolean acted = super.dragged(e);
		if (!acted) {
			if (!popupUp && editMode == EDIT_MODE.NONE) {
				int nodeClicked = model.getCurModel().getNodeClicked(
						e.getPoint());
				if (nodeClicked != NOTHING) {
					nodeDrag = nodeClicked;
				}
				if (nodeDrag != NOTHING) {
					model.setChangesMade(true);
					model.getNodes().get(nodeDrag).setRealPos(
							port.frameToRealCoord(e.getPoint()));
				} else {
					Point changeInRealCoords = port.frameToRealCoord(new Point(
							oldMouse.x - e.getPoint().x, oldMouse.y
									- e.getPoint().y));

					changeInRealCoords.x -= port.getPort().x;
					changeInRealCoords.y -= port.getPort().y;
					port.setPort(new Rectangle(port.getPort().x
							+ changeInRealCoords.x, port.getPort().y
							+ changeInRealCoords.y, port.getPort().width, port
							.getPort().height));
					model.resize();
					oldMouse = e.getPoint();
				}
			}
		}
		return acted;
	}

	@Override
	protected boolean moved(MouseEvent e) {
		boolean acted = super.moved(e);
		if (!acted) {
			if (editMode == EDIT_MODE.ADDING_EDGE && partialEdge != null) {
				partialEdge.changeLastAnchor(port
						.frameToRealCoord(e.getPoint()));
			} else if (editMode == EDIT_MODE.DRAG_DRAWING) {
				if (partialDrawing == null) {
					editMode = EDIT_MODE.NONE;
				} else {
					partialDrawing.moveCorner(port.frameToRealCoord(e
							.getPoint()));
				}

			} else {
				int clicked = model.getCurModel().getNodeClicked(e.getPoint());
				if (clicked != NOTHING) {
					// popupTable(clicked);
				} else if (tempTablePanel != null) {
					// popdownTable();
				}
			}
		}
		return acted;
	}

	@Override
	protected boolean clicked(MouseEvent e) {
		boolean acted = super.clicked(e);
		if (!acted) {
			if (!popupUp) {
				checkClick(e);
			}
		}
		return acted;
	}

	protected boolean pressed(MouseEvent e) {
		boolean acted = super.pressed(e);
		if (!acted) {
			oldMouse = e.getPoint();
		}
		return acted;
	}

	@Override
	protected boolean released(MouseEvent e) {
		boolean acted = super.released(e);
		if (!acted) {
			nodeDrag = NOTHING;
		}
		return acted;
	}
}
