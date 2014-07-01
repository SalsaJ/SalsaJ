package ij.plugin.filter;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Panel.*;
import ij.measure.Calibration;

/**
 * Displays the active image's look-up table.
 *
 * @author Thomas @created 19 novembre 2007
 */
public class LutViewer implements PlugInFilter {

    ImagePlus imp;

    /**
     * Description of the Method
     *
     * @param arg Description of the Parameter
     * @param imp Description of the Parameter
     * @return Description of the Return Value
     */
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL - DOES_RGB + NO_UNDO + NO_CHANGES;
    }


    /*
     * EU_HOU CHANGES
     */
    /**
     * Main processing method for the LutViewer object
     *
     * @param ip Description of the Parameter
     */
    public void run(ImageProcessor ip) {
        ImagePlus LUTview = prepare(ip, imp.createLut(), imp.getCalibration());
        new LutWindow(LUTview, new ImageCanvas(LUTview));
    }

    /**
     * Main processing method for the LutViewer object
     *
     * @param ip Description of the Parameter
     * @param lut Description of the Parameter
     * @param cal Description of the Parameter
     * @return Description of the Return Value
     */
    static ImagePlus prepare(ImageProcessor ip, LookUpTable lut, Calibration cal) {
        int xMargin = 35;
        int yMargin = 20;
        int width = 128;
        int height = 0;
        int x;
        int y;
        int x1;
        int y1;
        int x2;
        int y2;
        int imageWidth;
        int imageHeight;
        int barHeight = 512;
        boolean isGray;
        double scale;

        int mapSize = lut.getMapSize();
        if (mapSize == 0) {
            return null;
        }
        //imageWidth = 2*width + 2*xMargin;
        //imageHeight = 2*yMargin +barHeight+2;
        imageWidth = width + 2 * xMargin;
        imageHeight = 2 * yMargin + barHeight + 2;

        Image img = IJ.getInstance().createImage(imageWidth, imageHeight);
        Graphics g = img.getGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, imageWidth, imageHeight);
        g.setColor(Color.black);
        //g.drawRect(xMargin, yMargin, width, height);
        //ip = imp.getChannelProcessor();

        IndexColorModel cm = (IndexColorModel) ip.getColorModel();
        //LookUpTable lut = new LookUpTable(cm);

        byte[] reds = lut.getReds();
        byte[] greens = lut.getGreens();
        byte[] blues = lut.getBlues();

        isGray = lut.isGrayscale();
        scale = 256.0 / mapSize;
        if (isGray) {
            g.setColor(Color.black);
        } else {
            g.setColor(Color.red);
        }
        //x = xMargin;
        x = xMargin + 2;
        //y = yMargin + height + 2;
        y = yMargin;
        double min = ip.getMin();
        double max = ip.getMax();

        if (cal != null) {
            min = cal.getCValue(min);
            max = cal.getCValue(max);
        }
        //lut.drawColorBar(g, x, y, 256, barHeight);
        lut.drawVertColorBar(g, x, y, width, barHeight, min, max);

        /*
         * y += barHeight + 15; g.setColor(Color.black); g.drawString("0", x -
         * 4, y); g.drawString("" + (mapSize - 1), x + width - 10, y);
         * g.drawString("255", 7, yMargin + 4);
         */
        //g.dispose();

        //ImagePlus imp = new ImagePlus("Look-Up Table", img);
        //imp.show();
        //new LutWindow(imp, new ImageCanvas(imp), ip);
        //EU_HOU Bundle
        return new ImagePlus(IJ.getPluginBundle().getString("LUTTitle"), img);
    }
}// LutViewer class

/**
 * Description of the Class
 *
 * @author Thomas @created 19 novembre 2007
 */
class LutWindow extends ImageWindow implements ActionListener {

    private Button button, buttonUpdate;
    private ImageProcessor ip;
    private ImagePlus oi;

    /**
     * Constructor for the LutWindow object
     *
     * @param imp Description of the Parameter
     * @param ic Description of the Parameter
     */
    /*
     * LutWindow(ImagePlus imp, ImageCanvas ic, ImageProcessor ip) { super(imp,
     * ic); this.ip = ip; addPanel();
	}
     */
    /**
     * Constructor for the LutWindow object
     *
     * @param imp Description of the Parameter
     * @param ic Description of the Parameter
     */
    LutWindow(ImagePlus imp, ImageCanvas ic) {
        super(imp, ic, false);
        oi = imp;
        addPanel();
    }

    /**
     * Adds a feature to the Panel attribute of the LutWindow object
     */
    void addPanel() {
        Panel panel = new Panel();

        panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        //EU_HOU Bundle
        button = new Button(IJ.getPluginBundle().getString("List"));
        buttonUpdate = new Button(IJ.getPluginBundle().getString("Update"));
        button.addActionListener(this);
        buttonUpdate.addActionListener(this);
        panel.add(button);
        panel.add(buttonUpdate);
        add(panel);
        pack();
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        Object b = e.getSource();

        if (b == button) {
            list(WindowManager.getCurrentImage().getProcessor());
        } else if (b == buttonUpdate) {
            ImagePlus imp = WindowManager.getCurrentImage();
            ImagePlus ipp = LutViewer.prepare(imp.getProcessor(), imp.createLut(), imp.getCalibration());
            oi.setImage(ipp.getImage());
            oi.updateAndDraw();
        }
    }

    /**
     * Description of the Method
     *
     * @param ip Description of the Parameter
     */
    void list(ImageProcessor ip) {
        IndexColorModel icm = (IndexColorModel) ip.getColorModel();
        int size = icm.getMapSize();
        byte[] r = new byte[size];
        byte[] g = new byte[size];
        byte[] b = new byte[size];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        StringBuffer sb = new StringBuffer();
        //EU_HOU Bundle
        String headings = IJ.getPluginBundle().getString("LUTHead");
        for (int i = 0; i < size; i++) {
            sb.append(i + "\t" + (r[i] & 255) + "\t" + (g[i] & 255) + "\t" + (b[i] & 255) + "\n");
        }
//EU_HOU Bundle
        TextWindow tw = new TextWindow(IJ.getPluginBundle().getString("LUTTextTitle"), headings, sb.toString(), 250, 400);
    }
}
