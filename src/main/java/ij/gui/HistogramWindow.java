//EU_HOU
package ij.gui;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.filter.Analyzer;
import ij.text.TextWindow;

/**
 *  This class is an extended ImageWindow that displays histograms.
 *
 *@author     Thomas
 *@created    20 novembre 2007
 */
public class HistogramWindow extends ImageWindow implements Measurements, ActionListener, ClipboardOwner {
	final static int WIN_WIDTH = 300;
	final static int WIN_HEIGHT = 240;
	final static int HIST_WIDTH = 256;
	final static int HIST_HEIGHT = 128;
	final static int BAR_HEIGHT = 12;
	final static int XMARGIN = 20;
	final static int YMARGIN = 10;

	/**
	 *  Description of the Field
	 */
	protected ImageStatistics stats;
	/**
	 *  Description of the Field
	 */
	protected int[] histogram;
	/**
	 *  Description of the Field
	 */
	protected LookUpTable lut;
	/**
	 *  Description of the Field
	 */
	protected Rectangle frame = null;
	/**
	 *  Description of the Field
	 */
	protected Button list, save, copy, log;
	/**
	 *  Description of the Field
	 */
	protected Label value, count;
	/**
	 *  Description of the Field
	 */
	protected static String defaultDirectory = null;
	/**
	 *  Description of the Field
	 */
	protected int decimalPlaces;
	/**
	 *  Description of the Field
	 */
	protected int digits;
	/**
	 *  Description of the Field
	 */
	protected int newMaxCount;
	/**
	 *  Description of the Field
	 */
	protected int plotScale = 1;
	/**
	 *  Description of the Field
	 */
	protected boolean logScale;
	/**
	 *  Description of the Field
	 */
	protected Calibration cal;
	/**
	 *  Description of the Field
	 */
	protected int yMax;
	/**
	 *  Description of the Field
	 */
	public static int nBins = 256;


