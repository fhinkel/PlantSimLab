package bmv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JFileChooser;

/**
 * This class holds the data of a trajectory, primary its name and the start
 * levels of each node in the model
 * 
 * @author plvines
 * 
 */
public class Trajectory {

	protected String trajName;
	protected int[] startLevels;
	protected Model model;
	protected String notes;

	/**
	 * PRIMARY CONSTRUCTOR PRE: model is defined POST: this trajectory has been
	 * initialized to default values and a reference to model
	 * 
	 * @param model
	 */
	public Trajectory(Model model) {
		this.model = model;
		startLevels = new int[0];
		updateStartLevels();
		trajName = "Unsaved";
		notes = "";
	}

	/**
	 * PRE: orig is defined POST: this trajectory is a deep copy of orig.
	 * 
	 * @param orig
	 */
	public Trajectory(Trajectory orig) {
		this.model = orig.getModel();
		startLevels = orig.getStartLevels().clone();
		updateStartLevels();
		trajName = orig.getTrajName();
		notes = orig.notes;
	}

	/**
	 * PRE: this trajectory is defined POST: the information pertinent to this
	 * trajectory is saved. Specifically: the name of the model and the start
	 * levels are saved in a .exp file in the folder of experiments inside the
	 * folder of the model. if the model is unsaved then the user is prompted to
	 * save it first
	 */
	protected void saveTraj(boolean updateNotes) {
		if (model.getModelName().equals("Unsaved")) {
			model.saveModel();
		}
		File saveFile;
		if (trajName.equals("Unsaved")) {
			JFileChooser jfc;
			jfc = new JFileChooser(new File("Models/" + model.getModelName()
					+ "/Experiments/"));
			jfc.setDialogTitle("Save trajectory as...");
			jfc.showSaveDialog(null);
			saveFile = jfc.getSelectedFile();
			if (!saveFile.canRead()) {
				saveFile = new File("Models/" + model.getModelName()
						+ "/Experiments/" + saveFile.getName() + ".exp");
				trajName = saveFile.getName().substring(0,
						saveFile.getName().indexOf('.'));
			}
		} else {
			saveFile = new File("Models/" + model.getModelName()
					+ "/Experiments/" + trajName + ".exp");
		}
		try {
			writeTraj(saveFile, updateNotes);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: saveFile is defined POST: the modelName followed by the trajectory
	 * starting levels notated as [ 1 0 2 ... 1 ] has been written to saveFile
	 * 
	 * @param saveFile
	 * @throws IOException
	 */
	private void writeTraj(File saveFile, boolean updateNotes)
			throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile));

		String output = model.getModelName() + "\n" + trajName + "\n";

		output += "[";
		for (int i = 0; i < model.getNodes().size(); i++) {
			output += " " + startLevels[i];
		}
		output += " ]\n";

		if (updateNotes) {
			notes = model.pane.textPanel.getText();
		}
		output += "<NOTES>\n" + notes + "</NOTES>";

		writer.write(output);
		writer.close();
	}

	public void knockout(int nodeIndex) {
		model.getNodes().get(nodeIndex).setKnockedOut(true);
		startLevels[nodeIndex] = -1;
	}

	public void unknockout(int nodeIndex) {
		model.getNodes().get(nodeIndex).setKnockedOut(false);
		startLevels[nodeIndex] = 0;
	}

	/**
	 * PRE: POST: this trajectory is now set based on a file selected by the
	 * user. If that trajectory used a different model than the current one, it
	 * has been loaded
	 */
	protected void loadTraj(boolean multiModel) {
		JFileChooser jfc;
		if (!model.getModelName().equals("Unsaved")) {
			jfc = new JFileChooser(new File("Models/" + model.getModelName()
					+ "/Experiments/"));
		} else {
			jfc = new JFileChooser(new File("Models/"));
		}
		jfc.setDialogTitle("Select a model file");
		jfc.showOpenDialog(null);
		File loadFile = jfc.getSelectedFile();

		try {
			readTraj(loadFile, multiModel);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: POST: this trajectory is now set based on a file selected by the
	 * user. If that trajectory used a different model than the current one, it
	 * has been loaded
	 */
	protected void loadTraj(File newFile) {
		try {
			readTraj(newFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * PRE: orig is defined POST: this trajectory is now a deep copy of orig
	 * 
	 * @param orig
	 */
	protected void loadTraj(Trajectory orig) {
		this.trajName = orig.getTrajName();
		this.model = orig.getModel();
		this.startLevels = orig.getStartLevels().clone();
		this.notes = "" + orig.notes;
	}

	/**
	 * PRE: loadFile is defined POST: the trajectory contained in loadFile has
	 * been loaded, if the model was different than the current model and
	 * multiMode=false then the different model has been loaded; if
	 * multiMode=true then the trajectory has not been loaded
	 * 
	 * @param loadFile
	 * @throws FileNotFoundException
	 */
	private void readTraj(File loadFile, boolean multiModel)
			throws FileNotFoundException {
		Scanner filescan = new Scanner(loadFile);

		String newModelName = filescan.next();
		boolean modelMismatch = true;

		modelMismatch = !newModelName.equals(model.getModelName());

		if (modelMismatch) {
			model.loadModel(newModelName);
		}
		trajName = filescan.next();
		filescan.next();
		startLevels = new int[model.getNodes().size()];
		int i = 0;
		for (; filescan.hasNextInt() && i < startLevels.length; i++) {
			startLevels[i] = filescan.nextInt();
			if (startLevels[i] >= 0) {
				model.getNodes().get(i).setScaleIndex(startLevels[i]);
				model.getNodes().get(i).setKnockedOut(false);
			} else {
				knockout(i);
			}
		}
		// In case the model had nodes added, let the trajectory load and
		// fill extra nodes in as level 0
		for (; i < startLevels.length; i++) {
			startLevels[i] = 0;
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
		model.pane.updateNotesPane(notes);

	}

	public void updateStartLevels() {
		if (model.getNodes().size() != startLevels.length) {
			startLevels = new int[model.getNodes().size()];
			for (int i = 0; i < startLevels.length; i++) {
				startLevels[i] = 1;
			}
		}
	}

	/**
	 * PRE: trajName is defined POST: RV = trajName
	 */
	public String getTrajName() {
		return trajName;
	}

	/**
	 * PRE: trajName is defined POST: trajName = trajName
	 */
	public void setTrajName(String trajName) {
		this.trajName = trajName;
	}

	/**
	 * PRE: startLevels is defined POST: RV = startLevels
	 */
	public int[] getStartLevels() {
		return startLevels;
	}

	/**
	 * PRE: startLevels is defined POST: startLevels = startLevels
	 */
	public void setStartLevels(int[] startLevels) {
		this.startLevels = startLevels;
	}

	/**
	 * PRE: model is defined POST: RV = model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * PRE: model is defined POST: model = model
	 */
	public void setModel(Model model) {
		this.model = model;
		updateStartLevels();
	}

}
