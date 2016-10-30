package ij.plugin.filter;
import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import java.awt.event.*;

/**
 *  Implements the Process/Plot Profile and Edit/Options/Profile Plot Options
 *  commands.
 *
 *@author     Thomas
 *@created    31 octobre 2007
 */
public class Profiler implements PlugInFilter {

	ImagePlus imp;
	static boolean verticalProfile;
	/*
	 *  EU_HOU CHANGES
	 */
	static boolean horizontalProfile;


	/*
	 *  EU_HOU END
	 */
	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		if (arg.equals("set")) {
			doOptions();
			return DONE;
		}
		this.imp = imp;
		return DOES_ALL + NO_UNDO + NO_CHANGES + ROI_REQUIRED;
	}


	/**
	 *  Main processing method for the Profiler object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
	boolean averageHorizontally = verticalProfile || IJ.altKeyDown();
		new ProfilePlot(imp, averageHorizontally).createWindow();
                //System.out.println("plot profil");
	}


	/**
	 *  Description of the Method
	 */
	public void doOptions() {
	double ymin = ProfilePlot.getFixedMin();
	double ymax = ProfilePlot.getFixedMax();
	boolean fixedScale = ymin != 0.0 || ymax != 0.0;
	boolean wasFixedScale = fixedScale;
//EU_HOU Bundle
	GenericDialog gd = new GenericDialog(IJ.getPluginBundle().getString("ProOptTitle"), IJ.getInstance());
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("Width") + " (" + IJ.getPluginBundle().getString("Pixels") + "):", PlotWindow.plotWidth, 0);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("Height") + " (" + IJ.getPluginBundle().getString("Pixels") + "):", PlotWindow.plotHeight, 0);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("Min") + " Y:", ymin, 2);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("Max") + " Y:", ymax, 2);
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ProOptScale"), fixedScale);
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ProOptSave"), !PlotWindow.saveXValues);
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ProOptAuto"), PlotWindow.autoClose);
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ProOptVert"), verticalProfile);
		//EU_HOU Bundle
		//gd.addCheckbox("List Values", PlotWindow.listValues);
		//EU_HOU Bundle
		// gd.addCheckbox("Interpolate Line Profiles", PlotWindow.interpolate);
		//EU_HOU Bundle
		//gd.addCheckbox("Draw Grid Lines", !PlotWindow.noGridLines);
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ProOptHoriz"), horizontalProfile);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
	Dimension screen = IJ.getScreenSize();
	int w = (int) gd.getNextNumber();
	int h = (int) gd.getNextNumber();
		if (w < 300) {
			w = 300;
		}
		if (w > screen.width - 140) {
			w = screen.width - 140;
		}
		if (h < 100) {
			h = 100;
		}
		if (h > screen.height - 300) {
			h = screen.height - 300;
		}
		PlotWindow.plotWidth = w;
		PlotWindow.plotHeight = h;
		ymin = gd.getNextNumber();
		ymax = gd.getNextNumber();
		fixedScale = gd.getNextBoolean();
		PlotWindow.saveXValues = !gd.getNextBoolean();
		PlotWindow.autoClose = gd.getNextBoolean();
		verticalProfile = gd.getNextBoolean();
		/*
		 *  EU_HOU CHANGES
		 */
		horizontalProfile = gd.getNextBoolean();
		/*
		 *  EU_HOU END
		 */
		//PlotWindow.listValues = gd.getNextBoolean();
		//PlotWindow.interpolate = gd.getNextBoolean();
		//PlotWindow.noGridLines = !gd.getNextBoolean();
		if (!fixedScale && !wasFixedScale && (ymin != 0.0 || ymax != 0.0)) {
			fixedScale = true;
		}
		if (!fixedScale) {
			ymin = 0.0;
			ymax = 0.0;
		} else if (ymin > ymax) {
		double tmp = ymin;
			ymin = ymax;
			ymax = tmp;
		}
		ProfilePlot.setMinAndMax(ymin, ymax);
		IJ.register(Profiler.class);
	}

}

