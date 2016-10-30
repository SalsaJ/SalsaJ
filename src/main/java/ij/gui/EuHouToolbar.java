//EU_HOU
package ij.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.ImageObserver;
import java.io.File;
import ij.*;
import ij.plugin.frame.Recorder;
import ij.plugin.MacroInstaller;
import ij.plugin.filter.PlugInFilter;
import ij.process.Photometer;
import java.util.*;
import java.net.URL;

/**
 *  The ImageJ toolbar.
 *
 *@author     thomas
 *@created    12 juillet 2007
 */
public class EuHouToolbar extends Canvas implements MouseListener, MouseMotionListener {

	/**
	 *  Description of the Field
	 */
	public final static int OPEN = 0;
	/**
	 *  Description of the Field
	 */
	public final static int SAVE = 1;

	/**
	 *  Description of the Field
	 */
	public final static int UNDO = 2;
	/**
	 *  Description of the Field
	 */
	public final static int PLOT_PROFILE = 3;

	/**
	 *  Description of the Field
	 */
	public final static int BRIGHTNESS_CONTRAST = 4;

	/**
	 *  Description of the Field
	 */
	public final static int NONE = 100;

	private final static int NUM_TOOLS = 5;
	//EU_HOU ToolBar icone dimension
	private final static int SIZE = 42;
	private final static int SIZECORR = (SIZE - 22) / 2;
	private final static int OFFSET = 5;

	private Dimension ps = new Dimension((SIZE) * NUM_TOOLS, SIZE);
	private boolean[] down;
	private static int current;
	private int x, y;
	private int xOffset, yOffset;
	private long mouseDownTime;
	private Graphics g;
	private static EuHouToolbar instance;
	private String[] names = new String[NUM_TOOLS];
	private Image[] icons = new Image[NUM_TOOLS];
	private String[] command = {
			"Open"
			, "Save"
			, "Undo"
			, "PlotProfile"
			, addPluginItem("adjust01")
			};
	private int pc;
	private String icon;
	private MacroInstaller macroInstaller;

	private static Color foregroundColor = Prefs.getColor(Prefs.FCOLOR, Color.black);
	private static Color backgroundColor = Prefs.getColor(Prefs.BCOLOR, Color.white);
//EU_HOU ToolBar Color
	private Color gray = ImageJ.backgroundColor;
	private Color brighter = gray.brighter();
	private Color darker = new Color(246, 160, 60);
	private Color evenDarker = new Color(150, 100, 40);

	private ResourceBundle table;
	private boolean drag = false;

	private static Photometer photometer=null;


	/**
	 *  Adds a feature to the PluginItem attribute of the EuHouToolbar class
	 *
	 *@param  s  The feature to be added to the PluginItem attribute
	 *@return    Description of the Return Value
	 */
	public static String addPluginItem(String s) {
	String st = Prefs.getString(s);
	int index = st.lastIndexOf(',');

	//System.out.println("EuHouToolbar.addPluginItem index=" + index);

	String command = st.substring(1, index - 1);

		if (command.endsWith("]")) {
		int openBracket = command.lastIndexOf('[');

			if (openBracket > 0) {
				command = command.substring(0, openBracket);
			}
		}
		return command;
	}


	/**
	 *  Constructor for the EuHouToolbar object
	 */
	public EuHouToolbar() {
		down = new boolean[NUM_TOOLS];

	ClassLoader cl = this.getClass().getClassLoader();

		for (int i = 0; i < NUM_TOOLS; ++i) {
		URL path = cl.getResource("images/haut-" + i + ".png");

			if (path != null) {
				icons[i] = Toolkit.getDefaultToolkit().getImage(path);
			} else {
				path = cl.getResource("images/none.png");
				icons[i] = Toolkit.getDefaultToolkit().getImage(path);
			}
		}
		resetButtons();
		setForeground(foregroundColor);
		setBackground(gray);
		//setBackground(Color.red);
		addMouseListener(this);
		addMouseMotionListener(this);
		instance = this;
		table = ResourceBundle.getBundle("ij/i18n/ToolBundle", IJ.getLocale());
	}


	/**
	 *  Returns a reference to the ImageJ toolbar.
	 *
	 *@return    The instance value
	 */
	public static EuHouToolbar getInstance() {
		return instance;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	private void drawButtons(Graphics g) {
		for (int i = 0; i < NUM_TOOLS; i++) {
			drawButton(g, i);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g       Description of the Parameter
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 *@param  raised  Description of the Parameter
	 */
	private void fill3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
		if (raised) {
			g.setColor(gray);
		} else {
			g.setColor(darker);
		}
		g.fillRect(x + 1, y + 1, width - 1, height - 1);
		//g.setColor(raised ? brighter : evenDarker);
		g.setColor(brighter);
		g.drawLine(x, y, x, y + height-1);
		g.drawLine(x, y, x + width - 1, y);
		g.setColor(raised ? evenDarker : brighter);
		g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
		g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
                	}


	/**
	 *  Description of the Method
	 *
	 *@param  g     Description of the Parameter
	 *@param  tool  Description of the Parameter
	 */
	private void drawButton(Graphics g, int tool) {
		fill3DRect(g, tool * SIZE + 1, 1, SIZE, SIZE, !down[tool]);
		g.setColor(Color.black);

	int x = tool * SIZE + OFFSET;
	int y = OFFSET;

		if (down[tool]) {
			x++;
			y++;
		}
		this.g = g;
		if (icons[tool] != null) {
			g.drawImage(icons[tool], x, y, this);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  tool  Description of the Parameter
	 */
	private void showMessage(int tool) {

		switch (tool) {

						case OPEN:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("OpenHint"));
							return;
						case SAVE:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("SaveHint"));
							return;
						case UNDO:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("UndoHint"));
							return;
						case PLOT_PROFILE:
							//EU_HOU Bundle
							IJ.showStatus(IJ.getBundle().getString("PlotProfile"));
							return;
						case BRIGHTNESS_CONTRAST:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("BrightnessContrastHint"));
							return;
						default:
							IJ.showStatus("");
							return;
		}
	}


