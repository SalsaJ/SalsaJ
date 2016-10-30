//EU_HOU
package ij.plugin;

//import java.awt.*;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.io.FileInfo;
import ij.io.FileOpener;
import ij.io.OpenDialog;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.io.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import nom.tam.fits.*;
import skyview.data.CoordinateFormatter;
import skyview.geometry.WCS;
// end by oli

/**
 */
public class FITS extends ImagePlus implements PlugIn {

    private static ResourceBundle bun = IJ.getPluginBundle(); //by oli
    private WCS wcs;
    private BasicHDU[] BHDU;
    private static FITS instance = null;

    /**
     * Main processing method for the FITS object
     *
     * @param arg Description of the Parameter
     */
    public void run(String arg) {
        {
            FileOutputStream fis = null;
            try {
                instance = this;
                wcs = null;
                ImagePlus imp = null;
                OpenDialog od = new OpenDialog("Open FITS...", arg);
                String directory = od.getDirectory();
                String fileName = od.getFileName();
                if (fileName == null) {
                    return;
                }
                IJ.showStatus("Opening: " + directory + fileName);
                IJ.log("Opening: " + directory + fileName);
                FitsDecoder fd = new FitsDecoder(directory, fileName);
                FileInfo fi = null;
                //IJ.log("fi =" + fi);
                try {
                    fi = fd.getInfo();
                } catch (IOException e) {
                }

                Fits myFits = new Fits(directory + fileName);
                // TEST TB 0612
                //myFits = new Fits("http://imagej.nih.gov/ij/images/m51.fits");
                //myFits=new Fits("http://www.snv.jussieu.fr/~wboudier/tmp/SMC-Cep-43522-1999-10-24-03-23-25.fits");
                BasicHDU[] bhdu = null;

                try {
                    bhdu = myFits.read();

                } catch (Exception e) {
                    // if nasa library does not work try classical ImageJ reading
                    FITS_Reader reader = new FITS_Reader();
                    reader.run(arg);
                    if (reader.getProcessor() != null) {
                        reader.show();
                    }
                    if (IJ.debug) {
                        System.out.println("PB reading fits");

                    }
                }
                int dim = bhdu[0].getAxes().length;

                try {
                    //By oli
                    // replace  bhdu[0].getAxes()[dim - 1] by fi.width
                    // and replace  bhdu[0].getAxes()[dim - 2] by fi.height
                    // or it would crash if it is not defined and go to the exception case at the end
                    int wi = 0;
                    int he = 0;
                    int de = 0;
                    if ((dim < 2) && (fi != null)) {
                        wi = fi.width;
                        he = fi.height;
                        de = 1;
                    } else {
                        wi = bhdu[0].getAxes()[dim - 1];
                        he = bhdu[0].getAxes()[dim - 2];
                        if (dim > 2) {
                            de = bhdu[0].getAxes()[dim - 3];
                        } else {
                            de = 1;
                        }
                    }
                    if (IJ.debug) {
                        System.out.println("Dim= " + dim + "  -- Height= " + he + " pixels -- Width= " + wi + " pixels");
                    }
                    //end by Oli

                    int bit = bhdu[0].getBitPix();
                    Header H = bhdu[0].getHeader();
                    String s = new String("");
                    if (bhdu[0].getObject() != null) {
                        s += IJ.getBundle().getString("OBJECT") + ": " + bhdu[0].getObject() + "\n";
                    }
                    if (bhdu[0].getTelescop() != null) {
                        s += IJ.getBundle().getString("TELESCOP") + ": " + bhdu[0].getTelescop() + "\n";
                    }
                    if (bhdu[0].getInstrum() != null) {
                        s += IJ.getBundle().getString("INSTRUM") + ": " + bhdu[0].getInstrum() + "\n";
                    }
                    if (bhdu[0].getFilter() != null) {
                        s += IJ.getBundle().getString("FILTER") + ": " + bhdu[0].getFilter() + "\n";
                    }
                    if (bhdu[0].getObserver() != null) {
                        s += IJ.getBundle().getString("OBSERVER") + ": " + bhdu[0].getObserver() + "\n";
                    }
                    if (bhdu[0].getExptime() != null) {
                        s += IJ.getBundle().getString("EXPTIME") + ": " + bhdu[0].getExptime() + "\n";
                    }
                    if (bhdu[0].getDateObs() != null) {
                        s += IJ.getBundle().getString("DATE-OBS") + ": " + bhdu[0].getDateObs() + "\n";
                    }
                    if (bhdu[0].getUT() != null) {
                        s += IJ.getBundle().getString("UT") + ": " + bhdu[0].getUT() + "\n";
                    }
                    if (bhdu[0].getUTC() != null) {
                        s += IJ.getBundle().getString("UT") + ": " + bhdu[0].getUTC() + "\n";
                    }
                    if (bhdu[0].getRA() != null) {
                        s += IJ.getBundle().getString("RA") + ": " + bhdu[0].getRA() + "\n";
                    }
                    if (bhdu[0].getDEC() != null) {
                        s += IJ.getBundle().getString("DEC") + ": " + bhdu[0].getDEC() + "\n";
                    }
                    s += "\n" + "\n" + "*************************************************************" + "\n";

                    if (IJ.debug) {
                        System.out.println("Header: \n" + s);
                    }

                    ImageData imgData = null;
                    if (bhdu[0].getData() != null) {
                        imgData = (ImageData) bhdu[0].getData();
                    } else {
                        if (IJ.debug) {
                            System.out.println("No data in fits !");
                        }
                    }
                    if ((wi > 0) && (he > 0)) {
                        //FileOpener fo = new FileOpener(fi);
                        //imp = fo.open(false);
                        if (de == 1) {
                            ImageProcessor ip = null;

                            ////////////////////////////////////////////////////////////////////////////////
                            ////////////////////////////////////////////////////////////////////////////////
                            ////////////////////////////////////////////////////////////////////////////////
                            if (IJ.debug) {
                                System.out.println("Bits " + bhdu[0].getBitPix() + " " + wi + " " + he);
                            }
                            /////////////////////////// 2D ///////////////////////////////////
                            if (bhdu[0].getNAXIS() == 2) {
                                //Profiler p = new Profiler();
                                ///////////////////////////// 16 BITS ///////////////////////
                                if (bhdu[0].getBitPix() == 16) {
                                    short[][] itab = (short[][]) imgData.getKernel();
                                    int idx = 0;
                                    float[] imgtab;
                                    FloatProcessor imgtmp;
                                    imgtmp = new FloatProcessor(wi, he);
                                    imgtab = new float[wi * he];
                                    for (int y = 0; y < he; y++) {
                                        for (int x = 0; x < wi; x++) {
                                            imgtab[idx] = (float) bhdu[0].getBZero() + (float) bhdu[0].getBScale() * (float) itab[y][x];
                                            idx++;
                                        }
                                    }
                                    imgtmp.setPixels(imgtab);
                                    imgtmp.resetMinAndMax();

                                    if (he == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
                                    }
                                    if (wi == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(100, he);
                                    }
                                    if (IJ.debug) {
                                        System.out.println("ip " + imgtmp + " imp " + this);
                                    }
                                    ip = imgtmp;
                                    ip.flipVertical();
                                    this.setProcessor(fileName, ip);

                                } // 8 bits
                                else if (bhdu[0].getBitPix() == 8) {
                                    byte[][] itab = (byte[][]) imgData.getKernel();
                                    int idx = 0;
                                    float[] imgtab;
                                    FloatProcessor imgtmp;
                                    imgtmp = new FloatProcessor(wi, he);
                                    imgtab = new float[wi * he];
                                    for (int y = 0; y < he; y++) {
                                        for (int x = 0; x < wi; x++) {
                                            if (itab[x][y] < 0) {
                                                itab[x][y] += 256;
                                            }
                                            imgtab[idx] = (float) bhdu[0].getBZero() + (float) bhdu[0].getBScale() * (float) (itab[y][x]);
                                            idx++;
                                        }
                                    }
                                    imgtmp.setPixels(imgtab);
                                    imgtmp.resetMinAndMax();

                                    if (he == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
                                    }
                                    if (wi == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(100, he);
                                    }
                                    if (IJ.debug) {
                                        System.out.println("ip " + imgtmp + " imp " + this);
                                    }
                                    ip = imgtmp;
                                    ip.flipVertical();
                                    this.setProcessor(fileName, ip);

                                } // 16-bits
                                ///////////////// 32 BITS ///////////////////////
                                else if (bhdu[0].getBitPix() == 32) {
                                    int[][] itab = (int[][]) imgData.getKernel();
                                    int idx = 0;
                                    float[] imgtab;
                                    FloatProcessor imgtmp;
                                    imgtmp = new FloatProcessor(wi, he);
                                    imgtab = new float[wi * he];
                                    for (int y = 0; y < he; y++) {
                                        for (int x = 0; x < wi; x++) {
                                            imgtab[idx] = (float) bhdu[0].getBZero() + (float) bhdu[0].getBScale() * (float) itab[y][x];
                                            idx++;
                                        }
                                    }
                                    imgtmp.setPixels(imgtab);
                                    imgtmp.resetMinAndMax();

                                    if (he == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
                                    }
                                    if (wi == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(100, he);
                                    }

                                    ip = imgtmp;
                                    ip.flipVertical();
                                    this.setProcessor(fileName, ip);

                                } // 32 bits
                                /////////////// -32 BITS ?? /////////////////////////////////
                                else if (bhdu[0].getBitPix() == -32) {
                                    float[][] itab = (float[][]) imgData.getKernel();
                                    int idx = 0;
                                    float[] imgtab;
                                    FloatProcessor imgtmp;
                                    imgtmp = new FloatProcessor(wi, he);
                                    imgtab = new float[wi * he];
                                    for (int y = 0; y < he; y++) {
                                        for (int x = 0; x < wi; x++) {
                                            imgtab[idx] = (float) bhdu[0].getBZero() + (float) bhdu[0].getBScale() * (float) itab[y][x];
                                            idx++;
                                        }
                                    }
                                    imgtmp.setPixels(imgtab);
                                    imgtmp.resetMinAndMax();

                                    if (he == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
                                    }
                                    if (wi == 1) {
                                        imgtmp = (FloatProcessor) imgtmp.resize(100, he);
                                    }

                                    ip = imgtmp;
                                    ip.flipVertical();
                                    this.setProcessor(fileName, ip);
                                    System.out.print("-32 bits " + ip + " " + this + " " + bhdu[0].getBZero() + " " + bhdu[0].getBScale() + " " + itab);
                                    System.out.print("Status " + bhdu[0].getSTATUS());

//                                    ///////// special spectre optique transit
                                    if ((bhdu[0].getSTATUS() != null) && (bhdu[0].getSTATUS().equals("SPECTRUM")) && (bhdu[0].getNAXIS() == 2)) {
                                        if (IJ.debug) {
                                            System.out.println("spectre optique");
                                        }
                                        //IJ.log("spectre optique");
                                        float[] xValues = new float[wi];
                                        float[] yValues = new float[wi];
                                        for (int y = 0; y < wi; y++) {
                                            yValues[y] = (float) itab[0][y];
                                            if (yValues[y] < 0) {
                                                yValues[y] = 0;
                                            }
                                        }
                                        String unitY = "Intensity ";
                                        unitY = bun.getString("IntensityRS") + " ";
                                        String unitX = "Longueur d'onde ";
                                        unitX = bun.getString("WavelengthRS") + " ";
                                        float CRVAL1 = 0;
                                        float CRPIX1 = 0;
                                        float CDELT1 = 0;
                                        if (bhdu[0].getCRVAL1() != null) {
                                            CRVAL1 = Float.parseFloat(bhdu[0].getCRVAL1());
                                        }
                                        if (bhdu[0].getCRPIX1() != null) {
                                            CRPIX1 = Float.parseFloat(bhdu[0].getCRPIX1());
                                        }
                                        if (bhdu[0].getCDELT1() != null) {
                                            CDELT1 = Float.parseFloat(bhdu[0].getCDELT1());
                                        }
                                        for (int x = 0; x < wi; x++) {
                                            xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
                                        }

                                        float odiv = 1;
                                        if (CRVAL1 < 0.000001) {
                                            odiv = 1000000;
                                            unitX += "(µm)";
                                        } else {
                                            unitX += "ADU";
                                        }

                                        for (int x = 0; x < wi; x++) {
                                            xValues[x] = xValues[x] * odiv;
                                        }

                                        Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " " + fileName, "X: " + unitX, "Y: " + unitY, xValues, yValues);
                                        P.draw();
                                    } //// end of special optique                                    
                                } // -32 bits 
                                else {
                                    if (IJ.debug) {
                                        System.out.println("other case ");
                                    }
                                    ip = imp.getProcessor();
                                }
                                //ip.flipVertical();
                                //this.setProcessor(fileName, ip);                                
                            } // 2D
                            ///////// special spectre radio Onsala
                            else if (bhdu[0].getNAXIS() == 3 && bhdu[0].getAxes()[dim - 2] == 1 && bhdu[0].getAxes()[dim - 3] == 1) {
                                if (IJ.debug) {
                                    System.out.println("Spectre radio Onsala");
                                }
                                short[][][] itab = (short[][][]) imgData.getKernel();
                                float[] xValues = new float[wi];
                                float[] yValues = new float[wi];

                                for (int y = 0; y < wi; y++) {
                                    yValues[y] = (float) bhdu[0].getBZero() + (float) bhdu[0].getBScale() * itab[0][0][y];
                                }

                                String unitY = "Intensity ";
                                unitY = bun.getString("IntensityRS") + " ";
                                String unitX = "Freq ";
                                unitX = bun.getString("FrequencyRS") + " ";
                                float CRVAL1 = 0;
                                float CRPIX1 = 0;
                                float CDELT1 = 0;
                                if (bhdu[0].getCRVAL1() != null) {
                                    CRVAL1 = Float.parseFloat(bhdu[0].getCRVAL1());
                                }
                                if (bhdu[0].getCRPIX1() != null) {
                                    CRPIX1 = Float.parseFloat(bhdu[0].getCRPIX1());
                                }
                                if (bhdu[0].getCDELT1() != null) {
                                    CDELT1 = Float.parseFloat(bhdu[0].getCDELT1());
                                }
                                for (int x = 0; x < wi; x++) {
                                    xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
                                }

                                int div = 1;
                                if (CRVAL1 > 2000000000) {
                                    div = 1000000000;
                                    unitX += "(Ghz)";
                                } else if (CRVAL1 > 1000000000) {
                                    div = 1000000;
                                    unitX += "(Mhz)";
                                } else if (CRVAL1 > 1000000) {
                                    div = 1000;
                                    unitX += "(Khz)";
                                } else {
                                    unitX += "(Hz)";
                                }

                                for (int x = 0; x < wi; x++) {
                                    xValues[x] = xValues[x] / div;
                                }

                                Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " " + fileName, "X: " + unitX, "Y: " + unitY, xValues, yValues);
                                P.draw();

                                FloatProcessor imgtmp;
                                imgtmp = new FloatProcessor(wi, he);
                                imgtmp.setPixels(yValues);
                                imgtmp.resetMinAndMax();

                                if (he == 1) {
                                    imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
                                }
                                if (wi == 1) {
                                    imgtmp = (FloatProcessor) imgtmp.resize(100, he);
                                }

                                ip = imgtmp;

                                ip.flipVertical();
                                setProcessor(fileName, ip);

                            }//// end of special radio Onsala

                            //By Oli, for radiotelescope UPMC/OBSPM spectra
                            if (bhdu[0].getTelescop() != null) {
                                if (bhdu[0].getTelescop().equals("SRT-PARIS")) {
                                    if (IJ.debug) {
                                        System.out.println("For radiotelescope UPMC/OBSPM spectra");
                                    }
                                    short[] Upmcitab = (short[]) imgData.getKernel();
                                    float[] xValues = new float[wi];
                                    float[] yValues = new float[wi];
                                    for (int y = 0; y < wi; y++) {
                                        yValues[y] = Upmcitab[y];
                                    }
                                    String unitY = "Intensity ";
                                    unitY = bun.getString("IntensityRS") + " ";
                                    String unitX = "Freq ";
                                    unitX = bun.getString("FrequencyRS") + " ";
                                    float CRVAL1 = 0;
                                    float CRPIX1 = 0;
                                    float CDELT1 = 0;
                                    if (bhdu[0].getCRVAL1() != null) {
                                        CRVAL1 = Float.parseFloat(bhdu[0].getCRVAL1());
                                    }
                                    if (bhdu[0].getCRPIX1() != null) {
                                        CRPIX1 = Float.parseFloat(bhdu[0].getCRPIX1());
                                    }
                                    if (bhdu[0].getCDELT1() != null) {
                                        CDELT1 = Float.parseFloat(bhdu[0].getCDELT1());
                                    }
                                    for (int x = 0; x < wi; x++) {
                                        xValues[x] = (CRVAL1 + (x - CRPIX1) * CDELT1) / 1000000;
                                    }
                                    FloatProcessor Upmcimgtmp;
                                    Upmcimgtmp = new FloatProcessor(wi, he);
                                    Upmcimgtmp.setPixels(yValues);
                                    Upmcimgtmp.resetMinAndMax();
                                    Upmcimgtmp = (FloatProcessor) Upmcimgtmp.resize(wi, 100);
                                    ip = Upmcimgtmp;
                                    ip.flipVertical();
                                    setProcessor(fileName, ip);
                                    // crop spectra between Freq= [1419.9 , 1420.9]
                                    double OliFrqMin = 1419.9;
                                    double OliFrqMax = 1420.9;
                                    if (IJ.debug) {
                                        System.out.println(OliFrqMin + "  " + OliFrqMax);
                                    }
                                    float Olicut;
                                    int x = 1;
                                    while (xValues[x] < OliFrqMin) {
                                        x++;
                                    }
                                    Olicut = yValues[x];
                                    if (IJ.debug) {
                                        System.out.println("X= " + x + "  X(x)= " + xValues[x] + " Y(x)= " + Olicut);
                                    }
                                    while (xValues[x] < OliFrqMax) {
                                        x++;
                                    }
                                    if (IJ.debug) {
                                        System.out.println("X= " + x + " X(x)= " + xValues[x] + " Y(x)= " + yValues[x - 1]);
                                    }
                                    Olicut = (Olicut + yValues[x - 1]) / 2;
                                    for (int y = 0; y < wi; y++) {
                                        if (yValues[y] < Olicut) {
                                            yValues[y] = Olicut;
                                        }
                                    }
                                    // end of crop
                                    unitX += "(MHz)";
                                    Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " " + fileName, "X: " + unitX, "Y: " + unitY, xValues, yValues);
                                    P.draw();
                                }
                            }   //end by Oli
                            
                            //New by thomas, SRT telescope , 18112014
                            if (bhdu[0].getTelescop() != null) {
                                if (bhdu[0].getTelescop().startsWith("SRT")) {
                                    if (IJ.debug) {
                                        System.out.println("For radiotelescope UPMC SRT");
                                    }
                                    short[] Upmcitab = (short[]) imgData.getKernel();
                                    float[] xValues = new float[wi];
                                    float[] yValues = new float[wi];
                                    for (int y = 0; y < wi; y++) {
                                        yValues[y] = Upmcitab[y];
                                    }
                                    String unitY = "Intensity ";
                                    unitY = bun.getString("IntensityRS") + " ";
                                    String unitX = "Freq ";
                                    unitX = bun.getString("FrequencyRS") + " ";
                                    float CRVAL1 = 0;
                                    float CRPIX1 = 0;
                                    float CDELT1 = 0;
                                    if (bhdu[0].getCRVAL1() != null) {
                                        CRVAL1 = Float.parseFloat(bhdu[0].getCRVAL1());
                                    }
                                    if (bhdu[0].getCRPIX1() != null) {
                                        CRPIX1 = Float.parseFloat(bhdu[0].getCRPIX1());
                                    }
                                    if (bhdu[0].getCDELT1() != null) {
                                        CDELT1 = Float.parseFloat(bhdu[0].getCDELT1());
                                    }
                                    for (int x = 0; x < wi; x++) {
                                        xValues[x] = (CRVAL1 + (x - CRPIX1) * CDELT1) / 1000000;
                                    }
                                    FloatProcessor Upmcimgtmp;
                                    Upmcimgtmp = new FloatProcessor(wi, he);
                                    Upmcimgtmp.setPixels(yValues);
                                    Upmcimgtmp.resetMinAndMax();
                                    Upmcimgtmp = (FloatProcessor) Upmcimgtmp.resize(wi, 100);
                                    ip = Upmcimgtmp;
                                    ip.flipVertical();
                                    setProcessor(fileName, ip);
                                    // crop spectra between Freq= [1419.9 , 1420.9]
                                    double OliFrqMin = 1419.9;
                                    double OliFrqMax = 1420.9;
                                    if (IJ.debug) {
                                        System.out.println(OliFrqMin + "  " + OliFrqMax);
                                    }
                                    float Olicut;
                                    int x = 1;
                                    while (xValues[x] < OliFrqMin) {
                                        x++;
                                    }
                                    Olicut = yValues[x];
                                    if (IJ.debug) {
                                        System.out.println("X= " + x + "  X(x)= " + xValues[x] + " Y(x)= " + Olicut);
                                    }
                                    while (xValues[x] < OliFrqMax) {
                                        x++;
                                    }
                                    if (IJ.debug) {
                                        System.out.println("X= " + x + " X(x)= " + xValues[x] + " Y(x)= " + yValues[x - 1]);
                                    }
                                    Olicut = (Olicut + yValues[x - 1]) / 2;
                                    for (int y = 0; y < wi; y++) {
                                        if (yValues[y] < Olicut) {
                                            yValues[y] = Olicut;
                                        }
                                    }
                                    // end of crop
                                    unitX += "(MHz)";
                                    Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " " + fileName, "X: " + unitX, "Y: " + unitY, xValues, yValues);
                                    P.draw();
                                }
                            }   //New by thomas, SRT telescope , 18112014

                            /////////////////////////////////////////////////////////////////////////////////////////
                            /////////////////////////////////////////////////////////////////////////////////////////
                            /////////////////////////////////////////////////////////////////////////////////////////                            
                            if (ip == null) {
                                ip = imp.getProcessor();
                                ip.flipVertical();
                                setProcessor(fileName, ip);
                            }
                        } // 1 image, no stack
                        ///////////// STACK
                        else {
                            ImageStack stack = imp.getStack();
                            for (int i = 1; i <= stack.getSize(); i++) {
                                stack.getProcessor(i).flipVertical();
                            }
                            setStack(fileName, stack);
                        } // STACK                        
                        //Calibration cal = imp.getCalibration();
                        if (fd != null) {
                            setProperty("Info", s + fd.getHeaderInfo());
                        }
                        if (fi != null) {
                            setFileInfo(fi);
                        }
//                        if (arg.equals("")) {
//                            show();
//                        }
                        show();
                    } // wi or he <0
                    else {
                        IJ.error("This does not appear to be a FITS file. " + wi + " " + he);
                    }

