package bmv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;

import bmv.Node.SHAPE;

/**
 * This class contains all the data and methods for a discrete mathematical
 * model made up of nodes and edges, as well as visual elements such as drawings
 * 
 * @author plvines
 * 
 */
public class Model {
	protected static final int NOTHING = -1;
	protected String modelName;
	protected ArrayList<Node> nodes;
	protected ArrayList<Edge> edges;
	protected int maxStates;
	protected boolean changesMade, continuous;
	protected ArrayList<Color> colors;
	protected StateVocabulary vocab;
	protected boolean multiJointed;
	protected String notes;
	protected BMVManager pane;
	protected Dimension totalSize;
	protected Viewport port;
	protected ArrayList<Drawing> drawings;
	protected boolean drawEdgeNames;
	protected Thread modelWriteThread;
	protected boolean interruptSaving;

	/**
	 * PRE: pane and port are defined POST: this object has been initialized
	 * based on the pane and port and default values have been given for class
	 * variables
	 * 
	 * @param pane
	 * @param port
	 */
	public Model(BMVManager pane, Viewport port) {
		this.port = port;
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		drawings = new ArrayList<Drawing>();
		maxStates = 2;
		changesMade = false;
		modelName = "Unsaved";
		continuous = true;
		vocab = new StateVocabulary();
		initializeColors();
		notes = "";
		totalSize = new Dimension(4000, 4000);
		port.setTotalSize(totalSize);
		this.pane = pane;
		drawEdgeNames = true;
		interruptSaving = false;
	}

	/**
	 * PRE: modelFilename is the name of a model file or is null POST: this is
	 * instantiated to the model designated by the user or modelFilename
	 * 
	 * @param modelFilename
	 */
	public Model(BMVManager pane, Viewport port, String modelFilename) {
		this.port = port;
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
		drawings = new ArrayList<Drawing>();
		maxStates = 2;
		changesMade = false;
		modelName = "Unsaved";
		continuous = true;
		vocab = new StateVocabulary();
		initializeColors();
		notes = "";
		totalSize = new Dimension(4000, 4000);
		port.setTotalSize(totalSize);
		this.pane = pane;
		drawEdgeNames = true;
		interruptSaving = false;
		loadModel(modelFilename);
	}

	/**
	 * PRE: orig is defined POST: this is a deep copy of orig
	 * 
	 * @param orig
	 * @throws InvalidTableException
	 */
	public Model(Model orig) throws InvalidTableException {
		nodes = new ArrayList<Node>();
		for (int i = 0; i < orig.getNodes().size(); i++) {
			nodes.add(new Node(orig.getNodes().get(i)));
			nodes.get(nodes.size() - 1).setModel(this);
		}
		edges = new ArrayList<Edge>();
		for (int i = 0; i < orig.getEdges().size(); i++) {
			edges.add(new Edge(orig.getEdges().get(i)));
			edges.get(edges.size() - 1).setStart(
					nodes.get(orig.getNodes().indexOf(
							orig.getEdges().get(i).getStart())));
			edges.get(edges.size() - 1).setEnd(
					nodes.get(orig.getNodes().indexOf(
							orig.getEdges().get(i).getEnd())));
			edges.get(edges.size() - 1).getStart().addOutEdge(
					edges.get(edges.size() - 1));
			edges.get(edges.size() - 1).getEnd().addInEdge(
					edges.get(edges.size() - 1));
		}
		maxStates = orig.getMaxStates();
		modelName = orig.getModelName();
		changesMade = orig.isChangesMade();
		vocab = orig.getVocab();
		pane = orig.pane;
		totalSize = orig.getTotalSize();
		port = orig.port;
		port.setTotalSize(totalSize);
		multiJointed = orig.multiJointed;
		notes = orig.notes;
		drawEdgeNames = orig.isDrawEdgeNames();
		interruptSaving = false;
	}

