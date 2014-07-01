package ij.plugin;
import ij.*;
import ij.process.*;
import ij.gui.*;

/**
 *  This plugin implements the Plugins/Utilities/Unlock, Image/Rename and
 *  Plugins/Utilities/Search commands.
 *
 *@author     Thomas
 *@created    30 novembre 2007
 */
public class SimpleCommands implements PlugIn {
	static String searchArg;
	private static String[] choices = {"Locked Image", "Clipboard", "Undo Buffer"};
	private static int choiceIndex = 0;


	/**
	 *  Main processing method for the SimpleCommands object
	 *
	 *@param  arg  Description of the Parameter
	 */
	public void run(String arg) {
		if (arg.equals("search")) {
			search();
		}
		if (arg.equals("import")) {
			IJ.runMacroFile("ij.jar:ImportResultsTable");
		} else if (arg.equals("rename")) {
			rename();
		} else if (arg.equals("reset")) {
			reset();
		} else if (arg.equals("about")) {
			aboutPluginsHelp();
		} else if (arg.equals("install")) {
			installation();
		}
	}


	/**
	 *  Description of the Method
	 */
	void reset() {
	GenericDialog gd = new GenericDialog("");
		//EU_HOU Bundle
		gd.addChoice("Reset:", choices, choices[choiceIndex]);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}
		choiceIndex = gd.getNextChoiceIndex();
		switch (choiceIndex) {
						case 0:
							unlock();
							break;
						case 1:
							resetClipboard();
							break;
						case 2:
							resetUndo();
							break;
		}
	}


	/**
	 *  Description of the Method
	 */
	void unlock() {
	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}
	boolean wasUnlocked = imp.lockSilently();
		if (wasUnlocked) {
			//EU_HOU Bundle
			IJ.showStatus("\"" + imp.getTitle() + "\" is not locked");
		} else {
			//EU_HOU Bundle
			IJ.showStatus("\"" + imp.getTitle() + "\" is now unlocked");
			IJ.beep();
		}
		imp.unlock();
	}


	/**
	 *  Description of the Method
	 */
	void resetClipboard() {
		ImagePlus.resetClipboard();
		//EU_HOU Bundle
		IJ.showStatus("Clipboard reset");
	}


	/**
	 *  Description of the Method
	 */
	void resetUndo() {
		Undo.setup(Undo.NOTHING, null);
		//EU_HOU Bundle
		IJ.showStatus("Undo reset");
	}


	/**
	 *  Description of the Method
	 */
	void rename() {
	ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}
	//EU_HOU Bundle
	GenericDialog gd = new GenericDialog(IJ.getBundle().getString("Rename"));
		gd.addStringField(IJ.getPluginBundle().getString("Title") + ":", imp.getTitle(), 30);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		} else {
			imp.setTitle(gd.getNextString());
		}
	}


	/**
	 *  Description of the Method
	 */
	void search() {
		searchArg = IJ.runMacroFile("ij.jar:Search", searchArg);
	}


	/**
	 *  Description of the Method
	 */
	void installation() {
	String url = "http://rsb.info.nih.gov/ij/docs/install/";
		if (IJ.isMacintosh()) {
			url += "osx.html";
		} else if (IJ.isWindows()) {
			url += "windows.html";
		} else if (IJ.isLinux()) {
			url += "linux.html";
		}
		IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
	}


	/**
	 *  Description of the Method
	 */
	void aboutPluginsHelp() {
		//EU_HOU Bundle
		IJ.showMessage("\"About Plugins\" Submenu",
				"Plugins packaged as JAR files can add entries\n" +
				"to this submenu. There is an example at\n \n" +
				"http://rsb.info.nih.gov/ij/plugins/jar-demo.html");
	}

}

