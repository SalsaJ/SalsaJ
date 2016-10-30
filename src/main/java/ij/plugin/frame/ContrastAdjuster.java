//EU_HOU
package ij.plugin.frame;
//import ij.plugin.frame.*;
import java.awt.*;
import java.awt.event.*;
import ij.*;
import ij.plugin.filter.LutViewer.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
/*
    EU_HOU CHANGES
  */
import java.util.*;

/*
    EU_HOU END
  */
/**
 *  This plugin implements the Brightness/Contrast, Window/level and Color
 *  Balance commands, all in the Image/Adjust sub-menu. It allows the user to
 *  interactively adjust the brightness and contrast of the active image. It is
 *  multi-threaded to provide a more responsive user interface.
 *
 *@author     Thomas
 *@created    19 novembre 2007
 */
public class ContrastAdjuster extends PlugInFrame implements Runnable,
		ActionListener, AdjustmentListener, ItemListener {

	/**
	 *  Description of the Field
	 */
	public final static String LOC_KEY = "b&c.loc";
	final static int AUTO_THRESHOLD = 5000;
	final static String[] channelLabelsKeys = {"RedColor", "GreenColor", "BlueColor", "CyanColor", "MagentaColor", "YellowColor", "ColorRGB"};
	static String[] channelLabels;
	//final static String[] channelLabels = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "RGB"};
	final static int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};

	ContrastPlot plot = new ContrastPlot();
	Thread thread;
	private static Frame instance;

	int minSliderValue = -1, maxSliderValue = -1, brightnessValue = -1, contrastValue = -1;
	int sliderRange = 256;
	boolean doAutoAdjust, doReset, doSet, doApplyLut;

	Panel panel, tPanel, lutPanel;
	Button autoB, resetB, setB, applyB;
	int previousImageID;
	int previousType;
	int previousSlice = 1;
	Object previousSnapshot;
	ImageJ ij;
	double min, max;
	double previousMin, previousMax;
	double defaultMin, defaultMax;
	int contrast, brightness;
	boolean RGBImage;
	Scrollbar minSlider, maxSlider, contrastSlider, brightnessSlider;
	Label minLabel, maxLabel, windowLabel, levelLabel;
	boolean done;
	int autoThreshold;
	GridBagLayout gridbag;
	GridBagConstraints c;
	int y = 0;
	boolean windowLevel, balance;
	Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
	Font sanFont = new Font("SansSerif", Font.PLAIN, 12);
	int channels = 7;// RGB
	Choice choice;
	/*
	    EU_HOU CHANGES
	  */
	static ResourceBundle bun = IJ.getPluginBundle();


	/**
	 *  Description of the Field
	 */
	//LookUpTable LUTview;


	/*
	    EU_HOU END
	  */
	//ColorTable colorpanel = new ColorTable();

	/**
	 *  Constructor for the ContrastAdjuster object
	 */
	public ContrastAdjuster() {
		//EU_HOU Bundle
		super(bun.getString("ContrastTitle"));
		if (channelLabels == null) {
			channelLabels = new String[channelLabelsKeys.length];
			for (int i = 0; i < channelLabels.length; ++i) {
				channelLabels[i] = IJ.getColorBundle().getString(channelLabelsKeys[i]);
			}
		}
	}


	/**
	 *  Main processing method for the ContrastAdjuster object
	 *
	 *@param  arg  Description of the Parameter
	 */
        @Override
	public void run(String arg) {
		windowLevel = arg.equals("wl");
		balance = arg.equals("balance");
		if (windowLevel) {
			//EU_HOU Bundle
			setTitle(bun.getString("WLTitle"));
		}
		else if (balance) {
			//EU_HOU Bundle
			setTitle(bun.getString("ColorTitle"));
			channels = 4;
		}

		if (instance != null) {
			instance.toFront();
			return;
		}
		instance = this;
		IJ.register(ContrastAdjuster.class);
		WindowManager.addWindow(this);

		ij = IJ.getInstance();
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		setLayout(gridbag);

		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;

		/*
		    EU_HOU CHANGES
		  */
		//color panel
		y = 0;
		c.gridx = 0;
		c.gridy = y++;
		//c.insets = new Insets(10, 0, 0, 10);
		//gridbag.setConstraints(colorpanel, c);
		//add(colorpanel);
		/*
		    EU_HOU CHANGES
		  */
		// plot
		c.gridy = y++;

		c.insets = new Insets(10, 10, 0, 10);
		gridbag.setConstraints(plot, c);
		add(plot);
		//EU_HOU

		//
		plot.addKeyListener(ij);
		// min and max labels

		if (!windowLevel) {
			panel = new Panel();
			c.gridy = y++;
			c.insets = new Insets(0, 10, 0, 10);
			gridbag.setConstraints(panel, c);
			panel.setLayout(new BorderLayout());
			minLabel = new Label("      ", Label.LEFT);
			minLabel.setFont(monoFont);
			panel.add("West", minLabel);
			maxLabel = new Label("      ", Label.RIGHT);
			maxLabel.setFont(monoFont);
			panel.add("East", maxLabel);
			add(panel);
		}

                // color channel popup menu
		if (balance) {
			c.gridy = y++;
			c.insets = new Insets(5, 10, 0, 10);
			choice = new Choice();
			for (int i = 0; i < channelLabels.length; i++) {
				choice.addItem(channelLabels[i]);
			}
			gridbag.setConstraints(choice, c);
			choice.addItemListener(this);
			choice.addKeyListener(ij);
                        choice.setBackground(Color.lightGray);
			add(choice);
		}

		// min slider
		if (!windowLevel) {
			minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(minSlider, c);
			add(minSlider);
			minSlider.addAdjustmentListener(this);
			minSlider.addKeyListener(ij);
			minSlider.setUnitIncrement(1);
			/*
			    EU_HOU CHANGES
			  */
			minSlider.setBackground(Color.red);
			/*
			    EU_HOU END
			  */
			minSlider.setFocusable(false);// prevents blinking on Windows
			//EU_HOU Bundle
			addLabel(bun.getString("Minimum"), null);
		}

		// max slider
		if (!windowLevel) {
			maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(maxSlider, c);
			add(maxSlider);
			maxSlider.addAdjustmentListener(this);
			maxSlider.addKeyListener(ij);
			maxSlider.setUnitIncrement(1);
			/*
			    EU_HOU CHANGES
			  */
			maxSlider.setBackground(Color.blue);
			/*
			    EU_HOU END
			  */
			maxSlider.setFocusable(false);
			//EU_HOU Bundle
			addLabel(bun.getString("Maximum"), null);
		}

		// brightness slider
		brightnessSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange);
		c.gridy = y++;
		c.insets = new Insets(windowLevel ? 12 : 2, 10, 0, 10);
		gridbag.setConstraints(brightnessSlider, c);
		add(brightnessSlider);
		brightnessSlider.addAdjustmentListener(this);
		brightnessSlider.addKeyListener(ij);
		/*
		    EU_HOU CHANGES
		  */
		brightnessSlider.setBackground(Color.yellow);
		/*
		    EU_HOU END
		  */
		brightnessSlider.setUnitIncrement(1);
		brightnessSlider.setFocusable(false);
		if (windowLevel) {
			//EU_HOU Bundle
			addLabel(bun.getString("Level"), levelLabel = new TrimmedLabel("        "));
		}
		else {
			//EU_HOU Bundle
			addLabel(bun.getString("Brightness"), null);
		}

		// contrast slider
		if (!balance) {
			contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange / 2, 1, 0, sliderRange);
			c.gridy = y++;
			c.insets = new Insets(2, 10, 0, 10);
			gridbag.setConstraints(contrastSlider, c);
			add(contrastSlider);
			contrastSlider.addAdjustmentListener(this);
			contrastSlider.addKeyListener(ij);
			/*
			    EU_HOU CHANGES
			  */
			contrastSlider.setBackground(Color.pink);
			/*
			    EU_HOU END
			  */
			contrastSlider.setUnitIncrement(1);
			contrastSlider.setFocusable(false);
			if (windowLevel) {
				//EU_HOU Bundle
				addLabel(bun.getString("Window"), windowLabel = new TrimmedLabel("        "));
			}
			else {
				//EU_HOU Bundle
				addLabel(bun.getString("Contrast"), null);
			}
		}

		

		// buttons
		int trim = IJ.isMacOSX() ? 20 : 0;

		panel = new Panel();
		panel.setLayout(new GridLayout(0, 2, 0, 0));
		//EU_HOU Bundle
		autoB = new TrimmedButton(bun.getString("Auto"), trim);
		autoB.addActionListener(this);
		autoB.addKeyListener(ij);
		panel.add(autoB);
		//EU_HOU Bundle
		resetB = new TrimmedButton(bun.getString("Reset"), trim);
		resetB.addActionListener(this);
		resetB.addKeyListener(ij);
		panel.add(resetB);
		//EU_HOU Bundle
		setB = new TrimmedButton(bun.getString("Set"), trim);
		setB.addActionListener(this);
		setB.addKeyListener(ij);
		panel.add(setB);
		//EU_HOU Bundle
		//applyB = new TrimmedButton(bun.getString("MyApply2"), trim);
                // TB 250613 apply button is not pedagogical --> supress
