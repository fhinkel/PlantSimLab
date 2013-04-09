package bmv;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * This class is extended for the various types of popup panels used in BMV. It
 * includes the ability to be resized by dragging the edges, move the entire
 * panel by draggging anywhere else, and built-in methods for adding Ok/Cancel
 * buttons and help popup messages
 * 
 * @author plvines
 * 
 */
public class MoveablePanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5798585231313806139L;

	private enum MODE {
		STATIC, MOVING, RESIZING
	};

	protected static final int FUDGEFACTOR = 5;
	protected static final int BORDER = 2;
	protected Dimension internalDistance;
	protected MODE mode;
	protected LayoutManager layout;
	protected Point location;
	protected JButton helpButton;
	protected JPopupMenu pmenu;

	/**
	 * PRIMARY CONSTRUCTOR
	 * PRE: location is defined
	 * POST: the panel has been constructed at the location. Mouse and keyboard
	 * listeners have been added
	 * 
	 * NOTE: extending subclasses are expected to specify the size of the panel
	 * based on content.
	 * 
	 * @param location
	 */
	public MoveablePanel(Point location) {
		this.location = location;

		layout = new BorderLayout();
		mode = MODE.STATIC;
		internalDistance = new Dimension(0, 0);
		setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR, 2));
		setBackground(Color.white);
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseHandler());
		addKeyListener(new KeyHandler());
		setFocusable(true);
		requestFocusInWindow();
	}

	/**
	 * PRE:
	 * POST:the panel has been constructed at 0,0. Mouse and keyboard
	 * listeners have been added
	 * 
	 * NOTE: extending subclasses are expected to specify the size of the panel
	 * based on content.
	 */
	public MoveablePanel() {
		layout = new BorderLayout();
		mode = MODE.STATIC;
		internalDistance = new Dimension(0, 0);
		setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR, 2));
		setBackground(Color.white);
		addMouseListener(new MouseHandler());
		addMouseMotionListener(new MouseHandler());
		addKeyListener(new KeyHandler());
		setFocusable(true);
		requestFocusInWindow();
	}

	/**
	 * PRE: helpMessage, width, height, and this panel have been defined,
	 * Resources/icons/16/207.png exists
	 * POST: a JButton has been added to the layout with the help icon (?). This
	 * button has had popup menu functionality extended so that a left or right
	 * click brings up a popup text windows display9ing "helpMessage" in it, and
	 * of the size designated by width and height parameters
	 * 
	 * @param helpMessage
	 * @param width
	 * @param height
	 */
	protected void addHelp(String helpMessage, int width, int height) {
		if (helpButton != null) {
			this.remove(helpButton);
		}
		ImageIcon icon = new ImageIcon("Resources/icons/16/207.png");
		helpButton = new JButton(icon);
		helpButton.setPreferredSize(new Dimension(helpButton.getIcon()
				.getIconWidth(), helpButton.getIcon().getIconHeight()));
		helpButton.setOpaque(false);
		helpButton.setBackground(Color.white);
		helpButton.setBorder(null);
		add(helpButton);

		pmenu = new JPopupMenu();
		JTextArea msg = new JTextArea(helpMessage);

		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		JScrollPane spane = new JScrollPane(msg);
		spane.setPreferredSize(new Dimension(width, height));
		spane.setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR));
		pmenu.add(spane);
		helpButton.setComponentPopupMenu(pmenu);
		helpButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				pmenu.show(helpButton, arg0.getPoint().x, arg0.getPoint().x);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
			}

			@Override
			public void mouseReleased(MouseEvent arg0) {
			}
		});

	}

	/**
	 * PRE: this panel is defined
	 * POST: an Ok button and a Cancel button have bee added to this panel's
	 * layout, they have been set to mnemonics of Enter and Esc respectively,
	 * and their action listeners have been set to call the ok() and cancel()
	 * methods
	 */
	protected void addOkCancel() {
		JButton button = new JButton("ok");
		button.setMnemonic(KeyEvent.VK_ENTER);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ok();
			}
		});
		button.setEnabled(true);
		add(button, BorderLayout.SOUTH);

		button = new JButton("cancel");
		button.setMnemonic(KeyEvent.VK_ESCAPE);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		button.setEnabled(true);
		add(button, BorderLayout.SOUTH);
	}

	/**
	 * SHOULD BE OVERRIDDEN WITH THE FUNCTIONALITY OF CLICKING THE OK BUTTON
	 */
	protected void ok() {
	}

	/**
	 * SHOULD BE OVERRIDDEN WITH THE FUNCTIONALITY OF CLICKING THE CANCEL BUTTON
	 */
	protected void cancel() {
	}

	/**
	 * PRE: click is defined
	 * POST: mode of the panel has been changed to MOVING, and the
	 * internalDistance has been set to the click's point
	 * 
	 * @param click
	 */
	public void startMoving(Point click) {
		mode = MODE.MOVING;
		internalDistance = new Dimension(click.x, click.y);
	}

	/**
	 * PRE: dragPoint, internalDistance, and this panel is general have been
	 * defined
	 * POST: the x and y of this panel have been changed so that their distance
	 * from dragPoint is equal to internalDistance, in each dimension
	 * 
	 * @param dragPoint
	 */
	public void move(Point dragPoint) {
		setBounds(getBounds().x + (dragPoint.x - internalDistance.width),
				getBounds().y + (dragPoint.y - internalDistance.height),
				getBounds().width, getBounds().height);
	}

	/**
	 * PRE: click is defined
	 * POST: internalDistance has now been changed to be a signifier of the
	 * direction of resizing. If the panel is being extended left, width = -1,
	 * if right, width = 1, if up, height = -1, if down height = 1
	 * 
	 * @param click
	 */
	public void startResizing(Point click) {
		mode = MODE.RESIZING;
		internalDistance = new Dimension(0, 0);
		// drag left edge
		if (click.x < FUDGEFACTOR) {
			internalDistance.width = -1;
		}
		// drag right edge
		else if (click.x - getBounds().width > -FUDGEFACTOR) {
			internalDistance.width = 1;
		}
		// only change x-axis
		if (click.y < FUDGEFACTOR) {
			internalDistance.height = -1;
		} else if (click.y - getBounds().height > -FUDGEFACTOR) {
			internalDistance.height = 1;
		}
	}

	/**
	 * PRE: dragPoint is defined
	 * POST: the bounds of the panel have been changed so that the panel edge
	 * being dragged is at the dragPoint and the rest of the panel edges are
	 * unmoved
	 * 
	 * @param dragPoint
	 */
	public void resize(Point dragPoint) {
		// REMEMBER: dragPoint is negative when dragging left/up...

		// dragging left
		if (internalDistance.width < 0) {
			setBounds(getBounds().x + dragPoint.x, getBounds().y,
					getBounds().width - dragPoint.x, getBounds().height);

		}
		// dragging right
		else if (internalDistance.width > 0) {
			setBounds(getBounds().x, getBounds().y, getBounds().width
					+ (dragPoint.x - getBounds().width), getBounds().height);

		}

		// dragging up
		if (internalDistance.height < 0) {
			setBounds(getBounds().x, getBounds().y + dragPoint.y,
					getBounds().width, getBounds().height - dragPoint.y);

		}

		// dragging Down
		else if (internalDistance.height > 0) {
			setBounds(getBounds().x, getBounds().y, getBounds().width,
					getBounds().height + (dragPoint.y - getBounds().height));
		}
		this.revalidate();

		if (helpButton != null) {
			helpButton.setPreferredSize(new Dimension(helpButton.getIcon()
					.getIconWidth(), helpButton.getIcon().getIconHeight()));
		}
	}

	/**
	 * PRE: event and this panel are defined
	 * POST: RV = true if event.point is within BORDER of one of the edges of
	 * this panel, false otherwise;
	 * 
	 * @param event
	 * @return
	 */
	private boolean clickedEdge(MouseEvent event) {
		Point click = event.getPoint();
		return (click.x < (5 + BORDER)
				|| click.x > getBounds().width - (5 + BORDER)
				|| click.y < (5 + BORDER) || click.y > getBounds().height
				- (5 + BORDER));
	}

	/**
	 * PRE: newBounds is defined
	 * POST: change the size of the panel to newBounds
	 * 
	 * @param newBounds
	 */
	public void changeSize(Rectangle newBounds) {
		changeSize(newBounds.x, newBounds.y, newBounds.width, newBounds.height);
	}

	/**
	 * PRE: newX, newY, newWidth, newHeight are defined
	 * POST: change the bounds of the panel to newX, newY, newWidth, newHeight;
	 * 
	 * @param newX
	 * @param newY
	 * @param newWidth
	 * @param newHeight
	 */
	public void changeSize(int newX, int newY, int newWidth, int newHeight) {
		setBounds(newX, newY, newWidth, newHeight);
	}

	protected void panelMouseClicked(MouseEvent e) {
	}

	protected void panelMouseEntered(MouseEvent e) {

	}

	protected void panelMouseExited(MouseEvent e) {

	}

	/**
	 * PRE: e is defined
	 * POST: if e was a click on the edge, then mode = RESIZING, otherwise mode
	 * = MOVING, and internalDistance has been appropriately changed in either
	 * case
	 * 
	 * @param e
	 */
	protected void panelMousePressed(MouseEvent e) {
		if (clickedEdge(e)) {
			startResizing(e.getPoint());
		} else {
			startMoving(e.getPoint());
		}
	}

	/**
	 * PRE: e is defined
	 * POST: mode = STATIC and internalDistance has been reset to 0,0
	 * 
	 * @param e
	 */
	protected void panelMouseReleased(MouseEvent e) {
		mode = MODE.STATIC;
		internalDistance = new Dimension(0, 0);
	}

	/**
	 * PRE: e is defined
	 * POST: if mode was MOVING, move(e) has been called. If mode was RESIZING
	 * resize(e) has been called
	 * 
	 * @param arg0
	 */
	protected void panelMouseDragged(MouseEvent e) {
		if (mode == MODE.MOVING) {
			move(e.getPoint());
		} else if (mode == MODE.RESIZING) {
			resize(e.getPoint());
		}
	}

	protected void panelMouseMoved(MouseEvent arg0) {

	}

	private class MouseHandler implements MouseListener, MouseMotionListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			panelMouseClicked(e);
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			requestFocus();
			if (hasFocus()) {
			}
			panelMouseEntered(e);

		}

		@Override
		public void mouseExited(MouseEvent e) {
			panelMouseExited(e);

		}

		@Override
		public void mousePressed(MouseEvent e) {
			panelMousePressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			panelMouseReleased(e);
		}

		@Override
		public void mouseDragged(MouseEvent arg0) {
			panelMouseDragged(arg0);

		}

		@Override
		public void mouseMoved(MouseEvent arg0) {
			panelMouseMoved(arg0);
		}

	}

	private void panelKeyPressed(KeyEvent press) {

	}

	private void panelKeyTyped(KeyEvent type) {

	}

	private void panelKeyReleased(KeyEvent release) {

	}

	private class KeyHandler implements KeyListener {

		@Override
		public void keyPressed(KeyEvent arg0) {
			panelKeyPressed(arg0);
		}

		@Override
		public void keyReleased(KeyEvent arg0) {
			panelKeyReleased(arg0);

		}

		@Override
		public void keyTyped(KeyEvent arg0) {
			panelKeyTyped(arg0);
		}

	}

}
