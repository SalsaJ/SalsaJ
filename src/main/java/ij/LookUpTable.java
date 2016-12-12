//EU_HOU
package ij;
import java.awt.*;
import java.awt.image.*;
import ij.process.*;

/**
 *  This class represents a color look-up table.
 *
 *@author     Thomas
 *@created    19 novembre 2007
 */
public class LookUpTable extends Object {
	private int width, height;
	private byte[] pixels;
	private int mapSize = 0;
	private ColorModel cm;
	private byte[] rLUT, gLUT, bLUT;


	/**
	 *  Constructs a LookUpTable object from an AWT Image.
	 *
	 *@param  img  Description of the Parameter
	 */
	public LookUpTable(Image img) {
	PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);

		try {
			pg.grabPixels();
			cm = pg.getColorModel();
		} catch (InterruptedException e) {}
		;
		getColors(cm);
	}


	/**
	 *  Constructs a LookUpTable object from a ColorModel.
	 *
	 *@param  cm  Description of the Parameter
	 */
	public LookUpTable(ColorModel cm) {
		this.cm = cm;
		getColors(cm);
	}


	/**
	 *  Gets the colors attribute of the LookUpTable object
	 *
	 *@param  cm  Description of the Parameter
	 */
	void getColors(ColorModel cm) {
		if (cm instanceof IndexColorModel) {
		IndexColorModel m = (IndexColorModel) cm;

			mapSize = m.getMapSize();
			rLUT = new byte[mapSize];
			gLUT = new byte[mapSize];
			bLUT = new byte[mapSize];
			m.getReds(rLUT);
			m.getGreens(gLUT);
			m.getBlues(bLUT);
		}
	}


	/**
	 *  Gets the mapSize attribute of the LookUpTable object
	 *
	 *@return    The mapSize value
	 */
	public int getMapSize() {
		return mapSize;
	}


	/**
	 *  Gets the reds attribute of the LookUpTable object
	 *
	 *@return    The reds value
	 */
	public byte[] getReds() {
		return rLUT;
	}


	/**
	 *  Gets the greens attribute of the LookUpTable object
	 *
	 *@return    The greens value
	 */
	public byte[] getGreens() {
		return gLUT;
	}


	/**
	 *  Gets the blues attribute of the LookUpTable object
	 *
	 *@return    The blues value
	 */
	public byte[] getBlues() {
		return bLUT;
	}


	/**
	 *  Gets the colorModel attribute of the LookUpTable object
	 *
	 *@return    The colorModel value
	 */
	public ColorModel getColorModel() {
		return cm;
	}


	/**
	 *  Returns <code>true</code> if this is a 256 entry grayscale LUT.
	 *
	 *@return    The grayscale value
	 *@see       ij.process.ImageProcessor#isColorLut
	 */
	public boolean isGrayscale() {
	boolean isGray = true;

		if (mapSize < 256) {
			return false;
		}
		for (int i = 0; i < mapSize; i++) {
			if ((rLUT[i] != gLUT[i]) || (gLUT[i] != bLUT[i])) {
				isGray = false;
			}
		}
		return isGray;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g       Description of the Parameter
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 */
	public void drawColorBar(Graphics g, int x, int y, int width, int height) {
		if (mapSize == 0) {
			return;
		}
	ColorProcessor cp = new ColorProcessor(width, height);
	double scale = 256.0 / mapSize;

		for (int i = 0; i < 256; i++) {
		int index = (int) (i / scale);

			cp.setColor(new Color(rLUT[index] & 0xff, gLUT[index] & 0xff, bLUT[index] & 0xff));
			cp.moveTo(i, 0);
			cp.lineTo(i, height);
		}
		g.drawImage(cp.createImage(), x, y, null);
		g.setColor(Color.black);
		g.drawRect(x, y, width, height);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  g       Description of the Parameter
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 *@param  imin    Description of the Parameter
	 *@param  imax    Description of the Parameter
	 */
	//EU_HOU ADD
	public void drawVertColorBar(Graphics g, int x, int y, int width, int height, double imin, double imax) {
		if (mapSize == 0) {
			return;
		}
	ColorProcessor cp = new ColorProcessor((3 * width) / 5, height);
	double scale = 256.0 / mapSize;
	double st = height / 256.0;
		cp.setLineWidth((int) (st + .5));
	int w2 = (3 * width) / 5;
		g.setColor(Color.black);
		for (int i = 0; i < 256; i++) {
		int y2 = height - (int) (i * st);
		int index = (int) (i / scale);
			cp.setColor(new Color(rLUT[index] & 0xff, gLUT[index] & 0xff, bLUT[index] & 0xff));
			cp.moveTo(0, y2);
			cp.lineTo(w2, y2);

			if ((i % 32) == 0) {
				g.drawLine(x + w2, y + y2, x + width, y + y2);
				g.drawString("" + (long) (imin + ((double) i * (imax - imin)) / 256.), x + w2 + 5, y + y2);
			}
		}
		g.drawImage(cp.createImage(), x, y, null);
		g.drawRect(x, y, width, height);
		g.drawString("" + (long) imax, x + w2 + 5, y);

	}


	/**
	 *  Description of the Method
	 *
	 *@param  ip      Description of the Parameter
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 */
	public void drawUnscaledColorBar(ImageProcessor ip, int x, int y, int width, int height) {
	ImageProcessor bar = null;

		if (ip instanceof ColorProcessor) {
			bar = new ColorProcessor(width, height);
		} else {
			bar = new ByteProcessor(width, height);
		}
		if (mapSize == 0) {//no color table; draw a grayscale bar
			for (int i = 0; i < 256; i++) {
				bar.setColor(new Color(i, i, i));
				bar.moveTo(i, 0);
				bar.lineTo(i, height);
			}
		} else {
			for (int i = 0; i < mapSize; i++) {
				bar.setColor(new Color(rLUT[i] & 0xff, gLUT[i] & 0xff, bLUT[i] & 0xff));
				bar.moveTo(i, 0);
				bar.lineTo(i, height);
			}
		}
		ip.insert(bar, x, y);
		ip.setColor(Color.black);
		ip.drawRect(x - 1, y, width + 2, height);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  invert  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	public static ColorModel createGrayscaleColorModel(boolean invert) {
	byte[] rLUT = new byte[256];
	byte[] gLUT = new byte[256];
	byte[] bLUT = new byte[256];

		if (invert) {
			for (int i = 0; i < 256; i++) {
				rLUT[255 - i] = (byte) i;
				gLUT[255 - i] = (byte) i;
				bLUT[255 - i] = (byte) i;
			}
		} else {
			for (int i = 0; i < 256; i++) {
				rLUT[i] = (byte) i;
				gLUT[i] = (byte) i;
				bLUT[i] = (byte) i;
			}
		}
		return (new IndexColorModel(8, 256, rLUT, gLUT, bLUT));
	}

}

