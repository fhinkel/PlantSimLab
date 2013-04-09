package bmv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * This class defines an edge between two nodes. Including which two nodes, the
 * edge's name, its color, and weighting
 * 
 * @author plvines
 * 
 */
public class Edge {

	protected static final int ACTIVATING = 1, INHIBITING = -1;
	protected static final int FUDGEFACTOR = 5;
	protected static final int THICKNESS_COEF = 2;
	private Model context;
	private Node start, end;
	private ArrayList<Point> anchors;
	private ArrayList<Point> pixAnchors;
	private Color lineColor;
	private int type, thickness;
	private boolean selected;
	private Polygon head;
	private int colorChoice;
	private double weight;
	private boolean multiJointed;
	private double lineAngle, intercept;
	private String name;
	private double nameAngle;
	private Point namePos;
	private Font nameFont;
	private Viewport port;
	private boolean drawName, forcedType;

	/**
	 * DEFAULT CONSTRUCTOR PRE: POST: this object has been initialized with lots
	 * of default values and nulls
	 */
	public Edge() {
		initialize(null, null, 1, null, null, 0, "", -1, null, false, false);
	}

	/**
	 * NOTE: even though this creates a deep copy of the Edge, that does not
	 * mean the start and end nodes will identify this object, they will still
	 * point to orig PRE: orig is defined POST: this object is a deep copy of
	 * orig
	 * 
	 * @param orig
	 */
	public Edge(Edge orig) {
		initialize(orig.getStart(), orig.getEnd(), orig.getWeight(), orig
				.getAnchors(), orig.getLineColor(), orig.getColorChoice(), orig
				.getName(), orig.getThickness(), orig.getContext(), orig
				.isMultiJointed(), orig.forcedType);
	}

	/**
	 * PRE: pStart, pEnd, pAnchors, pColor, colorChoice, pName, pContext, and
	 * multijointed are defined. POST: this edge has been initialized based off
	 * of these parameters
	 * 
	 * @param pStart
	 * @param pEnd
	 * @param pAnchors
	 * @param pColor
	 * @param colorChoice
	 * @param pName
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Node pEnd, ArrayList<Point> pAnchors,
			Color pColor, int colorChoice, String pName, Model pContext,
			boolean multiJointed) {
		initialize(pStart, pEnd, 1, pAnchors, pColor, colorChoice, pName, -1,
				pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pAnchors, pColor, colorChoice, pName, pThickness,
	 * pContext, and multijointed are defined. POST: this edge has been
	 * initialized based off of these parameters
	 * 
	 * @param pStart
	 * @param pEnd
	 * @param pAnchors
	 * @param pColor
	 * @param colorChoice
	 * @param pThickness
	 * @param pName
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Node pEnd, ArrayList<Point> pAnchors,
			Color pColor, int colorChoice, int pThickness, String pName,
			Model pContext, boolean multiJointed) {
		initialize(pStart, pEnd, 1, pAnchors, pColor, colorChoice, pName,
				pThickness, pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pAnchors, pColor, colorChoice, pWeight, pName,
	 * pContext, and multijointed are defined. POST: this edge has been
	 * initialized based off of these parameters
	 * 
	 * @param pStart
	 * @param pEnd
	 * @param pWeight
	 * @param pAnchors
	 * @param pColor
	 * @param colorChoice
	 * @param pName
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Node pEnd, double pWeight,
			ArrayList<Point> pAnchors, Color pColor, int colorChoice,
			String pName, Model pContext, boolean multiJointed) {
		initialize(pStart, pEnd, pWeight, pAnchors, pColor, colorChoice, pName,
				-1, pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pWeight, pAnchors, pColor, colorChoice, pThickness,
	 * pName, pContext, and multijointed are defined. POST: this edge has been
	 * initialized based off of these parameters
	 * 
	 * @param pStart
	 * @param pEnd
	 * @param pWeight
	 * @param pAnchors
	 * @param pColor
	 * @param colorChoice
	 * @param pThickness
	 * @param pName
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Node pEnd, double pWeight,
			ArrayList<Point> pAnchors, Color pColor, int colorChoice,
			int pThickness, String pName, Model pContext, boolean multiJointed) {
		initialize(pStart, pEnd, pWeight, pAnchors, pColor, colorChoice, pName,
				pThickness, pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pWeight, pColor, colorChoice, pName, pContext, and
	 * multijointed are defined. POST: this edge has been initialized based off
	 * of these parameters
	 * 
	 * @param pStart
	 * @param startPoint
	 * @param pColor
	 * @param colorChoice
	 * @param pWeight
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Point startPoint, Color pColor, int colorChoice,
			double pWeight, Model pContext, boolean multiJointed) {
		ArrayList<Point> pAnchors = new ArrayList<Point>();
		pAnchors.add(startPoint);
		pAnchors.add(startPoint);
		initialize(pStart, null, pWeight, pAnchors, pColor, colorChoice,
				"unnamed", -1, pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pWeight, pThickness, pColor, colorChoice, pName,
	 * pContext, and multijointed are defined. POST: this edge has been
	 * initialized based off of these parameters
	 * 
	 * @param pStart
	 * @param startPoint
	 * @param pColor
	 * @param colorChoice
	 * @param pThickness
	 * @param pWeight
	 * @param pContext
	 * @param multiJointed
	 */
	public Edge(Node pStart, Point startPoint, Color pColor, int colorChoice,
			int pThickness, double pWeight, Model pContext, boolean multiJointed) {
		ArrayList<Point> pAnchors = new ArrayList<Point>();
		pAnchors.add(startPoint);
		pAnchors.add(startPoint);
		initialize(pStart, null, pWeight, pAnchors, pColor, colorChoice,
				"unnamed", pThickness, pContext, multiJointed, false);
	}

