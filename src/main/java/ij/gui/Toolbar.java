//EU_HOU
package ij.gui;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.*;
import java.io.File;
import java.util.Hashtable;
import ij.*;
import ij.plugin.frame.Recorder;
import ij.plugin.frame.Editor;
import ij.plugin.MacroInstaller;
import ij.macro.Program;
/*
 *  EU_HOU CHANGES
 */
import java.util.ResourceBundle;
import java.net.URL;
/*
 *  EU_HOU END
 */
/**
 *  The SalsaJ toolbar.
 *
 * @author     Thomas
 * @created    6 novembre 2007
 */
public class Toolbar extends Canvas implements MouseListener, MouseMotionListener, ItemListener, ActionListener {
    


	/**
	 *  Description of the Field
	 */
	public final static int LINE = 0;
	/**
	 *  Description of the Field
	 */
	public final static int POLYLINE = 1;
	/**
	 *  Description of the Field
	 */
	public final static int FREELINE = 2;
        /**
	 *  ** Description of the Field
	 */
	public final static int RECTANGLE = 3;
	/**
	 *  Description of the Field
	 */
	public final static int OVAL = 4;
	/**
	 *  Description of the Field
	 */
	public final static int POLYGON = 5;

	/**
	 *  Description of the Field
	 */
	public final static int MAGNIFIER = 6;
	/**
	 *  Description of the Field
	 */
	public final static int HAND = 7;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE1 = 8;

	/**
	 *  Description of the Field
	 */
	public final static int SPARE2 = 9;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE3 = 10;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE4 = 11;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE5 = 12;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE6 = 13;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE7 = 14;
	/**
	 *  Description of the Field
	 */
	public final static int SPARE8 = 15;
        /**
	 *  Description of the Field
	 */
	public final static int SPARE9 = 16;
	/**
	 *  Description of the Field
	 */
	public final static int DOUBLE_CLICK_THRESHOLD = 650;

	private final static int NUM_TOOLS = 17;
	private final static int NUM_BUTTONS = 17;
	//EU_HOU ToolBar icone dimension
	private final static int SIZE = 42;
	private final static int OFFSET = 5;// sert a centrer les icones (icones 32*32 et bouton 42*42 donc 5 de chq coté
	private final static String BRUSH_SIZE = "toolbar.brush.size";
	//EU_HOU ToolBar dimension
	private Dimension ps = new Dimension((SIZE) * 13 +100, SIZE+5);
	private boolean[] down;
	private static int current;
	private int previous;
	private int x, y;
	private int xOffset, yOffset;
	private long mouseDownTime;
	private Graphics g;
        private Graphics gbis;
	private static Toolbar instance;
	private int mpPrevious = LINE;
	private String[] names = new String[NUM_TOOLS];
	private String[] icons = new String[NUM_TOOLS];
	//EU_HOU ToolBar icone
	private Image imgs[] = new Image[NUM_TOOLS];
	private PopupMenu[] menus = new PopupMenu[NUM_TOOLS];
	private int pc;
	private String icon;
	private MacroInstaller macroInstaller;
	private int startupTime;
	//EU_HOU multibouton
	private PopupMenu drawPopup, linePopup, switchPopup;
	//EU_HOU multibouton
	private CheckboxMenuItem ovalItem, rectangleItem, polygonItem;
	private CheckboxMenuItem straightLineItem, polyLineItem, freeLineItem, profileLineItem;
	private String currentSet = "Startup Macros";

	private static Color foregroundColor = Prefs.getColor(Prefs.FCOLOR, Color.black);
	private static Color backgroundColor = Prefs.getColor(Prefs.BCOLOR, Color.white);
	private static boolean brushEnabled;
	private static int brushSize = (int) Prefs.get(BRUSH_SIZE, 15);
	private int lineType = LINE;
	//EU_HOU multibouton
	private int drawType = RECTANGLE;
	//EU_HOU ToolBar Color
	private Color gray = ImageJ.backgroundColor;
	private Color brighter = gray.brighter();
	private Color darker = new Color(246, 160, 60);
	private Color evenDarker = new Color(150, 100, 40);
	private Color triangleColor = new Color(250, 100, 40);
	/*
	 *  EU_HOU CHANGES
	 */
	private ResourceBundle table;
	private boolean drag = false;


	/*
	 *  EU_HOU END
	 */
	/**
	 *  Constructor for the Toolbar object
	 */
	public Toolbar() {
		down = new boolean[NUM_TOOLS];

		/*
		 *  EU_HOU CHANGES
		 */
		ClassLoader cl = this.getClass().getClassLoader();
		//EU_HOU ToolBar icone
		for (int i = 0; i < NUM_TOOLS; ++i) {
			URL path = cl.getResource("images/bas-" + i + ".png");
			//System.out.println("path" + i + ":" + path);
			if (path != null) {
				imgs[i] = Toolkit.getDefaultToolkit().getImage(path);
			}
                        else {
				path = cl.getResource("images/none.png");
				imgs[i] = Toolkit.getDefaultToolkit().getImage(path);
			}
			//System.out.println("imgs[" + i + "]=" + imgs[i] + "path" + i + ":" + path);
		}
		/*
		 *  EU_HOU END
		 */
		resetButtons();
		down[0] = true;
		setForeground(Color.black);
		setBackground(gray);
		addMouseListener(this);
		addMouseMotionListener(this);
		instance = this;
		/*
		 *  EU_HOU CHANGES
		 */
		table = ResourceBundle.getBundle("ij/i18n/ToolBundle", IJ.getLocale());
		//EU_HOU Bundle
		names[NUM_TOOLS - 1] = "Switch to alternate macro tool sets";
		//icons[NUM_TOOLS - 1] = "C900T1c12>T7c12>";// ">>"
		icons[NUM_TOOLS - 1] = "Cf60T5c15>Tcc15>";// ">>"
		addPopupMenus();
		if (IJ.isMacOSX() || IJ.isVista()) {
			Prefs.antialiasedTools = true;
		}
		/*
		 *  EU_HOU END
		 */
	}


