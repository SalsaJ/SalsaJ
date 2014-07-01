//EU_HOU
package ij;
import ij.util.Tools;
import ij.text.TextWindow;
import ij.plugin.MacroInstaller;
import ij.plugin.frame.Recorder;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
//EU_HOU Changes
import ij.gui.*;

/**
 *  Runs ImageJ menu commands in a separate thread.
 *
 * @author     Thomas
 * @created    7 novembre 2007
 */
public class Executer implements Runnable {

	private static String previousCommand;

	private String command;
	private Thread thread;
	/*
	 *  EU_HOU CHANGES
	 */
	private static ResourceBundle menubun;
	private static ResourceBundle etiq;


	/*
	 *  EU_HOU CHANGES
	 */
	/**
	 *  Create an Executer to run the specified menu command in this thread using
	 *  the active image.
	 *
	 * @param  cmd  Description of the Parameter
	 */
	public Executer(String cmd) {
		command = cmd;
	}


	/**
	 *  Create an Executer that runs the specified menu command in a separate
	 *  thread using the active image image.
	 *
	 * @param  cmd      Description of the Parameter
	 * @param  ignored  Description of the Parameter
	 */
	public Executer(String cmd, ImagePlus ignored) {
		if (cmd.startsWith("Repeat")) {
			command = previousCommand;
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
		} else {
			command = cmd;
			if (!(cmd.equals("Undo") || cmd.equals("Close"))) {
				previousCommand = cmd;
			}
		}
		IJ.resetEscape();
		thread = new Thread(this, cmd);
		thread.setPriority(Math.max(thread.getPriority() - 2, Thread.MIN_PRIORITY));
		thread.start();
		/*
		 *   EU_HOU Changes
		 */
		menubun = IJ.getBundle();
		/*
		 *  EU_HOU End
		 */
	}


	/**
	 *  Main processing method for the Executer object
	 */
	public void run() {
		if (command == null) {
			return;
		}
		try {
			if (Recorder.record) {
				Recorder.setCommand(command);
				runCommand(command);
				Recorder.saveCommand();
			} else {
				runCommand(command);
			}
		} catch (Throwable e) {
			IJ.showStatus("");
			IJ.showProgress(1.0);

			ImagePlus imp = WindowManager.getCurrentImage();

			if (imp != null) {
				imp.unlock();
			}
			String msg = e.getMessage();

			if (e instanceof OutOfMemoryError) {
				IJ.outOfMemory(command);
			} else if (e instanceof RuntimeException && msg != null && msg.equals(Macro.MACRO_CANCELED)) {
				;
			} //do nothing
			else {
				CharArrayWriter caw = new CharArrayWriter();
				PrintWriter pw = new PrintWriter(caw);

				e.printStackTrace(pw);

				String s = caw.toString();

				if (IJ.isMacintosh()) {
					if (s.indexOf("ThreadDeath") > 0) {
						return;
					}
					s = Tools.fixNewLines(s);
				}
				if (IJ.getInstance() != null) {
					new TextWindow("Exception", s, 350, 250);
				} else {
					IJ.log(s);
				}
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 * @param  cmd  Description of the Parameter
	 */
	void runCommand(String cmd) {
		/*
	 *  EU_HOU CHANGES
	 */
		Hashtable table = Menus.getCommands();
		String className = new String();
		String[] cN = new String[2];
		EuHouToolbar etb = new EuHouToolbar();
		String PhotometerGood = "";
		String ClearPhotometerGood = "";
		String PhotometrySettingsGood = "";
		String menubunGood = "";
		String addPluginItemGood = "";

		System.out.println("Executer avant if1 cmd: " + cmd);
		try {
			System.out.println("Executer 1");
			//EU_HOU Bundle
			PhotometerGood = menubun.getString("Photometer");
		} catch (Exception exp) {}
		try {
			System.out.println("Executer 2");
			//EU_HOU Bundle
			ClearPhotometerGood = menubun.getString("ClearPhotometer");
		} catch (Exception exp) {}
		try {
			System.out.println("Executer 3");
			//EU_HOU Bundle
			PhotometrySettingsGood = menubun.getString("PhotometrySettings");
		} catch (Exception exp) {}
		try {
			System.out.println("Executer 4");
			addPluginItemGood = etb.addPluginItem(cmd);
			System.out.println("Executer addPluginItemGood: " + addPluginItemGood);
		} catch (Exception exp) {}
		try {
			System.out.println("Executer 5");
			//EU_HOU Bundle
			menubunGood = menubun.getString(cmd);
			System.out.println("Executer menubunGood: " + menubunGood);
		} catch (Exception exp) {}

		if (cmd.equals(PhotometerGood) || cmd.equals("Photometer")) {
			EuHouToolbar.getInstance().photometerButtonAction();
		} else if (cmd.equals(ClearPhotometerGood) || cmd.equals("ClearPhotometer")) {
			EuHouToolbar.getInstance().clearPhotometer();
		} else if (cmd.equals(PhotometrySettingsGood) || cmd.equals("PhotometrySettings")) {
			EuHouToolbar.getInstance().settingsAction();
		} else if (table.get(cmd) != null) {
			className = (String) table.get(cmd);
		} else if (addPluginItemGood != "") {
			className = (String) table.get(addPluginItemGood);
		} else if (menubunGood != "") {
			className = (String) table.get(menubunGood);
		}
		System.out.println("Executer apres if cmd: " + cmd + " , className: " + className);
		/*
		 *  EU_HOU END
		 */
		if (className != null) {
			String arg = "";

			if (className.endsWith("\")")) {
				// extract string argument (e.g. className("arg"))
				int argStart = className.lastIndexOf("(\"");

				System.out.println("arg :" + arg);
				if (argStart > 0) {
					arg = className.substring(argStart + 2, className.length() - 2);
					className = className.substring(0, argStart);
				}
			}
			if (IJ.shiftKeyDown() && className.startsWith("ij.plugin.Macro_Runner")) {
				IJ.open(IJ.getDirectory("plugins") + arg);
				IJ.setKeyUp(KeyEvent.VK_SHIFT);
			} else {
				IJ.runPlugIn(cmd, className, arg);
			}
		} else {
			// Is this command in Plugins>Macros?
			if (MacroInstaller.runMacroCommand(cmd)) {
				return;
			}

			// Is this command a LUT name?
			String path = Prefs.getHomeDir() + File.separator + "luts" + File.separator + cmd + ".lut";
			File f = new File(path);

			if (f.exists()) {
				IJ.open(path);
			} else {
				//EU_HOU Bundle
				IJ.error(etiq.getString("UnrecCmdErr") + " " + cmd);
			}
		}
	}



	/**
	 *  Returns the last command executed. Returns null if no command has been
	 *  executed.
	 *
	 * @return    The command value
	 */
	public static String getCommand() {
		return previousCommand;
	}

}