	/**
	 * PRE: pStart, pEnd, pAnchors, pWeight, pThickness, pColor, colorChoice,
	 * pName, pContext, and multijointed are defined. POST: this edge has been
	 * initialized based off of these parameters, pixAnchors have been built,
	 * nameVariables have been initialized
	 * 
	 * @param pStart
	 * @param pEnd
	 * @param pWeight
	 * @param pAnchors
	 * @param pColor
	 * @param pColorChoice
	 * @param pName
	 * @param pThickness
	 * @param pContext
	 * @param multiJointed
	 */
	private void initialize(Node pStart, Node pEnd, double pWeight,
			ArrayList<Point> pAnchors, Color pColor, int pColorChoice,
			String pName, int pThickness, Model pContext, boolean multiJointed,
			boolean pForcedType) {
		this.port = pContext.getPort();
		weight = pWeight;
		if (weight < 0) {
			type = INHIBITING;
		} else {
			type = ACTIVATING;
		}
		start = pStart;
		name = pName;
		end = pEnd;
		context = pContext;
		colorChoice = pColorChoice;
		if (pAnchors == null) {
			anchors = new ArrayList<Point>();
			if (start != null) {
				anchors.add(new Point(start.getRealPos()));
			}
			if (end != null) {
				anchors.add(new Point(end.getRealPos()));
			}
		} else {
			copyAnchors(pAnchors);
		}
		if (pColor == null) {
			lineColor = Color.black;
		} else {
			lineColor = pColor;
		}
		thickness = (int) (Math.min(port.getScaleFactor()[0], port
				.getScaleFactor()[1]) * THICKNESS_COEF);
		// thickness = 1;

		buildPixAnchors();
		selected = false;
		drawName = context.isDrawEdgeNames();
		this.multiJointed = multiJointed;
		forcedType = pForcedType;
	}

