//EU_HOU
package ij.process;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import ij.gui.*;

/**
 *  Objects of the class contain a 16-bit unsigned image and methods that
 *  operate on that image.
 *
 *@author     Thomas
 *@created    21 novembre 2007
 */
public class ShortProcessor extends ImageProcessor {

	private int min, max, snapshotMin, snapshotMax;
	private short[] pixels;
	private short[] snapshotPixels;
	private byte[] pixels8;
	private byte[] LUT;
	private boolean fixedScale;


	/**
	 *  Creates a new ShortProcessor using the specified pixel array and
	 *  ColorModel. Set 'cm' to null to use the default grayscale LUT.
	 *
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 *@param  pixels  Description of the Parameter
	 *@param  cm      Description of the Parameter
	 */
	public ShortProcessor(int width, int height, short[] pixels, ColorModel cm) {
		if (pixels != null && width * height != pixels.length) {
			throw new IllegalArgumentException(WRONG_LENGTH);
		}
		init(width, height, pixels, cm);
	}


	/**
	 *  Creates a blank ShortProcessor using the default grayscale LUT that
	 *  displays zero as black. Call invertLut() to display zero as white.
	 *
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 */
	public ShortProcessor(int width, int height) {
		this(width, height, new short[width * height], null);
	}


	/**
	 *  Creates a ShortProcessor from a TYPE_USHORT_GRAY BufferedImage.
	 *
	 *@param  bi  Description of the Parameter
	 */
	public ShortProcessor(BufferedImage bi) {
		if (bi.getType() != BufferedImage.TYPE_USHORT_GRAY) {
			throw new IllegalArgumentException("Type!=TYPE_USHORT_GRAY");
		}
	WritableRaster raster = bi.getRaster();
	DataBuffer buffer = raster.getDataBuffer();
	short[] data = ((DataBufferUShort) buffer).getData();
		//short[] data2 = new short[data.length];
		//System.arraycopy(data, 0, data2, 0, data.length);
		init(raster.getWidth(), raster.getHeight(), data, null);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 *@param  pixels  Description of the Parameter
	 *@param  cm      Description of the Parameter
	 */
	void init(int width, int height, short[] pixels, ColorModel cm) {
		this.width = width;
		this.height = height;
		this.pixels = pixels;
		this.cm = cm;
		resetRoi();
		if (pixels != null) {
			findMinAndMax();
		}
		fgColor = max;
	}


	/**
	 *  Obsolete. 16 bit images are normally unsigned but signed images can be used
	 *  by subtracting 32768 and using a calibration function to restore the
	 *  original values.
	 *
	 *@param  width     Description of the Parameter
	 *@param  height    Description of the Parameter
	 *@param  pixels    Description of the Parameter
	 *@param  cm        Description of the Parameter
	 *@param  unsigned  Description of the Parameter
	 */
	public ShortProcessor(int width, int height, short[] pixels, ColorModel cm, boolean unsigned) {
		this(width, height, pixels, cm);
	}


	/**
	 *  Obsolete. 16 bit images are normally unsigned but signed images can be used
	 *  by subtracting 32768 and using a calibration function to restore the
	 *  original values.
	 *
	 *@param  width     Description of the Parameter
	 *@param  height    Description of the Parameter
	 *@param  unsigned  Description of the Parameter
	 */
	public ShortProcessor(int width, int height, boolean unsigned) {
		this(width, height);
	}


	/**
	 *  Description of the Method
	 */
	public void findMinAndMax() {
		if (fixedScale) {
			return;
		}
	int size = width * height;
	int value;

		min = 65535;
		max = 0;
		for (int i = 0; i < size; i++) {
			value = pixels[i] & 0xffff;
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
		}
	}


	/**
	 *  Create an 8-bit AWT image by scaling pixels in the range min-max to 0-255.
	 *
	 *@return    Description of the Return Value
	 */
	public Image createImage() {
	boolean firstTime = pixels8 == null;

		if (firstTime || !lutAnimation) {
		// scale from 16-bits to 8-bits
		int size = width * height;

			if (pixels8 == null) {
				pixels8 = new byte[size];
			}
		int value;
		double scale = 256.0 / (max - min + 1);

			for (int i = 0; i < size; i++) {
				value = (pixels[i] & 0xffff) - min;
				if (value < 0) {
					value = 0;
				}
				value = (int) (value * scale);
				if (value > 255) {
					value = 255;
				}
				pixels8[i] = (byte) value;
			}
		}
		if (cm == null) {
			makeDefaultColorModel();
		}
		if (source == null) {
			source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
			source.setAnimated(true);
			source.setFullBufferUpdates(true);
			img = Toolkit.getDefaultToolkit().createImage(source);
		} else if (newPixels) {
			source.newPixels(pixels8, cm, 0, width);
			newPixels = false;
		} else {
			source.newPixels();
		}
		lutAnimation = false;
		return img;
	}


	/**
	 *  Returns a new, blank ShortProcessor with the specified width and height.
	 *
	 *@param  width   Description of the Parameter
	 *@param  height  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	public ImageProcessor createProcessor(int width, int height) {
	ImageProcessor ip2 = new ShortProcessor(width, height, new short[width * height], getColorModel());

		ip2.setMinAndMax(getMin(), getMax());
		return ip2;
	}


	/**
	 *  Description of the Method
	 */
	public void snapshot() {
		snapshotWidth = width;
		snapshotHeight = height;
		snapshotMin = min;
		snapshotMax = max;
		if (snapshotPixels == null || (snapshotPixels != null && snapshotPixels.length != pixels.length)) {
			snapshotPixels = new short[width * height];
		}
		System.arraycopy(pixels, 0, snapshotPixels, 0, width * height);
	}


	/**
	 *  Description of the Method
	 */
	public void reset() {
		if (snapshotPixels == null) {
			return;
		}
		min = snapshotMin;
		max = snapshotMax;
		System.arraycopy(snapshotPixels, 0, pixels, 0, width * height);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  mask  Description of the Parameter
	 */
	public void reset(ImageProcessor mask) {
		if (mask == null || snapshotPixels == null) {
			return;
		}
		if (mask.getWidth() != roiWidth || mask.getHeight() != roiHeight) {
			throw new IllegalArgumentException(maskSizeError(mask));
		}
	byte[] mpixels = (byte[]) mask.getPixels();

		for (int y = roiY, my = 0; y < (roiY + roiHeight); y++, my++) {
		int i = y * width + roiX;
		int mi = my * roiWidth;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				if (mpixels[mi++] == 0) {
					pixels[i] = snapshotPixels[i];
				}
				i++;
			}
		}
	}


	/**
	 *  Sets the snapshotPixels attribute of the ShortProcessor object
	 *
	 *@param  pixels  The new snapshotPixels value
	 */
	public void setSnapshotPixels(Object pixels) {
		snapshotPixels = (short[]) pixels;
		snapshotWidth = width;
		snapshotHeight = height;
	}


	/**
	 *  Gets the snapshotPixels attribute of the ShortProcessor object
	 *
	 *@return    The snapshotPixels value
	 */
	public Object getSnapshotPixels() {
		return snapshotPixels;
	}


	/*
	 *  Obsolete.
	 */
	//public boolean isUnsigned() {
	//	return true;
	//}

	/**
	 *  Returns the smallest displayed pixel value.
	 *
	 *@return    The min value
	 */
	public double getMin() {
		return min;
	}


	/**
	 *  Returns the largest displayed pixel value.
	 *
	 *@return    The max value
	 */
	public double getMax() {
		return max;
	}


	/**
	 *  Sets the min and max variables that control how real pixel values are
	 *  mapped to 0-255 screen values.
	 *
	 *@param  min  The new minAndMax value
	 *@param  max  The new minAndMax value
	 *@see         #resetMinAndMax
	 *@see         ij.plugin.frame.ContrastAdjuster
	 */
	public void setMinAndMax(double min, double max) {
		if (min == 0.0 && max == 0.0) {
			resetMinAndMax();
			return;
		}
		if (min < 0.0) {
			min = 0.0;
		}
		if (max > 65535.0) {
			max = 65535.0;
		}
		this.min = (int) min;
		this.max = (int) max;
		fixedScale = true;
		resetThreshold();
	}


	/**
	 *  Recalculates the min and max values used to scale pixel values to 0-255 for
	 *  display. This ensures that this ShortProcessor is set up to correctly
	 *  display the image.
	 */
	public void resetMinAndMax() {
		fixedScale = false;
		findMinAndMax();
		resetThreshold();
	}


	/**
	 *  Gets the pixel attribute of the ShortProcessor object
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@return    The pixel value
	 */
	public int getPixel(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			return pixels[y * width + x] & 0xffff;
		} else {
			return 0;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public final int get(int x, int y) {
		return pixels[y * width + x] & 0xffff;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public final void set(int x, int y, int value) {
		pixels[y * width + x] = (short) value;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 *@return        Description of the Return Value
	 */
	public final int get(int index) {
		return pixels[index] & 0xffff;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public final void set(int index, int value) {
		pixels[index] = (short) value;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public final float getf(int x, int y) {
		return pixels[y * width + x] & 0xffff;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public final void setf(int x, int y, float value) {
		pixels[y * width + x] = (short) value;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 *@return        Description of the Return Value
	 */
	public final float getf(int index) {
		return pixels[index] & 0xffff;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  index  Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public final void setf(int index, float value) {
		pixels[index] = (short) value;
	}


	/**
	 *  Uses bilinear interpolation to find the pixel value at real coordinates
	 *  (x,y).
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@return    The interpolatedPixel value
	 */
	public double getInterpolatedPixel(double x, double y) {
		if (x < 0.0) {
			x = 0.0;
		}
		if (x >= width - 1.0) {
			x = width - 1.001;
		}
		if (y < 0.0) {
			y = 0.0;
		}
		if (y >= height - 1.0) {
			y = height - 1.001;
		}
		return getInterpolatedPixel(x, y, pixels);
	}


	/**
	 *  Stores the specified value at (x,y). Does nothing if (x,y) is outside the
	 *  image boundary. Values outside the range 0-65535 are clipped.
	 *
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public void putPixel(int x, int y, int value) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			if (value > 65535) {
				value = 65535;
			}
			if (value < 0) {
				value = 0;
			}
			pixels[y * width + x] = (short) value;
		}
	}


	/**
	 *  Stores the specified real value at (x,y). Does nothing if (x,y) is outside
	 *  the image boundary. Values outside the range 0-65535 (-32768-32767 for
	 *  signed images) are clipped. Support for signed values requires a
	 *  calibration table, which is set up automatically with PlugInFilters.
	 *
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public void putPixelValue(int x, int y, double value) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			if (cTable != null && cTable[0] == -32768f) {// signed image
				value += 32768.0;
			}
			if (value > 65535.0) {
				value = 65535.0;
			} else if (value < 0.0) {
				value = 0.0;
			}
			pixels[y * width + x] = (short) (value + 0.5);
		}
	}


	/**
	 *  Draws a pixel in the current foreground color.
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 */
	public void drawPixel(int x, int y) {
		if (x >= clipXMin && x <= clipXMax && y >= clipYMin && y <= clipYMax) {
			putPixel(x, y, fgColor);
		}
	}


	/**
	 *  Returns the value of the pixel at (x,y) as a float. For signed images,
	 *  returns a signed value if a calibration table has been set using
	 *  setCalibrationTable() (this is done automatically in PlugInFilters).
	 *
	 *@param  x  Description of the Parameter
	 *@param  y  Description of the Parameter
	 *@return    The pixelValue value
	 */
	public float getPixelValue(int x, int y) {
		if (x >= 0 && x < width && y >= 0 && y < height) {
			if (cTable == null) {
				return pixels[y * width + x] & 0xffff;
			} else {
				return cTable[pixels[y * width + x] & 0xffff];
			}
		} else {
			return 0f;
		}
	}


	/**
	 *  Returns a reference to the short array containing this image's pixel data.
	 *  To avoid sign extension, the pixel values must be accessed using a mask
	 *  (e.g. int i = pixels[j]&0xffff).
	 *
	 *@return    The pixels value
	 */
	public Object getPixels() {
		return (Object) pixels;
	}


	/**
	 *  Returns a copy of the pixel data. Or returns a reference to the snapshot
	 *  buffer if it is not null and 'snapshotCopyMode' is true.
	 *
	 *@return    The pixelsCopy value
	 *@see       ImageProcessor#snapshot
	 *@see       ImageProcessor#setSnapshotCopyMode
	 */
	public Object getPixelsCopy() {
		if (snapshotPixels != null && snapshotCopyMode) {
			snapshotCopyMode = false;
			return snapshotPixels;
		} else {
		short[] pixels2 = new short[width * height];

			System.arraycopy(pixels, 0, pixels2, 0, width * height);
			return pixels2;
		}
	}


	/**
	 *  Sets the pixels attribute of the ShortProcessor object
	 *
	 *@param  pixels  The new pixels value
	 */
	public void setPixels(Object pixels) {
		this.pixels = (short[]) pixels;
		resetPixels(pixels);
		if (pixels == null) {
			snapshotPixels = null;
		}
		if (pixels == null) {
			pixels8 = null;
		}
	}


	/**
	 *  Gets the row2 attribute of the ShortProcessor object
	 *
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  data    Description of the Parameter
	 *@param  length  Description of the Parameter
	 */
	void getRow2(int x, int y, int[] data, int length) {
	int value;

		for (int i = 0; i < length; i++) {
			data[i] = pixels[y * width + x + i] & 0xffff;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  data    Description of the Parameter
	 *@param  length  Description of the Parameter
	 */
	void putColumn2(int x, int y, int[] data, int length) {
	int value;

		for (int i = 0; i < length; i++) {
			pixels[(y + i) * width + x] = (short) data[i];
		}
	}


	/**
	 *  Copies the image contained in 'ip' to (xloc, yloc) using one of the
	 *  transfer modes defined in the Blitter interface.
	 *
	 *@param  ip    Description of the Parameter
	 *@param  xloc  Description of the Parameter
	 *@param  yloc  Description of the Parameter
	 *@param  mode  Description of the Parameter
	 */
	public void copyBits(ImageProcessor ip, int xloc, int yloc, int mode) {
		if (!(ip instanceof ShortProcessor)) {
			throw new IllegalArgumentException("16-bit image required");
		}
		new ShortBlitter(this).copyBits(ip, xloc, yloc, mode);
	}


	/**
	 *  Transforms the pixel data using a 65536 entry lookup table.
	 *
	 *@param  lut  Description of the Parameter
	 */
	public void applyTable(int[] lut) {
		if (lut.length != 65536) {
			throw new IllegalArgumentException("lut.length!=65536");
		}
	int lineStart;
	int lineEnd;
	int v;

		for (int y = roiY; y < (roiY + roiHeight); y++) {
			lineStart = y * width + roiX;
			lineEnd = lineStart + roiWidth;
			for (int i = lineEnd; --i >= lineStart; ) {
				v = lut[pixels[i] & 0xffff];
				pixels[i] = (short) v;
			}
		}
		findMinAndMax();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  op     Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	private void process(int op, double value) {
	int v1;
	int v2;
	double range = max - min;
	boolean resetMinMax = roiWidth == width && roiHeight == height && !(op == FILL);
	int offset = cTable != null && cTable[0] == -32768f ? 32768 : 0;// signed images have 32768 offset
	int min2 = min - offset;
	int max2 = max - offset;
	int fgColor2 = fgColor - offset;

		for (int y = roiY; y < (roiY + roiHeight); y++) {
		int i = y * width + roiX;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				v1 = (pixels[i] & 0xffff) - offset;
				switch (op) {
								case INVERT:
									v2 = max2 - (v1 - min2);
									break;
								case FILL:
									v2 = fgColor2;
									break;
								case ADD:
									v2 = v1 + (int) value;
									break;
								case MULT:
									v2 = (int) Math.round(v1 * value);
									break;
								case AND:
									v2 = v1 & (int) value;
									break;
								case OR:
									v2 = v1 | (int) value;
									break;
								case XOR:
									v2 = v1 ^ (int) value;
									break;
								case GAMMA:
									if (range <= 0.0 || v1 == min2) {
										v2 = v1;
									} else {
										v2 = (int) (Math.exp(value * Math.log((v1 - min2) / range)) * range + min2);
									}
									break;
								case LOG:
									if (v1 <= 0) {
										v2 = 0;
									} else {
										v2 = (int) (Math.log(v1) * (max2 / Math.log(max2)));
									}
									break;
								case EXP:
									v2 = (int) (Math.exp(v1 * (Math.log(max2) / max2)));
									break;
								case SQR:
									v2 = v1 * v1;
									break;
								case SQRT:
									v2 = (int) Math.sqrt(v1);
									break;
								case ABS:
									v2 = (int) Math.abs(v1);
									break;
								case MINIMUM:
									if (v1 < value) {
										v2 = (int) value;
									} else {
										v2 = v1;
									}
									break;
								case MAXIMUM:
									if (v1 > value) {
										v2 = (int) value;
									} else {
										v2 = v1;
									}
									break;
								default:
									v2 = v1;
				}
				v2 += offset;
				if (v2 < 0) {
					v2 = 0;
				}
				if (v2 > 65535) {
					v2 = 65535;
				}
				pixels[i++] = (short) v2;
			}
		}
		if (resetMinMax) {
			findMinAndMax();
		}
	}


	/**
	 *  Description of the Method
	 */
	public void invert() {
		resetMinAndMax();
		process(INVERT, 0.0);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void add(int value) {
		process(ADD, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void add(double value) {
		process(ADD, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void multiply(double value) {
		process(MULT, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void and(int value) {
		process(AND, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void or(int value) {
		process(OR, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void xor(int value) {
		process(XOR, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void gamma(double value) {
		process(GAMMA, value);
	}


	/**
	 *  Description of the Method
	 */
	public void log() {
		process(LOG, 0.0);
	}


	/**
	 *  Description of the Method
	 */
	public void exp() {
		process(EXP, 0.0);
	}


	/**
	 *  Description of the Method
	 */
	public void sqr() {
		process(SQR, 0.0);
	}


	/**
	 *  Description of the Method
	 */
	public void sqrt() {
		process(SQRT, 0.0);
	}


	/**
	 *  Description of the Method
	 */
	public void abs() {
		process(ABS, 0.0);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void min(double value) {
		process(MINIMUM, value);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of the Parameter
	 */
	public void max(double value) {
		process(MAXIMUM, value);
	}


	/**
	 *  Fills the current rectangular ROI.
	 */
	public void fill() {
		process(FILL, 0.0);
	}


	/**
	 *  Fills pixels that are within roi and part of the mask. Does nothing if the
	 *  mask is not the same as the ROI.
	 *
	 *@param  mask  Description of the Parameter
	 */
	public void fill(ImageProcessor mask) {
		if (mask == null) {
			fill();
			return;
		}
	int roiWidth = this.roiWidth;
	int roiHeight = this.roiHeight;
	int roiX = this.roiX;
	int roiY = this.roiY;

		if (mask.getWidth() != roiWidth || mask.getHeight() != roiHeight) {
			return;
		}
	byte[] mpixels = (byte[]) mask.getPixels();

		for (int y = roiY, my = 0; y < (roiY + roiHeight); y++, my++) {
		int i = y * width + roiX;
		int mi = my * roiWidth;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				if (mpixels[mi++] != 0) {
					pixels[i] = (short) fgColor;
				}
				i++;
			}
		}
	}


	/**
	 *  3x3 convolution contributed by Glynne Casteel.
	 *
	 *@param  kernel  Description of the Parameter
	 */
	public void convolve3x3(int[] kernel) {
	int p1;
	int p2;
	int p3;
	int
				p4;
	int p5;
	int p6;
	int
				p7;
	int p8;
	int p9;
	int k1 = kernel[0];
	int k2 = kernel[1];
	int k3 = kernel[2];
	int
				k4 = kernel[3];
	int k5 = kernel[4];
	int k6 = kernel[5];
	int
				k7 = kernel[6];
	int k8 = kernel[7];
	int k9 = kernel[8];

	int scale = 0;

		for (int i = 0; i < kernel.length; i++) {
			scale += kernel[i];
		}
		if (scale == 0) {
			scale = 1;
		}
	int inc = roiHeight / 25;

		if (inc < 1) {
			inc = 1;
		}

	short[] pixels2 = (short[]) getPixelsCopy();
	int offset;
	int sum;
	int rowOffset = width;

		for (int y = yMin; y <= yMax; y++) {
			offset = xMin + y * width;
			p1 = 0;
			p2 = pixels2[offset - rowOffset - 1] & 0xffff;
			p3 = pixels2[offset - rowOffset] & 0xffff;
			p4 = 0;
			p5 = pixels2[offset - 1] & 0xffff;
			p6 = pixels2[offset] & 0xffff;
			p7 = 0;
			p8 = pixels2[offset + rowOffset - 1] & 0xffff;
			p9 = pixels2[offset + rowOffset] & 0xffff;

			for (int x = xMin; x <= xMax; x++) {
				p1 = p2;
				p2 = p3;
				p3 = pixels2[offset - rowOffset + 1] & 0xffff;
				p4 = p5;
				p5 = p6;
				p6 = pixels2[offset + 1] & 0xffff;
				p7 = p8;
				p8 = p9;
				p9 = pixels2[offset + rowOffset + 1] & 0xffff;
				sum = k1 * p1 + k2 * p2 + k3 * p3
						 + k4 * p4 + k5 * p5 + k6 * p6
						 + k7 * p7 + k8 * p8 + k9 * p9;
				sum /= scale;
				if (sum > 65535) {
					sum = 65535;
				}
				if (sum < 0) {
					sum = 0;
				}
				pixels[offset++] = (short) sum;
			}
			if (y % inc == 0) {
				showProgress((double) (y - roiY) / roiHeight);
			}
		}
		showProgress(1.0);
	}


	/**
	 *  Filters using a 3x3 neighborhood.
	 *
	 *@param  type  Description of the Parameter
	 */
	public void filter(int type) {
	int p1;
	int p2;
	int p3;
	int p4;
	int p5;
	int p6;
	int p7;
	int p8;
	int p9;
	int inc = roiHeight / 25;

		if (inc < 1) {
			inc = 1;
		}

	short[] pixels2 = (short[]) getPixelsCopy();
	int offset;
	int sum1;
	int sum2;
	int sum = 0;
	int rowOffset = width;

		for (int y = yMin; y <= yMax; y++) {
			offset = xMin + y * width;
			p1 = 0;
			p2 = pixels2[offset - rowOffset - 1] & 0xffff;
			p3 = pixels2[offset - rowOffset] & 0xffff;
			p4 = 0;
			p5 = pixels2[offset - 1] & 0xffff;
			p6 = pixels2[offset] & 0xffff;
			p7 = 0;
			p8 = pixels2[offset + rowOffset - 1] & 0xffff;
			p9 = pixels2[offset + rowOffset] & 0xffff;

			for (int x = xMin; x <= xMax; x++) {
				p1 = p2;
				p2 = p3;
				p3 = pixels2[offset - rowOffset + 1] & 0xffff;
				p4 = p5;
				p5 = p6;
				p6 = pixels2[offset + 1] & 0xffff;
				p7 = p8;
				p8 = p9;
				p9 = pixels2[offset + rowOffset + 1] & 0xffff;

				switch (type) {
								case BLUR_MORE:
									sum = (p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8 + p9) / 9;
									break;
								case FIND_EDGES:
									sum1 = p1 + 2 * p2 + p3 - p7 - 2 * p8 - p9;
									sum2 = p1 + 2 * p4 + p7 - p3 - 2 * p6 - p9;
									sum = (int) Math.sqrt(sum1 * sum1 + sum2 * sum2);
									break;
				}

				pixels[offset++] = (short) sum;
			}
			if (y % inc == 0) {
				showProgress((double) (y - roiY) / roiHeight);
			}
		}
		if (type == BLUR_MORE) {
			showProgress(1.0);
		} else {
			findMinAndMax();
		}
	}


	/**
	 *  Rotates the image or ROI 'angle' degrees clockwise.
	 *
	 *@param  angle  Description of the Parameter
	 *@see           ImageProcessor#setInterpolate
	 */
	public void rotate(double angle) {
	short[] pixels2 = (short[]) getPixelsCopy();
	double centerX = roiX + (roiWidth - 1) / 2.0;
	double centerY = roiY + (roiHeight - 1) / 2.0;
	int xMax = roiX + this.roiWidth - 1;

	double angleRadians = -angle / (180.0 / Math.PI);
	double ca = Math.cos(angleRadians);
	double sa = Math.sin(angleRadians);
	double tmp1 = centerY * sa - centerX * ca;
	double tmp2 = -centerX * sa - centerY * ca;
	double tmp3;
	double tmp4;
	double xs;
	double ys;
	int index;
	int ixs;
	int iys;
	double dwidth = width;
	double dheight = height;
	double xlimit = width - 1.0;
	double xlimit2 = width - 1.001;
	double ylimit = height - 1.0;
	double ylimit2 = height - 1.001;
	// zero is 32768 for signed images
	int background = cTable != null && cTable[0] == -32768 ? 32768 : 0;

		for (int y = roiY; y < (roiY + roiHeight); y++) {
			index = y * width + roiX;
			tmp3 = tmp1 - y * sa + centerX;
			tmp4 = tmp2 + y * ca + centerY;
			for (int x = roiX; x <= xMax; x++) {
				xs = x * ca + tmp3;
				ys = x * sa + tmp4;
				if ((xs >= -0.01) && (xs < dwidth) && (ys >= -0.01) && (ys < dheight)) {
					if (interpolate) {
						if (xs < 0.0) {
							xs = 0.0;
						}
						if (xs >= xlimit) {
							xs = xlimit2;
						}
						if (ys < 0.0) {
							ys = 0.0;
						}
						if (ys >= ylimit) {
							ys = ylimit2;
						}
						pixels[index++] = (short) (getInterpolatedPixel(xs, ys, pixels2) + 0.5);
					} else {
						ixs = (int) (xs + 0.5);
						iys = (int) (ys + 0.5);
						if (ixs >= width) {
							ixs = width - 1;
						}
						if (iys >= height) {
							iys = height - 1;
						}
						pixels[index++] = pixels2[width * iys + ixs];
					}
				} else {
					pixels[index++] = (short) background;
				}
			}
			if (y % 30 == 0) {
				showProgress((double) (y - roiY) / roiHeight);
			}
		}
		showProgress(1.0);
	}


	/*
	 *  EU_HOU Add
	 */
	/**
	 *  Translates the image or ROI on x and y.
	 *
	 *@param  tx  Description of the Parameter
	 *@param  ty  Description of the Parameter
	 *@see        ImageProcessor#setInterpolate
	 */
	public void translate(double tx, double ty) {
	//System.out.println("Short");
	short[] pixels2 = (short[]) getPixelsCopy();
	int xMax = roiX + this.roiWidth - 1;

	double xs;

	double ys;
	int index;
	int ixs;
	int iys;
	double dwidth = width;
	double dheight = height;
	double xlimit = width - 1.0;
	double xlimit2 = width - 1.001;
	double ylimit = height - 1.0;
	double ylimit2 = height - 1.001;

	int background = cTable != null && cTable[0] == -32768 ? 32768 : 0;
		//System.out.println(background);

		for (int y = roiY; y < (roiY + roiHeight); y++) {
			index = y * width + roiX;
			for (int x = roiX; x <= xMax; x++) {
				xs = x - tx;
				ys = y + ty;
				if ((xs >= (double) roiX) && (xs < (double) (roiX + roiWidth)) && (ys >= (double) roiY) && (ys < (double) (roiY + roiHeight))) {
//				if ((xs>=-0.01) && (xs<dwidth) && (ys>=-0.01) && (ys<dheight)) {
					if (interpolate) {
						if (xs < 0.0) {
							xs = 0.0;
						}
						if (xs >= xlimit) {
							xs = xlimit2;
						}
						if (ys < 0.0) {
							ys = 0.0;
						}
						if (ys >= ylimit) {
							ys = ylimit2;
						}
						pixels[index++] = (short) (getInterpolatedPixel(xs, ys, pixels2) + 0.5);
					} else {
						ixs = (int) (xs + 0.5);
						iys = (int) (ys + 0.5);
						if (ixs >= width) {
							ixs = width - 1;
						}
						if (iys >= height) {
							iys = height - 1;
						}
						pixels[index++] = pixels2[width * iys + ixs];
					}
				} else {
					pixels[index++] = (short) background;
				}
			}
			if (y % 30 == 0) {
				showProgress((double) (y - roiY) / roiHeight);
			}
		}
		hideProgress();
	}


	/*
	 *  EU_HOU END
	 */
	/**
	 *  Description of the Method
	 */
	public void flipVertical() {
	int index1;
	int index2;
	short tmp;

		for (int y = 0; y < roiHeight / 2; y++) {
			index1 = (roiY + y) * width + roiX;
			index2 = (roiY + roiHeight - 1 - y) * width + roiX;
			for (int i = 0; i < roiWidth; i++) {
				tmp = pixels[index1];
				pixels[index1++] = pixels[index2];
				pixels[index2++] = tmp;
			}
		}
	}


	/**
	 *  Scales the image or selection using the specified scale factors.
	 *
	 *@param  xScale  Description of the Parameter
	 *@param  yScale  Description of the Parameter
	 *@see            ImageProcessor#setInterpolate
	 */
	public void scale(double xScale, double yScale) {
	double xCenter = roiX + roiWidth / 2.0;
	double yCenter = roiY + roiHeight / 2.0;
	int xmin;
	int xmax;
	int ymin;
	int ymax;

		if ((xScale > 1.0) && (yScale > 1.0)) {
			//expand roi
			xmin = (int) (xCenter - (xCenter - roiX) * xScale);
			if (xmin < 0) {
				xmin = 0;
			}
			xmax = xmin + (int) (roiWidth * xScale) - 1;
			if (xmax >= width) {
				xmax = width - 1;
			}
			ymin = (int) (yCenter - (yCenter - roiY) * yScale);
			if (ymin < 0) {
				ymin = 0;
			}
			ymax = ymin + (int) (roiHeight * yScale) - 1;
			if (ymax >= height) {
				ymax = height - 1;
			}
		} else {
			xmin = roiX;
			xmax = roiX + roiWidth - 1;
			ymin = roiY;
			ymax = roiY + roiHeight - 1;
		}

	short[] pixels2 = (short[]) getPixelsCopy();
	boolean checkCoordinates = (xScale < 1.0) || (yScale < 1.0);
	int index1;
	int index2;
	int xsi;
	int ysi;
	double ys;
	double xs;
	double xlimit = width - 1.0;
	double xlimit2 = width - 1.001;
	double ylimit = height - 1.0;
	double ylimit2 = height - 1.001;

		for (int y = ymin; y <= ymax; y++) {
			ys = (y - yCenter) / yScale + yCenter;
			ysi = (int) ys;
			if (ys < 0.0) {
				ys = 0.0;
			}
			if (ys >= ylimit) {
				ys = ylimit2;
			}
			index1 = y * width + xmin;
			index2 = width * (int) ys;
			for (int x = xmin; x <= xmax; x++) {
				xs = (x - xCenter) / xScale + xCenter;
				xsi = (int) xs;
				if (checkCoordinates && ((xsi < xmin) || (xsi > xmax) || (ysi < ymin) || (ysi > ymax))) {
					pixels[index1++] = (short) min;
				} else {
					if (interpolate) {
						if (xs < 0.0) {
							xs = 0.0;
						}
						if (xs >= xlimit) {
							xs = xlimit2;
						}
						pixels[index1++] = (short) (getInterpolatedPixel(xs, ys, pixels2) + 0.5);
					} else {
						pixels[index1++] = pixels2[index2 + xsi];
					}
				}
			}
			if (y % 20 == 0) {
				showProgress((double) (y - ymin) / height);
			}
		}
		showProgress(1.0);
	}


	/**
	 *  Uses bilinear interpolation to find the pixel value at real coordinates
	 *  (x,y).
	 *
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  pixels  Description of the Parameter
	 *@return         The interpolatedPixel value
	 */
	private final double getInterpolatedPixel(double x, double y, short[] pixels) {
	int xbase = (int) x;
	int ybase = (int) y;
	double xFraction = x - xbase;
	double yFraction = y - ybase;
	int offset = ybase * width + xbase;
	int lowerLeft = pixels[offset] & 0xffff;
	int lowerRight = pixels[offset + 1] & 0xffff;
	int upperRight = pixels[offset + width + 1] & 0xffff;
	int upperLeft = pixels[offset + width] & 0xffff;
	double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
	double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		return lowerAverage + yFraction * (upperAverage - lowerAverage);
	}


	/**
	 *  Creates a new ShortProcessor containing a scaled copy of this image or
	 *  selection.
	 *
	 *@param  dstWidth   Description of the Parameter
	 *@param  dstHeight  Description of the Parameter
	 *@return            Description of the Return Value
	 */
	public ImageProcessor resize(int dstWidth, int dstHeight) {
	double srcCenterX = roiX + roiWidth / 2.0;
	double srcCenterY = roiY + roiHeight / 2.0;
	double dstCenterX = dstWidth / 2.0;
	double dstCenterY = dstHeight / 2.0;
	double xScale = (double) dstWidth / roiWidth;
	double yScale = (double) dstHeight / roiHeight;

		if (interpolate) {
			dstCenterX += xScale / 2.0;
			dstCenterY += yScale / 2.0;
		}
	ImageProcessor ip2 = createProcessor(dstWidth, dstHeight);
	short[] pixels2 = (short[]) ip2.getPixels();
	double xs;
	double ys;
	double xlimit = width - 1.0;
	double xlimit2 = width - 1.001;
	double ylimit = height - 1.0;
	double ylimit2 = height - 1.001;
	int index1;
	int index2;

		for (int y = 0; y <= dstHeight - 1; y++) {
			ys = (y - dstCenterY) / yScale + srcCenterY;
			if (interpolate) {
				if (ys < 0.0) {
					ys = 0.0;
				}
				if (ys >= ylimit) {
					ys = ylimit2;
				}
			}
			index1 = width * (int) ys;
			index2 = y * dstWidth;
			for (int x = 0; x <= dstWidth - 1; x++) {
				xs = (x - dstCenterX) / xScale + srcCenterX;
				if (interpolate) {
					if (xs < 0.0) {
						xs = 0.0;
					}
					if (xs >= xlimit) {
						xs = xlimit2;
					}
					pixels2[index2++] = (short) (getInterpolatedPixel(xs, ys, pixels) + 0.5);
				} else {
					pixels2[index2++] = pixels[index1 + (int) xs];
				}
			}
			if (y % 20 == 0) {
				showProgress((double) y / dstHeight);
			}
		}
		showProgress(1.0);
		return ip2;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	public ImageProcessor crop() {
	ImageProcessor ip2 = createProcessor(roiWidth, roiHeight);
	short[] pixels2 = (short[]) ip2.getPixels();

		for (int ys = roiY; ys < roiY + roiHeight; ys++) {
		int offset1 = (ys - roiY) * roiWidth;
		int offset2 = ys * width + roiX;

			for (int xs = 0; xs < roiWidth; xs++) {
				pixels2[offset1++] = pixels[offset2++];
			}
		}
		return ip2;
	}


	/**
	 *  Returns a duplicate of this image.
	 *
	 *@return    Description of the Return Value
	 */
	public synchronized ImageProcessor duplicate() {
	ImageProcessor ip2 = createProcessor(width, height);
	short[] pixels2 = (short[]) ip2.getPixels();

		System.arraycopy(pixels, 0, pixels2, 0, width * height);
		return ip2;
	}


	/**
	 *  Sets the foreground fill/draw color.
	 *
	 *@param  color  The new color value
	 */
	public void setColor(Color color) {
	int bestIndex = getBestIndex(color);

		if (bestIndex > 0 && getMin() == 0.0 && getMax() == 0.0) {
			setValue(bestIndex);
			setMinAndMax(0.0, 255.0);
		} else if (bestIndex == 0 && getMin() > 0.0 && (color.getRGB() & 0xffffff) == 0) {
			if (cTable != null && cTable[0] == -32768f) {// signed image
				setValue(32768.0);
			} else {
				setValue(0.0);
			}
		} else {
			fgColor = (int) (getMin() + (getMax() - getMin()) * (bestIndex / 255.0));
		}

	}


	/**
	 *  Sets the default fill/draw value, where 0<=value<=65535).
	 *
	 *@param  value  The new value value
	 */
	public void setValue(double value) {
		fgColor = (int) value;
		if (fgColor < 0) {
			fgColor = 0;
		}
		if (fgColor > 65535) {
			fgColor = 65535;
		}
	}


	/**
	 *  Does nothing. The rotate() and scale() methods always zero fill.
	 *
	 *@param  value  The new backgroundValue value
	 */
	public void setBackgroundValue(double value) {
	}


	/**
	 *  Returns 65536 bin histogram of the current ROI, which can be
	 *  non-rectangular.
	 *
	 *@return    The histogram value
	 */
	public int[] getHistogram() {
		if (mask != null) {
			return getHistogram(mask);
		}
	int[] histogram = new int[65536];

		for (int y = roiY; y < (roiY + roiHeight); y++) {
		int i = y * width + roiX;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				histogram[pixels[i++] & 0xffff]++;
			}
		}
		return histogram;
	}


	/**
	 *  Gets the histogram attribute of the ShortProcessor object
	 *
	 *@param  mask  Description of the Parameter
	 *@return       The histogram value
	 */
	int[] getHistogram(ImageProcessor mask) {
		if (mask.getWidth() != roiWidth || mask.getHeight() != roiHeight) {
			throw new IllegalArgumentException(maskSizeError(mask));
		}
	byte[] mpixels = (byte[]) mask.getPixels();
	int[] histogram = new int[65536];

		for (int y = roiY, my = 0; y < (roiY + roiHeight); y++, my++) {
		int i = y * width + roiX;
		int mi = my * roiWidth;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				if (mpixels[mi++] != 0) {
					histogram[pixels[i] & 0xffff]++;
				}
				i++;
			}
		}
		return histogram;
	}


	/**
	 *  Sets the threshold attribute of the ShortProcessor object
	 *
	 *@param  minThreshold  The new threshold value
	 *@param  maxThreshold  The new threshold value
	 *@param  lutUpdate     The new threshold value
	 */
	public void setThreshold(double minThreshold, double maxThreshold, int lutUpdate) {
		if (minThreshold == NO_THRESHOLD) {
			resetThreshold();
			return;
		}
		if (minThreshold < 0.0) {
			minThreshold = 0.0;
		}
		if (maxThreshold > 65535.0) {
			maxThreshold = 65535.0;
		}
		if (max > min) {
		double minT = Math.round(((minThreshold - min) / (max - min)) * 255.0);
		double maxT = Math.round(((maxThreshold - min) / (max - min)) * 255.0);

			super.setThreshold(minT, maxT, lutUpdate);// update LUT
		} else {
			super.resetThreshold();
		}
		this.minThreshold = Math.round(minThreshold);
		this.maxThreshold = Math.round(maxThreshold);
	}


	/**
	 *  Performs a convolution operation using the specified kernel.
	 *
	 *@param  kernel        Description of the Parameter
	 *@param  kernelWidth   Description of the Parameter
	 *@param  kernelHeight  Description of the Parameter
	 */
	public void convolve(float[] kernel, int kernelWidth, int kernelHeight) {
	ImageProcessor ip2 = convertToFloat();

		ip2.setRoi(getRoi());
		new ij.plugin.filter.Convolver().convolve(ip2, kernel, kernelWidth, kernelHeight);
		ip2 = ip2.convertToShort(false);

	short[] pixels2 = (short[]) ip2.getPixels();

		System.arraycopy(pixels2, 0, pixels, 0, pixels.length);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  range  Description of the Parameter
	 */
	public void noise(double range) {
	Random rnd = new Random();
	int v;
	int ran;
	boolean inRange;

		for (int y = roiY; y < (roiY + roiHeight); y++) {
		int i = y * width + roiX;

			for (int x = roiX; x < (roiX + roiWidth); x++) {
				inRange = false;
				do {
					ran = (int) Math.round(rnd.nextGaussian() * range);
					v = (pixels[i] & 0xffff) + ran;
					inRange = v >= 0 && v <= 65535;
					if (inRange) {
						pixels[i] = (short) v;
					}
				} while (!inRange);
				i++;
			}
		}
		resetMinAndMax();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  level  Description of the Parameter
	 */
	public void threshold(int level) {
		for (int i = 0; i < width * height; i++) {
			if ((pixels[i] & 0xffff) <= level) {
				pixels[i] = 0;
			} else {
				pixels[i] = (short) 255;
			}
		}
		findMinAndMax();
	}


	/**
	 *  Returns a FloatProcessor with the same image, no scaling or calibration
	 *  (pixel values 0 to 65535). The roi, mask, lut (ColorModel), threshold,
	 *  min&max are also set for the FloatProcessor
	 *
	 *@param  channelNumber  Ignored (needed for compatibility with
	 *      ColorProcessor.toFloat)
	 *@param  fp             Here a FloatProcessor can be supplied, or null. The
	 *      FloatProcessor is overwritten by this method (re-using its pixels array
	 *      improves performance).
	 *@return                A FloatProcessor with the converted image data
	 */
	public FloatProcessor toFloat(int channelNumber, FloatProcessor fp) {
	int size = width * height;

		if (fp == null || fp.getWidth() != width || fp.getHeight() != height) {
			fp = new FloatProcessor(width, height, new float[size], cm);
		}
	float[] fPixels = (float[]) fp.getPixels();

		for (int i = 0; i < size; i++) {
			fPixels[i] = pixels[i] & 0xffff;
		}
		fp.setRoi(getRoi());
		fp.setMask(mask);
		fp.setThreshold(minThreshold, maxThreshold, ImageProcessor.NO_LUT_UPDATE);
		//##can be NO_LUT_UPDATE
		fp.setMinAndMax(min, max);
		return fp;
	}


	/**
	 *  Sets the pixels from a FloatProcessor, no scaling. Also the min&max values
	 *  are taken from the FloatProcessor.
	 *
	 *@param  channelNumber  Ignored (needed for compatibility with
	 *      ColorProcessor.toFloat)
	 *@param  fp             The FloatProcessor where the image data are read from.
	 */
	public void setPixels(int channelNumber, FloatProcessor fp) {
	float[] fPixels = (float[]) fp.getPixels();
	float value;
	int size = width * height;

		for (int i = 0; i < size; i++) {
			value = fPixels[i] + 0.49999995f;
			if (value < 0f) {
				value = 0f;
			}
			if (value > 65535f) {
				value = 65535f;
			}
			pixels[i] = (short) value;
		}
		setMinAndMax(fp.getMin(), fp.getMax());
	}


	/**
	 *  Returns the maximum possible pixel value.
	 *
	 *@return    Description of the Return Value
	 */
	public double maxValue() {
		return 65535.0;
	}


	/**
	 *  Not implemented.
	 */
	public void medianFilter() { }


	/**
	 *  Not implemented.
	 */
	public void erode() { }


	/**
	 *  Not implemented.
	 */
	public void dilate() { }

}