	/**
	 * PRE: "Resources/colorconfig.cfg" exists POST: the colors arraylist has
	 * been initialized from the file colorconfig.cfg
	 */
	protected void initializeColors() {
		colors = new ArrayList<Color>();
		try {
			Scanner fileScan = new Scanner(
					new File("Resources/colorconfig.cfg"));

			while (fileScan.hasNext()) {
				colors.add(new Color(fileScan.nextInt(), fileScan.nextInt(),
						fileScan.nextInt()));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: click is defined POST: returns the index of the node click is inside
	 * of, or -1/NOTHING if click did not touch a node
	 * 
	 * @param click
	 * @return
	 */
	protected int getNodeClicked(Point click) {
		int nodeClicked = NOTHING;
		for (int i = 0; nodeClicked == -1 && i < nodes.size(); i++) {
			if (nodes.get(i).clicked(click)) {
				nodeClicked = i;
			}
		}
		return nodeClicked;
	}

	/**
	 * PRE: this model is defined POST: the configuration of this model is saved
	 * in a .csv file, along with the tables for all nodes with tables defined
	 * in separate .csv files, all of which are saved in a folder of the name
	 * saveFile, under Models/
	 */
	protected void saveModel() {
		File saveFile = null;
		if (modelName.equals("Unsaved")) {
			JFileChooser jfc;
			jfc = new JFileChooser(new File("Models/"));
			jfc.setDialogTitle("Save model as...");
			jfc.showSaveDialog(null);
			if (jfc.getSelectedFile() != null) {
				saveFile = jfc.getSelectedFile();

				modelName = saveFile.getName().substring(0,
						saveFile.getName().indexOf('.'));
			}
			// Allow the user to cancel saving and move on
			else {
				changesMade = false;
			}
		} else {
			saveFile = new File("Models/" + modelName + "/" + modelName);
		}
		if (saveFile != null) {
			if (!saveFile.canRead()) {
				saveFile = new File("Models/" + saveFile.getName());
				saveFile.mkdir();
				File expFolder = new File("Models/" + saveFile.getName()
						+ "/Experiments");
				expFolder.mkdir();
				File tableFolder = new File("Models/" + saveFile.getName()
						+ "/Tables");
				tableFolder.mkdir();
				saveFile = new File("Models/" + saveFile.getName() + "/"
						+ saveFile.getName() + ".csv");
			}

			if (modelWriteThread != null) {
				while (modelWriteThread.isAlive()) {
					interruptSaving = true;
				}
				interruptSaving = false;
			}
			modelWriteThread = new Thread(new ModelWriter(saveFile));
			modelWriteThread.start();
			changesMade = false;
		}
	}

	private class ModelWriter implements Runnable {

		File saveFile;
		File tempSaveFile;

		public ModelWriter(File saveFile) {
			super();
			this.saveFile = saveFile;
			tempSaveFile = new File(saveFile.getParent() + "/temp.csv");
		}

		/**
		 * PRE: saveFile is a valid file in the correct directory to save this
		 * model POST: this model has been saved in saveFile and neighboring
		 * node.csv files
		 * 
		 * @param saveFile
		 * @throws IOException
		 */
		@Override
		public void run() {
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(tempSaveFile));

				String modelInfo = modelName + "\n\n" + nodes.size()
						+ "\tNodes\n\n" + maxStates + "\tStates\n\n"
						+ totalSize.width + "\tWidth\n" + totalSize.height
						+ "\tHeight\n\n";

				if (!interruptSaving) {
					// Write nodes
					for (int i = 0; i < nodes.size(); i++) {
						modelInfo += "AbrevName:\t"
								+ nodes.get(i).getAbrevName() + "\nFullName:\t"
								+ nodes.get(i).getFullName() + "\nColor:\t"
								+ nodes.get(i).getColorChoice() + "\nShape:\t"
								+ nodes.get(i).getShape().ordinal()
								+ "\nRadius:\t" + nodes.get(i).getRad()
								+ "\nStates:\t" + nodes.get(i).getNumStates();
						for (int k = 0; k < nodes.get(i).getTermsUsed().size(); k++) {
							modelInfo += "\t"
									+ nodes.get(i).getTermsUsed().get(k)
											.getWord();
						}

						modelInfo += "\nLoc:\t" + nodes.get(i).getRealPos().x
								+ "\t" + nodes.get(i).getRealPos().y
								+ "\nTable:\tTables/"
								+ nodes.get(i).getAbrevName() + ".csv\n\n";
					}
					modelInfo += "\n" + edges.size() + "\tEdges\n\n";

					if (!interruptSaving) {
						// Write edges
						for (int i = 0; i < edges.size(); i++) {
							modelInfo += "Start:\t"
									+ edges.get(i).getStart().getAbrevName()
									+ "\nEnd:\t"
									+ edges.get(i).getEnd().getAbrevName()
									+ "\nName:\t" + edges.get(i).getName()
									+ "\nWeight:\t" + edges.get(i).getWeight();

							if (edges.get(i).isForcedType()) {
								modelInfo += "\nForcedType:\t"
										+ edges.get(i).getType();
							}
							modelInfo += "\nColor:\t"
									+ edges.get(i).getColorChoice() + "\n"
									+ edges.get(i).getAnchors().size()
									+ "\tAnchors:";
							for (int k = 0; k < edges.get(i).getAnchors()
									.size(); k++) {
								modelInfo += "\t"
										+ edges.get(i).getAnchors().get(k).x
										+ '\t'
										+ edges.get(i).getAnchors().get(k).y;
							}
							modelInfo += "\n\n";
						}
						if (!interruptSaving) {
							modelInfo += "\n" + drawings.size()
									+ "\tDrawings\n\n";
							for (int i = 0; i < drawings.size(); i++) {
								modelInfo += "\nColor:\t"
										+ drawings.get(i).getColorChoice()
										+ '\n' + drawings.get(i).getNumPoints()
										+ "\tPoints:\t";

								for (int k = 0; k < drawings.get(i)
										.getNumPoints(); k++) {
									modelInfo += ""
											+ drawings.get(i).getxPoints()[k]
											+ '\t'
											+ drawings.get(i).getyPoints()[k]
											+ '\t';
								}
								modelInfo += "\n\n";
							}

							modelInfo += "<NOTES>\n" + notes + "</NOTES>";

							if (!interruptSaving) {
								writer.write(modelInfo);

								writer.close();

								tempSaveFile.renameTo(saveFile);

								for (int i = 0; i < nodes.size(); i++) {
									if (nodes.get(i).getTableStatus() == Node.TABLE_STATUS.UP_TO_DATE)
										nodes.get(i).writeTable(
												saveFile.getParent()
														+ "/Tables/");
								}
							}
						}
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	protected void updateNotes() {
		notes = "" + pane.textPanel.getText();
		System.out.println("NOTES: " + notes);
	}

	/**
	 * PRE: "Models/modelName/" exists POST: this model's simulation file has
	 * been written to its model folder and named simulation.txt
	 * 
	 * @return
	 */
	protected File writeSimFile() {
		File simFile = null;
		try {
			simFile = new File("Models/" + modelName + "/simulation.txt");
			BufferedWriter writer = new BufferedWriter(new FileWriter(simFile));
			String simString = generateSimulationString();

			writer.write(simString);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return simFile;
	}

	/**
	 * PRE: nodes are all defined with valid tables POST: RV = string containing
	 * the text of a combo file used for running Cyclone, it consists of the
	 * number of nodes, followed by each node's table
	 * 
	 * @return
	 */
	private String generateSimulationString() {
		String simString = modelName + "\nsimulation\n" + nodes.size() + "\n";

		for (int i = 0; i < nodes.size(); i++) {
			simString += nodes.get(i).getNumStates() + "\t";
		}
		simString += "\n\n";

		boolean differentSpeeds = checkForDifferentSpeeds();
		Node curNode;
		NodeTable table;
		for (int a = 0; a < nodes.size(); a++) {
			curNode = nodes.get(a);

			// Add header information
			if (differentSpeeds) {
				simString += "SPEED: " + nodes.get(a).getTable().getSpeed()
						+ "\n";
			}
			for (int b = 0; b < curNode.getInEdges().size(); b++) {
				simString += "x"
						+ (nodes
								.indexOf(curNode.getInEdges().get(b).getStart()) + 1)
						+ " ";
			}
			simString += "x" + (a + 1) + " x" + (a + 1) + "\n";

			table = curNode.getTable();
			// if (!curNode.isKnockedOut()) {
			for (int c = 0; c < table.getRowCount(); c++) {
				simString += ((Term) table.getValueAt(c,
						table.getColumnCount() - 1)).getValue()
						+ "\n";
			}
			simString += "\n";
			// } else {
			// for (int c = 0; c < table.getRowCount(); c++) {
			// simString += "0\n";
			// }
			// simString += "\n";
			// }
		}

		return simString;
	}

	/**
	 * PRE: POST: returns true if two or more nodes have different speeds, false
	 * otherwise
	 * 
	 * @return
	 */
	private boolean checkForDifferentSpeeds() {
		boolean different = false;
		int firstSpeed = nodes.get(0).getTable().getSpeed();
		for (int i = 1; !different && i < nodes.size(); i++) {
			if (nodes.get(i).getTable().getSpeed() != firstSpeed) {
				different = true;
			}
		}
		return different;
	}

	/**
	 * PRE: scales is defined and |scales| >= |nodes| POST: all nodes have had
	 * their scale and knockout status updated to reflect the values in scales
	 * 
	 * @param scales
	 */
	protected void setScaleIndices(int[] scales) {
		for (int i = 0; i < nodes.size(); i++) {
			if (scales[i] >= 0) {
				nodes.get(i).setScaleIndex(scales[i]);
				nodes.get(i).setKnockedOut(false);
			} else {
				nodes.get(i).setScale(1);
				nodes.get(i).setKnockedOut(true);
			}
		}
	}

	/**
	 * PRE: POST: if a valid file was selected, then this object now contains
	 * all the data of the selected model including nodes, node tables, and
	 * edges
	 */
	protected void loadModel(String model) {
		File loadFile;
		pane.resetZoom();
		if (model == null) {
			JFileChooser jfc;
			jfc = new JFileChooser(new File("Models/"));
			jfc.setDialogTitle("Select a model file");
			jfc.showOpenDialog(null);
			loadFile = jfc.getSelectedFile();
		} else {
			if (model.contains(".")) {
				loadFile = new File("Models/"
						+ model.substring(0, model.indexOf('.')) + "/" + model);
			} else {
				loadFile = new File("Models/" + model + "/" + model + ".csv");
			}
		}
		try {
			pane.startLoading();
			vocab = new StateVocabulary();
			readModel(loadFile);
		} catch (Exception e) {
			e.printStackTrace();
			pane.doneLoading();
		}
	}

	/**
	 * PRE: loadFile is defined POST: the model specified by loadFile has been
	 * loaded into this object
	 * 
	 * @param loadFile
	 * @throws FileNotFoundException
	 * @throws InvalidTableException
	 */
	private void readModel(File loadFile) throws FileNotFoundException,
			InvalidTableException {

		Scanner filescan = new Scanner(loadFile);
		filescan.useDelimiter(Pattern.compile("(\t|\n)+"));
		pane.updateLoading();
		modelName = filescan.next();

		int numNodes = filescan.nextInt();
		filescan.next();

		maxStates = filescan.nextInt();
		filescan.next();

		totalSize = new Dimension(0, 0);
		totalSize.width = filescan.nextInt();
		filescan.next();
		totalSize.height = filescan.nextInt();
		filescan.next();
		port.setTotalSize(totalSize);

		File[] tables = new File[numNodes];
		pane.updateLoading();
		loadNodes(filescan, numNodes, tables);

		int numEdges = filescan.nextInt();
		filescan.next();
		pane.updateLoading();
		loadEdges(filescan, numEdges);

		int numDrawings = filescan.nextInt();
		filescan.next();
		pane.updateLoading();
		loadDrawings(filescan, numDrawings);

		pane.updateLoading();
		for (int i = 0; i < numNodes; i++) {
			if (tables[i].canRead()) {
				nodes.get(i).readTable(tables[i]);
			}
		}

		filescan.findWithinHorizon("<NOTES>", 0);
		filescan.nextLine();
		String line = filescan.nextLine();
		notes = "";
		while (!line.endsWith("</NOTES>") && filescan.hasNextLine()) {
			notes += line + "\n";
			line = filescan.nextLine();
		}
		notes += line.substring(0, line.indexOf("</NOTES>"));

		pane.doneLoading();
	}

	/**
	 * PRE: filescan is defined and set to the model file containing the node
	 * data, numNodes is the number of nodes in the model POST: filescan has
	 * been progressed to the end of the nodes listings, all nodes have been
	 * loaded into this model
	 * 
	 * @param filescan
	 * @param numNodes
	 * @throws InvalidTableException
	 */
	private void loadNodes(Scanner filescan, int numNodes, File[] tables)
			throws InvalidTableException {
		nodes = new ArrayList<Node>();
		ArrayList<Term> terms;
		String abrevName, fullName, labelCheck;
		int color;
		int shape;
		int states;
		double rad;
		Point loc;

		for (int i = 0; i < numNodes; i++) {
			filescan.next();
			abrevName = filescan.next();
			labelCheck = filescan.next();

			if (labelCheck.equalsIgnoreCase("fullname:")) {
				fullName = filescan.next();
				filescan.next();
			} else {
				fullName = abrevName;
			}
			color = filescan.nextInt();
			filescan.next();
			shape = filescan.nextInt();
			filescan.next();
			rad = filescan.nextDouble();
			filescan.next();
			states = filescan.nextInt();

			terms = new ArrayList<Term>();
			for (int k = 0; k < states; k++) {
				terms.add(new Term(filescan.next(), k));
			}

			filescan.next();
			loc = new Point(filescan.nextInt(), filescan.nextInt());
			filescan.next();
			tables[i] = new File("Models/" + modelName + "/" + filescan.next());

			nodes
					.add(new Node(abrevName, fullName, new ImageIcon(
							"Resources/Images/node" + shape + "-" + color
									+ ".png"), color, SHAPE.values()[shape],
							loc, rad, states, this, terms));
		}
	}

	/**
	 * PRE: filescan is defined and set to the model file containing the node
	 * data, numEdges is the number of edges in the model POST: filescan has
	 * been progressed to the end of the edges listings, all edges have been
	 * loaded into this model
	 * 
	 * @param filescan
	 * @param numEdges
	 */
	private void loadEdges(Scanner filescan, int numEdges) {
		String startName, endName, edgeName, labelCheck;
		int color, numAnchors;
		double weight;
		boolean multiJointed = false, forcedType = false;
		int type = 0;
		ArrayList<Point> anchors;
		Node start = null;
		Node end = null;

		edges = new ArrayList<Edge>();
		for (int i = 0; i < numEdges; i++) {
			filescan.next();
			startName = filescan.next();
			filescan.next();
			endName = filescan.next();
			filescan.next();
			edgeName = filescan.next();
			filescan.next();
			weight = filescan.nextDouble();
			labelCheck = filescan.next();
			if (labelCheck.equalsIgnoreCase("forcedType:")) {
				forcedType = true;
				type = filescan.nextInt();
				filescan.next();
			}
			color = filescan.nextInt();
			numAnchors = filescan.nextInt();
			multiJointed = (numAnchors != 2);
			filescan.next();
			anchors = new ArrayList<Point>(numAnchors);
			for (int k = 0; k < numAnchors; k++) {
				anchors.add(new Point(filescan.nextInt(), filescan.nextInt()));
			}

			for (Node startIter : nodes) {
				if (startIter.getAbrevName().equalsIgnoreCase(startName)) {
					start = startIter;
				}
			}
			for (Node endIter : nodes) {
				if (endIter.getAbrevName().equalsIgnoreCase(endName)) {
					end = endIter;
				}
			}
			edges.add(new Edge(start, end, weight, anchors, colors.get(color),
					color, edgeName, this, multiJointed));
			if (forcedType) {
				edges.get(edges.size() - 1).setType(type, true);
			}
			ArrayList<Edge> tempEdges = start.getOutEdges();
			tempEdges.add(edges.get(edges.size() - 1));
			start.setOutEdges(tempEdges);
			tempEdges = end.getInEdges();
			tempEdges.add(edges.get(edges.size() - 1));
			end.setInEdges(tempEdges);
		}
	}

	/**
	 * PRE: scan and numDrawings is defined POST: numDrawings drawings in scan
	 * have been loaded, initializing the drawings arraylist
	 * 
	 * @param scan
	 * @param numDrawings
	 */
	private void loadDrawings(Scanner scan, int numDrawings) {
		drawings = new ArrayList<Drawing>();
		int colorChoice, numPoints;
		int[] xPoints, yPoints;

		for (int i = 0; i < numDrawings; i++) {
			scan.next();
			colorChoice = scan.nextInt();
			numPoints = scan.nextInt();
			scan.next();

			xPoints = new int[numPoints];
			yPoints = new int[numPoints];
			for (int k = 0; k < numPoints; k++) {
				xPoints[k] = scan.nextInt();
				yPoints[k] = scan.nextInt();
			}

			drawings.add(new Drawing(colorChoice, xPoints, yPoints, numPoints,
					this, port));
		}
	}

	/**
	 * PRE: POST: all nodes, edges, and drawings have been adjusted based on the
	 * current port size
	 */
	public void resize() {
		// this.divSize = divSize;
		for (int i = 0; i < nodes.size(); i++) {
			nodes.get(i).resize();
		}
		for (int i = 0; i < edges.size(); i++) {
			edges.get(i).resize();
		}
		for (int i = 0; i < drawings.size(); i++) {
			drawings.get(i).resize();
		}
	}

	/**
	 * PRE: orig is defined POST: this model is now a shallow copy of orig
	 * 
	 * @param orig
	 */
	public void becomeModel(Model orig) {
		nodes = orig.getNodes();

		edges = orig.getEdges();
		maxStates = orig.getMaxStates();
		modelName = orig.getModelName();
		changesMade = orig.isChangesMade();
		vocab = orig.getVocab();
		pane = orig.pane;
	}

	/**
	 * PRE: modelName is defined POST: RV = modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * PRE: modelName is defined POST: modelName = modelName
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * PRE: nodes is defined POST: RV = nodes
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}

	/**
	 * PRE: nodes is defined POST: nodes = nodes
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}

	/**
	 * PRE: edges is defined POST: RV = edges
	 */
	public ArrayList<Edge> getEdges() {
		return edges;
	}

	/**
	 * PRE: edges is defined POST: edges = edges
	 */
	public void setEdges(ArrayList<Edge> edges) {
		this.edges = edges;
	}

	/**
	 * PRE: maxStates is defined POST: RV = maxStates
	 */
	public int getMaxStates() {
		return maxStates;
	}

	/**
	 * PRE: maxStates is defined POST: maxStates = maxStates
	 */
	public void setMaxStates(int maxStates) {
		this.maxStates = maxStates;
	}

	/**
	 * PRE: changesMade is defined POST: RV = changesMade
	 */
	public boolean isChangesMade() {
		return changesMade;
	}

	/**
	 * PRE: changesMade is defined POST: changesMade = changesMade
	 */
	public void setChangesMade(boolean changesMade) {
		this.changesMade = changesMade;
	}

	/**
	 * PRE: continuous is defined POST: RV = continuous
	 */
	public boolean isContinuous() {
		return continuous;
	}

	/**
	 * PRE: continuous is defined POST: continuous = continuous
	 */
	public void setContinuous(boolean continuous) {
		this.continuous = continuous;
	}

	/**
	 * PRE: colors is defined POST: RV = colors
	 */
	public ArrayList<Color> getColors() {
		return colors;
	}

	/**
	 * PRE: colors is defined POST: colors = colors
	 */
	public void setColors(ArrayList<Color> colors) {
		this.colors = colors;
	}

	/**
	 * PRE: vocab is defined POST: RV = vocab
	 */
	public StateVocabulary getVocab() {
		return vocab;
	}

	/**
	 * PRE: vocab is defined POST: vocab = vocab
	 */
	public void setVocab(StateVocabulary vocab) {
		this.vocab = vocab;
	}

	/**
	 * PRE: totalSize is defined POST: RV = totalSize
	 */
	public Dimension getTotalSize() {
		return totalSize;
	}

	/**
	 * PRE: port is defined POST: RV = port
	 */
	public Viewport getPort() {
		return port;
	}

	/**
	 * PRE: port is defined POST: port = port
	 */
	public void setPort(Viewport port) {
		this.port = port;
	}

	/**
	 * PRE: drawings is defined POST: RV = drawings
	 */
	public ArrayList<Drawing> getDrawings() {
		return drawings;
	}

	/**
	 * PRE: drawings is defined POST: drawings = drawings
	 */
	public void setDrawings(ArrayList<Drawing> drawings) {
		this.drawings = drawings;
	}

	/**
	 * PRE: totalSize is defined POST: totalSize = totalSize
	 */
	public void setTotalSize(Dimension totalSize) {
		this.totalSize = totalSize;
	}

	public String toString() {
		String str = "";
		str += modelName + " " + maxStates;
		return str;
	}

	/**
	 * PRE: drawEdgeNames is defined POST: RV = drawEdgeNames
	 */
	public boolean isDrawEdgeNames() {
		return drawEdgeNames;
	}

	/**
	 * PRE: notes is defined POST: RV = notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * PRE: notes is defined POST: notes = notes
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * PRE: pane is defined POST: RV = pane
	 */
	public BMVManager getPane() {
		return pane;
	}

	/**
	 * PRE: pane is defined POST: pane = pane
	 */
	public void setPane(BMVManager pane) {
		this.pane = pane;
	}

	/**
	 * PRE: drawEdgeNames is defined POST: drawEdgeNames = drawEdgeNames
	 */
	public void setDrawEdgeNames(boolean drawEdgeNames) {
		this.drawEdgeNames = drawEdgeNames;
		for (int i = 0; i < edges.size(); i++) {
			edges.get(i).setDrawName(drawEdgeNames);
		}
	}

	public boolean equals(Model other) {
		return other.getModelName().equals(this.getModelName());
	}

}
