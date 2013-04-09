package bmv;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * This class represents the section of the model to be displayed in the
 * BMVPanel; it provides the ability to zoom in and out and move around the
 * model area
 * 
 * @author plvines
 * 
 */
public class Viewport {

	protected Rectangle port;
	protected Dimension totalSize;
	protected double[] scaleFactor;
	protected double minScaleFactor;
	protected Dimension frameSize;

	/**
	 * PRE: this Viewport is undefined POST: default values are initialized
	 */
	public Viewport() {
		port = new Rectangle(0, 0, 1, 1);
		scaleFactor = new double[] { 1, 1 };
		frameSize = new Dimension(1, 1);
		totalSize = new Dimension(1, 1);
		minScaleFactor = 1;
	}

	/**
	 * PRE: orig is defined POST: this Viewport is a deep copy of orig
	 * 
	 * @param orig
	 */
	public Viewport(Viewport orig) {
		this.port = new Rectangle(orig.getPort());
		this.frameSize = orig.frameSize;
		this.totalSize = orig.totalSize;
		updateScaleFactor(frameSize);
	}

	/**
	 * PRIMARY CONSTRUCTOR PRE: port, frameSize, and totalSize are defined POST:
	 * this Viewport is initialized with the viewable area of port, and the data
	 * about the frame it will scale to display in and the total size of the
	 * model is is viewing are provided
	 * 
	 * @param port
	 * @param frameSize
	 * @param totalSize
	 */
	public Viewport(Rectangle port, Dimension frameSize, Dimension totalSize) {
		this.port = new Rectangle(port);
		this.frameSize = frameSize;
		this.totalSize = totalSize;
		updateScaleFactor(frameSize);
	}

	/**
	 * PRE: frameSize and port are defined POST: the scaleFactors are updated to
	 * properly resize to fit into the frameSize specified
	 * 
	 * @param frameSize
	 */
	public void updateScaleFactor(Dimension frameSize) {
		scaleFactor = new double[] {
				((double) frameSize.width) / ((double) port.width),
				((double) frameSize.height) / ((double) port.height), };
		minScaleFactor = Math.min(scaleFactor[0], scaleFactor[1]);
	}

	/**
	 * PRE: frameSize and port are defined POST: this simply refreshes the
	 * scaleFactor in case the frameSize or port has changed
	 */
	public void updateScaleFactor() {
		updateScaleFactor(this.frameSize);
	}

	/**
	 * PRE: viewCoord is defined POST: returns the value of viewCoord in terms
	 * of totalSize (absolute coordinates in the model)
	 * 
	 * @param viewCoord
	 * @return
	 */
	public Point frameToRealCoord(Point viewCoord) {
		return new Point(
				(int) (((double) viewCoord.x / scaleFactor[0]) + port.x),
				(int) (((double) viewCoord.y / scaleFactor[1]) + port.y));
	}

	/**
	 * PRE: realCoord is defined POST: returns the value of realCoord in terms
	 * of frameSize (used to place objects from the model coordinates into the
	 * viewport/frame)
	 * 
	 * @param realCoord
	 * @return
	 */
	public Point realToFrameCoord(Point realCoord) {
		return new Point((int) ((realCoord.x - port.x) * scaleFactor[0]),
				(int) ((realCoord.y - port.y) * scaleFactor[1]));
	}

	/**
	 * PRE: newPort is defined POST: this viewPort is a copy of newPort
	 * 
	 * @param newPort
	 */
	public void become(Viewport newPort) {
		port = newPort.port;
		frameSize = newPort.frameSize;
		totalSize = newPort.totalSize;
		updateScaleFactor();
	}

	/**
	 * PRE: newCenter is defined POST: the center of this Viewport is moved to
	 * newCenter, but it is forced to stay within the boundaries of totalSize
	 * 
	 * @param newCenter
	 */
	public void movePortCenter(Point newCenter) {
		port.x = newCenter.x - (port.width / 2);
		port.y = newCenter.y - (port.height / 2);
		if (port.x < 0) {
			port.x = 0;
		} else if (port.x + port.width > totalSize.width) {
			port.x = totalSize.width - port.width;
		}
		if (port.y < 0) {
			port.y = 0;
		} else if (port.y + port.height > totalSize.height) {
			port.y = totalSize.height - port.height;
		}
	}

	/**
	 * PRE: frameSize is defined POST: change the frameSize and updated the
	 * scaleFactors
	 * 
	 * @param frameSize
	 */
	public void update(Dimension frameSize) {
		updateScaleFactor(frameSize);
	}

	/**
	 * PRE: port and frameSize are defined POST: change the frameSize this
	 * viewport maps to and change the size of the viewport in the model
	 * 
	 * @param port
	 * @param frameSize
	 */
	public void update(Rectangle port, Dimension frameSize) {
		this.frameSize = frameSize;
		setPort(port);
	}

	/**
	 * PRE: percentChange is defined POST: change the size of the viewport by
	 * percentChange
	 * 
	 * @param percentChange
	 */
	protected void zoom(double percentChange) {
		double change = percentChange / 2;
		port = new Rectangle((int) (port.x - ((change - .5) * port.width)),
				(int) (port.y - ((change - .5) * port.height)),
				(int) (port.width * percentChange),
				(int) (port.height * percentChange));
		if (port.x < 0) {
			port.x = 0;
		}
		if (port.y < 0) {
			port.y = 0;
		}
		if (port.width <= 0) {
			port.width = 1;
		} else if (port.x + port.width > totalSize.width) {
			port.x = Math.max(0, totalSize.width - port.width);
			if (port.width > totalSize.width) {
				port.width = totalSize.width;
			}
		}
		if (port.height <= 0) {
			port.height = 1;
		} else if (port.y + port.height > totalSize.height) {
			port.y = Math.max(0, totalSize.height - port.height);
			if (port.height > totalSize.height) {
				port.height = totalSize.height;
			}
		}
		updateScaleFactor();
	}

	/**
	 * PRE: port is defined POST: RV = port
	 */
	public Rectangle getPort() {
		return port;
	}

	/**
	 * PRE: port is defined POST: port = port
	 */
	public void setPort(Rectangle port) {
		if (port.x >= 0) {
			this.port.x = port.x;
		}
		if (port.y >= 0) {
			this.port.y = port.y;
		}
		if (port.width <= 0) {
			port.width = 1;
		} else {
			this.port.width = port.width;
		}
		if (port.height <= 0) {
			port.height = 1;
		} else {
			this.port.height = port.height;
		}
		update(frameSize);
	}

	/**
	 * PRE: scaleFactor is defined POST: RV = scaleFactor
	 */
	public double[] getScaleFactor() {
		return scaleFactor;
	}

	/**
	 * PRE: totalSize is defined POST: totalSize = totalSize
	 */
	public void setTotalSize(Dimension totalSize) {
		this.totalSize = totalSize;
	}

	/**
	 * PRE: minScaleFactor is defined POST: RV = minScaleFactor
	 */
	public double getMinScaleFactor() {
		return minScaleFactor;
	}

	public String toString() {
		return "[" + port.x + "," + port.y + "," + port.width + ","
				+ port.height + "]\t" + scaleFactor[0] + "," + scaleFactor[1];
	}
}
