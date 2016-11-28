package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;
import java.awt.geom.*;

/**
 *  This plugin implements the Image/Rotate/Arbitrarily command.
 *
 *@author     Thomas
 *@created    3 decembre 2007
 */
public class Rotator implements ExtendedPlugInFilter, DialogListener {
	private int flags = DOES_ALL | SUPPORTS_MASKING | PARALLELIZE_STACKS;
	private static double angle = 15.0;
	private static boolean interpolate = true;
	private static boolean fillWithBackground;
	private static boolean enlarge;
	private static int gridLines = 1;
	private ImagePlus imp;
	private int bitDepth;
	boolean canEnlarge;
	boolean isEnlarged;
	GenericDialog gd;
	PlugInFilterRunner pfr;


	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		if (imp != null) {
			bitDepth = imp.getBitDepth();
		Roi roi = imp.getRoi();
		Rectangle r = roi != null ? roi.getBounds() : null;
			canEnlarge = r == null || (r.x == 0 && r.y == 0 && r.width == imp.getWidth() && r.height == imp.getHeight());
		}
		return flags;
	}


	/**
	 *  Main processing method for the Rotator object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
		if (enlarge && gd.wasOKed()) {
			synchronized (this) {
				if (!isEnlarged) {
					enlargeCanvas();
					isEnlarged = true;
				}
			}
		}
		if (isEnlarged) {//enlarging may have made the ImageProcessor invalid, also for the parallel threads
		int slice = pfr.getSliceNumber();
			if (imp.getStackSize() == 1) {
				ip = imp.getProcessor();
			} else {
				ip = imp.getStack().getProcessor(slice);
			}
		}
		ip.setInterpolate(interpolate);
		if (fillWithBackground) {
		Color bgc = Toolbar.getBackgroundColor();
			if (bitDepth == 8) {
				ip.setBackgroundValue(ip.getBestIndex(bgc));
			} else if (bitDepth == 24) {
				ip.setBackgroundValue(bgc.getRGB());
			}
		} else {
			ip.setBackgroundValue(0);
		}
		ip.rotate(angle);
		if (!gd.wasOKed()) {
			drawGridLines(gridLines);
		}
		if (isEnlarged && imp.getStackSize() == 1) {
			imp.changes = true;
			imp.updateAndDraw();
			Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
		}
	}


	/**
	 *  Description of the Method
	 */
	void enlargeCanvas() {
		imp.unlock();
		if (imp.getStackSize() == 1) {
			Undo.setup(Undo.COMPOUND_FILTER, imp);
		}
		IJ.run("Select All");
		IJ.run("Rotate...", "angle=" + angle);
	Roi roi = imp.getRoi();
	Rectangle r = roi.getBounds();
		//EU_HOU Bundle
		IJ.showStatus("Rotate: Enlarging...");
		IJ.run("Canvas Size...", "width=" + r.width + " height=" + r.height + " position=Center " + (fillWithBackground ? "" : "zero"));
		//EU_HOU Bundle
		IJ.showStatus("Rotating...");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  lines  Description of the Parameter
	 */
	void drawGridLines(int lines) {
	ImageCanvas ic = imp.getCanvas();
		if (ic == null) {
			return;
		}
		if (lines == 0) {
			ic.setDisplayList(null);
			return;
		}
	GeneralPath path = new GeneralPath();
	float width = imp.getWidth();
	float height = imp.getHeight();
	float xinc = width / lines;
	float yinc = height / lines;
	float xstart = xinc / 2f;
	float ystart = yinc / 2f;
		for (int i = 0; i < lines; i++) {
			path.moveTo(xstart + xinc * i, 0f);
			path.lineTo(xstart + xinc * i, height);
			path.moveTo(0f, ystart + yinc * i);
			path.lineTo(width, ystart + yinc * i);
		}
		ic.setDisplayList(path, null, null);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp      Description of the Parameter
	 *@param  command  Description of the Parameter
	 *@param  pfr      Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
		this.pfr = pfr;
		//EU_HOU Bundle
		gd = new GenericDialog(IJ.getPluginBundle().getString("Rotate"), IJ.getInstance());
		gd.addNumericField(IJ.getPluginBundle().getString("AngleUnity"), angle, (int) angle == angle ? 1 : 2);
		gd.addNumericField(IJ.getPluginBundle().getString("GridLines"), gridLines, 0);
		gd.addCheckbox(IJ.getPluginBundle().getString("Interpolate"), interpolate);
		if (bitDepth == 8 || bitDepth == 24) {
			gd.addCheckbox(IJ.getPluginBundle().getString("FillwithBg"), fillWithBackground);
		}
		if (canEnlarge) {
			gd.addCheckbox(IJ.getPluginBundle().getString("EnlargetoFit"), enlarge);
		} else {
			enlarge = false;
		}
		gd.addPreviewCheckbox(pfr);
		gd.addDialogListener(this);
		gd.showDialog();
		drawGridLines(0);
		if (gd.wasCanceled()) {
			return DONE;
		}
		if (!enlarge) {
			flags |= KEEP_PREVIEW;
		} // standard filter without enlarge
		else if (imp.getStackSize() == 1) {
			flags |= NO_CHANGES;
		}// undoable as a "compound filter"
		return IJ.setupDialog(imp, flags);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  gd  Description of the Parameter
	 *@param  e   Description of the Parameter
	 *@return     Description of the Return Value
	 */
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		angle = gd.getNextNumber();
		//only check for invalid input to "angle", don't care about gridLines
		if (gd.invalidNumber()) {
			if (gd.wasOKed()) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("AngleErr"));
			}
			return false;
		}
		gridLines = (int) gd.getNextNumber();
		interpolate = gd.getNextBoolean();
		if (bitDepth == 8 || bitDepth == 24) {
			fillWithBackground = gd.getNextBoolean();
		}
		if (canEnlarge) {
			enlarge = gd.getNextBoolean();
		}
		return true;
	}


	/**
	 *  Sets the nPasses attribute of the Rotator object
	 *
	 *@param  nPasses  The new nPasses value
	 */
	public void setNPasses(int nPasses) {
	}

}