                    IJ.showStatus("");
                    //IJ.log("exception " + fis);
                    File file = new File(IJ.getDirectory("home")+".tmp.fits");
                    fis = new FileOutputStream(file);
                    DataOutputStream dos = new DataOutputStream(fis);
                    //IJ.log("exception " + file + " " + fis + " " + dos);
                    myFits.write(dos);
                    try {
                        wcs = new WCS(H);
                        if (IJ.debug) {
                            System.out.println("Reading the Wcs");
                        }
                    } catch (Exception e) {
                    }

                    } catch (FileNotFoundException ex) {
                    Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FitsException ex) {
                    Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        fis.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                /////////////////////////// Exception //////////////
            } catch (Exception e) {
                if (IJ.debug) {
                    System.out.println("This is the exception case " + e);
                }
                if (this != null) {
                    return;
                }
                OpenDialog od = new OpenDialog("Open FITS...", arg);
                String directory = od.getDirectory();
                String fileName = od.getFileName();
                if (fileName == null) {
                    return;
                }
                IJ.showStatus("Opening: " + directory + fileName);
                FitsDecoder fd = new FitsDecoder(directory, fileName);
                FileInfo fi = null;
                try {
                    fi = fd.getInfo();
                } catch (IOException er) {
                }
                if (fi != null && fi.width > 0 && fi.height > 0 && fi.offset > 0) {
                    FileOpener fo = new FileOpener(fi);
                    ImagePlus imp = fo.open(false);
                    if (fi.nImages == 1) {
                        ImageProcessor ip = imp.getProcessor();
                        ip.flipVertical(); // origin is at bottom left corner
                        setProcessor(fileName, ip);
                    } else {
                        ImageStack stack = imp.getStack(); // origin is at bottom left corner
                        for (int i = 1; i <= stack.getSize(); i++) {
                            stack.getProcessor(i).flipVertical();
                        }
                        setStack(fileName, stack);
                    }
                    Calibration cal = imp.getCalibration();
                    if (fi.fileType == FileInfo.GRAY16_SIGNED && fd.getBscale() == 1.0 && fd.getBzero() == 32768.0) {
                        cal.setFunction(Calibration.NONE, null, "Gray Value");
                    }
                    // By Oli                   
                    if (fi.fileType == FileInfo.GRAY16_SIGNED && fd.getBscale() == 1.0 && fd.getBzero() == 0.0) {
                        cal.setFunction(Calibration.NONE, null, "Gray Value");
                    }
                    // end by Oli
                    setCalibration(cal);
                    setProperty("Info", fd.getHeaderInfo());
                    try {
                        BasicHDU[] bhduExc = null;
                        Fits myFits = null;
                        myFits = new Fits(directory + File.separator + fileName);
                        bhduExc = myFits.read();
                        Header HH = bhduExc[0].getHeader();
                        wcs = new WCS(HH);
                        if (IJ.debug) {
                            System.out.println("Reading the Wcs in the exception");
                        }
                        String s = new String("");
                        if (bhduExc[0].getObject() != null) {
                            s += IJ.getBundle().getString("OBJECT") + ": " + bhduExc[0].getObject() + "\n";
                        }
                        if (bhduExc[0].getTelescop() != null) {
                            s += IJ.getBundle().getString("TELESCOP") + ": " + bhduExc[0].getTelescop() + "\n";
                        }
                        if (bhduExc[0].getInstrum() != null) {
                            s += IJ.getBundle().getString("INSTRUM") + ": " + bhduExc[0].getInstrum() + "\n";
                        }
                        if (bhduExc[0].getFilter() != null) {
                            s += IJ.getBundle().getString("FILTER") + ": " + bhduExc[0].getFilter() + "\n";
                        }
                        if (bhduExc[0].getObserver() != null) {
                            s += IJ.getBundle().getString("OBSERVER") + ": " + bhduExc[0].getObserver() + "\n";
                        }
                        if (bhduExc[0].getExptime() != null) {
                            s += IJ.getBundle().getString("EXPTIME") + ": " + bhduExc[0].getExptime() + "\n";
                        }
                        if (bhduExc[0].getDateObs() != null) {
                            s += IJ.getBundle().getString("DATE-OBS") + ": " + bhduExc[0].getDateObs() + "\n";
                        }
                        if (bhduExc[0].getUT() != null) {
                            s += IJ.getBundle().getString("UT") + ": " + bhduExc[0].getUT() + "\n";
                        }
                        if (bhduExc[0].getUTC() != null) {
                            s += IJ.getBundle().getString("UT") + ": " + bhduExc[0].getUTC() + "\n";
                        }
                        if (bhduExc[0].getRA() != null) {
                            s += IJ.getBundle().getString("RA") + ": " + bhduExc[0].getRA() + "\n";
                        }
                        if (bhduExc[0].getDEC() != null) {
                            s += IJ.getBundle().getString("DEC") + ": " + bhduExc[0].getDEC() + "\n";
                        }
                        s += "\n" + "\n" + "*************************************************************" + "\n";
                        setProperty("Info", s + fd.getHeaderInfo());
                    } catch (Exception ee) {
                    }
                    setFileInfo(fi); // needed for File->Revert
                    if (arg.equals("")) {
                        show();
                    }
                } else {
                    if (this == null) {
                        IJ.error("This does not appear to be a FITS file.");
                    }
                }
                IJ.showStatus("");
            } // catch
        }

    }


    /*
     * return the keyword with his value in string format euhou JL
     */
    /**
     * Description of the Method
     *
     * @param keyword Description of the Parameter
     * @param path Description of the Parameter
     * @return Description of the Return Value
     * @exception IOException Description of the Exception
     */
    public static String getkeyword(String keyword, String path)
            throws IOException {
        RandomAccessFile f;
        ImagePlus imp = null;

        imp = new ImagePlus(path);

        FileInfo fi;
        fi = imp.getFileInfo();
        ImageProcessor ip = imp.getProcessor();

        File file = new File(path);
        f = new RandomAccessFile(file, "r");
        f.seek(0);
        String s = getString(80, f);
        while (!s.startsWith("END")) {
            if (s.startsWith(keyword)) {
                f.close();
                return s;
            }
            s = getString(80, f);
        }
        f.close();
        s = "";
        return s;
    }

    // modif tb 0607 skyview
    /**
     * Gets the locationAsString attribute of the FITS object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @return The locationAsString value
     */
    public String getLocationAsString(int x, int y) {
        String s;
        if (wcs != null) {
            double[] in = new double[2];
            in[0] = (double) (x);
            in[1] = (double) (getProcessor().getHeight() - y - 1.0);
            //in[2]=0.0;
            double[] out = wcs.inverse().transform(in);
            double[] coord = new double[2];
            skyview.geometry.Util.coord(out, coord);
            CoordinateFormatter cf = new CoordinateFormatter();
            String[] ra = cf.sexagesimal(Math.toDegrees(coord[0]) / 15.0, 8).split(" ");
            String[] dec = cf.sexagesimal(Math.toDegrees(coord[1]), 8).split(" ");

            s = "x=" + x + ",y=" + y + " (RA=" + ra[0] + "h" + ra[1] + "m" + ra[2] + "s,  DEC=" + dec[0] + "° " + dec[1] + "' " + dec[2] + "\"" + ")";

        } else {
            s = "x=" + x + " y=" + y;
        }
        if (getStackSize() > 1) {
            s += " z=" + (getCurrentSlice() - 1);
        }
        return s;
    }

    // modif tb 0607 skyview
    /**
     * Gets the string attribute of the FITS class
     *
     * @param length Description of the Parameter
     * @param f Description of the Parameter
     * @return The string value
     * @exception IOException Description of the Exception
     */
    static String getString(int length, RandomAccessFile f)
            throws IOException {
        byte[] b = new byte[length];
        f.read(b);
        return new String(b);
    }

    public BasicHDU[] getBHDU() {
        return BHDU;
    }

    public static FITS getInstance() {
        return instance;
    }
}
