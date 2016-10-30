/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.process;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.PlotWindow;
import java.util.ResourceBundle;

/**
 *
 * @author thomas
 */
public abstract class Spectrum {

    // raw data (pixels or units)
    double[] XdataRaw;
    double[] YdataRaw;
    // display data
    double[] XdataDisplay;
    double[] YdataDisplay;
    // fits decoder information
    int CRPIX1;
    float CRVAL1;
    int CDELT1;
    String CTYPE1;
    String CUNIT1;
    boolean optique;
    static public final int DISPLAY_VELOCITY = 1;
    static public final int DISPLAY_FREQ = 2;
    static public final int DISPLAY_WAVELENGTH = 3;
    static public final int DISPLAY_DEFAULT = 0;
    int display = 0;
    // CONSTANTES
    static final double SPEED_C=299792458; // in m/s
    static final double WAVE_REF=21.10611405413;// in cm
    static final double FREQ_REF=1420.40575177; // in MHz
    
    // Plot
    private static ResourceBundle bun = IJ.getPluginBundle();
    String freqUnit = "THz";
    String waveUnit = "nm";

    public Spectrum(double[] XdataRaw, double[] YdataRaw) {
        this.XdataRaw = XdataRaw;
        this.YdataRaw = YdataRaw;
    }

    public int getDisplay() {
        return display;
    }

    public void setDisplay(int display) {
        this.display = display;
    }

    public boolean isOptique() {
        return optique;
    }

    public void setOptique(boolean optique) {
        this.optique = optique;
    }

    public void computeDisplay() {
        int nb = XdataRaw.length;
        double[] Xtemp = new double[nb];
        double[] Ytemp = new double[nb];

        // X values in mode (freq, ...) used in CTYPE1;
        for (int i = 0; i < nb; i++) {
            Xtemp[i] = (i - CRPIX1) * CDELT1 + CRVAL1;
            Ytemp[i] = YdataRaw[i];
        }
        // convertir en unité d'affichage
        // classe ij.util.Unit
    }

    public PlotWindow plot() {
        // compute new values
        computeDisplay();

        /// LABELS
        String xLabel, yLabel;
        switch (display) {
            case DISPLAY_VELOCITY:
                //EU_HOU Bundle
                xLabel = bun.getString("VelocityRS") + " (" + bun.getString("unitsVelocityRS") + ")";
                break;
            case DISPLAY_FREQ:
                //EU_HOU Bundle
                // xLabel = bun.getString("FrequencyRS") + " (" + bun.getString("unitsFrequencyRS") + ")";
                xLabel = bun.getString("FrequencyRS") + " (" + freqUnit + ")";
                break;
            case DISPLAY_WAVELENGTH:
                //EU_HOU Bundle
                // xLabel = bun.getString("WavelengthRS") + " (" + bun.getString("unitsWavelengthRS") + ")";
                xLabel = bun.getString("WavelengthRS") + " (" + waveUnit + ")";
                break;
            default:
                pixel:
                //EU_HOU Bundle
                xLabel = bun.getString("ChannelRS");
                break;
        }

        ////*/////////// PLOT //////////////////////////
        ImagePlus impdata = new ImagePlus();
        FloatProcessor ipdata = new FloatProcessor(XdataDisplay.length, 150);
        impdata.setProcessor("data", ipdata);
        //FileInfo fi = new FileInfo();
        //fi.fileName = fileName;
        //fi.directory = directory;
        //impdata.setFileInfo(fi);
        yLabel = bun.getString("IntensityRS");
        PlotWindow.RadioSpectra = true;
        PlotWindow.Base_Line_subtracted = false;
        PlotWindow.Base_Line = false;
        PlotWindow.ZERO_LINE = false;
        PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + "test-spectrum", xLabel, yLabel, XdataDisplay, YdataDisplay, impdata);

        return pw;
    }
}
