package bmv;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToggleButton;
import bmv.BMVManager.ARCH;
import bmv.BMVManager.OS;

/**
 * This subclass of BMVPanel handles the experiment setup and editing, including
 * running experiment, batch knockouts, and stochastic runs.
 * 
 * @author plvines
 * 
 */
public class TrajPanel extends BMVPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7707176937026465467L;

	protected enum MODE {
		NOTHING, KNOCKOUT
	};

	private final static int TRAJ_NAME = 1, MODEL_NAME = 0;
	private final static int NUM_STATIC_ELEMS = 2;

	protected int nodeSelected;
	protected MODE mode;
	private RandomRunPanel randomRunPanel;

	public TrajPanel(ModelHolder model, Viewport port, Trajectory trajectory,
			BMVManager pLayer) {
		super(model, port, trajectory, pLayer);
	}

	/**
	 * initializes the TrajPanel-specific variables to default values
	 */
	protected void initialize() {
		super.initialize();
		bg = new ImageIcon("Resources/Images/bgTraj.png");
		setPreferredSize(START_SIZE);
		nodeSelected = NOTHING;
		mode = MODE.NOTHING;
		updateNames();

	}

	/**
	 * initializes toolbar and labels
	 */
	protected void initializeMenu() {
		super.initializeMenu();

		ImageIcon icon = new ImageIcon("Resources/icons/16/150.png");
		JToggleButton tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Fully knockdown this node for the experiment (K)");
		tbutton.setMnemonic(KeyEvent.VK_K);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (nodeSelected != NOTHING) {
					trajectory.knockout(nodeSelected);
				} else {
					if (mode != MODE.KNOCKOUT) {
						mode = MODE.KNOCKOUT;
					} else {
						mode = MODE.NOTHING;
					}
				}
				zoomingIn = false;
				zoomPort = null;
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/202.png");
		JButton button = new JButton(icon);
		button.setToolTipText("Simulate this experiment in the model (E)");
		button.setMnemonic(KeyEvent.VK_E);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				runTraj();
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/203.png");
		button = new JButton(icon);
		button.setToolTipText("Simulate this experiment in the model with each node knocked out one at a time (B)");
		button.setMnemonic(KeyEvent.VK_B);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				runBatchKnockout();
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/204.png");
		button = new JButton(icon);
		button.setToolTipText("Simulate this experiment updating a single random variable each time (R)");
		button.setMnemonic(KeyEvent.VK_R);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				promptRandomRun();
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);

		// JMenu spacer = new JMenu();
		// spacer.setPreferredSize(new Dimension(900, 10));
		// toolbar.add(spacer);

		addHelp("This mode is used to set up the starting states for an experiment. Once this is set, the experiment can be run, which will simulate the model starting at the specified states and continuing until reaching a stable equilibrial or oscillatory state.\nIn adding to setting the starting state of each node, nodes may also be \"knocked out\", which sets them to always be at their lowest state throughout the experimental simulation.\n\nIn addition to the basic simulation, there two other modes of simulation: Multi-KO and stochastic.\nIn multi-KO the program will run multiple simulations of the experiment, each with one node knocked out. This provides an easy way to generate results for a netowrk analysis involving multiple single-knockouts.\nStochastic runs will run the experiment from the specified start. However, instead of updating all nodes at each step, only one, randomly selected, node will be updated at each step. This type of simulation is not guaranteed to end in a stable equilibrium or oscillation so the user must specify the number of steps to simulate. Due to the random nature of this simulation, it is also helpful to run multiple copies of it to generate an average result for each step so the user will be asked to specify the number of runs as well.",
				300, 300);

		JLabel modeName = new JLabel("Experimental Setup");
		modeName.setHorizontalAlignment(JLabel.LEFT);
		modeName.setVerticalAlignment(JLabel.TOP);
		modeName.setFont(new Font("helvetica", Font.BOLD, 16));
		this.add(modeName);
	}

	/**
	 * initializes trajectory and model name displays
	 */
	protected void initializeGUI() {
		super.initializeGUI();

		for (int i = 0; i < NUM_STATIC_ELEMS; i++) {
			staticGUI.add(null);
		}

		Font commonFont = new Font("arial", Font.BOLD, 12);
		Color buttonColor = Color.blue;
		Color activeColor = Color.pink;
		int buttonHeight = 19;

		// Model Name
		staticGUI.set(MODEL_NAME, new GUIElement(new int[] { 550,
				10 + buttonHeight * 0, 10, 16 }, "", buttonColor, activeColor,
				commonFont, false));

		// Traj Name
		staticGUI.set(TRAJ_NAME, new GUIElement(new int[] { 550,
				10 + buttonHeight * 1, 10, 16 }, "", buttonColor, activeColor,
				commonFont, false));

	}

	public void paint(Graphics g) {
		super.paint(g);

		// Paints the actual state number next to each node
		g.setColor(Color.black);
		g.setFont(new Font("arial", Font.BOLD, 16));
		for (Node node : model.getNodes()) {
			g.drawString(
					node.getTermsUsed().get(node.getScaleIndex()).getWord(),
					(int) (node.getPixPos().x - 5 - (node.getScaledRad() / 1.25)),
					(int) (node.getPixPos().y - 5 - (node.getScaledRad() / 1.25)));
		}
	}

	protected void resize() {
		super.resize();
		updateNames();

	}

	/**
	 * PRE: model is defined POST: nodes, edges ,and startLevels are updated to
	 * reflect the model being used
	 * 
	 * @param model
	 */
	protected void update() {
		super.update();
		trajectory.updateStartLevels();
		for (int i = 0; i < model.getNodes().size(); i++) {
			model.getCurModel().setScaleIndices(trajectory.getStartLevels());
		}
		updateNames();
	}

	/**
	 * updates the positioning of the model and trajectory names
	 */
	protected void updateNames() {
		staticGUI.get(MODEL_NAME).setMessage(model.getModelName());
		staticGUI.get(TRAJ_NAME).setMessage(trajectory.getTrajName());
		staticGUI.get(MODEL_NAME).setPos(
				new int[] {
						getSize().width
								- (parent.mainPane.getDividerLocation())
								- (model.getModelName().length() * 4 + 100),
						20, staticGUI.get(MODEL_NAME).getPos()[2],
						staticGUI.get(MODEL_NAME).getPos()[3] });
		staticGUI
				.get(TRAJ_NAME)
				.setPos(new int[] {
						getSize().width
								- (parent.mainPane.getDividerLocation())
								- (trajectory.getTrajName().length() * 4 + 100),
						35, staticGUI.get(TRAJ_NAME).getPos()[2],
						staticGUI.get(TRAJ_NAME).getPos()[3] });
	}

	/**
	 * PRE:
	 * POST: the RandomRunPanel has been instantiated and added to the layer,
	 * prompting the user to select step and run numbers
	 */
	private void promptRandomRun() {
		randomRunPanel = new RandomRunPanel(new Point(50, 50), this);
		nudgePanel(randomRunPanel);
		randomRunPanel.setOpaque(true);
		layer.add(randomRunPanel, new Integer(3));
		transferFocus();
		popupUp = true;

	}

	/**
	 * PRE: the trajectory is defined
	 * POST: a copy of this trajectory has been created for each node in the
	 * model, and each of these copies has a single, different, node knocked
	 * out, and they have all been simulated
	 */
	private void runBatchKnockout() {
		Trajectory defaultTraj = new Trajectory(model.getCurModel());
		Trajectory currentTraj = new Trajectory(trajectory);
		defaultTraj.setTrajName("batchTraj");
		defaultTraj.setStartLevels(trajectory.getStartLevels());
		defaultTraj.saveTraj(false);
		trajectory.saveTraj(true);
		for (int i = 0; i < model.getNodes().size(); i++) {
			trajectory.loadTraj(new File("Models/" + model.getModelName()
					+ "/Experiments/batchTraj.exp"));
			trajectory.setTrajName(currentTraj.getTrajName() + "_"
					+ model.getNodes().get(i).getAbrevName() + "_KO");
			trajectory.knockout(i);
			trajectory.saveTraj(false);
			runTraj();
		}
		(new File("Models/" + model.getModelName()
				+ "/Experiments/batchTraj.exp")).delete();

		trajectory.loadTraj(currentTraj);
		parent.updateBrowser();
	}

	/**
	 * PRE: numRuns and numSteps are defined
	 * POST: the current trajectory has been run numRuns times in random update
	 * mode, running for numSteps each time
	 * 
	 * @param numRuns
	 * @param numSteps
	 */
	protected void runRandomUpdateTraj(int numRuns, int numSteps) {
		if (numRuns > 0 && numSteps > 0) {
			if (trajectory.getTrajName().equals("Unsaved")) {
				trajectory.saveTraj(true);
			}
			model.getCurModel().writeSimFile();

			String outputName = "Models/" + model.getModelName()
					+ "/Experiments/" + trajectory.getTrajName() + ".randres";
			File trajFile = new File("Models/" + model.getModelName()
					+ "/Experiments/" + trajectory.getTrajName() + ".exp");
			File simFile = new File("Models/" + model.getModelName()
					+ "/simulation.txt");
			String[] cmd = new String[] {
					"ERROR: This should point to a Cyclone program",
					simFile.getAbsolutePath(), "-c", "1", "-traj",
					trajFile.getAbsolutePath(), "-v", "-f", outputName,
					"-random", "" + numRuns, "" + numSteps, "-it" };

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

			runCyclone(outputName, trajFile, simFile, cmd);
		}

		layer.remove(randomRunPanel);
		randomRunPanel = null;
		popupDown();
	}

	/**
	 * PRE:
	 * POST returns true if all popups are down:
	 */
	private void popupDown() {
		popupUp = (randomRunPanel != null);
	}

	/**
	 * PRE:
	 * POST: the current trajectory has been saved and cyclone has been started
	 * running on it and the model simulaiton file.
	 */
	private void runTraj() {
		trajectory.saveTraj(true);

		model.getCurModel().writeSimFile();
		String outputName = "Models/" + model.getModelName() + "/Experiments/"
				+ trajectory.getTrajName() + ".res";

		File trajFile = new File("Models/" + model.getModelName()
				+ "/Experiments/" + trajectory.getTrajName() + ".exp");
		File simFile = new File("Models/" + model.getModelName()
				+ "/simulation.txt");

		String[] cmd = new String[] {
				"ERROR: This should point to a Cyclone program",
				simFile.getAbsolutePath(), "-c", "1", "-traj",
				trajFile.getAbsolutePath(), "-v", "-f", outputName, "-it" };
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
		runCyclone(outputName, trajFile, simFile, cmd);
	}

	/**
	 * PRE: outputName, trajFile, simFile, and cmd are defined
	 * POST: a new process has been started running cmd
	 * 
	 * @param outputName
	 * @param trajFile
	 * @param simFile
	 * @param cmd
	 */
	private void runCyclone(String outputName, File trajFile, File simFile,
			String[] cmd) {

		try {
			Process proc = Runtime.getRuntime().exec(cmd);

			InputStream stderr = proc.getInputStream();

			InputStreamReader isr = new InputStreamReader(stderr);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null)
				System.out.println(line);

			// int exitVal = proc.waitFor();

			proc.destroy();
			parent.updateBrowser();
			parent.setNewResult(model.getModelName(),
					outputName.substring(outputName.lastIndexOf('/') + 1), true);

		} catch (Exception e) {
			System.out.println("error");
			e.printStackTrace();
		}
	}

	/**
	 * PRE: node is defined, drag is defined POST: the node has been scaled to
	 * the smallest scale encompassing the drag point, to a maximum of its
	 * largest scale available. StartLevels[node] have been similarly changed to
	 * the scaleIndex of that scale
	 * 
	 * @param node
	 */
	protected void scaleNode(Node node, Point drag) {
		if (node != null) {
			trajectory.unknockout(nodeSelected);
			int dx = drag.x - node.getPixPos().x;
			int dy = drag.y - node.getPixPos().y;
			double dist = (dx * dx) + (dy * dy);
			dist /= node.getRad() * node.getRad();

			double[] scaleRange = Node.scaleRange[node.getNumStates()];
			boolean unset = true;
			for (int i = 0; unset && i < scaleRange.length; i++) {
				if (dist < scaleRange[i] * scaleRange[i]) {
					node.setScaleIndex(i);
					trajectory.getStartLevels()[nodeSelected] = i;
					unset = false;
				}
			}
			// dragged beyond greatest scale
			if (unset) {
				node.setScaleIndex(scaleRange.length - 1);
				trajectory.getStartLevels()[nodeSelected] = scaleRange.length - 1;
			}
		}
	}

	/**
	 * PRE: node and changeAmount have been defined
	 * POST: node is scaled by changeAmount
	 * 
	 * @param node
	 * @param changeAmount
	 */
	protected void scaleNode(Node node, int changeAmount) {
		if (node != null) {
			trajectory.unknockout(nodeSelected);
			double[] scaleRange = Node.scaleRange[node.getNumStates()];
			boolean unset = true;
			for (int i = 0; unset && i < scaleRange.length; i++) {
				int newScale = node.getScaleIndex() + changeAmount;
				if (newScale >= node.getNumStates()) {
					newScale = newScale % node.getNumStates();
				}
				node.setScaleIndex(newScale);
				trajectory.getStartLevels()[nodeSelected] = newScale;
				unset = false;
			}
		}
	}

	/**
	 * PRE: trajectory is defined POST: RV = trajectory
	 */
	public Trajectory getTrajectory() {
		return trajectory;
	}

	/**
	 * PRE: trajectory is defined POST: trajectory = trajectory
	 */
	public void setTrajectory(Trajectory trajectory) {
		this.trajectory = trajectory;
	}

	/**
	 * PRE: the knockout button has been clicked
	 * POST: if there was a node selected, it has been knocked out. Otherwise
	 * mode has been changed to Knockout so that the next node selected will be
	 * knocket out
	 */
	public void knockoutClicked() {
		if (nodeSelected != NOTHING) {
			trajectory.knockout(nodeSelected);
		} else {
			if (mode != MODE.KNOCKOUT) {
				mode = MODE.KNOCKOUT;
			} else {
				mode = MODE.NOTHING;
			}
		}
		zoomingIn = false;
		zoomPort = null;

	}

	public void runExpClicked() {
		runTraj();
		zoomingIn = false;
		zoomPort = null;
	}

	public void runKOClicked() {
		runBatchKnockout();
		zoomingIn = false;
		zoomPort = null;

	}

	public void runRandomClicked() {
		promptRandomRun();
	}

	@Override
	protected boolean dragged(MouseEvent e) {
		boolean acted = super.dragged(e);
		if (!acted) {
			if (nodeSelected != NOTHING && mode != MODE.KNOCKOUT) {
				scaleNode(model.getNodes().get(nodeSelected), e.getPoint());
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
		return acted;
	}

	protected boolean clicked(MouseEvent e) {
		boolean acted = super.clicked(e);
		if (!acted) {
			if (e.getClickCount() > 1 && nodeSelected != NOTHING) {
				scaleNode(model.getNodes().get(nodeSelected), 1);
			}
		}
		return acted;
	}

	@Override
	protected boolean pressed(MouseEvent e) {
		boolean acted = super.pressed(e);
		if (!acted) {
			if (nodeSelected != NOTHING) {
				model.getNodes().get(nodeSelected).setSelected(false);
			}
			nodeSelected = model.getCurModel().getNodeClicked(e.getPoint());
			if (nodeSelected != NOTHING) {
				model.getNodes().get(nodeSelected).setSelected(true);
				if (mode == MODE.KNOCKOUT) {
					trajectory.knockout(nodeSelected);
					mode = MODE.NOTHING;
				}
			}
		}
		return acted;
	}
}
