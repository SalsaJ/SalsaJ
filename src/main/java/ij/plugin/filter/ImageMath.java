package ij.plugin.filter;

import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;

/**
 *  This plugin implements ImageJ's Process/Math submenu.
 *
 *@author     Thomas
 *@created    3 decembre 2007
 */
public class ImageMath implements PlugInFilter {

    private String arg;
    private ImagePlus imp;
    private boolean canceled;
    private boolean first;// first stack slice?
    private double lower;
    private double upper;
    private static double addValue = 25;
    private static double mulValue = 1.25;
    private static double minValue = 0;
    private static double maxValue = 255;
    private final static String defaultAndValue = "11110000";
    private static String andValue = defaultAndValue;
    private final static double defaultGammaValue = 0.5;
    private static double gammaValue = defaultGammaValue;

    /**
     *  Description of the Method
     *
     *@param  arg  Description of the Parameter
     *@param  imp  Description of the Parameter
     *@return      Description of the Return Value
     */
    public int setup(String arg, ImagePlus imp) {
        this.arg = arg;
        this.imp = imp;
        first = true;
        IJ.register(ImageMath.class);
        return IJ.setupDialog(imp, DOES_ALL + SUPPORTS_MASKING);
    }

    /**
     *  Main processing method for the ImageMath object
     *
     *@param  ip  Description of the Parameter
     */
    public void run(ImageProcessor ip) {

        double value;

        if (canceled) {
            return;
        }

        if (arg.equals("add")) {
            if (first) {
                //EU_HOU Bundle
                addValue = getValue(IJ.getPluginBundle().getString("Add"), IJ.getPluginBundle().getString("Value"), addValue, 0);
            }
            if (canceled) {
                return;
            }
            ip.add(addValue);
            return;
        }

        if (arg.equals("sub")) {
            if (first) {
                //EU_HOU Bundle
                addValue = getValue(IJ.getPluginBundle().getString("Subtract"), IJ.getPluginBundle().getString("Value"), addValue, 0);
            }
            if (canceled) {
                return;
            }
            ip.add(-addValue);
            return;
        }

        if (arg.equals("mul")) {
            if (first) {
                //EU_HOU Bundle
                mulValue = getValue(IJ.getPluginBundle().getString("Multiply"), IJ.getPluginBundle().getString("Value"), mulValue, 2);
            }
            if (canceled) {
                return;
            }
            ip.multiply(mulValue);
            return;
        }

        if (arg.equals("div")) {
            if (first) {
                //EU_HOU Bundle
                mulValue = getValue(IJ.getPluginBundle().getString("Divide"), IJ.getPluginBundle().getString("Value"), mulValue, 2);
            }
            if (canceled) {
                return;
            }
            if (mulValue != 0.0) {
                ip.multiply(1.0 / mulValue);
            }
            return;
        }

        if (arg.equals("and")) {
            if (first) {
                //EU_HOU Bundle
                andValue = getBinaryValue(IJ.getPluginBundle().getString("AND"), IJ.getPluginBundle().getString("BinValue"), andValue);
            }
            if (canceled) {
                return;
            }
            try {
                ip.and(Integer.parseInt(andValue, 2));
            } catch (NumberFormatException e) {
                andValue = defaultAndValue;
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("BinValReqErr"));
            }
            return;
        }

