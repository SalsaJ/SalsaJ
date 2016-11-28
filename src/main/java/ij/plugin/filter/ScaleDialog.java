package ij.plugin.filter;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.*;
import ij.util.Tools;
import java.awt.*;
import java.awt.event.*;

/**
 *  Implements the Analyze/Set Scale command.
 *
 *@author     Thomas
 *@created    30 novembre 2007
 */
public class ScaleDialog implements PlugInFilter {

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
		IJ.register(ScaleDialog.class);
		return DOES_ALL + NO_CHANGES;
	}


	/**
	 *  Main processing method for the ScaleDialog object
	 *
	 *@param  ip  Description of the Parameter
	 */
	public void run(ImageProcessor ip) {
	double measured = 0.0;
	double known = 1.0;
	double aspectRatio = 1.0;
	String unit = "cm";
	boolean global1 = imp.getGlobalCalibration() != null;
	boolean global2;
	Calibration cal = imp.getCalibration();
	Calibration calOrig = cal.copy();
	boolean isCalibrated = cal.scaled();

	String scale = "<no scale>";
	int digits = 2;
		if (isCalibrated) {
			measured = 1.0 / cal.pixelWidth;
			digits = Tools.getDecimalPlaces(measured, measured);
			known = 1.0;
			aspectRatio = cal.pixelHeight / cal.pixelWidth;
			unit = cal.getUnit();
			scale = IJ.d2s(measured, digits) + " pixels/" + unit;
		}
	Roi roi = imp.getRoi();
		if (roi != null && (roi instanceof Line)) {
			measured = ((Line) roi).getRawLength();
			known = 0.0;
		}
//EU_HOU Bundle
	SetScaleDialog gd = new SetScaleDialog(IJ.getPluginBundle().getString("ScDiagTitle"), scale);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("ScDiagDistPix") + ":", measured, digits, 8, null);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("ScDiagDist") + ":", known, 2, 8, null);
		//EU_HOU Bundle
		gd.addNumericField(IJ.getPluginBundle().getString("ScDiagRatio") + ":", aspectRatio, 1, 8, null);
		//EU_HOU Bundle
		gd.addStringField(IJ.getPluginBundle().getString("ScDiagUnit") + ":", unit);
		//EU_HOU Bundle
		gd.addMessage(IJ.getPluginBundle().getString("ScDiagScale") + ":" + "12345.789 pixels per centimeter");
		//EU_HOU Bundle
		gd.addCheckbox(IJ.getPluginBundle().getString("ScDiagGlobal"), global1);
		gd.addPanel(makeButtonPanel(gd), GridBagConstraints.EAST, new Insets(5, 0, 0, 25));
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		measured = gd.getNextNumber();
		known = gd.getNextNumber();
		aspectRatio = gd.getNextNumber();
		unit = gd.getNextString();
		if (unit.equals("um")) {
			unit = IJ.micronSymbol + "m";
		} else if (unit.equals("A")) {
			unit = "" + IJ.angstromSymbol;
		}
		global2 = gd.getNextBoolean();
		if (measured != 0.0 && known == 0.0) {
			imp.setGlobalCalibration(global2 ? cal : null);
			return;
		}
		if (measured <= 0.0 || unit.startsWith("pixel") || unit.startsWith("Pixel") || unit.equals("")) {
			cal.pixelWidth = 1.0;
			cal.pixelHeight = 1.0;
			cal.pixelDepth = 1.0;
			cal.setUnit("pixel");
		} else {
			cal.pixelWidth = known / measured;
			if (aspectRatio != 0.0) {
				cal.pixelHeight = cal.pixelWidth * aspectRatio;
			} else {
				cal.pixelHeight = cal.pixelWidth;
			}
			cal.pixelDepth = cal.pixelWidth;
			cal.setUnit(unit);
		}
		if (!cal.equals(calOrig)) {
			imp.setCalibration(cal);
		}
		imp.setGlobalCalibration(global2 ? cal : null);
		if (global2 || global2 != global1) {
			WindowManager.repaintImageWindows();
		} else {
			imp.repaintWindow();
		}
	}


	/**
	 *  Creates a panel containing an "Unscale" button.
	 *
	 *@param  gd  Description of the Parameter
	 *@return     Description of the Return Value
	 */
	Panel makeButtonPanel(SetScaleDialog gd) {
	Panel panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		//EU_HOU Bundle
		gd.unscaleButton = new Button("Reset");
		gd.unscaleButton.addActionListener(gd);
		panel.add(gd.unscaleButton);
		return panel;
	}

}

/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    30 novembre 2007
 */
class SetScaleDialog extends GenericDialog {


	final static String NO_SCALE = "<no scale>";
	String initialScale;
	Button unscaleButton;


	/**
	 *  Constructor for the SetScaleDialog object
	 *
	 *@param  title  Description of the Parameter
	 *@param  scale  Description of the Parameter
	 */
	public SetScaleDialog(String title, String scale) {
		super(title);
		initialScale = scale;
	}


	/**
	 *  Description of the Method
	 */
	protected void setup() {
		initialScale += "          ";
		setScale(initialScale);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void textValueChanged(TextEvent e) {
	Double d = getValue(((TextField) numberField.elementAt(0)).getText());
		if (d == null) {
			setScale(NO_SCALE);
			return;
		}
	double measured = d.doubleValue();
		d = getValue(((TextField) numberField.elementAt(1)).getText());
		if (d == null) {
			setScale(NO_SCALE);
			return;
		}
	double known = d.doubleValue();
	String theScale;
	String unit = ((TextField) stringField.elementAt(0)).getText();
	boolean noScale = measured <= 0 || known <= 0 || unit.startsWith("pixel") || unit.startsWith("Pixel") || unit.equals("");
		if (noScale) {
			theScale = NO_SCALE;
		} else {
		double scale = measured / known;
		int digits = Tools.getDecimalPlaces(scale, scale);
			theScale = IJ.d2s(scale, digits) + " pixels/" + unit;
		}
		setScale(theScale);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == unscaleButton) {
			((TextField) numberField.elementAt(0)).setText("0.00");
			((TextField) numberField.elementAt(1)).setText("1.00");
			((TextField) numberField.elementAt(2)).setText("1.0");
			((TextField) stringField.elementAt(0)).setText("pixel");
			setScale(NO_SCALE);
		}
	}


	/**
	 *  Sets the scale attribute of the SetScaleDialog object
	 *
	 *@param  theScale  The new scale value
	 */
	void setScale(String theScale) {
		//EU_HOU Bundle
		((Label) theLabel).setText("Scale: " + theScale);
	}

}

