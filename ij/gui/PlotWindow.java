package ij.gui;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import java.util.*;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.util.*;
import ij.text.TextWindow;
import ij.plugin.filter.Analyzer;
import ij.plugin.RadioSpectrum_Reader;

/**
 * This class is an extended ImageWindow that displays line graphs.
 */
public class PlotWindow extends ImageWindow implements ActionListener, ClipboardOwner {

    /**
     * Display points using a circle 5 pixels in diameter.
     */
    public static final int CIRCLE = 0;
    private boolean drawPending;
    /**
     * Display points using an X-shaped mark.
     */
    public static final int X = 1;
    /**
     * Connect points with solid lines.
     */
    public static final int LINE = 2;
    private static final int LEFT_MARGIN = 50;
    private static final int RIGHT_MARGIN = 20;
    private static final int TOP_MARGIN = 20;
    private static final int BOTTOM_MARGIN = 30;
    private static final int WIDTH = 450;
    private static final int HEIGHT = 200;
    private static final String MIN = "pp.min";
    private static final String MAX = "pp.max";
    private static final String PLOT_WIDTH = "pp.width";
    private static final String PLOT_HEIGHT = "pp.height";
    private static final String OPTIONS = "pp.options";
    private static final int SAVE_X_VALUES = 1;
    private static final int AUTO_CLOSE = 2;
    public static boolean interpolate;
    static boolean noGridLines;
    private int frameWidth;
    private int frameHeight;
    private int xloc;
    private int yloc;
    private int flags;
    private Rectangle frame = null;
    private float[] xValues, yValues, xInitValues, yInitValues, yValuesBaseLine, yValuesMergeGaussFit;
    private double[] gaussFitResult;
    private int nbgauss;
    private float[] errorBars;
    private int nPoints;
    private double xScale, yScale;
    private double xMin, xMax, xInitMin, xInitMax, yMin, yMax;
    private Button list, save, copy, setting, setScale, option, setScaleRadio, baseline, subtract, gauss;
    private Label Xcoord, Ycoord;
    private static String defaultDirectory = null;
    private String xLabel;
    private String yLabel;
    private Font font = new Font("Helvetica", Font.PLAIN, 12);
    private boolean fixedYScale;
    private ImageProcessor ip;
    private static int options;
    private int lineWidth = Line.getWidth();
    private int defaultDigits = -1;
    private boolean realNumbers;
    private int xdigits, ydigits;
    private boolean modifListener = false;
    private int oldX = -1, oldY = -1, oldindex = -1;
    private int Xpoints[], Ypoints[];
    /**
     * Save x-values only. To set, use Edit/Options/ Profile Plot Options.
     */
    public static boolean saveXValues;
    /**
     * Automatically close window after saving values. To set, use
     * Edit/Options/Profile Plot Options.
     */
    public static boolean autoClose;
    /**
     * The width of the plot in pixels.
     */
    public static int plotWidth = WIDTH;
    /**
     * The height of the plot in pixels.
     */
    public static int plotHeight = HEIGHT;
    private Plot plot;
    private ImagePlus origin = null;
    private Roi region;
    /**
     * Begin the plot profile with the number pixel selected and not zero. the
     * selection should be horizontal
     */
    private static boolean horizontal;//EU_HOU_rb
    /**
     * Specific menu for the spectrum analysis
     */
    public static boolean RadioSpectra = false;//EU_HOU_jl
    public static boolean Base_Line = false;
    public static boolean Base_Line_subtracted = false;
    public static boolean ZERO_LINE = false;
    public final static int X_NUMBERS = 0x1;
    /**
     * flag for numeric labels of x-axis ticks
     */
    public final static int Y_NUMBERS = 0x2;
    /**
     * flag for drawing x-axis ticks
     */
    public final static int X_TICKS = 0x4;
    /**
     * flag for drawing x-axis ticks
     */
    public final static int Y_TICKS = 0x8;
    /**
     * flag for drawing vertical grid lines for x-axis ticks
     */
    public final static int X_GRID = 0x10;
    /**
     * flag for drawing horizontal grid lines for y-axis ticks
     */
    public final static int Y_GRID = 0x20;
    /**
     * flag for forcing frame to coincide with the grid/ticks in x direction
     * (results in unused space)
     */
    public final static int X_FORCE2GRID = 0x40;
    /**
     * flag for forcing frame to coincide with the grid/ticks in y direction
     * (results in unused space)
     */
    public final static int Y_FORCE2GRID = 0x80;
    /**
     * the default flags
     */
    public final static int DEFAULT_FLAGS = X_NUMBERS + Y_NUMBERS
            + /*
             * X_TICKS + Y_TICKS +
             */ X_GRID + Y_GRID;
    public final static int MAX_INTERVALS = 12;//maximum number of intervals between ticks or grid lines
    public final static int MIN_X_GRIDWIDTH = 60;//minimum distance between grid lines or ticks along x
    public final static int MIN_Y_GRIDWIDTH = 40;//minimum distance between grid lines or ticks along y
    public final static int TICK_LENGTH = 3;
    public final Color gridColor = new Color(0xc0c0c0);
    private Vector labels = new Vector(), xlabs = new Vector(), ylabs = new Vector();
    // static initializer

    static {
        IJ.register(PlotWindow.class); //keeps options from being reset on some JVMs
        options = Prefs.getInt(OPTIONS, SAVE_X_VALUES);
        saveXValues = (options & SAVE_X_VALUES) != 0;
        autoClose = (options & AUTO_CLOSE) != 0;
        plotWidth = Prefs.getInt(PLOT_WIDTH, WIDTH);
        plotHeight = Prefs.getInt(PLOT_HEIGHT, HEIGHT);
    }