	/**
	 * PRE: name and pixAnchors are defined POST: the nameFont has been
	 * initialized based off name and angle of the line segment
	 */
	private void initializeName() {
		if (end != null) {

			int x1, y1, x2, y2;
			int nameJoint = pixAnchors.size() / 2;
			x1 = pixAnchors.get(nameJoint).x;
			x2 = pixAnchors.get(nameJoint - 1).x;
			y1 = pixAnchors.get(nameJoint).y;
			y2 = pixAnchors.get(nameJoint - 1).y;

			double dx = x2 - x1;
			double dy = y2 - y1;

			if (dx != 0) {
				nameAngle = Math.atan(dy / dx);
			} else {
				nameAngle = Math.PI / 2;
			}

			AffineTransform at = new AffineTransform();
			at.setToTranslation(30, 50);
			AffineTransform fontAT = new AffineTransform();
			fontAT.rotate(nameAngle);

			Font tempNameFont = new Font("helvetica", Font.BOLD,
					thickness * 2 + 10);

			nameFont = tempNameFont.deriveFont(fontAT);

			namePos = new Point((int) (x1 + (dx / 2)), (int) (y1 + (dy / 2)));
			namePos.x -= (name.length() * 2 * Math.cos(nameAngle)) + 5
					* Math.cos(Math.PI / 2 + nameAngle);
			namePos.y -= (name.length() * 2 * Math.sin(nameAngle)) + 5
					* Math.sin(Math.PI / 2 + nameAngle);

		} else {
			namePos = null;
			nameFont = null;
			nameAngle = 0;
		}
	}

	/**
	 * PRE: context.context.getDivSize() is defined, end is defined, and anchors
	 * are defined POST: pixAnchors is redefined based on the division size of
	 * the panel context and the head is adjusted based on position of end
	 */
	public void resize() {
		thickness = (int) (Math.min(port.getScaleFactor()[0], port
				.getScaleFactor()[1]) * THICKNESS_COEF);
		// thickness = Math.min(context.getDivSize().width,
		// context.getDivSize().height) / 5;
		buildPixAnchors();
	}

	/**
	 * PRE: anchors, thickness ,and context are defined POST: rects is
	 * initialized to a list of rectangles representing the lines to be drawn,
	 * in pixel coordinates
	 */
	protected void buildPixAnchors() {
		pixAnchors = new ArrayList<Point>(anchors.size());

		if (multiJointed) {
			if (end != null && anchors.size() >= 2) {
				int xDiff = anchors.get(anchors.size() - 1).x
						- anchors.get(anchors.size() - 2).x;
				if (xDiff != 0) {
					anchors.set(anchors.size() - 2, new Point(anchors
							.get(anchors.size() - 2).x, end.getRealPos().y));
					anchors.set(anchors.size() - 1, new Point(anchors
							.get(anchors.size() - 1).x, end.getRealPos().y));
				} else {
					anchors.set(anchors.size() - 1, new Point(
							end.getRealPos().x,
							anchors.get(anchors.size() - 1).y));
					anchors.set(anchors.size() - 2, new Point(
							end.getRealPos().x,
							anchors.get(anchors.size() - 2).y));
				}

			}

			for (int i = 0; i < anchors.size(); i++) {
				pixAnchors.add(port.realToFrameCoord(anchors.get(i)));
			}
		} else {
			pixAnchors.add(port.realToFrameCoord(anchors.get(0)));
			pixAnchors.add(port.realToFrameCoord(anchors.get(1)));
		}

		if (!multiJointed) {
			if (nameAngle != 0 && nameAngle != Math.PI / 2
					&& nameAngle != -Math.PI / 2) {
				if (pixAnchors.get(1).x > pixAnchors.get(0).x) {
					lineAngle = ((double) (pixAnchors.get(1).y - pixAnchors
							.get(0).y))
							/ ((double) (pixAnchors.get(1).x - pixAnchors
									.get(0).x));
					intercept = pixAnchors.get(0).y
							- (pixAnchors.get(0).x * lineAngle);
				} else {
					lineAngle = ((double) (pixAnchors.get(0).y - pixAnchors
							.get(1).y))
							/ ((double) (pixAnchors.get(0).x - pixAnchors
									.get(1).x));
					intercept = pixAnchors.get(1).y
							- (pixAnchors.get(1).x * lineAngle);
				}
			} else if (nameAngle == 0) {
				lineAngle = 0;
				intercept = pixAnchors.get(0).y;
			} else {
				lineAngle = Double.MAX_VALUE;
				intercept = pixAnchors.get(0).y;
			}
		}
		if (start != null) {
			adjustStart();
		}
		if (end != null) {
			adjustEnd();
		}
		initializeName();

	}