	/**
	 *  Description of the Method
	 */
	private void resetButtons() {
		for (int i = 0; i < NUM_TOOLS; i++) {
			down[i] = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		drawButtons(g);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void update(Graphics g) {
		for (int i = 0; i < NUM_TOOLS; i++) {
			if (g.hitClip(i * SIZE, 0, SIZE, SIZE)) {
				drawButton(g, i);
			}
		}
	}


	/**
	 *  Sets the tool attribute of the EuHouToolbar object
	 *
	 *@param  tool  The new tool value
	 */
	public void setTool(int tool) {
		if (tool < 0 || tool >= NUM_TOOLS) {
			return;
		}
		setTool2(tool);
	}


	/**
	 *  Sets the tool2 attribute of the EuHouToolbar object
	 *
	 *@param  tool  The new tool2 value
	 */
	private void setTool2(int tool) {
		if (tool < 0 || tool >= NUM_TOOLS) {
			return;
		}
		current = tool;
// 	down[current] = true;
// 	Graphics g = this.getGraphics();
//	drawButton(g, current);
//	g.dispose();
		showMessage(current);

	Hashtable table = Menus.getCommands();
	String Cmd;

		if (command[tool] != null) {
			try {
				//EU_HOU Bundle
				Cmd = IJ.getBundle().getString(command[tool]);
			} catch (MissingResourceException e) {
				Cmd = command[tool];
			}
			//System.out.println(Cmd);
			if (Cmd == "options01") {
				IJ.doCommand(Prefs.getCommand(Cmd));
			} else {
				IJ.doCommand(Cmd);
			}
			if (Recorder.record && (tool != OPEN)) {
				Recorder.record(Cmd);
			}
		}
		if (IJ.isMacOSX()) {
			repaint();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) {
		if (drag) {
			return;
		}
	int x = e.getX();
	int newTool = x / SIZE;

		if (newTool > BRIGHTNESS_CONTRAST) {
			return;
		}
	boolean doubleClick = newTool == current && (System.currentTimeMillis() - mouseDownTime) <= 500;

		mouseDownTime = System.currentTimeMillis();
		if (!doubleClick) {
			setTool2(newTool);

		}

		down[newTool] = false;

	Graphics g = getGraphics();

		drawButton(g, newTool);
		g.dispose();

	}



	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) {
		drag = false;

	int x = e.getX();

		x /= SIZE;
		if (x > BRIGHTNESS_CONTRAST) {
			return;
		} else {
			down[x] = !down[x];

		Graphics g = getGraphics();

			drawButton(g, x);

		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseExited(MouseEvent e) {
		showMessage(NONE);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseDragged(MouseEvent e) {
		drag = true;

	int x = e.getX();

		x /= SIZE;
		if (x > BRIGHTNESS_CONTRAST) {
			return;
		}
		down[x] = false;

	Graphics g = getGraphics();

		drawButton(g, x);
		g.dispose();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseMoved(MouseEvent e) {
	int x = e.getX();

		x /= SIZE;
		showMessage(x);

	}


	/**
	 *  Gets the size attribute of the EuHouToolbar object
	 *
	 *@return    The size value
	 */
	public Dimension getSize() {
		return ps;
	}


	/**
	 *  Gets the preferredSize attribute of the EuHouToolbar object
	 *
	 *@return    The preferredSize value
	 */
	public Dimension getPreferredSize() {
		return ps;
	}


	/**
	 *  Gets the minimumSize attribute of the EuHouToolbar object
	 *
	 *@return    The minimumSize value
	 */
	public Dimension getMinimumSize() {
		return ps;
	}


	/**
	 *  Description of the Method
	 */
	public void photometerButtonAction() {
            IJ.setTool(Toolbar.LINE);
            Enumeration en = WindowManager.getImageWindows().elements();
            photometer=Photometer.getInstance();
            if(photometer==null)
		photometer = new Photometer();
            else
		photometer.activate();

            while (en.hasMoreElements()) {
		ImageCanvas c = ((ImageWindow) en.nextElement()).getCanvas();
			c.addMouseListener(photometer);
		}

	}
        


	/**
	 *  Description of the Method
	 */
	public void settingsAction() {
	//down[PHOTOSETTINGS] = !down[PHOTOSETTINGS];
	//Graphics gr = getGraphics();
	//	drawButton(gr, PHOTOSETTINGS);
	PhotometerParams s = PhotometerParams.getInstance();
		if (s == null) {
			s = new PhotometerParams();
		} else {
                        s.show();
		}
	}


	/**
	 *  Description of the Method
	 */
	public static void clearPhotometer() {
        if (Photometer.getInstance()!=null)
		Photometer.clear();
	}

}

