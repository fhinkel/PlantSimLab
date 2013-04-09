package bmv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import javax.swing.BorderFactory;

/**
 * This class displays a BarGraph of the Result it is passed. It should be
 * updated using the UpdateTime method to change the state displayed.
 * 
 * @author plvines
 * 
 */
public class BarPanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 815622356528563201L;
	private BMVPanel context;
	private Result result;
	private Point location;
	private Dimension graphDim;
	private int curTimeStep, prevTimeStep, displayedState;
	private long dTime, timeStepSize;
	private boolean inLimitCycle;
	private Font[] nameFont;

	/**
	 * PRE: orig is defined
	 * POST: this is a shallow copy of orig
	 * 
	 * @param orig
	 */
	public BarPanel(BarPanel orig) {
		super();
		context = orig.getContext();
		result = orig.getResult();
		location = new Point(orig.getLocation().x, orig.getLocation().y);
		graphDim = new Dimension(orig.getGraphDim().width,
				orig.getGraphDim().height);
		curTimeStep = orig.getCurTimeStep();
		prevTimeStep = orig.getPrevTimeStep();
		displayedState = orig.getDisplayedState();
		dTime = orig.getdTime();
		timeStepSize = orig.getTimeStepSize();
		inLimitCycle = orig.isInLimitCycle();
		setBounds(orig.getBounds().x, orig.getBounds().y,
				orig.getBounds().width, orig.getBounds().height);
		initializeNames();
	}

	/**
	 * PRIMARY CONSTRUCTOR
	 * PRE: pLocation, pContext, and pResults are defined
	 * POST: this has been initialized to the location, panel, and result,
	 * 
	 * @param pLocation
	 * @param pContext
	 * @param pResults
	 */
	public BarPanel(Point pLocation, BMVPanel pContext, Result pResults) {
		super();
		context = pContext;
		location = pLocation;

		graphDim = new Dimension(Math.min(context.getSize().width
				- context.parent.mainPane.getDividerLocation() - 50, context
				.getModel().getNodes().size() * 30), 200);
		result = pResults;

		setBounds(location.x, location.y - 4, graphDim.width + 6,
				graphDim.height + 20 + 4);
		inLimitCycle = false;
		initialize();
	}

	/**
	 * PRE:
	 * POST: initialize class variables to default values
	 */
	private void initialize() {
		curTimeStep = 0;
		prevTimeStep = 0;
		dTime = 0;
		timeStepSize = 1;
		inLimitCycle = false;
		initializeNames();

		addHelp("This panel shows the values of each node in the current state. Scales are normalized so two nodes with different numbers of states show the same-scale bar graphs.\n\nIf the current state is a part of a limit cycle the border of this panel is color red",
				200, 165);
	}

	/**
	 * PRE:
	 * POST: initialize the font for the names based on the size of the panel
	 */
	private void initializeNames() {
		nameFont = new Font[context.getModel().getNodes().size()];
		for (int i = 0; i < context.getModel().getNodes().size(); i++) {
			nameFont[i] = new Font("helvetica", Font.PLAIN,
					(int) ((2 * (graphDim.width / context.getModel().getNodes()
							.size()) - 6) / (context.getModel().getNodes()
							.get(i).getAbrevName().length() + 2)));
		}
	}

	/**
	 * extension of MoveablePanel.resize: resizes the graph bars and names
	 */
	public void resize(Point dragPoint) {
		super.resize(dragPoint);
		graphDim.width = getBounds().width;
		graphDim.height = getBounds().height - 24;
		initializeNames();
	}

	/**
	 * Paints the bar graphs according to the current state and previous state,
	 * and the time in between them, for each node
	 */
	public void paint(Graphics g) {
		super.paint(g);

		double valueDiff = 0;
		double scale = 0;

		// Change in real time since the last update, modified to create a
		// sinusoidal change in value
		double timing = Math
				.sin((Math.PI * (dTime) / (double) (2 * timeStepSize)));

		double curTimeValue, prevTimeValue;
		double curTimeScaleIndex, prevTimeScaleIndex;
		g.setFont(new Font("arial", Font.BOLD, 12));

		// for each node in the model
		for (int i = 0; i < context.getModel().getNodes().size(); i++) {

			// Set the color based on the node
			g.setColor(context.getModel().getColors()
					.get(context.getModel().getNodes().get(i).getColorChoice()));

			curTimeScaleIndex = result.getResult()[curTimeStep][i];
			if (curTimeScaleIndex > context.getModel().getNodes().get(i)
					.getNumStates()) {
				curTimeScaleIndex = context.getModel().getNodes().get(i)
						.getNumStates() - 1;
			}
			prevTimeScaleIndex = result.getResult()[prevTimeStep][i];
			if (prevTimeScaleIndex > context.getModel().getNodes().get(i)
					.getNumStates()) {
				prevTimeScaleIndex = context.getModel().getNodes().get(i)
						.getNumStates() - 1;
			}

			// get the scale value of the current timestep
			curTimeValue = Node.scaleRange[context.getModel().getNodes().get(i)
					.getNumStates()][(int) curTimeScaleIndex]
					+ ((curTimeScaleIndex % 1) * (Node.scaleRange[context
							.getModel().getNodes().get(i).getNumStates()][Math
							.min((int) curTimeScaleIndex + 1,
									Node.scaleRange[0].length)] - Node.scaleRange[context
							.getModel().getNodes().get(i).getNumStates()][(int) curTimeScaleIndex]));

			// get the scale value of the next timestep
			prevTimeValue = Node.scaleRange[context.getModel().getNodes()
					.get(i).getNumStates()][(int) prevTimeScaleIndex]
					+ ((prevTimeScaleIndex % 1) * (Node.scaleRange[context
							.getModel().getNodes().get(i).getNumStates()][Math
							.min((int) prevTimeScaleIndex + 1,
									Node.scaleRange[0].length)] - Node.scaleRange[context
							.getModel().getNodes().get(i).getNumStates()][(int) prevTimeScaleIndex]));

			// get the difference
			valueDiff = curTimeValue - prevTimeValue;

			scale = (prevTimeValue + (timing * valueDiff));
			g.fillRect(
					3 + (graphDim.width / context.getModel().getNodes().size())
							* i,
					(int) (graphDim.height - (scale * graphDim.height / 2.5)),
					(graphDim.width / context.getModel().getNodes().size()) - 6,
					(int) (scale * graphDim.height / 2.5) - 3);

			// draw the node name
			g.setColor(Color.black);
			g.setFont(nameFont[i]);
			g.drawString(context.getModel().getNodes().get(i).getAbrevName(), 3
					+ (graphDim.width / context.getModel().getNodes().size())
					* i, graphDim.height + 12);
			g.setFont(Font.decode("helvetica"));

			// Draw the state term/percentage
			if (((ResultsPanel) context).getMode() != ResultsPanel.MODE.RANDOM) {
				g.drawString(
						""
								+ Double.valueOf(
										result.getResult()[displayedState][i])
										.intValue(), 3
								+ (graphDim.width / context.getModel()
										.getNodes().size()) * i,
						graphDim.height - (int) (scale * graphDim.height / 2.5)
								- 2);
			} else {
				int statePercentage = (int) (result.getResult()[displayedState][i] + 1);
				if (statePercentage >= context.getModel().getNodes().get(i)
						.getNumStates()) {
					statePercentage -= 1;
				}
				g.drawString(
						""
								+ ((int) ((result.getResult()[displayedState][i] * 100)) % 101)
								+ "% "
								+ context.getModel().getNodes().get(i)
										.getTermsUsed().get(statePercentage), 3
								+ (graphDim.width / context.getModel()
										.getNodes().size()) * i,
						graphDim.height - (int) (scale * graphDim.height / 2.5)
								- 2);
			}
		}
	}

	/**
	 * PRE: displayedState, curTimeStep, prevTimeStep, dTime, timeStepSize, and
	 * inLimitCycle are all defined, displayedState, curTimeStep, and
	 * prevTimeStep are all < |Result|, dTime < timeStepSize
	 * POST: this BarPanel's variables have been set to the parameters so that
	 * painting will be updated
	 * 
	 * @param displayedState
	 * @param curTimeStep
	 * @param prevTimeStep
	 * @param dTime
	 * @param timeStepSize
	 * @param inLimitCycle
	 */
	public void updateTime(int displayedState, int curTimeStep,
			int prevTimeStep, long dTime, long timeStepSize,
			boolean inLimitCycle) {
		this.curTimeStep = curTimeStep;
		this.prevTimeStep = prevTimeStep;
		this.dTime = dTime;
		this.timeStepSize = timeStepSize;
		this.displayedState = displayedState;
		if (!this.inLimitCycle && inLimitCycle) {
			setBorder(BorderFactory.createLineBorder(Color.red, 2));
		} else if (!inLimitCycle && this.inLimitCycle) {
			setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR, 2));
		}
		this.inLimitCycle = inLimitCycle;

	}

	/**
	 * PRE: location is defined POST: RV = location
	 */
	public Point getLocation() {
		return location;
	}

	/**
	 * PRE: location is defined POST: location = location
	 */
	public void setLocation(Point location) {
		this.location = location;
		setBounds(location.x, location.y, getBounds().width, getBounds().height);
	}

	/**
	 * PRE: context is defined POST: RV = context
	 */
	public BMVPanel getContext() {
		return context;
	}

	/**
	 * PRE: context is defined POST: context = context
	 */
	public void setContext(BMVPanel context) {
		this.context = context;
	}

	/**
	 * PRE: result is defined POST: RV = result
	 */
	public Result getResult() {
		return result;
	}

	/**
	 * PRE: result is defined POST: result = result
	 */
	public void setResult(Result result) {
		this.result = result;
	}

	/**
	 * PRE: graphDim is defined POST: RV = graphDim
	 */
	public Dimension getGraphDim() {
		return graphDim;
	}

	/**
	 * PRE: graphDim is defined POST: graphDim = graphDim
	 */
	public void setGraphDim(Dimension graphDim) {
		this.graphDim = graphDim;
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
	 * PRE: timeStepSize is defined POST: RV = timeStepSize
	 */
	public long getTimeStepSize() {
		return timeStepSize;
	}

	/**
	 * PRE: timeStepSize is defined POST: timeStepSize = timeStepSize
	 */
	public void setTimeStepSize(long timeStepSize) {
		this.timeStepSize = timeStepSize;
	}

	/**
	 * PRE: inLimitCycle is defined POST: RV = inLimitCycle
	 */
	public boolean isInLimitCycle() {
		return inLimitCycle;
	}

	/**
	 * PRE: inLimitCycle is defined POST: inLimitCycle = inLimitCycle
	 */
	public void setInLimitCycle(boolean inLimitCycle) {
		this.inLimitCycle = inLimitCycle;
	}

}
