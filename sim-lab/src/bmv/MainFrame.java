package bmv;

import java.awt.Dimension;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * The main frame class of the PlanSimLab program, contains the panels for the
 * program
 * functions
 * 
 * @author plvines
 * 
 */
public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 968698930892108685L;
	// protected JFrame frame;
	protected static final String VERSION = "1.03";
	BMVManager holder;

	/**
	 * PRE: this program is not running
	 * POST: the MainFrame has been created, a content pane loaded into it, and
	 * all user execution has been completed and the frame has been closed with
	 * the program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		new MainFrame();
	}

	public MainFrame() {
		// Force default metal look and feel
		try {
			UIManager.setLookAndFeel(UIManager
					.getCrossPlatformLookAndFeelClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}

		// frame = new JFrame("PlantSimLab - v" + VERSION);
		setName("SimLab - v" + VERSION);
		setPreferredSize(new Dimension(1000, 800));
		holder = new BMVManager(this, getLayeredPane());
		holder.setOpaque(true);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addWindowListener(new WindowListener() {
			@Override
			public void windowOpened(WindowEvent e) {
			}

			@Override
			public void windowIconified(WindowEvent e) {
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				holder.saveCurrent();
				System.out.println("CLOSING");
				if (holder.model.getCurModel().modelWriteThread != null
						&& holder.model.getCurModel().modelWriteThread
								.isAlive()) {
					long startTime = System.currentTimeMillis();
					boolean messageAdded = false;

					// waits for the save to finish, if it takes longer than a
					// second a popup is displayed to assuage user fears
					while (holder.model.getCurModel().modelWriteThread != null
							&& holder.model.getCurModel().modelWriteThread
									.isAlive()) {
						if (startTime + 1000 <= System.currentTimeMillis()
								&& !messageAdded) {
							JOptionPane.showMessageDialog(holder,
									"Saving model...");
							messageAdded = true;
						}
					}

				}
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowActivated(WindowEvent e) {
			}
		});

		pack();
		setVisible(true);
		setResizable(true);

		Timer timer = new Timer();
		timer.schedule(new ForcePaint(), 100, 100);

	}

	protected void exit() {
	}

	private class ForcePaint extends TimerTask {

		@Override
		public void run() {
			holder.repaint();
		}

	}

}
