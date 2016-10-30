//EU_HOU
package ij.gui;

import java.awt.*;
import java.awt.image.*;
import java.util.Properties;
import java.awt.event.*;
import ij.*;
import ij.process.*;
import ij.io.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.macro.Interpreter;
/*
 *  EU_HOU CHANGES
 */
import java.util.Locale;
import java.util.ResourceBundle;
/*
 *  EU_HOU END
 */
/**
 *  A frame for displaying images.
 *
 *@author     Thomas
 *@created    29 octobre 2007
 */
public class ImageWindow extends Frame implements FocusListener, WindowListener, WindowStateListener, MouseWheelListener {

	/**
	 *  Description of the Field
	 */
	public final static int MIN_WIDTH = 128;
	/**
	 *  Description of the Field
	 */
	public final static int MIN_HEIGHT = 32;

	/**
	 *  Description of the Field
	 */
	protected ImagePlus imp;
	/**
	 *  Description of the Field
	 */
	protected ImageJ ij;
	/**
	 *  Description of the Field
	 */
	protected ImageCanvas ic;
	private double initialMagnification = 1;
	private int newWidth, newHeight;
	/**
	 *  Description of the Field
	 */
	protected boolean closed;
	private boolean newCanvas;
	private static Rectangle maxWindow;
	private boolean unzoomWhenMinimizing = true;
	Rectangle maxBounds;

	private final static int XINC = 8;
	private final static int YINC = 12;
	private final static int TEXT_GAP = 10;
	private static int xbase = -1;
	private static int ybase;
	private static int xloc;
	private static int yloc;
	private static int count;
	private static boolean centerOnScreen;

	private int textGap = centerOnScreen ? 0 : TEXT_GAP;

	/**
	 *  This variable is set false if the user presses the escape key or closes the
	 *  window.
	 */
	public boolean running;

	/**
	 *  This variable is set false if the user clicks in this window, presses the
	 *  escape key, or closes the window.
	 */
	public boolean running2;
	/*
	 *  EU_HOU CHANGES
	 */
	private static ResourceBundle etiq;

	private boolean publicImage;


	/**
	 *  Sets the publicImage attribute of the ImageWindow object
	 *
	 *@param  b  The new publicImage value
	 */
	//EU_HOU Add
	public void setPublicImage(boolean b) {
		publicImage = b;
	}


	/**
	 *  Gets the publicImage attribute of the ImageWindow object
	 *
	 *@return    The publicImage value
	 */
	//EU_HOU Add
	public boolean isPublicImage() {
		return publicImage;
	}


	/**
	 *  Constructor for the ImageWindow object
	 *
	 *@param  imp  Description of the Parameter
	 *@param  pub  Description of the Parameter
	 */
	public ImageWindow(ImagePlus imp, boolean pub) {
		this(imp, new ImageCanvas(imp), pub);
		System.out.println("ImageWindow(ImagePlus imp, boolean pub)");
	}


	/**
	 *  Constructor for the ImageWindow object
	 *
	 *@param  imp  Description of the Parameter
	 */
	public ImageWindow(ImagePlus imp) {
		this(imp, new ImageCanvas(imp));
		System.out.println("ImageWindow(ImagePlus imp)");
	}


	/**
	 *  Constructor for the ImageWindow object
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ic   Description of the Parameter
	 */
	public ImageWindow(ImagePlus imp, ImageCanvas ic) {
		this(imp, new ImageCanvas(imp), true);
		System.out.println("ImageWindow(ImagePlus imp, ImageCanvas ic)");
	}



	/**
	 *  Constructor for the ImageWindow object
	 *
	 *@param  title  Description of the Parameter
	 */
	public ImageWindow(String title) {
		super(title);
		System.out.println("ImageWindow(String title)");
	}



