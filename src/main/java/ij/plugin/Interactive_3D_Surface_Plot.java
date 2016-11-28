package ij.plugin;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.plugin.jRenderer3D.JRenderer3D;
import ij.plugin.jRenderer3D.Line3D;
import ij.plugin.jRenderer3D.Text3D;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.ImageObserver;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * Interactive_3D_SurfacePlot (was SurfacePlot_3D)
 *
 * (C) Author:  Kai Uwe Barthel: barthel (at) fhtw-berlin.de
 *
 * Version 2.22
 * 			2007 Nov. 2
 * 			- axes may be labeled with number and units
 * 			- z-ratio can be selected to be equal to the xy-ratio or it may be adapted automatically
 * 			- fixed a bug which could cause the frame to be resized on start-up
 * 			  leading to wrong sizes, display or even crashes
 *
 * Version 2.1
 * 			2007 Jan. 15
 * 			corrected plots for selections
 * 			faster filled mode
 * 			gradient coloring
 * 			isolines (may be improved)
 * 			(changed all transform data to double)
 *
 * Version 2.0 (.01)
 * 			2007 Jan. 9
 * 			resizabel canvas
 * 			scaling options
 *
 *
 * Version 1.5 (.03)
 * 			2005 Oct. 10
 * 			texture mapping
 * 			aspect ratio is preserved
 *
 * Version 1.4
 * 			surface plot image can be converted to an ImageJ image
 * 			better interpolation for lut-colors, faster filled mode
 *
 * Version 1.33
 * 			better lighting, better interpolation for Luts
 *
 * Version 1.3			November, 26 2004
 * 			Smoothing and ligthing of the plot, better "filled" mode
 *
 * Version 1.2			November, 20 2004
 * 			only the bounding box of the selection is taken for the plot
 * 			8Bit Color fixed,
 * 			Mesh Mode, Fill Mode fixed, better scaling
 *
 * Version 1.0           November, 19 2004
 * 		First version
 *
 */
/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    27 novembre 2007
 */
public class Interactive_3D_Surface_Plot implements PlugIn, MouseListener, MouseMotionListener, ItemListener {

	private final String version = " v2.22 ";

//	private String[] params   = {
//			"display_mode=",
//			"lut=",
//			"z-aspect=",
//			"dist=",
//			"depth=",
//			"thresh=",
//			"axes=",
//			"markers=",
//			"scale=",
//			"angle_x=",
//			"angle_z="
//	};
//
//	private float[] paramVals = {
//			SLICE_TRILINEAR,   // "display_mode=",
//			ORIG,   // "lut=",
//			1,   // "z-aspect=",
//			0,   // "dist=",
//			6,   // "depth=",
//			128, // "thresh=",
//			1,   // "axes=",
//			1,   // "markers=",
//			1,   // "scale=",
//			115, // "angle_x=",
//			-35  // "angle_z="
//	};


	// constants
	private final String DOTS = "Dots";
	private final String LINES = "Lines";
	private final String MESH = "Mesh";
	private final String ISOLINES = "Isolines";
	private final String FILLED = "Filled";
	/**
	 *  Description of the Field
	 */
	protected String plotType = LINES;

	private final String ORIGINAL = "Original Colors";
	private final String GRAYSCALE = "Grayscale";
	private final String SPECTRUM = "Spectrum LUT";
	private final String FIRE = "Fire LUT";
	private final String THERMAL = "Thermal LUT";
	private final String GRADIENT = "Gradient";
	//private final String GRADIENT2 = "Gradient2";
	private final String ORANGE = "Orange";
	private final String BLUE = "Blue";
	private String colorType = ORIGINAL;

	// application window
	private JFrame frame;

	// panels
	private JPanel mainPanel;
	private JPanel settingsPanel1;
	private JPanel settingsPanel2;

	// setting components
	private JComboBox comboDisplayType;
	private JComboBox comboDisplayColors;

	private JSlider sliderLight;
	private JSlider sliderGridSize;
	private JSlider sliderSmoothing;
	private JSlider sliderScale;
	private JSlider sliderZRatio;
	private JSlider sliderPerspective;
	private JSlider sliderMin;
	private JSlider sliderMax;

	private JCheckBox checkInverse, checkAxes, checkText, checkLines, checkAuto;

	// image panel / canvas
	private ImageRegion imageRegion;

	// imageJ components
	private ImagePlus image;

	// imgeJ3D API components
	private JRenderer3D jRenderer3D;

	// other global params
	final static int SIZE = 600;
	private int startWindowWidth = SIZE;
	private int startWindowHeight = SIZE;
	private int windowWidth = SIZE;
	private int windowHeight = SIZE;

	private double scaleWindow = 1.;// scaling caused by the resize

	private int xStart;
	private int yStart;
	private boolean drag;
	private int xdiff;
	private int ydiff;
	private double light = 0.2;
	private double smoothOld = 0;

	private boolean invertZ = false;

	private boolean isExamplePlot = false;

	private int imageWidth;
	private int imageHeight;

	private double scaleInit = 1;
	private double zRatioInit = 1;

	private double scaledWidth;

	private double scaledHeight;

	private double minVal;

	private double maxVal;

	private String units;

	private double maxDistance;

	private Calibration cal;

	private boolean isEqualxyzRatio = false;

	private double zAspectRatioSlider = 1;

	private double zAspectRatio = 1;

	private int maxS = 100;

	private int minS = 0;



