package ij.plugin;

import java.util.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;
import ij.process.*;
import ij.util.*;
import ij.text.*;
import java.io.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens and displays Spectre images. 1D
 *
 * @author jerome
 * @created 5 juillet 2006
 */
public class RadioSpectrum_Reader implements PlugIn {

    //private String directory, fileName;
    /**
     * Description of the Field
     */
    public static int SCALETYPE;
    private final int pixel = 0, velocity = 1, frequency = 2, wavelength = 3;
    private static double xGaussCenter, yGaussCenter, widthGauss, area, l, b;
    private static String xLabel, yLabel;
    private static Hashtable results;
    private static TextWindow res;
    private static int rdindex = 1;
    private static ResourceBundle bun = IJ.getPluginBundle();
    public static boolean optique;
    private static boolean spectdata;
    // TB ref wave to compute velocity (in nm)
    public static double wave_ref = 589;

    String freqUnit = "THz";
    String waveUnit = "nm";

    /**
     * Main processing method for the RadioSpectrum_Reader object
     *
     * @param arg Description of the Parameter
     */
    public void run(String arg) {
        //EU_HOU Bundle
        OpenDialog od = new OpenDialog(IJ.getBundle().getString("Open"), arg);
        String directory = od.getDirectory();
        String fileName = od.getFileName();

        if (fileName == null) {
            return;
        }
        //EU_HOU Bundle
        IJ.showStatus("Opening: " + directory + fileName);
        InputStreamReader stream = null;
        if (directory.startsWith("http")) {
            IJ.log("Reading Spectrum over the Internet " + directory + " " + fileName);
            try {
                stream = new InputStreamReader((new URL(directory + fileName)).openStream());
            } catch (IOException ex) {
                Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                stream = new FileReader(directory + fileName);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (stream == null) {
            return;
        }
        // TEST DATA SPECTRUM
        if (fileName.endsWith(".dat") || fileName.endsWith(".data") || fileName.endsWith(".csv")) {
            spectdata = true;
        } else {
            spectdata = false;
        }

        //IJ.log("freq 0 "+wave_ref);
        ///////////////// SPECTDATA OPTIQUE //////////////////////////////////////
        if (spectdata) {
            PlotWindow pw = readSpectData(stream, fileName, directory);
            if (pw != null) {
                pw.draw();
            }
            // FITS
        } else {
            PlotWindow pw = readFITS(fileName, directory);
            if (pw != null) {
                pw.draw();
            }
        }
    }

    /**
     * Description of the Method
     *
     * @param xvals Description of the Parameter
     * @param yvals Description of the Parameter
     * @param a Description of the Parameter
     * @return Description of the Return Value
     */
    public static int gaussianFitSpectralLine(float[] xvals, float[] yvals, double[] a) {
        int ma;
        int DIM = 3;
        int dof = xvals.length;
        double amplitude = a[0];
        double center = a[1];
        double width = a[2];
        double[] chisq;
        double[] ochisq;
        chisq = new double[1];
        ochisq = new double[1];
        int iter;
        int itst;
        double yvalsMax;
        double[] aa = Tools.getMinMax(yvals);
        yvalsMax = aa[1];
        if (amplitude == 0) {
            amplitude = yvalsMax;
        }
        if (center == 0) {
            for (int i = 0; i < xvals.length; i++) {
                if (yvals[i] == yvalsMax) {
                    center = (double) xvals[i];
                }
            }

        }
        if (width == 0) {
            width = 10.0;
        }

        ma = DIM;
        if (dof <= ma) {
            //EU_HOU Bundle
            IJ.error(bun.getString("error1") /*
             * too few points to fit
             */);
            return 0;
        }

        double[][] covar = new double[ma][ma];
        double[][] alpha = new double[ma][ma];
        double[] sig = new double[xvals.length];
        for (int i = 0; i < sig.length; i++) {
            sig[i] = 1.0;
        }
        a[0] = amplitude;
        a[1] = center;
        a[2] = width;
        int[] ia = new int[3];
        for (int i = 0; i < ia.length; i++) {
            ia[i] = 1;
        }
        double[] alamda = new double[1];
        alamda[0] = -1;
        int err;
        err = Tools.mrqmin(Tools.toDouble(xvals), Tools.toDouble(yvals), sig, dof, a, ia, ma, covar, alpha, chisq, ochisq, alamda);
        iter = 0;
        itst = 0;
        while ((itst < 5) && (err != 0)) {
            iter++;
            ochisq[0] = chisq[0];
            err = Tools.mrqmin(Tools.toDouble(xvals), Tools.toDouble(yvals), sig, dof, a, ia, ma, covar, alpha, chisq, ochisq, alamda);
            if (chisq[0] > ochisq[0]) {
                itst = 0;
            } else if (java.lang.Math.abs(ochisq[0] - chisq[0]) < 0.001) {
                itst++;
            }
        }
        alamda[0] = 0.0;
        if (err != 0) {
            err = Tools.mrqmin(Tools.toDouble(xvals), Tools.toDouble(yvals), sig, dof, a, ia, ma, covar, alpha, chisq, ochisq, alamda);
        }
        amplitude = a[0];
        center = a[1];
        width = a[2];

        return err;
    }

    /**
     * Description of the Method
     */
    public static void activate() {

        if (res == null) {
            results = new Hashtable();
            StringBuffer sb;
            if (results.size() > 0) {
                Enumeration keys = results.keys();
                sb = new StringBuffer();
                int i = 1;
                int m = 0;
                while (keys.hasMoreElements()) {
                    ++i;
                    int n = Integer.parseInt((String) keys.nextElement());
                    if (n > m) {
                        m = n;
                    }
                }

                for (int j = 1; j <= m; ++j) {
                    IRad res = (IRad) results.get(String.valueOf(j));
                    if (res != null) {
                        sb.append(j + "\t" + res.toString() + "\n");
                    }
                }

            } else {
                sb = null;
            }
            //EU_HOU Bundle
            res = new TextWindow(bun.getString("GaussFitResults"), bun.getString("measurements") + "\t" + bun.getString("File") + "\tl\tb\t" + xLabel + "\t" + yLabel + "\t" + bun.getString("Width") + "\t" + bun.getString("Area"), (sb == null ? null : new String(sb)), 536, 200);
            res.setLocation(5, 475);
        }
        if (!res.isShowing()) {
            rdindex = 1;
            //EU_HOU Bundle
            res.getTextPanel().setColumnHeadings(bun.getString("measurements") + "\t" + bun.getString("File") + "\tl\tb\t" + xLabel + "\t" + yLabel + "\t" + bun.getString("Width") + "\t" + bun.getString("Area"));
            res.setVisible(true);
        }
        //EU_HOU Bundle
        WindowManager.activateWindow(bun.getString("GaussFitResults"));
    }

    /**
     * Description of the Method
     */
    public static void clear() {
        if (results == null) {
            return;
        }
        results.clear();
        rdindex = 1;
        if (res != null) {
            res.getTextPanel().selectAll();
            res.getTextPanel().clearSelection();
        }
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public static void clear(String s) {
        StringTokenizer st1 = new StringTokenizer(s, "\n");
        while (st1.hasMoreTokens()) {
            String s1 = st1.nextToken();
            StringTokenizer st = new StringTokenizer(s1, "\t");
            String img;
            int index;
            try {
                index = Integer.parseInt(st.nextToken());
                img = st.nextToken();
                results.remove(String.valueOf(index));
            } catch (Exception e) {
            }
            ;
        }
    }

    /**
     * Description of the Method
     */
    public void close() {
        if (res != null) {
            res.close();
            res = null;

        }
    }

    /**
     * Description of the Class
     *
     * @author thomas
     * @created 5 juillet 2006
     */
    static class IRad {

        String title;
        double xx, yy, ww, aa, ll, bb;

        /**
         * Constructor for the IRad object
         *
         * @param i Description of the Parameter
         * @param xx Description of the Parameter
         * @param yy Description of the Parameter
         * @param ww Description of the Parameter
         * @param aa Description of the Parameter
         * @param ll Description of the Parameter
         * @param bb Description of the Parameter
         */
        public IRad(String i, double xx, double yy, double ww, double aa, double ll, double bb) {
            this.title = new String(i);
            this.xx = xx;
            this.yy = yy;
            this.ww = ww;
            this.aa = aa;
            this.ll = ll;
            this.bb = bb;
        }

        /**
         * Description of the Method
         *
         * @return Description of the Return Value
         */
        public String toString() {
            return new String(title + "\t" + IJ.d2s(ll) + "\t" + IJ.d2s(bb) + "\t" + IJ.d2s(xx) + "\t" + IJ.d2s(yy) + "\t" + IJ.d2s(ww)
                    + "\t" + IJ.d2s(aa));
        }
    }

    /**
     * Adds a feature to the Result attribute of the RadioSpectrum_Reader class
     *
     * @param imp The feature to be added to the Result attribute
     * @param a The feature to be added to the Result attribute
     */
    public static void addResult(ImagePlus imp, double[] a) {
        activate();
        FileInfo fi = imp.getOriginalFileInfo();
        String t = fi.fileName;
        area = 1.064467 * a[0] * a[2];
        widthGauss = a[2];
        xGaussCenter = a[1];
        yGaussCenter = a[0];
        res.append(new String(rdindex + "\t" + t + "\t" + IJ.d2s(l) + "\t" + IJ.d2s(b) + "\t" + IJ.d2s(xGaussCenter) + "\t" + IJ.d2s(yGaussCenter)
                + "\t" + IJ.d2s(widthGauss) + "\t" + IJ.d2s(area)));
        res.getTextPanel().resetSelection();
        results.put(String.valueOf(rdindex), new IRad(t, xGaussCenter, yGaussCenter, widthGauss, area, l, b));
        rdindex++;
    }

    private PlotWindow readSpectData(InputStreamReader stream, String fileName, String directory) {
        optique = true;
        ArrayList xarray = new ArrayList();
        ArrayList yarray = new ArrayList();
        int cpt = 0;
        String[] sp;
        String s;
        boolean xx = false;
        try {

            BufferedReader bf = new BufferedReader(stream);
            String line = bf.readLine(); // header
            while (line != null) {
                line = bf.readLine();
                if (line == null) {
                    break;
                }
                //System.out.println("line " + line);
                // chiffres séparés part des espaces
                sp = line.split(" ");
                xx = false;
                for (int i = 0; i < sp.length; i++) {
                    s = sp[i];
                    if (s.length() > 0) {
                        //System.out.println(i + " " + s + " " + cpt);
                        if (!xx) {
                            xarray.add(Float.parseFloat(sp[i]));
                            xx = true;
                        } else {
                            yarray.add(Float.parseFloat(sp[i]));
                        }
                    }
                }
                cpt++;
            }
            double[] xtemp = new double[cpt];
            double[] ytemp = new double[cpt];
            for (int i = 0; i < cpt; i++) {
                xtemp[i] = ((Float) xarray.get(i)).floatValue();
                ytemp[i] = ((Float) yarray.get(i)).floatValue();
            }
            /////////////////// COMPUTE VALUES
            double tmp_wave, tmp_freq, tmp_freq0;
            int length = cpt;
            double[] xValues = new double[length];
            for (int i = 0; i < length; i++) {
                tmp_wave = xtemp[i] / 10; // A to nanometers
                tmp_freq = ((3.0E8 / (tmp_wave * 1.E-9))); // Hz
                //tmp_freq0 = (float) (3.e8 / 589.0e-9); // Hz; 589.6
                tmp_freq0 = (3.e17 / (wave_ref)); // Hz; 589.6
                switch (SCALETYPE) {
                    case pixel:
                        xValues[i] = (i);
                        break;
                    case velocity:
                        //xValues[length - 1 - i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001);
                        // thomas 19.09.06
                        xValues[i] = ((3.e8 * (tmp_freq0 / tmp_freq - 1.0)) * 1e-3);
                        break;
                    case frequency:
                        xValues[i] = ((tmp_freq) * 1e-12); // THz
                        break;
                    case wavelength:
                        xValues[i] = (tmp_wave);
                        break;
                    default:
                        xValues[i] = (i);
                        break;
                }
            }
            double[] yValues = new double[length];
            System.arraycopy(ytemp, 0, yValues, 0, length);

            /// LABELS
            switch (SCALETYPE) {
                case velocity:
                    //EU_HOU Bundle
                    xLabel = bun.getString("VelocityRS") + " (" + bun.getString("unitsVelocityRS") + ")";
                    break;
                case frequency:
                    //EU_HOU Bundle
                    // xLabel = bun.getString("FrequencyRS") + " (" + bun.getString("unitsFrequencyRS") + ")";
                    xLabel = bun.getString("FrequencyRS") + " (" + freqUnit + ")";
                    break;
                case wavelength:
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
            FloatProcessor ipdata = new FloatProcessor(cpt, 150);
            impdata.setProcessor("data", ipdata);
            FileInfo fi = new FileInfo();
            fi.fileName = fileName;
            fi.directory = directory;
            impdata.setFileInfo(fi);
            yLabel = bun.getString("IntensityRS");
            PlotWindow.RadioSpectra = true;
            PlotWindow.Base_Line_subtracted = false;
            PlotWindow.Base_Line = false;
            PlotWindow.ZERO_LINE = false;
            PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fileName, xLabel, yLabel, xValues, yValues, impdata);

            return pw;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private PlotWindow readFITS(String fileName, String directory) {
        ////////////////////// FITS
        IJ.log("Opening FITS spectrum " + directory + " " + fileName);
        FitsDecoder fd = new FitsDecoder(directory, fileName);
        FileInfo fi = null;

        try {
            fi = fd.getInfo();
        } catch (IOException e) {
        }
        System.out.print(fi.width + " " + fi.height + " " + fi.nImages);

        //if (fi != null && fi.width > 0 && fi.height == 1 && fi.offset > 0 && fi.nImages == 1) {
        if ((fi != null) && (fi.width > 0) && (fi.height > 0) && (fi.offset > 0) && (fi.nImages == 1)) {
            System.out.print(fi.width + " " + fi.height + " " + fi.nImages);
            fi.fileType = FileInfo.GRAY16_UNSIGNED;
            FileOpener fo = new FileOpener(fi);
            ImagePlus imp = fo.open(false);
            ImageProcessor ip = imp.getProcessor();
            ip.flipVertical();
            Calibration cal = imp.getCalibration();
            if (fi.height == 150) {
                optique = true;
            }

            double bzero = fd.getBzero();
            double bscale = fd.getBscale();

            imp.setProperty("Info", fd.getHeaderInfo());
            imp.setFileInfo(fi);
            int length;
            short[] shortpixels = null;
            float[] floatpixels = null;
            System.out.print(FileInfo.GRAY16_SIGNED);
            if ((fi.fileType == FileInfo.GRAY16_SIGNED) || (fi.fileType == FileInfo.GRAY16_UNSIGNED)) {
                shortpixels = (short[]) ip.getPixels();
                length = shortpixels.length;
            } else {
                floatpixels = (float[]) ip.getPixels();
                length = floatpixels.length;
            }
            //EU_HOU Bundle
            xLabel = IJ.getPluginBundle().getString("ChannelRS");
            /*
             * switch (SCALETYPE) { case velocity: //EU_HOU Bundle //xLabel
             * = bun.getString("VelocityRS") + " (" +
             * bun.getString("unitsVelocityRS") + ")"; xLabel =
             * bun.getString("VelocityRS") + "THZ"; break; case frequency:
             * //EU_HOU Bundle xLabel = bun.getString("FrequencyRS") + " ("
             * + bun.getString("unitsFrequencyRS") + ")"; xLabel =
             * bun.getString("FrequencyRS") + " (" +
             * bun.getString("unitsFrequencyRS") + ")"; break; case
             * wavelength: //EU_HOU Bundle xLabel =
             * bun.getString("WavelengthRS") + " (" +
             * bun.getString("unitsWavelengthRS") + ")"; break; default:
             * pixel: //EU_HOU Bundle xLabel = bun.getString("ChannelRS");
             * break; }
             */

            //EU_HOU Bundle
            yLabel = bun.getString("IntensityRS");

            l = fd.getCrval2();
            b = fd.getCrval3();

            // PASSER EN DOUBLE (TB)
            double[] xValues = new double[length];
            IJ.showProgress(0);

            double deltav;
            double restfreq = fd.getRestfreq();
            ///////////////////////////////////// RADIO /////////////////////
            if (!optique) {
                if (restfreq == 0.0) {
                    restfreq = fd.getCrval1();
                }
                if ((fd.getDeltav() == 0.0) & (SCALETYPE == velocity)) {
                    deltav = -3.0E8 * fd.getCdelt1() / restfreq; // SI m/s
                    System.out.println(deltav);
                } else {
                    deltav = fd.getDeltav();
                }
                if (fd.getTelescop().startsWith("LAB-")) { // Lab 11/12/12 ALM -- data stored in VELO-LSR(=CTYPE1)
                    for (int i = 0; i < length; i++) {
                        switch (SCALETYPE) {
                            case pixel:
                                xValues[i] = (i);
                                break;
                            case velocity:
                                xValues[i] = (((fd.getCrval1() + fd.getCdelt1() * ((i)) - fd.getCrpix1())) * 0.001);//km/s
                                break;
                            case frequency:
                                xValues[i] = (restfreq * (1.e0 - ((fd.getCrval1() + fd.getCdelt1() * (((i)) - fd.getCrpix1()))) / 3.0e8) * 1.e-6);//MHz
                                break;
                            case wavelength:
                                ///////////////////////////////////////////////
                                xValues[i] = (3.0E+10 / restfreq / (1. - (fd.getCrval1() + fd.getCdelt1() * ((i) - fd.getCrpix1())) / 3.e8));//cm
                                break;
                            default:
                                xValues[i] = (i);
                                break;
                        }
                    }

                } else { // SRT or Onsala 11/12/12 ALM
                    for (int i = 0; i < length; i++) {
                        switch (SCALETYPE) {
                            case pixel:
                                xValues[i] = (float) (i);
                                break;
                            case velocity:
                                //xValues[length - 1 - i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001);
                                // thomas 19.09.06
                                if (fd.getVelolsr() > 100) // m/s
                                {
                                    xValues[length - 1 - i] = (((((i) - fd.getCrpix1())) * deltav - fd.getVelolsr()) * 0.001 * 1.0f + (3.0E8 * (1.0f - fd.getCrval1() / restfreq)) * 0.001); //km/s
                                }
                                if (fd.getVelolsr() <= 100) //km/s
                                {
                                    xValues[length - 1 - i] = (((((i) - fd.getCrpix1())) * deltav * 0.001 - fd.getVelolsr()) * 1.0f + (3.0E8 * (1.0f - fd.getCrval1() / restfreq)) * 0.001); //km/s
                                }
                                break;
                            case frequency:
                                if (fd.getVelolsr() > 100) // m/s
                                {
                                    xValues[i] = (((fd.getCrval1() + fd.getCdelt1() * ((i) - fd.getCrpix1())) * 0.000001) + fd.getVelolsr() * restfreq / 3.e8 * 1.e-6);//MHz
                                }
                                if (fd.getVelolsr() <= 100) // km/s
                                {
                                    xValues[i] = (((fd.getCrval1() + fd.getCdelt1() * ((i) - fd.getCrpix1())) * 0.000001) + fd.getVelolsr() * 1.e3 * restfreq / 3.e8 * 1.e-6);//MHz
                                }
                                break;
                            case wavelength:
                                ///////////////////////////////////////////////
                                //xValues[i] = (float) (fd.getCrval1() - (float) (i));
                                if (fd.getVelolsr() > 100) // m/s
                                {
                                    xValues[i] = (3.0E+10 / (fd.getCrval1() + fd.getCdelt1() * (fd.getCrpix1() - (i)) + fd.getVelolsr() * restfreq / 3.e8));//cm
                                }
                                if (fd.getVelolsr() <= 100) //km/s
                                {
                                    xValues[i] = (3.0E+10 / (fd.getCrval1() + fd.getCdelt1() * (fd.getCrpix1() - (i)) + fd.getVelolsr() * 1.e3 * restfreq / 3.e8));//cm
                                }
                                break;
                            default:
                                xValues[i] = (i);
                                break;
                        }
                    }
                }
                double[] yValues = new double[length];
                double[] coeff = cal.getCoefficients();
                if (coeff == null) {
                    coeff = new double[2];
                    coeff[0] = 0.0;
                    coeff[1] = 1.0;
                }
                bzero = bscale * coeff[0] + bzero;
                //coeff[1]*fd.getBzero()+coeff[0];
                bscale = coeff[1] * bscale;

                if ((fi.fileType == FileInfo.GRAY16_SIGNED) || (fi.fileType == FileInfo.GRAY16_UNSIGNED)) {
                    for (int i = 0; i < length; i++) {
                        // thomas 19.09.06 ajout velocity alm 10/12/12 modif
                        if (fd.getTelescop().startsWith("LAB-")) { // LAB 11/12/12 ALM -- data stored in VELO-LSR
                            if ((SCALETYPE == wavelength) || (SCALETYPE == velocity)) {
                                yValues[length - 1 - i] = (float) (bscale * (shortpixels[i]) + bzero);
                            } else {
                                yValues[i] = (float) (bscale * (shortpixels[i]) + bzero);
                            }
                        } // srt
                        else {
                            if ((SCALETYPE == wavelength) || (SCALETYPE == velocity)) {
                                yValues[length - 1 - i] = (float) (bscale * (shortpixels[i]) + bzero);
                            } else {
                                yValues[i] = (float) (bscale * (shortpixels[i]) + bzero);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < length; i++) {
                        //if (SCALETYPE == wavelength) {
                        //   yValues[length - 1 - i] = (float) (bscale * (floatpixels[i]) + bzero);
                        //}
                        yValues[i] = (float) (bscale * (floatpixels[i]) + bzero);
                    }
                }
////////////////////////// Test Spectrum
//                    SpectrumRadio spectrum = new SpectrumRadio(xValues, yValues);
//                    spectrum.setFd(fd);
//                    spectrum.setDisplay(Spectrum.DISPLAY_DEFAULT);
//                    PlotWindow pwtest = spectrum.plot();
//                    pwtest.draw();
//
//                    SpectrumRadio spectrum2 = new SpectrumRadio(xValues, yValues);
//                    spectrum2.setFd(fd);
//                    spectrum2.setDisplay(Spectrum.DISPLAY_VELOCITY);
//                    PlotWindow pwtest2 = spectrum2.plot();
//                    pwtest2.draw();
////////////////////////// Test Spectrum                    

                /// LABELS
                freqUnit = "MHz";
                waveUnit = "cm";
                switch (SCALETYPE) {
                    case velocity:
                        //EU_HOU Bundle
                        xLabel = bun.getString("VelocityRS") + " (" + bun.getString("unitsVelocityRS") + ")";
                        break;
                    case frequency:
                        //EU_HOU Bundle
                        // xLabel = bun.getString("FrequencyRS") + " (" + bun.getString("unitsFrequencyRS") + ")";
                        xLabel = bun.getString("FrequencyRS") + " (" + freqUnit + ")";
                        break;
                    case wavelength:
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

                ////////////////////////// Test Spectrum
//                    SpectrumRadio spectrum = new SpectrumRadio(xValues, yValues);
//                    spectrum.setFd(fd);
//                    spectrum.setDisplay(Spectrum.DISPLAY_DEFAULT);
//                    PlotWindow pwtest = spectrum.plot();
//                    pwtest.draw();
//
//                    SpectrumRadio spectrum2 = new SpectrumRadio(xValues, yValues);
//                    spectrum2.setFd(fd);
//                    spectrum2.setDisplay(Spectrum.DISPLAY_VELOCITY);
//                    PlotWindow pwtest2 = spectrum2.plot();
//                    pwtest2.draw();
////////////////////////// Test Spectrum     
                ImagePlus impdata = new ImagePlus();
                FloatProcessor proc = new FloatProcessor(1, 1);
                impdata.setProcessor("spectrum", proc);
                fi.directory = directory;
                fi.fileName = fileName;
                impdata.setFileInfo(fi);

                PlotWindow.RadioSpectra = true;
                PlotWindow.Base_Line_subtracted = false;
                PlotWindow.Base_Line = false;
                PlotWindow.ZERO_LINE = false;
                //EU_HOU Bundle
                //IJ.log("spectre info " + impdata + " " + impdata.getOriginalFileInfo());
                PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fileName, xLabel, yLabel, xValues, yValues, impdata);

                return pw;

                //               pw.draw();
                //Plot pw = new Plot(IJ.getBundle().getString("PlotWinTitle") + "  " + fileName, xLabel, yLabel, xValues, yValues, null, null);
                //pw.show();
            }
///////////////////////////// OPTIQUE ///////////////////////////////////////
            if (optique) {
                length = fi.width;

                xValues = new double[length];
                if (restfreq == 0.0) {
                    restfreq = 3.0E8 / (fd.getCrval1() * 1.e-10);
                }
                if ((fd.getDeltav() == 0.0) & (SCALETYPE == velocity)) {
                    deltav = -fd.getCdelt1() / restfreq;

                } else {
                    deltav = fd.getDeltav();
                }
                double tmp_wave, tmp_freq, tmp_freq0;
                for (int i = 0; i < length; i++) {

                    tmp_wave = (fd.getCrval1() + fd.getCdelt1() * ((i))) * 0.1f; // nanometers
                    tmp_freq = ((3.0E8 / (tmp_wave * 1.E-9))); // Hz
                    //tmp_freq0 = (float) (3.e8 / 589.0e-9); // Hz; 589.6
                    tmp_freq0 = (3.e17 / (wave_ref)); // Hz; 589.6
                    switch (SCALETYPE) {
                        case pixel:
                            xValues[i] = (i);
                            break;
                        case velocity:
                            //xValues[length - 1 - i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001);
                            // thomas 19.09.06
                            xValues[i] = ((3.e8 * (tmp_freq0 / tmp_freq - 1.0)) * 1e-3);
                            break;
                        case frequency:
                            xValues[i] = ((tmp_freq) * 1e-12); // THz
                            break;
                        case wavelength:
                            xValues[i] = (tmp_wave);
                            break;
                        default:
                            xValues[i] = (i);
                            break;
                    }
                }

                double[] yValues = new double[length];

                double[] coeff = cal.getCoefficients();
                if (coeff == null) {
                    coeff = new double[2];
                    coeff[0] = 0.0;
                    coeff[1] = 1.0;

                }
                // ??
                bscale = 2 * fi.width;
                bzero = bscale * coeff[0] + bzero;
                //coeff[1]*fd.getBzero()+coeff[0];
                bscale = coeff[1] * bscale;

                // TB
                bzero = 32768;
                bscale = 1;

                if ((fi.fileType == FileInfo.GRAY16_SIGNED) || (fi.fileType == FileInfo.GRAY16_UNSIGNED)) {
                    for (int i = 0; i < length; i++) {
                            // thomas 19.09.06 ajout velocity
                        // if ((SCALETYPE == wavelength) || (SCALETYPE == velocity)) {

                        //   yValues[length - 1 - i] = (float) (bscale * (shortpixels[i]) + bzero);
                        //} else
                        yValues[i] = (bzero + bscale * shortpixels[i]);

                    }
                } else {
                    for (int i = 0; i < length; i++) {
                        //if (SCALETYPE == wavelength) {
                        //    yValues[length - 1 - i] = (float)  (floatpixels[i]) ;
                        //}else{
                        yValues[i] = (bzero + bscale * floatpixels[i]);
                    }
                }

//                    // Test Spectrum
//                    SpectrumOptical spectrum = new SpectrumOptical(xValues, yValues);
//                    spectrum.setFd(fd);
//                    spectrum.setWave_ref(wave_ref);
//                    spectrum.setDisplay(Spectrum.DISPLAY_DEFAULT);
//                    PlotWindow pwtest = spectrum.plot();
//                    pwtest.draw();
//                    spectrum.setDisplay(Spectrum.DISPLAY_WAVELENGTH);
//                    PlotWindow pwtest2 = spectrum.plot();
//                    pwtest2.draw();
                /// LABELS
                switch (SCALETYPE) {
                    case velocity:
                        //EU_HOU Bundle
                        xLabel = bun.getString("VelocityRS") + " (" + bun.getString("unitsVelocityRS") + ")";
                        break;
                    case frequency:
                        //EU_HOU Bundle
                        // xLabel = bun.getString("FrequencyRS") + " (" + bun.getString("unitsFrequencyRS") + ")";
                        xLabel = bun.getString("FrequencyRS") + " (" + freqUnit + ")";
                        break;
                    case wavelength:
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

                PlotWindow.RadioSpectra = true;
                PlotWindow.Base_Line_subtracted = false;
                PlotWindow.Base_Line = false;
                PlotWindow.ZERO_LINE = false;
                //EU_HOU Bundle
                PlotWindow pw = new PlotWindow(IJ.getBundle().getString("PlotWinTitle") + "  " + fileName, xLabel, yLabel, xValues, yValues, imp);

                return pw;

                // pw.draw();
            }//optique
        } else {
            //EU_HOU Bundle
            IJ.log("Error FITS decoder ");
            IJ.error(bun.getString("error0"));
        }

        IJ.showStatus("");

        return null;
    }
}