	/**
	 *  Constructor for the ImageWindow object
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ic   Description of the Parameter
	 *@param  pub  Description of the Parameter
	 */
	public ImageWindow(ImagePlus imp, ImageCanvas ic, boolean pub) {
		super(imp.getTitle());
		/*
	 *  EU_HOU END
	 */
		System.out.println("ImageWindow(ImagePlus imp, ImageCanvas ic, boolean pub)");
		publicImage = pub;
		if (Prefs.blackCanvas && getClass().getName().equals("ij.gui.ImageWindow")) {
			setForeground(Color.white);
			setBackground(Color.black);
		} else {
			setForeground(Color.black);
			if (IJ.isLinux()) {
				setBackground(ImageJ.backgroundColor);
			} else {
				setBackground(Color.white);
			}
		}
		ij = IJ.getInstance();

	/*
		 *  EU_HOU CHANGE
		 */
	Locale lang = ij.getLocale();

		etiq = IJ.getBundle();
		/*
		 *  EU_HOU END
		 */
		this.imp = imp;
		if (ic == null) {
			ic = new ImageCanvas(imp);
			newCanvas = true;
		}
		this.ic = ic;

	ImageWindow previousWindow = imp.getWindow();

		setLayout(new ImageLayout(ic));
		add(ic);
		addFocusListener(this);
		addWindowListener(this);
		addWindowStateListener(this);
		addKeyListener(ij);
		setFocusTraversalKeysEnabled(false);
		if (!(this instanceof StackWindow)) {
			addMouseWheelListener(this);
		}
		setResizable(true);
		WindowManager.addWindow(this);
		imp.setWindow(this);
		if (previousWindow != null) {
			if (newCanvas) {
				setLocationAndSize(false);
			} else {
				ic.update(previousWindow.getCanvas());
			}

		Point loc = previousWindow.getLocation();

			setLocation(loc.x, loc.y);
			if (!(this instanceof StackWindow)) {
				pack();
				show();
			}
		boolean unlocked = imp.lockSilently();
		boolean changes = imp.changes;

			imp.changes = false;
			previousWindow.close();
			imp.changes = changes;
			if (unlocked) {
				imp.unlock();
			}
			WindowManager.setCurrentWindow(this);
		} else {
			setLocationAndSize(false);
			if (ij != null && !IJ.isMacintosh()) {
			Image img = ij.getIconImage();

				if (img != null) {
					try {
						setIconImage(img);
					} catch (Exception e) {}
				}
			}
			if (centerOnScreen) {
				GUI.center(this);
				centerOnScreen = false;
			}
			if (Interpreter.isBatchMode()) {
				WindowManager.setTempCurrentImage(imp);
				Interpreter.addBatchModeImage(imp);
			} else {
				show();
			}
		}
	}


	/**
	 *  Sets the locationAndSize attribute of the ImageWindow object
	 *
	 *@param  updating  The new locationAndSize value
	 */
	private void setLocationAndSize(boolean updating) {
	int width = imp.getWidth();
	int height = imp.getHeight();

		if (maxWindow == null) {
			maxWindow = getMaxWindow();
		}
		if (WindowManager.getWindowCount() <= 1) {
			xbase = -1;
		}
		if (width > maxWindow.width / 2 && xbase > maxWindow.x + 5 + XINC * 6) {
			xbase = -1;
		}
		if (xbase == -1) {
			count = 0;
			xbase = maxWindow.x + 5;
			if (width * 2 <= maxWindow.width) {
				xbase = maxWindow.x + maxWindow.width / 2 - width / 2;
			}
			ybase = maxWindow.y;
			xloc = xbase;
			yloc = ybase;
		}
	int x = xloc;
	int y = yloc;

		xloc += XINC;
		yloc += YINC;
		count++;
		if (count % 6 == 0) {
			xloc = xbase;
			yloc = ybase;
		}

	int sliderHeight = (this instanceof StackWindow) ? 20 : 0;
	int screenHeight = maxWindow.height - sliderHeight;
	double mag = 1;

		while (xbase + XINC * 4 + width * mag > maxWindow.width || ybase + height * mag > screenHeight) {
		double mag2 = ImageCanvas.getLowerZoomLevel(mag);

			if (mag2 == mag) {
				break;
			}
			mag = mag2;
		}

		if (mag < 1.0) {
			initialMagnification = mag;
			ic.setDrawingSize((int) (width * mag), (int) (height * mag));
		}
		ic.setMagnification(mag);
		if (y + height * mag > screenHeight) {
			y = ybase;
		}
		if (!updating) {
			setLocation(x, y);
		}
		if (Prefs.open100Percent && ic.getMagnification() < 1.0) {
			while (ic.getMagnification() < 1.0) {
				ic.zoomIn(0, 0);
			}
			setSize(Math.min(width, maxWindow.width - x), Math.min(height, screenHeight - y));
			validate();
		} else {
			pack();
		}
		maxBounds = getMaximumBounds();
		if (!IJ.isLinux()) {
			setMaximizedBounds(maxBounds);
		}
	}


