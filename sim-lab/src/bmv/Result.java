package bmv;

/**
 * Class to hold the data of a result, including the 2D array of the
 * trajectory/cycle, the name of the model the results was from, and the length
 * of the path to the cycle, the cycle length, and the total length
 * 
 * Subclassed to CycResult and TrajResult
 * 
 * @author plvines
 * 
 */
public abstract class Result {

	protected double[][] result;
	protected String modelName;
	protected int pathLength, cycleLength, totalLength;
	protected Model model;

	/**
	 * PRE: result is defined
	 * POST: RV = result
	 */
	public double[][] getResult() {
		return result;
	}

	/**
	 * PRE: result is defined
	 * POST: result = result
	 */
	public void setResult(double[][] result) {
		this.result = result;
	}

	/**
	 * PRE: modelName is defined
	 * POST: RV = modelName
	 */
	public String getModelName() {
		return modelName;
	}

	/**
	 * PRE: modelName is defined
	 * POST: modelName = modelName
	 */
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}

	/**
	 * PRE: pathLength is defined
	 * POST: RV = pathLength
	 */
	public int getPathLength() {
		return pathLength;
	}

	/**
	 * PRE: pathLength is defined
	 * POST: pathLength = pathLength
	 */
	public void setPathLength(int pathLength) {
		this.pathLength = pathLength;
	}

	/**
	 * PRE: cycleLength is defined
	 * POST: RV = cycleLength
	 */
	public int getCycleLength() {
		return cycleLength;
	}

	/**
	 * PRE: cycleLength is defined
	 * POST: cycleLength = cycleLength
	 */
	public void setCycleLength(int cycleLength) {
		this.cycleLength = cycleLength;
	}

	/**
	 * PRE: totalLength is defined
	 * POST: RV = totalLength
	 */
	public int getTotalLength() {
		return totalLength;
	}

	/**
	 * PRE: totalLength is defined
	 * POST: totalLength = totalLength
	 */
	public void setTotalLength(int totalLength) {
		this.totalLength = totalLength;
	}

	/**
	 * PRE: model is defined
	 * POST: RV = model
	 */
	public Model getModel() {
		return model;
	}

	/**
	 * PRE: model is defined
	 * POST: model = model
	 */
	public void setModel(Model model) {
		this.model = model;
	}

}