	/**
	 *  Displays a histogram using the title "Histogram of ImageName".
	 *
	 *@param  imp  Description of the Parameter
	 */
	public HistogramWindow(ImagePlus imp) {
		//EU_HOU Bundle
		super(NewImage.createByteImage(IJ.getBundle().getString("HistoTitle") + " " + imp.getShortTitle(), WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, 256, 1.0, 0.0);
	}


	/**
	 *  Displays a histogram using the specified title and number of bins.
	 *  Currently, the number of bins must be 256 expect for 32 bit images.
	 *
	 *@param  title  Description of the Parameter
	 *@param  imp    Description of the Parameter
	 *@param  bins   Description of the Parameter
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins) {
		super(NewImage.createByteImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, bins, 0.0, 0.0);
	}


	/**
	 *  Displays a histogram using the specified title, number of bins and
	 *  histogram range. Currently, the number of bins must be 256 and the
	 *  histogram range range must be the same as the image range expect for 32 bit
	 *  images.
	 *
	 *@param  title    Description of the Parameter
	 *@param  imp      Description of the Parameter
	 *@param  bins     Description of the Parameter
	 *@param  histMin  Description of the Parameter
	 *@param  histMax  Description of the Parameter
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins, double histMin, double histMax) {
		super(NewImage.createByteImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		showHistogram(imp, bins, histMin, histMax);
	}


	/**
	 *  Displays a histogram using the specified title, number of bins, histogram
	 *  range and yMax.
	 *
	 *@param  title    Description of the Parameter
	 *@param  imp      Description of the Parameter
	 *@param  bins     Description of the Parameter
	 *@param  histMin  Description of the Parameter
	 *@param  histMax  Description of the Parameter
	 *@param  yMax     Description of the Parameter
	 */
	public HistogramWindow(String title, ImagePlus imp, int bins, double histMin, double histMax, int yMax) {
		super(NewImage.createByteImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		this.yMax = yMax;
		showHistogram(imp, bins, histMin, histMax);
	}


	/**
	 *  Displays a histogram using the specified title and ImageStatistics.
	 *
	 *@param  title  Description of the Parameter
	 *@param  imp    Description of the Parameter
	 *@param  stats  Description of the Parameter
	 */
	public HistogramWindow(String title, ImagePlus imp, ImageStatistics stats) {
		super(NewImage.createByteImage(title, WIN_WIDTH, WIN_HEIGHT, 1, NewImage.FILL_WHITE));
		//IJ.log("HistogramWindow: "+stats.histMin+"  "+stats.histMax+"  "+stats.nBins);
		this.yMax = stats.histYMax;
		showHistogram(imp, stats);
	}


	/**
	 *  Draws the histogram using the specified title and number of bins.
	 *  Currently, the number of bins must be 256 expect for 32 bit images.
	 *
	 *@param  imp   Description of the Parameter
	 *@param  bins  Description of the Parameter
	 */
	public void showHistogram(ImagePlus imp, int bins) {
		showHistogram(imp, bins, 0.0, 0.0);
	}


	/**
	 *  Draws the histogram using the specified title, number of bins and histogram
	 *  range. Currently, the number of bins must be 256 and the histogram range
	 *  range must be the same as the image range expect for 32 bit images.
	 *
	 *@param  imp      Description of the Parameter
	 *@param  bins     Description of the Parameter
	 *@param  histMin  Description of the Parameter
	 *@param  histMax  Description of the Parameter
	 */
	public void showHistogram(ImagePlus imp, int bins, double histMin, double histMax) {
	boolean limitToThreshold = (Analyzer.getMeasurements() & LIMIT) != 0;

		stats = imp.getStatistics(AREA + MEAN + MODE + MIN_MAX + (limitToThreshold ? LIMIT : 0), bins, histMin, histMax);
		showHistogram(imp, stats);
	}


	/**
	 *  Draws the histogram using the specified title and ImageStatistics.
	 *
	 *@param  imp    Description of the Parameter
	 *@param  stats  Description of the Parameter
	 */
	public void showHistogram(ImagePlus imp, ImageStatistics stats) {
		setup();
		this.stats = stats;
		cal = imp.getCalibration();

	boolean limitToThreshold = (Analyzer.getMeasurements() & LIMIT) != 0;

		imp.getMask();
		histogram = stats.histogram;
		if (limitToThreshold && histogram.length == 256) {
		ImageProcessor ip = imp.getProcessor();

			if (ip.getMinThreshold() != ImageProcessor.NO_THRESHOLD) {
			int lower = scaleDown(ip, ip.getMinThreshold());
			int upper = scaleDown(ip, ip.getMaxThreshold());

				for (int i = 0; i < lower; i++) {
					histogram[i] = 0;
				}
				for (int i = upper + 1; i < 256; i++) {
					histogram[i] = 0;
				}
			}
		}
		lut = imp.createLut();

	int type = imp.getType();
	boolean fixedRange = type == ImagePlus.GRAY8 || type == ImagePlus.COLOR_256 || type == ImagePlus.COLOR_RGB;
	ImageProcessor ip = this.imp.getProcessor();
	boolean color = !(imp.getProcessor() instanceof ColorProcessor) && !lut.isGrayscale();

		if (color) {
			ip = ip.convertToRGB();
		}
		drawHistogram(ip, fixedRange);
		if (color) {
			this.imp.setProcessor(null, ip);
		} else {
			this.imp.updateAndDraw();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void setup() {
	Panel buttons = new Panel();

		buttons.setLayout(new FlowLayout(FlowLayout.RIGHT));
		//EU_HOU Bundle
		list = new Button(IJ.getBundle().getString("List"));
		list.addActionListener(this);
		buttons.add(list);
		//EU_HOU Bundle
		copy = new Button(IJ.getBundle().getString("Copy"));
		copy.addActionListener(this);
		buttons.add(copy);
		//EU_HOU Bundle
		log = new Button(IJ.getBundle().getString("HistoLog"));
		log.addActionListener(this);
		buttons.add(log);

	Panel valueAndCount = new Panel();

		valueAndCount.setLayout(new GridLayout(2, 1));
		value = new Label("                  ");//21
		value.setFont(new Font("Monospaced", Font.PLAIN, 12));
		valueAndCount.add(value);
		count = new Label("                  ");
		count.setFont(new Font("Monospaced", Font.PLAIN, 12));
		valueAndCount.add(count);
		buttons.add(valueAndCount);
		add(buttons);
		pack();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 */
	public void mouseMoved(int x, int y) {
		if (value == null || count == null) {
			return;
		}
		if ((frame != null) && x >= frame.x && x <= (frame.x + frame.width)) {
			x = x - frame.x;
			if (x > 255) {
				x = 255;
			}
		int index = (int) (x * ((double) histogram.length) / HIST_WIDTH);
			//EU_HOU Bundle
			value.setText(" " + IJ.getBundle().getString("Value") + ":" + IJ.d2s(cal.getCValue(stats.histMin + index * stats.binSize), digits));
			count.setText(" " + IJ.getBundle().getString("Count") + ":" + histogram[index]);
		} else {
			value.setText("");
			count.setText("");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip          Description of the Parameter
	 *@param  fixedRange  Description of the Parameter
	 */
	protected void drawHistogram(ImageProcessor ip, boolean fixedRange) {
	int x;
	int y;
	int maxCount2 = 0;
	int mode2 = 0;
	int saveModalCount;

		ip.setColor(Color.black);
		ip.setLineWidth(1);
		decimalPlaces = Analyzer.getPrecision();
		digits = cal.calibrated() || stats.binSize != 1.0 ? decimalPlaces : 0;
		saveModalCount = histogram[stats.mode];
		for (int i = 0; i < histogram.length; i++) {
			if ((histogram[i] > maxCount2) && (i != stats.mode)) {
				maxCount2 = histogram[i];
				mode2 = i;
			}
		}
		newMaxCount = stats.maxCount;
		if ((newMaxCount > (maxCount2 * 2)) && (maxCount2 != 0)) {
			newMaxCount = (int) (maxCount2 * 1.5);
			//histogram[stats.mode] = newMaxCount;
		}
		if (IJ.shiftKeyDown()) {
			logScale = true;
			drawLogPlot(yMax > 0 ? yMax : newMaxCount, ip);
		}
		drawPlot(yMax > 0 ? yMax : newMaxCount, ip);
		histogram[stats.mode] = saveModalCount;
		x = XMARGIN + 1;
		y = YMARGIN + HIST_HEIGHT + 2;
		lut.drawUnscaledColorBar(ip, x - 1, y, 256, BAR_HEIGHT);
		y += BAR_HEIGHT + 15;
		drawText(ip, x, y, fixedRange);
	}


	/**
	 *  Scales a threshold level to the range 0-255.
	 *
	 *@param  ip         Description of the Parameter
	 *@param  threshold  Description of the Parameter
	 *@return            Description of the Return Value
	 */
	int scaleDown(ImageProcessor ip, double threshold) {
	double min = ip.getMin();
	double max = ip.getMax();

		if (max > min) {
			return (int) (((threshold - min) / (max - min)) * 255.0);
		} else {
			return 0;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  maxCount  Description of the Parameter
	 *@param  ip        Description of the Parameter
	 */
	void drawPlot(int maxCount, ImageProcessor ip) {
		if (maxCount == 0) {
			maxCount = 1;
		}
		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.drawRect(frame.x - 1, frame.y, frame.width + 2, frame.height + 1);

	int index;
	int y;

		for (int i = 0; i < HIST_WIDTH; i++) {
			index = (int) (i * (double) histogram.length / HIST_WIDTH);
			y = (int) ((double) HIST_HEIGHT * histogram[index]) / maxCount;
			if (y > HIST_HEIGHT) {
				y = HIST_HEIGHT;
			}
			ip.drawLine(i + XMARGIN, YMARGIN + HIST_HEIGHT, i + XMARGIN, YMARGIN + HIST_HEIGHT - y);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  maxCount  Description of the Parameter
	 *@param  ip        Description of the Parameter
	 */
	void drawLogPlot(int maxCount, ImageProcessor ip) {
		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.drawRect(frame.x - 1, frame.y, frame.width + 2, frame.height + 1);

	double max = Math.log(maxCount);

		ip.setColor(Color.gray);

	int index;
	int y;

		for (int i = 0; i < HIST_WIDTH; i++) {
			index = (int) (i * (double) histogram.length / HIST_WIDTH);
			y = histogram[index] == 0 ? 0 : (int) (HIST_HEIGHT * Math.log(histogram[index]) / max);
			if (y > HIST_HEIGHT) {
				y = HIST_HEIGHT;
			}
			ip.drawLine(i + XMARGIN, YMARGIN + HIST_HEIGHT, i + XMARGIN, YMARGIN + HIST_HEIGHT - y);
		}
		ip.setColor(Color.black);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip          Description of the Parameter
	 *@param  x           Description of the Parameter
	 *@param  y           Description of the Parameter
	 *@param  fixedRange  Description of the Parameter
	 */
	void drawText(ImageProcessor ip, int x, int y, boolean fixedRange) {
		ip.setFont(new Font("SansSerif", Font.PLAIN, 12));
		ip.setAntialiasedText(true);

	double hmin = cal.getCValue(stats.histMin);
	double hmax = cal.getCValue(stats.histMax);

		if (fixedRange && !cal.calibrated() && hmin == 0 && hmax == 255) {
			hmin = 0;
			hmax = 256;
		}
		ip.drawString(d2s(hmin), x - 4, y);
		ip.drawString(d2s(hmax), x + HIST_WIDTH - getWidth(hmax, ip) + 10, y);

	double binWidth = (hmax - hmin) / stats.nBins;

		binWidth = Math.abs(binWidth);

	boolean showBins = binWidth != 1.0 || !fixedRange;
	int col1 = XMARGIN + 5;
	int col2 = XMARGIN + HIST_WIDTH / 2;
	int row1 = y + 25;

		if (showBins) {
			row1 -= 8;
		}
	int row2 = row1 + 15;
	int row3 = row2 + 15;
	int row4 = row3 + 15;
		//EU_HOU Bundle
		ip.drawString(IJ.getBundle().getString("HistoCount") + ": " + stats.pixelCount, col1, row1);
		ip.drawString(IJ.getBundle().getString("Mean") + ": " + d2s(stats.mean), col1, row2);
		ip.drawString(IJ.getBundle().getString("StdDev") + ": " + d2s(stats.stdDev), col1, row3);
		ip.drawString(IJ.getBundle().getString("Mode") + ": " + d2s(stats.dmode) + " (" + stats.maxCount + ")", col2, row3);
		ip.drawString(IJ.getBundle().getString("Min") + ": " + d2s(stats.min), col2, row1);
		ip.drawString(IJ.getBundle().getString("Max") + ": " + d2s(stats.max), col2, row2);

		if (showBins) {
			//EU_HOU Bundle
			ip.drawString(IJ.getBundle().getString("Bins") + ": " + d2s(stats.nBins), col1, row4);
			ip.drawString(IJ.getBundle().getString("BinWidth") + ": " + d2s(binWidth), col2, row4);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  d  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	String d2s(double d) {
		if (d == Double.MAX_VALUE || d == -Double.MAX_VALUE) {
			return "0";
		} else if (Double.isNaN(d)) {
			return ("NaN");
		} else if (Double.isInfinite(d)) {
			return ("Infinity");
		} else if ((int) d == d) {
			return IJ.d2s(d, 0);
		} else {
			return IJ.d2s(d, decimalPlaces);
		}
	}


	/**
	 *  Gets the width attribute of the HistogramWindow object
	 *
	 *@param  d   Description of the Parameter
	 *@param  ip  Description of the Parameter
	 *@return     The width value
	 */
	int getWidth(double d, ImageProcessor ip) {
		return ip.getStringWidth(d2s(d));
	}


	/**
	 *  Description of the Method
	 */
	protected void showList() {
	StringBuffer sb = new StringBuffer();
	String vheading = stats.binSize == 1.0 ? "value" : "bin start";

		if (cal.calibrated() && !cal.isSigned16Bit()) {
			for (int i = 0; i < stats.nBins; i++) {
				sb.append(i + "\t" + IJ.d2s(cal.getCValue(stats.histMin + i * stats.binSize), digits) + "\t" + histogram[i] + "\n");
			}
		//EU_HOU Bundle
		TextWindow tw = new TextWindow(getTitle(), IJ.getBundle().getString("HistoHead") + vheading, sb.toString(), 200, 400);
		} else {
			for (int i = 0; i < stats.nBins; i++) {
				sb.append(IJ.d2s(cal.getCValue(stats.histMin + i * stats.binSize), digits) + "\t" + histogram[i] + "\n");
			}
		//EU_HOU Bundle
		TextWindow tw = new TextWindow(getTitle(), vheading + IJ.getBundle().getString("HistoHead"), sb.toString(), 200, 400);
		}
	}


	/**
	 *  Description of the Method
	 */
	protected void copyToClipboard() {
	Clipboard systemClipboard = null;

		try {
			systemClipboard = getToolkit().getSystemClipboard();
		} catch (Exception e) {
			systemClipboard = null;
		}
		if (systemClipboard == null) {
			//EU_HOU Bundle
			IJ.error(IJ.getBundle().getString("ClipCopyErr"));
			return;
		}
		//EU_HOU Bundle
		IJ.showStatus(IJ.getBundle().getString("HistoCopyVal"));

	CharArrayWriter aw = new CharArrayWriter(stats.nBins * 4);
	PrintWriter pw = new PrintWriter(aw);

		for (int i = 0; i < stats.nBins; i++) {
			pw.print(IJ.d2s(cal.getCValue(stats.histMin + i * stats.binSize), digits) + "\t" + histogram[i] + "\n");
		}

	String text = aw.toString();

		pw.close();

	StringSelection contents = new StringSelection(text);

		systemClipboard.setContents(contents, this);
		//EU_HOU Bundle
		IJ.showStatus(text.length() + " " + IJ.getBundle().getString("CharCopied"));
	}


	/**
	 *  Description of the Method
	 */
	void replot() {
		logScale = !logScale;

	ImageProcessor ip = this.imp.getProcessor();

		frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
		ip.setColor(Color.white);
		ip.setRoi(frame.x - 1, frame.y, frame.width + 2, frame.height);
		ip.fill();
		ip.resetRoi();
		ip.setColor(Color.black);
		if (logScale) {
			drawLogPlot(yMax > 0 ? yMax : newMaxCount, ip);
			drawPlot(yMax > 0 ? yMax : newMaxCount, ip);
		} else {
			drawPlot(yMax > 0 ? yMax : newMaxCount, ip);
		}
		this.imp.updateAndDraw();
	}


	/*
	 *  void rescale() {
	 *  Graphics g = img.getGraphics();
	 *  plotScale *= 2;
	 *  if ((newMaxCount/plotScale)<50) {
	 *  plotScale = 1;
	 *  frame = new Rectangle(XMARGIN, YMARGIN, HIST_WIDTH, HIST_HEIGHT);
	 *  g.setColor(Color.white);
	 *  g.fillRect(frame.x, frame.y, frame.width, frame.height);
	 *  g.setColor(Color.black);
	 *  }
	 *  drawPlot(newMaxCount/plotScale, g);
	 *  /ImageProcessor ip = new ColorProcessor(img);
	 *  /this.imp.setProcessor(null, ip);
	 *  this.imp.setImage(img);
	 *  }
	 */
	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) {
	Object b = e.getSource();

		if (b == list) {
			showList();
		} else if (b == copy) {
			copyToClipboard();
		} else if (b == log) {
			replot();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  clipboard  Description of the Parameter
	 *@param  contents   Description of the Parameter
	 */
	public void lostOwnership(Clipboard clipboard, Transferable contents) { }


	/**
	 *  Gets the histogram attribute of the HistogramWindow object
	 *
	 *@return    The histogram value
	 */
	public int[] getHistogram() {
		return histogram;
	}

}