	/**
	 *  Description of the Method
	 *
	 *@param  args  Description of the Parameter
	 */
	public static void main(String args[]) {
	Interactive_3D_Surface_Plot sp = new Interactive_3D_Surface_Plot();

//		//new ImageJ(); // !!!
//
//		//IJ.open("/users/barthel/Desktop/Depth_Image.tif");
//		//		IJ.open("/users/barthel/Desktop/K2.tif");
//		IJ.open("/users/barthel/Desktop/t.tif");
//		//IJ.open("/users/barthel/Desktop/image_128-1.png");
//		//IJ.open("/users/barthel/Desktop/Dot_Blot.jpg");
//		//IJ.open("/users/barthel/Desktop/plot2.tif");
//		//IJ.run("Set Scale...", "distance=1.001 known=100 pixel=1 unit=µm");
//		//IJ.run("Set Scale...", "distance=2.2 known=5 pixel=1 unit=µm");
//		//IJ.run("Set Scale...", "distance=12 known=1000 pixel=1 unit=µm");
//		//IJ.run("Set Scale...", "distance=30 known=0.5 pixel=1 unit=µm");
//		//IJ.run("Fire");
//		//IJ.makeRectangle(80, 80, 4, 5);
//		IJ.makeOval(40, 40, 120, 150);
//		//IJ.run("makePolygon(98,147,163,92,243,127,206,228,147,197,150,180)");
//		//IJ.makeLine(80, 80, 80,80);
//
//
//
//		sp.image = IJ.getImage();
//
//		int[] xpoints = new int[]{98,163,243,206,147,150};
//		int[] ypoints = new int[]{147,92,127,228,197,180};
//		Roi roi1 = new PolygonRoi(xpoints, ypoints, xpoints.length, Roi.POLYGON);
//		sp.image.setRoi(roi1);
//
//		Roi roi = sp.image.getRoi();
//
//		if (roi != null) {
//			Rectangle rect = roi.getBoundingRect();
//			if (rect.x < 0)
//				rect.x = 0;
//			if (rect.y < 0)
//				rect.y = 0;
//			sp.imageWidth = rect.width;
//			sp.imageHeight = rect.height;
//
//			if (sp.imageWidth == 0 ||sp.imageHeight == 0) {
//				sp.image.killRoi();
//				sp.imageWidth = sp.image.getWidth();
//				sp.imageHeight = sp.image.getHeight();
//			}
//		}
//		else {
//			sp.imageWidth = sp.image.getWidth();
//			sp.imageHeight = sp.image.getHeight();
//		}
//
////		sp.image.show();
////		sp.image.updateAndDraw();



		sp.generateSampleImage();

		sp.runApplication("Example Plot");

	}


	/**
	 *  Description of the Method
	 */
	private void generateSampleImage() {

		imageWidth = 256;
		imageHeight = 256;
	int[] pixels = new int[imageWidth * imageHeight];

		for (int y = 0; y < imageHeight; y++) {
		int dy1 = y - 80;
		int dy2 = y - 60;

			for (int x = 0; x < imageWidth; x++) {
			int dx1 = x - 90;
			int dx2 = x - 180;
			double r1 = Math.sqrt(dx1 * dx1 + dy1 * dy1) / 60;
			double r2 = Math.sqrt(dx2 * dx2 + dy2 * dy2) / 100;

			int v1 = (int) (255 * Math.exp(-r2 * r2));
			int v2 = (int) (255 * Math.exp(-r1 * r1));

				pixels[y * imageWidth + x] = 0xFF000000 | ((int) (v2 + v1) << 16) | (int) ((v2) << 8) | (y / 4);
			}
		}
	MemoryImageSource source = new MemoryImageSource(imageWidth, imageHeight, pixels, 0, imageWidth);

	Image awtImage = Toolkit.getDefaultToolkit().createImage(source);

	BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	Graphics2D g2D = bufferedImage.createGraphics();

		g2D.drawImage(awtImage, 0, 0, null);

	float ninth = 1.0f / 9.0f;
	float[] blurKernel = {
				ninth, ninth, ninth,
				ninth, ninth, ninth,
				ninth, ninth, ninth,
				};

	BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel));
		bufferedImage = op.filter(bufferedImage, null);
		bufferedImage = op.filter(bufferedImage, null);
		g2D = bufferedImage.createGraphics();
		g2D.setColor(new Color(0x3300FF));
	Font font = new Font("Sans", Font.BOLD, 60);
		g2D.setFont(font);
		g2D.drawString("ImageJ", 20, 220);

		bufferedImage = op.filter(bufferedImage, null);
		bufferedImage = op.filter(bufferedImage, null);

		g2D.dispose();

		image = new ImagePlus("Example Plot", bufferedImage);
		isExamplePlot = true;
	}


	/**
	 *  Main processing method for the Interactive_3D_Surface_Plot object
	 *
	 *@param  arg  Description of the Parameter
	 */
	public void run(String arg) {
		if (IJ.versionLessThan("1.36b")) {
			return;
		}

		image = WindowManager.getCurrentImage();

		if (image != null) {
		Roi roi = image.getRoi();

			if (roi != null) {
			Rectangle rect = roi.getBoundingRect();
				if (rect.x < 0) {
					rect.x = 0;
				}
				if (rect.y < 0) {
					rect.y = 0;
				}
				imageWidth = rect.width;
				imageHeight = rect.height;

				if (imageWidth == 0 || imageHeight == 0) {
					image.killRoi();
					imageWidth = image.getWidth();
					imageHeight = image.getHeight();
				}
			} else {
				imageWidth = image.getWidth();
				imageHeight = image.getHeight();
			}
		} else {
			generateSampleImage();
		}

		runApplication(image.getTitle());
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 */
	private void runApplication(String name) {
	// create window/frame
	String str = "Interactive 3D Surface Plot" + version + " (" + name + ")";
		frame = new JFrame(str);

		// create application gui
		createGUI();

		// creates the 3d renderer // NOTE: image must be loaded
		create3DRenderer();

		frame.pack();
	}



	/**
	 *  * 3D RENDERER * *
	 */

	/**
	 *  Initializes the JRenderer3D. Set Background, the surface plot, plot mode,
	 *  lightning mode. Adds a coordinate system. Sets scale. Renders and updates
	 *  the image.
	 */
	private void create3DRenderer() {

	double wc = (imageWidth) / 2.;
	double hc = (imageHeight) / 2.;
	double dc = 256 / 2.;

		cal = image.getCalibration();

	ImageProcessor ip = image.getProcessor();

		scaledWidth = cal.getX(imageWidth);
		scaledHeight = cal.getY(imageHeight);

		minVal = ip.getMin();
		maxVal = ip.getMax();
		units = cal.getUnits();

//		IJ.log("Units: " + units);
//		IJ.log("X: " + scaledWidth);
//		IJ.log("Y: " + scaledHeight);
//		IJ.log("scaled: " + cal.scaled());
//		//IJ.log("Ratio: " + cal.());
//		IJ.log("Min: " + minVal);
//		IJ.log("Max: " + maxVal);


		// create 3D renderer
		// center in the middle of the image
		jRenderer3D = new JRenderer3D(wc, hc, dc);
		jRenderer3D.setBufferSize(startWindowWidth, startWindowHeight);

		setScaleAndZRatio();

	int grid = 128;

	int gridHeight;

	int gridWidth;
		;
		if (imageHeight > imageWidth) {
			gridHeight = grid;
			gridWidth = grid * imageWidth / imageHeight;
		} else {
			gridWidth = grid;
			gridHeight = grid * imageHeight / imageWidth;
		}

		jRenderer3D.setSurfacePlotGridSize(gridWidth, gridHeight);

		// surface plot
		jRenderer3D.setSurfacePlot(image);

		if (isExamplePlot) {
			colorType = GRADIENT;
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_GRADIENT);
			plotType = FILLED;
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_FILLED);

			jRenderer3D.setTransformRotationXYZ(60, 0, -20);// viewing angle (in degrees)
			//scaleInit *= 0.86;
		} else {
			colorType = ORIGINAL;
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_ORIGINAL);
			plotType = LINES;
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_LINES);

			jRenderer3D.setTransformRotationXYZ(65, 0, 39);// viewing angle (in degrees)
		}

	double smooth = sliderSmoothing.getValue() * (grid / 512.);
		jRenderer3D.setSurfaceSmoothingFactor(smooth);

		jRenderer3D.setSurfacePlotLight(sliderLight.getValue() / 100.);

