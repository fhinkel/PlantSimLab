package bmv;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;

import javax.swing.JTextArea;

/**
 * This class functions similarly to a process queue; the model holder almost
 * appears as the same object as the model being held in curModel (many get and
 * set methods are reimplemented within this class). However, the model pointed
 * to by this class can be changed easily by calling loadModel. The modelHolder
 * keeps these loaded models alive as objects in its list, so that they do not
 * have to be re-read from disc if a user wishes to switch between models
 * frequently.
 * The list automatically stores models in least-recent to most-recently used
 * order, and if a new model is loaded that would put it over the max size, the
 * oldest model object is removed. All model objects are still saved to the hard
 * drive when a new model is loaded, they just don't have to be re-read.
 * 
 * @author plvines
 * 
 */
public class ModelHolder {

	ArrayList<Model> modelList;

	Model curModel;

	JTextArea notePane;

	BMVManager pane;

	private static final int MAX_SIZE = 10;

	/**
	 * PRE: startModel, notePane, and pane are defined
	 * POST: this modelHolder object is defined and initialized with startModel
	 * as curModel
	 * 
	 * @param startModel
	 * @param notePane
	 * @param pane
	 */
	public ModelHolder(Model startModel, JTextArea notePane, BMVManager pane) {
		modelList = new ArrayList<Model>();
		modelList.add(startModel);
		curModel = startModel;
		this.notePane = notePane;
		this.pane = pane;
	}

	/**
	 * PRE: pane.curMode and curModel are defined
	 * POST: if the pane being used was edit (so notes would correspond to this
	 * model file) then notes are copied from notePane; either way, the current
	 * model is written to the hard drive
	 */
	public void saveModel() {
		if (pane.curMode == BMVManager.MODE.EDIT) {
			curModel.setNotes(notePane.getText());
		}
		if (!curModel.getModelName().equalsIgnoreCase("unsaved")) {
			curModel.saveModel();
		}
	}

	/**
	 * PRE: name is defined
	 * POST: the current model has been saved. If a model with name = name
	 * exists in the modelList, it has been set as the current model. otherwise,
	 * the desired model has been read from the hard drive. The modellist has
	 * been shifted so that the previous curModel is now at size -2 and the
	 * curModel is at size -1
	 * 
	 * @param name
	 */
	public void loadModel(String name) {
		saveModel();

		boolean found = false;
		if (name != null) {

			// look in modelList
			for (int i = 0; !found && i < modelList.size(); i++) {
				if (name.equals(modelList.get(i).getModelName())) {
					curModel = modelList.get(i);

					// move it to the end of the list
					modelList.remove(i);
					modelList.add(curModel);
					found = true;
				}
			}
		}

		// load from hard drive
		if (!found) {
			modelList.add(new Model(curModel.getPane(), curModel.getPort(),
					name));
			curModel = modelList.get(modelList.size() - 1);
			if (modelList.size() > MAX_SIZE) {
				modelList.remove(1);
			}
		}

		notePane.setText(curModel.getNotes());
	}

	/**
	 * PRE: modelName is defined
	 * POST: if a model with modelName was in modelList, it has been removed
	 * from modelList. If it was the current model, the curModel has been
	 * changed to size -2
	 * 
	 * @param modelName
	 */
	public void removeModelByName(String modelName) {
		for (int i = 0; i < modelList.size(); i++) {
			if (modelName.equals(modelList.get(i).getModelName())) {
				if (curModel == modelList.get(i)) {
					curModel = modelList.get(modelList.size() - 2);
					pane.curPanel.update();
				}
				modelList.remove(i);
			}
		}
	}

	/**
	 * PRE: modelName is defined
	 * POST: RV = modelName
	 */
	public String getModelName() {
		return curModel.getModelName();
	}

	/**
	 * PRE: modelName is defined
	 * POST: modelName = modelName
	 */
	public void setModelName(String modelName) {
		curModel.setModelName(modelName);
	}

	/**
	 * PRE: nodes is defined
	 * POST: RV = nodes
	 */
	public ArrayList<Node> getNodes() {
		return curModel.getNodes();
	}

	/**
	 * PRE: edges is defined
	 * POST: RV = edges
	 */
	public ArrayList<Edge> getEdges() {
		return curModel.getEdges();
	}