        if (arg.equals("or")) {
            if (first) {
                //EU_HOU Bundle
                andValue = getBinaryValue(IJ.getPluginBundle().getString("OR"), IJ.getPluginBundle().getString("BinValue"), andValue);
            }
            if (canceled) {
                return;
            }
            try {
                ip.or(Integer.parseInt(andValue, 2));
            } catch (NumberFormatException e) {
                andValue = defaultAndValue;
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("BinValReqErr"));
            }
            return;
        }

        if (arg.equals("xor")) {
            if (first) {
                //EU_HOU Bundle
                andValue = getBinaryValue(IJ.getPluginBundle().getString("XOR"), IJ.getPluginBundle().getString("BinValue"), andValue);
            }
            if (canceled) {
                return;
            }
            try {
                ip.xor(Integer.parseInt(andValue, 2));
            } catch (NumberFormatException e) {
                andValue = defaultAndValue;
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("BinValReqErr"));
            }
            return;
        }

        if (arg.equals("min")) {
            if (first) {
                //EU_HOU Bundle
                minValue = getValue(IJ.getPluginBundle().getString("Min"), IJ.getPluginBundle().getString("Value"), minValue, 0);
            }
            if (canceled) {
                return;
            }
            ip.min(minValue);
            if (!(ip instanceof ByteProcessor)) {
                ip.resetMinAndMax();
            }
            return;
        }

        if (arg.equals("max")) {
            if (first) {
                //EU_HOU Bundle
                maxValue = getValue(IJ.getPluginBundle().getString("Max"), IJ.getPluginBundle().getString("Value"), maxValue, 0);
            }
            if (canceled) {
                return;
            }
            ip.max(maxValue);
            if (!(ip instanceof ByteProcessor)) {
                ip.resetMinAndMax();
            }
            return;
        }

        if (arg.equals("gamma")) {
            if (first) {
                //EU_HOU Bundle
                gammaValue = getValue(IJ.getPluginBundle().getString("Gamma"), IJ.getPluginBundle().getString("Value01"), gammaValue, 2);
            }
            if (canceled) {
                return;
            }
            if (gammaValue < 0.1 || gammaValue > 5.0) {
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("GammaValErr"));
                gammaValue = defaultGammaValue;
                return;
            }
            ip.gamma(gammaValue);
            return;
        }

        if (arg.equals("log")) {
            ip.log();
            return;
        }

        if (arg.equals("exp")) {
            ip.exp();
            return;
        }

        if (arg.equals("sqr")) {
            ip.sqr();
            return;
        }

        if (arg.equals("sqrt")) {
            ip.sqrt();
            return;
        }

        if (arg.equals("reciprocal")) {
            if (!isFloat(ip)) {
                return;
            }
            float[] pixels = (float[]) ip.getPixels();
            for (int i = 0; i < ip.getWidth() * ip.getHeight(); i++) {
                if (pixels[i] == 0f) {
                    pixels[i] = Float.NaN;
                } else {
                    pixels[i] = 1f / pixels[i];
                }
            }
            ip.resetMinAndMax();
            return;
        }

        if (arg.equals("nan")) {
            setBackgroundToNaN(ip);
            return;
        }

        if (arg.equals("abs")) {
            if (!((ip instanceof FloatProcessor) || imp.getCalibration().isSigned16Bit())) {
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("32BitSigned16BitImgReqErr"));
                canceled = true;
            } else {
                ip.abs();
                ip.resetMinAndMax();
            }
            return;
        }
    }

    /**
     *  Gets the float attribute of the ImageMath object
     *
     *@param  ip  Description of the Parameter
     *@return     The float value
     */
    boolean isFloat(ImageProcessor ip) {
        if (!(ip instanceof FloatProcessor)) {
            //EU_HOU Bundle
            IJ.error(IJ.getPluginBundle().getString("32BitFloatImgReqErr"));
            canceled = true;
            return false;
        } else {
            return true;
        }
    }

    /**
     *  Gets the value attribute of the ImageMath object
     *
     *@param  title         Description of the Parameter
     *@param  prompt        Description of the Parameter
     *@param  defaultValue  Description of the Parameter
     *@param  digits        Description of the Parameter
     *@return               The value value
     */
    double getValue(String title, String prompt, double defaultValue, int digits) {
        int places = Analyzer.getPrecision();
        if (digits > 0 || (int) defaultValue != defaultValue) {
            digits = Math.max(places, 1);
        }
        GenericDialog gd = new GenericDialog(title);
        gd.addNumericField(prompt, defaultValue, digits, 8, null);
        gd.showDialog();
        if (first) {
            imp.startTiming();
        }
        first = false;
        canceled = gd.wasCanceled();
        if (canceled) {
            return defaultValue;
        }
        return gd.getNextNumber();
    }

    /**
     *  Gets the binaryValue attribute of the ImageMath object
     *
     *@param  title         Description of the Parameter
     *@param  prompt        Description of the Parameter
     *@param  defaultValue  Description of the Parameter
     *@return               The binaryValue value
     */
    String getBinaryValue(String title, String prompt, String defaultValue) {
        GenericDialog gd = new GenericDialog(title);
        gd.addStringField(prompt, defaultValue);
        gd.showDialog();
        if (first) {
            imp.startTiming();
        }
        first = false;
        canceled = gd.wasCanceled();
        if (canceled) {
            return defaultValue;
        }
        return gd.getNextString();
    }

    /**
     *  Set non-thresholded pixels in a float image to NaN.
     *
     *@param  ip  The new backgroundToNaN value
     */
    void setBackgroundToNaN(ImageProcessor ip) {
        if (first) {
            lower = ip.getMinThreshold();
            upper = ip.getMaxThreshold();
            first = false;
            if (lower == ImageProcessor.NO_THRESHOLD || !(ip instanceof FloatProcessor)) {
                //EU_HOU Bundle
                IJ.error(IJ.getPluginBundle().getString("Thresh32BitFloatImgReqErr"));
                canceled = true;
                return;
            }
        }
        float[] pixels = (float[]) ip.getPixels();
        int width = ip.getWidth();
        int height = ip.getHeight();
        double v;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                v = pixels[y * width + x];
                if (v < lower || v > upper) {
                    pixels[y * width + x] = Float.NaN;
                }
            }
        }
        ip.resetMinAndMax();
        return;
    }
}
