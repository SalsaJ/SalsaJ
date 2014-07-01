package ij.plugin.filter;
//import ij.plugin.filter.PlugInFilter;
import ij.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ij.text.TextWindow;
import ij.measure.*;

/**
 *  This plugin displays a calibration bar on the active image. Bob Dougherty,
 *  OptiNav, Inc., 4/14/2002 Based largely on HistogramWindow.java by Wayne
 *  Rasband. Version 0 4/14/2002 Version 1 6/14/2002 Many revisions, including
 *  addition of the legend to the image. Version 2 6/15/2002 Control over
 *  colors, including none. Automatic box width. Version 3 6/17/2002 0 decimal
 *  places by default. Option for manual box width. July 2002: Modified by
 *  Daniel Marsh and renamed CalibrationBar.
 *
 *@author     Thomas
 *@created    30 novembre 2007
 */

public class CalibrationBar implements PlugInFilter {
	final static int BAR_LENGTH = 128;
	final static int BAR_THICKNESS = 12;
	final static int XMARGIN = 10;
	final static int YMARGIN = 10;
	final static int WIN_HEIGHT = BAR_LENGTH;
	final static int BOX_PAD = 0;
	final static int LINE_WIDTH = 1;
	static int nBins = 256;
	final static String[] colorKeys = {"WhiteColor", "LGrayColor", "DGrayColor", "BlackColor", "RedColor", "GreenColor", "BlueColor", "YellowColor", "ColorNone"};
	final static String[] locationKeys = {"CaliUpR", "CaliDoR", "CaliDoL", "CaliUpL", "CaliSel"};
	static String[] colors;
	static String[] locations;
	final static int UPPER_RIGHT = 0, LOWER_RIGHT = 1, LOWER_LEFT = 2, UPPER_LEFT = 3, AT_SELECTION = 4;

	static String fillColor;//= colors[0];
	static String textColor;//= colors[3];
	static String location;//= locations[UPPER_RIGHT];
	static double zoom = 1;
	static int numLabels = 5;
	static int fontSize = 12;
	static int decimalPlaces = 0;

	ImagePlus imp;
	ImagePlus impOriginal;

	LiveDialog gd;

	ImageStatistics stats;
	Calibration cal;
	int[] histogram;
	LookUpTable lut;
	Image img;
	Button setup, redraw, insert, unInsert;
	Checkbox ne, nw, se, sw;
	CheckboxGroup locGroup;
	Label value, note;
	int newMaxCount;
	boolean logScale;
	int win_width;
	int userPadding = 0;
	int fontHeight = 0;
	boolean boldText = false;
	Object backupPixels;
	byte[] byteStorage;
	int[] intStorage;
	short[] shortStorage;
	float[] floatStorage;
	String boxOutlineColor;// = colors[8];
	String barOutlineColor;// = colors[3];

