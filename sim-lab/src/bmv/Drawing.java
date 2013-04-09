package bmv;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;

/**
 * This class represents a line drawing with a number of x and y points, a
 * thickness and color to draw the line with, and the objects required to paint
 * it properly on the BMVPanel
 * 
 * @author plvines
 * 
 */
public class Drawing {

	private static final int THICKNESS_COEF = 4;
	int[] xPoints, xPaintingPoints;
	int[] yPoints, yPaintingPoints;
	int numPoints;
	int thickness;
	Viewport port;
	Model model;
	Color color;
	int colorChoice;
	boolean selected;

	/**
	 * RECTANGLE CONSTRUCTOR
	 * PRE: ct, colorChoice, model, port are all defined
	 * POST: this Drawing is now a rectangle with rect's points and color, and
	 * references to the model and port
	 * 
	 * @param rect
	 * @param colorChoice
	 * @param model
	 * @param port
	 */
	public Drawing(Rectangle rect, int colorChoice, Model model, Viewport port) {
		this.model = model;
		this.port = port;
		this.colorChoice = colorChoice;
		color = model.getColors().get(colorChoice);
		xPoints = new int[] { rect.x, rect.x + rect.width, rect.x + rect.width,
				rect.x };
		yPoints = new int[] { rect.y, rect.y, rect.y + rect.height,
				rect.y + rect.height };
		xPaintingPoints = new int[xPoints.length];
		yPaintingPoints = new int[yPoints.length];
		numPoints = Math.min(xPoints.length, yPoints.length);
		thickness = (int) (Math.min(port.getScaleFactor()[0],
				port.getScaleFactor()[1]) * THICKNESS_COEF);
		selected = false;
	}

	/**
	 * GENERAL CONSTRUCTOR
	 * PRE: colorChoice, xPoints, yPoint, numPoints, model, and port are
	 * defined. numPoints = |xPoints| = |yPoints|
	 * POST: this Drawing is now a drawing whose lines are defined by xPoints
	 * and yPoints.
	 * 
	 * @param colorChoice
	 * @param xPoints
	 * @param yPoints
	 * @param numPoints
	 * @param model
	 * @param port
	 */
	public Drawing(int colorChoice, int[] xPoints, int[] yPoints,
			int numPoints, Model model, Viewport port) {
		this.colorChoice = colorChoice;
		this.numPoints = numPoints;
		color = model.getColors().get(colorChoice);
		this.xPoints = xPoints;
		this.yPoints = yPoints;
		this.model = model;
		this.port = port;
		xPaintingPoints = new int[xPoints.length];
		yPaintingPoints = new int[yPoints.length];
		thickness = (int) (Math.min(port.getScaleFactor()[0],
				port.getScaleFactor()[1]) * THICKNESS_COEF);
		selected = false;
	}

	/**
	 * PRE: g is defined
	 * POST: constructs the x and y painting points based on the port
	 * coordinates and then paints them according to the thickness,
	 * selectedness, and colorchoice
	 * 
	 * @param g
	 */
	public void paint(Graphics2D g) {
		Point conversionTemp;
		Stroke origStroke = g.getStroke();

		for (int i = 0; i < numPoints; i++) {
			conversionTemp = port.realToFrameCoord(new Point(xPoints[i],
					yPoints[i]));
			xPaintingPoints[i] = conversionTemp.x;
			yPaintingPoints[i] = conversionTemp.y;
		}
		if (selected) {
			g.setColor(Color.white);
			g.setStroke(new BasicStroke(thickness + 4));

			g.drawPolygon(xPaintingPoints, yPaintingPoints, numPoints);
		}

		g.setColor(color);
		g.setStroke(new BasicStroke(thickness));

		g.drawPolygon(xPaintingPoints, yPaintingPoints, numPoints);

		g.setStroke(origStroke);
	}