	/**
	 *  Gets the maxWindow attribute of the ImageWindow object
	 *
	 *@return    The maxWindow value
	 */
	Rectangle getMaxWindow() {
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	Rectangle maxWindow = ge.getMaximumWindowBounds();
	Dimension ijSize = ij != null ? ij.getSize() : new Dimension(0, 0);

		maxWindow.y += ijSize.height;
		maxWindow.height -= ijSize.height;
		return maxWindow;
	}


	/**
	 *  Gets the initialMagnification attribute of the ImageWindow object
	 *
	 *@return    The initialMagnification value
	 */
	public double getInitialMagnification() {
		return initialMagnification;
	}


	/**
	 *  Override Container getInsets() to make room for some text above the image.
	 *
	 *@return    The insets value
	 */
	public Insets getInsets() {
	Insets insets = super.getInsets();
	double mag = ic.getMagnification();
	int extraWidth = (int) ((MIN_WIDTH - imp.getWidth() * mag) / 2.0);

		if (extraWidth < 0) {
			extraWidth = 0;
		}
	int extraHeight = (int) ((MIN_HEIGHT - imp.getHeight() * mag) / 2.0);

		if (extraHeight < 0) {
			extraHeight = 0;
		}
		insets = new Insets(insets.top + textGap + extraHeight, insets.left + extraWidth, insets.bottom + extraHeight, insets.right + extraWidth);
		return insets;
	}


	/**
	 *  Draws the subtitle.
	 *
	 *@param  g  Description of the Parameter
	 */
	public void drawInfo(Graphics g) {
		if (textGap != 0) {
		Insets insets = super.getInsets();

			if (imp instanceof CompositeImage) {
				g.setColor(((CompositeImage) imp).getChannelColor());
			}
			g.drawString(createSubtitle(), 5, insets.top + TEXT_GAP);
		}
	}


