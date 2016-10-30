package ij.process;

import ij.*;
import ij.gui.*;
import ij.text.*;
import ij.measure.*;
import java.awt.event.*;
import java.util.*;
import java.lang.Math;

/**
 * @author omar
 */
public class Photometer implements MouseListener, WindowListener {

    public final static short STAR_RAD = 1;
    public final static short SKY_RAD = 1 << 1;
    public final static short SKY_VAL = 1 << 2;
    public final static short SKY_STAR = 1 << 3;
    public final static short COORD = 1 << 4;
    private short exec = 0;
    private int x = -1, y = -1;
    private int slic = -1;
    private ImageStatistics stat, istat;
    private OvalRoi out, in;
    private float skyval;
    private static TextWindow res = null;
    private static Photometer instance = null;
    private static int phindex = 1;
    private ImagePlus imp;//EU_HOU rb
    private static IPhot iphot;
    private static PhotometerSave PS;

    /**
     * Constructor for the Photometer object
     */
    public Photometer() {

        res = new TextWindow(IJ.getBundle().getString("Photometer"), IJ.getBundle().getString("PhotometerLabels"), null, 600, 200, false);
        res.setPhotometry(true);
        if (PhotometerSave.getInstance() == null) {
            PS = new PhotometerSave();
        } else {
            PS = PhotometerSave.getInstance();
        }

        IJ.register(Photometer.class);

        instance = this;
        if (PS.results.size() > 0) {
            StringBuffer sb;
            if (PS.results.size() > 0) {
                Enumeration keys = PS.results.keys();
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
                    IPhot res = (IPhot) PS.results.get(String.valueOf(j));
                    if (res != null) {
                        sb.append(j + "\t" + res.toString() + "\n");
                    }
                }

            } else {
                sb = null;
            }
            if (res == null) {
                res = new TextWindow(IJ.getBundle().getString("Photometer"), IJ.getBundle().getString("PhotometerLabels"), null, 600, 200, false);
                res.setPhotometry(true);
            }
            res.append(new String(sb));
        }
        this.activate();
    }

    /**
     * Gets the instance attribute of the Photometer class
     *
     * @return The instance value
     */
    public static Photometer getInstance() {
        return instance;
    }
    
    public PhotometerSave getPS(){
        return PS;
    }

    public static void activate() {
        WindowManager.activateWindow(IJ.getBundle().getString("Photometer"));
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void mousePressed(MouseEvent e) {
        if (instance != null) {

            short op = 0;
            PhotometerParams pp = PhotometerParams.getInstance();

            if (pp != null) {
                op = pp.getStatus();
            }

            int tool = Toolbar.getToolId();

            if (((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) || (tool == Toolbar.MAGNIFIER) || (tool == Toolbar.HAND)) {
                return;
            }

            stat = null;
            istat = null;
            x = e.getX();
            y = e.getY();

            ImageCanvas canvas;
            //EU_HOU rb ImagePlus imp;
            try {
                canvas = (ImageCanvas) e.getSource();
                x = canvas.offScreenX(x);
                y = canvas.offScreenY(y);
                imp = canvas.getImage();

                if ((imp.getType() != ImagePlus.GRAY8) && (imp.getType() != ImagePlus.GRAY16) && (imp.getType() != ImagePlus.GRAY32)) {
                    //EU_HOU Bundle
                    IJ.error(IJ.getBundle().getString("TypeReqErr") + IJ.getBundle().getString("Grayscale"));
                    return;
                }
            } catch (Exception ex) {
                return;
            }
            if (imp.getStackSize() > 1) {
                slic = imp.getCurrentSlice();
            }

            Calibration cal = imp.getCalibration();




            int anul_rad = 8; //recherche la zone pixels maximum à 10 pixel autour du pixel cliké (boite de 16 sur 16 pixels)
            int rad = 2; //zone pixels prise en compte (boite de 5 sur 5 pixels)
            int itab[];
            float tab[];
            int maxind;
            int imwidth = imp.getProcessor().getWidth();
            int imheight = imp.getProcessor().getHeight();

            // DETERMINATION DU CENTRE DE L'OBJET
            if ((op & COORD) == 0) {
                double pixM = 0;
                double pixL = 0;
                int valnul = 0;
                for (int ii = x - anul_rad; ii <= x + anul_rad; ++ii) {
                    for (int jj = y - anul_rad; jj <= y + anul_rad; ++jj) {
                        pixL = 0;
                        valnul = 0;
                        for (int i = ii - rad; i <= ii + rad; ++i) {
                            for (int j = jj - rad; j <= jj + rad; ++j) {
                                if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                                    pixL += (double) cal.getCValue(imp.getPixel(i, j)[0]);
                                } else {
                                    ++valnul;
                                }
                            }
                        }
                        pixL /= (2 * rad + 1) * (2 * rad + 1) - valnul;
                        if (pixL > pixM) {
                            pixM = pixL;
                            x = ii;
                            y = jj;
                        }
                    }
                }


            } else {
                try {
                    int t[] = PhotometerParams.getInstance().getCoord();

                    x = t[0];
                    y = t[1];
                } catch (NumberFormatException exc) {
                    //EU_HOU Bundle
                    IJ.error("CoordFmt");
                    return;
                }
            }

            // GET SKY VALUE
            // 1/ get all pixel values at range 50 pixel away from centre
            anul_rad = 50;
            tab = new float[2 * anul_rad * 2 * anul_rad];
            int raduis[] = new int[4];
            if (imp.getType() != ImagePlus.GRAY32) {
                for (int ii = 0; ii < 2 * anul_rad; ++ii) {
                    for (int jj = 0; jj < 2 * anul_rad; ++jj) {
                        int x1 = x - anul_rad + ii;
                        int y1 = y - anul_rad + jj;
                        if (x1 >= 0 && y1 >= 0 && x1 < imwidth && y1 < imheight) {
                            tab[ii * 2 * anul_rad + jj] = imp.getPixel(x1, y1)[0];
                        } else {
                            tab[ii * 2 * anul_rad + jj] = Float.MIN_VALUE;
                        }
                    }
                }
            } else {
                FloatProcessor ip = (FloatProcessor) imp.getProcessor();
                for (int ii = 0; ii < 2 * anul_rad; ++ii) {
                    for (int jj = 0; jj < 2 * anul_rad; ++jj) {
                        int x1 = x - anul_rad + ii;
                        int y1 = y - anul_rad + jj;
                        if (x1 >= 0 && y1 >= 0 && x1 < imwidth && y1 < imheight) {
                            tab[(ii * 2 * anul_rad) + jj] = Float.intBitsToFloat(ip.getPixel(x1, y1));
                        } else {
                            tab[(ii * 2 * anul_rad) + jj] = Float.MIN_VALUE;
                        }
                    }
                }
            }

            int ii;
            int jj;
            //2/ extract mediane frequent value
            java.util.Arrays.sort(tab);

            ii = 0;
            while (tab[ii] <= Float.MIN_VALUE) {
                ++ii;
            }

            /*
             * //extract most frequent value
             *
             * itab = new int[2*anul_rad * 2*anul_rad]; long maxval = (long)
             * tab[ii]; for (jj = ii; jj < 2*anul_rad * 2*anul_rad; ++jj) { if
             * ((long) tab[jj] > maxval+1) { ii = jj; maxval = (long) tab[ii]; }
             * else ++itab[ii]; }
             *
             * maxind = 0; for (jj = 0; jj <= ii; ++jj) { if (itab[jj] >
             * itab[maxind]) { maxind = jj; }
             *
             *
             * }
             */


            int medval = (2 * anul_rad * 2 * anul_rad - 1 - ii) / 2;

            float median = tab[medval];

            maxind = medval;


            //  CALC FHWM, STAR RADIUS, SKY RADIUS
            itab = new int[5];
            if (imp.getType() != ImagePlus.GRAY32) {
                skyval = (float) cal.getCValue(tab[maxind]);

                double cval = 0;
                int valnul = 0;
                for (int i = x - rad; i <= x + rad; ++i) {
                    for (int j = y - rad; j <= y + rad; ++j) {
                        if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                            cval += (Double) cal.getCValue(imp.getPixel(i, j)[0]);
                        } else {
                            ++valnul;
                        }
                    }
                }
                cval = (cval / ((2 * rad + 1) * (2 * rad + 1) - valnul) - skyval) / 2;
                double sval = cval + skyval;




                int ix1 = x;
                while ((ix1 < imwidth) && ((float) cal.getCValue(imp.getPixel(ix1, y)[0]) > sval)) {
                    ++ix1;
                }
                raduis[0] = ix1 - x;
                int ix2 = x;
                while ((ix2 >= 0) && ((float) cal.getCValue(imp.getPixel(ix2, y)[0]) > sval)) {
                    --ix2;
                }
                raduis[1] = x - ix2;
                int iy1 = y;
                while ((iy1 < imheight) && ((float) cal.getCValue(imp.getPixel(x, iy1)[0]) > sval)) {
                    ++iy1;
                }
                raduis[2] = iy1 - y;
                int iy2 = y;
                while ((iy2 >= 0) && ((float) cal.getCValue(imp.getPixel(x, iy2)[0]) > sval)) {
                    --iy2;
                }
                raduis[3] = y - iy2;

                //recherche plus grand rayon
                java.util.Arrays.sort(raduis);
                //pas de rayon négatif ou nul et pas de rayon exorbitant
                if (raduis[3] <= 0) {
                    raduis[3] = 4;
                }
                if (raduis[3] >= 10) {
                    raduis[3] = 8;
                }

            } else {
                FloatProcessor ip = (FloatProcessor) imp.getProcessor();
                //valeur du ciel
                skyval = tab[maxind];
                //calcul de la valeur de la largeur à mi hauteur
                double cval = 0;
                int valnul = 0;
                for (int i = x - rad; i <= x + rad; ++i) {
                    for (int j = y - rad; j <= y + rad; ++j) {
                        if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                            cval += Float.intBitsToFloat(ip.getPixel(i, j));
                        } else {
                            ++valnul;
                        }
                    }
                }
                cval = (cval / ((2 * rad + 1) * (2 * rad + 1) - valnul) - skyval) / 2;
                double sval = cval + skyval;
                //calcul du rayon à la mi hauteur
                //recherche du rayon (a mi hauteur) dans les 2 directions suivant les 2axes
                int ix1 = x;
                while ((ix1 < imwidth) && (Float.intBitsToFloat(ip.getPixel(ix1, y)) > sval)) {
                    ++ix1;
                }
                raduis[0] = ix1 - x;
                int ix2 = x;
                while ((ix2 >= 0) && (Float.intBitsToFloat(ip.getPixel(ix2, y)) > sval)) {
                    --ix2;
                }
                raduis[1] = x - ix2;
                int iy1 = y;
                while ((iy1 < imheight) && ((Float.intBitsToFloat(ip.getPixel(x, iy1))) > sval)) {
                    ++iy1;
                }
                raduis[2] = iy1 - y;
                int iy2 = y;
                while ((iy2 >= 0) && (Float.intBitsToFloat(ip.getPixel(x, iy2)) > sval)) {
                    --iy2;
                }
                raduis[3] = y - iy2;

                //recherche plus grand rayon
                java.util.Arrays.sort(raduis);
                //pas de rayon négatif ou nul et pas de rayon exorbitant
                if (raduis[3] <= 0) {
                    raduis[3] = 4;
                }
                if (raduis[3] >= 10) {
                    raduis[3] = 8;
                }

            }


            int StarRaduis = 0;
            int SkyRadius = 0;
            //StarRaduis => calculé ou fixé via PhotometerParams
            if ((op & STAR_RAD) == 0) {
                StarRaduis = raduis[3] * 4 + 2; // take Fhwm x 4 + 2 pixels
            } else {
                try {
                    StarRaduis = (int) PhotometerParams.getInstance().getVal(STAR_RAD);
                } catch (NumberFormatException nfe) {
                    //EU_HOU Bundle
                    IJ.error("StarRadFmt");
                    StarRaduis = raduis[3] * 4 + 2;
                }
            }
            //SkyRaduis => calculé ou fixé via PhotometerParams
            if ((op & SKY_RAD) == 0) {
                if ((op & SKY_STAR) == 0) {
                    SkyRadius = (int) (1.5 * StarRaduis);
                } else {
                    SkyRadius = (int) (1.5 * StarRaduis);
                }
            } else {
                try {
                    SkyRadius = (int) PhotometerParams.getInstance().getVal(SKY_RAD);
                } catch (NumberFormatException nfe) {
                    //EU_HOU Bundle
                    IJ.error("SkyRadFmt");
                    SkyRadius = (int) (1.5 * StarRaduis);
                }
            }

            // FINAL CALCULATION
            //out=>cercle du ciel
            out = new OvalRoi(x - SkyRadius, y - SkyRadius, 2 * SkyRadius, 2 * SkyRadius);
            //in=>cercle de l'étoile
            in = new OvalRoi(x - StarRaduis, y - StarRaduis, 2 * StarRaduis, 2 * StarRaduis);


            imp.setRoi(out);
            stat = imp.getStatistics();
            //System.out.println(stat.mean+" "+stat.mode+" "+stat.min+" "+stat.max+" "+stat.area);


            imp.setRoi(in);
            istat = imp.getStatistics();
            //System.out.println(istat.mean+" "+istat.mode+" "+istat.min+" "+istat.max+" "+istat.area);


            if ((op & SKY_VAL) == 0) {
                stat.mean = (stat.mean * stat.pixelCount - istat.mean * istat.pixelCount) / (stat.pixelCount - istat.pixelCount);
            } else {
                try {
                    stat.mean = PhotometerParams.getInstance().getVal(SKY_VAL);
                } catch (NumberFormatException nfe) {
                    //EU_HOU Bundle
                    IJ.error("SkyValFmt");
                    stat.mean = (stat.mean * stat.pixelCount - istat.mean * istat.pixelCount) / (stat.pixelCount - istat.pixelCount);
                }
            }
            istat.mean = (istat.mean - stat.mean) * istat.pixelCount;


            //passage de parametre pour la console setting
            if (PhotometerParams.getInstance() != null) {
                PhotometerParams.getInstance().setX(x);
                PhotometerParams.getInstance().setY(y);
                PhotometerParams.getInstance().setstatf(StarRaduis);
                PhotometerParams.getInstance().setradtf(SkyRadius);
                PhotometerParams.getInstance().setvaltf((int) stat.mean);
                PhotometerParams.getInstance().setIMP(imp);
            }
        }
    }

    public void computePhotometry(ImagePlus imp, int x, int y) {

        short op = 0;
        PhotometerParams pp = PhotometerParams.getInstance();

        if (pp != null) {
            op = pp.getStatus();
        }

        if (imp.getStackSize() > 1) {
            slic = imp.getCurrentSlice();
        }

        Calibration cal = imp.getCalibration();




        int anul_rad = 8; //recherche la zone pixels maximum à 10 pixel autour du pixel cliké (boite de 16 sur 16 pixels)
        int rad = 2; //zone pixels prise en compte (boite de 5 sur 5 pixels)
        int itab[];
        float tab[];
        int maxind;
        int imwidth = imp.getProcessor().getWidth();
        int imheight = imp.getProcessor().getHeight();

        // DETERMINATION DU CENTRE DE L'OBJET
        if ((op & COORD) == 0) {
            double pixM = 0;
            double pixL = 0;
            int valnul = 0;
            for (int ii = x - anul_rad; ii <= x + anul_rad; ++ii) {
                for (int jj = y - anul_rad; jj <= y + anul_rad; ++jj) {
                    pixL = 0;
                    valnul = 0;
                    for (int i = ii - rad; i <= ii + rad; ++i) {
                        for (int j = jj - rad; j <= jj + rad; ++j) {
                            if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                                pixL += (double) cal.getCValue(imp.getPixel(i, j)[0]);
                            } else {
                                ++valnul;
                            }
                        }
                    }
                    pixL /= (2 * rad + 1) * (2 * rad + 1) - valnul;
                    if (pixL > pixM) {
                        pixM = pixL;
                        x = ii;
                        y = jj;
                    }
                }
            }


        } else {
            try {
                int t[] = PhotometerParams.getInstance().getCoord();

                x = t[0];
                y = t[1];
            } catch (NumberFormatException exc) {
                //EU_HOU Bundle
                IJ.error("CoordFmt");
                return;
            }
        }

        // GET SKY VALUE
        // 1/ get all pixel values at range 50 pixel away from centre
        anul_rad = 50;
        tab = new float[2 * anul_rad * 2 * anul_rad];
        int raduis[] = new int[4];
        if (imp.getType() != ImagePlus.GRAY32) {
            for (int ii = 0; ii < 2 * anul_rad; ++ii) {
                for (int jj = 0; jj < 2 * anul_rad; ++jj) {
                    int x1 = x - anul_rad + ii;
                    int y1 = y - anul_rad + jj;
                    if (x1 >= 0 && y1 >= 0 && x1 < imwidth && y1 < imheight) {
                        tab[ii * 2 * anul_rad + jj] = imp.getPixel(x1, y1)[0];
                    } else {
                        tab[ii * 2 * anul_rad + jj] = Float.MIN_VALUE;
                    }
                }
            }
        } else {
            FloatProcessor ip = (FloatProcessor) imp.getProcessor();
            for (int ii = 0; ii < 2 * anul_rad; ++ii) {
                for (int jj = 0; jj < 2 * anul_rad; ++jj) {
                    int x1 = x - anul_rad + ii;
                    int y1 = y - anul_rad + jj;
                    if (x1 >= 0 && y1 >= 0 && x1 < imwidth && y1 < imheight) {
                        tab[(ii * 2 * anul_rad) + jj] = Float.intBitsToFloat(ip.getPixel(x1, y1));
                    } else {
                        tab[(ii * 2 * anul_rad) + jj] = Float.MIN_VALUE;
                    }
                }
            }
        }

        int ii;
        int jj;
        //2/ extract mediane frequent value
        java.util.Arrays.sort(tab);

        ii = 0;
        while (tab[ii] <= Float.MIN_VALUE) {
            ++ii;
        }

        /*
         * //extract most frequent value
         *
         * itab = new int[2*anul_rad * 2*anul_rad]; long maxval = (long)
         * tab[ii]; for (jj = ii; jj < 2*anul_rad * 2*anul_rad; ++jj) { if
         * ((long) tab[jj] > maxval+1) { ii = jj; maxval = (long) tab[ii]; }
         * else ++itab[ii]; }
         *
         * maxind = 0; for (jj = 0; jj <= ii; ++jj) { if (itab[jj] >
         * itab[maxind]) { maxind = jj; }
         *
         *
         * }
         */


        int medval = (2 * anul_rad * 2 * anul_rad - 1 - ii) / 2;

        float median = tab[medval];

        maxind = medval;


        //  CALC FHWM, STAR RADIUS, SKY RADIUS
        itab = new int[5];
        if (imp.getType() != ImagePlus.GRAY32) {
            skyval = (float) cal.getCValue(tab[maxind]);

            double cval = 0;
            int valnul = 0;
            for (int i = x - rad; i <= x + rad; ++i) {
                for (int j = y - rad; j <= y + rad; ++j) {
                    if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                        cval += (Double) cal.getCValue(imp.getPixel(i, j)[0]);
                    } else {
                        ++valnul;
                    }
                }
            }
            cval = (cval / ((2 * rad + 1) * (2 * rad + 1) - valnul) - skyval) / 2;
            double sval = cval + skyval;




            int ix1 = x;
            while ((ix1 < imwidth) && ((float) cal.getCValue(imp.getPixel(ix1, y)[0]) > sval)) {
                ++ix1;
            }
            raduis[0] = ix1 - x;
            int ix2 = x;
            while ((ix2 >= 0) && ((float) cal.getCValue(imp.getPixel(ix2, y)[0]) > sval)) {
                --ix2;
            }
            raduis[1] = x - ix2;
            int iy1 = y;
            while ((iy1 < imheight) && ((float) cal.getCValue(imp.getPixel(x, iy1)[0]) > sval)) {
                ++iy1;
            }
            raduis[2] = iy1 - y;
            int iy2 = y;
            while ((iy2 >= 0) && ((float) cal.getCValue(imp.getPixel(x, iy2)[0]) > sval)) {
                --iy2;
            }
            raduis[3] = y - iy2;

            //recherche plus grand rayon
            java.util.Arrays.sort(raduis);
            //pas de rayon négatif ou nul et pas de rayon exorbitant
            if (raduis[3] <= 0) {
                raduis[3] = 4;
            }
            if (raduis[3] >= 10) {
                raduis[3] = 8;
            }

        } else {
            FloatProcessor ip = (FloatProcessor) imp.getProcessor();
            //valeur du ciel
            skyval = tab[maxind];
            //calcul de la valeur de la largeur à mi hauteur
            double cval = 0;
            int valnul = 0;
            for (int i = x - rad; i <= x + rad; ++i) {
                for (int j = y - rad; j <= y + rad; ++j) {
                    if (i >= 0 && i < imwidth && j >= 0 && j < imheight) {
                        cval += Float.intBitsToFloat(ip.getPixel(i, j));
                    } else {
                        ++valnul;
                    }
                }
            }
            cval = (cval / ((2 * rad + 1) * (2 * rad + 1) - valnul) - skyval) / 2;
            double sval = cval + skyval;
            //calcul du rayon à la mi hauteur
            //recherche du rayon (a mi hauteur) dans les 2 directions suivant les 2axes
            int ix1 = x;
            while ((ix1 < imwidth) && (Float.intBitsToFloat(ip.getPixel(ix1, y)) > sval)) {
                ++ix1;
            }
            raduis[0] = ix1 - x;
            int ix2 = x;
            while ((ix2 >= 0) && (Float.intBitsToFloat(ip.getPixel(ix2, y)) > sval)) {
                --ix2;
            }
            raduis[1] = x - ix2;
            int iy1 = y;
            while ((iy1 < imheight) && ((Float.intBitsToFloat(ip.getPixel(x, iy1))) > sval)) {
                ++iy1;
            }
            raduis[2] = iy1 - y;
            int iy2 = y;
            while ((iy2 >= 0) && (Float.intBitsToFloat(ip.getPixel(x, iy2)) > sval)) {
                --iy2;
            }
            raduis[3] = y - iy2;

            //recherche plus grand rayon
            java.util.Arrays.sort(raduis);
            //pas de rayon négatif ou nul et pas de rayon exorbitant
            if (raduis[3] <= 0) {
                raduis[3] = 4;
            }
            if (raduis[3] >= 10) {
                raduis[3] = 8;
            }

        }


        int StarRaduis = 0;
        int SkyRadius = 0;
        //StarRaduis => calculé ou fixé via PhotometerParams
        if ((op & STAR_RAD) == 0) {
            StarRaduis = raduis[3] * 4 + 2; // take Fhwm x 4 + 2 pixels
        } else {
            try {
                StarRaduis = (int) PhotometerParams.getInstance().getVal(STAR_RAD);
            } catch (NumberFormatException nfe) {
                //EU_HOU Bundle
                IJ.error("StarRadFmt");
                StarRaduis = raduis[3] * 4 + 2;
            }
        }
        //SkyRaduis => calculé ou fixé via PhotometerParams
        if ((op & SKY_RAD) == 0) {
            if ((op & SKY_STAR) == 0) {
                SkyRadius = (int) (1.5 * StarRaduis);
            } else {
                SkyRadius = (int) (1.5 * StarRaduis);
            }
        } else {
            try {
                SkyRadius = (int) PhotometerParams.getInstance().getVal(SKY_RAD);
            } catch (NumberFormatException nfe) {
                //EU_HOU Bundle
                IJ.error("SkyRadFmt");
                SkyRadius = (int) (1.5 * StarRaduis);
            }
        }

        // FINAL CALCULATION
        //out=>cercle du ciel
        out = new OvalRoi(x - SkyRadius, y - SkyRadius, 2 * SkyRadius, 2 * SkyRadius);
        //in=>cercle de l'étoile
        in = new OvalRoi(x - StarRaduis, y - StarRaduis, 2 * StarRaduis, 2 * StarRaduis);


        imp.setRoi(out);
        stat = imp.getStatistics();
        //System.out.println(stat.mean+" "+stat.mode+" "+stat.min+" "+stat.max+" "+stat.area);


        imp.setRoi(in);
        istat = imp.getStatistics();
        //System.out.println(istat.mean+" "+istat.mode+" "+istat.min+" "+istat.max+" "+istat.area);


        if ((op & SKY_VAL) == 0) {
            stat.mean = (stat.mean * stat.pixelCount - istat.mean * istat.pixelCount) / (stat.pixelCount - istat.pixelCount);
        } else {
            try {
                stat.mean = PhotometerParams.getInstance().getVal(SKY_VAL);
            } catch (NumberFormatException nfe) {
                //EU_HOU Bundle
                IJ.error("SkyValFmt");
                stat.mean = (stat.mean * stat.pixelCount - istat.mean * istat.pixelCount) / (stat.pixelCount - istat.pixelCount);
            }
        }
        istat.mean = (istat.mean - stat.mean) * istat.pixelCount;


        //passage de parametre pour la console setting
        if (PhotometerParams.getInstance() != null) {
            PhotometerParams.getInstance().setX(x);
            PhotometerParams.getInstance().setY(y);
            PhotometerParams.getInstance().setstatf(StarRaduis);
            PhotometerParams.getInstance().setradtf(SkyRadius);
            PhotometerParams.getInstance().setvaltf((int) stat.mean);
            PhotometerParams.getInstance().setIMP(imp);
        }
        String t;
        try {
            t = IJ.getImage().getTitle();
        } catch (Exception ex) {
            return;
        }
        
         try {
            String message = "";
            Double sqrt_stat_mean;
            if (stat.mean < 0) {
                sqrt_stat_mean = Math.sqrt(-stat.mean);
            } else {
                sqrt_stat_mean = Math.sqrt(stat.mean);
            }
            if (istat.mean <= Math.sqrt(stat.mean)) {
                message = IJ.getBundle().getString("PhotometerWarningMesure");
            }


            PS.s2 = "\t" + t + "\t" + x + "\t" + y + "\t" + (int) istat.mean + "\t" + in.width() / 2 + "\t" + (int) stat.mean + "\t" + out.width() / 2 + "\t" + message;


            if (PS.s1.compareTo(PS.s2) != 0) {
                res.append(new String(phindex + PS.s2));
                res.getTextPanel().resetSelection();
                PS.results.put(String.valueOf(phindex), new IPhot(t, x, y, in, (int) istat.mean, (int) stat.mean, this));
                PS.s1 = PS.s2;
                phindex++;
            }

        } catch (Exception exp) {
            return;
        }
    }

    public int[] getCoord() {
        int t[] = new int[2];
        t[0] = x;
        t[1] = y;
        return t;
    }

    public ImagePlus getIMP() {
        return this.imp;
    }

    public void mouseReleased(MouseEvent e) {

        String t;
        int tool = Toolbar.getToolId();


        if ((tool == Toolbar.MAGNIFIER) || (tool == Toolbar.HAND)) {
            return;
        }

        try {
            t = ((ImageCanvas) (e.getSource())).getImage().getTitle();
        } catch (Exception ex) {
            return;
        }
        if ((stat == null) || (istat == null)) {
            //EU_HOU Bundle
            //IJ.error(IJ.getBundle().getString("PhotometryError"));
            return;
        }
        try {
            String message = "";
            Double sqrt_stat_mean;
            if (stat.mean < 0) {
                sqrt_stat_mean = Math.sqrt(-stat.mean);
            } else {
                sqrt_stat_mean = Math.sqrt(stat.mean);
            }
            if (istat.mean <= Math.sqrt(stat.mean)) {
                message = IJ.getBundle().getString("PhotometerWarningMesure");
            }


            PS.s2 = "\t" + t + "\t" + x + "\t" + y + "\t" + (int) istat.mean + "\t" + in.width() / 2 + "\t" + (int) stat.mean + "\t" + out.width() / 2 + "\t" + message;


            if (PS.s1.compareTo(PS.s2) != 0) {
                res.append(new String(phindex + PS.s2));
                res.getTextPanel().resetSelection();
                PS.results.put(String.valueOf(phindex), new IPhot(t, x, y, in, (int) istat.mean, (int) stat.mean, this));
                PS.s1 = PS.s2;
                phindex++;
            }

        } catch (Exception exp) {
            return;
        }

    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public void set(String s) {
        StringTokenizer st = new StringTokenizer(s, "\t");
        int index = Integer.parseInt(st.nextToken());
        IPhot resu = (IPhot) PS.results.get(String.valueOf(index));
        WindowManager.setCurrentWindow(resu.win);
        //try {

        resu.win.getImagePlus().setRoi(resu.r);
        //} catch (Exception ex) {;}

    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Description of the Method
     */
    public static void clear() {
        if (PS.results == null) {
            return;
        }
        PS.results.clear();
        phindex = 1;
        if (res != null) {
            res.getTextPanel().selectAll();
            res.getTextPanel().clearSelection();
        }
        Enumeration en = WindowManager.getImageWindows().elements();
        while (en.hasMoreElements()) {
            ((ImageWindow) en.nextElement()).getImagePlus().killRoi();
        }

        PS.s1 = "";
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
                WindowManager.activateWindow(img);
                WindowManager.getCurrentWindow().getImagePlus().killRoi();
                PS.results.remove(String.valueOf(index));
            } catch (Exception e) {
            }         
        }
    }

    /**
     * Description of the Method
     */
    public void close() {
    }

    /**
     * Sets the op attribute of the Photometer object
     *
     * @param op The new op value
     */
    public void setOp(short op) {
        exec = op;
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowActivated(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowClosing(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowClosed(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowDeactivated(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowDeiconified(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowIconified(WindowEvent e) {
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void windowOpened(WindowEvent e) {
    }

    public static void resetPh() {
        res = null;
        instance = null;
    }
}
