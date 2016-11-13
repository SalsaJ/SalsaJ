package ij.plugin;

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
import nom.tam.fits.BasicHDU;
import nom.tam.fits.Data;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.ImageHDU;
import nom.tam.image.compression.hdu.CompressedImageHDU;
import skyview.data.CoordinateFormatter;
import skyview.geometry.WCS;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import static nom.tam.fits.header.InstrumentDescription.FILTER;
import static nom.tam.fits.header.ObservationDescription.DEC;
import static nom.tam.fits.header.ObservationDescription.RA;
import static nom.tam.fits.header.ObservationDurationDescription.EXPTIME;
import static nom.tam.fits.header.Standard.NAXIS;

public class FITS extends ImagePlus implements PlugIn
{
    private static ResourceBundle bun = IJ.getPluginBundle();
    private WCS wcs;
    private FileInfo fileInfo;
    private ImagePlus imagePlus;
    private FitsDecoder fitsDecoder;
    private String fileName;
    private Fits fits;
    private int wi;
    private int he;
    private int de;
    private String imageDescription;

    /**
     * Main processing method for the FITS object
     *
     * @param arg Description of the Parameter
     */
    public void run(String arg)
    {
        try
        {
            displayFITSImage(arg);
        }
        catch (Exception e)
        {
            Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, e);
            IJ.error(e.getMessage());
            if (IJ.debug)
            {
                System.out.println("This is the exception case " + e);
            }
            OpenFileUsingFileOpener(arg);
        }
    }

    private void displayFITSImage(String path) throws IOException, FitsException
    {
        wcs = null;
        imagePlus = null;

        BasicHDU[] hdu = getHDU(path);
        int imageIndex = 0;

        Data imgData;
        BasicHDU displayHdu;
        if (hdu[imageIndex].getHeader().getIntValue(NAXIS) == 0)
        {
            imageIndex = 1;
            displayHdu = getCompressedImageData((CompressedImageHDU) hdu[imageIndex]);
            imgData = displayHdu.getData();
            wi = hdu[imageIndex].getHeader().getIntValue("ZNAXIS1");
            he = hdu[imageIndex].getHeader().getIntValue("ZNAXIS2");
            de = 1;
        }
        else
        {
            displayHdu = hdu[imageIndex];
            imgData = getImageData(hdu[imageIndex]);
            fixDimensions(hdu[imageIndex], hdu[imageIndex].getAxes().length);
        }

        buildImageDescriptionFromHeader(displayHdu);

        if ((wi < 0) || (he < 0))
        {
            throw new FitsException("This does not appear to be a FITS file. " + wi + " " + he);
        }
        if (de == 1)
        {
                displaySingleImage(displayHdu, imgData);
        }
        else
        {
            displayStackedImage();
        }

        if (fitsDecoder != null)
        {
            setProperty("Info", imageDescription + fitsDecoder.getHeaderInfo());
        }
        if (fileInfo != null)
        {
            setFileInfo(fileInfo);
        }
        show();

        IJ.showStatus("");
        writeTemporaryFITSFile(hdu[imageIndex]);
    }

    private ImageHDU getCompressedImageData(CompressedImageHDU hdu) throws FitsException
    {
        return hdu.asImageHDU();
    }

    private void displayStackedImage()
    {
        ImageStack stack = imagePlus.getStack();
        for (int i = 1; i <= stack.getSize(); i++)
        {
            stack.getProcessor(i).flipVertical();
        }
        setStack(fileName, stack);
    }

    private void displaySingleImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor imageProcessor = null;
        int dim = hdu.getAxes().length;

        if (IJ.debug)
        {
            System.out.println("Bits " + hdu.getBitPix() + " " + wi + " " + he);
        }
        if (hdu.getHeader().getIntValue(NAXIS) == 2)
        {
            imageProcessor = process2DimensionalImage(hdu, imgData);
        }
        // special spectre radio Onsala
        else if (hdu.getHeader().getIntValue(NAXIS) == 3
                && hdu.getAxes()[dim - 2] == 1 && hdu.getAxes()[dim - 3] == 1)
        {
            imageProcessor = process3DimensionalImage(hdu, imgData);
        }

        // For radio telescope UPMC/OBSPM spectra
        if (hdu.getTelescope() != null)
        {
            if (hdu.getTelescope().equals("SRT-PARIS"))
            {
                imageProcessor = processSRTParisSpectra_Oli(hdu, imgData);
            }
            //New by thomas, SRT telescope , 18112014
            if (hdu.getTelescope().startsWith("SRT"))
            {
                imageProcessor = processSRTParisSpectra_Thomas(hdu, imgData);
            }
        }

        if (imageProcessor == null)
        {
            imageProcessor = imagePlus.getProcessor();
            imageProcessor.flipVertical();
            setProcessor(fileName, imageProcessor);
        }
    }

    private void OpenFileUsingFileOpener(String path)
    {
        OpenDialog od = new OpenDialog("Open FITS...", path);
        String directory = od.getDirectory();
        String fileName = od.getFileName();
        if (fileName == null)
        {
            return;
        }
        IJ.showStatus("Opening: " + directory + fileName);
        fitsDecoder = new FitsDecoder(directory, fileName);
        try
        {
            fileInfo = fitsDecoder.getInfo();
        }
        catch (IOException er)
        {
            IJ.error(er.getMessage());
        }
        if (fileInfo != null && fileInfo.width > 0 && fileInfo.height > 0 && fileInfo.offset > 0)
        {
            FileOpener fo = new FileOpener(fileInfo);
            ImagePlus imp = fo.open(false);
            if (fileInfo.nImages == 1)
            {
                ImageProcessor ip = imp.getProcessor();
                ip.flipVertical(); // origin is at bottom left corner
                setProcessor(fileName, ip);
            }
            else
            {
                ImageStack stack = imp.getStack(); // origin is at bottom left corner
                for (int i = 1; i <= stack.getSize(); i++)
                {
                    stack.getProcessor(i).flipVertical();
                }
                setStack(fileName, stack);
            }
            Calibration cal = imp.getCalibration();
            if (fileInfo.fileType == FileInfo.GRAY16_SIGNED && fitsDecoder.getBscale() == 1.0
                    && fitsDecoder.getBzero() == 32768.0)
            {
                cal.setFunction(Calibration.NONE, null, "Gray Value");
            }
            // By Oli
            if (fileInfo.fileType == FileInfo.GRAY16_SIGNED && fitsDecoder.getBscale() == 1.0
                    && fitsDecoder.getBzero() == 0.0)
            {
                cal.setFunction(Calibration.NONE, null, "Gray Value");
            }
            // end by Oli
            setCalibration(cal);
            setProperty("Info", fitsDecoder.getHeaderInfo());
            try
            {
                BasicHDU[] bhduExc;
                Fits myFits;
                myFits = new Fits(directory + File.separator + fileName);
                bhduExc = myFits.read();
                Header HH = bhduExc[0].getHeader();
                wcs = new WCS(HH);
                if (IJ.debug)
                {
                    System.out.println("Reading the Wcs in the exception");
                }
                buildImageDescriptionFromHeader(bhduExc[0]);
            }
            catch (Exception e)
            {
                IJ.error(e.getMessage());
            }
            setFileInfo(fileInfo); // needed for File->Revert
            if (path.equals(""))
            {
                show();
            }
        }
        else
        {
            IJ.error("This does not appear to be a FITS file.");
        }
        IJ.showStatus("");
    }

    private void writeTemporaryFITSFile(BasicHDU hdu) throws FileNotFoundException, FitsException
    {
        File file = new File(IJ.getDirectory("home") + ".tmp.fits");
        FileOutputStream fis = new FileOutputStream(file);
        DataOutputStream dos = new DataOutputStream(fis);
        fits.write(dos);
        try
        {
            wcs = new WCS(hdu.getHeader());
            if (IJ.debug)
            {
                System.out.println("Reading the WCS");
            }
        }
        catch (Exception e)
        {
            Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, e);
        }
        finally
        {
            try
            {
                fis.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(FITS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private ImageProcessor processSRTParisSpectra_Thomas(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;
        if (IJ.debug)
        {
            System.out.println("For radiotelescope UPMC SRT");
        }
        short[] Upmcitab = (short[]) imgData.getKernel();
        float[] xValues = new float[wi];
        float[] yValues = new float[wi];
        for (int y = 0; y < wi; y++)
        {
            yValues[y] = Upmcitab[y];
        }
        String unitY;
        unitY = bun.getString("IntensityRS") + " ";
        String unitX;
        unitX = bun.getString("FrequencyRS") + " ";
        float crval1 = 0;
        float crpix1 = 0;
        float cdelt1 = 0;
        if (hdu.getHeader().getStringValue("CRVAL1") != null)
        {
            crval1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CRVAL1"));
        }
        if (hdu.getHeader().getStringValue("CRPIX1") != null)
        {
            crpix1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CRPIX1"));
        }
        if (hdu.getHeader().getStringValue("CDELT1") != null)
        {
            cdelt1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CDELT1"));
        }
        for (int x = 0; x < wi; x++)
        {
            xValues[x] = (crval1 + (x - crpix1) * cdelt1) / 1000000;
        }
        FloatProcessor floatProcessor;
        floatProcessor = new FloatProcessor(wi, he);
        floatProcessor.setPixels(yValues);
        floatProcessor.resetMinAndMax();
        floatProcessor = (FloatProcessor) floatProcessor.resize(wi, 100);
        ip = floatProcessor;
        ip.flipVertical();
        setProcessor(fileName, ip);
        // crop spectra between Freq= [1419.9 , 1420.9]
        double OliFrqMin = 1419.9;
        double OliFrqMax = 1420.9;
        if (IJ.debug)
        {
            System.out.println(OliFrqMin + "  " + OliFrqMax);
        }
        float Olicut;
        int x = 1;
        while (xValues[x] < OliFrqMin)
        {
            x++;
        }
        Olicut = yValues[x];
        if (IJ.debug)
        {
            System.out.println(
                    "X= " + x + "  X(x)= " + xValues[x] + " Y(x)= "
                            + Olicut);
        }
        while (xValues[x] < OliFrqMax)
        {
            x++;
        }
        if (IJ.debug)
        {
            System.out.println(
                    "X= " + x + " X(x)= " + xValues[x] + " Y(x)= "
                            + yValues[x - 1]);
        }
        Olicut = (Olicut + yValues[x - 1]) / 2;
        for (int y = 0; y < wi; y++)
        {
            if (yValues[y] < Olicut)
            {
                yValues[y] = Olicut;
            }
        }
        // end of crop
        unitX += "(MHz)";
        Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " "
                + fileName, "X: " + unitX, "Y: " + unitY, xValues,
                yValues);
        P.draw();
        return ip;
    }

    private ImageProcessor processSRTParisSpectra_Oli(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;
        if (IJ.debug)
        {
            System.out.println("For radio telescope UPMC/OBSPM spectra");
        }
        short[] Upmcitab = (short[]) imgData.getKernel();
        float[] xValues = new float[wi];
        float[] yValues = new float[wi];
        for (int y = 0; y < wi; y++)
        {
            yValues[y] = Upmcitab[y];
        }
        String unitY;
        unitY = bun.getString("IntensityRS") + " ";
        String unitX;
        unitX = bun.getString("FrequencyRS") + " ";
        float CRVAL1 = 0;
        float CRPIX1 = 0;
        float CDELT1 = 0;
        if (hdu.getHeader().getStringValue("CRVAL1") != null)
        {
            CRVAL1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CRVAL1"));
        }
        if (hdu.getHeader().getStringValue("CRPXI1") != null)
        {
            CRPIX1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CRPIX1"));
        }
        if (hdu.getHeader().getStringValue("CDELT1") != null)
        {
            CDELT1 = Float.parseFloat(
                    hdu.getHeader().getStringValue("CDELT1"));
        }
        for (int x = 0; x < wi; x++)
        {
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
        if (IJ.debug)
        {
            System.out.println(OliFrqMin + "  " + OliFrqMax);
        }
        float Olicut;
        int x = 1;
        while (xValues[x] < OliFrqMin)
        {
            x++;
        }
        Olicut = yValues[x];
        if (IJ.debug)
        {
            System.out.println(
                    "X= " + x + "  X(x)= " + xValues[x] + " Y(x)= "
                            + Olicut);
        }
        while (xValues[x] < OliFrqMax)
        {
            x++;
        }
        if (IJ.debug)
        {
            System.out.println(
                    "X= " + x + " X(x)= " + xValues[x] + " Y(x)= "
                            + yValues[x - 1]);
        }
        Olicut = (Olicut + yValues[x - 1]) / 2;
        for (int y = 0; y < wi; y++)
        {
            if (yValues[y] < Olicut)
            {
                yValues[y] = Olicut;
            }
        }
        // end of crop
        unitX += "(MHz)";
        Plot P = new Plot(IJ.getBundle().getString("PlotWinTitle") + " "
                + fileName, "X: " + unitX, "Y: " + unitY, xValues,
                yValues);
        P.draw();
        return ip;
    }

    private ImageProcessor process3DimensionalImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;
        if (IJ.debug)
        {
            System.out.println("Spectre radio Onsala");
        }
        short[][][] itab = (short[][][]) imgData.getKernel();
        float[] xValues = new float[wi];
        float[] yValues = new float[wi];

        for (int y = 0; y < wi; y++)
        {
            yValues[y] = (float) hdu.getBZero()
                    + (float) hdu.getBScale() * itab[0][0][y];
        }

        String unitY;
        unitY = bun.getString("IntensityRS") + " ";
        String unitX;
        unitX = bun.getString("FrequencyRS") + " ";
        float CRVAL1 = 0;
        float CRPIX1 = 0;
        float CDELT1 = 0;
        if (hdu.getHeader().getStringValue("CRVAL1") != null)
        {
            CRVAL1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CRVAL1"));
        }
        if (hdu.getHeader().getStringValue("CRPIX1") != null)
        {
            CRPIX1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CRPIX1"));
        }
        if (hdu.getHeader().getStringValue("CDELT1") != null)
        {
            CDELT1 = Float
                    .parseFloat(hdu.getHeader().getStringValue("CDELT1"));
        }
        for (int x = 0; x < wi; x++)
        {
            xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
        }

        int div = 1;
        if (CRVAL1 > 2000000000)
        {
            div = 1000000000;
            unitX += "(Ghz)";
        }
        else if (CRVAL1 > 1000000000)
        {
            div = 1000000;
            unitX += "(Mhz)";
        }
        else if (CRVAL1 > 1000000)
        {
            div = 1000;
            unitX += "(Khz)";
        }
        else
        {
            unitX += "(Hz)";
        }

        for (int x = 0; x < wi; x++)
        {
            xValues[x] = xValues[x] / div;
        }

        Plot P = new Plot(
                IJ.getBundle().getString("PlotWinTitle") + " " + fileName,
                "X: " + unitX, "Y: " + unitY, xValues, yValues);
        P.draw();

        FloatProcessor imgtmp;
        imgtmp = new FloatProcessor(wi, he);
        imgtmp.setPixels(yValues);
        imgtmp.resetMinAndMax();

        if (he == 1)
        {
            imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
        }
        if (wi == 1)
        {
            imgtmp = (FloatProcessor) imgtmp.resize(100, he);
        }

        ip = imgtmp;

        ip.flipVertical();
        setProcessor(fileName, ip);
        return ip;
    }

    private ImageProcessor process2DimensionalImage(BasicHDU hdu, Data imgData)
            throws FitsException
    {
        ImageProcessor ip;//Profiler p = new Profiler();
        ///////////////////////////// 16 BITS ///////////////////////
        if (hdu.getBitPix() == 16)
        {
            short[][] itab = (short[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * (float) itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }
            if (IJ.debug)
            {
                System.out.println("ip " + imgtmp + " imp " + this);
            }
            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 8 bits
        else if (hdu.getBitPix() == 8)
        {
            byte[][] itab = (byte[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    if (itab[x][y] < 0)
                    {
                        itab[x][y] += 256;
                    }
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale()
                            * (float) (itab[y][x]);
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }
            if (IJ.debug)
            {
                System.out.println("ip " + imgtmp + " imp " + this);
            }
            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 16-bits
        ///////////////// 32 BITS ///////////////////////
        else if (hdu.getBitPix() == 32)
        {
            int[][] itab = (int[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * (float) itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }

            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

        } // 32 bits
        /////////////// -32 BITS ?? /////////////////////////////////
        else if (hdu.getBitPix() == -32)
        {
            float[][] itab = (float[][]) imgData.getKernel();
            int idx = 0;
            float[] imgtab;
            FloatProcessor imgtmp;
            imgtmp = new FloatProcessor(wi, he);
            imgtab = new float[wi * he];
            for (int y = 0; y < he; y++)
            {
                for (int x = 0; x < wi; x++)
                {
                    imgtab[idx] = (float) hdu.getBZero()
                            + (float) hdu.getBScale() * itab[y][x];
                    idx++;
                }
            }
            imgtmp.setPixels(imgtab);
            imgtmp.resetMinAndMax();

            if (he == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(wi, 100);
            }
            if (wi == 1)
            {
                imgtmp = (FloatProcessor) imgtmp.resize(100, he);
            }

            ip = imgtmp;
            ip.flipVertical();
            this.setProcessor(fileName, ip);

            // special spectre optique transit
            if ((hdu.getHeader().getStringValue("STATUS") != null) && (hdu
                    .getHeader().getStringValue("STATUS")
                    .equals("SPECTRUM")) && (
                    hdu.getHeader().getIntValue(NAXIS) == 2))
            {
                if (IJ.debug)
                {
                    System.out.println("spectre optique");
                }
                //IJ.log("spectre optique");
                float[] xValues = new float[wi];
                float[] yValues = new float[wi];
                for (int y = 0; y < wi; y++)
                {
                    yValues[y] = itab[0][y];
                    if (yValues[y] < 0)
                    {
                        yValues[y] = 0;
                    }
                }
                String unitY;
                unitY = bun.getString("IntensityRS") + " ";
                String unitX;
                unitX = bun.getString("WavelengthRS") + " ";
                float CRVAL1 = 0;
                float CRPIX1 = 0;
                float CDELT1 = 0;
                if (hdu.getHeader().getStringValue("CRVAL1") != null)
                {
                    CRVAL1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CRVAL1"));
                }
                if (hdu.getHeader().getStringValue("CRPIX1") != null)
                {
                    CRPIX1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CRPIX1"));
                }
                if (hdu.getHeader().getStringValue("CDELT1") != null)
                {
                    CDELT1 = Float.parseFloat(
                            hdu.getHeader().getStringValue("CDELT1"));
                }
                for (int x = 0; x < wi; x++)
                {
                    xValues[x] = CRVAL1 + (x - CRPIX1) * CDELT1;
                }

                float odiv = 1;
                if (CRVAL1 < 0.000001)
                {
                    odiv = 1000000;
                    unitX += "(µm)";
                }
                else
                {
                    unitX += "ADU";
                }

                for (int x = 0; x < wi; x++)
                {
                    xValues[x] = xValues[x] * odiv;
                }

                Plot P = new Plot(
                        IJ.getBundle().getString("PlotWinTitle") + " "
                                + fileName, "X: " + unitX, "Y: " + unitY,
                        xValues, yValues);
                P.draw();
            } //// end of special optique
        } // -32 bits
        else
        {
            if (IJ.debug)
            {
                System.out.println("other case ");
            }
            ip = imagePlus.getProcessor();
        }
        return ip;
    }

    private Data getImageData(BasicHDU hdu)
    {
        Data imgData = null;
        if (hdu.getData() != null)
        {
            imgData = hdu.getData();
        }
        else
        {
            if (IJ.debug)
            {
                System.out.println("No data in fits !");
            }
        }
        return imgData;
    }

    private void buildImageDescriptionFromHeader(BasicHDU hdu)
    {
        imageDescription = "";
        if (hdu.getObject() != null)
        {
            imageDescription += IJ.getBundle().getString("OBJECT") + ": " + hdu.getObject() + "\n";
        }
        if (hdu.getTelescope() != null)
        {
            imageDescription += IJ.getBundle().getString("TELESCOP") + ": " + hdu.getTelescope()
                    + "\n";
        }
        if (hdu.getInstrument() != null)
        {
            imageDescription += IJ.getBundle().getString("INSTRUM") + ": " + hdu.getInstrument()
                    + "\n";
        }
        if (hdu.getHeader().getStringValue(FILTER) != null)
        {
            imageDescription += IJ.getBundle().getString("FILTER") + ": " + hdu.getHeader()
                    .getStringValue(FILTER) + "\n";
        }
        if (hdu.getObserver() != null)
        {
            imageDescription +=
                    IJ.getBundle().getString("OBSERVER") + ": " + hdu.getObserver() + "\n";
        }
        if (hdu.getHeader().getStringValue(EXPTIME) != null)
        {
            imageDescription += IJ.getBundle().getString("EXPTIME") + ": " + hdu.getHeader()
                    .getStringValue(EXPTIME) + "\n";
        }
        if (hdu.getObservationDate() != null)
        {
            imageDescription +=
                    IJ.getBundle().getString("DATE-OBS") + ": " + hdu.getObservationDate()
                            + "\n";
        }
        if (hdu.getHeader().getStringValue("UT") != null)
        {
            imageDescription += IJ.getBundle().getString("UT") + ": " + hdu.getHeader()
                    .getStringValue("UT") + "\n";
        }
        if (hdu.getHeader().getStringValue("UTC") != null)
        {
            imageDescription += IJ.getBundle().getString("UT") + ": " + hdu.getHeader()
                    .getStringValue("UTC") + "\n";
        }
        if (hdu.getHeader().getStringValue(RA) != null)
        {
            imageDescription += IJ.getBundle().getString("RA") + ": " + hdu.getHeader()
                    .getStringValue(RA) + "\n";
        }
        if (hdu.getHeader().getStringValue(DEC) != null)
        {
            imageDescription += IJ.getBundle().getString("DEC") + ": " + hdu.getHeader()
                    .getStringValue(DEC) + "\n";
        }
        imageDescription += "\n" + "\n"
                + "*************************************************************"
                + "\n";

        if (IJ.debug)
        {
            System.out.println("Header: \n" + imageDescription);
        }
        setProperty("Info", imageDescription + fitsDecoder.getHeaderInfo());

    }

    private void fixDimensions(BasicHDU hdu, int dim) throws FitsException
    {
        //By oli
        // replace  hdu.getAxes()[dim - 1] by fi.width
        // and replace  hdu.getAxes()[dim - 2] by fi.height
        // or it would crash if it is not defined and go to the exception case at the end
        if ((dim < 2) && (fileInfo != null))
        {
            wi = fileInfo.width;
            he = fileInfo.height;
            de = 1;
        }
        else
        {
            wi = hdu.getAxes()[dim - 1];
            he = hdu.getAxes()[dim - 2];
            if (dim > 2)
            {
                de = hdu.getAxes()[dim - 3];
            }
            else
            {
                de = 1;
            }
        }
        if (IJ.debug)
        {
            System.out.println(
                    "Dim= " + dim + "  -- Height= " + he + " pixels -- Width= " + wi
                            + " pixels");
        }
    }

    private BasicHDU[] getHDU(String path) throws IOException, FitsException
    {
        OpenDialog od = new OpenDialog("Open FITS...", path);
        String directory = od.getDirectory();
        fileName = od.getFileName();
        if (fileName == null)
        {
            throw new IOException("Null filename.");
        }
        IJ.showStatus("Opening: " + directory + fileName);
        IJ.log("Opening: " + directory + fileName);
        fitsDecoder = new FitsDecoder(directory, fileName);
        fileInfo = null;
        try
        {
            fileInfo = fitsDecoder.getInfo();
        }
        catch (IOException e)
        {
            IJ.log(e.getMessage());
        }

        fits = new Fits(directory + fileName);
        BasicHDU[] bhdu = null;

        try
        {
            bhdu = fits.read();
        }
        catch (Exception e)
        {
            // if nasa library does not work try classical ImageJ reading
            FITS_Reader reader = new FITS_Reader();
            reader.run(path);
            if (reader.getProcessor() != null)
            {
                reader.show();
            }
            if (IJ.debug)
            {
                System.out.println("PB reading fits");

            }
        }
        return bhdu;

    }

    /**
     * Gets the locationAsString attribute of the FITS object
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @return The locationAsString value
     */
    public String getLocationAsString(int x, int y)
    {
        String s;
        if (wcs != null)
        {
            double[] in = new double[2];
            in[0] = (double) (x);
            in[1] = getProcessor().getHeight() - y - 1.0;
            //in[2]=0.0;
            double[] out = wcs.inverse().transform(in);
            double[] coord = new double[2];
            skyview.geometry.Util.coord(out, coord);
            CoordinateFormatter cf = new CoordinateFormatter();
            String[] ra = cf.sexagesimal(Math.toDegrees(coord[0]) / 15.0, 8).split(" ");
            String[] dec = cf.sexagesimal(Math.toDegrees(coord[1]), 8).split(" ");

            s = "x=" + x + ",y=" + y + " (RA=" + ra[0] + "h" + ra[1] + "m" + ra[2] + "s,  DEC="
                    + dec[0] + "° " + dec[1] + "' " + dec[2] + "\"" + ")";

        }
        else
        {
            s = "x=" + x + " y=" + y;
        }
        if (getStackSize() > 1)
        {
            s += " z=" + (getCurrentSlice() - 1);
        }
        return s;
    }

    /**
     * Gets the string attribute of the FITS class
     *
     * @param length Description of the Parameter
     * @param f      Description of the Parameter
     * @return The string value
     * @throws IOException Description of the Exception
     */
    static String getString(int length, RandomAccessFile f)
            throws IOException
    {
        byte[] b = new byte[length];
        f.read(b);
        return new String(b);
    }

}
