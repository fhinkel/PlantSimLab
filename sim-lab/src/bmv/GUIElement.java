package bmv;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.ImageIcon;

public class GUIElement {

	protected int[] pos; // [x1, y1, width, height]
	protected ImageIcon image, activeImage;
	protected String message;
	protected Color textColor, activeColor, boxColor;
	protected long activeCount;
	protected long time;
	protected long oldTime;
	protected ArrayList<String> brokenMessage;
	protected boolean drawBox;

	protected Font font;

	public GUIElement() {
		initialize(new int[] { 0, 0, 0, 0 }, "", null, null, Color.blue,
				Color.LIGHT_GRAY, Color.PINK,
				new Font("arial", Font.PLAIN, 12), false);
	}

	public GUIElement(int ppos[], String pmessage, Color ptextColor,
			Color pactiveColor, Font pFont, boolean pDrawBox) {
		initialize(ppos, pmessage, null, null, ptextColor, pactiveColor,
				Color.PINK, pFont, pDrawBox);

	}

	public GUIElement(int ppos[], String pmessage, Color ptextColor,
			Color pactiveColor, boolean pDrawBox) {
		initialize(ppos, pmessage, null, null, ptextColor, pactiveColor,
				Color.PINK, new Font("arial", Font.PLAIN, 12), pDrawBox);

	}

	public GUIElement(int ppos[], String pmessage, ImageIcon pimage,
			ImageIcon pactiveImage, boolean pDrawBox) {
		initialize(ppos, pmessage, pimage, pactiveImage, Color.blue,
				Color.LIGHT_GRAY, Color.PINK,
				new Font("arial", Font.PLAIN, 12), pDrawBox);
	}

	public GUIElement(int ppos[], String pmessage, ImageIcon pimage,
			ImageIcon pactiveImage, Color pTextColor, Color pActiveTextColor,
			Color pBoxColor, Font pFont, boolean pDrawBox) {
		initialize(ppos, pmessage, pimage, pactiveImage, pTextColor,
				pActiveTextColor, pBoxColor, pFont, pDrawBox);
	}

	protected void initialize(int ppos[], String pmessage, ImageIcon pimage,
			ImageIcon pactiveImage, Color ptextColor, Color pactiveColor,
			Color pboxColor, Font pfont, boolean pDrawBox) {
		pos = new int[4];
		for (int i = 0; i < 4; i++) {
			pos[i] = ppos[i];
		}
		message = pmessage;
		brokenMessage = null;
		initializeMessage();
		activeCount = 0;
		image = pimage;
		activeImage = pactiveImage;
		textColor = ptextColor;
		boxColor = pboxColor;
		boxColor = new Color(boxColor.getRed(), boxColor.getGreen(),
				boxColor.getBlue());
		activeColor = pactiveColor;
		font = pfont;
		drawBox = pDrawBox;
	}

	private void initializeMessage() {
		if (message.indexOf('\n') != -1) {
			brokenMessage = new ArrayList<String>();
			int lagIndex = 0;
			for (int i = 0; i < message.length(); i++) {
				if (message.charAt(i) == '\n') {
					brokenMessage.add(message.substring(lagIndex, i));
					lagIndex = i;
				}
			}
			brokenMessage.add(message.substring(lagIndex, message.length()));
		} else {
			brokenMessage = null;
		}
	}

	protected void updateActiveCount() {
		time = System.currentTimeMillis();
		activeCount -= (time - oldTime);
		oldTime = time;
	}

	public void draw(Graphics g) {
		// if a string
		if (activeCount > 0) {
			updateActiveCount();
		}
		if (image != null) {
			if (activeCount > 0) {
				g.drawImage(activeImage.getImage(), pos[0], pos[1], pos[2],
						pos[3], null);
			} else {
				g.drawImage(image.getImage(), pos[0], pos[1], pos[2], pos[3],
						null);
			}

		}
		if (!message.equals("")) {
			drawMessage(g);
		}
	}

	protected void drawMessage(Graphics g) {
		if (drawBox) {
			g.setColor(boxColor);
			g.fillRect(pos[0], pos[1], pos[2], pos[3]);
		}
		Font temp = g.getFont();
		g.setFont(font);
		// If active
		if (activeCount > 0) {
			g.setColor(activeColor);
		} else {
			g.setColor(textColor);
		}

		if (brokenMessage != null) {
			for (int i = 0; i < brokenMessage.size(); i++) {
				g.drawString(
						brokenMessage.get(i),
						pos[0],
						pos[1]
								+ ((i + 1) * (g.getFont().getSize() + pos[3]) / (brokenMessage
										.size() + 1)));
			}
		} else {
			g.drawString(message, pos[0], pos[1]
					+ (g.getFont().getSize() + pos[3]) / 2);
		}
		g.setFont(temp);
	}

	public boolean clicked(MouseEvent event) {
		boolean clicked = false;
		Point click = event.getPoint();

		if (click.x > pos[0] && click.y > pos[1] && click.x < pos[0] + pos[2]
				&& click.y < pos[1] + pos[3]) {
			clicked = true;
			startActiveCount();
		}

		return clicked;
	}

	protected void startActiveCount() {
		activeCount = 100;
		oldTime = System.currentTimeMillis();
	}

	/**
	 * @return the pos
	 */
	public int[] getPos() {
		return pos;
	}

	/**
	 * @param pos
	 *            the pos to set
	 */
	public void setPos(int[] pos) {
		this.pos = pos;
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
	 * @return the activeImage
	 */
	public ImageIcon getActiveImage() {
		return activeImage;
	}

	/**
	 * @param activeImage
	 *            the activeImage to set
	 */
	public void setActiveImage(ImageIcon activeImage) {
		this.activeImage = activeImage;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
		initializeMessage();
	}

	/**
	 * @return the textColor
	 */
	public Color getTextColor() {
		return textColor;
	}

	/**
	 * @param textColor
	 *            the textColor to set
	 */
	public void setTextColor(Color textColor) {
		this.textColor = textColor;
	}

	/**
	 * @return the activeColor
	 */
	public Color getActiveColor() {
		return activeColor;
	}

	/**
	 * @param activeColor
	 *            the activeColor to set
	 */
	public void setActiveColor(Color activeColor) {
		this.activeColor = activeColor;
	}

	/**
	 * @return the boxColor
	 */
	public Color getBoxColor() {
		return boxColor;
	}

	/**
	 * @param boxColor
	 *            the boxColor to set
	 */
	public void setBoxColor(Color boxColor) {
		this.boxColor = boxColor;
	}

	/**
	 * @return the activeCount
	 */
	public long getActiveCount() {
		return activeCount;
	}

	/**
	 * @param activeCount
	 *            the activeCount to set
	 */
	public void setActiveCount(int activeCount) {
		this.activeCount = activeCount;
	}

	/**
	 * @return the font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * @param font
	 *            the font to set
	 */
	public void setFont(Font font) {
		this.font = font;
	}

	/**
	 * PRE: pX is defined, pos is defined
	 * POST: pos[0] (the X anchor) = pX
	 * 
	 * @param pX
	 */
	public void changeX(int pX) {
		pos[0] = pX;
	}

	/**
	 * PRE: pY is defined, pos is defined
	 * POST: pos[1] (the Y anchor) = pY
	 * 
	 * @param pY
	 */
	public void changeY(int pY) {
		pos[1] = pY;
	}

}
