//EU_HOU
package ij.gui;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import ij.*;
import ij.process.*;
import ij.util.*;
import ij.plugin.filter.Analyzer;
import ij.macro.Interpreter;
import ij.measure.Calibration;
/*
    EU_HOU CHANGES
  */
//import ij.plugin.RadioSpectrum_Reader;
import ij.io.*;
/*
    EU_HOU END
  */
/**
 *  This class is an image that line graphs can be drawn on.
 *
 * @author     Thomas
 * @created    31 octobre 2007
 */
public class Plot {

	/**
	 *  Display points using a circle 5 pixels in diameter.
	 */
	public final static int CIRCLE = 0;
	/**
	 *  Display points using an X-shaped mark.
	 */
	public final static int X = 1;
	/**
	 *  Display points using an box-shaped mark.
	 */
	public final static int BOX = 3;
	/**
	 *  Display points using an tiangular mark.
	 */
	public final static int TRIANGLE = 4;
	/**
	 *  Display points using an cross-shaped mark.
	 */
	public final static int CROSS = 5;
	/**
	 *  Display points using a single pixel.
	 */
	public final static int DOT = 6;
	/**
	 *  Connect points with solid lines.
	 */
	public final static int LINE = 2;
	///** flag multiplier for maximum number of ticks&grid lines along x */
	//public static final int X_INTERVALS_M = 0x1;
	///** flag multiplier for maximum number of ticks&grid lines along y */
	//public static final int Y_INTERVALS_M = 0x100;
	/**
	 *  flag for numeric labels of x-axis ticks
	 */
	public final static int X_NUMBERS = 0x1;
	/**
	 *  flag for numeric labels of x-axis ticks
	 */
	public final static int Y_NUMBERS = 0x2;
	/**
	 *  flag for drawing x-axis ticks
	 */
	public final static int X_TICKS = 0x4;
	/**
	 *  flag for drawing x-axis ticks
	 */
	public final static int Y_TICKS = 0x8;
	/**
	 *  flag for drawing vertical grid lines for x-axis ticks
	 */
	public final static int X_GRID = 0x10;
	/**
	 *  flag for drawing horizontal grid lines for y-axis ticks
	 */
	public final static int Y_GRID = 0x20;
	/**
	 *  flag for forcing frame to coincide with the grid/ticks in x direction
	 *  (results in unused space)
	 */
	public final static int X_FORCE2GRID = 0x40;
	/**
	 *  flag for forcing frame to coincide with the grid/ticks in y direction
	 *  (results in unused space)
	 */
	public final static int Y_FORCE2GRID = 0x80;
	/**
	 *  the default flags
	 */
	public final static int DEFAULT_FLAGS = X_NUMBERS + Y_NUMBERS +
	/*
	    X_TICKS + Y_TICKS +
	  */
			X_GRID + Y_GRID;
	/**
	 *  the margin width left of the plot frame (enough for 5-digit numbers such as
	 *  unscaled 16-bit data
	 */
	public final static int LEFT_MARGIN = 60;
	/**
	 *  the margin width right of the plot frame
	 */
	public final static int RIGHT_MARGIN = 18;
	/**
	 *  the margin width above the plot frame
	 */
	public final static int TOP_MARGIN = 15;
	/**
	 *  the margin width below the plot frame
	 */
	public final static int BOTTOM_MARGIN = 40;
//EU_HOU Changes private => public
	/**
	 *  Description of the Field
	 */
	public final static int WIDTH = 450;
	/**
	 *  Description of the Field
	 */
	public final static int HEIGHT = 200;
	/**
	 *  Description of the Field
	 */
	public final static int MAX_INTERVALS = 12;//maximum number of intervals between ticks or grid lines
	/**
	 *  Description of the Field
	 */
	public final static int MIN_X_GRIDWIDTH = 60;//minimum distance between grid lines or ticks along x
	/**
	 *  Description of the Field
	 */
	public final static int MIN_Y_GRIDWIDTH = 40;//minimum distance between grid lines or ticks along y
	/**
	 *  Description of the Field
	 */
	public final static int TICK_LENGTH = 3;//length of ticks
	/**
	 *  Description of the Field
	 */
	public final Color gridColor = new Color(0xc0c0c0);//light gray
	/**
	 *  Description of the Field
	 */
	public int frameWidth;
	/**
	 *  Description of the Field
	 */
	public int frameHeight;
	//EU_HOU end
	Rectangle frame = null;
	//float[] xValues, yValues;
	float[] errorBars;
	int nPoints;
	//double xMin, xMax, yMin, yMax;
	//EU_HOU Changes private => public
	/**
	 *  Description of the Field
	 */
	public double xScale, yScale;
	/**
	 *  Description of the Field
	 */
	public static String defaultDirectory = null;
	/**
	 *  Description of the Field
	 */
	public String xLabel;
	/**
	 *  Description of the Field
	 */
	public String yLabel;
	//EU_HOU end
	private int flags;
	private Font font = new Font("Helvetica", Font.PLAIN, 12);
	private boolean fixedYScale;
	private int lineWidth = 1;// Line.getWidth();
	private int markSize = 5;
	/**
	 *  Description of the Field
	 */
	//EU_HOU Add
	// imageprocessor from plot to draw lines in plot
	private ImageProcessor ip;
	boolean radio = false;
	/**
	 *  Description of the Field
	 */
	public String title;
	private boolean initialized;
	private boolean plotDrawn;
	private int plotWidth = PlotWindow.plotWidth;
	private int plotHeight = PlotWindow.plotHeight;
	private boolean multiplePlots;
	private boolean drawPending;
	/*
	    EU_HOU CHANGES
	  */
	/**
	 *  Description of the Field
	 */
	public float[] xValues, yValues, xInitValues, yInitValues, yValuesBaseLine, yValuesMergeGaussFit;
	/**
	 *  Description of the Field
	 */
	public double[] gaussFitResult;
	/**
	 *  Description of the Field
	 */
	public int nbgauss;
	/**
	 *  Description of the Field
	 */
	public double xMin, xMax, xInitMin, xInitMax, yMin, yMax;
	/**
	 *  Description of the Field
	 */
	public Button list, save, copy, setting, setScale, option, setScaleRadio, baseline, subtract, gauss;
	/**
	 *  Description of the Field
	 */
	public Label Xcoord, Ycoord;
	/**
	 *  Description of the Field
	 */
	public boolean modifListener = false;
	/**
	 *  Description of the Field
	 */
	public int oldX = -1, oldY = -1, oldindex = -1;
	/**
	 *  Description of the Field
	 */
	public int Xpoints[], Ypoints[];
	/**
	 *  Description of the Field
	 */
	protected ImagePlus imp;
	/**
	 *  Description of the Field
	 */
	public ImagePlus origin;
	/**
	 *  Description of the Field
	 */
	public Roi region;
	/**
	 *  Begin the plot profile with the number pixel selected and not zero. the
	 *  selection should be horizontal
	 */
	public static boolean horizontal;
	//EU_HOU_rb
	/**
	 *  Specific menu for the spectrum analysis
	 */
	public static boolean RadioSpectra = false;
	//EU_HOU_jl
	/**
	 *  Description of the Field
	 */
	public static boolean Base_Line = false;
	/**
	 *  Description of the Field
	 */
	public static boolean Base_Line_subtracted = false;
	/**
	 *  Description of the Field
	 */
	public static boolean ZERO_LINE = false;
	private Vector labels = new Vector(), xlabs = new Vector(), ylabs = new Vector();


