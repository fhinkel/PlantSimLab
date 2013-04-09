package bmv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.ImageIcon;

/**
 * This class represents a node in a network model
 * 
 * @author plvines
 * 
 */
public class Node {

	protected static enum TABLE_STATUS {
		UP_TO_DATE, OUT_OF_DATE
	}

	protected static enum SHAPE {
		SQUARE, CIRCLE, TRIANGLE
	}

	private static final int RED = 13;
	protected static final double scaleRange[][] = {
			{ 1 },
			{ 1 },
			{ .75, 1.5 },
			{ .5, 1.0, 1.5 },
			{ .5, .7937, 1.259921, 2.0 },
			{ .5, .70710678, 1.0, 1.4142, 2.0 },
			{ .5, 0.659753955, .87055, 1.148698, 1.5157, 2.0 },
			{ .5, .62996, .7937, 1.0, 1.2599, 1.5874, 2.0 },
			{ .5, .6095, .742997144, .90572366, 1.1040895, 2.0 },
			{ .5, .5946, .70710678, .840896, 1.0, 1.189207, 1.14142, 1.681792,
					2.0 } };
	private TABLE_STATUS tableStatus;
	private String abrevName, fullName;
	private ImageIcon image;
	private double rad; // radius
	private double scale;
	private NodeTable table, oldTable;
	private ArrayList<Edge> inEdges, outEdges;
	private Font nameFont;
	private Color nameColor;
	private Point pixPos, realPos;
	private boolean selected, knockedOut;
	private Model model;
	private long time;
	private boolean drawFlashes;
	private int colorChoice, numStates;
	private SHAPE shape;
	private int scaleIndex;
	private ArrayList<Term> termsUsed;
	private Viewport port;
	private Point stateChanged; // x is the old number of states, y is the new
								// number of states

	/**
	 * Default constructor.
	 * 
	 * @throws InvalidTableException
	 */
	public Node() throws InvalidTableException {
		initialize("", "", null, 0, SHAPE.CIRCLE, new Point(0, 0), 0, 0, 0, 0,
				null, null, null, null);
	}

	/**
	 * PRE: orig is fully defined as a Node POST: this Node is now a deep copy
	 * of orig
	 * 
	 * @param orig
	 * @throws InvalidTableException
	 */
	public Node(Node orig) throws InvalidTableException {
		initialize("" + orig.getAbrevName(), "" + orig.getFullName(),
				new ImageIcon(orig.getImage().getImage()),
				orig.getColorChoice(), orig.getShape(), orig.getRealPos(),
				orig.getRad(), orig.getScale(), orig.getScaleIndex(),
				orig.getNumStates(), null, null, orig.getTable(),
				orig.getModel());
		termsUsed = orig.getTermsUsed();
	}

	/**
	 * PRE: pRealPos, pRad, and pmodel are defined
	 * POST: a node is constructed at pRealPos, with pRad radius, and as a part
	 * of pmodel model
	 * 
	 * @param pRealPos
	 * @param pRad
	 * @param pmodel
	 * @throws InvalidTableException
	 */
	public Node(Point pRealPos, double pRad, Model pmodel)
			throws InvalidTableException {
		initialize("", "", new ImageIcon("Resources/Images/node0.png"), 0,
				SHAPE.CIRCLE, pRealPos, pRad, 1, -1, 2, null, null, null,
				pmodel);
	}

	/**
	 * PRE: pName, pImage, pColorChoice, pShape, pRealPos, pRad, pNumStates, and
	 * pmodel are defined.
	 * POST: a new node is constructed according to the input parameters
	 * 
	 * @param pName
	 * @param pImage
	 * @param pColorChoice
	 * @param pShape
	 * @param pRealPos
	 * @param pRad
	 * @param pNumStates
	 * @param pmodel
	 * @throws InvalidTableException
	 */
	public Node(String pAbrevName, String pFullName, ImageIcon pImage,
			int pColorChoice, SHAPE pShape, Point pRealPos, double pRad,
			int pNumStates, Model pmodel) throws InvalidTableException {
		initialize(pAbrevName, pFullName, pImage, pColorChoice, pShape,
				pRealPos, pRad, 1, -1, pNumStates, null, null, null, pmodel);
	}