	/**
	 *  Creates the subtitle.
	 *
	 *@return    Description of the Return Value
	 */
	public String createSubtitle() {
	String s = "";
	int nSlices = imp.getStackSize();

		if (nSlices > 1) {
		ImageStack stack = imp.getStack();
		int currentSlice = imp.getCurrentSlice();

			s += currentSlice + "/" + nSlices;

		String label = stack.getShortSliceLabel(currentSlice);

			if (label != null && label.length() > 0) {
				s += " (" + label + ")";
			}
			if ((this instanceof StackWindow) && running2) {
				return s;
			}
			s += "; ";
		}
	int type = imp.getType();
	Calibration cal = imp.getCalibration();

		if (cal.scaled()) {
			s += IJ.d2s(imp.getWidth() * cal.pixelWidth, 2) + "x" + IJ.d2s(imp.getHeight() * cal.pixelHeight, 2)
					 + " " + cal.getUnits() + " (" + imp.getWidth() + "x" + imp.getHeight() + "); ";
		} else {
			s += imp.getWidth() + "x" + imp.getHeight() + " pixels; ";
		}

	int size = (imp.getWidth() * imp.getHeight() * imp.getStackSize()) / 1024;

		switch (type) {
						case ImagePlus.GRAY8:
						case ImagePlus.COLOR_256:
							//EU_HOU Bundle
							s += etiq.getString("8-bit");
							break;
						case ImagePlus.GRAY16:
							//EU_HOU Bundle
							s += etiq.getString("16-bit");
							size *= 2;
							break;
						case ImagePlus.GRAY32:
							//EU_HOU Bundle
							s += etiq.getString("32-bit");
							size *= 4;
							break;
						case ImagePlus.COLOR_RGB:
							//EU_HOU Bundle
							s += etiq.getString("RGBColor");
							size *= 4;
							break;
		}
		if (imp.isInvertedLut()) {
			s += " (inverting LUT)";
		}
		if (size >= 10000) {
			s += "; " + (int) Math.round(size / 1024.0) + "MB";
		} else if (size >= 1024) {
		double size2 = size / 1024.0;

			s += "; " + IJ.d2s(size2, (int) size2 == size2 ? 0 : 1) + "MB";
		} else {
			s += "; " + size + "K";
		}
		return s;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		//if (IJ.debugMode) IJ.log("wPaint: " + imp.getTitle());
		drawInfo(g);

	Rectangle r = ic.getBounds();
	int extraWidth = MIN_WIDTH - r.width;
	int extraHeight = MIN_HEIGHT - r.height;

		if (extraWidth <= 0 && extraHeight <= 0 && !Prefs.noBorder && !IJ.isLinux()) {
			g.drawRect(r.x - 1, r.y - 1, r.width + 1, r.height + 1);
		}
	}


	/**
	 *  Removes this window from the window list and disposes of it. Returns false
	 *  if the user cancels the "save changes" dialog.
	 *
	 *@return    Description of the Return Value
	 */
	public boolean close() {
	boolean isRunning = running || running2;

		running = running2 = false;
		if (isRunning) {
			IJ.wait(500);
		}
		if (ij == null || IJ.getApplet() != null || Interpreter.isBatchMode() || IJ.macroRunning()) {
			imp.changes = false;
		}
		if (imp.changes) {
		String msg;
		String name = imp.getTitle();

			if (name.length() > 22) {
				//EU_HOU Bundle
				msg = IJ.getBundle().getString("SaveChanges") + "\n" + "\"" + name + "\"?";
			} else {
				//EU_HOU Bundle
				msg = IJ.getBundle().getString("SaveChanges") + "\n" + "\"" + name + "\"?";
			}

		YesNoCancelDialog d = new YesNoCancelDialog(this, "SalsaJ", msg);

			if (d.cancelPressed()) {
				return false;
			} else if (d.yesPressed()) {
			FileSaver fs = new FileSaver(imp);

				if (!fs.save()) {
					return false;
				}
			}
		}
		closed = true;
		if (WindowManager.getWindowCount() == 0) {
			xloc = 0;
			yloc = 0;
		}
		WindowManager.removeWindow(this);
		setVisible(false);
		if (ij != null && ij.quitting()) {// this may help avoid thread deadlocks
			return true;
		}
		dispose();
		imp.flush();
		return true;
	}


	/**
	 *  Gets the imagePlus attribute of the ImageWindow object
	 *
	 *@return    The imagePlus value
	 */
	public ImagePlus getImagePlus() {
		return imp;
	}