	/**
	 * PRE: maxStates is defined
	 * POST: RV = maxStates
	 */
	public int getMaxStates() {
		return curModel.getMaxStates();
	}

	/**
	 * PRE: maxStates is defined
	 * POST: maxStates = maxStates
	 */
	public void setMaxStates(int maxStates) {
		curModel.setMaxStates(maxStates);
	}

	/**
	 * PRE: changesMade is defined
	 * POST: RV = changesMade
	 */
	public boolean isChangesMade() {
		return curModel.isChangesMade();
	}

	/**
	 * PRE: changesMade is defined
	 * POST: changesMade = changesMade
	 */
	public void setChangesMade(boolean changesMade) {
		curModel.setChangesMade(changesMade);
	}

	/**
	 * PRE: continuous is defined
	 * POST: RV = continuous
	 */
	public boolean isContinuous() {
		return curModel.isContinuous();
	}

	/**
	 * PRE: continuous is defined
	 * POST: continuous = continuous
	 */
	public void setContinuous(boolean continuous) {
		curModel.setContinuous(continuous);
	}

	/**
	 * PRE: colors is defined
	 * POST: RV = colors
	 */
	public ArrayList<Color> getColors() {
		return curModel.getColors();
	}

	/**
	 * PRE: colors is defined
	 * POST: colors = colors
	 */
	public void setColors(ArrayList<Color> colors) {
		curModel.setColors(colors);
	}

	/**
	 * PRE: vocab is defined
	 * POST: RV = vocab
	 */
	public StateVocabulary getVocab() {
		return curModel.getVocab();
	}

	/**
	 * PRE: vocab is defined
	 * POST: vocab = vocab
	 */
	public void setVocab(StateVocabulary vocab) {
		curModel.setVocab(vocab);
	}

	/**
	 * PRE: totalSize is defined
	 * POST: RV = totalSize
	 */
	public Dimension getTotalSize() {
		return curModel.getTotalSize();
	}

	/**
	 * PRE: port is defined
	 * POST: RV = port
	 */
	public Viewport getPort() {
		return curModel.getPort();
	}

	/**
	 * PRE: port is defined
	 * POST: port = port
	 */
	public void setPort(Viewport port) {
		curModel.setPort(port);
	}

	/**
	 * PRE: drawings is defined
	 * POST: RV = drawings
	 */
	public ArrayList<Drawing> getDrawings() {
		return curModel.getDrawings();
	}

	/**
	 * PRE: drawings is defined
	 * POST: drawings = drawings
	 */
	public void setDrawings(ArrayList<Drawing> drawings) {
		curModel.setDrawings(drawings);
	}

	/**
	 * PRE: totalSize is defined
	 * POST: totalSize = totalSize
	 */
	public void setTotalSize(Dimension totalSize) {
		curModel.setTotalSize(totalSize);
	}

	public String toString() {
		return curModel.toString();
	}

	/**
	 * PRE: drawEdgeNames is defined
	 * POST: RV = drawEdgeNames
	 */
	public boolean isDrawEdgeNames() {
		return curModel.isDrawEdgeNames();
	}

	/**
	 * PRE: notes is defined
	 * POST: RV = notes
	 */
	public String getNotes() {
		return curModel.getNotes();
	}

	/**
	 * PRE: notes is defined
	 * POST: notes = notes
	 */
	public void setNotes(String notes) {
		curModel.setNotes(notes);
	}

	/**
	 * PRE: pane is defined
	 * POST: RV = pane
	 */
	public BMVManager getPane() {
		return curModel.getPane();
	}

	/**
	 * PRE: pane is defined
	 * POST: pane = pane
	 */
	public void setPane(BMVManager pane) {
		curModel.setPane(pane);
	}

	/**
	 * PRE: drawEdgeNames is defined
	 * POST: drawEdgeNames = drawEdgeNames
	 */
	public void setDrawEdgeNames(boolean drawEdgeNames) {
		curModel.setDrawEdgeNames(drawEdgeNames);
	}

	public void resize() {
		curModel.resize();
	}

	/**
	 * PRE: curModel is defined
	 * POST: RV = curModel
	 */
	public Model getCurModel() {
		return curModel;
	}
}