	/**
	 * PRE: pName, pImage, pColorChoice, pShape, pRealPos, pRad, pNumStates, and
	 * pmodel are defined
	 * POST: a new node is constructed according to the input parameters
	 * 
	 * @param pName
	 * @param pImage
	 * @param pColorChoice
	 * @param pShape
	 * @param pRealPos
	 * @param pRad
	 * @param pNumStates
	 * @param pmodel
	 * @param pTerms
	 * @throws InvalidTableException
	 */
	public Node(String pAbrevName, String pFullName, ImageIcon pImage,
			int pColorChoice, SHAPE pShape, Point pRealPos, double pRad,
			int pNumStates, Model pmodel, ArrayList<Term> pTerms)
			throws InvalidTableException {
		termsUsed = pTerms;
		initialize(pAbrevName, pFullName, pImage, pColorChoice, pShape,
				pRealPos, pRad, 1, -1, pNumStates, null, null, null, pmodel);
	}

	/**
	 * PRE: All parameters are defined, except only one of tableFile,
	 * pStringTable, and pTable need to be defined POST: All instance variables
	 * of this Node are initialized to appropriate values, all parameters are
	 * deep-copied
	 * 
	 * @param pName
	 * @param pImage
	 * @param pRealPos
	 * @param pPixPos
	 * @param pRad
	 * @param pScale
	 * @param tableFile
	 * @param pStringTable
	 * @param pTable
	 * @throws InvalidTableException.
	 */
	private void initialize(String pAbrevName, String pFullName,
			ImageIcon pImage, int pColorChoice, SHAPE pShape, Point pRealPos,
			double pRad, double pScale, int pScaleIndex, int pNumStates,
			File tableFile, String pStringTable, NodeTable pTable, Model pmodel)
			throws InvalidTableException {

		model = pmodel;
		port = model.getPort();
		abrevName = pAbrevName;
		fullName = pFullName;
		image = pImage;
		numStates = pNumStates;
		tableStatus = TABLE_STATUS.OUT_OF_DATE;

		inEdges = new ArrayList<Edge>();
		outEdges = new ArrayList<Edge>();
		if (tableFile == null) {
			if (pStringTable == null) {
				if (pTable != null) {
					table = new NodeTable(pTable);
					tableStatus = TABLE_STATUS.UP_TO_DATE;
				} else {
					table = new NodeTable(this, model);
					tableStatus = TABLE_STATUS.OUT_OF_DATE;
				}
			} else {
				table = new NodeTable(this, pStringTable, model);
				tableStatus = TABLE_STATUS.UP_TO_DATE;
			}
		} else {
			readTable(tableFile);
		}

		if (termsUsed == null) {
			termsUsed = new ArrayList<Term>();
			for (int i = 0; i < numStates; i++) {
				termsUsed.add(model.getVocab().getTermSet(numStates).get(i));
			}
		}

		setRealPos(new Point(pRealPos.x, pRealPos.y));
		rad = pRad;

		if (pScaleIndex != -1) {
			scaleIndex = pScaleIndex;
			scale = scaleRange[numStates][scaleIndex];
		} else {
			scale = pScale;
			scaleIndex = numStates / 2;
		}

		backupTable();
		initializeName();
		selected = false;
		knockedOut = false;
		time = System.currentTimeMillis();
		drawFlashes = false;
		colorChoice = pColorChoice;
		shape = pShape;
		stateChanged = null;
	}

	/**
	 * PRE:name, rad, and scale have been defined
	 * POST: nameFont and nameColor have been defined according to name, rad,
	 * and scale
	 */
	private void initializeName() {
		nameColor = Color.white;
		nameFont = new Font("helvetica", Font.BOLD,
				(int) ((2 * (rad * scale)) / abrevName.length()));
	}