	/**
	 * PRE: click and rects are defined POST: RV = true if the point of click
	 * falls within the bounds of any rectangle in this edge
	 * 
	 * @param click
	 * @return
	 */
	public boolean clicked(MouseEvent click) {
		boolean clicked = false;

		Line2D.Double line;

		for (int i = 0; !clicked && i < pixAnchors.size() - 1; i++) {

			line = new Line2D.Double(pixAnchors.get(i), pixAnchors.get(i + 1));
			clicked = line.intersects(new Rectangle2D.Double(
					click.getPoint().x - 5, click.getPoint().y - 5, 10, 10));
		}

		return clicked;
	}

	/**
	 * PRE: g is defined POST: this Edge is painted to g
	 * 
	 * @param g
	 */
	public void paint(Graphics g) {
		g.setColor(lineColor);
		Graphics2D g2 = (Graphics2D) g;
		Stroke origStroke = g2.getStroke();
		BasicStroke b1 = new BasicStroke(2 * thickness);
		BasicStroke b2 = new BasicStroke(2 * thickness + 6);
		g2.setStroke(b1);
		if (selected) {
			g.setColor(Color.white);
			g2.setStroke(b2);
			for (int i = 0; i < pixAnchors.size() - 1; i++) {
				g.drawLine(pixAnchors.get(i).x, pixAnchors.get(i).y, pixAnchors
						.get(i + 1).x, pixAnchors.get(i + 1).y);
			}
			g.setColor(lineColor);
			g2.setStroke(b1);
		}
		for (int i = 0; i < pixAnchors.size() - 1; i++) {
			g.drawLine(pixAnchors.get(i).x, pixAnchors.get(i).y, pixAnchors
					.get(i + 1).x, pixAnchors.get(i + 1).y);

		}
		if (head != null) {
			g.fillPolygon(head);
		}
		if (namePos != null && drawName) {
			g.setFont(nameFont);
			g.setColor(lineColor);
			Graphics2D g2d = (Graphics2D) g;

			g2d.drawString(name, namePos.x, namePos.y);
		}

		g2.setStroke(origStroke);
	}

	/**
	 * PRE: g and this edge are defined POST: this edge has been painted to g as
	 * a black line
	 * 
	 * @param g
	 */
	public void paintBW(Graphics g) {
		g.setColor(Color.black);
		Graphics2D g2 = (Graphics2D) g;
		Stroke origStroke = g2.getStroke();
		BasicStroke b1 = new BasicStroke(2 * thickness);
		g2.setStroke(b1);

		for (int i = 0; i < pixAnchors.size() - 1; i++) {
			g.drawLine(pixAnchors.get(i).x, pixAnchors.get(i).y, pixAnchors
					.get(i + 1).x, pixAnchors.get(i + 1).y);

		}
		if (head != null) {
			g.fillPolygon(head);
		}
		if (namePos != null && drawName) {
			g.setFont(nameFont);
			g.setColor(Color.black);
			Graphics2D g2d = (Graphics2D) g;

			g2d.drawString(name, namePos.x, namePos.y);
		}

		g2.setStroke(origStroke);
	}

	/**
	 * PRE: pAnchors is defined POST: anchors is a deep copy of pAnchors
	 * 
	 * @param pAnchors
	 */
	private void copyAnchors(ArrayList<Point> pAnchors) {
		anchors = new ArrayList<Point>();

		for (Point anchorIter : pAnchors) {
			anchors.add(new Point(anchorIter.x, anchorIter.y));
		}
	}

	/**
	 * PRE: newAnchor is defined POST: newAnchor has been added to anchors and
	 * pix anchors have been rebuilt
	 * 
	 * @param newAnchor
	 */
	public void addAnchor(Point newAnchor) {
		anchors.add(newAnchor);
		buildPixAnchors();
	}

