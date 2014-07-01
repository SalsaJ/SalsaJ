package ij.plugin;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.util.Tools;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.Recorder;
import ij.measure.Calibration;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;

/**
 * This plugin implements the Analyze/Histogram command.
 *
 * @author Thomas
 * @created 30 novembre 2007
 */
public class Histogram implements PlugIn, TextListener {

    private static int nBins = 256;
    private static boolean useImageMinAndMax = true;
    private static double xMin, xMax;
    private static String yMax = "Auto";
    private static boolean stackHistogram;
    private static int imageID;
    private Checkbox checkbox;
    private TextField minField, maxField;
    private String defaultMin, defaultMax;

    /**
     * Main processing method for the Histogram object
     *
     * @param arg Description of the Parameter
     */
    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        int bitDepth = imp.getBitDepth();
        if (bitDepth == 32 || IJ.altKeyDown()) {
            IJ.setKeyUp(KeyEvent.VK_ALT);
            if (!showDialog(imp)) {
                return;
            }
        } else {
            int flags = setupDialog(imp, 0);
            if (flags == PlugInFilter.DONE) {
                return;
            }
            stackHistogram = flags == PlugInFilter.DOES_STACKS;
            Calibration cal = imp.getCalibration();
            nBins = 256;
            if (stackHistogram && ((bitDepth == 8 && !cal.calibrated()) || bitDepth == 24)) {
                xMin = 0.0;
                xMax = 256.0;
                useImageMinAndMax = false;
            } else {
                useImageMinAndMax = true;
            }
            yMax = "Auto";
        }
        ImageStatistics stats = null;
        if (useImageMinAndMax) {
            xMin = 0.0;
            xMax = 0.0;
        }
        int iyMax = (int) Tools.parseDouble(yMax, 0.0);
        boolean customHistogram = (bitDepth == 8 || bitDepth == 24) && (!(xMin == 0.0 && xMax == 0.0) || nBins != 256 || iyMax > 0);
        ImageWindow.centerNextImage();
        if (stackHistogram || customHistogram) {
            ImagePlus imp2 = imp;
            if (customHistogram && !stackHistogram && imp.getStackSize() > 1) {
                //EU_HOU Bundle
                imp2 = new ImagePlus("Temp", imp.getProcessor());
            }
            stats = new StackStatistics(imp2, nBins, xMin, xMax);
            stats.histYMax = iyMax;
            //EU_HOU Bundle
            new HistogramWindow(IJ.getBundle().getString("HistoTitle") + " " + imp.getShortTitle(), imp, stats);
        } else {
            //EU_HOU Bundle
            new HistogramWindow(IJ.getBundle().getString("HistoTitle") + " " + imp.getShortTitle(), imp, nBins, xMin, xMax, iyMax);
        }
    }

    /**
     * Description of the Method
     *
     * @param imp Description of the Parameter
     * @return Description of the Return Value
     */
    boolean showDialog(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor();
        double min = ip.getMin();
        double max = ip.getMax();
        if (imp.getID() != imageID || (min == xMin && min == xMax)) {
            useImageMinAndMax = true;
        }
        if (imp.getID() != imageID || useImageMinAndMax) {
            xMin = min;
            xMax = max;
            Calibration cal = imp.getCalibration();
            xMin = cal.getCValue(xMin);
            xMax = cal.getCValue(xMax);
        }
        
        // case 32-bits, use mean +- 5 sigma (TB 250613)
        if (imp.getBitDepth() == 32) {
            if (imp.getImageStackSize() == 1) {
                ImageStatistics stats = new FloatStatistics(imp.getProcessor());
                xMin = Math.max(stats.mean - 5 * stats.stdDev, stats.min);
                xMax = Math.min(stats.mean + 5 * stats.stdDev, stats.max);
            } else {
                StackStatistics stats = new StackStatistics(imp);
                xMin = Math.max(stats.mean - 5 * stats.stdDev, stats.min);
                xMax = Math.min(stats.mean + 5 * stats.stdDev, stats.max);
            }
            Calibration cal = imp.getCalibration();
            xMin = cal.getCValue(xMin);
            xMax = cal.getCValue(xMax);
            useImageMinAndMax = false;
        }

        defaultMin = IJ.d2s(xMin, 2);
        defaultMax = IJ.d2s(xMax, 2);
        imageID = imp.getID();
        int stackSize = imp.getStackSize();
        //EU_HOU Bundle
        GenericDialog gd = new GenericDialog(IJ.getBundle().getString("Histogram"));
        //EU_HOU Bundle
        gd.addNumericField(IJ.getBundle().getString("Bins") + ":", HistogramWindow.nBins, 0);
        //EU_HOU Bundle
        gd.addCheckbox(IJ.getBundle().getString("HistoMinMax") + ":", useImageMinAndMax);
        //gd.addMessage("          or");
        gd.addMessage("");
        int fwidth = 6;
        int nwidth = Math.max(IJ.d2s(xMin, 2).length(), IJ.d2s(xMax, 2).length());
        if (nwidth > fwidth) {
            fwidth = nwidth;
        }
        //EU_HOU Bundle
        gd.addNumericField(IJ.getBundle().getString("HistoXmin") + ":", xMin, 2, fwidth, null);
        gd.addNumericField(IJ.getBundle().getString("HistoXmax") + ":", xMax, 2, fwidth, null);
        gd.addMessage(" ");
        gd.addStringField(IJ.getBundle().getString("HistoYmax") + ":", yMax, 6);
        if (stackSize > 1) {
            //EU_HOU Bundle
            gd.addCheckbox("Stack Histogram", stackHistogram);
        }
        Vector numbers = gd.getNumericFields();
        minField = (TextField) numbers.elementAt(1);
        minField.addTextListener(this);
        maxField = (TextField) numbers.elementAt(2);
        maxField.addTextListener(this);
        checkbox = (Checkbox) (gd.getCheckboxes().elementAt(0));
        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        }
        nBins = (int) gd.getNextNumber();
        if (nBins >= 2 && nBins <= 1000) {
            HistogramWindow.nBins = nBins;
        }
        useImageMinAndMax = gd.getNextBoolean();
        xMin = gd.getNextNumber();
        xMax = gd.getNextNumber();
        yMax = gd.getNextString();
        stackHistogram = (stackSize > 1) ? gd.getNextBoolean() : false;
        IJ.register(Histogram.class);
        return true;
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    @Override
    public void textValueChanged(TextEvent e) {
        boolean rangeChanged = !defaultMin.equals(minField.getText()) || !defaultMax.equals(maxField.getText());
        if (rangeChanged) {
            checkbox.setState(false);
        }
    }

    /**
     * Description of the Method
     *
     * @param imp Description of the Parameter
     * @param flags Description of the Parameter
     * @return Description of the Return Value
     */
    int setupDialog(ImagePlus imp, int flags) {
        int stackSize = imp.getStackSize();
        if (stackSize > 1) {
            String macroOptions = Macro.getOptions();
            if (macroOptions != null) {
                if (macroOptions.indexOf("stack ") >= 0) {
                    return flags + PlugInFilter.DOES_STACKS;
                } else {
                    return flags;
                }
            }
            //EU_HOU Bundle
            YesNoCancelDialog d = new YesNoCancelDialog(IJ.getInstance(),
                    IJ.getBundle().getString("Histogram"), "Include all " + stackSize + " slices?");
            if (d.cancelPressed()) {
                return PlugInFilter.DONE;
            } else if (d.yesPressed()) {
                if (Recorder.record) {
                    Recorder.recordOption("stack");
                }
                return flags + PlugInFilter.DOES_STACKS;
            }
            if (Recorder.record) {
                Recorder.recordOption("slice");
            }
        }
        return flags;
    }
}