	/**
	 * PRE: model.model.getDivSize() is defined, realPos is defined POST:
	 * pixPos is redefined based on the division size of the panel model and
	 * realPos
	 */
	public void resize() {
		setRealPos(realPos);
		rad = (port.getMinScaleFactor() * 20) + 5;
		initializeName();
	}

	/**
	 * PRE: table is defined POST: oldTable is now a deep copy of table
	 */
	private void backupTable() {
		if (table != null) {
			oldTable = new NodeTable(table);
		} else {
			oldTable = null;
		}
	}

	/**
	 * PRE: newTable and table are defined POST: if newTable is correctly
	 * formatted, table = parsed newTable, otherwise table is unchanged but
	 * oldTable = table
	 * 
	 * @param table
	 */
	public void newTable(String newTable) {
		backupTable();
		if (checkEdgesToTable(newTable.substring(0, newTable.indexOf('\n')))) {
			table = new NodeTable(this, newTable, model);
			tableStatus = TABLE_STATUS.UP_TO_DATE;
		} else {
			revertTable();
		}
	}

	/**
	 * PRE: table and oldTable are defined POST: table is now a deep copy of
	 * oldTable
	 */
	protected void revertTable() {
		table = oldTable;
		backupTable();
	}

	/**
	 * PRE: g is defined POST: the image of this Node is painted to g at the
	 * position indicated by pixPos
	 * 
	 * @param g
	 */
	public void paint(Graphics2D g) {

		if (System.currentTimeMillis() - time > 500) {
			time = System.currentTimeMillis();
			drawFlashes = !drawFlashes;
		}
		int scaledRad = (int) (scale * rad);

		if (shape == SHAPE.TRIANGLE) {
			scaledRad *= 1.25;
		}
		if (selected) {
			g.setColor(Color.white);
			Stroke origStroke = ((Graphics2D) g).getStroke();
			((Graphics2D) g).setStroke(new BasicStroke(5));
			if (shape == SHAPE.SQUARE) {
				g.drawRect(pixPos.x - scaledRad - 4, pixPos.y - scaledRad - 4,
						2 * scaledRad + 7, 2 * scaledRad + 7);
			} else if (shape == SHAPE.CIRCLE) {
				g.drawOval(pixPos.x - scaledRad - 4, pixPos.y - scaledRad - 4,
						2 * scaledRad + 7, 2 * scaledRad + 7);
			} else if (shape == SHAPE.TRIANGLE) {
				g.drawPolyline(new int[] { pixPos.x - scaledRad - 4, pixPos.x,
						pixPos.x + scaledRad + 4, pixPos.x - scaledRad - 4 },
						new int[] { pixPos.y + scaledRad + 2,
								pixPos.y - scaledRad - 4,
								pixPos.y + scaledRad + 2,
								pixPos.y + scaledRad + 2 }, 4);
			}
			((Graphics2D) g).setStroke(origStroke);
		}

		AffineTransform trans = null;

		trans = new AffineTransform((double) scaledRad / 400, 0, 0,
				(double) scaledRad / 400, pixPos.x - scaledRad, pixPos.y
						- scaledRad);
		g.drawImage(image.getImage(), trans, null);

		if (drawFlashes && tableStatus == TABLE_STATUS.OUT_OF_DATE) {
			if (colorChoice != RED) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.blue);
			}
			Stroke origStroke = ((Graphics2D) g).getStroke();
			((Graphics2D) g).setStroke(new BasicStroke(5));
			if (shape == SHAPE.SQUARE) {
				g.drawRect(pixPos.x - scaledRad + 5, pixPos.y - scaledRad + 5,
						2 * scaledRad - 10, 2 * scaledRad - 10);
			} else if (shape == SHAPE.CIRCLE) {
				g.drawOval(pixPos.x - scaledRad + 5, pixPos.y - scaledRad + 5,
						2 * scaledRad - 10, 2 * scaledRad - 10);
			} else if (shape == SHAPE.TRIANGLE) {
				g.drawPolyline(new int[] { pixPos.x - scaledRad + 10, pixPos.x,
						pixPos.x + scaledRad - 10, pixPos.x - scaledRad + 10 },
						new int[] { pixPos.y + scaledRad - 6,
								pixPos.y - scaledRad + 7,
								pixPos.y + scaledRad - 6,
								pixPos.y + scaledRad - 6 }, 4);
			}
			((Graphics2D) g).setStroke(origStroke);
		}
		if (knockedOut) {
			Stroke origStroke = ((Graphics2D) g).getStroke();
			((Graphics2D) g).setStroke(new BasicStroke(5));

			if (colorChoice != RED) {
				g.setColor(Color.red);
			} else {
				g.setColor(Color.blue);
			}
			g.drawLine(pixPos.x + (int) (scaledRad * .7), pixPos.y
					- (int) (scaledRad * .7),
					pixPos.x - (int) (scaledRad * .7), pixPos.y
							+ (int) (scaledRad * .7));
			g.drawLine(pixPos.x - (int) (scaledRad * .7), pixPos.y
					- (int) (scaledRad * .7),
					pixPos.x + (int) (scaledRad * .7), pixPos.y
							+ (int) (scaledRad * .7));
			((Graphics2D) g).setStroke(origStroke);
		}
		g.setColor(nameColor);
		g.setFont(nameFont);
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D text = fm.getStringBounds(abrevName, g);
		int xString = (int) (pixPos.x - text.getCenterX());

		int yString = 0;
		if (shape == SHAPE.TRIANGLE) {
			yString = (int) ((pixPos.y + (scaledRad)) - 5);
		} else {
			yString = (int) (pixPos.y - text.getCenterY());
		}
		g.drawString(abrevName, xString, yString);
	}

	public void paintBW(Graphics2D g) {

		int scaledRad = (int) (scale * rad);

		if (shape == SHAPE.TRIANGLE) {
			scaledRad *= 1.25;
		}
		g.setColor(Color.black);
		Stroke origStroke = ((Graphics2D) g).getStroke();
		((Graphics2D) g).setStroke(new BasicStroke(5));
		if (shape == SHAPE.SQUARE) {
			g.fillRect(pixPos.x - scaledRad - 4, pixPos.y - scaledRad - 4,
					2 * scaledRad + 7, 2 * scaledRad + 7);
		} else if (shape == SHAPE.CIRCLE) {
			g.fillOval(pixPos.x - scaledRad - 4, pixPos.y - scaledRad - 4,
					2 * scaledRad + 7, 2 * scaledRad + 7);
		} else if (shape == SHAPE.TRIANGLE) {
			g.fillPolygon(new int[] { pixPos.x - scaledRad - 4, pixPos.x,
					pixPos.x + scaledRad + 4, pixPos.x - scaledRad - 4 },
					new int[] { pixPos.y + scaledRad + 2,
							pixPos.y - scaledRad - 4, pixPos.y + scaledRad + 2,
							pixPos.y + scaledRad + 2 }, 4);
		}
		((Graphics2D) g).setStroke(origStroke);

		if (knockedOut) {
			((Graphics2D) g).setStroke(new BasicStroke(5));

			g.setColor(Color.gray);
			g.drawLine(pixPos.x + (int) (scaledRad * .7), pixPos.y
					- (int) (scaledRad * .7),
					pixPos.x - (int) (scaledRad * .7), pixPos.y
							+ (int) (scaledRad * .7));
			g.drawLine(pixPos.x - (int) (scaledRad * .7), pixPos.y
					- (int) (scaledRad * .7),
					pixPos.x + (int) (scaledRad * .7), pixPos.y
							+ (int) (scaledRad * .7));
			((Graphics2D) g).setStroke(origStroke);
		}
		g.setColor(Color.white);
		g.setFont(nameFont);
		FontMetrics fm = g.getFontMetrics();
		Rectangle2D text = fm.getStringBounds(abrevName, g);
		int xString = (int) (pixPos.x - text.getCenterX());

		int yString = 0;
		if (shape == SHAPE.TRIANGLE) {
			yString = (int) ((pixPos.y + (scaledRad)) - 5);
		} else {
			yString = (int) (pixPos.y - text.getCenterY());
		}
		g.drawString(abrevName, xString, yString);
	}

	/**
	 * PRE: input is defined
	 * POST: the input file has been read and the table within it used to
	 * construct the nodetable for this node
	 * 
	 * @param input
	 * @throws InvalidTableException
	 */
	public void readTable(File input) throws InvalidTableException {
		try {
			Scanner filescan = new Scanner(input);
			String buffer = "";
			while (filescan.hasNextLine()) {
				buffer += filescan.nextLine() + " \n";
			}
			tableStatus = TABLE_STATUS.UP_TO_DATE;
			table = new NodeTable(this, buffer, model);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new InvalidTableException();
		}

	}

	/**
	 * PRE: writeDirectory is defined as a valid path POST: the table of this
	 * node is written in correct format to the writeDirectory as a file named
	 * [name].csv
	 * 
	 * @param writeDirectory
	 * @throws IOException
	 */
	public void writeTable(String writeDirectory) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(
				writeDirectory + abrevName + ".csv")));

		// for (int i = 0; i < inEdges.size(); i++) {
		// tableString += inEdges.get(i).getStart().getName() + "\t";
		// }
		// tableString += name + "\t" + name + "\n";

		// for (int i = 0; i < inEdges.size(); i++) {
		// tableString += inEdges.get(i).getStart().getNumStates() + "\t";
		// }
		// tableString += "\n";

		System.out.println(table.valueString());
		writer.write(table.valueString());
		writer.close();
	}

	/**
	 * PRE: tableHeader is defined as the first line of the table which includes
	 * the names of all the nodes influencing this one POST: RV = true if the
	 * names of input nodes match the names of start nodes for all edgesIn,
	 * otherwise returns false
	 * 
	 * @return
	 */
	private boolean checkEdgesToTable(String tableHeader) {
		boolean correct = true;
		Scanner headerScan = new Scanner(tableHeader);

		for (int i = 0; correct && i < inEdges.size(); i++) {
			correct = headerScan.next().equalsIgnoreCase(
					inEdges.get(i).getStart().getAbrevName());
		}
		while (correct && headerScan.hasNext()) {
			correct = headerScan.next().equalsIgnoreCase(this.abrevName);
		}

		return correct;
	}

	/**
	 * PRE: event is defined, this node's pos is defined POST: RV = true if the
	 * click occurred within the bounds of this Node as designated by rad *
	 * scale in pixels
	 * 
	 * @param event
	 * @return
	 */
	public boolean clicked(Point event) {

		int dx = event.x - pixPos.x;
		int dy = event.y - pixPos.y;

		double distance = ((dx * dx) + (dy * dy));
		return (distance <= (rad * scale) * (rad * scale));
	}

	/**
	 * PRE: inEdges and newIn are defined POST: inEdges now includes newIn,
	 * tableStatus has been changed to OUT_OF_DATE
	 * 
	 * @param newIn
	 */
	public void addInEdge(Edge newIn) {
		tableStatus = TABLE_STATUS.OUT_OF_DATE;
		inEdges.add(newIn);
		backupTable();
		table = new NodeTable(this, model);
	}

	/**
	 * PRE: outEdges and newOut have been defined POST: outEdges now contains
	 * newOut
	 * 
	 * @param newOut
	 */
	public void addOutEdge(Edge newOut) {
		outEdges.add(newOut);
	}

	/**
	 * PRE: edgeToRemove and inEdges are defined POST: edgeToRemove has been
	 * removed from inEdges, tableStatus = OUT_OF_DATE
	 * 
	 * @param edgeToRemove
	 */
	public void removeIn(Edge edgeToRemove) {
		inEdges.remove(edgeToRemove);
		tableStatus = TABLE_STATUS.OUT_OF_DATE;
		backupTable();
		table = new NodeTable(this, model);

	}

	/**
	 * PRE: edgeToRemove and outEdges are defined POST: edgeToRemove has been
	 * removed from outEdges
	 * 
	 * @param edgeToRemove
	 */
	public void removeOut(Edge edgeToRemove) {
		outEdges.remove(edgeToRemove);
	}

	/**
	 * @return the image
	 */
	public ImageIcon getImage() {
		return image;
	}

	/**
	 * @param image
	 *            the image to set
	 */
	public void setImage(ImageIcon image) {
		this.image = image;
	}

	/**
	 * PRE: scaleIndex is defined POST: RV = scaleIndex
	 */
	public int getScaleIndex() {
		return scaleIndex;
	}

	/**
	 * PRE: scaleIndex is defined POST: scaleIndex = scaleIndex
	 */
	public void setScaleIndex(int scaleIndex) {
		this.scaleIndex = scaleIndex;
		setScale(scaleRange[numStates][scaleIndex]);
	}

	/**
	 * PRE: the pixPos is defined. POST: RV = pixPos
	 */
	public Point getPixPos() {
		return pixPos;
	}

	/**
	 * PRE: POST: pixPos = pixPos
	 */
	public void setPixPos(Point pixPos) {
		this.pixPos = pixPos;
	}

	/**
	 * PRE: the realPos is defined. POST: RV = realPos
	 */
	public Point getRealPos() {
		return realPos;
	}

	/**
	 * PRE: POST: realPos = realPos
	 */
	public void setRealPos(Point realPos) {
		this.realPos = realPos;
		pixPos = port.realToFrameCoord(realPos);
		for (int i = 0; i < inEdges.size(); i++) {
			inEdges.get(i).resize();
		}
		for (int i = 0; i < outEdges.size(); i++) {
			outEdges.get(i).resize();
		}
	}

	/**
	 * @return the rad
	 */
	public double getRad() {
		return rad;
	}

	/**
	 * @param rad
	 *            the rad to set
	 */
	public void setRad(double rad) {
		this.rad = rad;
	}

	/**
	 * @return the scale
	 */
	public double getScale() {
		return scale;
	}

	/**
	 * @param scale
	 *            the scale to set
	 */
	public void setScale(double scale) {
		this.scale = scale;
		initializeName();
		for (int i = 0; i < inEdges.size(); i++) {
			inEdges.get(i).buildPixAnchors();
		}
		for (int i = 0; i < outEdges.size(); i++) {
			outEdges.get(i).buildPixAnchors();
		}
	}

	/**
	 * PRE: the name is defined. POST: RV = name
	 */
	public String getAbrevName() {
		return abrevName;
	}

	/**
	 * PRE: POST: name = name
	 */
	public void setAbrevName(String abrevName) {
		this.abrevName = abrevName;
		initializeName();
		table.updateColumnNames();
		for (int i = 0; i < outEdges.size(); i++) {
			outEdges.get(i).getEnd().getTable().updateColumnNames();
		}
	}

	/**
	 * PRE: the name is defined. POST: RV = name
	 */
	public String getFullName() {
		return fullName;
	}

	/**
	 * PRE: POST: name = name
	 */
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public void setNames(String abrevName, String fullName) {
		setAbrevName(abrevName);
		setFullName(fullName);
	}

	/**
	 * PRE: table is defined POST: RV = table
	 */
	public NodeTable getTable() {
		return table;
	}

	public void setTable(NodeTable table) {
		backupTable();
		this.table = new NodeTable(table);
		tableStatus = TABLE_STATUS.UP_TO_DATE;
	}

	/**
	 * PRE: scale and rad are defined POST: RV = scale * rad
	 * 
	 * @return
	 */
	public double getScaledRad() {
		return scale * rad;
	}

	/**
	 * PRE: inEdges is defined POST: RV = inEdges
	 */
	public ArrayList<Edge> getInEdges() {
		return inEdges;
	}

	/**
	 * PRE: inEdges is defined POST: inEdges = inEdges
	 */
	public void setInEdges(ArrayList<Edge> inEdges) {
		this.inEdges = inEdges;
	}

	/**
	 * PRE: outEdges is defined POST: RV = outEdges
	 */
	public ArrayList<Edge> getOutEdges() {
		return outEdges;
	}

	/**
	 * PRE: outEdges is defined POST: outEdges = outEdges
	 */
	public void setOutEdges(ArrayList<Edge> outEdges) {
		this.outEdges = outEdges;
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
		image = new ImageIcon("Resources/Images/node" + shape.ordinal() + "-"
				+ colorChoice + ".png");
		for (int i = 0; i < outEdges.size(); i++) {
			outEdges.get(i).setColorChoice(colorChoice);
		}
	}

	/**
	 * PRE: tableStatus is defined POST: RV = tableStatus
	 */
	public TABLE_STATUS getTableStatus() {
		return tableStatus;
	}

	/**
	 * PRE: tableStatus is defined POST: tableStatus = tableStatus
	 */
	public void setTableStatus(TABLE_STATUS tableStatus) {
		this.tableStatus = tableStatus;
	}

	/**
	 * PRE: numStates is defined POST: RV = numStates
	 */
	public int getNumStates() {
		return numStates;
	}

	/**
	 * PRE: termsUsed is defined POST: RV = termsUsed
	 */
	public ArrayList<Term> getTermsUsed() {
		return termsUsed;
	}

	/**
	 * PRE: termsUsed is defined POST: termsUsed = termsUsed
	 */
	public void setTermsUsed(ArrayList<Term> termsUsed) {
		this.termsUsed = termsUsed;
		table = new NodeTable(this, model);
		if (stateChanged == null) {
			table.refreshNames();
		}
		for (int i = 0; i < outEdges.size(); i++) {
			outEdges.get(i).getEnd().setTableStatus(TABLE_STATUS.OUT_OF_DATE);
			if (stateChanged == null) {
				outEdges.get(i).getEnd().getTable().refreshNames();
			}
		}
	}

	/**
	 * PRE: knockedOut is defined POST: RV = knockedOut
	 */
	public boolean isKnockedOut() {
		return knockedOut;
	}

	/**
	 * PRE: knockedOut is defined POST: knockedOut = knockedOut
	 */
	public void setKnockedOut(boolean knockedOut) {
		this.knockedOut = knockedOut;
		if (knockedOut) {
			setScaleIndex(0);
			setScale(1);
		}
	}

	/**
	 * PRE: numStates is defined POST: numStates = numStates
	 */
	public void setNumStates(int numStates) {
		if (numStates != this.numStates) {
			if (tableStatus == TABLE_STATUS.UP_TO_DATE) {
				stateChanged = new Point(this.numStates, numStates);
			} else {
				stateChanged = null;
			}
			this.numStates = numStates;
			tableStatus = TABLE_STATUS.OUT_OF_DATE;
		}
	}

	/**
	 * PRE: stateChanged is defined POST: RV = stateChanged
	 */
	public Point getStateChanged() {
		return stateChanged;
	}

	/**
	 * PRE: stateChanged is defined POST: stateChanged = stateChanged
	 */
	public void setStateChanged(Point stateChanged) {
		this.stateChanged = stateChanged;
	}

	/**
	 * PRE: shape is defined POST: RV = shape
	 */
	public SHAPE getShape() {
		return shape;
	}

	/**
	 * PRE: shape is defined POST: shape = shape
	 */
	public void setShape(SHAPE shape) {
		if (shape != this.shape) {
			image = new ImageIcon("Resources/Images/node" + shape.ordinal()
					+ "-" + colorChoice + ".png");
		}
		this.shape = shape;
	}

}
