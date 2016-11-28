package ij.plugin;
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.util.ResourceBundle.*;

/**
 *  This plugin implements the Image/Adjust/Canvas Size command. It changes the
 *  canvas size of an image or stack without resizing the image. The border is
 *  filled with the current background color.
 *
 *@author     Jeffrey Kuhn (jkuhn at ccwf.cc.utexas.edu)
 *@created    30 novembre 2007
 */
public class CanvasResizer implements PlugIn {
	boolean zeroFill = Prefs.get("resizer.zero", false);


	/**
	 *  Main processing method for the CanvasResizer object
	 *
	 *@param  arg  Description of the Parameter
	 */
	public void run(String arg) {
	int wOld;
	int hOld;
	int wNew;
	int hNew;
	boolean fIsStack = false;

	ImagePlus imp = IJ.getImage();
		wOld = imp.getWidth();
		hOld = imp.getHeight();

	ImageStack stackOld = imp.getStack();
		if ((stackOld != null) && (stackOld.getSize() > 1)) {
			fIsStack = true;
		}

	String[] sPositions = {
				"Top-Left", "Top-Center", "Top-Right",
				"Center-Left", "Center", "Center-Right",
				"Bottom-Left", "Bottom-Center", "Bottom-Right"
				};
	int i = 0;
		for (i = 0; i < 9; i++) {
			sPositions[i] = IJ.getPluginBundle().getString(sPositions[i]);
		}
	//EU_HOU Bundle
	String strTitle = fIsStack ? IJ.getPluginBundle().getString("CanvasResizerTitle2") : IJ.getPluginBundle().getString("CanvasResizerTitle1");
	GenericDialog gd = new GenericDialog(strTitle);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getBundle().getString("Width") + ":", wOld, 0, 5, IJ.getBundle().getString("Pixels"));
		gd.addNumericField(IJ.getBundle().getString("Height") + ":", hOld, 0, 5, IJ.getBundle().getString("Pixels"));
		gd.addChoice(IJ.getPluginBundle().getString("Position") + ":", sPositions, sPositions[4]);
		gd.addCheckbox(IJ.getPluginBundle().getString("ZeroFile"), zeroFill);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}

		wNew = (int) gd.getNextNumber();
		hNew = (int) gd.getNextNumber();
	int iPos = gd.getNextChoiceIndex();
		zeroFill = gd.getNextBoolean();
		Prefs.set("resizer.zero", zeroFill);

	int xOff;

	int yOff;
	int xC = (wNew - wOld) / 2;// offset for centered
	int xR = (wNew - wOld);// offset for right
	int yC = (hNew - hOld) / 2;// offset for centered
	int yB = (hNew - hOld);// offset for bottom

		switch (iPos) {
						case 0:// TL
							xOff = 0;
							yOff = 0;
							break;
						case 1:// TC
							xOff = xC;
							yOff = 0;
							break;
						case 2:// TR
							xOff = xR;
							yOff = 0;
							break;
						case 3:// CL
							xOff = 0;
							yOff = yC;
							break;
						case 4:// C
							xOff = xC;
							yOff = yC;
							break;
						case 5:// CR
							xOff = xR;
							yOff = yC;
							break;
						case 6:// BL
							xOff = 0;
							yOff = yB;
							break;
						case 7:// BC
							xOff = xC;
							yOff = yB;
							break;
						case 8:// BR
							xOff = xR;
							yOff = yB;
							break;
						default:// center
							xOff = xC;
							yOff = yC;
							break;
		}

		if (fIsStack) {
		ImageStack stackNew = expandStack(stackOld, wNew, hNew, xOff, yOff);
			imp.setStack(null, stackNew);
		} else {
			if (!IJ.macroRunning()) {
				Undo.setup(Undo.COMPOUND_FILTER, imp);
			}
		ImageProcessor newIP = expandImage(imp.getProcessor(), wNew, hNew, xOff, yOff);
			imp.setProcessor(null, newIP);
			if (!IJ.macroRunning()) {
				Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  stackOld  Description of the Parameter
	 *@param  wNew      Description of the Parameter
	 *@param  hNew      Description of the Parameter
	 *@param  xOff      Description of the Parameter
	 *@param  yOff      Description of the Parameter
	 *@return           Description of the Return Value
	 */
	public ImageStack expandStack(ImageStack stackOld, int wNew, int hNew, int xOff, int yOff) {
	int nFrames = stackOld.getSize();
	ImageProcessor ipOld = stackOld.getProcessor(1);
	java.awt.Color colorBack = Toolbar.getBackgroundColor();

	ImageStack stackNew = new ImageStack(wNew, hNew, stackOld.getColorModel());
	ImageProcessor ipNew;

		for (int i = 1; i <= nFrames; i++) {
			IJ.showProgress((double) i / nFrames);
			ipNew = ipOld.createProcessor(wNew, hNew);
			if (zeroFill) {
				ipNew.setValue(0.0);
			} else {
				ipNew.setColor(colorBack);
			}
			ipNew.fill();
			ipNew.insert(stackOld.getProcessor(i), xOff, yOff);
			stackNew.addSlice(stackOld.getSliceLabel(i), ipNew);
		}
		return stackNew;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  ipOld  Description of the Parameter
	 *@param  wNew   Description of the Parameter
	 *@param  hNew   Description of the Parameter
	 *@param  xOff   Description of the Parameter
	 *@param  yOff   Description of the Parameter
	 *@return        Description of the Return Value
	 */
	public ImageProcessor expandImage(ImageProcessor ipOld, int wNew, int hNew, int xOff, int yOff) {
	ImageProcessor ipNew = ipOld.createProcessor(wNew, hNew);
		if (zeroFill) {
			ipNew.setValue(0.0);
		} else {
			ipNew.setColor(Toolbar.getBackgroundColor());
		}
		ipNew.fill();
		ipNew.insert(ipOld, xOff, yOff);
		return ipNew;
	}

}