	/**
	 * PRE: newAnchor is defined, anchors is defined and is of length >= 2 POST:
	 * the last entry in anchors is changed to be a point with one component
	 * from newAnchor and one component from the second-to-last vector, chosen
	 * to form the longest axis-oriented line
	 * 
	 * @param newAnchor
	 */
	public void changeLastAnchor(Point newAnchor) {
		if (anchors.size() > 1) {
			Point newLast = new Point();
			if (multiJointed) {
				int xDifference = Math.abs(newAnchor.x
						- anchors.get(anchors.size() - 2).x);
				int yDifference = Math.abs(newAnchor.y
						- anchors.get(anchors.size() - 2).y);
				if (xDifference > yDifference) {
					newLast.x = newAnchor.x;
					newLast.y = anchors.get(anchors.size() - 2).y;
				} else {
					newLast.y = newAnchor.y;
					newLast.x = anchors.get(anchors.size() - 2).x;
				}
			} else {
				newLast = newAnchor;
			}
			anchors.set(anchors.size() - 1, newLast);
			pixAnchors.set(pixAnchors.size() - 1, port.realToFrameCoord(anchors
					.get(anchors.size() - 1)));

		}
	}

	/**
	 * PRE: anchors and pixAnchors are defined POST: the highest index of
	 * anchors and pixAnchors is removed
	 */
	protected void removeLastAnchor() {
		if (anchors.size() > 0 && pixAnchors.size() > 0) {
			anchors.remove(anchors.size() - 1);
			pixAnchors.remove(pixAnchors.size() - 1);
		}
	}

	/**
	 * PRE: start is defined POST: changes the first anchor to match up with
	 * start's position
	 */
	private void adjustStart() {
		anchors.set(0, start.getRealPos());
	}