//                applyB = new TrimmedButton("Apply", trim);
//		applyB.addActionListener(this);
//		applyB.addKeyListener(ij);
//		panel.add(applyB);
		
                c.gridy = y++;
		c.insets = new Insets(8, 5, 10, 5);
		gridbag.setConstraints(panel, c);
		add(panel);

		addKeyListener(ij);// ImageJ handles keyboard shortcuts
		pack();

		//LutViewer

		Point loc = Prefs.getLocation(LOC_KEY);

		if (loc != null) {
			setLocation(loc);
		}
		else {
			GUI.center(this);
		}
		if (IJ.isMacOSX()) {
			setResizable(false);
		}
		show();

		thread = new Thread(this, "ContrastAdjuster");
		//thread.setPriority(thread.getPriority()-1);
		thread.start();
		setup();
	}


	/**
	 *  Adds a feature to the Label attribute of the ContrastAdjuster object
	 *
	 *@param  text    The feature to be added to the Label attribute
	 *@param  label2  The feature to be added to the Label attribute
	 */
	void addLabel(String text, Label label2) {
		if (label2 == null && IJ.isMacOSX()) {
			text += "    ";
		}
		panel = new Panel();
		c.gridy = y++;

		int bottomInset = IJ.isMacOSX() && IJ.isJava14() ? 4 : 0;

		c.insets = new Insets(0, 10, bottomInset, 0);
		gridbag.setConstraints(panel, c);
		panel.setLayout(new FlowLayout(label2 == null ? FlowLayout.CENTER : FlowLayout.LEFT, 0, 0));

		Label label = new TrimmedLabel(text);

		label.setFont(sanFont);
		panel.add(label);
		if (label2 != null) {
			label2.setFont(monoFont);
			label2.setAlignment(Label.LEFT);
			panel.add(label2);
		}
		add(panel);
	}


	/**
	 *  Description of the Method
	 */
	void setup() {
		ImagePlus imp = WindowManager.getCurrentImage();
		//LookUpTable lut = imp.createLut();
		//Calibration cal = imp.getCalibration();
		//LutViewer lutview = new LutViewer();
		if (imp != null) {
			//IJ.write("setup");
			ImageProcessor ip = imp.getProcessor();

			setup(imp);
			//LUTview = lutview.prepare(ip, lut, cal);
			//ImageWindow p = new ImageWindow(LUTview);
			//LUTview = imp.createLut();
			updatePlot();
			updateLabels(imp, ip);
			imp.updateAndDraw();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		Object source = e.getSource();

		if (source == minSlider) {
			minSliderValue = minSlider.getValue();
		}
		else if (source == maxSlider) {
			maxSliderValue = maxSlider.getValue();
		}
		else if (source == contrastSlider) {
			contrastValue = contrastSlider.getValue();
		}
		else {
			brightnessValue = brightnessSlider.getValue();
		}
		notify();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public synchronized void actionPerformed(ActionEvent e) {
		Button b = (Button) e.getSource();

		if (b == null) {
			return;
		}
		if (b == resetB) {
			doReset = true;
		}
		else if (b == autoB) {
			doAutoAdjust = true;
		}
		else if (b == setB) {
			doSet = true;
		}
		else if (b == applyB) {
			doApplyLut = true;
		}
		notify();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	ImageProcessor setup(ImagePlus imp) {
		Roi roi = imp.getRoi();

		if (roi != null) {
			roi.endPaste();
		}
		ImageProcessor ip = imp.getProcessor();
		int type = imp.getType();
		int slice = imp.getCurrentSlice();

		RGBImage = type == ImagePlus.COLOR_RGB;

		boolean snapshotChanged = RGBImage && previousSnapshot != null && ((ColorProcessor) ip).getSnapshotPixels() != previousSnapshot;

		if (imp.getID() != previousImageID || snapshotChanged || type != previousType || slice != previousSlice) {
			setupNewImage(imp, ip);
		}
		previousImageID = imp.getID();
		previousType = type;
		previousSlice = slice;
		return ip;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void setupNewImage(ImagePlus imp, ImageProcessor ip) {
		//IJ.write("setupNewImage");
		previousMin = min;
		previousMax = max;
		if (RGBImage) {
			ip.snapshot();
			previousSnapshot = ((ColorProcessor) ip).getSnapshotPixels();
		}
		else {
			previousSnapshot = null;
		}

		double min2 = ip.getMin();
		double max2 = ip.getMax();

		if (imp.getType() == ImagePlus.COLOR_RGB) {
			min2 = 0.0;
			max2 = 255.0;
		}
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			ip.resetMinAndMax();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();
		}
		else {
			defaultMin = 0;
			defaultMax = 255;
		}
		setMinAndMax(ip, min2, max2);
		min = ip.getMin();
		max = ip.getMax();
		if (IJ.debugMode) {
			//EU_HOU Bundle
			IJ.log("min: " + min);
			//EU_HOU Bundle
			IJ.log("max: " + max);
			//EU_HOU Bundle
			IJ.log("defaultMin: " + defaultMin);
			//EU_HOU Bundle
			IJ.log("defaultMax: " + defaultMax);
		}
		plot.defaultMin = defaultMin;
		plot.defaultMax = defaultMax;
		//plot.histogram = null;
		int valueRange = (int) (defaultMax - defaultMin);
		int newSliderRange = valueRange;

		if (newSliderRange > 640 && newSliderRange < 1280) {
			newSliderRange /= 2;
		}
		else if (newSliderRange >= 1280) {
			newSliderRange /= 5;
		}
		if (newSliderRange < 256) {
			newSliderRange = 256;
		}
		if (newSliderRange > 1024) {
			newSliderRange = 1024;
		}
		double displayRange = max - min;

		if (valueRange >= 1280 && valueRange != 0 && displayRange / valueRange < 0.25) {
			newSliderRange *= 1.6666;
		}
		//IJ.log(valueRange+" "+displayRange+" "+newSliderRange);
		if (newSliderRange != sliderRange) {
			sliderRange = newSliderRange;
			updateScrollBars(null, true);
		}
		else {
			updateScrollBars(null, false);
		}
		if (!doReset) {
			plotHistogram(imp);
		}
		autoThreshold = 0;
	}


	/**
	 *  Sets the minAndMax attribute of the ContrastAdjuster object
	 *
	 *@param  ip   The new minAndMax value
	 *@param  min  The new minAndMax value
	 *@param  max  The new minAndMax value
	 */
	void setMinAndMax(ImageProcessor ip, double min, double max) {
		if (channels != 7 && ip instanceof ColorProcessor) {
			((ColorProcessor) ip).setMinAndMax(min, max, channels);
		}
		else {
			ip.setMinAndMax(min, max);
		}
	}


	/**
	 *  Description of the Method
	 */
	void updatePlot() {
		plot.min = min;
		plot.max = max;
		plot.repaint();
		/*
		    EU_HOU CHANGES
		  */
		/*
		    colorpanel.newmin = (long) min;
		    colorpanel.newmax = (long) max;
		    colorpanel.orgmin = (long) defaultMin;
		    colorpanel.orgmax = (long) defaultMax;
		    switch (WindowManager.getCurrentImage().getType()) {
		    case ImagePlus.GRAY16:
		    colorpanel.factor = 256;
		    break;
		    case ImagePlus.GRAY32:
		    colorpanel.factor = 65536;
		    break;
		    default:
		    colorpanel.factor = 1;
		    }
		    colorpanel.repaint();
		  */
		/*
		    EU_HOU END
		  */
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void updateLabels(ImagePlus imp, ImageProcessor ip) {
		double min = ip.getMin();
		double max = ip.getMax();
		int type = imp.getType();
		Calibration cal = imp.getCalibration();
		boolean realValue = type == ImagePlus.GRAY32;

		if (cal.calibrated()) {
			min = cal.getCValue((int) min);
			max = cal.getCValue((int) max);
			if (type != ImagePlus.GRAY16) {
				realValue = true;
			}
		}
		int digits = realValue ? 2 : 0;

		if (windowLevel) {
			//IJ.log(min+" "+max);
			double window = max - min;
			double level = min + (window) / 2.0;

			windowLabel.setText(IJ.d2s(window, digits));
			levelLabel.setText(IJ.d2s(level, digits));
		}
		else {
			minLabel.setText(IJ.d2s(min, digits));
			maxLabel.setText(IJ.d2s(max, digits));
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  sb        Description of the Parameter
	 *@param  newRange  Description of the Parameter
	 */
	void updateScrollBars(Scrollbar sb, boolean newRange) {
		if (sb == null || sb != contrastSlider) {
			double mid = sliderRange / 2;
			double c = ((defaultMax - defaultMin) / (max - min)) * mid;

			if (c > mid) {
				c = sliderRange - ((max - min) / (defaultMax - defaultMin)) * mid;
			}
			contrast = (int) c;
			if (contrastSlider != null) {
				if (newRange) {
					contrastSlider.setValues(contrast, 1, 0, sliderRange);
				}
				else {
					contrastSlider.setValue(contrast);
				}
			}
		}
		if (sb == null || sb != brightnessSlider) {
			double level = min + (max - min) / 2.0;
			double normalizedLevel = 1.0 - (level - defaultMin) / (defaultMax - defaultMin);

			brightness = (int) (normalizedLevel * sliderRange);
			if (newRange) {
				brightnessSlider.setValues(brightness, 1, 0, sliderRange);
			}
			else {
				brightnessSlider.setValue(brightness);
			}
		}
		if (minSlider != null && (sb == null || sb != minSlider)) {
			if (newRange) {
				minSlider.setValues(scaleDown(min), 1, 0, sliderRange);
			}
			else {
				minSlider.setValue(scaleDown(min));
			}
		}
		if (maxSlider != null && (sb == null || sb != maxSlider)) {
			if (newRange) {
				maxSlider.setValues(scaleDown(max), 1, 0, sliderRange);
			}
			else {
				maxSlider.setValue(scaleDown(max));
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  v  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	int scaleDown(double v) {
		if (v < defaultMin) {
			v = defaultMin;
		}
		if (v > defaultMax) {
			v = defaultMax;
		}
		return (int) ((v - defaultMin) * (sliderRange - 1.0) / (defaultMax - defaultMin));
	}


	/**
	 *  Restore image outside non-rectangular roi.
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void doMasking(ImagePlus imp, ImageProcessor ip) {
		ImageProcessor mask = imp.getMask();

		if (mask != null) {
			ip.reset(mask);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp       Description of the Parameter
	 *@param  ip        Description of the Parameter
	 *@param  minvalue  Description of the Parameter
	 */
	void adjustMin(ImagePlus imp, ImageProcessor ip, double minvalue) {
		min = defaultMin + minvalue * (defaultMax - defaultMin) / (sliderRange - 1.0);
		if (max > defaultMax) {
			max = defaultMax;
		}
		if (min > max) {
			max = min;
		}
		setMinAndMax(ip, min, max);
		if (min == max) {
			setThreshold(ip);
		}
		if (RGBImage) {
			doMasking(imp, ip);
		}
		updateScrollBars(minSlider, false);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp       Description of the Parameter
	 *@param  ip        Description of the Parameter
	 *@param  maxvalue  Description of the Parameter
	 */
	void adjustMax(ImagePlus imp, ImageProcessor ip, double maxvalue) {
		max = defaultMin + maxvalue * (defaultMax - defaultMin) / (sliderRange - 1.0);
		//IJ.log("adjustMax: "+maxvalue+"  "+max);
		if (min < defaultMin) {
			min = defaultMin;
		}
		if (max < min) {
			min = max;
		}
		setMinAndMax(ip, min, max);
		if (min == max) {
			setThreshold(ip);
		}
		if (RGBImage) {
			doMasking(imp, ip);
		}
		updateScrollBars(maxSlider, false);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp     Description of the Parameter
	 *@param  ip      Description of the Parameter
	 *@param  bvalue  Description of the Parameter
	 */
	void adjustBrightness(ImagePlus imp, ImageProcessor ip, double bvalue) {
		double center = defaultMin + (defaultMax - defaultMin) * ((sliderRange - bvalue) / sliderRange);
		double width = max - min;

		min = center - width / 2.0;
		max = center + width / 2.0;
		setMinAndMax(ip, min, max);
		if (min == max) {
			setThreshold(ip);
		}
		if (RGBImage) {
			doMasking(imp, ip);
		}
		updateScrollBars(brightnessSlider, false);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp     Description of the Parameter
	 *@param  ip      Description of the Parameter
	 *@param  cvalue  Description of the Parameter
	 */
	void adjustContrast(ImagePlus imp, ImageProcessor ip, int cvalue) {
		double slope;
		double center = min + (max - min) / 2.0;
		double range = defaultMax - defaultMin;
		double mid = sliderRange / 2;

		if (cvalue <= mid) {
			slope = cvalue / mid;
		}
		else {
			slope = mid / (sliderRange - cvalue);
		}
		if (slope > 0.0) {
			min = center - (0.5 * range) / slope;
			max = center + (0.5 * range) / slope;
		}
		setMinAndMax(ip, min, max);
		if (RGBImage) {
			doMasking(imp, ip);
		}
		updateScrollBars(contrastSlider, false);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void reset(ImagePlus imp, ImageProcessor ip) {
		if (RGBImage) {
			ip.reset();
		}
		if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
			ip.resetMinAndMax();
			defaultMin = ip.getMin();
			defaultMax = ip.getMax();
			plot.defaultMin = defaultMin;
			plot.defaultMax = defaultMax;
		}
		min = defaultMin;
		max = defaultMax;
		setMinAndMax(ip, min, max);
		updateScrollBars(null, false);
		plotHistogram(imp);
		autoThreshold = 0;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 */
	void plotHistogram(ImagePlus imp) {
		ImageStatistics stats;

		if (balance && (channels == 4 || channels == 2 || channels == 1) && imp.getType() == ImagePlus.COLOR_RGB) {
			int w = imp.getWidth();
			int h = imp.getHeight();
			byte[] r = new byte[w * h];
			byte[] g = new byte[w * h];
			byte[] b = new byte[w * h];

			((ColorProcessor) imp.getProcessor()).getRGB(r, g, b);

			byte[] pixels = null;

			if (channels == 4) {
				pixels = r;
			}
			else if (channels == 2) {
				pixels = g;
			}
			else if (channels == 1) {
				pixels = b;
			}
			ImageProcessor ip = new ByteProcessor(w, h, pixels, null);

			stats = ImageStatistics.getStatistics(ip, 0, imp.getCalibration());
		}
		else {
			stats = imp.getStatistics();
		}

		Color color = Color.gray;

		if (imp instanceof CompositeImage) {
			color = ((CompositeImage) imp).getChannelColor();
		}
		plot.setHistogram(stats, color);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void apply(ImagePlus imp, ImageProcessor ip) {
		String option = null;

		if (RGBImage) {
			imp.unlock();
		}
		if (!imp.lock()) {
			return;
		}
		if (imp.getType() == ImagePlus.COLOR_RGB) {
			if (imp.getStackSize() > 1) {
				applyRGBStack(imp);
			}
			else {
				ip.snapshot();
				reset(imp, ip);
				imp.changes = true;
				if (Recorder.record) {
					Recorder.record("run", "Apply LUT");
				}
			}
			imp.unlock();
			return;
		}
		if (imp.getType() != ImagePlus.GRAY8) {
			IJ.beep();
			//EU_HOU Bundle
			IJ.showStatus(bun.getString("AdjTypeErr"));
			imp.unlock();
			return;
		}
		int[] table = new int[256];
		int min = (int) ip.getMin();
		int max = (int) ip.getMax();

		for (int i = 0; i < 256; i++) {
			if (i <= min) {
				table[i] = 0;
			}
			else if (i >= max) {
				table[i] = 255;
			}
			else {
				table[i] = (int) (((double) (i - min) / (max - min)) * 255);
			}
		}
		ip.setRoi(imp.getRoi());
		if (imp.getStackSize() > 1) {
			ImageStack stack = imp.getStack();
			//EU_HOU Bundle
			YesNoCancelDialog d = new YesNoCancelDialog(this,
					IJ.getBundle().getString("ProcStackTitle"), bun.getString("LUT1") + " " + stack.getSize() + " " + bun.getString("LUT2"));

			if (d.cancelPressed()) {
				imp.unlock();
				return;
			}
			if (d.yesPressed()) {
				int current = imp.getCurrentSlice();
				ImageProcessor mask = imp.getMask();

				for (int i = 1; i <= imp.getStackSize(); i++) {
					imp.setSlice(i);
					ip = imp.getProcessor();
					if (mask != null) {
						ip.snapshot();
					}
					ip.applyTable(table);
					ip.reset(mask);
				}
				imp.setSlice(current);
				option = "stack";
			}
			else {
				if (ip.getMask() != null) {
					ip.snapshot();
				}
				ip.applyTable(table);
				ip.reset(ip.getMask());
				option = "slice";
			}
		}
		else {
			if (ip.getMask() != null) {
				ip.snapshot();
			}
			ip.applyTable(table);
			ip.reset(ip.getMask());
		}
		reset(imp, ip);
		imp.changes = true;
		imp.unlock();
		if (Recorder.record) {
			if (option != null) {
				Recorder.record("run", "Apply LUT", option);
			}
			else {
				Recorder.record("run", "Apply LUT");
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 */
	void applyRGBStack(ImagePlus imp) {
		int current = imp.getCurrentSlice();
		int n = imp.getStackSize();
//EU_HOU Bundle
		if (!IJ.showMessageWithCancel(IJ.getBundle().getString("ProcStackTitle"), bun.getString("RGB1") + n + "\n" + bun.getString("RGB2") + "\n" +
				IJ.getBundle().getString("NoUndoConfirm"))) {
			return;
		}
		ImageProcessor mask = imp.getMask();

		for (int i = 1; i <= n; i++) {
			if (i != current) {
				imp.setSlice(i);

				ImageProcessor ip = imp.getProcessor();

				if (mask != null) {
					ip.snapshot();
				}
				setMinAndMax(ip, min, max);
				ip.reset(mask);
				IJ.showProgress((double) i / n);
			}
		}
		imp.setSlice(current);
		imp.changes = true;
		if (Recorder.record) {
			Recorder.record("run", "Apply LUT", "stack");
		}
	}


	/**
	 *  Sets the threshold attribute of the ContrastAdjuster object
	 *
	 *@param  ip  The new threshold value
	 */
	void setThreshold(ImageProcessor ip) {
		if (!(ip instanceof ByteProcessor)) {
			return;
		}
		if (((ByteProcessor) ip).isInvertedLut()) {
			ip.setThreshold(max, 255, ImageProcessor.NO_LUT_UPDATE);
		}
		else {
			ip.setThreshold(0, max, ImageProcessor.NO_LUT_UPDATE);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 *@param  ip   Description of the Parameter
	 */
	void autoAdjust(ImagePlus imp, ImageProcessor ip) {
		if (RGBImage) {
			ip.reset();
		}
		Calibration cal = imp.getCalibration();

		imp.setCalibration(null);

		ImageStatistics stats = imp.getStatistics();// get uncalibrated stats

		imp.setCalibration(cal);

		int limit = stats.pixelCount / 10;
		int[] histogram = stats.histogram;

		if (autoThreshold < 10) {
			autoThreshold = AUTO_THRESHOLD;
		}
		else {
			autoThreshold /= 2;
		}

		int threshold = stats.pixelCount / autoThreshold;
		int i = -1;
		boolean found = false;
		int count;

		do {
			i++;
			count = histogram[i];
			if (count > limit) {
				count = 0;
			}
			found = count > threshold;
		} while (!found && i < 255);
		int hmin = i;

		i = 256;
		do {
			i--;
			count = histogram[i];
			if (count > limit) {
				count = 0;
			}
			found = count > threshold;
		} while (!found && i > 0);
		int hmax = i;

		if (hmax >= hmin) {
			imp.killRoi();
			min = stats.histMin + hmin * stats.binSize;
			max = stats.histMin + hmax * stats.binSize;
			if (min == max) {
				min = stats.min;
				max = stats.max;
			}
			setMinAndMax(ip, min, max);
		}
		else {
			reset(imp, ip);
			return;
		}
		updateScrollBars(null, false);

		Roi roi = imp.getRoi();

		if (roi != null) {
			ImageProcessor mask = roi.getMask();

			if (mask != null) {
				ip.reset(mask);
			}
		}
		if (Recorder.record) {
			Recorder.record("run", "Enhance Contrast", "saturated=0.5");
		}
	}


	/**
	 *  Sets the minAndMax attribute of the ContrastAdjuster object
	 *
	 *@param  imp  The new minAndMax value
	 *@param  ip   The new minAndMax value
	 */
	void setMinAndMax(ImagePlus imp, ImageProcessor ip) {
		min = ip.getMin();
		max = ip.getMax();

		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor) || cal.calibrated() ? 2 : 0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		//EU_HOU Bundle
		GenericDialog gd = new GenericDialog(bun.getString("SetMinMaxTitle"));
//EU_HOU Bundle
		gd.addNumericField(bun.getString("MinVal"), minValue, digits);
		//EU_HOU Bundle
		gd.addNumericField(bun.getString("MaxVal"), maxValue, digits);
//EU_HOU Bundle
		gd.addCheckbox("Propagate to all open images", false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		minValue = gd.getNextNumber();
		maxValue = gd.getNextNumber();
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);

		boolean propagate = gd.getNextBoolean();

		if (maxValue >= minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(ip, min, max);
			updateScrollBars(null, false);
			if (RGBImage) {
				doMasking(imp, ip);
			}
			if (propagate) {
				IJ.runMacroFile("ij.jar:PropagateMinAndMax");
			}
			if (Recorder.record) {
				if (imp.getBitDepth() == 32) {
					Recorder.record("setMinAndMax", min, max);
				}
				else {
					Recorder.record("setMinAndMax", (int) min, (int) max);
				}
			}
		}
	}


	/**
	 *  Sets the windowLevel attribute of the ContrastAdjuster object
	 *
	 *@param  imp  The new windowLevel value
	 *@param  ip   The new windowLevel value
	 */
	void setWindowLevel(ImagePlus imp, ImageProcessor ip) {
		min = ip.getMin();
		max = ip.getMax();

		Calibration cal = imp.getCalibration();
		int digits = (ip instanceof FloatProcessor) || cal.calibrated() ? 2 : 0;
		double minValue = cal.getCValue(min);
		double maxValue = cal.getCValue(max);
		//IJ.log("setWindowLevel: "+min+" "+max);
		double windowValue = maxValue - minValue;
		double levelValue = minValue + windowValue / 2.0;
		//EU_HOU Bundle
		GenericDialog gd = new GenericDialog(bun.getString(""));
		//EU_HOU Bundle

		gd.addNumericField(bun.getString("WinCenter"), levelValue, digits);
		//EU_HOU Bundle
		gd.addNumericField(bun.getString("WinWidth"), windowValue, digits);
		//EU_HOU Bundle
		gd.addCheckbox("Propagate to all open images", false);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		levelValue = gd.getNextNumber();
		windowValue = gd.getNextNumber();
		minValue = levelValue - (windowValue / 2.0);
		maxValue = levelValue + (windowValue / 2.0);
		minValue = cal.getRawValue(minValue);
		maxValue = cal.getRawValue(maxValue);

		boolean propagate = gd.getNextBoolean();

		if (maxValue >= minValue) {
			min = minValue;
			max = maxValue;
			setMinAndMax(ip, minValue, maxValue);
			updateScrollBars(null, false);
			if (RGBImage) {
				doMasking(imp, ip);
			}
			if (propagate) {
				IJ.runMacroFile("ij.jar:PropagateMinAndMax");
			}
			if (Recorder.record) {
				Recorder.record("setMinAndMax", (int) min, (int) max);
			}
		}
	}


	final static int RESET = 0, AUTO = 1, SET = 2, APPLY = 3, THRESHOLD = 4, MIN = 5, MAX = 6,
			BRIGHTNESS = 7, CONTRAST = 8, UPDATE = 9;

	// Separate thread that does the potentially time-consuming processing
	/**
	 *  Main processing method for the ContrastAdjuster object
	 */
	public void run() {
		while (!done) {
			synchronized (this) {
				try {
					wait();
				}
				catch (InterruptedException e) {}
			}
			doUpdate();
		}
	}


	/**
	 *  Description of the Method
	 */
	void doUpdate() {
		ImagePlus imp;
		ImageProcessor ip;
		int action;
		int minvalue = minSliderValue;
		int maxvalue = maxSliderValue;
		int bvalue = brightnessValue;
		int cvalue = contrastValue;

		if (doReset) {
			action = RESET;
		}
		else if (doAutoAdjust) {
			action = AUTO;
		}
		else if (doSet) {
			action = SET;
		}
		else if (doApplyLut) {
			action = APPLY;
		}
		else if (minSliderValue >= 0) {
			action = MIN;
		}
		else if (maxSliderValue >= 0) {
			action = MAX;
		}
		else if (brightnessValue >= 0) {
			action = BRIGHTNESS;
		}
		else if (contrastValue >= 0) {
			action = CONTRAST;
		}
		else {
			return;
		}
		minSliderValue = maxSliderValue = brightnessValue = contrastValue = -1;
		doReset = doAutoAdjust = doSet = doApplyLut = false;
		imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.beep();
			//EU_HOU Bundle
			IJ.showStatus(bun.getString("NoImg"));
			return;
		}
		ip = imp.getProcessor();
		if (RGBImage && !imp.lock()) {
			imp = null;
			return;
		}
		//IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
		switch (action) {
						case RESET:
							reset(imp, ip);
							if (Recorder.record) {
								Recorder.record("resetMinAndMax");
							}
							break;
						case AUTO:
							autoAdjust(imp, ip);
							break;
						case SET:
							if (windowLevel) {
								setWindowLevel(imp, ip);
							}
							else {
								setMinAndMax(imp, ip);
							}
							break;
						case APPLY:
							apply(imp, ip);
							break;
						case MIN:
							adjustMin(imp, ip, minvalue);
							break;
						case MAX:
							adjustMax(imp, ip, maxvalue);
							break;
						case BRIGHTNESS:
							adjustBrightness(imp, ip, bvalue);
							break;
						case CONTRAST:
							adjustContrast(imp, ip, cvalue);
							break;
		}
		updatePlot();
		updateLabels(imp, ip);
		imp.updateChannelAndDraw();
		if (RGBImage) {
			imp.unlock();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowClosing(WindowEvent e) {
		close();
		Prefs.saveLocation(LOC_KEY, getLocation());
	}


	/**
	 *  Overrides close() in PlugInFrame.
	 */
	public void close() {
		super.close();
		instance = null;
		done = true;
		synchronized (this) {
			notify();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void windowActivated(WindowEvent e) {
		super.windowActivated(e);
		setup();
		WindowManager.setWindow(this);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public synchronized void itemStateChanged(ItemEvent e) {
		channels = channelConstants[choice.getSelectedIndex()];
		doReset = true;
		notify();
	}


	/**
	 *  Resets this ContrastAdjuster and brings it to the front.
	 */
	public void updateAndDraw() {
		previousImageID = 0;
		toFront();
	}


	/**
	 *  Updates the ContrastAdjuster.
	 */
	public static void update() {
		if (instance != null) {
			ContrastAdjuster ca = ((ContrastAdjuster) instance);

			ca.previousImageID = 0;
			ca.setup();
		}
	}

}// ContrastAdjuster class

/*
    EU_HOU CHANGES UNUSED
  */
/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    15 octobre 2007
 */
class ColorTable extends Canvas {


	private Image img;
	LookUpTable lut;
	long min = -1;
	long max = -1;
	long orgmin, orgmax;
	long newmax, newmin;
	//int histogram[] = new int[64];
	double slope;
	long factor;
	double bval;
	boolean magdisp = false;


	/**
	 *  Constructor for the ColorTable object
	 */
	public ColorTable() {
		System.out.println("B&C lut " + ContrastAdjuster.WIDTH + " " + ContrastAdjuster.HEIGHT);
		setSize(ContrastAdjuster.WIDTH + 2, ContrastAdjuster.HEIGHT + 2);
		min = 0;
		max = 255;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		/*
		    min = newmin;
		    max = newmax;
		    img = createImage(ContrastAdjuster.WIDTH, ContrastAdjuster.HEIGHT);
		    Graphics gr = img.getGraphics();
		    double step = ((double) (max - min)) / 64;
		    64 samples
		    int isg;
		    try {
		    isg = WindowManager.getCurrentImage().getType();
		    lut = new LookUpTable(WindowManager.getCurrentImage().getImage());
		    }
		    catch (Exception ex) {
		    return;
		    }
		    boolean isGray;
		    if (lut != null) {
		    lut = WindowManager.getCurrentImage().createLut();
		    }
		    isGray = lut.isGrayscale() && !((isg == ImagePlus.COLOR_256) || (isg == ImagePlus.COLOR_RGB));
		    double width;
		    if (min == max) {
		    width = ContrastAdjuster.WIDTH;
		    slope = Long.MAX_VALUE;
		    }
		    else {
		    width = ContrastAdjuster.WIDTH / 64;
		    slope = (double) (orgmax - orgmin) / (double) (max - min);
		    }
		    slope = (double)(orgmax-orgmin)/(double)(max-min);
		    float org = ((float) (min - orgmin) / (float) (orgmax - orgmin));
		    int xorg = (int) (org * (float) ContrastAdjuster.WIDTH);
		    if (magdisp) {
		    xorg = 0;
		    slope = 256 / ContrastAdjuster.WIDTH;
		    }
		    gr.setColor(Color.white);
		    gr.fillRect(0, 0, ContrastAdjuster.WIDTH, ContrastAdjuster.HEIGHT);
		    byte[] reds;
		    byte[] greens;
		    byte[] blues;
		    int mapSize = lut.getMapSize();
		    java.awt.image.ColorModel cm = lut.getColorModel();
		    if (cm instanceof IndexColorModel) {
		    IndexColorModel m = (IndexColorModel) cm;
		    mapSize = m.getMapSize();
		    reds = new byte[mapSize];
		    greens = new byte[mapSize];
		    blues = new byte[mapSize];
		    m.getReds(reds);
		    m.getGreens(greens);
		    m.getBlues(blues);
		    }
		    else {
		    mapSize = 256;
		    reds = new byte[mapSize];
		    greens = new byte[mapSize];
		    blues = new byte[mapSize];
		    for (int i = 0; i < mapSize; i++) {
		    reds[i] = (byte) i;
		    greens[i] = (byte) i;
		    blues[i] = (byte) i;
		    }
		    }
		    int xend = (slope != Long.MAX_VALUE ?
		    (int) (((float) ContrastAdjuster.WIDTH) / slope) + xorg
		    : Integer.MAX_VALUE);
		    if (xend != Integer.MAX_VALUE) {
		    for (int i = xorg; i < xend; i += (int) width) {
		    int j = (int) ((double) ((i - xorg) * mapSize) / (double) (xend - xorg));
		    gr.setColor(new Color(reds[j] & 0xff, greens[j] & 0xff, blues[j] & 0xff));
		    gr.fillRect(i, 0, (int) width, ContrastAdjuster.HEIGHT);
		    }
		    }
		    else {
		    gr.setColor(new Color(reds[mapSize - 1] & 0xff, greens[mapSize - 1] & 0xff, blues[mapSize - 1] & 0xff));
		    gr.fillRect(0, 0, ContrastAdjuster.WIDTH, ContrastAdjuster.HEIGHT);
		    }
		    if (xorg > 0) {
		    gr.setColor(new Color(reds[0] & 0xff, greens[0] & 0xff, blues[0] & 0xff));
		    gr.fillRect(0, 0, xorg, ContrastAdjuster.HEIGHT);
		    }
		    if (xend < ContrastAdjuster.WIDTH) {
		    gr.setColor(new Color(reds[mapSize - 1] & 0xff, greens[mapSize - 1] & 0xff, blues[mapSize - 1] & 0xff));
		    gr.fillRect(xend, 0, ContrastAdjuster.WIDTH - xend, ContrastAdjuster.HEIGHT);
		    }
		    g.drawImage(img, 0, 0, this);
		  */
		System.out.println("drawImage B&C LUT OK");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void update(Graphics g) {
		if (min == newmin && max == newmax) {
			return;
		}

		paint(g);
	}
}
/*
    EU_HOU CHANGES
  */
/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    19 novembre 2007
 */
class ContrastPlot extends Canvas implements MouseListener {


	final static int WIDTH = 256, HEIGHT = 64;
	double defaultMin = 0;
	double defaultMax = 255;
	double min = 0;
	double max = 255;
	int[] histogram;
	int hmax;
	Image os;
	Graphics osg;
	Color color = Color.gray;


	/**
	 *  Constructor for the ContrastPlot object
	 */
	public ContrastPlot() {
		addMouseListener(this);
		setSize(WIDTH + 1, HEIGHT + 1);
	}


	/**
	 *  Overrides Component getPreferredSize(). Added to work around a bug in Java
	 *  1.4.1 on Mac OS X.
	 *
	 *@return    The preferredSize value
	 */
        @Override
	public Dimension getPreferredSize() {
		return new Dimension(WIDTH + 1, HEIGHT + 1);
	}


	/**
	 *  Sets the histogram attribute of the ContrastPlot object
	 *
	 *@param  stats  The new histogram value
	 *@param  color  The new histogram value
	 */
	void setHistogram(ImageStatistics stats, Color color) {
		this.color = color;
		histogram = stats.histogram;
		if (histogram.length != 256) {
			histogram = null;
			return;
		}
		//for (int i = 0; i < 128; i++) {
		//	histogram[i] = (histogram[2 * i] + histogram[2 * i + 1]) / 2;
		//}

		int maxCount = 0;
		int mode = 0;

		for (int i = 0; i < WIDTH; i++) {
			if (histogram[i] > maxCount) {
				maxCount = histogram[i];
				mode = i;
			}
		}

		int maxCount2 = 0;

		for (int i = 0; i < WIDTH; i++) {
			if ((histogram[i] > maxCount2) && (i != mode)) {
				maxCount2 = histogram[i];
			}
		}
		hmax = stats.maxCount;
		if ((hmax > (maxCount2 * 2)) && (maxCount2 != 0)) {
			hmax = (int) (maxCount2 * 1.5);
			histogram[mode] = hmax;
		}
		os = null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void update(Graphics g) {
		paint(g);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		int x1;
		int y1;
		int x2;
		int y2;
		int x12;
		int x22;

		double scale = (double) WIDTH / (defaultMax - defaultMin);
		double slope = 0.0;

		if (max != min) {
			slope = HEIGHT / (max - min);
		}
		if (min >= defaultMin) {
			x1 = (int) (scale * (min - defaultMin));
			y1 = HEIGHT;
		}
		else {
			x1 = 0;
			x12 = 0;
			if (max > min) {
				y1 = HEIGHT - (int) ((defaultMin - min) * slope);
			}
			else {
				y1 = HEIGHT;
			}
		}
		if (max <= defaultMax) {
			x2 = (int) (scale * (max - defaultMin));
			y2 = 0;
		}
		else {
			x2 = WIDTH;
			x22 = WIDTH;
			if (max > min) {
				y2 = HEIGHT - (int) ((defaultMax - min) * slope);
			}
			else {
				y2 = 0;
			}
		}

		if (histogram != null) {
			// EU_HOU
			//if (os == null && hmax != 0) {
			os = createImage(WIDTH, HEIGHT);
			osg = os.getGraphics();
			osg.setColor(Color.orange);
			osg.fillRect(0, 0, WIDTH, HEIGHT);
			osg.setColor(color);
			// EU_HOU
			int idx = 0;
			LookUpTable lut = WindowManager.getCurrentImage().createLut();
			byte[] reds = null;
			byte[] greens = null;
			byte[] blues = null;
			if (lut != null) {
				reds = lut.getReds();
				greens = lut.getGreens();
				blues = lut.getBlues();
			}
			System.out.println("lut=" + lut + " r=" + reds);
			for (int i = 0; i < WIDTH; i++) {
				if (i <= x1) {
					idx = 0;
				}
				else if (i >= x2) {
					idx = 255;
				}
				else {
					idx = (int) (255 * (i - x1) / (x2 - x1 - 1));
				}
				//System.out.println("i=" + i + " idx=" + idx + " r=" + (reds[idx] & 0xff) + " g=" + (greens[idx] & 0xff) + " b=" + (blues[idx] & 0xff));
				if (reds != null) {
					osg.setColor(new Color(reds[idx] & 0xff, greens[idx] & 0xff, blues[idx] & 0xff));
				}
				else {
					osg.setColor(new Color(idx, idx, idx));
				}
				osg.drawLine(i, HEIGHT, i, HEIGHT - ((int) (HEIGHT * histogram[i]) / hmax));
			}
			osg.dispose();
			//}
			if (os != null) {
				g.drawImage(os, 0, 0, this);
			}
		}
		else {
			g.setColor(Color.white);
			g.fillRect(0, 0, WIDTH, HEIGHT);
		}

		//g.setColor(Color.black);
		//g.drawLine(x1, y1, x2, y2);
		/*
		    EU_HOU CHANGES barres de couleur
		  */
		g.setColor(Color.red);
		g.drawRect(x1, 0, WIDTH - x1, HEIGHT);
		g.setColor(Color.blue);
		g.drawRect(0, 0, x2, HEIGHT);
		g.setColor(Color.green);
		//g.drawLine(x2, HEIGHT - 5, x2, HEIGHT);
		/*
		    EU_HOU END
		  */
		g.drawRect(0, 0, WIDTH, HEIGHT);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mousePressed(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void mouseExited(MouseEvent e) { }


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

}// ContrastPlot class

/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    19 novembre 2007
 */
class TrimmedLabel extends Label {


	int trim = IJ.isMacOSX() && IJ.isJava14() ? 0 : 6;


	/**
	 *  Constructor for the TrimmedLabel object
	 *
	 *@param  title  Description of the Parameter
	 */
	public TrimmedLabel(String title) {
		super(title);
	}


	/**
	 *  Gets the minimumSize attribute of the TrimmedLabel object
	 *
	 *@return    The minimumSize value
	 */
	public Dimension getMinimumSize() {
		return new Dimension(super.getMinimumSize().width, super.getMinimumSize().height - trim);
	}


	/**
	 *  Gets the preferredSize attribute of the TrimmedLabel object
	 *
	 *@return    The preferredSize value
	 */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

}// TrimmedLabel class


