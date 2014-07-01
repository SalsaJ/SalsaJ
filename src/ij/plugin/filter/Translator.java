/*
 *  Translator.java
 *
 *  Created on 12 d�cembre 2004, 16:41
 */
package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import java.awt.*;

/**
 *  This plugin implements the Image/Rotate command.
 *
 *@author     Thomas
 *@created    21 novembre 2007
 */
public class Translator implements PlugInFilter {
	private static double tx = 0;
	private static double ty = 0;
	private static boolean interpolate = true;
	private static boolean firstTime;
	private static boolean canceled;
	private ImagePlus imp;


	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		IJ.register(Translator.class);
		firstTime = true;
		canceled = false;
		return IJ.setupDialog(imp, DOES_ALL);
	}


	/**
	 *  Main processing method for the Translator object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
		if (canceled) {
			return;
		}
		if (firstTime) {
		//EU_HOU Bundle
		GenericDialog gd = new GenericDialog(IJ.getPluginBundle().getString("Translate"), IJ.getInstance());
			//EU_HOU Bundle
			gd.addNumericField(IJ.getPluginBundle().getString("TX") + ":", tx, 1);
			//EU_HOU Bundle
			gd.addNumericField(IJ.getPluginBundle().getString("TY") + ":", ty, 1);
			//EU_HOU Bundle
			gd.addCheckbox(IJ.getPluginBundle().getString("Interpolate"), interpolate);
			gd.showDialog();
			canceled = gd.wasCanceled();
			if (canceled) {
				return;
			}
			tx = gd.getNextNumber();
			if (gd.invalidNumber()) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("TxErr"));
				return;
			}
			ty = gd.getNextNumber();
			if (gd.invalidNumber()) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("TyErr"));
				return;
			}
			interpolate = gd.getNextBoolean();
			imp.startTiming();
			firstTime = false;
		}
		ip.setInterpolate(interpolate);
		ip.translate(tx, ty);
	}

}