	/**
	 *  Adds a feature to the PopupMenus attribute of the Toolbar object
	 */
	//EU_HOU multibouton
	void addPopupMenus() {
		//System.out.println("addPopupMenus");

		linePopup = new PopupMenu();
		if (Menus.getFontSize() != 0) {
			linePopup.setFont(Menus.getFont());
		}
		//EU_HOU Bundle
		straightLineItem = new CheckboxMenuItem(IJ.getBundle().getString("StraightLines"), lineType == LINE);
		straightLineItem.addItemListener(this);
		linePopup.add(straightLineItem);
		//EU_HOU Bundle
		polyLineItem = new CheckboxMenuItem(IJ.getBundle().getString("SegmentedLines"), lineType == POLYLINE);
		polyLineItem.addItemListener(this);
		linePopup.add(polyLineItem);
		//EU_HOU Bundle
		freeLineItem = new CheckboxMenuItem(IJ.getBundle().getString("FreehandLines"), lineType == FREELINE);
		freeLineItem.addItemListener(this);
		linePopup.add(freeLineItem);
                add(linePopup);
		//EU_HOU Add
		drawPopup = new PopupMenu();
		if (Menus.getFontSize() != 0) {
			drawPopup.setFont(Menus.getFont());
		}
		//EU_HOU Bundle
		rectangleItem = new CheckboxMenuItem(IJ.getBundle().getString("Rectangle"), drawType == RECTANGLE);
		rectangleItem.addItemListener(this);
		drawPopup.add(rectangleItem);
		//EU_HOU Bundle
		ovalItem = new CheckboxMenuItem(IJ.getBundle().getString("Oval"), drawType == OVAL);
		ovalItem.addItemListener(this);
		drawPopup.add(ovalItem);
		//EU_HOU Bundle
		polygonItem = new CheckboxMenuItem(IJ.getBundle().getString("Polygon"), drawType == POLYGON);
		polygonItem.addItemListener(this);
		drawPopup.add(polygonItem);
		add(drawPopup);
		//EU_HOU End
		switchPopup = new PopupMenu();
		if (Menus.getFontSize() != 0) {
			switchPopup.setFont(Menus.getFont());
		}
		add(switchPopup);
	}


	/**
	 *  Returns the ID of the current tool (Toolbar.RECTANGLE, Toolbar.OVAL, etc.).
	 *
	 * @return    The toolId value
	 */
	public static int getToolId() {
		//System.out.println("getToolId");
		return current;
	}


	/**
	 *  Returns the ID of the tool whose name (the description displayed in the
	 *  status bar) starts with the specified string, or -1 if the tool is not
	 *  found.
	 *
	 * @param  name  Description of the Parameter
	 * @return       The toolId value
	 */
	public int getToolId(String name) {
		//System.out.println("getToolId" + name);

		int tool = -1;

		for (int i = 0; i <= SPARE9; i++) {
			if (names[i] != null && names[i].startsWith(name)) {
				tool = i;
				break;
			}
		}
		return tool;
	}