	/*
	    EU_HOU END
	  */
	/**
	 *  Construct a new PlotWindow.
	 *
	 * @param  title        the window title
	 * @param  xLabel       the x-axis label
	 * @param  yLabel       the y-axis label
	 * @param  xValues      the x-coodinates, or null
	 * @param  yValues      the y-coodinates, or null
	 * @param  flags        sum of flag values controlling appearance of ticks,
	 *      grid, etc.
	 * @param  xInitValues  Description of the Parameter
	 * @param  yInitValues  Description of the Parameter
	 * @param  orig         Description of the Parameter
	 * @param  reg          Description of the Parameter
	 */
	//EU_HOU
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, float[] xInitValues, float[] yInitValues, int flags, ImagePlus orig, Roi reg) {
		this.title = title;
		this.xLabel = xLabel;
		this.yLabel = yLabel;
		/*
		    EU_HOU CHANGES
		  */
		this.xInitValues = xInitValues;
		this.yInitValues = yInitValues;
		/*
		    EU_HOU END
		  */
		this.flags = flags;
		if (xValues == null || yValues == null) {
			xValues = new float[1];
			yValues = new float[1];
			xValues[0] = -1f;
			yValues[0] = -1f;
		}
		this.xValues = xValues;
		this.yValues = yValues;
		double[] a = Tools.getMinMax(xValues);
		xMin = a[0];
		xMax = a[1];
		/*
		    EU_HOU CHANGES
		  */
		a = Tools.getMinMax(xInitValues);
		xInitMin = a[0];
		xInitMax = a[1];
		/*
		    EU_HOU END
		  */
		a = Tools.getMinMax(yValues);
		yMin = a[0];
		yMax = a[1];
		fixedYScale = false;
		nPoints = xValues.length;
		drawPending = true;

		//EU_HOU
		origin = orig;
		region = reg;
	}


	/**
	 *  This version of the constructor uses the default flags.
	 *
	 * @param  title    Description of the Parameter
	 * @param  xLabel   Description of the Parameter
	 * @param  yLabel   Description of the Parameter
	 * @param  xValues  Description of the Parameter
	 * @param  yValues  Description of the Parameter
	 * @param  orig     Description of the Parameter
	 * @param  reg      Description of the Parameter
	 */
	//EU_HOU
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, ImagePlus orig, Roi reg) {
		/*
		    EU_HOU CHANGES
		  */
		this(title, xLabel, yLabel, xValues, yValues, xValues, yValues, DEFAULT_FLAGS, orig, reg);

		this.horizontal = false;
		/*
		    EU_HOU END
		  */
	}


	/**
	 *Constructor for the Plot object
	 *
	 * @param  title         Description of the Parameter
	 * @param  xLabel        Description of the Parameter
	 * @param  yLabel        Description of the Parameter
	 * @param  xValues       Description of the Parameter
	 * @param  yValues       Description of the Parameter
	 * @param  RadioSpectra  Description of the Parameter
	 */
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, boolean RadioSpectra) {
		/*
		    EU_HOU CHANGES
		  */
		this(title, xLabel, yLabel, xValues, yValues, xValues, yValues, DEFAULT_FLAGS, null, null);

		this.horizontal = false;

		this.radio = RadioSpectra;
		/*
		    EU_HOU END
		  */
	}

	//EU_HOU
	/**
	 *  Constructor for the Plot object
	 *
	 * @param  title    Description of the Parameter
	 * @param  xLabel   Description of the Parameter
	 * @param  yLabel   Description of the Parameter
	 * @param  xValues  Description of the Parameter
	 * @param  yValues  Description of the Parameter
	 */
	public Plot(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
		this(title, xLabel, yLabel, xValues, yValues, xValues, yValues, DEFAULT_FLAGS, null, null);
	}


	/**
	 *  This version of the constructor accepts double arrays.
	 *
	 * @param  title    Description of the Parameter
	 * @param  xLabel   Description of the Parameter
	 * @param  yLabel   Description of the Parameter
	 * @param  xValues  Description of the Parameter
	 * @param  yValues  Description of the Parameter
	 * @param  flags    Description of the Parameter
	 */
	//EU_HOU
	public Plot(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, int flags) {
		this(title, xLabel, yLabel, xValues != null ? Tools.toFloat(xValues) : null, yValues != null ? Tools.toFloat(yValues) : null, xValues != null ? Tools.toFloat(xValues) : null, yValues != null ? Tools.toFloat(yValues) : null, flags, null, null);
		this.horizontal = false;
	}


	/**
	 *  This version of the constructor accepts double arrays and uses the default
	 *  flags
	 *
	 * @param  title    Description of the Parameter
	 * @param  xLabel   Description of the Parameter
	 * @param  yLabel   Description of the Parameter
	 * @param  xValues  Description of the Parameter
	 * @param  yValues  Description of the Parameter
	 */
	//EU_HOU
	public Plot(String title, String xLabel, String yLabel, double[] xValues, double[] yValues) {
		this(title, xLabel, yLabel, xValues != null ? Tools.toFloat(xValues) : null, yValues != null ? Tools.toFloat(yValues) : null, xValues != null ? Tools.toFloat(xValues) : null, yValues != null ? Tools.toFloat(yValues) : null, DEFAULT_FLAGS, null, null);
	}


	/**
	 *  Sets the x-axis and y-axis range.
	 *
	 * @param  xMin  The new limits value
	 * @param  xMax  The new limits value
	 * @param  yMin  The new limits value
	 * @param  yMax  The new limits value
	 */
	public void setLimits(double xMin, double xMax, double yMin, double yMax) {
		this.xMin = xMin;
		this.xMax = xMax;
		this.yMin = yMin;
		this.yMax = yMax;
		fixedYScale = true;
		if (initialized) {
			setScale();
		}
	}


	/**
	 *  Sets the canvas size (i.e., size of the resulting ImageProcessor). By
	 *  default, the size is adjusted for the plot frame size specified in Edit>
	 *  Options>Profile Plot Options
	 *
	 * @param  width   The new size value
	 * @param  height  The new size value
	 */
	public void setSize(int width, int height) {
		if (!initialized && width > LEFT_MARGIN + RIGHT_MARGIN + 20 && height > TOP_MARGIN + BOTTOM_MARGIN + 20) {
			plotWidth = width - LEFT_MARGIN - RIGHT_MARGIN;
			plotHeight = height - TOP_MARGIN - BOTTOM_MARGIN;
		}
	}


	/**
	 *  Adds a set of points to the plot or adds a curve if shape is set to LINE.
	 *
	 * @param  x      the x-coodinates
	 * @param  y      the y-coodinates
	 * @param  shape  CIRCLE, X, BOX, TRIANGLE, CROSS, DOT or LINE
	 * @param  c      The feature to be added to the Points attribute
	 */
	public void addPoints(float[] x, float[] y, int shape, Color c) {
		setup();
		switch (shape) {
						case CIRCLE:
						case X:
						case BOX:
						case TRIANGLE:
						case CROSS:
						case DOT:
							for (int i = 0; i < x.length; i++) {
								int xt = LEFT_MARGIN + (int) ((x[i] - xMin) * xScale);
								int yt = TOP_MARGIN + frameHeight - (int) ((y[i] - yMin) * yScale);
								if (xt >= frame.x && yt >= frame.y && xt <= frame.x + frame.width && yt <= frame.y + frame.height) {
									drawShape(shape, xt, yt, markSize);
								}
							}
							break;
						case LINE:
							ip.setClipRect(frame);
							int xts[] = new int[x.length];
							int yts[] = new int[y.length];
							for (int i = 0; i < x.length; i++) {
								xts[i] = LEFT_MARGIN + (int) ((x[i] - xMin) * xScale);
								yts[i] = TOP_MARGIN + frameHeight - (int) ((y[i] - yMin) * yScale);
							}
							/*
							    EU_HOU CHANGES
							  */
							setColor(c);
							drawPolyline(ip, xts, yts, x.length);
							setColor(Color.black);
							/*
							    EU_HOU END
							  */
							ip.setClipRect(null);
							break;
		}
		multiplePlots = true;
		if (xValues.length == 1) {
			xValues = x;
			yValues = y;
			nPoints = x.length;
			drawPending = false;
		}
	}


	/*
	    EU_HOU CHANGES
	  */
	/**
	 *  Adds a feature to the Points attribute of the PlotWindow object
	 *
	 * @param  x      The feature to be added to the Points attribute
	 * @param  y      The feature to be added to the Points attribute
	 * @param  shape  The feature to be added to the Points attribute
	 */
	public void addPoints(float[] x, float[] y, int shape) {
		addPoints(x, y, shape, Color.black);
	}


	/*
	    EU_HOU END
	  */
	/**
	 *  Adds a set of points to the plot using double arrays. Must be called before
	 *  the plot is displayed.
	 *
	 * @param  x      The feature to be added to the Points attribute
	 * @param  y      The feature to be added to the Points attribute
	 * @param  shape  The feature to be added to the Points attribute
	 */
	public void addPoints(double[] x, double[] y, int shape) {
		addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  shape  Description of the Parameter
	 * @param  x      Description of the Parameter
	 * @param  y      Description of the Parameter
	 * @param  size   Description of the Parameter
	 */
	void drawShape(int shape, int x, int y, int size) {
		int xbase = x - size / 2;
		int ybase = y - size / 2;
		switch (shape) {
						case X:
							ip.drawLine(xbase, ybase, xbase + size, ybase + size);
							ip.drawLine(xbase + size, ybase, xbase, ybase + size);
							break;
						case BOX:
							ip.drawLine(xbase, ybase, xbase + size, ybase);
							ip.drawLine(xbase + size, ybase, xbase + size, ybase + size);
							ip.drawLine(xbase + size, ybase + size, xbase, ybase + size);
							ip.drawLine(xbase, ybase + size, xbase, ybase);
							break;
						case TRIANGLE:
							ip.drawLine(x, ybase, xbase + size, ybase + size);
							ip.drawLine(xbase + size, ybase + size, xbase, ybase + size);
							ip.drawLine(xbase, ybase + size, x, ybase);
							break;
						case CROSS:
							ip.drawLine(xbase, y, xbase + size, y);
							ip.drawLine(x, ybase, x, ybase + size);
							break;
						case DOT:
							ip.drawDot(x, y);
							break;
						default:// 5x5 oval
							ip.drawLine(x - 1, y - 2, x + 1, y - 2);
							ip.drawLine(x - 1, y + 2, x + 1, y + 2);
							ip.drawLine(x + 2, y + 1, x + 2, y - 1);
							ip.drawLine(x - 2, y + 1, x - 2, y - 1);
							break;
		}
	}


	/**
	 *  Adds error bars to the plot.
	 *
	 * @param  errorBars  The feature to be added to the ErrorBars attribute
	 */
	public void addErrorBars(float[] errorBars) {
		if (errorBars.length != nPoints) {
			throw new IllegalArgumentException("errorBars.length != npoints");
		}
		this.errorBars = errorBars;
	}


	/**
	 *  Adds error bars to the plot.
	 *
	 * @param  errorBars  The feature to be added to the ErrorBars attribute
	 */
	public void addErrorBars(double[] errorBars) {
		addErrorBars(Tools.toFloat(errorBars));
	}


	/**
	 *  Draws text at the specified location, where (0,0) is the upper left corner
	 *  of the the plot frame and (1,1) is the lower right corner.
	 *
	 * @param  x      The feature to be added to the Label attribute
	 * @param  y      The feature to be added to the Label attribute
	 * @param  label  The feature to be added to the Label attribute
	 */
	public void addLabel(double x, double y, String label) {
		setup();
		int xt = LEFT_MARGIN + (int) (x * frameWidth);
		int yt = TOP_MARGIN + (int) (y * frameHeight);
		ip.drawString(label, xt, yt);
		/*
		    EU_HOU CHANGES
		  */
		labels.add(label);
		xlabs.add(new Integer(xt));
		ylabs.add(new Integer(yt));
		/*
		    EU_HOU END
		  */
	}


	/*
	    Draws text at the specified location, using the coordinate system defined
	    by setLimits() and the justification specified by setJustification().
	  */
	//	public void addText(String text, double x, double y) {
	//		setup();
	//		int xt = LEFT_MARGIN + (int)((x-xMin)*xScale);
	//		int yt = TOP_MARGIN + frameHeight - (int)((y-yMin)*yScale);
	//		if (justification==CENTER)
	//			xt -= ip.getStringWidth(text)/2;
	//		else if (justification==RIGHT)
	//			xt -= ip.getStringWidth(text);
	//		ip.drawString(text, xt, yt);
	//	}

	/**
	 *  Sets the justification used by addLabel(), where <code>justification</code>
	 *  is ImageProcessor.LEFT, ImageProcessor.CENTER or ImageProcessor.RIGHT.
	 *
	 * @param  justification  The new justification value
	 */
	public void setJustification(int justification) {
		setup();
		ip.setJustification(justification);
	}


	/**
	 *  Changes the drawing color. For selecting the color of the data passed with
	 *  the constructor, use <code>setColor</code> before <code>draw</code>. The
	 *  frame and labels are always drawn in black.
	 *
	 * @param  c  The new color value
	 */
	public void setColor(Color c) {
		setup();
		if (!(ip instanceof ColorProcessor)) {
			ip = ip.convertToRGB();
			ip.setLineWidth(lineWidth);
			ip.setFont(font);
			ip.setAntialiasedText(true);
		}
		ip.setColor(c);
	}


	/**
	 *  Changes the line width.
	 *
	 * @param  lineWidth  The new lineWidth value
	 */
	public void setLineWidth(int lineWidth) {
		if (lineWidth < 1) {
			lineWidth = 1;
		}
		setup();
		ip.setLineWidth(lineWidth);
		this.lineWidth = lineWidth;
		markSize = lineWidth == 1 ? 5 : 7;
	}


	/**
	 *  Changes the font.
	 *
	 * @param  font  Description of the Parameter
	 */
	public void changeFont(Font font) {
		setup();
		ip.setFont(font);
		this.font = font;
	}


	/**
	 *  Description of the Method
	 */
	void setup() {
		if (initialized) {
			return;
		}
		initialized = true;
		createImage();
		ip.setColor(Color.black);
		if (lineWidth > 3) {
			lineWidth = 3;
		}
		ip.setLineWidth(lineWidth);
		ip.setFont(font);
		ip.setAntialiasedText(true);
		if (frameWidth == 0) {
			frameWidth = plotWidth;
			frameHeight = plotHeight;
		}
		frame = new Rectangle(LEFT_MARGIN, TOP_MARGIN, frameWidth, frameHeight);
		setScale();
		if (PlotWindow.noGridLines) {
			drawAxisLabels();
		} else {
			drawTicksEtc();
		}
	}


	/**
	 *  Sets the scale attribute of the Plot object
	 */
	void setScale() {
		if ((xMax - xMin) == 0.0) {
			xScale = 1.0;
		} else {
			xScale = frame.width / (xMax - xMin);
		}
		if ((yMax - yMin) == 0.0) {
			yScale = 1.0;
		} else {
			yScale = frame.height / (yMax - yMin);
		}
	}


	/**
	 *  Description of the Method
	 */
	void drawAxisLabels() {
		int digits = getDigits(yMin, yMax);
		String s = IJ.d2s(yMax, digits);
		int sw = ip.getStringWidth(s);
		if ((sw + 4) > LEFT_MARGIN) {
			ip.drawString(s, 4, TOP_MARGIN - 4);
		} else {
			ip.drawString(s, LEFT_MARGIN - ip.getStringWidth(s) - 4, TOP_MARGIN + 10);
		}
		s = IJ.d2s(yMin, digits);
		sw = ip.getStringWidth(s);
		if ((sw + 4) > LEFT_MARGIN) {
			ip.drawString(s, 4, TOP_MARGIN + frame.height);
		} else {
			ip.drawString(s, LEFT_MARGIN - ip.getStringWidth(s) - 4, TOP_MARGIN + frame.height);
		}
		FontMetrics fm = ip.getFontMetrics();
		int x = LEFT_MARGIN;
		int y = TOP_MARGIN + frame.height + fm.getAscent() + 6;
		digits = getDigits(xMin, xMax);
		ip.drawString(IJ.d2s(xMin, digits), x, y);
		s = IJ.d2s(xMax, digits);
		ip.drawString(s, x + frame.width - ip.getStringWidth(s) + 6, y);
		ip.drawString(xLabel, LEFT_MARGIN + (frame.width - ip.getStringWidth(xLabel)) / 2, y + 3);
		drawYLabel(yLabel, LEFT_MARGIN - 4, TOP_MARGIN, frame.height, fm);
	}


	/**
	 *  Description of the Method
	 */
	void drawTicksEtc() {
		int fontAscent = ip.getFontMetrics().getAscent();
		int fontMaxAscent = ip.getFontMetrics().getMaxAscent();
		if ((flags & (X_NUMBERS + X_TICKS + X_GRID)) != 0) {
			double step = Math.abs(Math.max(frame.width / MAX_INTERVALS, MIN_X_GRIDWIDTH) / xScale);//the smallest allowable increment
			step = niceNumber(step);
			int i1;
			int i2;
			if ((flags & X_FORCE2GRID) != 0) {
				i1 = (int) Math.floor(Math.min(xMin, xMax) / step + 1.e-10);//this also allows for inverted xMin, xMax
				i2 = (int) Math.ceil(Math.max(xMin, xMax) / step - 1.e-10);
				xMin = i1 * step;
				xMax = i2 * step;
				xScale = frame.width / (xMax - xMin);//rescale to make it fit
			} else {
				i1 = (int) Math.ceil(Math.min(xMin, xMax) / step - 1.e-10);
				i2 = (int) Math.floor(Math.max(xMin, xMax) / step + 1.e-10);
			}
			int digits = -(int) Math.floor(Math.log(step) / Math.log(10) + 1e-6);
			if (digits < 0) {
				digits = 0;
			}
			int y1 = TOP_MARGIN;
			int y2 = TOP_MARGIN + frame.height;
			int yNumbers = y2 + fontAscent + 7;
			for (int i = 0; i <= (i2 - i1); i++) {
				double v = (i + i1) * step;
				int x = (int) Math.round((v - xMin) * xScale) + LEFT_MARGIN;
				if ((flags & X_GRID) != 0) {
					ip.setColor(gridColor);
					ip.drawLine(x, y1, x, y2);
					ip.setColor(Color.black);
				}
				if ((flags & X_TICKS) != 0) {
					ip.drawLine(x, y1, x, y1 + TICK_LENGTH);
					ip.drawLine(x, y2, x, y2 - TICK_LENGTH);
				}
				if ((flags & X_NUMBERS) != 0) {
					String s = IJ.d2s(v, digits);
					ip.drawString(s, x - ip.getStringWidth(s) / 2, yNumbers);
				}
			}
		}
		int maxNumWidth = 0;
		if ((flags & (Y_NUMBERS + Y_TICKS + Y_GRID)) != 0) {
			double step = Math.abs(Math.max(frame.width / MAX_INTERVALS, MIN_Y_GRIDWIDTH) / yScale);//the smallest allowable increment
			step = niceNumber(step);
			int i1;
			int i2;
			if ((flags & X_FORCE2GRID) != 0) {
				i1 = (int) Math.floor(Math.min(yMin, yMax) / step + 1.e-10);//this also allows for inverted xMin, xMax
				i2 = (int) Math.ceil(Math.max(yMin, yMax) / step - 1.e-10);
				yMin = i1 * step;
				yMax = i2 * step;
				yScale = frame.height / (yMax - yMin);//rescale to make it fit
			} else {
				i1 = (int) Math.ceil(Math.min(yMin, yMax) / step - 1.e-10);
				i2 = (int) Math.floor(Math.max(yMin, yMax) / step + 1.e-10);
			}
			int digits = -(int) Math.floor(Math.log(step) / Math.log(10) + 1e-6);
			if (digits < 0) {
				digits = 0;
			}
			int x1 = LEFT_MARGIN;
			int x2 = LEFT_MARGIN + frame.width;
			for (int i = 0; i <= (i2 - i1); i++) {
				double v = (i + i1) * step;
				int y = TOP_MARGIN + frame.height - (int) Math.round((v - yMin) * yScale);
				if ((flags & Y_GRID) != 0) {
					ip.setColor(gridColor);
					ip.drawLine(x1, y, x2, y);
					ip.setColor(Color.black);
				}
				if ((flags & Y_TICKS) != 0) {
					ip.drawLine(x1, y, x1 + TICK_LENGTH, y);
					ip.drawLine(x2, y, x2 - TICK_LENGTH, y);
				}
				if ((flags & Y_NUMBERS) != 0) {
					String s = IJ.d2s(v, digits);
					int w = ip.getStringWidth(s);
					if (w > maxNumWidth) {
						maxNumWidth = w;
					}
					ip.drawString(s, LEFT_MARGIN - w - 4, y + fontMaxAscent / 2 + 1);
				}
			}
		}
		if ((flags & Y_NUMBERS) == 0) {//simply note y-axis min&max
			int digits = getDigits(yMin, yMax);
			String s = IJ.d2s(yMax, digits);
			int sw = ip.getStringWidth(s);
			if ((sw + 4) > LEFT_MARGIN) {
				ip.drawString(s, 4, TOP_MARGIN - 4);
			} else {
				ip.drawString(s, LEFT_MARGIN - ip.getStringWidth(s) - 4, TOP_MARGIN + 10);
			}
			s = IJ.d2s(yMin, digits);
			sw = ip.getStringWidth(s);
			if ((sw + 4) > LEFT_MARGIN) {
				ip.drawString(s, 4, TOP_MARGIN + frame.height);
			} else {
				ip.drawString(s, LEFT_MARGIN - ip.getStringWidth(s) - 4, TOP_MARGIN + frame.height);
			}
		}
		FontMetrics fm = ip.getFontMetrics();
		int x = LEFT_MARGIN;
		int y = TOP_MARGIN + frame.height + fm.getAscent() + 6;
		if ((flags & X_NUMBERS) == 0) {//simply note x-axis min&max
			int digits = getDigits(xMin, xMax);
			ip.drawString(IJ.d2s(xMin, digits), x, y);
			String s = IJ.d2s(xMax, digits);
			ip.drawString(s, x + frame.width - ip.getStringWidth(s) + 6, y);
		} else {
			y += fm.getAscent();
		}//space needed for x numbers
                drawXLabel(xLabel, 270, 235, 0, fm);
		//ip.drawString(xLabel, LEFT_MARGIN + (frame.width - ip.getStringWidth(xLabel)) / 2, y + 6);
		drawYLabel(yLabel, LEFT_MARGIN - maxNumWidth - 4, TOP_MARGIN, frame.height, fm);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  v  Description of the Parameter
	 * @return    Description of the Return Value
	 */
	double niceNumber(double v) {//the smallest "nice" number >= v. "Nice" numbers are .. 0.5, 1, 2, 5, 10, 20 ...
		double base = Math.pow(10, Math.floor(Math.log(v) / Math.log(10) - 1.e-6));
		if (v > 5.0000001 * base) {
			return 10 * base;
		} else if (v > 2.0000001 * base) {
			return 5 * base;
		} else {
			return 2 * base;
		}
	}


	/**
	 *  Description of the Method
	 */
	void createImage() {
		if (ip != null) {
			return;
		}
		int width = plotWidth + LEFT_MARGIN + RIGHT_MARGIN;
		int height = plotHeight + TOP_MARGIN + BOTTOM_MARGIN;
		byte[] pixels = new byte[width * height];
		for (int i = 0; i < width * height; i++) {
			pixels[i] = (byte) 255;
		}
		ip = new ByteProcessor(width, height, pixels, null);
	}


	/**
	 *  Gets the digits attribute of the Plot object
	 *
	 * @param  n1  Description of the Parameter
	 * @param  n2  Description of the Parameter
	 * @return     The digits value
	 */
	int getDigits(double n1, double n2) {
		if (Math.round(n1) == n1 && Math.round(n2) == n2) {
			return 0;
		} else {
			n1 = Math.abs(n1);
			n2 = Math.abs(n2);
			double n = n1 < n2 && n1 > 0.0 ? n1 : n2;
			double diff = Math.abs(n2 - n1);
			if (diff > 0.0 && diff < n) {
				n = diff;
			}
			int digits = 1;
			if (n < 10.0) {
				digits = 2;
			}
			if (n < 0.01) {
				digits = 3;
			}
			if (n < 0.001) {
				digits = 4;
			}
			if (n < 0.0001) {
				digits = 5;
			}
			return digits;
		}
	}


	/**
	 *  Draws the plot specified in the constructor.
	 */
	public void draw() {
		int x;
		int y;
		double v;

		if (plotDrawn) {
			return;
		}
		plotDrawn = true;
		createImage();
		setup();

		if (drawPending) {
			ip.setClipRect(frame);
			//EU_HOU
			Xpoints = new int[nPoints];
			Ypoints = new int[nPoints];
			for (int i = 0; i < nPoints; i++) {
				Xpoints[i] = LEFT_MARGIN + (int) ((xValues[i] - xMin) * xScale);
				Ypoints[i] = TOP_MARGIN + frame.height - (int) ((yValues[i] - yMin) * yScale);
			}
			drawPolyline(ip, Xpoints, Ypoints, nPoints);
			ip.setClipRect(null);
			if (this.errorBars != null) {
				//EU_HOU
				int[] xpoints = new int[2];
				int[] ypoints = new int[2];
				for (int i = 0; i < nPoints; i++) {
					xpoints[0] = xpoints[1] = LEFT_MARGIN + (int) ((xValues[i] - xMin) * xScale);
					ypoints[0] = TOP_MARGIN + frame.height - (int) ((yValues[i] - yMin - errorBars[i]) * yScale);
					ypoints[1] = TOP_MARGIN + frame.height - (int) ((yValues[i] - yMin + errorBars[i]) * yScale);
					drawPolyline(ip, xpoints, ypoints, 2);
				}
			}
		}

		if (ip instanceof ColorProcessor) {
			ip.setColor(Color.black);
		}
		ip.drawRect(frame.x, frame.y, frame.width + 1, frame.height + 1);
	}


	/**
	 *  Description of the Method
	 *
	 * @param  ip  Description of the Parameter
	 * @param  x   Description of the Parameter
	 * @param  y   Description of the Parameter
	 * @param  n   Description of the Parameter
	 */
	void drawPolyline(ImageProcessor ip, int[] x, int[] y, int n) {
		ip.moveTo(x[0], y[0]);
		for (int i = 0; i < n; i++) {
			ip.lineTo(x[i], y[i]);
		}
	}



	/**
	 *  Description of the Method
	 *
	 * @param  yLabel  Description of the Parameter
	 * @param  x       Description of the Parameter
	 * @param  y       Description of the Parameter
	 * @param  height  Description of the Parameter
	 * @param  fm      Description of the Parameter
	 */
	void drawYLabel(String yLabel, int x, int y, int height, FontMetrics fm) {
		if (yLabel.equals("")) {
			return;
		}
		int w = fm.stringWidth(yLabel) + 5;
		int h = fm.getHeight() + 5;
		ImageProcessor label = new ByteProcessor(w, h);
		label.setColor(Color.white);
		label.fill();
		label.setColor(Color.black);
		label.setFont(font);
		label.setAntialiasedText(true);
		int descent = fm.getDescent();
		label.drawString(yLabel, 0, h - descent);
		label = label.rotateLeft();
		int y2 = y + (height - ip.getStringWidth(yLabel)) / 2;
		if (y2 < y) {
			y2 = y;
		}
		int x2 = Math.max(x - h, 0);
		ip.insert(label, x2, y2);
	}

        void drawXLabel(String yLabel, int x, int y, int height, FontMetrics fm) {
		if (yLabel.equals("")) {
			return;
		}
		int w = fm.stringWidth(yLabel) + 5;
		int h = fm.getHeight() + 5;
		ImageProcessor label = new ByteProcessor(w, h);
		label.setColor(Color.white);
		label.fill();
		label.setColor(Color.black);
		label.setFont(font);
		label.setAntialiasedText(true);
		int descent = fm.getDescent();
		label.drawString(yLabel, 0, h - descent);
		int y2 = y + (height - ip.getStringWidth(yLabel)) / 2;
		if (y2 < y) {
			y2 = y;
		}
		int x2 = Math.max(x - h, 0);
		ip.insert(label, x2, y2);
	}

	/**
	 *  Gets the blankProcessor attribute of the Plot object
	 *
	 * @return    The blankProcessor value
	 */
	ImageProcessor getBlankProcessor() {
		createImage();
		return ip;
	}


	/**
	 *  Gets the coordinates attribute of the Plot object
	 *
	 * @param  x  Description of the Parameter
	 * @param  y  Description of the Parameter
	 * @return    The coordinates value
	 */
	String getCoordinates(int x, int y) {
		String text = "";
		if (!frame.contains(x, y)) {
			return text;
		}
		if (fixedYScale || multiplePlots) {// display cursor location
			double xv = (x - LEFT_MARGIN) / xScale + xMin;
			double yv = (TOP_MARGIN + frameHeight - y) / yScale + yMin;
			text = "X=" + IJ.d2s(xv, getDigits(xv, xv)) + ", Y=" + IJ.d2s(yv, getDigits(yv, yv));
		} else {// display x and f(x)
			int index = (int) ((x - frame.x) / ((double) frame.width / nPoints));
			if (index > 0 && index < nPoints) {
				double xv = xValues[index];
				double yv = yValues[index];
				text = "X=" + IJ.d2s(xv, getDigits(xv, xv)) + ", Y=" + IJ.d2s(yv, getDigits(yv, yv));
			}
		}
		return text;
	}


	/**
	 *  Returns the plot as an ImageProcessor.
	 *
	 * @return    The processor value
	 */
	public ImageProcessor getProcessor() {
		draw();
		return ip;
	}


	/**
	 *  Returns the plot as an ImagePlus.
	 *
	 * @return    The imagePlus value
	 */
	public ImagePlus getImagePlus() {
		draw();
		ImagePlus img = new ImagePlus(title, ip);
		Calibration cal = img.getCalibration();
		cal.xOrigin = LEFT_MARGIN;
		cal.yOrigin = TOP_MARGIN + frameHeight + yMin * yScale;
		cal.pixelWidth = 1.0 / xScale;
		cal.pixelHeight = 1.0 / yScale;
		cal.setInvertY(true);
		return img;
	}


	/**
	 *  Displays the plot in a PlotWindow and returns a reference to the
	 *  PlotWindow.
	 *
	 * @return    Description of the Return Value
	 */
	public PlotWindow show() {
		draw();
		if (Prefs.useInvertingLut && (ip instanceof ByteProcessor) && !Interpreter.isBatchMode() && IJ.getInstance() != null) {
			ip.invertLut();
			ip.invert();
		}
		if ((IJ.macroRunning() && IJ.getInstance() == null) || Interpreter.isBatchMode()) {
			ImagePlus imp = new ImagePlus(title, ip);
			WindowManager.setTempCurrentImage(imp);
			Interpreter.addBatchModeImage(imp);
			return null;
		}
		ImageWindow.centerNextImage();

		// EU_HOU
		imp = getImagePlus();
		return new PlotWindow(this, imp);
	}

	// EU_HOU
	/**
	 *  Description of the Method
	 *
	 * @param  x  Description of the Parameter
	 * @param  y  Description of the Parameter
	 */
	public void updatePlot(int x, int y) {
		// EU_HOU
		//System.out.println("PlotWindow mousemoved 0");
		if (frame == null) {
			return;
		}

		int xOrigine = 0;

		//if ((region != null) && (horizontal)) {
		//	xOrigine = ((Line) region).getX();
		//}

		if (frame.contains(oldX - 1, oldY)) {
			//System.out.println("PlotWindow mousemoved 1");
			ip.setColor(Color.white);
			// EU_HOU
			if (imp != null) {
				Roi region = imp.getRoi();
				imp.setRoi(frame);
				ip.fill();
				imp.setRoi(region);
			}

			/*
			    ip.drawLine(oldX,frame.y ,oldX,frame.y+frame.height);
			    ip.drawLine(frame.x,Ypoints[oldindex],frame.x+frame.width,Ypoints[oldindex]);
			  */
			ip.setColor(Color.black);
			drawPolyline(ip, Xpoints, Ypoints, nPoints);
			ip.setColor(Color.black);
			ip.drawRect(frame.x, frame.y, frame.width + 1, frame.height + 1);
			ip.setAntialiasedText(true);
			Enumeration e = labels.elements();
			int i = 0;
			while (e.hasMoreElements()) {
				int xxx = ((Integer) xlabs.elementAt(i)).intValue();
				int yyy = ((Integer) ylabs.elementAt(i++)).intValue();
				ip.drawString((String) e.nextElement(), xxx, yyy);
			}
			// EU_HOU
			if (imp != null) {
				imp.updateAndDraw();
			}
		}
		if (frame.contains(x, y)) {
			//System.out.println("PlotWindow mousemoved 2");
			int index = (int) ((x - frame.x) / ((double) frame.width / xValues.length));
			ip.setColor(Color.gray.brighter());
			ip.drawLine(x, frame.y, x, frame.y + frame.height);
			ip.drawLine(frame.x, Ypoints[index], frame.x + frame.width, Ypoints[index]);
			ip.setColor(Color.black);

			ip.drawRect(frame.x, frame.y, frame.width + 1, frame.height + 1);
			// EU_HOU
			if (imp != null) {
				imp.updateAndDraw();
			}
			if (index > 0 && index < xValues.length) {
				double xv;
				xv = xValues[index];
				//if(horizontal) xv-= xOrigine;
				double yv = yValues[index];
				//ip.drawPixel( LEFT_MARGIN + (int)((xv-xMin)*xScale),TOP_MARGIN + frame.height - (int)((yv-yMin)*yScale));


				if ((origin != null) && (region != null) && (region.getType() == Roi.LINE)) {
					origin.getWindow().getCanvas().setRoi(region);
					Line lr = (Line) region;
					if (horizontal) {
						xv -= xOrigine;
					}
					// dessin du petit point dans l'image originale
					double c = xv / (origin.getRoi().getLength());
					int xi = lr.x1 + (int) (c * (lr.x2 - lr.x1) + 0.5);
					int yi = lr.y1 + (int) (c * (lr.y2 - lr.y1) + 0.5);
					origin.getWindow().getCanvas().setPoint(xi, yi, region);
				}
				oldindex = index;
			}
			//coordinates.setText("X=" + d2s(x/xScale+xMin)+", Y=" + d2s((frameHeight-y)/yScale+yMin));
		}

		oldX = x;
		oldY = y;

		if (yValuesBaseLine != null) {
			addPoints(xValues, yValuesBaseLine, LINE, Color.red);
		}
		if (yValuesMergeGaussFit != null) {
			addPoints(xValues, yValuesMergeGaussFit, LINE, Color.red);
		}
		//displayGaussianFit();
		if (ZERO_LINE) {
			addPoints(xValues, new float[xValues.length], LINE, Color.blue);
		}
		drawTicksEtc();
	}
	// EU_HOU

	//EU_HOU
	/**
	 *  Description of the Method
	 */
	public void killOverlayOrigin() {
		if ((origin != null) && (region != null) && (region.getType() == Roi.LINE)) {

			origin.getWindow().getCanvas().setPoint(-1, -1, null);
		}
	}
	//EU_HOU
}