//		renderAndUpdateDisplay();
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {}
		renderAndUpdateDisplay();
	}


	/**
	 *  Sets the scaleAndZRatio attribute of the Interactive_3D_Surface_Plot object
	 */
	private void setScaleAndZRatio() {
		if (isEqualxyzRatio) {
			zRatioInit = (maxVal - minVal) / (255 * scaledWidth / imageWidth);

			//  determine initial scale factor
			scaleInit = 0.55 * Math.max(startWindowHeight, startWindowWidth) / (double) Math.max(imageWidth, Math.max(255 * zRatioInit, imageHeight));
		} else {
			scaleInit = 0.55 * Math.max(startWindowHeight, startWindowWidth) / (double) Math.max(imageHeight, imageWidth);
			zRatioInit = 0.55 * startWindowHeight / (256 * scaleInit);
		}

		zAspectRatio = zRatioInit * zAspectRatioSlider;

		scaleWindow = Math.min(windowHeight, windowWidth) / (double) startWindowHeight;

		//IJ.log("Z-Ratio: " + zRatioInit);

		jRenderer3D.setTransformZAspectRatio(zAspectRatio);
	double scaleSlider = sliderScale.getValue() / 100.;

	double scale = scaleInit * scaleSlider * scaleWindow;
		//IJ.log("ScaleInit: " + scaleInit + " ScaleWindow " + scaleWindow + " Scale " + scale);

		jRenderer3D.setTransformScale(scale);

		jRenderer3D.setTransformPerspective(sliderPerspective.getValue() / 100.);

		maxDistance = Math.max(scaledWidth, Math.max(scaledHeight, 256 * Math.max(zAspectRatio, 1)));
		jRenderer3D.setTransformMaxDistance(maxDistance);

		addCoordinateSystem();

	}


	/**
	 *  Draws a simple coordinate system and labels it
	 */
	private void addCoordinateSystem() {

		jRenderer3D.clearText();
		jRenderer3D.clearLines();
		jRenderer3D.clearCubes();

	int id = 256;

	double min = minVal + minS / 100. * (maxVal - minVal);
	double max = maxVal - (100. - maxS) / 100 * (maxVal - minVal);
//		System.out.println("MinS " + minS + " MaxS " + maxS + " MinVal " + minVal + " MaxVal " + maxVal + " min " + min );
//		System.out.println("scaleInit " + scaleInit + " zRatioInit " + zRatioInit );

	// add text to the coordinate system

	double off = 12 / scaleInit;// text position offset
	double fontSize = 10 / scaleInit;
	double offZ = off / zAspectRatio;
	int ticksDist = 40;
	Color textColor = Color.white;
	Color lineColor = new Color(90, 90, 100);

	double x1 = 0;
	double y1 = 0;
	double z1 = 0;
	double x2 = imageWidth;
	double y2 = imageHeight;
	double z2 = id;

	int numTicks = (int) Math.round(imageHeight * scaleInit / ticksDist);
	double pos = 0;
	double stepValue = calcStepSize(scaledHeight, numTicks);

		for (double value = 0; value <= scaledHeight; value += stepValue) {
		String s;
			if (Math.floor(value) - value == 0) {
				s = "" + (int) value;
			} else {
				s = "" + (int) Math.round(value * 1000) / 1000.;
			}
			// unit String for the last position
			if (value + stepValue > scaledHeight || value == scaledHeight) {
				if (!units.equals("pixels")) {
					s = "y/" + units;
				} else {
					s = "y";
				}
			}

			pos = (value * imageHeight / scaledHeight);
			y1 = y2 = pos;

			jRenderer3D.addText3D(new Text3D(s, x1 - off, y1, z1 - offZ, textColor, fontSize, 2));
			jRenderer3D.addText3D(new Text3D(s, x2 + off, y2, z1 - offZ, textColor, fontSize));

			jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x1, y1, z2, lineColor, true));
			jRenderer3D.addLine3D(new Line3D(x2, y2, z1, x2, y2, z2, lineColor));

			jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x2, y2, z1, lineColor, true));
			jRenderer3D.addLine3D(new Line3D(x1, y1, z2, x2, y2, z2, lineColor));
		}

		numTicks = (int) Math.round(imageWidth * scaleInit / ticksDist);
		stepValue = calcStepSize(scaledWidth, numTicks);

		y1 = 0;
		y2 = imageHeight;
		for (double value = 0; value <= scaledWidth; value += stepValue) {
		String s;
			if (Math.floor(value) - value == 0) {
				s = "" + (int) value;
			} else {
				s = "" + (int) Math.round(value * 1000) / 1000.;
			}
			if (value + stepValue > scaledWidth || value == scaledWidth) {
				if (!units.equals("pixels")) {
					s = "x/" + units;
				} else {
					s = "x";
				}
			}

			pos = value * imageWidth / scaledWidth;
			x1 = x2 = pos;

			jRenderer3D.addText3D(new Text3D(s, x1, y1 - off, z1 - offZ, textColor, fontSize, 2));
			jRenderer3D.addText3D(new Text3D(s, x2, y2 + off, z1 - offZ, textColor, fontSize));

			jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x1, y1, z2, lineColor, true));
			jRenderer3D.addLine3D(new Line3D(x2, y2, z1, x2, y2, z2, lineColor));

			jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x2, y2, z1, lineColor, true));
			jRenderer3D.addLine3D(new Line3D(x1, y1, z2, x2, y2, z2, lineColor));
		}

	double d = max - min;
		numTicks = (int) Math.round(255 * zAspectRatio * scaleInit / (ticksDist / 1.3));
		stepValue = calcStepSize(d, numTicks);

		x1 = 0;
		y1 = 0;
		x2 = imageWidth;
		y2 = imageHeight;

	double minStart = Math.floor(min / stepValue) * stepValue;
	double delta = minStart - min;

		for (double value = 0; value + delta <= d; value += stepValue) {
		String s;
			if ( /*Math.abs(value) > 1 && */Math.floor(minStart + value) - (minStart + value) == 0) {
				s = "" + (int) (minStart + value);
			} else {
				s = "" + (int) Math.round((minStart + value) * 1000) / 1000.;
			}
			pos = ((value + delta) * id / d);
			if (pos >= 0) {
				z1 = z2 = pos;
				if (invertZ) {
					z1 = z2 = 255 - pos;
				}
				jRenderer3D.addText3D(new Text3D(s, x1 - off, y1 - off, z1, textColor, fontSize, 4));
				jRenderer3D.addText3D(new Text3D(s, x2 + off, y2 + off, z2, textColor, fontSize));
				jRenderer3D.addText3D(new Text3D(s, x1 - off, y2 + off, z1, textColor, fontSize));
				jRenderer3D.addText3D(new Text3D(s, x2 + off, y1 - off, z2, textColor, fontSize));

				jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x1, y2, z2, lineColor, true));
				jRenderer3D.addLine3D(new Line3D(x2, y1, z1, x2, y2, z2, lineColor));

				jRenderer3D.addLine3D(new Line3D(x1, y1, z1, x2, y1, z2, lineColor, true));
				jRenderer3D.addLine3D(new Line3D(x1, y2, z1, x2, y2, z2, lineColor));
			}
		}

		// add coordinate system
		jRenderer3D.add3DCube(0, 0, 0, imageWidth, imageHeight, id, Color.white);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  range        Description of the Parameter
	 *@param  targetSteps  Description of the Parameter
	 *@return              Description of the Return Value
	 */
	double calcStepSize(double range, double targetSteps) {
	// Calculate an initial guess at step size
	double tempStep = range / targetSteps;

	// Get the magnitude of the step size
	double mag = Math.floor(Math.log(tempStep) / Math.log(10.));
	double magPow = Math.pow((double) 10.0, mag);

	// Calculate most significant digit of the new step size
	double magMsd = ((int) (tempStep / magPow + .5));

		// promote the MSD to either 1, 2, 4, or 5
		if (magMsd > 6) {// 5
			magMsd = 10.0;
		} else if (magMsd > 3) {
			magMsd = 5.0;
		} else if (magMsd > 2) {
			magMsd = 4.0;
		} else if (magMsd > 1) {
			magMsd = 2.0;
		}

		return magMsd * magPow;
	}


	/**
	 *  Renders and updates the 3D image. Image region is repainted.
	 */
	private void renderAndUpdateDisplay() {
		jRenderer3D.doRendering();
		imageRegion.setImage(jRenderer3D);
		imageRegion.repaint();
	}



	/**
	 *  * ACTION LISTENERS * *
	 *
	 *@param  arg0  Description of the Parameter
	 */

	public void mouseClicked(MouseEvent arg0) {
	Object source = arg0.getSource();
		if (source == imageRegion) {// top view

			if (arg0.getClickCount() == 2) {
				jRenderer3D.setTransformRotationXYZ(0, 0, 0);
				renderAndUpdateDisplay();
			} else if (arg0.getClickCount() >= 3) {
				jRenderer3D.setTransformRotationXYZ(90, 0, 0);
				renderAndUpdateDisplay();
			}
		}
	}



	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mouseMoved(MouseEvent arg0) { }



	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mousePressed(MouseEvent arg0) {
	Object source = arg0.getSource();

		if (source == imageRegion) {
			xStart = arg0.getX();
			yStart = arg0.getY();
			xdiff = 0;
			ydiff = 0;
			drag = true;
			renderAndUpdateDisplay();
		}
	}



	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mouseReleased(MouseEvent arg0) {
		//Object source = arg0.getSource();
		drag = false;
		setPlotType(plotType);
		setColorType(colorType);
		jRenderer3D.setSurfacePlotLight(light);
		renderAndUpdateDisplay();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mouseDragged(MouseEvent arg0) {
	Object source = arg0.getSource();
		if (source == imageRegion) {
			if (drag == true) {
				jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_DOTSNOLIGHT);

			int xAct = arg0.getX();
			int yAct = arg0.getY();
				xdiff = xAct - xStart;
				ydiff = yAct - yStart;
				xStart = xAct;
				yStart = yAct;

				jRenderer3D.changeTransformRotationXZ(-ydiff / 2., xdiff / 2.);

				renderAndUpdateDisplay();
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mouseEntered(MouseEvent arg0) { }


	/**
	 *  Description of the Method
	 *
	 *@param  arg0  Description of the Parameter
	 */
	public void mouseExited(MouseEvent arg0) { }



	/**
	 *  Sets the surface plot mode. Renders and updates the image.
	 *
	 *@param  type  The new plotType value
	 */
	private void setPlotType(String type) {
		if (type.equals(DOTS)) {
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_DOTS);
		} else if (type.equals(LINES)) {
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_LINES);
		} else if (type.equals(MESH)) {
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_MESH);
		} else if (type.equals(ISOLINES)) {
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_ISOLINES);
		} else if (type.equals(FILLED)) {
			jRenderer3D.setSurfacePlotMode(JRenderer3D.SURFACEPLOT_FILLED);
		}

		renderAndUpdateDisplay();
	}



	/**
	 *  Sets the surface color type. Renders and updates the image.
	 *
	 *@param  type  The new colorType value
	 */
	private void setColorType(String type) {
		colorType = type;
		if (type.equals(ORIGINAL)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_ORIGINAL);
		} else if (type.equals(GRAYSCALE)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_GRAY);
		} else if (type.equals(SPECTRUM)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_SPECTRUM);
		} else if (type.equals(FIRE)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_FIRE);
		} else if (type.equals(THERMAL)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_THERMAL);
		} else if (type.equals(GRADIENT)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_GRADIENT);//		else if (type.equals(GRADIENT2))
