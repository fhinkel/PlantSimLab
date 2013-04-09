package bmv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Class for viewing results of experiments and simulations. Includes the
 * ability to change resolutions of results, view thme as line graphs, bar
 * graphs, and the default view of dynamically rescaling nodes
 * 
 * @author plvines
 * 
 */
public class ResultsPanel extends BMVPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1773842664496877882L;

	protected static enum MODE {
		TRAJ, FULL, RANDOM
	};

	private int timeStepSize = 1000;

	private static final int SPD = 0, CYC = 1, STATE = 2, IN_CYCLE = 3,
			RESOLUTION = 4, CYC_INFO = 5, TRAJ_NAME = 6, MODEL_NAME = 7;
	private final static int NUM_STATIC_ELEMS = 8;

	private ArrayList<Result> results;
	private long curTime, oldTime, dTime;
	private int curTimeStep, prevTimeStep, tranSpeed;
	private int cycleSelected;
	private boolean paused, reading, noFile, inExperiments;
	private MODE mode;
	private int displayedState;
	private BarPanel barPanel;
	private LinePanel linePanel;
	private String runName;
	private boolean[] knockouts;
	private String notes;
	private File resultsFile;
	private int resolution;
	private JSlider resolutionSlider;

	public ResultsPanel(ModelHolder model, Viewport port,
			Trajectory trajectory, BMVManager p) {
		super(model, port, trajectory, p);
	}

	/**
	 * initialize resultPanel specific variables to defaults
	 */
	protected void initialize() {
		super.initialize();
		bg = new ImageIcon("Resources/Images/bgResults.png");
		setPreferredSize(START_SIZE);

		results = new ArrayList<Result>();
		paused = false;
		reading = false;
		noFile = true;
		knockouts = new boolean[0];
		runName = "";
		mode = MODE.FULL;
		curTimeStep = 0;
		prevTimeStep = 0;
		displayedState = 0;
		cycleSelected = NOTHING;
		tranSpeed = 5;
		barPanel = null;
		timeStepSize = (int) (((double) 1 / tranSpeed) * 5000);
		curTime = System.currentTimeMillis();
		oldTime = curTime;
		dTime = curTime - oldTime;
		inExperiments = false;
		pause();
		notes = "";
		resolution = 1;
	}

	/**
	 * initialize the toolbar and resolution slider
	 */
	protected void initializeMenu() {
		super.initializeMenu();

		ImageIcon icon = new ImageIcon("Resources/icons/16/104.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		JButton button = new JButton(icon);
		button.setToolTipText("Decrease Speed");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeSpeed(-1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/103.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		button = new JButton(icon);
		button.setToolTipText("Increase Speed");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeSpeed(+1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/141.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		JToggleButton tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Play/Pause (P)");
		tbutton.setMnemonic(KeyEvent.VK_P);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				pause();
				zoomingIn = false;
				zoomPort = null;
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/132.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		button = new JButton(icon);
		button.setToolTipText("Previous State (Left)");
		button.setMnemonic(KeyEvent.VK_LEFT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeState(-1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/131.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		button = new JButton(icon);
		button.setToolTipText("Next State (Right)");
		button.setMnemonic(KeyEvent.VK_RIGHT);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeState(+1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/138.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		button = new JButton(icon);
		button.setToolTipText("Previous Cycle (Up)");
		button.setMnemonic(KeyEvent.VK_UP);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeCycle(-1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/137.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		button = new JButton(icon);
		button.setToolTipText("Next Cycle (Down)");
		button.setMnemonic(KeyEvent.VK_DOWN);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				changeCycle(+1);
				zoomingIn = false;
				zoomPort = null;
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(new Dimension(10, 10));
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/081.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Show Bargraph (B)");
		tbutton.setMnemonic(KeyEvent.VK_B);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addBars();
				zoomingIn = false;
				zoomPort = null;
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(new Dimension(10, 10));
		toolbar.add(tbutton);

		icon = new ImageIcon("Resources/icons/16/083.png");
		// icon = new ImageIcon(icon.getImage().getScaledInstance(10, 10,
		// Image.SCALE_SMOOTH));
		tbutton = new JToggleButton(icon);
		tbutton.setToolTipText("Show Linegraph (L)");
		tbutton.setMnemonic(KeyEvent.VK_L);
		tbutton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addLines();
				zoomingIn = false;
				zoomPort = null;
			}
		});
		tbutton.setEnabled(true);
		tbutton.setPreferredSize(new Dimension(10, 10));
		toolbar.add(tbutton);

		addHelp("This mode is used for visualizing the results of model analyses, both full model analysis and simulation of experiments. The primary view shows the level of each node at the currently viewed state in the result. Additionally, a bar graph showing this same data (alt-B), and a line graph showing the entirety of the trajectory can be displayed (alt-L).\n\nResolution: the resolution slider allows the user to change the timescale the result is being viewed it. A resolution of 1 will show every state transition, while a resolution of 4 will condense the result so that only every 4th state is shown. This can be particularly useful when looking at models with nodes of different timescales, or experiments that have particularly long paths to their final state. \n\nIf viewing a full model analysis there may be multiple cycles, each can be viewed by using the change cycle buttons on the toolbar. This will also display the percentage of all possible starting states which come to equilibrium at that particular cycle. This essentially represents the \"basin of attraction\" of each equlibrium point.",
				300, 300);

		// JMenu spacer = new JMenu();
		// spacer.setPreferredSize(new Dimension(5000, 10));
		// toolbar.add(spacer);

		JLabel modeName = new JLabel("Results Viewer");
		modeName.setHorizontalAlignment(JLabel.LEFT);
		modeName.setVerticalAlignment(JLabel.TOP);
		modeName.setFont(new Font("helvetica", Font.BOLD, 16));
		this.add(modeName);

		// Results resolution slider
		resolutionSlider = new JSlider(SwingConstants.VERTICAL, 1, 16, 1);
		resolutionSlider.setInverted(true);
		resolutionSlider.setSnapToTicks(false);
		resolutionSlider.setSize(new Dimension(95, 175));
		resolutionSlider.setForeground(Color.blue);
		resolutionSlider.setPaintLabels(true);
		resolutionSlider.setPaintTicks(true);
		resolutionSlider.setPaintTrack(true);
		resolutionSlider.setOpaque(false);
		resolutionSlider.setMajorTickSpacing(4);
		this.add(resolutionSlider, BorderLayout.WEST);
		resolutionSlider.addChangeListener(new ResolutionListener());
		resolutionSlider.setLocation(0, 150);
		updateSlider();

		JPanel spacer = new JPanel();
		spacer.setPreferredSize(new Dimension(100, 5000));
		spacer.setVisible(false);

		add(spacer, BorderLayout.WEST);
	}

	/**
	 * PRE: resolutionSlider is defined
	 * POST: the resolutionSlider value range is updated to reflect the
	 * currently viewed result
	 */
	private void updateSlider() {
		if (results != null) {
			if (results.get(cycleSelected).getTotalLength() <= 1) {
				resolutionSlider.setVisible(false);
				resolution = 1;
				if (staticGUI.get(RESOLUTION) != null) {
					staticGUI.get(RESOLUTION).setMessage("");
				}
			} else {
				resolutionSlider.setMaximum(results.get(cycleSelected)
						.getTotalLength());
				JLabel label;
				Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();

				label = new JLabel("Fine");
				label.setForeground(Color.blue);
				labels.put(new Integer(1), label);

				label = new JLabel("Course");
				label.setForeground(Color.blue);
				labels.put(new Integer(resolutionSlider.getMaximum()), label);
				resolution = 1;
				resolutionSlider.setValue(0);
				resolutionSlider.setLabelTable(labels);
				resolutionSlider.setVisible(true);
				if (staticGUI.get(RESOLUTION) != null) {
					staticGUI.get(RESOLUTION).setMessage(
							"Resolution: " + resolution);
				}
			}
		} else {
			resolutionSlider.setMaximum(16);
		}

	}

	/**
	 * initialize the trajectory and model name labels and other result data
	 * displayed
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

		// Transition Speed
		staticGUI.set(SPD, new GUIElement(new int[] { 10,
				20 + buttonHeight * 1, 70, 16 }, "Speed: " + tranSpeed,
				buttonColor, activeColor, commonFont, false));

		// Cycle
		staticGUI.set(CYC, new GUIElement(new int[] { 10,
				20 + buttonHeight * 3, 70, 16 }, "Cycle: 0/0", buttonColor,
				activeColor, commonFont, false));

		staticGUI.set(CYC_INFO, new GUIElement(new int[] { 10,
				20 + buttonHeight * 4, 70, 32 }, "", buttonColor, activeColor,
				commonFont, false));
		// State
		staticGUI.set(STATE, new GUIElement(new int[] { 10,
				20 + buttonHeight * 2, 70, 16 }, "State: 0/0", buttonColor,
				activeColor, commonFont, false));

		// In limit cycle message
		staticGUI.set(IN_CYCLE, new GUIElement(new int[] { 10,
				20 + buttonHeight * 3, 70, 16 }, "", Color.red, activeColor,
				commonFont, false));

		// resolution value
		staticGUI.set(RESOLUTION, new GUIElement(new int[] { 10,
				20 + buttonHeight * 6, 70, 16 }, "Resolution: " + resolution,
				buttonColor, activeColor, commonFont, false));

	}

	private class ResolutionListener implements ChangeListener {
		@Override
		public void stateChanged(ChangeEvent arg0) {
			if (!((JSlider) arg0.getSource()).getValueIsAdjusting()) {
				resolution = ((JSlider) arg0.getSource()).getValue();
				if (staticGUI.get(RESOLUTION) != null) {
					staticGUI.get(RESOLUTION).setMessage(
							"Resolution: " + resolution);
				}
				System.out.println(resolution);
				if (!paused) {
					pause();
				}
				setStateOnResolutionChange();
			}
		}
	}

	protected void update() {
		super.update();
		if (knockouts.length != model.getNodes().size()) {
			knockouts = new boolean[model.getNodes().size()];
			for (int i = 0; i < knockouts.length; i++) {
				model.getNodes().get(i).setKnockedOut(false);
			}
		} else {
			for (int i = 0; i < knockouts.length; i++) {
				model.getNodes().get(i).setKnockedOut(knockouts[i]);
			}
		}
		updateNames();
		for (int i = 0; i < model.getNodes().size(); i++) {
			model.getNodes().get(i).setScale(1);
			if (model.getNodes().get(i).isKnockedOut()) {
				model.getNodes().get(i).setScale(1);
			}
			model.getNodes().get(i).setSelected(false);
		}
		changeSpeed(0);
	}

	/**
	 * paint the model and update the bar nad line panels if they exist
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (!reading && !noFile) {
			g.setColor(Color.black);
			g.setFont(new Font("arial", Font.BOLD, 16));
			for (int i = 0; i < model.getNodes().size(); i++) {
				g.drawString(
						model.getNodes()
								.get(i)
								.getTermsUsed()
								.get(Double
										.valueOf(
												results.get(cycleSelected)
														.getResult()[displayedState][i])
										.intValue()).getWord(),
						(int) (model.getNodes().get(i).getPixPos().x - 5 - (model
								.getNodes().get(i).getScaledRad() / 1.25)),
						(int) (model.getNodes().get(i).getPixPos().y - 5 - (model
								.getNodes().get(i).getScaledRad() / 1.25)));
			}
			updateTime();
			if (barPanel != null) {
				barPanel.updateTime(displayedState, curTimeStep, prevTimeStep,
						dTime, timeStepSize,
						displayedState >= results.get(cycleSelected)
								.getPathLength());
			}
		}
	}

	/**
	 * PRE: time variables and results are defined POST: the previous and
	 * current steps are updated based on the time since the last update, then
	 * each node's scale is updated to reflect the change in time
	 */
	private void updateTime() {

		curTime = System.currentTimeMillis();
		if (!paused && results.get(cycleSelected).getTotalLength() > 1) {
			dTime = curTime - oldTime;
		} else {
			oldTime = curTime - dTime;
		}

		// change displayedState
		if (dTime >= timeStepSize / 2) {
			displayedState += resolution;
			if (displayedState > curTimeStep) {
				displayedState = curTimeStep;
			}
			staticGUI.get(STATE).setMessage(
					"State: " + (displayedState + 1) + "/"
							+ results.get(cycleSelected).getTotalLength());

		}
		// change timeStep
		if (dTime >= timeStepSize) {
			dTime = 0;
			oldTime = curTime;
			prevTimeStep = curTimeStep;

			if (curTimeStep + resolution >= results.get(cycleSelected)
					.getTotalLength()) {
				if (mode == MODE.FULL) {
					curTimeStep = 0;
				} else if (mode == MODE.TRAJ) {
					curTimeStep = (((results.get(cycleSelected).getPathLength() - 1) / resolution) + 1)
							* resolution;
					staticGUI.get(IN_CYCLE).setMessage("IN LIMIT CYCLE");
				}
			} else {
				curTimeStep += resolution;
			}
		}
		if (!paused) {
			updateScales();
		}
	}

	private void setStateOnResolutionChange() {
		curTimeStep = curTimeStep - (curTimeStep % resolution);
		prevTimeStep = curTimeStep - resolution;
		if (prevTimeStep < 0) {
			prevTimeStep = 0;
		}
		dTime = 0;
		displayedState = prevTimeStep;
		staticGUI.get(STATE).setMessage(
				"State: " + (displayedState + 1) + "/"
						+ results.get(cycleSelected).getTotalLength());
	}

	/**
	 * PRE: dTime, timeStepSize, curTimeStep, prevTimeStep, and results are all
	 * defined POST: the scales of the nodes are adjusted to reflect the current
	 * place in time between curTimeStep and prevTimeStep based on dTime. edges
	 * are also adjusted to take into account the changing size of nodes
	 */
	private void updateScales() {
		double valueDiff = 0;
		double scale = 0;

		// Change in real time since the last update, modified to create a
		// sinusoidal change in value
		double timing = Math
				.sin((Math.PI * (dTime) / (double) (2 * timeStepSize)));

		double curTimeValue, prevTimeValue;
		int curTimeScaleIndex, prevTimeScaleIndex;

		// Loop over each node to scale it properly
		for (int i = 0; i < model.getNodes().size(); i++) {

			// Ensure there are no out-of-range state errors
			curTimeScaleIndex = Double.valueOf(
					results.get(cycleSelected).getResult()[curTimeStep][i])
					.intValue();
			if (curTimeScaleIndex > model.getNodes().get(i).getNumStates()) {
				curTimeScaleIndex = model.getNodes().get(i).getNumStates() - 1;
			}
			prevTimeScaleIndex = Double.valueOf(
					results.get(cycleSelected).result[prevTimeStep][i])
					.intValue();
			if (prevTimeScaleIndex > model.getNodes().get(i).getNumStates()) {
				prevTimeScaleIndex = model.getNodes().get(i).getNumStates() - 1;
			}

			curTimeValue = Node.scaleRange[model.getNodes().get(i)
					.getNumStates()][curTimeScaleIndex];
			prevTimeValue = Node.scaleRange[model.getNodes().get(i)
					.getNumStates()][prevTimeScaleIndex];

			valueDiff = curTimeValue - prevTimeValue;

			scale = (prevTimeValue + (timing * valueDiff));

			model.getNodes().get(i).setScale(scale);
		}
	}

	/**
	 * PRE: filename may or may not be defined POST: a file is loaded, either
	 * based on filename or a user prompt, and its results are loaded into this
	 * ResultsPanel and displayed
	 * 
	 * @param filename
	 */
	protected void newFile(String filename) {
		boolean reopenBars = false, reopenLines = false;
		if (barPanel != null) {
			closeBars();
			reopenBars = true;
		}
		if (linePanel != null) {
			closeLines();
			reopenLines = true;
		}
		reading = true;

		File loadFile = null;
		if (filename == null
				|| !(filename.endsWith(".res") || filename.endsWith(".randres"))) {

			JFileChooser jfc;
			do {
				if (model.getModelName().equals("Unsaved")) {
					jfc = new JFileChooser(new File("Models/"));
				} else if (inExperiments) {
					jfc = new JFileChooser(new File("Models/"
							+ model.getModelName() + "/Experiments/"));
				} else {
					jfc = new JFileChooser(new File("Models/"
							+ model.getModelName()));
				}
				jfc.showOpenDialog(null);
			} while (jfc.getSelectedFile() != null
					&& !(jfc.getSelectedFile().getName().endsWith(".res") || jfc
							.getSelectedFile().getName().endsWith(".randres")));

			loadFile = jfc.getSelectedFile();
		} else {
			loadFile = new File(filename);
		}
		if (loadFile != null) {
			try {
				loadFile(loadFile);
				updateNames();
				if (reopenBars) {
					addBars();
				}
				if (reopenLines) {
					addLines();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

		updateSlider();
	}

	/**
	 * PRE: file is defined as proper results file POST: the contents of file
	 * are loaded as a new set of results, this results in the results array
	 * being defined, and the mode being properly set
	 * 
	 * @param file
	 * @throws FileNotFoundException
	 */
	private void loadFile(File file) throws FileNotFoundException {
		Scanner filescan = new Scanner(file);
		resultsFile = file;
		String newModelName = filescan.next();
		if (!model.getModelName().equals(newModelName)) {
			model.loadModel(newModelName);
		}

		runName = filescan.next();

		// Set mode
		String modeString = filescan.next();
		if (modeString.equalsIgnoreCase("TRAJECTORY")) {
			mode = MODE.TRAJ;
		} else if (modeString.equalsIgnoreCase("RANDOM")) {
			filescan.next();
			mode = MODE.RANDOM;
		} else {
			mode = MODE.FULL;
		}

		prevTimeStep = 0;
		curTimeStep = 0;
		displayedState = 0;
		results = new ArrayList<Result>();
		cycleSelected = 0;

		// Parse a trajectory
		if (mode == MODE.TRAJ) {
			results.add(new TrajResult(file, model.getCurModel()));
			updateTrajModeGUI();
			loadKnockouts();
			inExperiments = true;
		}
		// Parse random update trajectory
		else if (mode == MODE.RANDOM) {
			filescan.next();
			int runs = filescan.nextInt();
			filescan.next();
			int stepsPerRun = filescan.nextInt();
			filescan.findWithinHorizon("RUN:", 0);
			filescan.next();

			ArrayList<ArrayList<Integer>> pathToCombine = null;
			ArrayList<ArrayList<Integer>> pathToCombine2 = null;
			pathToCombine2 = (parseRandomPath(filescan, stepsPerRun));
			for (int i = 1; i < runs; i++) {
				filescan.nextLine();
				pathToCombine = parseRandomPath(filescan, stepsPerRun);
				for (int k = 0; k < stepsPerRun; k++) {
					for (int m = 0; m < pathToCombine.get(0).size(); m++) {
						pathToCombine2.get(k).set(
								m,
								pathToCombine2.get(k).get(m)
										+ pathToCombine.get(k).get(m));
					}
				}
			}
			double[][] combined = new double[stepsPerRun][model.getNodes()
					.size()];
			for (int k = 0; k < stepsPerRun; k++) {
				for (int m = 0; m < model.getNodes().size(); m++) {
					combined[k][m] = (((double) pathToCombine2.get(k).get(m)) / runs);
				}
			}
			results.add(new TrajResult(combined, model.getCurModel()));
			updateTrajModeGUI();
			loadKnockouts();
			inExperiments = true;
		}
		// Parse a full simulation
		else {
			// Skip state space value
			filescan.findWithinHorizon("CYCLES:", 0);
			int cycles = filescan.nextInt();
			for (int i = 0; i < cycles; i++) {
				results.add(new CycResult(file, i, model.getCurModel()));
			}
			inExperiments = false;
			knockouts = new boolean[model.getNodes().size()];
			updateFullModeGUI();
		}
		if (!paused) {
			pause();
		}

		filescan.findWithinHorizon("<NOTES>", 0);
		filescan.nextLine();
		String line = filescan.nextLine();
		notes = "";
		while (!line.endsWith("</NOTES>") && filescan.hasNextLine()) {
			notes += line + "\n";
			line = filescan.nextLine();
		}
		parent.updateNotesPane(notes);

		updateTime();
		updateScales();
		staticGUI.get(IN_CYCLE).setMessage("");
		reading = false;
		noFile = false;
	}

	protected void saveNotes() {
		BufferedWriter writer;
		try {
			Scanner origFileScan = new Scanner(resultsFile);
			String resultsContent = origFileScan.nextLine();
			while (origFileScan.hasNextLine()
					&& !resultsContent.endsWith("<NOTES>")) {
				resultsContent += "\n" + origFileScan.nextLine();
			}
			writer = new BufferedWriter(new FileWriter(resultsFile));
			notes = model.pane.textPanel.getText();
			resultsContent += "\n" + notes + "\n</NOTES>";
			writer.write(resultsContent);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: a .exp file is defined for this results file
	 * POST: the nodes designated as knockouts by a '-1' in the .exp file are
	 * set as knocked out
	 */
	private void loadKnockouts() {
		File trajFile = new File("Models/" + model.getModelName()
				+ "/Experiments/" + runName + ".exp");

		try {
			Scanner scan = new Scanner(trajFile);
			knockouts = new boolean[model.getNodes().size()];
			while (!scan.next().equals("[")) {
			}
			for (int i = 0; i < model.getNodes().size(); i++) {
				if (scan.nextInt() < 0) {
					model.getNodes().get(i).setKnockedOut(true);
					knockouts[i] = true;
				} else {
					model.getNodes().get(i).setKnockedOut(false);
					knockouts[i] = false;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * update the model and experiment name placements
	 */
	protected void updateNames() {
		staticGUI.get(MODEL_NAME).setMessage(model.getModelName());
		staticGUI.get(TRAJ_NAME).setMessage(runName);
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
	 * POST: removes the cycle-specific GUI elements and updates the state
	 * display based on the current result
	 */
	private void updateTrajModeGUI() {
		removeCycleGUI();
		staticGUI.get(STATE).setMessage(
				"State: " + (displayedState + 1) + "/"
						+ results.get(cycleSelected).getTotalLength());
	}

	/**
	 * PRE:
	 * POST: adds the cycle-specific GUI elements and updates the state display
	 * based on the urrent result
	 */
	private void updateFullModeGUI() {
		addCycleGUI();
		staticGUI.get(STATE).setMessage(
				"State: " + (displayedState + 1) + "/"
						+ results.get(cycleSelected).getTotalLength());

	}

	/**
	 * PRE: GUI elements are CYC, CYC_LABEL, CYC_INC, and CYC_DEC are defined
	 * POST: these elements now have blank messages
	 */
	private void removeCycleGUI() {
		staticGUI.get(CYC).setMessage("");
		staticGUI.get(CYC_INFO).setMessage("");
		((JMenuBar) toolbar).getComponent(CYCLE_INC).setEnabled(false);
		((JMenuBar) toolbar).getComponent(CYCLE_DEC).setEnabled(false);
	}

	/**
	 * PRE: the result is a simulation/cycle result
	 * POST: the cycle-specific GUI elements have been added
	 */
	private void addCycleGUI() {
		staticGUI.get(CYC).setMessage(
				"Cycle: " + (cycleSelected + 1) + "/" + results.size());
		staticGUI.get(CYC_INFO).setMessage(
				((CycResult) results.get(cycleSelected)).getComponentSize()
						+ "% of Start States \nend in this cycle");

		toolbar.getComponent(CYCLE_INC).setEnabled(true);
		toolbar.getComponent(CYCLE_DEC).setEnabled(true);
	}

	/**
	 * PRE: scan1 and size are defined.
	 * POST: the path for this result is parsed. This is used for parsing and
	 * loading the rando mresults, whereas normal trajecory and simulation
	 * results parsings are handled in the CycResult and TrajResult classes
	 * 
	 * @param scan
	 * @param size
	 * @return
	 */
	private ArrayList<ArrayList<Integer>> parseRandomPath(Scanner scan1,
			int size) {

		ArrayList<ArrayList<Integer>> path = new ArrayList<ArrayList<Integer>>(
				size);

		String line = scan1.nextLine();
		for (int i = 0; i < size; i++) {
			line = scan1.nextLine();
			Scanner scan = new Scanner(line);
			path.add(new ArrayList<Integer>(model.getNodes().size()));
			for (int k = 0; k < model.getNodes().size(); k++) {
				while (!scan.hasNextInt()) {
					scan.next();
				}
				path.get(i).add(scan.nextInt());
			}
		}

		return path;
	}

	/**
	 * PRE: tranSpeed and change are defined POST: tranSpeed has been adjusted
	 * by change to a max of 9 or a min of 0. The staticGUI Element SPD has been
	 * updated
	 * 
	 * @param change
	 */
	private void changeSpeed(int change) {
		tranSpeed += change;
		if (tranSpeed > 9) {
			tranSpeed = 9;
		} else if (tranSpeed < 1) {
			tranSpeed = 1;
		}
		timeStepSize = (int) ((double) 1 / tranSpeed * 5000);
		staticGUI.get(SPD).setMessage("Speed: " + tranSpeed);

	}

	/**
	 * PRE: cycleSelected and change are defined POST: cycleSelected is adjusted
	 * by change amount, wrapping around to 0 if it would be greater than the
	 * number of cycles, or to max if it would be less than 0. the staticGUI
	 * element CYC has been updated
	 * 
	 * @param change
	 */
	private void changeCycle(int change) {
		cycleSelected += change;
		if (cycleSelected >= results.size()) {
			cycleSelected = 0;
		} else if (cycleSelected < 0) {
			cycleSelected = results.size() - 1;
		}
		staticGUI.get(CYC).setMessage(
				"Cycle: " + (cycleSelected + 1) + "/" + results.size());
		staticGUI.get(CYC_INFO).setMessage(
				((CycResult) results.get(cycleSelected)).getComponentSize()
						+ "% of Start States \nend in this cycle");
		// Go to the first state
		changeState(results.get(cycleSelected).getTotalLength());
		if (barPanel != null) {
			closeBars();
			addBars();
		}
		if (linePanel != null) {
			closeLines();
			addLines();
		}
		updateSlider();
	}

	/**
	 * PRE: prevTimeStep, curTimeStep, dTime, oldTime, results, and change are
	 * defined. POST: prevTimeStep is adjusted by change amount, wrapping to max
	 * or min state if necessary. curTimeStep is adjusted to be 1 greater than
	 * prevTimeStep, wrapping if necessary. dTime = 0, oldTime = now, staticGUI
	 * element STATE has been updated to reflect prevTimeStep
	 * 
	 * @param change
	 */
	private void changeState(int change) {
		prevTimeStep += change;
		if (prevTimeStep >= results.get(cycleSelected).getTotalLength()) {
			prevTimeStep = 0;
			if (results.get(cycleSelected).getTotalLength() > 1) {
				curTimeStep = 1;
			} else {
				curTimeStep = 0;
			}
		} else if (prevTimeStep < 0) {
			prevTimeStep = results.get(cycleSelected).getTotalLength() - 1;
			curTimeStep = 0;
		} else {
			curTimeStep = prevTimeStep + 1;
		}
		if (curTimeStep >= results.get(cycleSelected).getTotalLength()) {
			curTimeStep = 0;
		}

		if (mode == MODE.TRAJ) {
			if (prevTimeStep >= results.get(cycleSelected).getPathLength()) {
				staticGUI.get(IN_CYCLE).setMessage("IN LIMIT CYCLE");
			} else {
				staticGUI.get(IN_CYCLE).setMessage("");
			}
		}

		dTime = 0;
		oldTime = System.currentTimeMillis();
		displayedState = prevTimeStep;

		staticGUI.get(STATE).setMessage(
				"State: " + (displayedState + 1) + "/"
						+ results.get(cycleSelected).getTotalLength());
		updateScales();

	}

	/**
	 * PRE: paused is defined
	 * POST: pause = !paused
	 */
	private void pause() {
		paused = !paused;
	}

	/**
	 * PRE: the barPanel is defined POST: barPanel is removed from the layer and
	 * then set to null
	 */
	protected void closeBars() {
		if (barPanel != null) {
			layer.remove(barPanel);
			barPanel = null;
			((JToggleButton) toolbar.getComponent(BARS)).setSelected(false);
		}
	}

	/**
	 * PRE: the barPanel is defined POST: barPanel is removed from the layer and
	 * then set to null
	 */
	protected void closeLines() {
		if (linePanel != null) {
			layer.remove(linePanel);
			linePanel = null;
			((JToggleButton) toolbar.getComponent(LINES)).setSelected(false);
		}
	}

	protected void resize() {
		super.resize();
		updateNames();
		if (barPanel != null) {
			Rectangle r = barPanel.getBounds();
			barPanel.setBounds(getSize().width - model.getNodes().size() * 30
					- 8, getSize().height - 250, (int) r.getWidth(),
					(int) r.getHeight());
		}
	}

	/**
	 * PRE: this ResultPanel is defined POST: the barPanel is defined and added
	 * to the layer
	 */
	protected void addBars() {
		if (!noFile && barPanel == null) {
			barPanel = new BarPanel(new Point(10, getSize().height - 230),
					this, results.get(cycleSelected));

			layer.add(barPanel, new Integer(3));
		} else if (barPanel != null) {
			closeBars();
		}
	}

	/**
	 * PRE: this ResultPanel is defined POST: the barPanel is defined and added
	 * to the layer
	 */
	protected void addLines() {
		if (!noFile && linePanel == null) {
			linePanel = new LinePanel(new Point(0, 100), this,
					results.get(cycleSelected));

			layer.add(linePanel, new Integer(3));
		} else if (linePanel != null) {
			closeLines();
		}
	}

	/**
	 * PRE: timeStepSize is defined POST: RV = timeStepSize
	 */
	public int getTimeStepSize() {
		return timeStepSize;
	}

	/**
	 * PRE: timeStepSize is defined POST: timeStepSize = timeStepSize
	 */
	public void setTimeStepSize(int timeStepSize) {
		this.timeStepSize = timeStepSize;
	}

	/**
	 * PRE: dTime is defined POST: RV = dTime
	 */
	public long getdTime() {
		return dTime;
	}

	/**
	 * PRE: dTime is defined POST: dTime = dTime
	 */
	public void setdTime(long dTime) {
		this.dTime = dTime;
	}

	/**
	 * PRE: curTimeStep is defined POST: RV = curTimeStep
	 */
	public int getCurTimeStep() {
		return curTimeStep;
	}

	/**
	 * PRE: curTimeStep is defined POST: curTimeStep = curTimeStep
	 */
	public void setCurTimeStep(int curTimeStep) {
		this.curTimeStep = curTimeStep;
	}

	/**
	 * PRE: prevTimeStep is defined POST: RV = prevTimeStep
	 */
	public int getPrevTimeStep() {
		return prevTimeStep;
	}

	/**
	 * PRE: prevTimeStep is defined POST: prevTimeStep = prevTimeStep
	 */
	public void setPrevTimeStep(int prevTimeStep) {
		this.prevTimeStep = prevTimeStep;
	}

	/**
	 * PRE: displayedState is defined POST: RV = displayedState
	 */
	public int getDisplayedState() {
		return displayedState;
	}

	/**
	 * PRE: displayedState is defined POST: displayedState = displayedState
	 */
	public void setDisplayedState(int displayedState) {
		this.displayedState = displayedState;
	}

	/**
	 * PRE: cycleSelected is defined POST: RV = cycleSelected
	 */
	public int getCycleSelected() {
		return cycleSelected;
	}

	/**
	 * PRE: cycleSelected is defined POST: cycleSelected = cycleSelected
	 */
	public void setCycleSelected(int cycleSelected) {
		this.cycleSelected = cycleSelected;
	}

	/**
	 * PRE: resolution is defined POST: RV = resolution
	 */
	public int getResolution() {
		return resolution;
	}

	/**
	 * PRE: resolution is defined POST: resolution = resolution
	 */
	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public MODE getMode() {
		return mode;
	}

	public void setMode(MODE mode) {
		this.mode = mode;
	}

	@Override
	protected boolean dragged(MouseEvent e) {
		boolean acted = super.dragged(e);
		if (!acted) {
			Point changeInRealCoords = port.frameToRealCoord(new Point(
					oldMouse.x - e.getPoint().x, oldMouse.y - e.getPoint().y));

			changeInRealCoords.x -= port.getPort().x;
			changeInRealCoords.y -= port.getPort().y;
			port.setPort(new Rectangle(port.getPort().x + changeInRealCoords.x,
					port.getPort().y + changeInRealCoords.y,
					port.getPort().width, port.getPort().height));
			model.resize();
			oldMouse = e.getPoint();
		}
		return acted;
	}
}