	/**
	 *  Returns a reference to the ImageJ toolbar.
	 *
	 * @return    The instance value
	 */
	public static Toolbar getInstance() {
		//System.out.println("getInstance");
		return instance;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	//EU_HOU Changes
	private void drawButtons(Graphics g) {
		//System.out.println("drawButtons");
		if (Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		/*
		 *  for (int i = 0; i < LINE; i++) {
		 *  drawButton(g, i);
		 *  }
		 */
		drawButton(g, lineType);
		drawButton(g, drawType);
		for (int i = MAGNIFIER; i < NUM_BUTTONS; i++) {
			//System.out.println("drawButtons bouton nb=" + i);
			drawButton(g, i);
		}

	}


	/**
	 *  Description of the Method
	 *
	 * @param  g       Description of the Parameter
	 * @param  x       Description of the Parameter
	 * @param  y       Description of the Parameter
	 * @param  width   Description of the Parameter
	 * @param  height  Description of the Parameter
	 * @param  raised  Description of the Parameter
	 */
	private void fill3DRect(Graphics g, int x, int y, int width, int height, boolean raised) {
		//System.out.println("fill3DRect" + raised);
		if (null == g) {
			return;
		}
		if (raised) {
			g.setColor(gray);
		} else {
			g.setColor(darker);
		}
		//g.fillRect(x + 1, y + 1, width - 2, height - 2);
		/*
		 *  EU_HOU CHANGES
		 */
		g.fillRect(x + 1, y + 1, width - 1, height - 1);
		//g.setColor(raised ? brighter : evenDarker);
		g.setColor(brighter);
		g.drawLine(x, y, x, y + height - 1);
		g.drawLine(x, y, x + width - 1, y);
		g.setColor(raised ? evenDarker : brighter);
		g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);
		g.drawLine(x + width - 1, y, x + width - 1, y + height - 1);
		/*
		 *  EU_HOU END
		 */
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g     Description of the Parameter
	 * @param  tool  Description of the Parameter
	 */
	private void drawButton(Graphics g, int tool) {
		//System.out.println("drawButton tool=" + tool);
		if (g == null) {
			return;
		}
		int index = toolIndex(tool);
		//System.out.println("drawButton index=" + index);
		/*
		 *  EU_HOU CHANGES
		 */
		fill3DRect(g, index * SIZE + 1, 1, SIZE, SIZE, !down[tool]);
		g.setColor(Color.black);

		int x = index * SIZE + OFFSET + 5;
		int y = OFFSET + 10;

		/*
		 *  EU_HOU END
		 */
		if (down[tool]) {
			x++;
			y++;
		}
		this.g = g;
		//if (tool >= SPARE1 && tool <= SPARE9 && icons[tool] != null) {
		if (tool >= SPARE1 && tool <= SPARE9 && icons[tool] != null) {
			drawIcon(g, tool, x, y);
			//drawIcon(g, icons[tool], x, y);
			return;
		}
		/*
		 *  EU_HOU CHANGES
		 */
		//EU_HOU ToolBar icone
		if (imgs[tool] == null) {
			imgs[tool] = createImage(SIZE - 2, SIZE - 2);

			Graphics gr = imgs[tool].getGraphics();

			/*
			 *  EU_HOU END
			 */
			switch (tool) {
							case LINE:
								xOffset = x + 3;
								yOffset = y + 8;
								m(0, 10);
								d(16, 4, gr);
								drawTriangle(11, 13, gr);
								return;
							case POLYLINE:
								xOffset = x + 5;
								yOffset = y + 7;
								m(14, 6);
								d(11, 3, gr);
								d(1, 3, gr);
								d(1, 4, gr);
								d(6, 9, gr);
								d(2, 13, gr);
								drawTriangle(11, 13, gr);
								return;
							case FREELINE:
								xOffset = x + 5;
								yOffset = y + 7;
								m(16, 4);
								d(14, 6, gr);
								d(12, 6, gr);
								d(9, 3, gr);
								d(8, 3, gr);
								d(6, 7, gr);
								d(2, 11, gr);
								d(1, 11, gr);
								drawTriangle(11, 13, gr);
								return;
							case MAGNIFIER:
								xOffset = x + 5;
								yOffset = y + 5;
								m(3, 0);
								d(3, 0, gr);
								d(5, 0, gr);
								d(8, 3, gr);
								d(8, 5, gr);
								d(7, 6, gr);
								d(7, 7, gr);
								d(6, 7, gr);
								d(5, 8, gr);
								d(3, 8, gr);
								d(0, 5, gr);
								d(0, 3, gr);
								d(3, 0, gr);
								m(8, 8);
								d(9, 8, gr);
								d(13, 12, gr);
								d(13, 13, gr);
								d(12, 13, gr);
								d(8, 9, gr);
								d(8, 8, gr);
								return;
							case HAND:
								xOffset = x + 5;
								yOffset = y + 4;
								m(5, 14);
								d(2, 11, gr);
								d(2, 10, gr);
								d(0, 8, gr);
								d(0, 7, gr);
								d(1, 6, gr);
								d(2, 6, gr);
								d(4, 8, gr);
								d(4, 6, gr);
								d(3, 5, gr);
								d(3, 4, gr);
								d(2, 3, gr);
								d(2, 2, gr);
								d(3, 1, gr);
								d(4, 1, gr);
								d(5, 2, gr);
								d(5, 3, gr);
								m(6, 5);
								d(6, 1, gr);
								d(7, 0, gr);
								d(8, 0, gr);
								d(9, 1, gr);
								d(9, 5, gr);
								m(9, 1);
								d(11, 1, gr);
								d(12, 2, gr);
								d(12, 6, gr);
								m(13, 4);
								d(14, 3, gr);
								d(15, 4, gr);
								d(15, 7, gr);
								d(14, 8, gr);
								d(14, 10, gr);
								d(13, 11, gr);
								d(13, 12, gr);
								d(12, 13, gr);
								d(12, 14, gr);
								return;
			}
		}
		//EU_HOU ToolBar icone
		g.drawImage(imgs[tool], index * SIZE + 5, 5, this);
	}


	/*
	 *  EU_HOU END
	 */
	/**
	 *  Description of the Method
	 *
	 * @param  x   Description of the Parameter
	 * @param  y   Description of the Parameter
	 * @param  gr  Description of the Parameter
	 */
	void drawTriangle(int x, int y, Graphics gr) {
		g.setColor(triangleColor);
		xOffset += x;
		yOffset += y;
		m(0, 0);
		d(4, 0, gr);
		m(1, 1);
		d(3, 1, gr);
		dot(2, 2);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g     Description of the Parameter
	 * @param  tool  Description of the Parameter
	 * @param  x     Description of the Parameter
	 * @param  y     Description of the Parameter
	 */
	//permet de dessiner les icones des macros
	void drawIcon(Graphics g, int tool, int x, int y) {
		//System.out.println("drawIcon");
		if (null == g) {
			return;
		}
		icon = icons[tool];
		this.icon = icon;

		int length = icon.length();
		int x1;
		int y1;
		int x2;
		int y2;

		pc = 0;
		while (true) {
			char command = icon.charAt(pc++);
			////System.out.println("drawIcon command=" + command);
			if (pc >= length) {
				break;
			}
			switch (command) {
							case 'B':
								x += v();
								y += v();
								break;// reset base
							case 'R':
								g.drawRect(x + v(), y + v(), v(), v());
								break;// rectangle
							case 'F':
								g.fillRect(x + v(), y + v(), v(), v());
								break;// filled rectangle
							case 'O':
								g.drawOval(x + v(), y + v(), v(), v());
								break;// oval
							case 'o':
								g.fillOval(x + v(), y + v(), v(), v());
								break;// filled oval
							case 'C':
								g.setColor(new Color(v() * 16, v() * 16, v() * 16));
								break;// set color
							case 'L':
								g.drawLine(x + v(), y + v(), x + v(), y + v());
								break;// line
							case 'D':
								g.fillRect(x + v(), y + v(), 1, 1);
								break;// dot
							case 'P':// polyline
								x1 = x + v();
								y1 = y + v();
								while (true) {
									x2 = v();
									if (x2 == 0) {
										break;
									}
									y2 = v();
									if (y2 == 0) {
										break;
									}
									x2 += x;
									y2 += y;
									g.drawLine(x1, y1, x2, y2);
									x1 = x2;
									y1 = y2;
								}
								break;
							case 'T':// text (one character)
								x2 = x + v();

								y2 = y + v();
								////System.out.println("x2=" + (x2 - x) + " ,y2=" + (y2 - y));
								int size = v() * 10 + v();
								////System.out.println("size=" + size);
								char[] c = new char[1];

								c[0] = pc < icon.length() ? icon.charAt(pc++) : 'e';
								g.setFont(new Font("SansSerif", Font.BOLD, size));
								g.drawString(new String(c), x2, y2);
								break;
							default:
								break;
			}
			if (pc >= length) {
				break;
			}
		}
		if (menus[tool] != null && menus[tool].getItemCount() > 0) {
			xOffset = x;
			yOffset = y;
			drawTriangle(13, 14, g);
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @return    Description of the Return Value
	 */
	int v() {
		if (pc >= icon.length()) {
			return 0;
		}
		char c = icon.charAt(pc++);
		//IJ.log("v: "+pc+" "+c+" "+toInt(c));
		switch (c) {
						case '0':
							return 0;
						case '1':
							return 1;
						case '2':
							return 2;
						case '3':
							return 3;
						case '4':
							return 4;
						case '5':
							return 5;
						case '6':
							return 6;
						case '7':
							return 7;
						case '8':
							return 8;
						case '9':
							return 9;
						case 'a':
							return 10;
						case 'b':
							return 11;
						case 'c':
							return 12;
						case 'd':
							return 13;
						case 'e':
							return 14;
						case 'f':
							return 15;
						default:
							return 0;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tool  Description of the Parameter
	 */
	private void showMessage(int tool) {
		if (tool >= SPARE1 && tool <= SPARE9 && names[tool] != null) {
			String name = names[tool];
			int index = name.indexOf("Action Tool");

			if (index != -1) {
				name = name.substring(0, index);
			} else {
				index = name.indexOf("Menu Tool");
				if (index != -1) {
					name = name.substring(0, index + 4);
				}
			}
			IJ.showStatus(name);
			return;
		}
		switch (tool) {
						case LINE:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("StraightSel"));
							return;
						case POLYLINE:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("SegSel"));
							return;
						case FREELINE:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("FreeLineSel"));
							return;
						case MAGNIFIER:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("MagTool"));
							return;
						case HAND:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("ScrollTool"));
							return;
						case RECTANGLE:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("RectSel"));
							return;
						case OVAL:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("OvalSel"));
							return;
						case POLYGON:
							//EU_HOU Bundle
							IJ.showStatus(table.getString("PolySel"));
							return;
						default:
							IJ.showStatus("");
							return;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  x  Description of the Parameter
	 * @param  y  Description of the Parameter
	 */
	private void m(int x, int y) {
		this.x = xOffset + x;
		this.y = yOffset + y;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  x   Description of the Parameter
	 * @param  y   Description of the Parameter
	 * @param  gr  Description of the Parameter
	 */
	private void d(int x, int y, Graphics gr) {
		x += xOffset;
		y += yOffset;
		gr.drawLine(this.x, this.y, x, y);
		this.x = x;
		this.y = y;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  x  Description of the Parameter
	 * @param  y  Description of the Parameter
	 */
	private void dot(int x, int y) {
		g.fillRect(x + xOffset, y + yOffset, 1, 1);
	}


	/**
	 *  Description of the Method
	 */
	private void resetButtons() {
		//System.out.println("resetButtons");
		for (int i = 0; i < NUM_TOOLS; i++) {
			down[i] = false;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		//System.out.println("paint");
		if (null == g) {
			return;
		}
		drawButtons(g);
	}


	/**
	 *  Sets the tool attribute of the Toolbar object
	 *
	 * @param  tool  The new tool value
	 */
	public void setTool(int tool) {
		//System.out.println("setTool" + tool);
		if (tool == current || tool < 0 || tool >= NUM_TOOLS) {
			return;
		}
		//if (tool == SPARE1 || (tool >= SPARE2 && tool <= SPARE9)) {
		if ((tool >= SPARE2 && tool <= SPARE9)) {
			if (names[tool] == null) {
				names[tool] = "Spare tool";
			}// enable tool
			if (names[tool].indexOf("Action Tool") != -1) {
				return;
			}
		}
		if (isLine(tool)) {
			lineType = tool;
		}
		//EU_HOU multibouton
		if (isDraw(tool)) {
			drawType = tool;
		}
		setTool2(tool);
	}


	/**
	 *  Sets the tool2 attribute of the Toolbar object
	 *
	 * @param  tool  The new tool2 value
	 */
	private void setTool2(int tool) {
		//System.out.println("setTool2" + tool);
		if (tool == current || !isValidTool(tool)) {
			return;
		}
		current = tool;
		down[current] = true;
		down[previous] = false;

		Graphics g = this.getGraphics();

		if (Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, previous);
		drawButton(g, current);
		if (null == g) {
			return;
		}
		g.dispose();
		showMessage(current);
		previous = current;
		if (Recorder.record) {
			Recorder.record("setTool", current);
		}
		if (IJ.isMacOSX()) {
			repaint();
		}
	}


	/**
	 *  Gets the validTool attribute of the Toolbar object
	 *
	 * @param  tool  Description of the Parameter
	 * @return       The validTool value
	 */
	boolean isValidTool(int tool) {
		//System.out.println("isValidTool" + tool);
		if (tool < 0 || tool >= NUM_TOOLS) {
			return false;
		}
		if ((tool == SPARE1 || (tool >= SPARE2 && tool <= SPARE9)) && names[tool] == null) {
			return false;
		}
		return true;
	}


	/**
	 *  Obsolete. Use getForegroundColor().
	 *
	 * @return    The color value
	 */
	public Color getColor() {
		return foregroundColor;
	}


	/**
	 *  Obsolete. Use setForegroundColor().
	 *
	 * @param  c  The new color value
	 */
	public void setColor(Color c) {
		if (c != null) {
			foregroundColor = c;
			//drawButton(this.getGraphics(), DROPPER);
		}
	}


	/**
	 *  Gets the foregroundColor attribute of the Toolbar class
	 *
	 * @return    The foregroundColor value
	 */
	public static Color getForegroundColor() {
		return foregroundColor;
	}


	/**
	 *  Sets the foregroundColor attribute of the Toolbar class
	 *
	 * @param  c  The new foregroundColor value
	 */
	public static void setForegroundColor(Color c) {
		if (c != null) {
			foregroundColor = c;
			//repaintTool(DROPPER);
		}
	}


	/**
	 *  Gets the backgroundColor attribute of the Toolbar class
	 *
	 * @return    The backgroundColor value
	 */
	public static Color getBackgroundColor() {
		return backgroundColor;
	}


	/**
	 *  Sets the backgroundColor attribute of the Toolbar class
	 *
	 * @param  c  The new backgroundColor value
	 */
	public static void setBackgroundColor(Color c) {
		if (c != null) {
			backgroundColor = c;
			//repaintTool(DROPPER);
		}
	}


	/**
	 *  Gets the brushSize attribute of the Toolbar class
	 *
	 * @return    The brushSize value
	 */
	public static int getBrushSize() {
		if (brushEnabled) {
			return brushSize;
		} else {
			return 0;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tool  Description of the Parameter
	 */
	static void repaintTool(int tool) {
		//System.out.println("repaintTool" + tool);
		if (IJ.getInstance() != null) {
			Toolbar tb = getInstance();
			Graphics g = tb.getGraphics();

			if (Prefs.antialiasedTools) {
				((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			}
			tb.drawButton(g, tool);
			if (g != null) {
				g.dispose();
			}
		}
		//Toolbar tb = getInstance();
		//tb.repaint(tool * SIZE , 0, SIZE, SIZE);
	}

	// Returns the toolbar position index of the specified tool
	/**
	 *  Description of the Method
	 *
	 * @param  tool  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	//EU_HOU Changes
	int toolIndex(int tool) {
		switch (tool) {
						//EU_HOU multibouton
						case LINE:
							return 0;
						case POLYLINE:
							return 0;
						case FREELINE:
							return 0;
                                                case RECTANGLE:
							return 1;
						case OVAL:
							return 1;
						case POLYGON:
							return 1;
						case MAGNIFIER:
							return 2;
						case HAND:
							return 3;
						case SPARE1:
							return 4;
						default:
							return tool - 4;
		}
	}

	// Returns the tool corresponding to the specified tool position index
	/**
	 *  Description of the Method
	 *
	 * @param  index  Description of the Parameter
	 * @return        Description of the Return Value
	 */
	//EU_HOU Changes
	int toolID(int index) {
		switch (index) {
						//EU_HOU multibouton
						case 0:
							return lineType;
						case 1:
							return drawType;
						case 2:
							return MAGNIFIER;
						case 3:
							return HAND;
						case 4:
							return SPARE1;
						default:
							return index + 4;
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) {
		/*
		 *  EU_HOU CHANGES
		 */
		if (drag) {
			return;
		}
		/*
		 *  EU_HOU END
		 */
		int x = e.getX();
		int newTool = 0;

		for (int i = 0; i < NUM_BUTTONS; i++) {
			if (x > i * SIZE && x < i * SIZE + SIZE) {
				newTool = toolID(i);
			}
		}

		if (newTool == SPARE9) {
			showSwitchPopupMenu(e);
			return;
		}

		if (!isValidTool(newTool)) {
			return;
		}
		if (menus[newTool] != null && menus[newTool].getItemCount() > 0) {
			menus[newTool].show(e.getComponent(), e.getX(), e.getY());
			return;
		}
		boolean doubleClick = newTool == current && (System.currentTimeMillis() - mouseDownTime) <= DOUBLE_CLICK_THRESHOLD;

		mouseDownTime = System.currentTimeMillis();
		if (!doubleClick) {
			mpPrevious = current;
			if (isMacroTool(newTool)) {
				String name = names[newTool];

				if (name.indexOf("Unused Tool") != -1) {
					return;
				}
				if (name.indexOf("Action Tool") != -1) {
					drawTool(newTool, true);
					IJ.wait(50);
					drawTool(newTool, false);
					runMacroTool(newTool);
					return;
				} else {
					name = name.endsWith(" ") ? name : name + " ";
					macroInstaller.runMacroTool(name + "Selected");
				}
			}
			setTool2(newTool);

			if (isLine(current) && (e.isPopupTrigger() || e.isMetaDown())) {
				straightLineItem.setState(lineType == LINE);
				polyLineItem.setState(lineType == POLYLINE);
				freeLineItem.setState(lineType == FREELINE);
				if (IJ.isMacOSX()) {
					IJ.wait(10);
				}
				linePopup.show(e.getComponent(), x, y);
				mouseDownTime = 0L;
			}
			//EU_HOU multibouton
			if (isDraw(current) && (e.isPopupTrigger() || e.isMetaDown())) {
				rectangleItem.setState(drawType == RECTANGLE);
				ovalItem.setState(drawType == OVAL);
				polygonItem.setState(drawType == POLYGON);
				if (IJ.isMacOSX()) {
					IJ.wait(10);
				}
				drawPopup.show(e.getComponent(), x, y);
				mouseDownTime = 0L;
				//EU_HOU End
			}
		} else {
			if (isMacroTool(current)) {
				String name = names[current].endsWith(" ") ? names[current] : names[current] + " ";

				macroInstaller.runMacroTool(name + "Options");
				return;
			}
			ImagePlus imp = WindowManager.getCurrentImage();

			switch (current) {

							case MAGNIFIER:
								if (imp != null) {
									ImageCanvas ic = imp.getCanvas();

									if (ic != null) {
										ic.unzoom();
									}
								}
								break;
							case LINE:
							case POLYLINE:
							case FREELINE:
								//IJ.runPlugIn("ij.plugin.frame.LineWidthAdjuster", "");
								IJ.doCommand(Prefs.getCommand("options01"));
								break;
							default:
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	void showSwitchPopupMenu(MouseEvent e) {
		String path = IJ.getDirectory("macros") + "toolsets/";

		if (path == null) {
			return;
		}
		boolean applet = IJ.getApplet() != null;
		File f = new File(path);
		String[] list;

		if (!applet && f.exists() && f.isDirectory()) {
			list = f.list();
			if (list == null) {
				return;
			}
		} else {
			list = new String[0];
		}

		boolean stackTools = false;

		for (int i = 0; i < list.length; i++) {
			if (list[i].equals("Stack Tools.txt")) {
				stackTools = true;
				break;
			}
		}
		switchPopup.removeAll();
		//Permet de changer le nom de la macro de demarrage ?
		path = IJ.getDirectory("macros") + "StartupMacros.txt";
		f = new File(path);
		if (!applet && f.exists()) {
			addItem("Startup Macros");
		} else {
			addItem("StartupMacros*");
		}
		if (!stackTools) {
			addItem("Stack Tools*");
		}
		for (int i = 0; i < list.length; i++) {
			String name = list[i];

			if (name.endsWith(".txt")) {
				name = name.substring(0, name.length() - 4);
				addItem(name);
			} else if (name.endsWith(".ijm")) {
				name = name.substring(0, name.length() - 4) + " ";
				addItem(name);
			}
		}
		//EU_HOU bundle
		addItem("Help...");
		//add(ovalPopup);
		if (IJ.isMacOSX()) {
			IJ.wait(10);
		}
		switchPopup.show(e.getComponent(), e.getX(), e.getY());
	}


	/**
	 *  Adds a feature to the Item attribute of the Toolbar object
	 *
	 * @param  name  The feature to be added to the Item attribute
	 */
	void addItem(String name) {
		//System.out.println("addItem" + name);

		CheckboxMenuItem item = new CheckboxMenuItem(name, name.equals(currentSet));

		item.addItemListener(this);
		switchPopup.add(item);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tool      Description of the Parameter
	 * @param  drawDown  Description of the Parameter
	 */
	void drawTool(int tool, boolean drawDown) {
		//System.out.println("drawTool" + drawDown);
		down[tool] = drawDown;

		Graphics g = this.getGraphics();

		if (!drawDown && Prefs.antialiasedTools) {
			Graphics2D g2d = (Graphics2D) g;

			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		drawButton(g, tool);
		if (null == g) {
			return;
		}
		g.dispose();
	}


	/**
	 *  Gets the line attribute of the Toolbar object
	 *
	 * @param  tool  Description of the Parameter
	 * @return       The line value
	 */
	boolean isLine(int tool) {
		//System.out.println("isLine" + tool);
		return tool == LINE || tool == POLYLINE || tool == FREELINE;
	}


	/**
	 *  Gets the draw attribute of the Toolbar object
	 *
	 * @param  tool  Description of the Parameter
	 * @return       The draw value
	 */
	//EU_HOU multibouton
	boolean isDraw(int tool) {
		//System.out.println("isLine" + tool);
		return tool == RECTANGLE || tool == OVAL || tool == POLYGON;
	}

	// Returns the line type (LINE, POLYLINE or FREELINE).
	//public int getLineType() {
	//	return lineType;
	//}

	/**
	 *  Description of the Method
	 */
	public void restorePreviousTool() {
		//System.out.println("restorePreviousTool");
		setTool2(mpPrevious);
	}


	/**
	 *  Gets the macroTool attribute of the Toolbar object
	 *
	 * @param  tool  Description of the Parameter
	 * @return       The macroTool value
	 */
	boolean isMacroTool(int tool) {
		//System.out.println("isMacroTool" + tool);
		return tool >= SPARE1 && tool <= SPARE9 && names[tool] != null && macroInstaller != null;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseExited(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseClicked(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseDragged(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	//EU_HOU multibouton
	public void itemStateChanged(ItemEvent e) {
		CheckboxMenuItem item = (CheckboxMenuItem) e.getSource();

		//System.out.println("itemStateChanged" + e + " " + item);
		if (item == straightLineItem) {
			lineType = LINE;
			setTool2(LINE);
			showMessage(LINE);
		} else if (item == polyLineItem) {
			lineType = POLYLINE;
			setTool2(POLYLINE);
			showMessage(POLYLINE);
		} else if (item == freeLineItem) {
			lineType = FREELINE;
			setTool2(FREELINE);
			showMessage(FREELINE);
		} else if (item == rectangleItem) {
			drawType = RECTANGLE;
			setTool2(RECTANGLE);
			showMessage(RECTANGLE);
		} else if (item == ovalItem) {
			drawType = OVAL;
			setTool2(OVAL);
			showMessage(OVAL);
		} else if (item == polygonItem) {
			drawType = POLYGON;
			setTool2(POLYGON);
			showMessage(POLYGON);
		} else {
			String label = item.getActionCommand();

			if (!label.equals("Help...")) {
				currentSet = label;
			}
			String path;
			//EU_HOU Bundle
			if (label.equals("Help...")) {
				//EU_HOU Bundle
				IJ.showMessage("Tool Switcher",
						"Use this drop down menu to switch to macro tool\n" +
						"sets located in the ImageJ/macros/toolsets folder,\n" +
						"or to revert to the ImageJ/macros/StartupMacros\n" +
						"set. The default tool sets, which have names\n" +
						"ending in '*', are loaded from ij.jar.\n" +
						" \n" +
						"Hold the shift key down while selecting a tool\n" +
						"set to view its source code.\n" +
						" \n" +
						"Several example tool sets are available at\n" +
						"<http://rsb.info.nih.gov/ij/macros/toolsets/>."
						);
				return;
			} else if (label.endsWith("*")) {
				// load from ij.jar
				MacroInstaller mi = new MacroInstaller();

				label = label.substring(0, label.length() - 1) + ".txt";
				path = "/macros/" + label;
				if (IJ.shiftKeyDown()) {
					String macros = mi.openFromIJJar(path);
					Editor ed = new Editor();

					ed.setSize(350, 300);
					ed.create(label, macros);
					IJ.setKeyUp(KeyEvent.VK_SHIFT);
				} else {
					mi.installFromIJJar(path);
				}
			} else {
				// load from ImageJ/macros/toolsets
				if (label.equals("Startup Macros")) {
					path = IJ.getDirectory("macros") + "StartupMacros.txt";
				} else if (label.endsWith(" ")) {
					path = IJ.getDirectory("macros") + "toolsets/" + label.substring(0, label.length() - 1) + ".ijm";
				} else {
					path = IJ.getDirectory("macros") + "toolsets/" + label + ".txt";
				}
				try {
					if (IJ.shiftKeyDown()) {
						IJ.open(path);
						IJ.setKeyUp(KeyEvent.VK_SHIFT);
					} else {
						new MacroInstaller().run(path);
					}
				} catch (Exception ex) {}
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) {

		MenuItem item = (MenuItem) e.getSource();

		//System.out.println("actionPerformed" + item);

		String cmd = e.getActionCommand();
		PopupMenu popup = (PopupMenu) item.getParent();
		int tool = -1;

		for (int i = SPARE1; i < NUM_TOOLS; i++) {
			if (popup == menus[i]) {
				tool = i;
				break;
			}
		}
		if (tool == -1) {
			return;
		}
		if (macroInstaller != null) {
			macroInstaller.runMenuTool(names[tool], cmd);
		}
	}


	/**
	 *  Gets the preferredSize attribute of the Toolbar object
	 *
	 * @return    The preferredSize value
	 */
	public Dimension getPreferredSize() {
		return ps;
	}


	/**
	 *  Gets the minimumSize attribute of the Toolbar object
	 *
	 * @return    The minimumSize value
	 */
	public Dimension getMinimumSize() {
		return ps;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  e  Description of the Parameter
	 */
	public void mouseMoved(MouseEvent e) {
		int x = e.getX();

		x = toolID(x / SIZE);
		showMessage(x);
	}


	/**
	 *  Adds a tool to the toolbar. The 'toolTip' string is displayed in the status
	 *  bar when the mouse is over the tool icon. The 'toolTip' string may include
	 *  icon (http://rsb.info.nih.gov/ij/developer/macro/macros.html#tools).
	 *  Returns the tool ID, or -1 if all tools are in use.
	 *
	 * @param  toolTip  The feature to be added to the Tool attribute
	 * @return          Description of the Return Value
	 */
	public int addTool(String toolTip) {
		//System.out.println("addTool" + toolTip);

		int index = toolTip.indexOf('-');
		boolean hasIcon = index >= 0 && (toolTip.length() - index) > 4;
		int tool = -1;

		if (names[SPARE1] == null) {
			tool = SPARE1;
		}
		if (tool == -1) {
			for (int i = SPARE2; i <= SPARE8; i++) {
				if (names[i] == null) {
					tool = i;
					break;
				}
			}
		}
		if (tool == -1) {
			return -1;
		}
		if (hasIcon) {
			icons[tool] = toolTip.substring(index + 1);
			if (index > 0 && toolTip.charAt(index - 1) == ' ') {
				names[tool] = toolTip.substring(0, index - 1);
			} else {
				names[tool] = toolTip.substring(0, index);
			}
		} else {
			if (toolTip.endsWith("-")) {
				toolTip = toolTip.substring(0, toolTip.length() - 1);
			} else if (toolTip.endsWith("- ")) {
				toolTip = toolTip.substring(0, toolTip.length() - 2);
			}
			names[tool] = toolTip;
		}
		if (tool == current && (names[tool].indexOf("Action Tool") != -1 || names[tool].indexOf("Unused Tool") != -1)) {
			//EU_HOU Chnages
			setTool(LINE);
		}
		if (names[tool].endsWith(" Menu Tool")) {
			installMenu(tool);
		}
		return tool;
	}


	/**
	 *  Description of the Method
	 *
	 * @param  tool  Description of the Parameter
	 */
	void installMenu(int tool) {
		Program pgm = macroInstaller.getProgram();
		Hashtable h = pgm.getMenus();

		if (h == null) {
			return;
		}
		String[] commands = (String[]) h.get(names[tool]);

		if (commands == null) {
			return;
		}
		if (menus[tool] == null) {
			menus[tool] = new PopupMenu("");
			if (Menus.getFontSize() != 0) {
				menus[tool].setFont(Menus.getFont());
			}
			add(menus[tool]);
		} else {
			menus[tool].removeAll();
		}
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].equals("-")) {
				menus[tool].addSeparator();
			} else {
				MenuItem mi = new MenuItem(commands[i]);

				mi.addActionListener(this);
				menus[tool].add(mi);
			}
		}
		if (tool == current) {
			//EU_HOU Chnages
			setTool(LINE);
		}
	}


	/**
	 *  Used by the MacroInstaller class to install macro tools.
	 *
	 * @param  name            The feature to be added to the MacroTool attribute
	 * @param  macroInstaller  The feature to be added to the MacroTool attribute
	 * @param  id              The feature to be added to the MacroTool attribute
	 */
	public void addMacroTool(String name, MacroInstaller macroInstaller, int id) {
		if (id == 0) {
			for (int i = SPARE1; i < NUM_TOOLS - 1; i++) {
				names[i] = null;
				icons[i] = null;
				if (menus[i] != null) {
					menus[i].removeAll();
				}
			}
		}
		this.macroInstaller = macroInstaller;
		addTool(name);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  id  Description of the Parameter
	 */
	void runMacroTool(int id) {
		if (macroInstaller != null) {
			macroInstaller.runMacroTool(names[id]);
		}
	}


	/**
	 *  Description of the Method
	 */
	//EU_HOU Changes pas utile?
	void showBrushDialog() {
		GenericDialog gd = new GenericDialog("Selection Brush");

		gd.addCheckbox("Enable Selection Brush", brushEnabled);
		gd.addNumericField("           Size:", brushSize, 0, 4, "pixels");
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		brushEnabled = gd.getNextBoolean();
		brushSize = (int) gd.getNextNumber();

		ImagePlus img = WindowManager.getCurrentImage();
		Roi roi = img != null ? img.getRoi() : null;
	}

}