//			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_GRADIENT2);
		} else if (type.equals(ORANGE)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_ORANGE);
		} else if (type.equals(BLUE)) {
			jRenderer3D.setSurfacePlotLut(JRenderer3D.LUT_BLUE);
		}

		renderAndUpdateDisplay();
	}



	/**
	 *  Updates illumination, smoothing and scaling. Renders and updates the image.
	 *
	 *@param  slider  Description of the Parameter
	 */
	private void sliderChange(JSlider slider) {
		if (slider == sliderLight) {
			light = sliderLight.getValue() / 100.;

			jRenderer3D.setSurfacePlotLight(light);
		String str = "Lighting: " + light;
			setSliderTitle(sliderLight, Color.black, str);

		} else if (slider == sliderGridSize) {
		int grid = 1 << sliderGridSize.getValue();

		int gridHeight;

		int gridWidth;
			if (imageHeight > imageWidth) {
				gridHeight = grid;
				gridWidth = grid * imageWidth / imageHeight;
			} else {
				gridWidth = grid;
				gridHeight = grid * imageHeight / imageWidth;
			}

			jRenderer3D.setSurfacePlotGridSize(gridWidth, gridHeight);
			jRenderer3D.setInverse(invertZ);

		double smooth = sliderSmoothing.getValue() * (grid / 512.);
			if (!slider.getValueIsAdjusting() || grid <= 256) {
				jRenderer3D.setSurfaceSmoothingFactor(smooth);
				smoothOld = smooth;
			}
		String str = "Grid Size: " + grid;
			setSliderTitle(sliderGridSize, Color.black, str);

			str = "Smoothing: " + (int) (smooth * 100) / 100.;
			setSliderTitle(sliderSmoothing, Color.black, str);
		} else if (slider == sliderSmoothing) {
		int grid = 1 << sliderGridSize.getValue();
		double smooth = sliderSmoothing.getValue() * (grid / 512.);

			if (smooth != smoothOld && (!slider.getValueIsAdjusting() || (1 << sliderGridSize.getValue()) <= 256)) {
				jRenderer3D.setSurfaceSmoothingFactor(smooth);
				smoothOld = smooth;
			}
		String str = "Smoothing: " + (int) (smooth * 100) / 100.;
			setSliderTitle(sliderSmoothing, Color.black, str);
		} else if (slider == sliderScale) {
		double scaleSlider = sliderScale.getValue() / 100.;
		String str = "Scale: " + (int) (scaleSlider * 100) / 100.;
			setSliderTitle(sliderScale, Color.black, str);
		double scale = scaleInit * scaleWindow * scaleSlider;
			jRenderer3D.setTransformScale(scale);
		} else if (slider == sliderPerspective) {
		double p = sliderPerspective.getValue() / 100.;
			jRenderer3D.setTransformPerspective(p);
		String str = "Perspective: " + p;
			setSliderTitle(sliderPerspective, Color.black, str);
		} else if (slider == sliderMin) {
			maxS = sliderMax.getValue();
			minS = sliderMin.getValue();

			if (minS >= maxS) {
				maxS = Math.min(255, minS + 1);
				sliderMax.setValue(maxS);
				sliderMax.repaint();
			}
		String str = "Min: " + minS + " %";
			setSliderTitle(sliderMin, Color.black, str);
			str = "Max: " + maxS + " %";
			setSliderTitle(sliderMax, Color.black, str);

			jRenderer3D.setSurfacePlotMinMax(minS, maxS);

			addCoordinateSystem();

		} else if (slider == sliderMax) {
			maxS = sliderMax.getValue();
			minS = sliderMin.getValue();

			if (maxS <= minS) {
				minS = Math.max(-1, maxS - 1);
				sliderMin.setValue(minS);
				sliderMin.repaint();
			}
		String str = "Min: " + minS + " %";
			setSliderTitle(sliderMin, Color.black, str);
			str = "Max: " + maxS + " %";
			setSliderTitle(sliderMax, Color.black, str);

			jRenderer3D.setSurfacePlotMinMax(minS, maxS);

			addCoordinateSystem();
		} else if (slider == sliderZRatio) {
			zAspectRatioSlider = sliderZRatio.getValue() / 100.;
		String str = "z-Ratio:" + zAspectRatioSlider;
			setSliderTitle(sliderZRatio, Color.black, str);
			zAspectRatio = zAspectRatioSlider * zRatioInit;
			jRenderer3D.setTransformZAspectRatio(zAspectRatio);
			maxDistance = Math.max(scaledWidth, Math.max(scaledHeight, 256 * Math.max(zAspectRatio, 1)));

			jRenderer3D.setTransformMaxDistance(maxDistance);

			addCoordinateSystem();
		}

		renderAndUpdateDisplay();
	}



	/**
	 *  Rezises the buffer size of the image. Renders and updates image.
	 *
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 */
	public void resizeImagePanel(int width, int height) {
		if (jRenderer3D != null) {
			scaleWindow = Math.min(width, height) / (double) startWindowHeight;

			jRenderer3D.setBufferSize(width, height);

			setScaleAndZRatio();

			renderAndUpdateDisplay();
		}
	}



	/**
	 *  * GUI-METHODS * *
	 */

	private void createGUI() {
		// add window listener
		frame.addWindowListener(
			new WindowAdapter() {
				public void windowClosing(WindowEvent event) {
					frame.dispose();
				}
			});

		// create window/frame content
		mainPanel = createMainPanel();

		// add content to window/frame
		frame.getContentPane().add(mainPanel);

		//	set size and visibility of frame
		frame.setSize(startWindowWidth, startWindowHeight);
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);

		// add component/resize listener
		frame.addComponentListener(
			new ComponentAdapter() {

				public void componentResized(ComponentEvent event) {
				Insets insetsFrame = frame.getInsets();
					windowWidth = frame.getWidth() - insetsFrame.left - insetsFrame.right - settingsPanel2.getWidth();
					windowHeight = frame.getHeight() - insetsFrame.bottom - insetsFrame.top - settingsPanel1.getHeight();
					if (windowHeight > 0 && windowWidth > 0) {
						resizeImagePanel(windowWidth, windowHeight);
					}
					frame.pack();
				}
			});

	// get screen dimensions
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	Dimension screenSize = toolkit.getScreenSize();
	int screenWidth = screenSize.width;
	int screenHeight = screenSize.height;

	// center frame on screen
	int centerX = screenWidth / 2 - startWindowWidth / 2;
	int centerY = screenHeight / 2 - startWindowHeight / 2;
		frame.setLocation(centerX, centerY);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createMainPanel() {
	// create image region panel
	JPanel imagePanel = createImagePanel();

		// create settings panel top
		settingsPanel1 = createSettingsPanelTop();

		// create settings panel right
		settingsPanel2 = createSettingsPanelRight();

	// create main panel
	JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// add panels to main panel
		mainPanel.add(settingsPanel1, BorderLayout.NORTH);
		mainPanel.add(settingsPanel2, BorderLayout.EAST);
		mainPanel.add(imagePanel, BorderLayout.CENTER);

		return mainPanel;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createImagePanel() {
		// create image region
		imageRegion = new ImageRegion();

		// init size
		imageRegion.setWidth(startWindowWidth);
		imageRegion.setHeight(startWindowHeight);

		imageRegion.addMouseListener(this);
		imageRegion.addMouseMotionListener(this);

	// create image region panel/canvas
	JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// add image region to panel
		panel.add(imageRegion, BorderLayout.CENTER);

		// return image panel
		return panel;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createSettingsPanelTop() {
	Dimension comboDim = new Dimension(400, 25);
	Dimension sliderDim1 = new Dimension(400, 50);

	// create combo panel
	JPanel comboPanel = createComboPanel();
		comboPanel.setPreferredSize(comboDim);

	// create slider panels
	JPanel sliderPanel1 = createSliderPanel1();
		sliderPanel1.setPreferredSize(sliderDim1);

		// create settings panel
		settingsPanel1 = new JPanel();
		settingsPanel1.setLayout(new BorderLayout());

		// add combo/slider panels
		settingsPanel1.add(comboPanel, BorderLayout.NORTH);
		settingsPanel1.add(sliderPanel1, BorderLayout.SOUTH);

		// return settings panel
		return settingsPanel1;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createSettingsPanelRight() {
	Dimension sliderDim2 = new Dimension(70, 400);

	// create slider panel
	JPanel sliderPanel2 = createSliderPanel2();
		sliderPanel2.setPreferredSize(sliderDim2);

		// create settings panel
		settingsPanel2 = new JPanel();
		settingsPanel2.setLayout(new BorderLayout());

		// add combo/slider panels
		settingsPanel2.add(sliderPanel2, BorderLayout.CENTER);

		// return settings panel
		return settingsPanel2;
	}


	/**
	 *  Description of the Method
	 */
	void createComboDisplay() {
//		 create display colors combo box
		comboDisplayColors = new JComboBox();

		comboDisplayColors.addItem(ORIGINAL);
		comboDisplayColors.addItem(GRAYSCALE);
		comboDisplayColors.addItem(SPECTRUM);
		comboDisplayColors.addItem(FIRE);
		comboDisplayColors.addItem(THERMAL);
		comboDisplayColors.addItem(GRADIENT);
//		comboDisplayColors.addItem(GRADIENT2);
		comboDisplayColors.addItem(BLUE);
		comboDisplayColors.addItem(ORANGE);
		comboDisplayColors.setSelectedIndex(isExamplePlot ? 5 : 0);

		comboDisplayColors.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					setColorType(comboDisplayColors.getSelectedItem().toString());
				}
			});
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createComboPanel() {

		// create display type combo box
		comboDisplayType = new JComboBox();

		comboDisplayType.addItem(DOTS);
		comboDisplayType.addItem(LINES);
		comboDisplayType.addItem(MESH);
		comboDisplayType.addItem(FILLED);
		comboDisplayType.addItem(ISOLINES);
		comboDisplayType.setSelectedIndex(isExamplePlot ? 3 : 1);

		comboDisplayType.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					plotType = comboDisplayType.getSelectedItem().toString();
					setPlotType(plotType);
				}
			});

		createComboDisplay();

	// create save button
	JButton saveButton = new JButton("Save Plot");
		saveButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					imageRegion.saveToImageJImage();
				}
			});

	// create save button
	JButton textureButton = new JButton("Load Texture");
		textureButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					loadTextureImage();
				}
			});

	// create combo panel
	JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 5, 0, 0));

		// add elements to combo panel
		panel.add(comboDisplayType);
		panel.add(comboDisplayColors);
		panel.add(textureButton);
		panel.add(saveButton);

		checkAuto = new JCheckBox("z-Ratio = xy-Ratio");
		checkAuto.setFont(new Font("Sans", Font.PLAIN, 11));
		checkAuto.setSelected(isEqualxyzRatio);
		checkAuto.addItemListener(this);
		panel.add(checkAuto);

		// return combo panel
		return panel;
	}


	/**
	 *  Description of the Method
	 */
	private void loadTextureImage() {

	ImagePlus impTexture = null;

	int[] wList = WindowManager.getIDList();
	boolean loadFromDisk = false;
		if (wList == null) {
			loadFromDisk = true;
		} else {
		String[] titles = new String[wList.length + 1];
			for (int i = 0; i < wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
				if (imp != null) {
					titles[i] = imp.getTitle();
				} else {
					titles[i] = "";
				}
			}
			titles[wList.length] = "\"Load File from Disk\"";

		GenericDialog gd = new GenericDialog("Load texture", IJ.getInstance());

			gd.addMessage("Please select an Image to be used as texture");

		String defaultItem = titles[0];
			gd.addChoice("Open Image:", titles, defaultItem);

			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
		int index = gd.getNextChoiceIndex();
			if (titles[index].equals("\"Load File from Disk\"")) {
				loadFromDisk = true;
			} else {
				impTexture = WindowManager.getImage(wList[index]);
			}
		}

		if (loadFromDisk == true) {
		JFileChooser fc = new JFileChooser();// open texture image

			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			String str = fc.getSelectedFile().getPath();
				try {
					IJ.run("Open...", "path='" + str + "'");
					impTexture = WindowManager.getCurrentImage();
				} catch (RuntimeException e) {
					JOptionPane.showMessageDialog(null, "Error opening Image", "", JOptionPane.PLAIN_MESSAGE);
					return;
				}
			}
		}

		if (impTexture != null) {
			//jRenderer3D.setSurfacePlotWithTexture(image, impTexture);
			jRenderer3D.setSurfacePlotTexture(impTexture);

			setPlotType(plotType);

			jRenderer3D.setSurfacePlotLight(light);

			setColorType(colorType);

		int max = sliderMax.getValue();
		int min = sliderMin.getValue();

			jRenderer3D.setSurfacePlotMinMax(min, max);
			jRenderer3D.setInverse(invertZ);

		int grid = 1 << sliderGridSize.getValue();
		double smooth = sliderSmoothing.getValue() * (grid / 512.);
			if (smooth < 1) {
				smooth = 0;
			}
			jRenderer3D.setSurfaceSmoothingFactor(smooth);
			smoothOld = smooth;

			setColorType(ORIGINAL);
			comboDisplayColors.setSelectedIndex(0);

			renderAndUpdateDisplay();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createSliderPanel1() {
		// create sliders
		sliderGridSize = createSliderHorizontal("Grid Size: 128", 5, 9, 7);// 32, 64, 128, 256, 512
	int grid = 128;

	int smooth = (isExamplePlot) ? 5 : 3;
	String str = "Smoothing: " + (int) ((smooth) * 100) / 100.;
		smooth *= (512 / grid);
		sliderSmoothing = createSliderHorizontal(str, 0, 100, smooth);

	int perspective = 0;
		str = "Perspective: " + perspective / 100.;
		sliderPerspective = createSliderHorizontal(str, 0, 100, perspective);

	int light_ = (int) (light * 100);
		sliderLight = createSliderHorizontal("Lighting: " + light_ / 100., 0, 100, light_);

	JPanel miniPanel = new JPanel();
		miniPanel.setLayout(new GridLayout(2, 2, 0, 3));

		checkAxes = new JCheckBox("Axes");
		checkAxes.setFont(new Font("Sans", Font.PLAIN, 11));
		checkAxes.setSelected(true);
		checkAxes.addItemListener(this);

		checkInverse = new JCheckBox("Invert");
		checkInverse.setFont(new Font("Sans", Font.PLAIN, 11));
		checkInverse.setSelected(false);
		checkInverse.addItemListener(this);

		checkText = new JCheckBox("Text");
		checkText.setFont(new Font("Sans", Font.PLAIN, 11));
		checkText.setSelected(true);
		checkText.addItemListener(this);

		checkLines = new JCheckBox("Lines");
		checkLines.setFont(new Font("Sans", Font.PLAIN, 11));
		checkLines.setSelected(true);
		checkLines.addItemListener(this);

		miniPanel.add(checkInverse);
		miniPanel.add(checkAxes);
		miniPanel.add(checkText);
		miniPanel.add(checkLines);

	// create slider panel
	JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 5));

		// add elements to combo panel
		panel.add(sliderGridSize);
		panel.add(sliderSmoothing);
		panel.add(sliderPerspective);
		panel.add(sliderLight);
		panel.add(miniPanel);

		return panel;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public synchronized void itemStateChanged(ItemEvent e) {
		//Object source = e.getItemSelectable();

		if (e.getSource() == checkInverse) {
			if (checkInverse.isSelected()) {
				invertZ = true;
			} else {
				invertZ = false;
			}
			jRenderer3D.setInverse(invertZ);
			maxS = sliderMax.getValue();
			minS = sliderMin.getValue();
			jRenderer3D.setSurfacePlotMinMax(minS, maxS);

			addCoordinateSystem();
		}
		if (e.getSource() == checkAxes) {
			if (checkAxes.isSelected()) {
				jRenderer3D.setAxes(true);
			} else {
				jRenderer3D.setAxes(false);
			}
		}
		if (e.getSource() == checkText) {
			if (checkText.isSelected()) {
				jRenderer3D.setText(true);
			} else {
				jRenderer3D.setText(false);
			}
		}
		if (e.getSource() == checkLines) {
			if (checkLines.isSelected()) {
				jRenderer3D.setLines(true);
			} else {
				jRenderer3D.setLines(false);
			}
		}
		if (e.getSource() == checkAuto) {
			if (checkAuto.isSelected()) {
				isEqualxyzRatio = true;
			} else {
				isEqualxyzRatio = false;
			}

			setScaleAndZRatio();
			renderAndUpdateDisplay();
		}

		renderAndUpdateDisplay();
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	private JPanel createSliderPanel2() {

		// create sliders
		sliderScale = createSliderVertical("Scale: 1.0", 25, 300, 100);
		sliderZRatio = createSliderVertical("z-Ratio: 1.0", 10, 400, 100);
		sliderMin = createSliderVertical("Min:  0 %", 0, 100, 0);
		sliderMax = createSliderVertical("Max: 100 %", 0, 100, 100);

	// create slider panel
	JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(4, 1));

		panel.add(sliderScale);
		panel.add(sliderZRatio);
		panel.add(sliderMax);
		panel.add(sliderMin);
		return panel;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  borderTitle  Description of the Parameter
	 *@param  min          Description of the Parameter
	 *@param  max          Description of the Parameter
	 *@param  value        Description of the Parameter
	 *@return              Description of the Return Value
	 */
	private JSlider createSliderHorizontal(String borderTitle, int min, int max, int value) {

	// create nested border
	Border empty = BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder());

	// create font for sliders
	Font sliderFont = new Font("Sans", Font.PLAIN, 11);

	// create slider
	JSlider slider = new JSlider(JSlider.HORIZONTAL, min, max, value);
		slider.setBorder(new TitledBorder(
				empty, borderTitle, TitledBorder.CENTER,
				TitledBorder.BELOW_TOP, sliderFont));

		slider.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent event) {
					sliderChange((JSlider) event.getSource());
				}
			});

		return slider;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  borderTitle  Description of the Parameter
	 *@param  min          Description of the Parameter
	 *@param  max          Description of the Parameter
	 *@param  value        Description of the Parameter
	 *@return              Description of the Return Value
	 */
	private JSlider createSliderVertical(String borderTitle, int min, int max, int value) {

	// create nested border
	Border empty = BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder());

	// create font for sliders
	Font sliderFont = new Font("Sans", Font.PLAIN, 11);

	// create slider
	JSlider slider = new JSlider(JSlider.VERTICAL, min, max, value);
		slider.setBorder(new TitledBorder(
				empty, borderTitle, TitledBorder.CENTER,
				TitledBorder.BELOW_TOP, sliderFont));

		slider.addChangeListener(
			new ChangeListener() {
				public void stateChanged(ChangeEvent event) {
					sliderChange((JSlider) event.getSource());
				}
			});

		return slider;
	}


	/**
	 *  Sets the sliderTitle attribute of the Interactive_3D_Surface_Plot object
	 *
	 *@param  slider  The new sliderTitle value
	 *@param  color   The new sliderTitle value
	 *@param  str     The new sliderTitle value
	 */
	private void setSliderTitle(JSlider slider, Color color, String str) {
	Border empty = BorderFactory.createTitledBorder(
				BorderFactory.createEmptyBorder());

	Font sliderFont = new Font("Sans", Font.PLAIN, 11);

		slider.setBorder(new TitledBorder(
				empty, str, TitledBorder.CENTER,
				TitledBorder.BELOW_TOP, sliderFont));

		//TitledBorder tb = new TitledBorder(empty,
		//		"", TitledBorder.CENTER, TitledBorder.TOP,
		//		new Font("Sans", Font.PLAIN, 12));
		//tb.setTitleJustification(TitledBorder.LEFT);
		//tb.setTitle(str);
		//tb.setTitleColor(color);
		//slider.setBorder(tb);
	}


	/**
	 *  Image Region
	 *
	 *@author     Thomas
	 *@created    27 novembre 2007
	 */
	class ImageRegion extends JPanel {

		private Image image;
		private int width;
		private int height;


		/**
		 *  Gets the preferredSize attribute of the ImageRegion object
		 *
		 *@return    The preferredSize value
		 */
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}


		/**
		 *  Gets the minimumSize attribute of the ImageRegion object
		 *
		 *@return    The minimumSize value
		 */
		public Dimension getMinimumSize() {
			return new Dimension(width, height);
		}


		/**
		 *  Sets the image attribute of the ImageRegion object
		 *
		 *@param  pic  The new image value
		 */
		public void setImage(JRenderer3D pic) {
			height = pic.getHeight();
			width = pic.getWidth();
			image = pic.getImage();
		}


		/**
		 *  Sets the image attribute of the ImageRegion object
		 *
		 *@param  image  The new image value
		 */
		public void setImage(Image image) {
			this.image = image;
		}


		/**
		 *  Description of the Method
		 *
		 *@param  g  Description of the Parameter
		 */
		public void paint(Graphics g) {

			if (image != null) {
				g.drawImage(image, 0, 0, width, height, (ImageObserver) this);
			}
		}


		/**
		 *  Description of the Method
		 */
		synchronized void saveToImageJImage() {

		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

			paint(bufferedImage.createGraphics());

		ImagePlus plotImage = NewImage.createRGBImage("ImageJ 3D", width, height, 1, NewImage.FILL_BLACK);

		ImageProcessor ip = plotImage.getProcessor();

		int[] pixels = (int[]) ip.getPixels();
			bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

			plotImage.show();
			plotImage.updateAndDraw();
		}

		//-------------------------------------------------------------------

		/**
		 *  Description of the Method
		 *
		 *@param  g  Description of the Parameter
		 */
		public void update(Graphics g) {
			paint(g);
		}


		/**
		 *  Gets the height attribute of the ImageRegion object
		 *
		 *@return    The height value
		 */
		public int getHeight() {
			return height;
		}


		/**
		 *  Sets the height attribute of the ImageRegion object
		 *
		 *@param  height  The new height value
		 */
		public void setHeight(int height) {
			this.height = height;
		}


		/**
		 *  Gets the width attribute of the ImageRegion object
		 *
		 *@return    The width value
		 */
		public int getWidth() {
			return width;
		}


		/**
		 *  Sets the width attribute of the ImageRegion object
		 *
		 *@param  width  The new width value
		 */
		public void setWidth(int width) {
			this.width = width;
		}
	}

}

