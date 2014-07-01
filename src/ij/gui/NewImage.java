//EU_HOU
package ij.gui;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import ij.*;
import ij.process.*;

/**
 *  New image dialog box plus several static utility methods for creating
 *  images.
 *
 *@author     Thomas
 *@created    20 novembre 2007
 */
public class NewImage {

	/**
	 *  Description of the Field
	 */
	public final static int GRAY8 = 0, GRAY16 = 1, GRAY32 = 2, RGB = 3;
	/**
	 *  Description of the Field
	 */
	public final static int FILL_BLACK = 1, FILL_RAMP = 2, FILL_WHITE = 4, CHECK_AVAILABLE_MEMORY = 8;
	private final static int OLD_FILL_WHITE = 0;

	final static String NAME = "new.name";
	final static String TYPE = "new.type";
	final static String FILL = "new.fill";
	final static String WIDTH = "new.width";
	final static String HEIGHT = "new.height";
	final static String SLICES = "new.slices";
//EU_HOU changes
	private static String name = "";
	private static int width = Prefs.getInt(WIDTH, 400);
	private static int height = Prefs.getInt(HEIGHT, 400);
	private static int slices = Prefs.getInt(SLICES, 1);
	private static int type = Prefs.getInt(TYPE, GRAY8);
	private static int fillWith = Prefs.getInt(FILL, OLD_FILL_WHITE);
	/*
	 *  EU_HOU CHANGES
	 */
	private static String[] typekeys = {"8-bit", "16-bit", "32-bit", "RGBColor"};
	private static String[] fillkeys = {"WhiteColor", "BlackColor", "Ramp"};

	private static String types[], fill[];