    /**
     * Construct a new PlotWindow.
     *
     * @param title	the window title
     * @param xLabel	the x-axis label
     * @param yLabel	the y-axis label
     * @param xValues	the x-coodinates
     * @param yValues	the y-coodinates
     */
    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, float[] xInitValues, float[] yInitValues) {
        super(NewImage.createByteImage(title, plotWidth + LEFT_MARGIN + RIGHT_MARGIN, plotHeight + TOP_MARGIN + BOTTOM_MARGIN, 1, NewImage.FILL_WHITE), false);
        IJ.showProgress(.1);
        this.xLabel = xLabel;
        this.yLabel = yLabel;
        this.xValues = xValues;
        this.yValues = yValues;
        this.xInitValues = xInitValues;
        this.yInitValues = yInitValues;
        double[] a = Tools.getMinMax(xValues);
        xMin = a[0];
        xMax = a[1];
        a = Tools.getMinMax(xInitValues);
        xInitMin = a[0];
        xInitMax = a[1];
        a = Tools.getMinMax(yValues);
        yMin = a[0];
        yMax = a[1];
        fixedYScale = false;
        nPoints = xValues.length;
        nbgauss = -1;
        gaussFitResult = new double[18];
        Panel buttons = new Panel();
        buttons.setLayout(new GridLayout(3, 3));
        list = new Button(IJ.getBundle().getString("List")/*
                 * " List "
                 */);
        list.addActionListener(this);
        buttons.add(list);
        save = new Button(IJ.getBundle().getString("Save")/*
                 * "Save..."
                 */);
        save.addActionListener(this);
        buttons.add(save);
        copy = new Button(IJ.getBundle().getString("Copy")/*
                 * "Copy..."
                 */);
        copy.addActionListener(this);
        buttons.add(copy);
        setting = new Button(IJ.getBundle().getString("Prop"));
        setting.addActionListener(this);
        buttons.add(setting);
        if (RadioSpectra) {
            String s;
            if (Base_Line_subtracted || Base_Line) {
                s = IJ.getPluginBundle().getString("Reset");
            } else {
                s = IJ.getBundle().getString("Set_Scale");
            }
            setScaleRadio = new Button(s);
            setScaleRadio.addActionListener(this);
            buttons.add(setScaleRadio);
            if (Base_Line) {
                subtract = new Button(IJ.getPluginBundle().getString("subtractRS"));
                subtract.addActionListener(this);
                buttons.add(subtract);
            } else {
                if ((Base_Line_subtracted)) {
                    gauss = new Button(IJ.getPluginBundle().getString("GaussianFit"));
                    gauss.addActionListener(this);
                    buttons.add(gauss);
                } else {
                    baseline = new Button(IJ.getPluginBundle().getString("baseline"));
                    baseline.addActionListener(this);
                    buttons.add(baseline);
                }
            }
        } else {
            setScale = new Button(IJ.getBundle().getString("Set_Scale"));
            setScale.addActionListener(this);
            buttons.add(setScale);
            //EU_HOU Bundle
            option = new Button(IJ.getPluginBundle().getString("ProOptTitle"));
            option.addActionListener(this);
            buttons.add(option);
        }
        buttons.add(new Label(""));
        Xcoord = new Label("     ");
        Xcoord.setFont(new Font("Monospaced", Font.PLAIN, 12));
        buttons.add(Xcoord);
        Ycoord = new Label("     ");
        Ycoord.setFont(new Font("Monospaced", Font.PLAIN, 12));
        buttons.add(Ycoord);
        add(buttons);
        pack();

        //IJ.showResults();
        //IJ.getTextPanel().setFont(new Font("Monospaced", Font.PLAIN, 12));
        /*
         * String frstLine=IJ.getBundle().getString("GetPosition"); frstLine
         * +="\n\t"; frstLine+=IJ.getBundle().getString("GetPositionLabels");
         * IJ.write(frstLine);
         */
    }

    /**
     * This version of the constructor excepts double arrays.
     */
    public PlotWindow(String title, String xLabel, String yLabel, double[] xValues, double[] yValues) {
        this(title, xLabel, yLabel, Tools.toFloat(xValues), Tools.toFloat(yValues), Tools.toFloat(xValues), Tools.toFloat(yValues));
        this.horizontal = false;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, float[] xInitValues, float[] yInitValues, final ImagePlus origin) {
        this(title, xLabel, yLabel, xValues, yValues, xInitValues, yInitValues);
        this.origin = origin;
        this.horizontal = false;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues) {
        this(title, xLabel, yLabel, xValues, yValues, xValues, yValues);
        this.horizontal = false;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, final ImagePlus origin) {
        this(title, xLabel, yLabel, xValues, yValues);
        this.origin = origin;
        this.horizontal = false;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, float[] yValuesBaseLine, final ImagePlus origin) {
        this(title, xLabel, yLabel, xValues, yValues, origin);
        if (Base_Line_subtracted) {
            this.yValuesMergeGaussFit = yValuesBaseLine;
        } else {
            this.yValuesBaseLine = yValuesBaseLine;
        }
        this.origin = origin;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, double[] gaussFitResult, int nbgauss, final ImagePlus origin) {
        this(title, xLabel, yLabel, xValues, yValues, origin);
        this.gaussFitResult = gaussFitResult;
        this.nbgauss = nbgauss;
        this.origin = origin;
    }

    public PlotWindow(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, final ImagePlus origin) {
        this(title, xLabel, yLabel, Tools.toFloat(xValues), Tools.toFloat(yValues));
        this.horizontal = false;
        this.origin = origin;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, boolean horiz, final ImagePlus origin, final Roi region) {
        this(title, xLabel, yLabel, xValues, yValues, origin);
        this.modifListener = true;
        this.region = (Roi) (region.clone());
        this.horizontal = horiz;
        this.RadioSpectra = false;
        if (horizontal) {
            int xOrigine = ((Line) region).getX();
            for (int i = 0; i < xValues.length; i++) {
                this.xValues[i] += xOrigine;
            }
            double[] a = Tools.getMinMax(xValues);
            this.xMin = a[0];
            this.xMax = a[1];
            a = Tools.getMinMax(xInitValues);
            this.xInitMin = a[0];
            this.xInitMax = a[1];
        }
        this.origin = origin;
    }

    public PlotWindow(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, float[] xInitValues, float[] yInitValues, boolean horiz, final ImagePlus origin, final Roi region) {
        this(title, xLabel, yLabel, xValues, yValues, false, origin, region);
        this.horizontal = horiz;
        this.xInitValues = xInitValues;
        this.yInitValues = yInitValues;
        double[] a = Tools.getMinMax(xInitValues);
        this.xInitMin = a[0];
        this.xInitMax = a[1];
        this.origin = origin;
    }

    public PlotWindow(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, boolean horiz, ImagePlus origin, Roi region) {
        this(title, xLabel, yLabel, Tools.toFloat(xValues), Tools.toFloat(yValues), horiz, origin, region);

        this.xLabel = xLabel;
        this.yLabel = yLabel;
        /*
         * EU_HOU CHANGES
         */
        this.xInitValues = xInitValues;
        this.yInitValues = yInitValues;
        /*
         * EU_HOU END
         */
        this.flags = flags;
        if (xValues == null || yValues == null) {
            xValues = new double[1];
            yValues = new double[1];
            xValues[0] = -1f;
            yValues[0] = -1f;
        }
        this.xValues = Tools.toFloat(xValues);
        this.yValues = Tools.toFloat(yValues);
        double[] a = Tools.getMinMax(xValues);
        xMin = a[0];
        xMax = a[1];
        /*
         * EU_HOU CHANGES
         */
        a = Tools.getMinMax(xInitValues);
        xInitMin = a[0];
        xInitMax = a[1];
        /*
         * EU_HOU END
         */
        a = Tools.getMinMax(yValues);
        yMin = a[0];
        yMax = a[1];
        fixedYScale = false;
        nPoints = xValues.length;
        drawPending = true;

        //EU_HOU
        origin = origin;
        region = region;
        System.out.println("this one is actually being used");
        this.origin = origin;
    }

    PlotWindow(Plot plot, ImagePlus imp) {
        super(imp);
        this.plot = plot;
        draw();
        this.origin = origin;
        System.out.println("Plot Window 12");
    }

    /**
     * Sets the x-axis and y-axis range.
     */
    public void setLimits(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
        fixedYScale = true;
    }

    /**
     * Adds a set of points to the plot.
     *
     * @param x	the x-coodinates
     * @param y	the y-coodinates
     * @param shape	CIRCLE, X or LINE
     * @param color	colour
     */
    public void addPoints(float[] x, float[] y, int shape, Color c) {
        setup();
        switch (shape) {
            case CIRCLE:
            case X:
                for (int i = 0; i < x.length; i++) {
                    int xt = LEFT_MARGIN + (int) ((x[i] - xMin) * xScale);
                    int yt = TOP_MARGIN + frameHeight - (int) ((y[i] - yMin) * yScale);
                    drawShape(shape, xt, yt, 5);
                }
                break;
            case LINE:
                int xts[] = new int[x.length];
                int yts[] = new int[y.length];
                for (int i = 0; i < x.length; i++) {
                    xts[i] = LEFT_MARGIN + (int) ((x[i] - xMin) * xScale);
                    yts[i] = TOP_MARGIN + frameHeight - (int) ((y[i] - yMin) * yScale);
                }
                setColor(c);
                drawPolyline(ip, xts, yts, x.length);
                setColor(Color.black);
                break;
        }
    }

    public void addPoints(float[] x, float[] y, int shape) {
        addPoints(x, y, shape, Color.black);
    }

    /**
     * Adds a set of points to the plot using double arrays. Must be called
     * before the plot is displayed.
     */
    public void addPoints(double[] x, double[] y, int shape) {
        addPoints(Tools.toFloat(x), Tools.toFloat(y), shape);
    }

    void drawShape(int shape, int x, int y, int size) {
        int xbase = x - size / 2;
        int ybase = y - size / 2;
        if (shape == X) {
            ip.drawLine(xbase, ybase, xbase + size, ybase + size);
            ip.drawLine(xbase + size, ybase, xbase, ybase + size);
        } else { // 5x5 oval
            ip.drawLine(x - 1, y - 2, x + 1, y - 2);
            ip.drawLine(x - 1, y + 2, x + 1, y + 2);
            ip.drawLine(x + 2, y + 1, x + 2, y - 1);
            ip.drawLine(x - 2, y + 1, x - 2, y - 1);
        }
    }

    /**
     * Adds error bars to the plot.
     */
    public void addErrorBars(float[] errorBars) {
        if (errorBars.length != nPoints) {
            throw new IllegalArgumentException("errorBars.length != npoints");
        }
        this.errorBars = errorBars;
    }

    /**
     * Draws a label.
     */
    public void addLabel(double x, double y, String label) {
        setup();
        int xt = LEFT_MARGIN + (int) (x * frameWidth);
        int yt = TOP_MARGIN + (int) (y * frameHeight);
        ip.drawString(label, xt, yt);
        labels.add(label);
        xlabs.add(new Integer(xt));
        ylabs.add(new Integer(yt));
    }

    /**
     * Changes the drawing color. The frame and labels are always drawn in
     * black.
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
     * Changes the line width.
     */
    public void setLineWidth(int lineWidth) {
        setup();
        ip.setLineWidth(lineWidth);
        this.lineWidth = lineWidth;
    }

    /**
     * Changes the font.
     */
    public void changeFont(Font font) {
        setup();
        ip.setFont(font);
        this.font = font;
    }

    /**
     * Displays the plot.
     */
    public void draw() {
        drawPlot();
        if (ip instanceof ColorProcessor) {
            imp.setProcessor(null, ip);
        } else {
            imp.updateAndDraw();
        }
        IJ.showProgress(1.1);
    }

    void setup() {
        if (ip != null) {
            return;
        }
        ip = imp.getProcessor();
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
     * Updates the graph X and Y values when the mouse is clicked. Overrides
     * mouseClicked() in ImageWindow.
     *
     * @see ij.gui.ImageWindow#mouseClicked
     */
    public void mouseClicked(int x, int y) {
        String xRB = "";
        String yRB = "";
        int xOrigine;
        if (region != null) {
            xOrigine = ((Line) region).getX();
        }
        if (frame == null) {
            return;
        }
        if (frame.contains(x, y)) {
            System.out.println("PlotWindow mousemoved 1");
            int index = (int) ((x - frame.x) / ((double) frame.width / xValues.length));
            if (index > 0 && index < xValues.length) {
                double xv = xValues[index];
                double yv = yValues[index];

                // 2 chiffres après la virgule
                //xRB = IJ.d2s(xv, getDigits(xv, 4));
                xRB = IJ.d2s(xv, 4);
                yRB = IJ.d2s(yv, getDigits(yv, 4));
            }
            IJ.log("\t" + xRB + " \t" + yRB);
        }
    }

    /**
     * Updates the graph X and Y values when the mouse is moved. Overrides
     * mouseMoved() in ImageWindow.
     *
     * @see ij.gui.ImageWindow#mouseMoved
     */
    public void mouseMoved(int x, int y) {
        // System.out.println("P");
        if (frame == null) {
            return;
        }


        int xOrigine = 0;

        if ((region != null) && (horizontal)) {
            xOrigine = ((Line) region).getX();
        }

        if (frame.contains(oldX - 1, oldY)) {
            ip.setColor(Color.white);
            Roi region = imp.getRoi();
            imp.setRoi(frame);
            ip.fill();
            imp.setRoi(region);

            /*
             * ip.drawLine(oldX,frame.y ,oldX,frame.y+frame.height);
             * ip.drawLine(frame.x,Ypoints[oldindex],frame.x+frame.width,Ypoints[oldindex]);
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
            if (imp != null) {
                imp.updateAndDraw();
            }
        }
        if (frame.contains(x, y)) {

            int index = (int) ((x - frame.x) / ((double) frame.width / xValues.length));
            ip.setColor(Color.gray.brighter());
            ip.drawLine(x, frame.y, x, frame.y + frameHeight);
            ip.drawLine(frame.x, Ypoints[index], frame.x + frameWidth, Ypoints[index]);
            ip.setColor(Color.black);

            ip.drawRect(frame.x, frame.y, frame.width + 1, frame.height + 1);
            imp.updateAndDraw();
            if (index > 0 && index < xValues.length) {
                double xv;
                xv = xValues[index];
                //if(horizontal) xv-= xOrigine;
                double yv = yValues[index];
                //ip.drawPixel( LEFT_MARGIN + (int)((xv-xMin)*xScale),TOP_MARGIN + frame.height - (int)((yv-yMin)*yScale));
                //String lab = "X=" + IJ.d2s(xv, getDigits(xv, 4));
                String lab = "X=" + IJ.d2s(xv, 4);
                Xcoord.setText(lab);
                lab = "Y=" + IJ.d2s(yv, getDigits(yv, 4));
                Ycoord.setText(lab);

                if ((origin != null) && (region != null) && (region.getType() == Roi.LINE)) {
                    origin.getWindow().getCanvas().setRoi(region);
                    Line lr = (Line) region;
                    if (horizontal) {
                        xv -= xOrigine;
                    }
                    double c = xv / (origin.getRoi().getLength());
                    int xi = lr.x1 + (int) (c * (lr.x2 - lr.x1));
                    int yi = lr.y1 + (int) (c * (lr.y2 - lr.y1));
                    origin.getWindow().getCanvas().setPoint(xi, yi, region);
                }
                oldindex = index;
            }
            //coordinates.setText("X=" + d2s(x/xScale+xMin)+", Y=" + d2s((frameHeight-y)/yScale+yMin));
        } else {
            Xcoord.setText("");
            Ycoord.setText("");
        }
        oldX = x;
        oldY = y;

        if (yValuesBaseLine != null) {
            addPoints(xValues, yValuesBaseLine, LINE, Color.red);
        }
        if (yValuesMergeGaussFit != null) {
            addPoints(xValues, yValuesMergeGaussFit, LINE, Color.red);
        }
        displayGaussianFit();
        if (ZERO_LINE) {
            addPoints(xValues, new float[xValues.length], LINE, Color.blue);
        }
        drawTick(ip);
    }

    void drawPlot() {
        int x, y;
        double v;

        setup();
        if (yValuesBaseLine != null) {
            addPoints(xValues, yValuesBaseLine, LINE, Color.red);
        }
        if (yValuesMergeGaussFit != null) {
            addPoints(xValues, yValuesMergeGaussFit, LINE, Color.red);
        }
        displayGaussianFit();
        if (ZERO_LINE) {
            addPoints(xValues, new float[xValues.length], LINE, Color.blue);
        }

        int xOrigine = 0;
        if ((region != null) && (horizontal)) {
            xOrigine = ((Line) region).getX();//EU_HOU_rb added
        }
        Xpoints = new int[nPoints];
        Ypoints = new int[nPoints];
        double value, xvalue;

        for (int i = 0; i < nPoints; i++) {
            value = yValues[i];
            xvalue = xValues[i];
            if (value < yMin) {
                value = yMin;
            }
            if (value > yMax) {
                value = yMax;
            }
            Xpoints[i] = LEFT_MARGIN + (int) ((xvalue - xMin) * xScale);
            Ypoints[i] = TOP_MARGIN + frame.height - (int) ((value - yMin) * yScale);
        }
        drawPolyline(ip, Xpoints, Ypoints, nPoints);
        IJ.showProgress(.4);
        if (this.errorBars != null) {
            int xpoints[] = new int[2];
            int ypoints[] = new int[2];
            for (int i = 0; i < nPoints; i++) {
                xpoints[0] = xpoints[1] = LEFT_MARGIN + (int) ((xValues[i] - xMin) * xScale);
                ypoints[0] = TOP_MARGIN + frame.height - (int) ((yValues[i] - yMin - errorBars[i]) * yScale);
                ypoints[1] = TOP_MARGIN + frame.height - (int) ((yValues[i] - yMin + errorBars[i]) * yScale);
                drawPolyline(ip, xpoints, ypoints, 2);
            }
        }


        if (ip instanceof ColorProcessor) {
            ip.setColor(Color.black);
        }
        ip.drawRect(frame.x, frame.y, frame.width + 1, frame.height + 1);
        int digits = getDigits(yMax, yMin);
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
        IJ.showProgress(.8);
        FontMetrics fm = ip.getFontMetrics();
        x = LEFT_MARGIN;
        y = TOP_MARGIN + frame.height + fm.getAscent() + 6;
        digits = getDigits(xMin, xMax);

        ip.drawString(IJ.d2s(xMin, digits), x, y);
        s = IJ.d2s(xMax, digits);
        ip.drawString(s, x + frame.width - ip.getStringWidth(s) + 6, y);
        ip.drawString(xLabel, LEFT_MARGIN + (frame.width - ip.getStringWidth(xLabel)) / 2, y + 3);
        drawYLabel(yLabel, LEFT_MARGIN, TOP_MARGIN, frame.height, fm);

        drawTick(ip);
    }

    public void mouseExited(MouseEvent e) {
        origin.getWindow().getCanvas().setPoint(-1, -1, null);
    }

    void drawPolyline(ImageProcessor ip, int[] x, int[] y, int n) {
        ip.moveTo(x[0], y[0]);
        for (int i = 0; i < n; i++) {
            ip.lineTo(x[i], y[i]);
        }
    }

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
        int x2 = x - h - 2;
        //new ImagePlus("after", label).show();
        ip.insert(label, x2, y2);
    }

    void showList() {
        StringBuffer sb = new StringBuffer();
        String headings;
        initDigits();
        if (errorBars != null) {
            if (saveXValues) {
                headings = "X\tY\tErrorBar";
            } else {
                headings = "Y\tErrorBar";
            }
            for (int i = 0; i < nPoints; i++) {
                if (saveXValues) {
                    sb.append(IJ.d2s(xValues[i], xdigits) + "\t" + IJ.d2s(yValues[i], ydigits) + "\t" + IJ.d2s(errorBars[i], ydigits) + "\n");
                } else {
                    sb.append(IJ.d2s(yValues[i], ydigits) + "\t" + IJ.d2s(errorBars[i], ydigits) + "\n");
                }
            }

        } else {
            if (saveXValues) {
                headings = "X\tY";
            } else {
                headings = "Y";
            }
            for (int i = 0; i < nPoints; i++) {
                if (saveXValues) {
                    sb.append(IJ.d2s(xValues[i], xdigits) + "\t" + IJ.d2s(yValues[i], ydigits) + "\n");
                } else {
                    sb.append(IJ.d2s(yValues[i], ydigits) + "\n");
                }
            }
        }
        TextWindow tw = new TextWindow(IJ.getBundle().getString("PlotValues")/*
                 * "Plot Values"
                 */, headings, sb.toString(), 200, 400);
        if (autoClose) {
            imp.changes = false;
            close();
        }
    }

    void drawTick(ImageProcessor ip) {
        double taille = xMax - xMin;
        taille = java.lang.Math.abs(taille);
        double k = 1;
        double kk = 0;
        double dtmp;
        int itmp;
        while (kk != k) {
            kk = k;
            dtmp = taille / k;
            if (dtmp <= 5) {
                k = k / 10;
            }
            if (dtmp >= 100) {
                k = k * 10;
            }
        }
        itmp = (int) java.lang.Math.round(xMin / k);
        double j = itmp * k;
        while (j < xMax) {
            if ((j > xMin) && (j < xMax)) {
                int n = 2;
                dtmp = j / k;
                itmp = (int) dtmp;
                itmp = itmp % 10;
                if (itmp == 0) {
                    n = 6;
                }
                ip.moveTo(LEFT_MARGIN + (int) ((j - xMin) * xScale), TOP_MARGIN + frame.height - n);
                ip.lineTo(LEFT_MARGIN + (int) ((j - xMin) * xScale), TOP_MARGIN + frame.height);
            }
            j += k;
        }
        taille = java.lang.Math.abs(yMax - yMin);
        k = 1;
        kk = 0;
        while (kk != k) {
            kk = k;
            dtmp = taille / k;
            if (dtmp <= 5) {
                k = k / 10;
            }
            if (dtmp >= 100) {
                k = k * 10;
            }
        }
        itmp = (int) java.lang.Math.round(yMin / k);
        j = itmp * k;
        while (j < yMax) {
            if ((j > yMin) && (j < yMax)) {
                int n = 2;
                dtmp = j / k;
                itmp = (int) dtmp;
                itmp = itmp % 10;
                if (itmp == 0) {
                    n = 6;
                }
                ip.moveTo(LEFT_MARGIN, TOP_MARGIN + frame.height - (int) ((j - yMin) * yScale));
                ip.lineTo(LEFT_MARGIN + n, TOP_MARGIN + frame.height - (int) ((j - yMin) * yScale));
            }
            j += k;
        }
    }

    void saveAsText() {
        FileDialog fd = new FileDialog(this, IJ.getBundle().getString("SaveAsText")/*
                 * "Save as Text..."
                 */, FileDialog.SAVE);
        if (defaultDirectory != null) {
            fd.setDirectory(defaultDirectory);
        }
        fd.show();
        String name = fd.getFile();
        String directory = fd.getDirectory();
        defaultDirectory = directory;
        fd.dispose();
        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(directory + name);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);


        } catch (IOException e) {
            IJ.error("" + e);
            return;
        }
        IJ.wait(250);  // give system time to redraw ImageJ window
        IJ.showStatus(IJ.getBundle().getString("SavePlotValues")/*
                 * "Saving plot values..."
                 */);
        initDigits();
        for (int i = 0; i < nPoints; i++) {
            if (saveXValues) {
                pw.println(IJ.d2s(xValues[i], xdigits) + "\t" + IJ.d2s(yValues[i], ydigits));
            } else {
                pw.println(IJ.d2s(yValues[i], ydigits));
            }
        }
        pw.close();
        if (autoClose) {
            imp.changes = false;
            close();
        }


    }

    void copyToClipboard() {
        Clipboard systemClipboard = null;
        try {
            systemClipboard = getToolkit().getSystemClipboard();
        } catch (Exception e) {
            systemClipboard = null;
        }
        if (systemClipboard == null) {
            IJ.error(IJ.getBundle().getString("ClipCopyErr")/*
                     * "Unable to copy to Clipboard."
                     */);
            return;
        }
        IJ.showStatus(IJ.getBundle().getString("CopyPlotValues")/*
                 * "Copying plot values..."
                 */);
        initDigits();
        CharArrayWriter aw = new CharArrayWriter(nPoints * 4);
        PrintWriter pw = new PrintWriter(aw);
        for (int i = 0; i < nPoints; i++) {
            if (saveXValues) {
                pw.print(IJ.d2s(xValues[i], xdigits) + "\t" + IJ.d2s(yValues[i], ydigits) + "\n");
            } else {
                pw.print(IJ.d2s(yValues[i], ydigits) + "\n");
            }


        }
        String text = aw.toString();
        pw.close();
        StringSelection contents = new StringSelection(text);
        systemClipboard.setContents(contents, this);
        IJ.showStatus(text.length() + " " + IJ.getBundle().getString("CharCopied")/*
                 * " characters copied to Clipboard"
                 */);
        if (autoClose) {
            imp.changes = false;
            close();
        }
    }

    void initDigits() {
        ydigits = Analyzer.getPrecision();
        if (ydigits == 0) {
            ydigits = 2;
        }
        if (ydigits != defaultDigits) {
            realNumbers = false;
            for (int i = 0; i < xValues.length; i++) {
                if ((int) xValues[i] != xValues[i]) {
                    realNumbers = true;
                }
            }
            defaultDigits = ydigits;
        }
        xdigits = realNumbers ? ydigits : 0;

    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
    }

    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();
        ImagePlus imp2 = WindowManager.getCurrentImage();
        if (b == list) {
            showList();
        } else if (b == save) {
            saveAsText();
        } else if (b == setting) {
            //if(imp2 != null )
            setPlotScale(imp2);
        } else if (b == setScaleRadio) {
            //if(PlotWindow.Base_Line_subtracted==false)
            PlotWindow.Base_Line_subtracted = false;
            setRadioSpectrumScale();
        } else if (b == baseline) {

            setRadioSpectrumBaseline();
        } else if (b == gauss) {
            if (nbgauss < 5) {
                gaussFitting();
            } else {
                IJ.error(IJ.getBundle().getString("error3"));
            }
        } else if (b == subtract) {

            subtractBaseline();
        } else if (b == setScale) {
            IJ.runPlugIn("ij.plugin.filter.ScaleDialog", "");
            IJ.runPlugIn("ij.plugin.filter.Profiler", "");
            if (imp2 != null) {
                close();
            }
        } else if (b == option) {

            IJ.runPlugIn("ij.plugin.filter.Profiler", "set");
            IJ.runPlugIn("ij.plugin.filter.Profiler", "");
            if (imp2 != null) {
                close();
            }
        } else {
            copyToClipboard();
        }
    }

    public float[] getYValues() {
        return yValues;
    }

    /**
     * Called once when ImageJ quits.
     */
    public static void savePreferences(Properties prefs) {
        double min = ProfilePlot.getFixedMin();
        double max = ProfilePlot.getFixedMax();
        if (!(min == 0.0 && max == 0.0) && min < max) {
            prefs.put(MIN, Double.toString(min));
            prefs.put(MAX, Double.toString(max));
        }
        if (plotWidth != WIDTH || plotHeight != HEIGHT) {
            prefs.put(PLOT_WIDTH, Integer.toString(plotWidth));
            prefs.put(PLOT_HEIGHT, Integer.toString(plotHeight));
        }
        int options = 0;
        if (saveXValues) {
            options |= SAVE_X_VALUES;
        }
        if (autoClose) {
            options |= AUTO_CLOSE;
        }
        prefs.put(OPTIONS, Integer.toString(options));
    }

    public boolean close() {
        boolean b = super.close();
        if (origin != null) {
            try {
                origin.getWindow().getCanvas().setPoint(-1, -1, null);
            } catch (NullPointerException e) {
            }
        }
        return b;
    }

    /**
     * dialog allow to Change the values or labels of the two axis.
     *
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

    public void setPlotScale(ImagePlus imp2) {
        int n = 2;
        if (RadioSpectrum_Reader.SCALETYPE == 0) {
            n = 0;
        }

        GenericDialog gd = new GenericDialog(IJ.getBundle().getString("Properties"), this);
        gd.addStringField("     X label :", xLabel, xLabel.length());
        gd.addNumericField("             X min : ", xMin, n);
        gd.addNumericField("             X max : ", xMax, n);
        gd.addStringField("     Y label :", yLabel, yLabel.length());
        gd.addNumericField("             Y min : ", yMin, 2);
        gd.addNumericField("             Y max : ", yMax, 2);

        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        String xLab = gd.getNextString();
        double xMi = gd.getNextNumber();
        double xMa = gd.getNextNumber();
        String yLab = gd.getNextString();
        double yMi = gd.getNextNumber();
        double yMa = gd.getNextNumber();
        double[] newxValues;
        double[] newyValues;
        double yvalue, xvalue;

        if ((xMi < xInitMin) || (xMi > xInitMax)) {
            xMi = xInitMin;
        }
        if ((xMa > xInitMax) || (xMa < xInitMin)) {
            xMa = xInitMax;
        }

        int ii = 0;
        int nInitPoints = xInitValues.length;
        for (int i = 0; i < nInitPoints; i++) {
            yvalue = yInitValues[i];
            xvalue = xInitValues[i];
            if ((xvalue >= xMi) && (xvalue <= xMa)) {
                ii++;
            }
        }

        newxValues = new double[ii];
        newyValues = new double[ii];
        ii = 0;
        for (int i = 0; i < nInitPoints; i++) {
            yvalue = yInitValues[i];
            xvalue = xInitValues[i];
            if ((xvalue >= xMi) && (xvalue <= xMa)) {
                newyValues[ii] = yvalue;
                newxValues[ii] = xvalue;
                ii++;
            }
        }
        PlotWindow pw;
        if (imp2 == null) {
            pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + " "/*
                     * "Plot of "
                     */ + origin.getShortTitle() + ((origin.getStackSize() > 1) ? "#" + origin.getCurrentSlice() : ""), xLab, yLab, Tools.toFloat(newxValues),
                    Tools.toFloat(newyValues), xInitValues, yInitValues, horizontal, origin, region);
        } else {
            pw = new PlotWindow(getTitle(), xLab, yLab, Tools.toFloat(newxValues),
                    Tools.toFloat(newyValues), xInitValues, yInitValues, origin);
        }
        pw.setLimits(xMi, xMa, yMi, yMa);
        pw.draw();
        close();
    }

    public void setRadioSpectrumScale() {
        double wave0 = RadioSpectrum_Reader.wave_ref;
        GenericDialog gd = new GenericDialog(IJ.getBundle().getString("Set_Scale"), this);
        String[] scales = {IJ.getPluginBundle().getString("ChannelRS"),
            IJ.getPluginBundle().getString("VelocityRS"),
            IJ.getPluginBundle().getString("FrequencyRS"),
            IJ.getPluginBundle().getString("WavelengthRS")};
        gd.addChoice(" ", scales, IJ.getPluginBundle().getString("ChannelRS"));
        if (RadioSpectrum_Reader.optique) {
            gd.addNumericField(IJ.getPluginBundle().getString("Wave_ref"), wave0, 3, 10, "nm");
        }
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        int index = gd.getNextChoiceIndex();
        if (RadioSpectrum_Reader.optique) {
            wave0 = gd.getNextNumber();
            RadioSpectrum_Reader.wave_ref = wave0;
        }
        FileInfo fi = origin.getOriginalFileInfo();
        RadioSpectrum_Reader.SCALETYPE = index;
        IJ.runPlugIn("ij.plugin.RadioSpectrum_Reader", fi.directory + fi.fileName);
        close();
    }

    public void setRadioSpectrumBaseline() {
        int n = 2;
        if (RadioSpectrum_Reader.SCALETYPE == 0) {
            n = 0;
        }
        GenericDialog gd = new GenericDialog("baseline", this);
        double x1 = 0.0, x2 = 0.0, x3 = 0.0, x4 = 0.0;
        int order = 1;
        gd.addMessage(IJ.getPluginBundle().getString("chooseInter"));
        gd.addMessage("                " + IJ.getPluginBundle().getString("Finter"));
        gd.addNumericField("              Xmin = ", x1, n);
        gd.addNumericField("              Xmax = ", x2, n);
        gd.addMessage("                " + IJ.getPluginBundle().getString("Sinter"));
        gd.addNumericField("              Xmin = ", x3, n);
        gd.addNumericField("              Xmax = ", x4, n);
        gd.addMessage(IJ.getPluginBundle().getString("chooseOder"));
        gd.addNumericField("             " + IJ.getPluginBundle().getString("order") + " = ", order, 0);
        gd.addCheckbox(IJ.getPluginBundle().getString("DisplayZero"), ZERO_LINE);
        gd.showDialog();
        x1 = gd.getNextNumber();
        x2 = gd.getNextNumber();
        x3 = gd.getNextNumber();
        x4 = gd.getNextNumber();
        order = (int) gd.getNextNumber();
        ZERO_LINE = gd.getNextBoolean();
        if (gd.wasCanceled()) {
            return;
        }
        if (x1 > x2) {
            change(x1, x2);
        }
        if (x3 > x4) {
            change(x3, x4);
        }
        if (order > 4) {
            order = 4;
        }
        if (order < 1) {
            order = 1;
        }
        if ((x1 < xInitMin) || (x1 > xInitMax)) {
            x1 = xInitMin;
        }
        if ((x2 > xInitMax) || (x2 < xInitMin)) {
            x2 = xInitMax;
        }
        if ((x3 < xInitMin) || (x3 > xInitMax)) {
            x3 = xInitMin;
        }
        if ((x4 > xInitMax) || (x4 < xInitMin)) {
            x4 = xInitMax;
        }
        int nbp = 0;
        for (int i = 0; i < xValues.length; i++) {
            if (((xValues[i] > x1) & (xValues[i] < x2)) || ((xValues[i] > x3) & (xValues[i] < x4))) {
                nbp++;
            }
        }


        float[] xbase = new float[nbp];
        float[] ybase = new float[nbp];
        float[] bLine = new float[order + 1];
        int ii = 0;
        for (int i = 0; i < xValues.length; i++) {
            if (((xValues[i] > x1) & (xValues[i] < x2)) || ((xValues[i] > x3) & (xValues[i] < x4))) {
                xbase[ii] = xValues[i];
                ybase[ii] = yValues[i];
                ii++;
            }
        }
        Tools.fittingData(xbase, ybase, bLine);
        yValuesBaseLine = new float[xValues.length];
        for (int i = 0; i < xValues.length; i++) {
            float poly = bLine[order];
            for (int j = 0; j < (order); j++) {
                poly = poly * xValues[i] + bLine[order - j - 1];
            }
            yValuesBaseLine[i] = poly;
        }
        PlotWindow.Base_Line = true;
        FileInfo fi = origin.getOriginalFileInfo();
        PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fi.fileName, xLabel, yLabel, xValues, yValues, yValuesBaseLine, origin);
        pw.draw();
        close();
    }

    void change(double x1, double x2) {
        double l = x1;
        x1 = x2;
        x2 = l;
    }

    public void subtractBaseline() {

        double[] newxValues;
        double[] newyValues;
        double yvalue, xvalue;
        for (int i = 0; i < xValues.length; i++) {
            yValues[i] -= yValuesBaseLine[i];
        }
        PlotWindow.Base_Line = false;
        PlotWindow.Base_Line_subtracted = true;
        PlotWindow pw = new PlotWindow(getTitle(), xLabel, yLabel, xValues, yValues, xInitValues, yInitValues, origin);
        pw.draw();
        close();


    }

    public void gaussFitting() {
        int n = 2;
        boolean erase = false;
        if (RadioSpectrum_Reader.SCALETYPE == 0) {
            n = 0;
        }
        GenericDialog gd = new GenericDialog(IJ.getPluginBundle().getString("GaussianfitTitle"), this);
        double x1 = 0.0, x2 = 0.0, ampl = 0.0, center = 0.0, width = 0.0;
        int order = 1;
        gd.addMessage(IJ.getPluginBundle().getString("ChooseParam"));
        gd.addMessage("                " + IJ.getPluginBundle().getString("interval"));
        gd.addNumericField("              Xmin = ", x1, n);
        gd.addNumericField("              Xmax = ", x2, n);
        gd.addMessage("                ");
        gd.addNumericField("              " + IJ.getPluginBundle().getString("amplitudeRS") + " = ", ampl, n);
        gd.addNumericField("              " + IJ.getPluginBundle().getString("centerRS") + " = ", center, n);
        gd.addNumericField("              " + IJ.getPluginBundle().getString("widthRS") + " = ", width, n);
        gd.addCheckbox(IJ.getPluginBundle().getString("eraseRS"), erase);
        gd.showDialog();
        x1 = gd.getNextNumber();
        x2 = gd.getNextNumber();
        ampl = gd.getNextNumber();
        center = gd.getNextNumber();
        width = gd.getNextNumber();
        erase = gd.getNextBoolean();
        if (gd.wasCanceled()) {
            return;
        }

        if (x1 > x2) {
            change(x1, x2);
        }

        int nbp = 0;
        for (int i = 0; i < xValues.length; i++) {
            if ((xValues[i] > x1) & (xValues[i] < x2)) {
                nbp++;
            }
        }


        float[] xgauss = new float[nbp];
        float[] ygauss = new float[nbp];
        int ii = 0;
        for (int i = 0; i < xValues.length; i++) {
            if ((xValues[i] > x1) & (xValues[i] < x2)) {
                xgauss[ii] = xValues[i];
                ygauss[ii] = yValues[i];
                ii++;
            }
        }
        double[] a = new double[3];
        a[0] = ampl;
        a[1] = center;
        a[2] = width;
        int err = RadioSpectrum_Reader.gaussianFitSpectralLine(xgauss, ygauss, a);
        if (err != 0) {
            if (!erase) {
                nbgauss++;
            }
            gaussFitResult[3 * nbgauss] = a[0];
            gaussFitResult[3 * nbgauss + 1] = a[1];
            gaussFitResult[3 * nbgauss + 2] = a[2];
            FileInfo fi = origin.getOriginalFileInfo();
            PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fi.fileName, xLabel, yLabel, xValues, yValues, gaussFitResult, nbgauss, origin);
            pw.draw();
            RadioSpectrum_Reader.addResult(origin, a);
            close();
        }
    }

    public void displayGaussianFit() {
        float[] ytmp = new float[xValues.length];

        for (int i = 0; i < nbgauss + 1; i++) {
            Tools.Fgauss(xValues, gaussFitResult, i, ytmp);
            addPoints(xValues, ytmp, LINE, Color.red);
        }
    }

    public void mergeGaussFitting() {
        GenericDialog gd = new GenericDialog("merge Gaussians fits of spectral line", this);
        gd.addMessage("do you want merge ?");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        float[][] ytmp = new float[nbgauss + 1][xValues.length];
        for (int i = 0; i < nbgauss + 1; i++) {
            Tools.Fgauss(xValues, gaussFitResult, i, ytmp[i]);
        }
        yValuesMergeGaussFit = new float[xValues.length];
        float tmpmax;
        float max;

        for (int j = 0; j < xValues.length; j++) {
            max = ytmp[0][j];
            for (int i = 1; i < nbgauss + 1; i++) {
                tmpmax = java.lang.Math.abs(ytmp[i][j] - yValues[j]);
                if (tmpmax < java.lang.Math.abs((max - yValues[j]))) {
                    max = ytmp[i][j];
                }
            }
            yValuesMergeGaussFit[j] = max;
        }
        FileInfo fi = origin.getOriginalFileInfo();
        PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fi.fileName, xLabel, yLabel, xValues, yValues, yValuesMergeGaussFit, origin);
        pw.draw();
        close();
    }

    public void setJustification(int justification) {
        throw new UnsupportedOperationException("Not yet implemented");
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
}
