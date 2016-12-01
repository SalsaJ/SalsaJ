package ij.plugin.filter;

import java.awt.*;
import java.awt.image.*;
import java.util.Vector;
import java.io.*;
import ij.*;
import ij.process.*;
import ij.io.*;
import ij.gui.*;
import ij.measure.*;

/**
 *  Saves the XY coordinates of the current ROI boundary.
 *
 *@author     Thomas
 *@created    3 decembre 2007
 */
public class XYWriter implements PlugInFilter {
	ImagePlus imp;


	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL + ROI_REQUIRED + NO_CHANGES;
	}


	/**
	 *  Main processing method for the XYWriter object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
		try {
			saveXYCoordinates(imp);
		} catch (IllegalArgumentException e) {
			//EU_HOU Bundle
			IJ.error("XYWriter", e.getMessage());
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 */
	public void saveXYCoordinates(ImagePlus imp) {
	Roi roi = imp.getRoi();
		if (roi == null) {
			//EU_HOU Bundle
			throw new IllegalArgumentException("ROI required");
		}
		if (!(roi instanceof PolygonRoi)) {
			//EU_HOU Bundle
			throw new IllegalArgumentException("Irregular area or line selection required");
		}
//EU_HOU Bundle
	SaveDialog sd = new SaveDialog("Save Coordinates as Text...", imp.getTitle(), ".txt");
	String name = sd.getFileName();
		if (name == null) {
			return;
		}
	String directory = sd.getDirectory();
	PrintWriter pw = null;
		try {
		FileOutputStream fos = new FileOutputStream(directory + name);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
			pw = new PrintWriter(bos);
		} catch (IOException e) {
			//EU_HOU Bundle
			IJ.error("XYWriter", "" + e);
			return;
		}

	Rectangle r = roi.getBounds();
	PolygonRoi p = (PolygonRoi) roi;
	int n = p.getNCoordinates();
	int[] x = p.getXCoordinates();
	int[] y = p.getYCoordinates();

	Calibration cal = imp.getCalibration();
	//EU_HOU Bundle
	String ls = System.getProperty("line.separator");
	boolean scaled = cal.scaled();
		for (int i = 0; i < n; i++) {
			if (scaled) {
				pw.print(IJ.d2s((r.x + x[i]) * cal.pixelWidth) + "\t" + IJ.d2s((r.y + y[i]) * cal.pixelHeight) + ls);
			} else {
				pw.print((r.x + x[i]) + "\t" + (r.y + y[i]) + ls);
			}
		}
		pw.close();
	}

}

