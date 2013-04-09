package bmv;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

public class ToggleGUIElement extends GUIElement {
	public ToggleGUIElement() {

	}

	public ToggleGUIElement(int ppos[], String pmessage, Color ptextColor,
			Color pactiveColor, Font pFont, boolean pDrawBox) {
		super.initialize(ppos, pmessage, null, null, ptextColor, pactiveColor,
				Color.PINK, pFont, pDrawBox);

	}

	public ToggleGUIElement(int ppos[], String pmessage, Color ptextColor,
			Color pactiveColor, boolean pDrawBox) {
		super.initialize(ppos, pmessage, null, null, ptextColor, pactiveColor,
				Color.PINK, new Font("arial", Font.PLAIN, 12), pDrawBox);

	}

	public ToggleGUIElement(int ppos[], String pmessage, ImageIcon pimage,
			ImageIcon pactiveImage, Color pTextColor, Color pActiveTextColor,
			Color pBoxColor, Font pFont, boolean pDrawBox) {
		super.initialize(ppos, pmessage, pimage, pactiveImage, pTextColor,
				pActiveTextColor, pBoxColor, pFont, pDrawBox);
	}

	public ToggleGUIElement(int ppos[], String pmessage, ImageIcon pimage,
			ImageIcon pactiveImage, boolean pDrawBox) {
		super.initialize(ppos, pmessage, pimage, pactiveImage, Color.blue,
				Color.LIGHT_GRAY, Color.PINK,
				new Font("arial", Font.PLAIN, 12), pDrawBox);
	}

	protected void updateActiveCount() {

	}

	protected void startActiveCount() {
		if (activeCount > 0) {
			activeCount = 0;
		} else {
			activeCount = 1;
		}
	}

	protected void toggle() {
		startActiveCount();
	}

	protected void toggleOff() {
		if (activeCount > 0) {
			startActiveCount();
		}
	}

	protected void toggleOn() {
		if (activeCount == 0) {
			startActiveCount();
		}
	}
}
