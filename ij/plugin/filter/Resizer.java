package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 *  This plugin implements ImageJ's Resize command.
 *
 *@author     Thomas
 *@created    30 novembre 2007
 */
public class Resizer implements PlugInFilter, TextListener, ItemListener {
	ImagePlus imp;
	private boolean crop;
	private static int newWidth;
	private static int newHeight;
	private static boolean constrain = true;
	private static boolean interpolate = true;
	private Vector fields, checkboxes;
	private double origWidth, origHeight;
	private boolean sizeToHeight;


	/**
	 *  Description of the Method
	 *
	 *@param  arg  Description of the Parameter
	 *@param  imp  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public int setup(String arg, ImagePlus imp) {
		crop = arg.equals("crop");
		this.imp = imp;
		IJ.register(Resizer.class);
		if (crop) {
			return DOES_ALL + ROI_REQUIRED + NO_CHANGES;
		} else {
			return DOES_ALL + NO_CHANGES;
		}
	}


	/**
	 *  Main processing method for the Resizer object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
	Roi roi = imp.getRoi();
		if (roi != null && roi.isLine()) {
			//EU_HOU Bundle
			IJ.error("The Crop and Adjust->Size commands\ndo not work with line selections.");
			return;
		}
	Rectangle r = ip.getRoi();
		origWidth = r.width;
		;
		origHeight = r.height;
		sizeToHeight = false;
	boolean restoreRoi = crop && roi != null && roi.getType() != Roi.RECTANGLE;
		if (roi != null) {
		Rectangle b = roi.getBounds();
		int w = ip.getWidth();
		int h = ip.getHeight();
			if (b.x < 0 || b.y < 0 || b.x + b.width > w || b.y + b.height > h) {
			ShapeRoi shape1 = new ShapeRoi(roi);
			ShapeRoi shape2 = new ShapeRoi(new Roi(0, 0, w, h));
				roi = shape2.and(shape1);
				if (restoreRoi) {
					imp.setRoi(roi);
				}
			}
		}
		if (crop) {
		Rectangle bounds = roi.getBounds();
			newWidth = bounds.width;
			newHeight = bounds.height;
			interpolate = false;
		} else {
			if (newWidth == 0 || newHeight == 0) {
				newWidth = (int) origWidth / 2;
				newHeight = (int) origHeight / 2;
			}
			if (constrain) {
				newHeight = (int) (newWidth * (origHeight / origWidth));
			}
		//EU_HOU Bundle
		GenericDialog gd = new GenericDialog(IJ.getPluginBundle().getString("ResizeTitle"), IJ.getInstance());
			//EU_HOU Bundle
			gd.addNumericField(IJ.getBundle().getString("Width") + " " + IJ.getBundle().getString("Pixels") + ":", newWidth, 0);
			//EU_HOU Bundle
			gd.addNumericField(IJ.getBundle().getString("Height") + " " + IJ.getBundle().getString("Pixels") + ":", newHeight, 0);
			//EU_HOU Bundle
			gd.addCheckbox(IJ.getPluginBundle().getString("ResizeConstrain"), constrain);
			//EU_HOU Bundle
			gd.addCheckbox(IJ.getPluginBundle().getString("Interpolate"), interpolate);
			//EU_HOU Bundle
			gd.addMessage(IJ.getPluginBundle().getString("ResizeWarn"));
			//EU_HOU Bundle
			fields = gd.getNumericFields();
			for (int i = 0; i < fields.size(); i++) {
				((TextField) fields.elementAt(i)).addTextListener(this);
			}
			checkboxes = gd.getCheckboxes();
			((Checkbox) checkboxes.elementAt(0)).addItemListener(this);
			gd.showDialog();
			if (gd.wasCanceled()) {
				return;
			}
			newWidth = (int) gd.getNextNumber();
			newHeight = (int) gd.getNextNumber();
			if (gd.invalidNumber()) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("ResizeErr"));
				return;
			}
			constrain = gd.getNextBoolean();
			interpolate = gd.getNextBoolean();
			if (constrain && newWidth == 0) {
				sizeToHeight = true;
			}
			if (newWidth <= 0.0 && !constrain) {
				newWidth = 50;
			}
			if (newHeight <= 0.0) {
				newHeight = 50;
			}
		}

		if (!crop && constrain) {
			if (sizeToHeight) {
				newWidth = (int) (newHeight * (origWidth / origHeight));
			} else {
				newHeight = (int) (newWidth * (origHeight / origWidth));
			}
		}
		if (ip.getWidth() == 1 || ip.getHeight() == 1) {
			ip.setInterpolate(false);
		} else {
			ip.setInterpolate(interpolate);
		}

	int nSlices = imp.getStackSize();
		try {
		StackProcessor sp = new StackProcessor(imp.getStack(), ip);
		ImageStack s2 = sp.resize(newWidth, newHeight);
		int newSize = s2.getSize();
			if (s2.getWidth() > 0 && newSize > 0) {
				if (restoreRoi) {
					imp.killRoi();
				}
			//imp.hide();
			Calibration cal = imp.getCalibration();
				if (cal.scaled()) {
					cal.pixelWidth *= origWidth / newWidth;
					cal.pixelHeight *= origHeight / newHeight;
					imp.setCalibration(cal);
				}
				imp.setStack(null, s2);
				//imp.show();
				if (restoreRoi) {
					imp.restoreRoi();
					roi = imp.getRoi();
					if (roi != null) {
						roi.setLocation(0, 0);
						imp.draw();
					}
				}
			}
			if (nSlices > 1 && newSize < nSlices) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("ResizeLost1") + (nSlices - newSize) + IJ.getPluginBundle().getString("ResizeLost2"));
			}
		} catch (OutOfMemoryError o) {
			//EU_HOU Bundle
			IJ.outOfMemory("Resize");
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void textValueChanged(TextEvent e) {
	TextField widthField = (TextField) fields.elementAt(0);
	TextField heightField = (TextField) fields.elementAt(1);
	int width = (int) Tools.parseDouble(widthField.getText(), -99);
	int height = (int) Tools.parseDouble(heightField.getText(), -99);
		if (width == -99 || height == -99) {
			return;
		}
		if (constrain) {
			if (width != newWidth) {
				sizeToHeight = false;
				newWidth = width;
				updateFields();
			} else if (height != newHeight) {
				sizeToHeight = true;
				newHeight = height;
				updateFields();
			}
		}
	}


	/**
	 *  Description of the Method
	 */
	void updateFields() {
		if (sizeToHeight) {
			newWidth = (int) (newHeight * (origWidth / origHeight));
		TextField widthField = (TextField) fields.elementAt(0);
			widthField.setText("" + newWidth);
		} else {
			newHeight = (int) (newWidth * (origHeight / origWidth));
		TextField heightField = (TextField) fields.elementAt(1);
			heightField.setText("" + newHeight);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void itemStateChanged(ItemEvent e) {
	Checkbox cb = (Checkbox) checkboxes.elementAt(0);
	boolean newConstrain = cb.getState();
		if (newConstrain && newConstrain != constrain) {
			updateFields();
		}
		constrain = newConstrain;
	}

}

