package ij.plugin;

import java.awt.*;
import java.io.*;
import ij.*;
import ij.io.*;
import ij.process.*;
import ij.measure.*;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Description of the Class
 *
 * @author Thomas @created 26 novembre 2007
 */
public class FitsDecoder {

    private String directory, fileName;
    private DataInputStream f;
    private StringBuffer info = new StringBuffer(512);
    private double bscale = 1.0, bzero = 0.0, restfreq = 0.0, crval1 = 0.0, crval2 = 0.0, crval3 = 0.0, cdelt1 = 0.0, cdelt2 = 0.0, cdelt3 = 0.0, crpix1 = 0.0,
            deltav = 0.0, velolsr = 0.0, crpix2 = 0.0, naxis1 = 0.0, naxis2 = 0.0,
            crpix3 = 0.0;
    private String ctype1 = "";
    private String cunit1 = "";
    private String Telescop = "";

    /**
     * Constructor for the FitsDecoder object
     *
     * @param directory Description of the Parameter
     * @param fileName Description of the Parameter
     */
    public FitsDecoder(String directory, String fileName) {
        this.directory = directory;
        this.fileName = fileName;


    }

    /**
     * Gets the info attribute of the FitsDecoder object
     *
     * @return The info value
     * @exception IOException Description of the Exception
     */
    public FileInfo getInfo() throws IOException {
        FileInfo fi = new FileInfo();
        fi.fileFormat = fi.FITS;
        fi.fileName = fileName;
        fi.directory = directory;
        fi.width = 0;
        fi.height = 1;
        fi.offset = 0;
        fi.url = "";

        DataInputStream stream = null;
        if (directory.startsWith("http")) {
            IJ.log("Reading over the Internet " + directory + " " + fileName);
            try {
                stream = new DataInputStream((new URL(directory + fileName)).openStream());
                fi.url = directory;
            } catch (IOException ex) {
                Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                stream = new DataInputStream(new FileInputStream(directory + fileName));
            } catch (FileNotFoundException ex) {
                Logger.getLogger(RadioSpectrum_Reader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (stream == null) {
            return null;
        }
        f = stream;
        //f = new DataInputStream(new FileInputStream(directory + fileName));
        String s = getString(80);
        info.append(s + "\n");
        if (!s.startsWith("SIMPLE")) {
            f.close();
            return null;
        }
        int count = 1;

        do {
            count++;

            s = getString(80);

            info.append(s + "\n");
            if (s.startsWith("BITPIX")) {

                int bitsPerPixel = getInteger(s);
                if (bitsPerPixel == 8) {
                    fi.fileType = FileInfo.GRAY8;
                } else if (bitsPerPixel == 16) {
                    fi.fileType = FileInfo.GRAY16_SIGNED;
                    // By oli, for these -16 bitpix zoombie files, do as for
                } else if (bitsPerPixel == -16) {
                    fi.fileType = FileInfo.GRAY16_SIGNED;
                    //fi.fileType = FileInfo.GRAY16_SIGNEDBAD;
                    // end by Oli
                } else if (bitsPerPixel == 32) {
                    fi.fileType = FileInfo.GRAY32_INT;
                } else if (bitsPerPixel == -32) {
                    fi.fileType = FileInfo.GRAY32_FLOAT;
                } else if (bitsPerPixel == -64) {
                    fi.fileType = FileInfo.GRAY64_FLOAT;
                } else {
                    //EU_HOU Bundle
                    IJ.error("BITPIX must be 8, 16, 32, -32 (float) or -64 (double).");
                    f.close();
                    return null;
                }
            } else if (s.startsWith("NAXIS1")) {
                fi.width = getInteger(s);

            } else if (s.startsWith("NAXIS2")) {
                fi.height = getInteger(s);

            } else if (s.startsWith("NAXIS3")) {//for multi-frame fits
                fi.nImages = getInteger(s);
            } else if (s.startsWith("BSCALE")) {
                bscale = getFloat(s);
            } else if (s.startsWith("BZERO")) {
                bzero = getFloat(s);
            } else if (s.startsWith("RESTFREQ")) {
                restfreq = getFloat(s);
            } else if (s.startsWith("CRVAL1")) {
                crval1 = getFloat(s);
            } else if (s.startsWith("CRVAL2")) {
                crval2 = getFloat(s);
            } else if (s.startsWith("CRVAL3")) {
                crval3 = getFloat(s);
            } else if (s.startsWith("CDELT1")) {
                cdelt1 = getFloat(s);
            } else if (s.startsWith("CDELT1")) {
                cdelt1 = getFloat(s);
            } else if (s.startsWith("CDELT2")) {
                cdelt2 = getFloat(s);
            } else if (s.startsWith("CDELT3")) {
                cdelt3 = getFloat(s);
		//} else if (s.startsWith("VELO-LSR") || s.startsWith("VLSR")) {
	    } else if (s.startsWith("VELO-LSR")) {
                velolsr = getFloat(s);
		// System.out.print("DEBUG FITSDecoder velolsr="+velolsr+"\n");
            } else if (s.startsWith("DELTAV")) {
                deltav = getFloat(s);

            } else if (s.startsWith("CRPIX1")) {
                crpix1 = getFloat(s);
            } else if (s.startsWith("CRPIX2")) {
                crpix2 = getFloat(s);
            } else if (s.startsWith("CRPIX3")) {
                crpix3 = getFloat(s);
                // } else if (s.startsWith("NAXIS1")) {
                //   naxis1 = getFloat(s);
                //} else if (s.startsWith("NAXIS2")) {
                //  naxis2 = getFloat(s);
            } else if (s.startsWith("CTYPE1")) {
                ctype1 = s.trim();
            } else if (s.startsWith("CUNIT1")) {
                cunit1 = s.trim();
            } else if (s.startsWith("TELESCOP")) {
                Telescop = s.trim();
            }


            if (count > 360 && fi.width == 0) {
                f.close();
                return null;
            }
        } while (!s.startsWith("END"));
        f.close();
        fi.offset = 2880 + 2880 * (((count * 80) - 1) / 2880);
        return fi;
    }

    /**
     * Gets the string attribute of the FitsDecoder object
     *
     * @param length Description of the Parameter
     * @return The string value
     * @exception IOException Description of the Exception
     */
    String getString(int length) throws IOException {
        byte[] b = new byte[length];
        f.read(b);
        return new String(b);
    }

    /**
     * Gets the integer attribute of the FitsDecoder object
     *
     * @param s Description of the Parameter
     * @return The integer value
     */
    int getInteger(String s) {
        s = s.substring(10, 30);
        s = s.trim();
        return Integer.parseInt(s);
    }

    /**
     * Gets the float attribute of the FitsDecoder object
     *
     * @param s Description of the Parameter
     * @return The float value
     */
    double getFloat(String s) {
        s = s.substring(10, 30);
        s = s.trim();
        Double d;
        try {
            d = new Double(s);
        } catch (NumberFormatException e) {
            d = null;
        }
        if (d != null) {
            return (d.doubleValue());
        } else {
            return 0.0;
        }
    }

    /**
     * Gets the bscale attribute of the FitsDecoder object
     *
     * @return The bscale value
     */
    public double getBscale() {
        return bscale;
    }

    public String getTelescop() {
        return Telescop;
    }

    /**
     * Gets the bzero attribute of the FitsDecoder object
     *
     * @return The bzero value
     */
    public double getBzero() {
        return bzero;
    }

    /**
     * Gets the restfreq attribute of the FitsDecoder object
     *
     * @return The restfreq value
     */
    public double getRestfreq() {
        return restfreq;
    }

    /**
     * Gets the crval1 attribute of the FitsDecoder object
     *
     * @return The crval1 value
     */
    public double getCrval1() {
        return crval1;
    }

    /**
     * Gets the crval2 attribute of the FitsDecoder object
     *
     * @return The crval2 value
     */
    public double getCrval2() {
        return crval2;
    }

    /**
     * Gets the crval3 attribute of the FitsDecoder object
     *
     * @return The crval3 value
     */
    public double getCrval3() {
        return crval3;
    }

    /**
     * Gets the cdelt1 attribute of the FitsDecoder object
     *
     * @return The cdelt1 value
     */
    public double getCdelt1() {
        return cdelt1;
    }

    /**
     * Gets the crpix1 attribute of the FitsDecoder object
     *
     * @return The crpix1 value
     */
    public double getCrpix1() {
        return crpix1;
    }

    /**
     * Gets the velolsr attribute of the FitsDecoder object
     *
     * @return The velolsr value
     */
    public double getVelolsr() {
        return velolsr;
    }

    /**
     * Gets the deltav attribute of the FitsDecoder object
     *
     * @return The deltav value
     */
    public double getDeltav() {
        return deltav;
    }

    /**
     * Gets the headerInfo attribute of the FitsDecoder object
     *
     * @return The headerInfo value
     */
    public String getHeaderInfo() {
        return new String(info);
    }

    public String getCtype1() {
        return ctype1;
    }

    public String getCunit1() {
        return cunit1;
    }
}
