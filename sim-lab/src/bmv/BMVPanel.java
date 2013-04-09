package bmv;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Generic panel class for the BMV program. This class contains functionality
 * common to all three panel subtypes. This class is not meant to be
 * implemented, so it is practically abstract.
 * 
 * @author plvines
 * 
 */
public class BMVPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected static final int NOTHING = -1;

	protected static final Dimension BUTTON_SIZE = new Dimension(15, 15);

	protected static final int SPEED_DEC = 3, SPEED_INC = 4, PAUSE = 5,
			STATE_DEC = 6, STATE_INC = 7, CYCLE_DEC = 8, CYCLE_INC = 9,
			BARS = 10, LINES = 11, ADD_NODE_BUTTON = 3,
			ADD_ACT_EDGE_BUTTON = 4, ADD_INH_EDGE_BUTTON = 5,
			DELETE_BUTTON = 6, DRAW_BUTTON = 7, COLOR_BOX = 8,
			SIMULATE_MODEL_BUTTON = 9, KNOCKOUT_BUTTON = 3, RUN_EXP_BUTTON = 4,
			RUN_KO_BUTTON = 5, RUN_RANDOM_BUTTON = 6;

	protected static final Color BORDER_COLOR = new Color(0, 190, 240);
	protected static final Color TEXT_TITLE_COLOR = new Color(0, 160, 240);
	protected static final Dimension START_SIZE = new Dimension(600, 800);
	protected static final double ZOOM_IN_PERCENT = .5;

	protected ArrayList<GUIElement> staticGUI;
	protected ImageIcon bg;
	protected BMVManager parent;
	protected boolean popupUp;
	protected JMenuBar toolbar;
	protected Point oldMouse;
	protected Viewport zoomPort;
	protected boolean zoomingIn;
	protected JButton helpButton;
	protected JPopupMenu helpPopup;
	protected ScreenCapPanel screenCapPanel;
	protected FullNamePanel nameDisplayPanel;

	/*
	 * REFERENCE VARIABLES These variables point to objects that are used across
	 * the program, and proper functionality depends on all the BMVPanels have
	 * references to the same objects for these 4, so one should never create a
	 * "new" one of these. If a "new" model is wanted, a function such as
	 * model.becomeModel should be used so that the reference is maintained
	 */
	protected Viewport port;
	protected ModelHolder model;
	protected Trajectory trajectory;
	protected JLayeredPane layer;

	/**
	 * PRIMARY CONSTRUCTOR PRE: model, port, trajectory, and pLayer are all
	 * defined POST: this panel has been initialized with references to the
	 * parameters.
	 * 
	 * @param model
	 * @param port
	 * @param trajectory
	 * @param pLayer
	 */
	public BMVPanel(ModelHolder model, Viewport port, Trajectory trajectory,
			BMVManager pLayer) {

		this.model = model;
		this.trajectory = trajectory;
		this.port = port;
		MouseHandler mh = new MouseHandler();
		addMouseListener(mh);
		addMouseMotionListener(mh);
		// setBounds(0, 0, START_SIZE.width, START_SIZE.height);
		setPreferredSize(START_SIZE);
		parent = pLayer;
		layer = pLayer.layeredPanes;
		popupUp = false;
		zoomPort = null;
		initialize();
	}

	/**
	 * PRE: this object is uninitialized POST: this object's menu and GUI have
	 * been initialized
	 */
	protected void initialize() {
		initializeMenu();
		initializeGUI();
	}

	/**
	 * PRE: POST: activeGUI and staticGUI have been instantiated
	 */
	protected void initializeGUI() {
		staticGUI = new ArrayList<GUIElement>();

	}

	/**
	 * PRE: icons 206, 206, and 058 exist in "Resources/icons/16/" POST: toolbar
	 * has been initialized to contain the general panel functions: zoom in,
	 * zoom out, and screencapture
	 */
	protected void initializeMenu() {

		// <<< <<< TOOLBAR >>> >>>> //
		toolbar = new JMenuBar();
		ImageIcon icon = new ImageIcon("Resources/icons/16/205.png");
		JButton button = new JButton(icon);
		button.setToolTipText("Zoom In (Z)");
		button.setMnemonic(KeyEvent.VK_Z);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				zoomingIn = !zoomingIn;
				if (zoomPort == null) {
					zoomPort = new Viewport(port);
					zoomPort.zoom(ZOOM_IN_PERCENT);
				} else {
					zoomPort = null;
				}
			}
		});

		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);

		icon = new ImageIcon("Resources/icons/16/206.png");
		button = new JButton(icon);
		button.setToolTipText("Zoom Out (X)");
		button.setMnemonic(KeyEvent.VK_X);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				port.zoom(2);
				model.resize();
			}
		});
		button.setEnabled(true);
		toolbar.add(button);

		button.setPreferredSize(BUTTON_SIZE);
		icon = new ImageIcon("Resources/icons/16/058.png");
		button = new JButton(icon);
		button
				.setToolTipText("Screencapture the current view and save it in ScreenCaptures/name.png (P)");
		button.setMnemonic(KeyEvent.VK_P);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				promptScreenCapture();
			}
		});
		button.setEnabled(true);
		button.setPreferredSize(BUTTON_SIZE);
		toolbar.add(button);
		// <<< <<< TOOLBAR >>> >>>> //

		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);

	}

	/**
	 * PRE: screenCapPanel = null POST: screenCapPanel has been initialized and
	 * popup conditions set
	 */
	protected void promptScreenCapture() {
		screenCapPanel = new ScreenCapPanel(new Point(100, 100), this);
		nudgePanel(screenCapPanel);
		screenCapPanel.setOpaque(true);
		layer.add(screenCapPanel, new Integer(3));
		transferFocus();
		popupUp = true;
	}

	/**
	 * PRE: screenCapPanel != null POST: screenCapPanel has been removed from
	 * the layer and set to null, popup conditions have been reset and a new
	 * thread for generating the screencapture image has been started
	 * 
	 * @param name
	 */
	protected void screenCapture(String name, boolean blackAndWhite) {
		layer.remove(screenCapPanel);
		screenCapPanel = null;
		popupUp = false;
		if (name != null) {
			Thread screenCapThread = new Thread(new ScreenCapturer(name,
					blackAndWhite));
			screenCapThread.start();
		}
	}

	/**
	 * PRE: model is defined POST: RV = model
	 */
	public ModelHolder getModel() {
		return model;
	}

	/**
	 * PRE: model is defined POST: model = model
	 */
	public void setModel(ModelHolder model) {
		this.model = model;
	}

	/**
	 * Private class to paint this panel to a .png as a separate thread
	 * 
	 * @author plvines
	 * 
	 */
	private class ScreenCapturer implements Runnable {
		String name;
		boolean blackAndWhite;

		public ScreenCapturer(String name, boolean blackAndWhite) {
			super();
			this.blackAndWhite = blackAndWhite;
			this.name = name;
		}

		@Override
		public void run() {
			BufferedImage bi = new BufferedImage(layer.getWidth(), layer
					.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = bi.createGraphics();

			if (!blackAndWhite) {
				layer.paint(g);
			} else {
				ArrayList<Node> nodes = model.getNodes();
				ArrayList<Edge> edges = model.getEdges();
				ArrayList<Drawing> drawings = model.getDrawings();

				g.setColor(Color.white);
				g.fillRect(0, 0, layer.getWidth(), layer.getHeight());

				for (Drawing iterDrawing : drawings) {
					iterDrawing.paintBW((Graphics2D) g);
				}
				for (Edge iterEdge : edges) {
					iterEdge.paintBW(g);
				}
				for (Node iterNode : nodes) {
					iterNode.paintBW((Graphics2D) g);
				}
			}

			try {
				ImageIO.write(bi, "png", new File("ScreenCaptures/" + name
						+ ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * PRE: helpMessage, width, and height are defined. width and height <
	 * this.getSize().width and .height, icon 207 exists at
	 * "Resources/icons/16/" POST: this BMVPanel's toolbar has had a help
	 * message added in the form of a button with icon 207 which, when left or
	 * right clicked, produces a popup menu text area of size width and height
	 * with helpMessage on it
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
		helpButton.setPreferredSize(BUTTON_SIZE);
		toolbar.add(helpButton);

		helpPopup = new JPopupMenu();
		JTextArea msg = new JTextArea(helpMessage);

		msg.setEditable(false);
		msg.setLineWrap(true);
		msg.setWrapStyleWord(true);
		JScrollPane spane = new JScrollPane(msg);
		spane.setPreferredSize(new Dimension(width, height));
		spane.setBorder(BorderFactory.createLineBorder(BMVPanel.BORDER_COLOR));
		helpPopup.add(spane);
		helpButton.setComponentPopupMenu(helpPopup);
		helpButton.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent arg0) {
				helpPopup
						.show(helpButton, arg0.getPoint().x, arg0.getPoint().x);
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
	 * PRE: POST: panel variables which require updating when switching to the
	 * panel have been updated
	 */
	protected void update() {
	}

	/**
	 * paints the background and, if at the edge of the port, paints the port
	 * border in yellow
	 */
	public void paint(Graphics g) {
		super.paintComponent(g);
		setBackground(Color.white);
		g.drawImage(bg.getImage(), 0, 0, getSize().width, getSize().height, 0,
				0, 1200, 800, null);

		// draw boundaries
		Stroke origStroke = ((Graphics2D) g).getStroke();
		if (port.getPort().x < 2) {
			((Graphics2D) g).setStroke(new BasicStroke(5));
			g.setColor(Color.yellow);
			g.drawLine(2, 0, 2, getSize().height);
		}
		if (port.getPort().y < 2) {
			((Graphics2D) g).setStroke(new BasicStroke(5));
			g.setColor(Color.yellow);
			g.drawLine(0, 23, getSize().width, 23);
		}
		if (port.getPort().x + port.getPort().width >= model.getTotalSize().width - 2) {
			((Graphics2D) g).setStroke(new BasicStroke(5));
			g.setColor(Color.yellow);
			g.drawLine(getSize().width - parent.mainPane.getDividerLocation()
					- 18, 0, getSize().width
					- parent.mainPane.getDividerLocation() - 18,
					getSize().height);
		}
		if (port.getPort().y + port.getPort().height >= model.getTotalSize().height - 2) {
			((Graphics2D) g).setStroke(new BasicStroke(5));
			g.setColor(Color.yellow);
			g.drawLine(0, getSize().height - 8, getSize().width,
					getSize().height - 8);
		}
		((Graphics2D) g).setStroke(origStroke);

		// menuBackground.draw(g);
		for (int i = 0; i < model.getDrawings().size(); i++) {
			model.getDrawings().get(i).paint((Graphics2D) g);
		}
		for (int i = 0; i < model.getEdges().size(); i++) {
			model.getEdges().get(i).paint(g);
		}
		for (int i = 0; i < model.getNodes().size(); i++) {
			model.getNodes().get(i).paint((Graphics2D) g);
		}

		for (int i = 0; i < staticGUI.size(); i++) {
			if (staticGUI.get(i) != null) {
				staticGUI.get(i).draw(g);
			}
		}

		super.paintChildren(g);

		if (zoomPort != null) {
			((Graphics2D) g).setStroke(new BasicStroke(5));
			g.setColor(Color.yellow);
			Point corner = port.realToFrameCoord(new Point(
					zoomPort.getPort().x, zoomPort.getPort().y));
			Point dims = new Point(
					(int) (zoomPort.getPort().width * port.getScaleFactor()[0]),
					(int) (zoomPort.getPort().height * port.getScaleFactor()[1]));
			g.drawRect(corner.x, corner.y, dims.x, dims.y);
			((Graphics2D) g).setStroke(origStroke);
		}
	}

	/**
	 * PRE: POST: modelName and trajName GUI displays have been updated
	 */
	protected void updateNames() {
	}

	/**
	 * PRE: POST: all resizing required upon a change in panel size has occurred
	 */
	protected void resize() {
	}

	/**
	 * PRE: panel and this.bounds are defined POST: panel's bounds have been
	 * adjusted so that it is fully within this panel's bounds, if necessary
	 * 
	 * @param panel
	 */
	protected void nudgePanel(JPanel panel) {
		Rectangle box = panel.getBounds();
		if (box.x + box.width > getBounds().width) {
			box.x = getBounds().width - box.width;
		}
		if (box.y + box.height > getBounds().height - 30) {
			box.y = getBounds().height - box.height - 30;
		}
		if (box.x < 0) {
			box.x = 0;
		}
		if (box.y < 0) {
			box.y = 0;
		}
		panel.setBounds(box);
	}

	/**
	 * PRE: maxStates is defined POST: RV = maxStates
	 */
	public int getMaxStates() {
		return model.getMaxStates();
	}

	protected boolean dragged(MouseEvent e) {
		return false;
	}

	protected boolean moved(MouseEvent e) {
		boolean acted = false;
		if (zoomingIn) {
			zoomPort.movePortCenter(port.frameToRealCoord(e.getPoint()));
			acted = true;
		}
		return acted;
	}

	protected boolean clicked(MouseEvent e) {
		boolean acted = false;

		if (zoomingIn) {
			port.become(zoomPort);
			model.resize();
			zoomingIn = false;
			zoomPort = null;
			acted = true;
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			int nodeClicked = getNodeClicked(e);
			if (nodeClicked != NOTHING) {
				displayFullName(model.getNodes().get(nodeClicked));
			}
		}
		return acted;
	}

	/**
	 * PRE: click is defined, nodes is defined POST: if the click fell within
	 * any node, that node is now the node selected
	 * 
	 * @param click
	 * @return
	 */
	protected int getNodeClicked(MouseEvent click) {
		int clicked = NOTHING;
		for (int i = model.getNodes().size() - 1; clicked == -1 && i >= 0; i--) {
			if (model.getNodes().get(i).clicked(click.getPoint())) {
				clicked = i;
			}
		}
		return clicked;
	}

	protected void displayFullName(Node node) {
		nameDisplayPanel = new FullNamePanel(node, new Point(
				node.getPixPos().x - 20, node.getPixPos().y - 30));
		layer.add(nameDisplayPanel, new Integer(3));
	}

	protected boolean pressed(MouseEvent e) {
		if (nameDisplayPanel != null) {
			layer.remove(nameDisplayPanel);
			nameDisplayPanel = null;
		}
		oldMouse = e.getPoint();
		return false;
	}

	protected boolean released(MouseEvent e) {
		return false;
	}

	protected boolean entered(MouseEvent e) {
		return false;
	}

	protected boolean exited(MouseEvent e) {
		return false;
	}

	private class MouseHandler implements MouseListener, MouseMotionListener {

		@Override
		public void mouseDragged(MouseEvent e) {
			dragged(e);
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			moved(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			clicked(e);

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			entered(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			exited(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {

			pressed(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {

			released(e);
		}
	}
}