	ImagePlus impData;
	ImageProcessor ip;
	String[] fieldNames = null;
	int insetPad;
	boolean decimalPlacesChanged;


	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		/*
		 *  EU_HOU CHANGES
		 */
		colors = new String[colorKeys.length];
		locations = new String[locationKeys.length];
		for (int i = 0; i < colorKeys.length; ++i) {
			//EU_HOU Bundle
			colors[i] = IJ.getColorBundle().getString(colorKeys[i]);
		}
		for (int i = 0; i < locationKeys.length; ++i) {
			//EU_HOU Bundle
			locations[i] = IJ.getPluginBundle().getString(locationKeys[i]);
		}
		boxOutlineColor = new String(colors[8]);
		barOutlineColor = new String(colors[3]);
		fillColor = new String(colors[0]);
		textColor = new String(colors[3]);
		location = new String(locations[0]);
		/*
		 *  EU_HOU END
		 */
		if (imp != null) {
			this.imp = imp;
			impData = imp;
			if (imp.getRoi() != null) {
				location = locations[AT_SELECTION];
			}
		}
		return DOES_ALL - DOES_RGB + NO_CHANGES;
	}


	/**
	 *  Main processing method for the CalibrationBar object
	 *
	 *@param  ipPassed  Description of the Parameter
	 */
	public void run(ImageProcessor ipPassed) {
	ImageCanvas ic = imp.getCanvas();
	double mag = (ic != null) ? ic.getMagnification() : 1.0;
		if (zoom <= 1 && mag < 1) {
			zoom = (double) 1.0 / mag;
		}
		ip = ipPassed.duplicate().convertToRGB();
		impOriginal = imp;
		//EU_HOU Bundle
		imp = new ImagePlus(imp.getTitle() + " " + IJ.getPluginBundle().getString("CaliWithBar"), ip);
		imp.setCalibration(impData.getCalibration());
		if (impOriginal.getRoi() != null) {
			imp.setRoi(impOriginal.getRoi());
		}
		imp.show();
		ip.snapshot();
		insetPad = imp.getWidth() / 50;
		if (insetPad < 4) {
			insetPad = 4;
		}
		updateColorBar();
		if (!showDialog()) {
			imp.hide();
			ip.reset();
			this.imp.updateAndDraw();
			return;
		}
		updateColorBar();

	}


	/**
	 *  Description of the Method
	 */
	private void updateColorBar() {
		ip.reset();
	Roi roi = imp.getRoi();
		if (roi != null && location.equals(locations[AT_SELECTION])) {
		Rectangle r = roi.getBounds();
			drawColorBar(imp, r.x, r.y);
		} else if (location.equals(locations[UPPER_LEFT])) {
			drawColorBar(imp, insetPad, insetPad);
		} else if (location.equals(locations[UPPER_RIGHT])) {
			calculateWidth();
			drawColorBar(imp, imp.getWidth() - insetPad - win_width, insetPad);
		} else if (location.equals(locations[LOWER_LEFT])) {
			drawColorBar(imp, insetPad, imp.getHeight() - (int) (WIN_HEIGHT * zoom + 2 * (int) (YMARGIN * zoom)) - (int) (insetPad * zoom));
		} else if (location.equals(locations[LOWER_RIGHT])) {
			calculateWidth();
			drawColorBar(imp, imp.getWidth() - win_width - insetPad,
					imp.getHeight() - (int) (WIN_HEIGHT * zoom + 2 * (int) (YMARGIN * zoom)) - insetPad);
		}

		this.imp.updateAndDraw();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private boolean showDialog() {
		//EU_HOU Bundle
		gd = new LiveDialog(IJ.getPluginBundle().getString("CaliTitle"));
		gd.addChoice(IJ.getPluginBundle().getString("CaliLoc") + ":", locations, location);
		gd.addChoice(IJ.getPluginBundle().getString("CaliFill") + ":", colors, fillColor);
		gd.addChoice(IJ.getPluginBundle().getString("CaliLab") + ":", colors, textColor);
		gd.addNumericField(IJ.getPluginBundle().getString("CaliNb") + ":", numLabels, 0);
		gd.addNumericField(IJ.getPluginBundle().getString("CaliPlaces") + ":", decimalPlaces, 0);
		gd.addNumericField(IJ.getPluginBundle().getString("CaliSize") + ":", fontSize, 0);
		gd.addNumericField(IJ.getPluginBundle().getString("CaliZoom") + ":", zoom, 1);
		gd.addCheckbox(IJ.getPluginBundle().getString("CaliBold"), boldText);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		location = gd.getNextChoice();
		fillColor = gd.getNextChoice();
		textColor = gd.getNextChoice();
		numLabels = (int) gd.getNextNumber();
		decimalPlaces = (int) gd.getNextNumber();
		fontSize = (int) gd.getNextNumber();
		zoom = (double) gd.getNextNumber();
		boldText = gd.getNextBoolean();
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip         Description of the Parameter
	 *@param  x          Description of the Parameter
	 *@param  y          Description of the Parameter
	 *@param  thickness  Description of the Parameter
	 *@param  length     Description of the Parameter
	 */
	public void verticalColorBar(ImageProcessor ip, int x, int y, int thickness, int length) {
	int width = thickness;
	int height = length;
	byte[] rLUT;
	byte[] gLUT;
	byte[] bLUT;
	int mapSize = 0;
	java.awt.image.ColorModel cm = lut.getColorModel();
		if (cm instanceof IndexColorModel) {
		IndexColorModel m = (IndexColorModel) cm;
			mapSize = m.getMapSize();
			rLUT = new byte[mapSize];
			gLUT = new byte[mapSize];
			bLUT = new byte[mapSize];
			m.getReds(rLUT);
			m.getGreens(gLUT);
			m.getBlues(bLUT);
		} else {
			mapSize = 256;
			rLUT = new byte[mapSize];
			gLUT = new byte[mapSize];
			bLUT = new byte[mapSize];
			for (int i = 0; i < mapSize; i++) {
				rLUT[i] = (byte) i;
				gLUT[i] = (byte) i;
				bLUT[i] = (byte) i;
			}
		}
	double colors = mapSize;
	int start = 0;
	ImageProcessor ipOrig = impOriginal.getProcessor();
		if (ipOrig instanceof ByteProcessor) {
		int min = (int) ipOrig.getMin();
			if (min < 0) {
				min = 0;
			}
		int max = (int) ipOrig.getMax();
			if (max > 255) {
				max = 255;
			}
			colors = max - min + 1;
			start = min;
		}
		for (int i = 0; i < (int) (BAR_LENGTH * zoom); i++) {
		int iMap = start + (int) Math.round((i * colors) / (BAR_LENGTH * zoom));
			if (iMap >= mapSize) {
				iMap = mapSize - 1;
			}

			ip.setColor(new Color(rLUT[iMap] & 0xff, gLUT[iMap] & 0xff, bLUT[iMap] & 0xff));
		int j = (int) (BAR_LENGTH * zoom) - i - 1;
			ip.drawLine(x, j + y, thickness + x, j + y);
		}

	Color c = getColor(barOutlineColor);
		if (c != null) {
			ip.setColor(c);
			ip.moveTo(x, y);
			ip.lineTo(x + width, y);
			ip.lineTo(x + width, y + height);
			ip.lineTo(x, y + height);
			ip.lineTo(x, y);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip       Description of the Parameter
	 *@param  xOffset  Description of the Parameter
	 *@param  yOffset  Description of the Parameter
	 */
	protected void drawColorBar(ImageProcessor ip, int xOffset, int yOffset) {
	int x;
	int y;

		ip.setColor(Color.black);
		if (decimalPlaces == -1) {
			decimalPlaces = Analyzer.getPrecision();
		}
		x = (int) (XMARGIN * zoom) + xOffset;
		y = (int) (YMARGIN * zoom) + yOffset;

		verticalColorBar(ip, x, y, (int) (BAR_THICKNESS * zoom), (int) (BAR_LENGTH * zoom));
		drawText(ip, x + (int) (BAR_THICKNESS * zoom), y, true);

	Color c = getColor(boxOutlineColor);
		//EU_HOU Bundle
		if (c != null && !fillColor.equals(IJ.getColorBundle().getString("ColorNone"))) {
			ip.setColor(c);
			ip.setLineWidth(LINE_WIDTH);
			ip.moveTo(xOffset + BOX_PAD, yOffset + BOX_PAD);
			ip.lineTo(xOffset + win_width - BOX_PAD, yOffset + BOX_PAD);
			ip.lineTo(xOffset + win_width - BOX_PAD, yOffset + (int) (WIN_HEIGHT * zoom + 2 * (int) (YMARGIN * zoom)) - BOX_PAD);
			ip.lineTo(xOffset + BOX_PAD, yOffset + (int) (WIN_HEIGHT * zoom + 2 * (int) (YMARGIN * zoom)) - BOX_PAD);
			ip.lineTo(xOffset + BOX_PAD, yOffset + BOX_PAD);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip      Description of the Parameter
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  active  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	int drawText(ImageProcessor ip, int x, int y, boolean active) {

	Color c = getColor(textColor);
		if (c == null) {
			return 0;
		}
		ip.setColor(c);

	double hmin = cal.getCValue(stats.histMin);
	double hmax = cal.getCValue(stats.histMax);
	double barStep = (double) (BAR_LENGTH * zoom);
		if (numLabels > 2) {
			barStep /= (numLabels - 1);
		}

	int fontType = boldText ? Font.BOLD : Font.PLAIN;
	Font font = null;
		if (fontSize < 9) {
			font = new Font("SansSerif", fontType, 9);
		} else {
			font = new Font("SansSerif", fontType, (int) (fontSize * zoom));
		}
		ip.setFont(font);
		ip.setAntialiasedText(true);
	int maxLength = 0;

	//Blank offscreen image for font metrics
	Image img = GUI.createBlankImage(128, 64);
	Graphics g = img.getGraphics();
	FontMetrics metrics = g.getFontMetrics(font);
		fontHeight = metrics.getHeight();

		for (int i = 0; i < numLabels; i++) {
		double yLabelD = (int) (YMARGIN * zoom + BAR_LENGTH * zoom - i * barStep - 1);
		int yLabel = (int) (Math.round(y + BAR_LENGTH * zoom - i * barStep - 1));
		Calibration cal = impOriginal.getCalibration();
		//s = cal.getValueUnit();
		ImageProcessor ipOrig = impOriginal.getProcessor();
		double min = ipOrig.getMin();
		double max = ipOrig.getMax();
			if (ipOrig instanceof ByteProcessor) {
				if (min < 0) {
					min = 0;
				}
				if (max > 255) {
					max = 255;
				}
			}

		double grayLabel = min + (max - min) / (numLabels - 1) * i;
			if (cal.calibrated()) {
				grayLabel = cal.getCValue(grayLabel);
			double cmin = cal.getCValue(min);
			double cmax = cal.getCValue(max);
				if (!decimalPlacesChanged && decimalPlaces == 0 && ((int) cmax != cmax || (int) cmin != cmin)) {
					decimalPlaces = 2;
				}
			}

			if (active) {
				ip.drawString(d2s(grayLabel), x + 5, yLabel + fontHeight / 2);
			}

		int iLength = metrics.stringWidth(d2s(grayLabel));
			if (iLength > maxLength) {
				maxLength = iLength;
			}
		}
		return maxLength;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  d  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	String d2s(double d) {
		return IJ.d2s(d, decimalPlaces);
	}


	/**
	 *  Gets the fontHeight attribute of the CalibrationBar object
	 *
	 *@return    The fontHeight value
	 */
	int getFontHeight() {
	Image img = GUI.createBlankImage(64, 64);//dummy version to get fontHeight
	Graphics g = img.getGraphics();
	int fontType = boldText ? Font.BOLD : Font.PLAIN;
	Font font = new Font("SansSerif", fontType, (int) (fontSize * zoom));
	FontMetrics metrics = g.getFontMetrics(font);
		return metrics.getHeight();
	}


	/**
	 *  Gets the color attribute of the CalibrationBar object
	 *
	 *@param  color  Description of the Parameter
	 *@return        The color value
	 */
	Color getColor(String color) {
	Color c = Color.white;
		if (color.equals(colors[1])) {
			c = Color.lightGray;
		} else if (color.equals(colors[2])) {
			c = Color.darkGray;
		} else if (color.equals(colors[3])) {
			c = Color.black;
		} else if (color.equals(colors[4])) {
			c = Color.red;
		} else if (color.equals(colors[5])) {
			c = Color.green;
		} else if (color.equals(colors[6])) {
			c = Color.blue;
		} else if (color.equals(colors[7])) {
			c = Color.yellow;
		} else if (color.equals(colors[8])) {
			c = null;
		}
		return c;
	}


	/**
	 *  Description of the Method
	 */
	void calculateWidth() {
		drawColorBar(imp, -1, -1);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  x    Description of the Parameter
	 *@param  y    Description of the Parameter
	 */
	public void drawColorBar(ImagePlus imp, int x, int y) {
	Roi roi = impOriginal.getRoi();
		if (roi != null) {
			impOriginal.killRoi();
		}
		stats = impOriginal.getStatistics(Measurements.MIN_MAX, nBins);
		if (roi != null) {
			impOriginal.setRoi(roi);
		}
		histogram = stats.histogram;
		lut = impOriginal.createLut();
		cal = impOriginal.getCalibration();

	int maxTextWidth = drawText(ip, 0, 0, false);
		win_width = (int) (XMARGIN * zoom) + 5 + (int) (BAR_THICKNESS * zoom) + maxTextWidth + (int) ((XMARGIN / 2) * zoom);
		if (x == -1 && y == -1) {
			return;
		}// return if calculating width

	Color c = getColor(fillColor);
		if (c != null) {
			ip.setColor(c);
			ip.setRoi(x, y, win_width, (int) (WIN_HEIGHT * zoom + 2 * (int) (YMARGIN * zoom)));
			ip.fill();
		}
		ip.resetRoi();

		drawColorBar(ip, x, y);
		imp.updateAndDraw();

		ip.setColor(Color.black);

	}


	/**
	 *  Description of the Class
	 *
	 *@author     Thomas
	 *@created    30 novembre 2007
	 */
	class LiveDialog extends GenericDialog {

		/**
		 *  Constructor for the LiveDialog object
		 *
		 *@param  title  Description of the Parameter
		 */
		LiveDialog(String title) {
			super(title);
		}


		/**
		 *  Description of the Method
		 *
		 *@param  e  Description of the Parameter
		 */
		public void textValueChanged(TextEvent e) {

			if (fieldNames == null) {
				fieldNames = new String[4];
				for (int i = 0; i < 4; i++) {
					fieldNames[i] = ((TextField) numberField.elementAt(i)).getName();
				}
			}

		TextField tf = (TextField) e.getSource();
		String name = tf.getName();
		String value = tf.getText();

			if (value.equals("")) {
				return;
			}

		int i = 0;
		boolean needsRefresh = false;

			if (name.equals(fieldNames[0])) {

				i = getValue(value).intValue();
				if (i < 1) {
					return;
				} else {
					needsRefresh = true;
					numLabels = i;
				}
			} else if (name.equals(fieldNames[1])) {
				i = getValue(value).intValue();
				if (i < 0) {
					return;
				} else {
					needsRefresh = true;
					decimalPlaces = i;
					decimalPlacesChanged = true;
				}

			} else if (name.equals(fieldNames[2])) {
				i = getValue(value).intValue();
				if (i < 1) {
					return;
				} else {
					needsRefresh = true;
					fontSize = i;

				}

			} else if (name.equals(fieldNames[3])) {
			double d = 0;
				d = getValue("0" + value).doubleValue();
				if (d <= 0) {
					return;
				} else {
					needsRefresh = true;
					zoom = d;
				}
			}

			if (needsRefresh) {
				updateColorBar();
			}
			return;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  e  Description of the Parameter
		 */
		public void itemStateChanged(ItemEvent e) {
			location = ((Choice) (choice.elementAt(0))).getSelectedItem();
			fillColor = ((Choice) (choice.elementAt(1))).getSelectedItem();
			textColor = ((Choice) (choice.elementAt(2))).getSelectedItem();
			boldText = ((Checkbox) (checkbox.elementAt(0))).getState();
			updateColorBar();
		}

	}//LiveDialog inner class

}