	/**
	 *  Sets the imagePlus attribute of the ImageWindow object
	 *
	 *@param  imp  The new imagePlus value
	 */
	void setImagePlus(ImagePlus imp) {
		this.imp = imp;
		repaint();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 */
	public void updateImage(ImagePlus imp) {
		if (imp != this.imp) {
			throw new IllegalArgumentException("imp!=this.imp");
		}
		this.imp = imp;
		ic.updateImage(imp);
		setLocationAndSize(true);
		pack();
		repaint();
	}


	/**
	 *  Gets the canvas attribute of the ImageWindow object
	 *
	 *@return    The canvas value
	 */
	public ImageCanvas getCanvas() {
		return ic;
	}


	/**
	 *  Gets the clipboard attribute of the ImageWindow class
	 *
	 *@return    The clipboard value
	 */
	static ImagePlus getClipboard() {
		return ImagePlus.getClipboard();
	}


	/**
	 *  Gets the maximumBounds attribute of the ImageWindow object
	 *
	 *@return    The maximumBounds value
	 */
	public Rectangle getMaximumBounds() {
	int width = imp.getWidth();
	int height = imp.getHeight();

		maxWindow = getMaxWindow();

	Insets insets = getInsets();
	int extraHeight = insets.top + insets.bottom;

		if (this instanceof StackWindow) {
			extraHeight += 25;
		}
	//if (IJ.isWindows()) extraHeight += 20;
	double maxHeight = maxWindow.height - extraHeight;
	double maxWidth = maxWindow.width;
	double mAspectRatio = maxWidth / maxHeight;
	double iAspectRatio = (double) width / height;
	int wWidth;
	int wHeight;

		if (iAspectRatio >= mAspectRatio) {
			wWidth = (int) maxWidth;
			wHeight = (int) (maxWidth / iAspectRatio);
		} else {
			wHeight = (int) maxHeight;
			wWidth = (int) (maxHeight * iAspectRatio);
		}

	int xloc = (int) (maxWidth - wWidth) / 2;

		if (xloc < 0) {
			xloc = 0;
		}
		return new Rectangle(xloc, maxWindow.y, wWidth, wHeight + extraHeight);
	}


	/**
	 *  Description of the Method
	 */
	public void maximize() {
		if (maxBounds == null) {
			return;
		}
	int width = imp.getWidth();
	int height = imp.getHeight();
	Insets insets = getInsets();
	int extraHeight = insets.top + insets.bottom + 5;

		if (this instanceof StackWindow) {
			extraHeight += 25;
		}
	double mag = Math.floor((maxBounds.height - extraHeight) * 100.0 / height) / 100.0;
	double aspectRatio = (double) width / height;

		if (mag > ic.getMagnification() || aspectRatio < 0.5 || aspectRatio > 2.0) {
			ic.setMagnification2(mag);
			ic.setSrcRect(new Rectangle(0, 0, width, height));
			ic.setDrawingSize((int) (width * mag), (int) (height * mag));
			validate();
			unzoomWhenMinimizing = true;
		} else {
			unzoomWhenMinimizing = false;
		}
	}


	/**
	 *  Description of the Method
	 */
	public void minimize() {
		if (unzoomWhenMinimizing) {
			ic.unzoom();
		}
		unzoomWhenMinimizing = true;
	}


	/**
	 *  Has this window been closed?
	 *
	 *@return    The closed value
	 */
	public boolean isClosed() {
		return closed;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void focusGained(FocusEvent e) {
		//IJ.log("focusGained: "+imp.getTitle());
		if (!Interpreter.isBatchMode() && ij != null && !ij.quitting()) {
			WindowManager.setCurrentWindow(this);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowActivated(WindowEvent e) {
	//IJ.log("windowActivated: "+imp.getTitle());
	ImageJ ij = IJ.getInstance();
	boolean quitting = ij != null && ij.quitting();

		if (IJ.isMacintosh() && ij != null && !quitting) {
			IJ.wait(10);// may be needed for Java 1.4 on OS X
			setMenuBar(Menus.getMenuBar());
		}
		imp.setActivated();// notify ImagePlus that image has been activated
		if (!closed && !quitting && !Interpreter.isBatchMode()) {
			WindowManager.setCurrentWindow(this);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowClosing(WindowEvent e) {
		//IJ.log("windowClosing: "+imp.getTitle()+" "+closed);
		if (closed) {
			return;
		}
		if (ij != null) {
			WindowManager.setCurrentWindow(this);
			IJ.doCommand("Close");
		} else {
			setVisible(false);
			dispose();
			WindowManager.removeWindow(this);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowStateChanged(WindowEvent e) {
	int oldState = e.getOldState();
	int newState = e.getNewState();
		//IJ.log("WSC: "+getBounds()+" "+oldState+" "+newState);
		if ((oldState & Frame.MAXIMIZED_BOTH) == 0 && (newState & Frame.MAXIMIZED_BOTH) != 0) {
			maximize();
		} else if ((oldState & Frame.MAXIMIZED_BOTH) != 0 && (newState & Frame.MAXIMIZED_BOTH) == 0) {
			minimize();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowClosed(WindowEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowDeactivated(WindowEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void focusLost(FocusEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowDeiconified(WindowEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowIconified(WindowEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowOpened(WindowEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  event  Description of the Parameter
	 */
	public void mouseWheelMoved(MouseWheelEvent event) {
	int rotation = event.getWheelRotation();
	int width = imp.getWidth();
	int height = imp.getHeight();
	Rectangle srcRect = ic.getSrcRect();
	int xstart = srcRect.x;
	int ystart = srcRect.y;

		if (IJ.spaceBarDown() || srcRect.height == height) {
			srcRect.x += rotation * Math.max(width / 200, 1);
			if (srcRect.x < 0) {
				srcRect.x = 0;
			}
			if (srcRect.x + srcRect.width > width) {
				srcRect.x = width - srcRect.width;
			}
		} else {
			srcRect.y += rotation * Math.max(height / 200, 1);
			if (srcRect.y < 0) {
				srcRect.y = 0;
			}
			if (srcRect.y + srcRect.height > height) {
				srcRect.y = height - srcRect.height;
			}
		}
		if (srcRect.x != xstart || srcRect.y != ystart) {
			ic.repaint();
		}
	}


	/**
	 *  Copies the current ROI to the clipboard. The entire image is copied if
	 *  there is no ROI.
	 *
	 *@param  cut  Description of the Parameter
	 */
	public void copy(boolean cut) {
		imp.copy(cut);
	}


	/**
	 *  Description of the Method
	 */
	public void paste() {
		imp.paste();
	}


	/**
	 *  This method is called by ImageCanvas.mouseMoved(MouseEvent).
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@see       ij.gui.ImageCanvas#mouseMoved
	 */
	public void mouseMoved(int x, int y) {
		imp.mouseMoved(x, y);
	}


	/*
	 *  EU_HOU added
	 */
	/**
	 *  This method is called by ImageCanvas.mouseClicked(MouseEvent).
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@see       ij.gui.ImageCanvas#mousePresseded
	 */
	public void mouseClicked(int x, int y) {
		imp.mouseClicked(x, y);
	}


	/*
	 *  EU_HOU end
	 */
	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public String toString() {
		return imp.getTitle();
	}


	/**
	 *  Causes the next image to be opened to be centered on the screen and
	 *  displayed without informational text above the image.
	 */
	public static void centerNextImage() {
		centerOnScreen = true;
	}


	/**
	 *  Moves and resizes this window. Changes the magnification so the image fills
	 *  the window.
	 *
	 *@param  x       The new locationAndSize value
	 *@param  y       The new locationAndSize value
	 *@param  width   The new locationAndSize value
	 *@param  height  The new locationAndSize value
	 */
	public void setLocationAndSize(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
		getCanvas().fitToWindow();
		pack();
	}

	//public void setBounds(int x, int y, int width, int height)	{
	//	super.setBounds(x, y, width, height);
	//	ic.resizeSourceRect(width, height);
	//}

}//class ImageWindow

