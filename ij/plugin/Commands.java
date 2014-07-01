package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.frame.*;
import ij.text.TextWindow;
import ij.macro.Interpreter;
import java.awt.Frame;
import java.io.File;
import java.applet.Applet;

/**
 *  Runs File and Window menu commands.
 *
 *@author     Thomas
 *@created    3 decembre 2007
 */
public class Commands implements PlugIn {

	/**
	 *  Main processing method for the Commands object
	 *
	 *@param  cmd  Description of the Parameter
	 */
	public void run(String cmd) {
		System.out.println("Commands.run :" + cmd);
		if (cmd.equals("new")) {
			System.out.println("Commands.new");
			new NewImage();
		} else if (cmd.equals("open")) {
			if (Prefs.useJFileChooser && !IJ.macroRunning()) {
				System.out.println("Commands.run2");
				new Opener().openMultiple();
			} else {
				System.out.println("Commands.run3");
				new Opener().open();
			}
		} else if (cmd.equals("close")) {
			System.out.println("Commands.run4");
			close();
		} else if (cmd.equals("save")) {
			System.out.println("Commands.run5");
			save();
		} else if (cmd.equals("ij")) {
			System.out.println("Commands.run6");
		ImageJ ij = IJ.getInstance();
			if (ij != null) {
				System.out.println("Commands.run7");
				ij.toFront();
			}
		} else if (cmd.equals("tab")) {
			System.out.println("Commands.run8");
			WindowManager.putBehind();
		} else if (cmd.equals("quit")) {
			System.out.println("Commands.run9");
		ImageJ ij = IJ.getInstance();
			if (ij != null) {
				System.out.println("Commands.run10");
				ij.quit();
			}
		} else if (cmd.equals("revert")) {
			System.out.println("Commands.run11");
			revert();
		} else if (cmd.equals("undo")) {
			System.out.println("Commands.run12");
			undo();
		} else if (cmd.equals("startup")) {
			System.out.println("Commands.run13");
			openStartupMacros();
		}
	}


	/**
	 *  Description of the Method
	 */
	void revert() {
	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null) {
			imp.revert();
		} else {
			IJ.noImage();
		}
	}


	/**
	 *  Description of the Method
	 */
	void save() {
	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null) {
			new FileSaver(imp).save();
		} else {
			IJ.noImage();
		}
	}


	/**
	 *  Description of the Method
	 */
	void undo() {
	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp != null) {
			Undo.undo();
		} else {
			IJ.noImage();
		}
	}


	/**
	 *  Description of the Method
	 */
	void close() {
	ImagePlus imp = WindowManager.getCurrentImage();
	Frame frame = WindowManager.getFrontWindow();
		if (frame == null || (Interpreter.isBatchMode() && frame instanceof ImageWindow)) {
			closeImage(imp);
		} else if (frame instanceof PlugInFrame) {
			((PlugInFrame) frame).close();
		} else if (frame instanceof TextWindow) {
			((TextWindow) frame).close();
		} else {
			closeImage(imp);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  imp  Description of the Parameter
	 */
	void closeImage(ImagePlus imp) {
		if (imp == null) {
			IJ.noImage();
			return;
		}
		imp.close();
		if (Recorder.record) {
			Recorder.record("close");
			Recorder.setCommand(null);// don't record run("Close")
		}
	}

	// Plugins>Macros>Open Startup Macros command
	/**
	 *  Description of the Method
	 */
	void openStartupMacros() {
	Applet applet = IJ.getApplet();
		if (applet != null) {
			IJ.run("URL...", "url=http://rsb.info.nih.gov/ij/applet/StartupMacros.txt");
		} else {
		String path = IJ.getDirectory("macros") + "/StartupMacros.txt";
		File f = new File(path);
			if (!f.exists()) {
				//EU_HOU Bundle
				IJ.error("\"StartupMacros.txt\" not found in ImageJ/macros/");
			} else {
				IJ.open(path);
			}
		}
	}

}