	public void paintBW(Graphics2D g) {
		Point conversionTemp;
		Stroke origStroke = g.getStroke();

		for (int i = 0; i < numPoints; i++) {
			conversionTemp = port.realToFrameCoord(new Point(xPoints[i],
					yPoints[i]));
			xPaintingPoints[i] = conversionTemp.x;
			yPaintingPoints[i] = conversionTemp.y;
		}

		g.setColor(Color.black);
		g.setStroke(new BasicStroke(thickness));

		g.drawPolygon(xPaintingPoints, yPaintingPoints, numPoints);

		g.setStroke(origStroke);
	}

	/**
	 * CURRENTLY ONLY VALID FOR RECTANGLES
	 * PRE: click is defined
	 * POST: returns true if click occurred on one of the lines making up this
	 * drawing
	 * 
	 * @param click
	 * @return
	 */
	public boolean clicked(Point click) {
		boolean clicked = false;

		int lowX, highX, lowY, highY;
		if (xPaintingPoints[0] > xPaintingPoints[1]) {
			lowX = xPaintingPoints[1];
			highX = xPaintingPoints[0];
		} else {
			lowX = xPaintingPoints[0];
			highX = xPaintingPoints[1];
		}
		if (yPaintingPoints[0] > yPaintingPoints[3]) {
			lowY = yPaintingPoints[3];
			highY = yPaintingPoints[0];
		} else {
			lowY = yPaintingPoints[0];
			highY = yPaintingPoints[3];
		}
		if (click.x >= lowX - thickness && click.x <= highX + thickness
				&& click.y >= lowY - thickness && click.y <= highY + thickness) {
			clicked = (click.x <= lowX + thickness);
			clicked = clicked || (click.x >= highX - thickness);
			clicked = clicked || (click.y <= lowY + thickness);
			clicked = clicked || (click.y >= highY - thickness);
		}
		return clicked;
	}

	/**
	 * PRE: newCorner is defined
	 * POST: updates x and y points for a rectangular drawing for moving the
	 * bottom-right corner to newCorner
	 * 
	 * @param newCorner
	 */
	public void moveCorner(Point newCorner) {
		xPoints[1] = newCorner.x;
		xPoints[2] = newCorner.x;
		yPoints[2] = newCorner.y;
		yPoints[3] = newCorner.y;
	}

	/**
	 * PRE: xPoints is defined
	 * POST: RV = xPoints
	 */
	public int[] getxPoints() {
		return xPoints;
	}

	/**
	 * PRE: xPoints is defined
	 * POST: xPoints = xPoints
	 */
	public void setxPoints(int[] xPoints) {
		this.xPoints = xPoints;
	}

	/**
	 * PRE: yPoints is defined
	 * POST: RV = yPoints
	 */
	public int[] getyPoints() {
		return yPoints;
	}

	/**
	 * PRE: yPoints is defined
	 * POST: yPoints = yPoints
	 */
	public void setyPoints(int[] yPoints) {
		this.yPoints = yPoints;
	}

	/**
	 * PRE: numPoints is defined
	 * POST: RV = numPoints
	 */
	public int getNumPoints() {
		return numPoints;
	}

	/**
	 * PRE: numPoints is defined
	 * POST: numPoints = numPoints
	 */
	public void setNumPoints(int numPoints) {
		this.numPoints = numPoints;
	}

	/**
	 * PRE: colorChoice is defined
	 * POST: RV = colorChoice
	 */
	public int getColorChoice() {
		return colorChoice;
	}

	/**
	 * PRE: colorChoice is defined
	 * POST: colorChoice = colorChoice
	 */
	public void setColorChoice(int colorChoice) {
		this.colorChoice = colorChoice;
	}

	public void resize() {
		thickness = (int) (Math.min(port.getScaleFactor()[0],
				port.getScaleFactor()[1]) * THICKNESS_COEF);
	}

	/**
	 * PRE: selected is defined
	 * POST: RV = selected
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * PRE: selected is defined
	 * POST: selected = selected
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

}