	/*
	 *  EU_HOU END
	 */
	/**
	 *  Constructor for the NewImage object
	 */
	public NewImage() {
		openImage();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp      Description of the Parameter
	 *@param  ip       Description of the Parameter
	 *@param  nSlices  Description of the Parameter
	 *@param  type     Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	static boolean createStack(ImagePlus imp, ImageProcessor ip, int nSlices, int type, int options) {
	int fill = getFill(options);
	int width = imp.getWidth();
	int height = imp.getHeight();
	long bytesPerPixel = 1;

		if (type == GRAY16) {
			bytesPerPixel = 2;
		} else if (type == GRAY32 || type == RGB) {
			bytesPerPixel = 4;
		}
	long size = (width * height * nSlices * bytesPerPixel);
	String size2 = size / (1024 * 1024) + "MB (" + width + "x" + height + "x" + nSlices + ")";

		if ((options & CHECK_AVAILABLE_MEMORY) != 0) {
		long max = IJ.maxMemory();// - 100*1024*1024;

			if (max > 0) {
			long inUse = IJ.currentMemory();
			long available = max - inUse;
				//IJ.log(size/(1024*1024)+"  "+available/(1024*1024));
				if (size > available) {
					//EU_HOU Bundle
					IJ.error("Out of Memory", "There is not enough free memory to allocate a \n"
							 + size2 + " stack.\n \n"
							 + "Memory available: " + available / (1024 * 1024) + "MB\n"
							 + "Memory in use: " + IJ.freeMemory() + "\n \n"
							 + "More information can be found in the \"Memory\"\n"
							 + "sections of the ImageJ installation notes at\n"
							 + "\"http://rsb.info.nih.gov/ij/docs/install/\".");
					return false;
				}
			}
		}
	ImageStack stack = imp.createEmptyStack();
	int inc = nSlices / 40;

		if (inc < 1) {
			inc = 1;
		}
		//EU_HOU Bundle
		IJ.showStatus("Allocating " + size2 + ". Press 'Esc' to abort.");
		IJ.resetEscape();
		try {
			stack.addSlice(null, ip);
			for (int i = 2; i <= nSlices; i++) {
				if ((i % inc) == 0) {
					IJ.showProgress(i, nSlices);
				}
			Object pixels2 = null;

				switch (type) {
								case GRAY8:
									pixels2 = new byte[width * height];
									break;
								case GRAY16:
									pixels2 = new short[width * height];
									break;
								case GRAY32:
									pixels2 = new float[width * height];
									break;
								case RGB:
									pixels2 = new int[width * height];
									break;
				}
				if (fill != FILL_BLACK || type == RGB) {
					System.arraycopy(ip.getPixels(), 0, pixels2, 0, width * height);
				}
				stack.addSlice(null, pixels2);
				if (IJ.escapePressed()) {
					IJ.beep();
					break;
				}
				;
			}
		} catch (OutOfMemoryError e) {
			IJ.outOfMemory(imp.getTitle());
			stack.trim();
		}
		IJ.showProgress(nSlices, nSlices);
		if (stack.getSize() > 1) {
			imp.setStack(null, stack);
		}
		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	static ImagePlus createImagePlus() {
		//ImagePlus imp = WindowManager.getCurrentImage();
		//if (imp!=null)
		//	return imp.createImagePlus();
		//else
		return new ImagePlus();
	}


	/**
	 *  Gets the fill attribute of the NewImage class
	 *
	 *@param  options  Description of the Parameter
	 *@return          The fill value
	 */
	static int getFill(int options) {
	int fill = options & 7;

		if (fill == OLD_FILL_WHITE) {
			fill = FILL_WHITE;
		}
		if (fill == 7 || fill == 6 || fill == 3 || fill == 5) {
			fill = FILL_BLACK;
		}
		return fill;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  slices   Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static ImagePlus createByteImage(String title, int width, int height, int slices, int options) {
	int fill = getFill(options);
	byte[] pixels = new byte[width * height];

		switch (fill) {
						case FILL_WHITE:
							for (int i = 0; i < width * height; i++) {
								pixels[i] = (byte) 255;
							}
							break;
						case FILL_BLACK:
							break;
						case FILL_RAMP:
						byte[] ramp = new byte[width];

							for (int i = 0; i < width; i++) {
								ramp[i] = (byte) ((i * 256.0) / width);
							}

						int offset;

							for (int y = 0; y < height; y++) {
								offset = y * width;
								for (int x = 0; x < width; x++) {
									pixels[offset++] = ramp[x];
								}
							}
							break;
		}

	ImageProcessor ip = new ByteProcessor(width, height, pixels, null);
	ImagePlus imp = createImagePlus();

		imp.setProcessor(title, ip);
		if (slices > 1) {
		boolean ok = createStack(imp, ip, slices, GRAY8, options);

			if (!ok) {
				imp = null;
			}
		}
		return imp;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  slices   Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static ImagePlus createRGBImage(String title, int width, int height, int slices, int options) {
	int fill = getFill(options);
	int[] pixels = new int[width * height];

		switch (fill) {
						case FILL_WHITE:
							for (int i = 0; i < width * height; i++) {
								pixels[i] = -1;
							}
							break;
						case FILL_BLACK:
							for (int i = 0; i < width * height; i++) {
								pixels[i] = 0xff000000;
							}
							break;
						case FILL_RAMP:
						int r;
						int g;
						int b;
						int offset;
						int[] ramp = new int[width];

							for (int i = 0; i < width; i++) {
								r = g = b = (byte) ((i * 256.0) / width);
								ramp[i] = 0xff000000 | ((r << 16) & 0xff0000) | ((g << 8) & 0xff00) | (b & 0xff);
							}
							for (int y = 0; y < height; y++) {
								offset = y * width;
								for (int x = 0; x < width; x++) {
									pixels[offset++] = ramp[x];
								}
							}
							break;
		}

	ImageProcessor ip = new ColorProcessor(width, height, pixels);
	ImagePlus imp = createImagePlus();

		imp.setProcessor(title, ip);
		if (slices > 1) {
		boolean ok = createStack(imp, ip, slices, RGB, options);

			if (!ok) {
				imp = null;
			}
		}
		return imp;
	}


	/**
	 *  Creates an unsigned short image.
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  slices   Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static ImagePlus createShortImage(String title, int width, int height, int slices, int options) {
	int fill = getFill(options);
	short[] pixels = new short[width * height];

		switch (fill) {
						case FILL_WHITE:
						case FILL_BLACK:
							break;
						case FILL_RAMP:
						short[] ramp = new short[width];

							for (int i = 0; i < width; i++) {
								ramp[i] = (short) (((i * 65536.0) / width) + 0.5);
							}

						int offset;

							for (int y = 0; y < height; y++) {
								offset = y * width;
								for (int x = 0; x < width; x++) {
									pixels[offset++] = ramp[x];
								}
							}
							break;
		}

	ImageProcessor ip = new ShortProcessor(width, height, pixels, null);

		if (fill == FILL_WHITE) {
			ip.invertLut();
		}
	ImagePlus imp = createImagePlus();

		imp.setProcessor(title, ip);
		if (slices > 1) {
		boolean ok = createStack(imp, ip, slices, GRAY16, options);

			if (!ok) {
				imp = null;
			}
		}
		imp.getProcessor().setMinAndMax(0, 65535);// default display range
		return imp;
	}


	/**
	 *  Obsolete. Short images are always unsigned.
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  slices   Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static ImagePlus createUnsignedShortImage(String title, int width, int height, int slices, int options) {
		return createShortImage(title, width, height, slices, options);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  slices   Description of the Parameter
	 *@param  options  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public static ImagePlus createFloatImage(String title, int width, int height, int slices, int options) {
	int fill = getFill(options);
	float[] pixels = new float[width * height];

		switch (fill) {
						case FILL_WHITE:
						case FILL_BLACK:
							break;
						case FILL_RAMP:
						float[] ramp = new float[width];

							for (int i = 0; i < width; i++) {
								ramp[i] = (float) ((i * 1.0) / width);
							}

						int offset;

							for (int y = 0; y < height; y++) {
								offset = y * width;
								for (int x = 0; x < width; x++) {
									pixels[offset++] = ramp[x];
								}
							}
							break;
		}

	ImageProcessor ip = new FloatProcessor(width, height, pixels, null);

		if (fill == FILL_WHITE) {
			ip.invertLut();
		}
	ImagePlus imp = createImagePlus();

		imp.setProcessor(title, ip);
		if (slices > 1) {
		boolean ok = createStack(imp, ip, slices, GRAY32, options);

			if (!ok) {
				imp = null;
			}
		}
		imp.getProcessor().setMinAndMax(0.0, 1.0);// default display range
		return imp;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  title    Description of the Parameter
	 *@param  width    Description of the Parameter
	 *@param  height   Description of the Parameter
	 *@param  nSlices  Description of the Parameter
	 *@param  type     Description of the Parameter
	 *@param  options  Description of the Parameter
	 */
	public static void open(String title, int width, int height, int nSlices, int type, int options) {
	int bitDepth = 8;
		System.out.println("open type=" + type);
		if (type == GRAY16) {
			bitDepth = 16;
		} else if (type == GRAY32) {
			bitDepth = 32;
		} else if (type == RGB) {
			bitDepth = 24;
		}
	long startTime = System.currentTimeMillis();
	ImagePlus imp = createImage(title, width, height, nSlices, bitDepth, options);

		if (imp != null) {
			WindowManager.checkForDuplicateName = true;
			imp.show();
			IJ.showStatus(IJ.d2s(((System.currentTimeMillis() - startTime) / 1000.0), 2) + " seconds");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  title     Description of the Parameter
	 *@param  width     Description of the Parameter
	 *@param  height    Description of the Parameter
	 *@param  nSlices   Description of the Parameter
	 *@param  bitDepth  Description of the Parameter
	 *@param  options   Description of the Parameter
	 *@return           Description of the Return Value
	 */
	public static ImagePlus createImage(String title, int width, int height, int nSlices, int bitDepth, int options) {
	ImagePlus imp = null;

		switch (bitDepth) {
						case 8:
							System.out.println("createImage 8");
							imp = createByteImage(title, width, height, nSlices, options);
							break;
						case 16:
							System.out.println("createImage 16");
							imp = createShortImage(title, width, height, nSlices, options);
							break;
						case 32:
							System.out.println("createImage 32");
							imp = createFloatImage(title, width, height, nSlices, options);
							break;
						case 24:
							System.out.println("createImage 24");
							imp = createRGBImage(title, width, height, nSlices, options);
							break;
						default:
							System.out.println("createImage default");
							//EU_HOU Bundle
							throw new IllegalArgumentException("Invalid bitDepth: " + bitDepth);
		}
		return imp;
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	boolean showDialog() {
		if (type < GRAY8 || type > RGB) {
			type = GRAY8;
		}
		if (fillWith < OLD_FILL_WHITE || fillWith > FILL_RAMP) {
			fillWith = OLD_FILL_WHITE;
		}
	//EU_HOU Bundle
	GenericDialog gd = new GenericDialog(IJ.getBundle().getString("New"), IJ.getInstance());
		//EU_HOU Bundle
		name = IJ.getBundle().getString("Untitled");
		//name = Prefs.getString(NAME, IJ.getBundle().getString("Untitled"));
		types = new String[typekeys.length];
		for (int i = 0; i < types.length; ++i) {
			types[i] = IJ.getBundle().getString(typekeys[i]);
		}
		fill = new String[fillkeys.length];
		for (int i = 0; i < fill.length; ++i) {
			fill[i] = IJ.getColorBundle().getString(fillkeys[i]);
		}
		//EU_HOU Bundle
		gd.addStringField(IJ.getBundle().getString("Name") + ":", name, 12);
		gd.addChoice(IJ.getBundle().getString("Type") + ":", types, types[type]);
		gd.addChoice(IJ.getBundle().getString("NewFill") + ":", fill, fill[fillWith]);
		gd.addNumericField(IJ.getBundle().getString("Width") + ":", width, 0, 5, IJ.getBundle().getString("Pixels"));
		gd.addNumericField(IJ.getBundle().getString("Height") + ":", height, 0, 5, IJ.getBundle().getString("Pixels"));
		gd.addNumericField(IJ.getBundle().getString("Slices") + ":", slices, 0, 5, "");
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		name = gd.getNextString();

	String s = gd.getNextChoice();
		//EU_HOU Bundle
		if (s.startsWith("8") || s.equals(IJ.getBundle().getString("8-bit"))) {
			type = GRAY8;
		} else if (s.startsWith("16") || s.equals(IJ.getBundle().getString("16-bit"))) {
			type = GRAY16;
		} else if (s.endsWith("RGB") || s.endsWith("rgb") || s.equals(IJ.getBundle().getString("RGBColor"))) {
			type = RGB;
		} else {
			type = GRAY32;
		}
		fillWith = gd.getNextChoiceIndex();
		width = (int) gd.getNextNumber();
		height = (int) gd.getNextNumber();
		slices = (int) gd.getNextNumber();
		return true;
	}


	/**
	 *  Description of the Method
	 */
	void openImage() {
		if (!showDialog()) {
			return;
		}
		try {
			open(name, width, height, slices, type, fillWith);
		} catch (OutOfMemoryError e) {
			//EU_HOU Bundle
			IJ.outOfMemory("New Image...");
		}
	}


	/**
	 *  Called when ImageJ quits.
	 *
	 *@param  prefs  Description of the Parameter
	 */
	public static void savePreferences(Properties prefs) {
		System.out.println(name);
		prefs.put(NAME, name);
		prefs.put(TYPE, Integer.toString(type));
		prefs.put(FILL, Integer.toString(fillWith));
		prefs.put(WIDTH, Integer.toString(width));
		prefs.put(HEIGHT, Integer.toString(height));
		prefs.put(SLICES, Integer.toString(slices));
	}

}