	/**
	 * PRE: end is defined POST: changes the head and last anchor to match up
	 * with end's position, incase end has moved or been resized
	 */
	private void adjustEnd() {
		anchors.set(anchors.size() - 1, end.getRealPos());
		// Activating
		if (type == ACTIVATING) {
			int spacer = thickness * 10;
			int multipleEdgesInSpacing = spacer
					* end.getInEdges().indexOf(this);

			int[] lineAxisPoints = new int[] { -spacer, 0, -spacer };
			int[] perpendicularAxisPoints = new int[] {
					-spacer + multipleEdgesInSpacing,
					0 + multipleEdgesInSpacing, spacer + multipleEdgesInSpacing };

			int xDiff = anchors.get(anchors.size() - 1).x
					- anchors.get(anchors.size() - 2).x;
			int yDiff = anchors.get(anchors.size() - 1).y
					- anchors.get(anchors.size() - 2).y;

			int[] arrowX, arrowY;

			Point lastPoint = pixAnchors.get(pixAnchors.size() - 1);

			// Arrow is pointing right
			if (xDiff > 0 && ((Math.abs(xDiff) > Math.abs(yDiff)))) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(
						end.getPixPos().x - (int) end.getScaledRad() - 10,
						lastPoint.y));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + lineAxisPoints[0],
						lastPoint.x + lineAxisPoints[1],
						lastPoint.x + lineAxisPoints[2] };
				arrowY = new int[] { lastPoint.y + perpendicularAxisPoints[0],
						lastPoint.y + perpendicularAxisPoints[1],
						lastPoint.y + perpendicularAxisPoints[2] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						- spacer, lastPoint.y + multipleEdgesInSpacing));
			}
			// Arrow is pointing left
			else if (xDiff < 0 && ((Math.abs(xDiff) > Math.abs(yDiff)))) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(
						end.getPixPos().x + (int) end.getScaledRad() + 10,
						lastPoint.y));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x - lineAxisPoints[0],
						lastPoint.x - lineAxisPoints[1],
						lastPoint.x - lineAxisPoints[2] };
				arrowY = new int[] { lastPoint.y + perpendicularAxisPoints[0],
						lastPoint.y + perpendicularAxisPoints[1],
						lastPoint.y + perpendicularAxisPoints[2] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ spacer, lastPoint.y + multipleEdgesInSpacing));
			}
			// Arrow is pointing down
			else if (yDiff > 0) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x,
						end.getPixPos().y - (int) end.getScaledRad() - 10));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + perpendicularAxisPoints[0],
						lastPoint.x + perpendicularAxisPoints[1],
						lastPoint.x + perpendicularAxisPoints[2] };
				arrowY = new int[] { lastPoint.y + lineAxisPoints[0],
						lastPoint.y + lineAxisPoints[1],
						lastPoint.y + lineAxisPoints[2] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ multipleEdgesInSpacing, lastPoint.y - spacer));
			}
			// Arrow is pointing up
			else {
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x,
						end.getPixPos().y + (int) end.getScaledRad() + 10));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + perpendicularAxisPoints[0],
						lastPoint.x + perpendicularAxisPoints[1],
						lastPoint.x + perpendicularAxisPoints[2] };
				arrowY = new int[] { lastPoint.y - lineAxisPoints[0],
						lastPoint.y - lineAxisPoints[1],
						lastPoint.y - lineAxisPoints[2] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ multipleEdgesInSpacing, lastPoint.y + spacer));
			}

			head = new Polygon(arrowX, arrowY, 3);
		}
		// Inhibiting
		else if (type == INHIBITING) {
			int spacer = thickness * 8;

			int multipleEdgesInSpacing = spacer
					* end.getInEdges().indexOf(this);

			int[] lineAxisPoints = new int[] { -spacer / 2, 0, 0, -spacer / 2 };
			int[] perpendicularAxisPoints = new int[] {
					-spacer + multipleEdgesInSpacing,
					-spacer + multipleEdgesInSpacing,
					spacer + multipleEdgesInSpacing,
					spacer + multipleEdgesInSpacing };

			int xDiff = anchors.get(anchors.size() - 1).x
					- anchors.get(anchors.size() - 2).x;
			int yDiff = anchors.get(anchors.size() - 1).y
					- anchors.get(anchors.size() - 2).y;

			int[] arrowX, arrowY;

			Point lastPoint = pixAnchors.get(pixAnchors.size() - 1);

			// Arrow is pointing right
			if (xDiff > 0 && ((Math.abs(xDiff) > Math.abs(yDiff)))) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(
						end.getPixPos().x - (int) end.getScaledRad() - 10,
						lastPoint.y));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + lineAxisPoints[0],
						lastPoint.x + lineAxisPoints[1],
						lastPoint.x + lineAxisPoints[2],
						lastPoint.x + lineAxisPoints[3] };
				arrowY = new int[] { lastPoint.y + perpendicularAxisPoints[0],
						lastPoint.y + perpendicularAxisPoints[1],
						lastPoint.y + perpendicularAxisPoints[2],
						lastPoint.y + perpendicularAxisPoints[3] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						- spacer / 2, lastPoint.y + multipleEdgesInSpacing));
			}
			// Arrow is pointing left
			else if (xDiff < 0 && ((Math.abs(xDiff) > Math.abs(yDiff)))) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(
						end.getPixPos().x + (int) end.getScaledRad() + 10,
						lastPoint.y));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x - lineAxisPoints[0],
						lastPoint.x - lineAxisPoints[1],
						lastPoint.x - lineAxisPoints[2],
						lastPoint.x - lineAxisPoints[3] };
				arrowY = new int[] { lastPoint.y + perpendicularAxisPoints[0],
						lastPoint.y + perpendicularAxisPoints[1],
						lastPoint.y + perpendicularAxisPoints[2],
						lastPoint.y + perpendicularAxisPoints[3] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ spacer / 2, lastPoint.y + multipleEdgesInSpacing));
			}
			// Arrow is pointing down
			else if (yDiff > 0) {
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x,
						end.getPixPos().y - (int) end.getScaledRad() - 10));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + perpendicularAxisPoints[0],
						lastPoint.x + perpendicularAxisPoints[1],
						lastPoint.x + perpendicularAxisPoints[2],
						lastPoint.x + perpendicularAxisPoints[3] };
				arrowY = new int[] { lastPoint.y + lineAxisPoints[0],
						lastPoint.y + lineAxisPoints[1],
						lastPoint.y + lineAxisPoints[2],
						lastPoint.y + lineAxisPoints[3] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ multipleEdgesInSpacing, lastPoint.y - spacer / 2));
			}
			// Arrow is pointing up
			else {
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x,
						end.getPixPos().y + (int) end.getScaledRad() + 10));
				lastPoint = pixAnchors.get(pixAnchors.size() - 1);

				arrowX = new int[] { lastPoint.x + perpendicularAxisPoints[0],
						lastPoint.x + perpendicularAxisPoints[1],
						lastPoint.x + perpendicularAxisPoints[2],
						lastPoint.x + perpendicularAxisPoints[3] };
				arrowY = new int[] { lastPoint.y - lineAxisPoints[0],
						lastPoint.y - lineAxisPoints[1],
						lastPoint.y - lineAxisPoints[2],
						lastPoint.y - lineAxisPoints[3] };
				pixAnchors.set(pixAnchors.size() - 1, new Point(lastPoint.x
						+ multipleEdgesInSpacing, lastPoint.y + spacer / 2));
			}

			head = new Polygon(arrowX, arrowY, 4);
		}
	}

	/**
	 * PRE: start is defined POST: RV = start
	 */
	public Node getStart() {
		return start;
	}

	/**
	 * PRE: start is defined POST: start = start
	 */
	public void setStart(Node start) {
		this.start = start;
		buildPixAnchors();
	}

	/**
	 * PRE: end is defined POST: RV = end
	 */
	public Node getEnd() {
		return end;
	}

	/**
	 * PRE: end is defined POST: end = end
	 */
	public void setEnd(Node end) {
		this.end = end;
		buildPixAnchors();

	}

	/**
	 * PRE: anchors is defined POST: RV = anchors
	 */
	public ArrayList<Point> getAnchors() {
		return anchors;
	}

	/**
	 * PRE: anchors is defined POST: anchors = anchors
	 */
	public void setAnchors(ArrayList<Point> anchors) {
		this.anchors = anchors;
		buildPixAnchors();
	}

	/**
	 * PRE: lineColor is defined POST: RV = lineColor
	 */
	public Color getLineColor() {
		return lineColor;
	}

	/**
	 * PRE: lineColor is defined POST: lineColor = lineColor
	 */
	public void setLineColor(Color lineColor) {
		this.lineColor = lineColor;
	}

	/**
	 * PRE: type is defined POST: RV = type
	 */
	public int getType() {
		return type;
	}

	/**
	 * PRE: type is defined, forced is defined and determines whether this
	 * edge's type can change with weighting or should always be displayed a
	 * certain way POST: if forced or not forcedType, type = type and forcedType
	 * = forced
	 */
	public void setType(int type, boolean forced) {
		if (forced || !forcedType) {
			this.type = type;
			forcedType = forced;
			buildPixAnchors();
		}
	}

	/**
	 * PRE: context is defined POST: RV = context
	 */
	public Model getContext() {
		return context;
	}

	/**
	 * PRE: context is defined POST: context = context
	 */

	public void setContext(Model context) {
		this.context = context;
	}

	/**
	 * PRE: thickness is defined POST: RV = thickness
	 */
	public int getThickness() {
		return thickness;
	}

	/**
	 * PRE: thickness is defined POST: thickness = thickness
	 */
	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	/**
	 * PRE: selected is defined POST: RV = selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * PRE: selected is defined POST: selected = selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * PRE: colorChoice is defined POST: RV = colorChoice
	 */
	public int getColorChoice() {
		return colorChoice;
	}

	/**
	 * PRE: colorChoice is defined POST: colorChoice = colorChoice
	 */
	public void setColorChoice(int colorChoice) {
		this.colorChoice = colorChoice;
		lineColor = context.getColors().get(colorChoice);
	}

	/**
	 * PRE: weight is defined POST: RV = weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * PRE: weight is defined POST: weight = weight
	 */
	public void setWeight(double weight) {
		if (weight < 0 && type == ACTIVATING) {
			setType(-1, false);
		} else if (weight > 0 && type == INHIBITING) {
			setType(1, false);
		}
		this.weight = weight;
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
	 * PRE: name is defined POST: RV = name
	 */
	public String getName() {
		return name;
	}

	/**
	 * PRE: name is defined POST: name = name
	 */
	public void setName(String name) {
		this.name = name;
		initializeName();
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

	public boolean isDrawName() {
		return drawName;
	}

	public void setDrawName(boolean drawName) {
		this.drawName = drawName;
	}

	/**
	 * PRE: forcedType is defined POST: RV = forcedType
	 */
	public boolean isForcedType() {
		return forcedType;
	}
}
