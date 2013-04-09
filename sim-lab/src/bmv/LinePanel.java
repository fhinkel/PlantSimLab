package bmv;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

/**
 * This class displays a result as a linegraph
 * 
 * @author plvines
 * 
 */
public class LinePanel extends MoveablePanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2445543321847837058L;
	private BMVPanel context;
	private Point location;
	private Dimension graphDim;
	private Result result;
	private double speedDiff;
	private static final int EXTRA_CYCLES_SHOWN = 3;
	private static final int STEP_SPACING = 40; // pixels
	private JToolBar toolbar;
	private boolean[] drawLine;

	/**
	 * PRE: orig is defined
	 * POST: this is a shallow copy of orig
	 * 
	 * @param orig
	 */
	public LinePanel(LinePanel orig) {
		context = orig.getContext();
		result = orig.getResult();
		location = new Point(orig.getLocation().x, orig.getLocation().y);
		graphDim = new Dimension(orig.getGraphDim().width,
				orig.getGraphDim().height);
		setBounds(orig.getBounds().x, orig.getBounds().y,
				orig.getBounds().width, orig.getBounds().height);
		initializeSpeedDiff();
	}

	/**
	 * PRIMARY CONSTRUCTOR
	 * PRE: pLocation, pContext, and pResults are defined
	 * POST: this LinePanel has been initialized based on pResult
	 * 
	 * @param pLocation
	 * @param pContext
	 * @param pResults
	 */
	public LinePanel(Point pLocation, BMVPanel pContext, Result pResults) {
		context = pContext;
		location = pLocation;

		result = pResults;

		setBounds(
				location.x,
				location.y,
				Math.min(
						context.getSize().width
								- context.parent.mainPane.getDividerLocation()
								- 50, result.getTotalLength() * 50), 200);
		graphDim = new Dimension(getBounds().width - 30, getBounds().height
				- (8 * result.getModel().getNodes().size()));
		initialize();
	}

	/**
	 * Class to toggle on and off the different node's line chartings
	 * 
	 * @author plvines
	 * 
	 */
	private class LineButton extends JToggleButton {

		/**
		 * 
		 */
		private static final long serialVersionUID = 2328268050662750502L;
		int index;

		public LineButton(Node node, int indexOfNode) {
			super("   ");
			index = indexOfNode;
			setBorder(BorderFactory.createLineBorder(Color.white));
			setBackground(result.getModel().getColors()
					.get(node.getColorChoice()));
			setToolTipText(node.getAbrevName());
			addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					toggleLine(index);
				}
			});
		}

	}

	/**
	 * PRE: result is defined
	 * POST: initialize all class variables based on the result and its model
	 */
	private void initialize() {
		layout = new BorderLayout();
		toolbar = new JToolBar();
		toolbar.setBackground(Color.white);
		toolbar.setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR));
		int count = 0;
		for (Node iterNode : result.getModel().getNodes()) {
			toolbar.add(new LineButton(iterNode, count));
			count++;
		}

		add(toolbar);
		toolbar.setFloatable(false);
		drawLine = new boolean[result.getModel().getNodes().size()];
		for (int i = 0; i < drawLine.length; i++) {
			drawLine[i] = true;
		}
		initializeSpeedDiff();
		addHelp("This is a line-graph showing the total time-course of the result. The start of the limit cycle is designated by the dashed red line. The limit cycle is repeated multiple times after this to show its cyclic nature more clearly.\n\nLines for specific nodes can be hidden by clicking the toggle-boxes at the top of this panel.",
				200, 190);
	}

	/**
	 * PRE: result is defined
	 * POST: initializes the speed differential between nodes to properly
	 * display state labels
	 */
	private void initializeSpeedDiff() {
		ArrayList<Node> nodes = result.getModel().getNodes();
		int fastestSpeed = 0;
		int slowestSpeed = Integer.MAX_VALUE;
		for (int i = 0; i < nodes.size(); i++) {
			if (nodes.get(i).getTable().getSpeed() > fastestSpeed) {
				fastestSpeed = nodes.get(i).getTable().getSpeed();
			}
			if (nodes.get(i).getTable().getSpeed() < slowestSpeed) {
				slowestSpeed = nodes.get(i).getTable().getSpeed();
			}
		}
		speedDiff = (double) fastestSpeed / slowestSpeed;
	}

	/**
	 * resizes the line graph along with the panel
	 */
	public void resize(Point dragPoint) {
		super.resize(dragPoint);
		graphDim = new Dimension(getBounds().width - 30, getBounds().height
				- (8 * result.getModel().getNodes().size()));
	}

	/**
	 * PRE: index is defined, index < |drawLine|
	 * POST: toggles on or off index
	 * 
	 * @param index
	 */
	private void toggleLine(int index) {
		drawLine[index] = !drawLine[index];
	}

	/**
	 * paints the graph
	 */
	public void paint(Graphics g) {

		super.paint(g);
		int pixPerTimeStep;

		// determines the spacing in pixels based on the number of states to
		// show and the width of the panel
		if (result.getTotalLength() > 1) {
			pixPerTimeStep = Math.max(
					1,
					(graphDim.width)
							/ (result.getTotalLength()
									+ (EXTRA_CYCLES_SHOWN * result
											.getCycleLength()) + 1));
		} else {
			pixPerTimeStep = graphDim.width;
		}
		Graphics2D g2 = (Graphics2D) g;
		Stroke origStroke = g2.getStroke();
		BasicStroke b1 = new BasicStroke(3);
		g2.setStroke(b1);

		int xStart = 13;

		paintPath(g, result, xStart, pixPerTimeStep);

		// draw the state markings
		g.setColor(Color.black);
		int markIncrement = Math.max(1, (STEP_SPACING / pixPerTimeStep));
		for (int i = 0; i < (result.getTotalLength() + (EXTRA_CYCLES_SHOWN * result
				.getCycleLength())) / speedDiff + 1; i += markIncrement) {
			g.drawString("" + (i + 1), (int) ((i * speedDiff) * pixPerTimeStep)
					+ xStart, getBounds().height - 3);
		}

		// draw the limit cycle indicator
		if (result.getPathLength() > 0) {
			g.setColor(Color.red);
			int dashes = 10;
			int dashSize = (getSize().height - 10) / (2 * dashes);
			for (int i = 0; i < (2 * dashes); i += 2) {
				g.drawLine(xStart + (pixPerTimeStep * result.getPathLength()),
						10 + i * dashSize,
						xStart + (pixPerTimeStep * result.getPathLength()), 10
								+ (i + 1) * dashSize);
			}
		}
		g2.setStroke(origStroke);
		super.paintChildren(g);
	}

	/**
	 * PRE: g, result, xStart, timeStepSpace, and resolution are defined
	 * POST: the path of result has been drawn starting at xStart, with
	 * timeStepSpace in between each step
	 * 
	 * @param g
	 * @param result
	 * @param xStart
	 * @param timeStepSpace
	 */
	private void paintPath(Graphics g, Result result, int xStart,
			int timeStepSpace) {
		int resolution = ((ResultsPanel) context).getResolution();
		double[][] states = result.getResult();

		// paint states in the path
		for (int i = 0; (i + 1) * resolution < states.length; i++) {
			paintState(
					g,
					states[i * resolution],
					states[(i + 1) * resolution],
					xStart + (i * resolution) * timeStepSpace,
					xStart
							+ ((i + 1) * ((ResultsPanel) context)
									.getResolution()) * timeStepSpace);
		}

		int xStart2 = xStart
				+ (((result.getTotalLength() - 1) / resolution) * resolution * timeStepSpace);

		int startInCycle;
		if (result.getPathLength() % resolution > 0) {
			startInCycle = resolution - (result.getPathLength() % resolution);
		} else {
			startInCycle = 0;
		}
		if (result.getCycleLength() > 0) {

			// paint additional cycles
			for (int k = 0; k < EXTRA_CYCLES_SHOWN; k++) {
				int lastState = result.getPathLength()
						+ (startInCycle + ((result.getCycleLength() - startInCycle) / resolution)
								* resolution);
				if ((result.getCycleLength() - startInCycle) % resolution == 0) {
					lastState -= resolution;
				}
				int firstState = startInCycle + result.getPathLength();

				if (result.getCycleLength() < resolution) {
					lastState = firstState = startInCycle;
				}

				paintState(g, states[lastState], states[firstState], xStart2,
						xStart2 + (resolution * timeStepSpace));
				xStart2 += resolution * timeStepSpace;

				for (int i = firstState; i + resolution < result
						.getTotalLength(); i += resolution) {
					paintState(g, states[i], states[i + resolution], xStart2,
							xStart2 + (resolution * timeStepSpace));
					xStart2 += resolution * timeStepSpace;
				}
			}
		}
	}

	/**
	 * PRE: g, prevState, curState ,xStart, and xSpace are defined
	 * POST: a line between prevState and curState is drawn starting at xStart
	 * and ending at xSpace
	 * 
	 * @param g
	 * @param prevState
	 * @param curState
	 * @param xStart
	 * @param xSpace
	 */
	private void paintState(Graphics g, double[] prevState, double[] curState,
			int xStart, int xSpace) {
		for (int k = 0; k < curState.length; k++) {
			if (drawLine[k]) {
				g.setColor(context
						.getModel()
						.getColors()
						.get(result.getModel().getNodes().get(k)
								.getColorChoice()));
				g.drawLine(
						xStart,
						(int) (graphDim.height + (k * 5) - (prevState[k] * ((graphDim.height - 20) / (context
								.getModel().getNodes().get(k).getNumStates() - 1)))),
						xSpace,
						(int) (graphDim.height + (k * 5) - (curState[k] * ((graphDim.height - 20) / (context
								.getModel().getNodes().get(k).getNumStates() - 1)))));
			}
		}
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

}
