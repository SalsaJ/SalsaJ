//EU_HOU
package ij;

import ij.gui.*;
import ij.process.*;
import ij.text.*;
import ij.io.*;
import ij.plugin.*;
import ij.plugin.filter.*;
import ij.util.Tools;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import java.awt.event.*;
import java.text.*;
import java.util.Locale;
import java.awt.*;
import java.applet.Applet;
import java.io.*;
import java.lang.reflect.*;
/*
 * EU_HOU CHANGES
 */
import java.util.ResourceBundle;
import ij.plugin.frame.Recorder;
import ij.*;
import java.net.URL;
import java.net.URLConnection;
/*
 * EU_HOU END
 */

/**
 * This class consists of static utility methods.
 *
 * @author Thomas @created 30 octobre 2007
 */
public class IJ {

    /**
     * Description of the Field
     */
    public final static int ALL_KEYS = 0x32;
    /**
     * Description of the Field
     */
    public static boolean debugMode;
    public static boolean debug = true;
    /**
     * Description of the Field
     */
    public static boolean hideProcessStackDialog;
    /**
     * Description of the Field
     */
    public final static char micronSymbol = '\u00B5';
    /**
     * Description of the Field
     */
    public final static char angstromSymbol = '\u00C5';
    /**
     * Description of the Field
     */
    public final static char degreeSymbol = '\u00B0';
    private static ImageJ ij;
    private static java.applet.Applet applet;
    private static ProgressBar progressBar;
    private static TextPanel textPanel;
    private static String osname;
    private static boolean isMac, isWin, isJava2, isJava14, isJava15, isJava16, isLinux, isVista;
    private static boolean altDown, spaceDown, shiftDown;
    private static boolean macroRunning;
    private static Thread previousThread;
    private static TextPanel logPanel;
    private static boolean notVerified = true;
    private static ClassLoader classLoader;
    private static boolean memMessageDisplayed;
    private static long maxMemory;
    private static boolean escapePressed;
    private static boolean redirectErrorMessages;
    private static boolean suppressPluginNotFoundError;
    private static Dimension screenSize;
    /*
     * EU_HOU CHANGES
     */
    private static ResourceBundle menubun, plgbun, colorbun, blitbun, toolbun;
    private static Locale locale;
    private static int ExecLevel;
    /**
     * Description of the Field
     */
    public final static int LEVEL_EASY = 1;
    /**
     * Description of the Field
     */
    public final static int LEVEL_INTER = 2;
    /**
     * Description of the Field
     */
    public final static int LEVEL_TOUGH = 3;
    /**
     * Description of the Field
     */
    public final static int LEVEL_TEACH = 10;
    /**
     * Description of the Field
     */
    public final static int LEVEL_DEVEL = 30;
    /*
     * END EU_HOU END
     */

    static {
        osname = System.getProperty("os.name");
        isWin = osname.startsWith("Windows");
        isMac = !isWin && osname.startsWith("Mac");
        isLinux = osname.startsWith("Linux");
        isVista = isWin && osname.indexOf("Vista") != -1;

        String version = System.getProperty("java.version").substring(0, 3);

        if (version.compareTo("2.9") <= 0) {// JVM on Sharp Zaurus PDA claims to be "3.1"!
            isJava2 = version.compareTo("1.1") > 0;
            isJava14 = version.compareTo("1.3") > 0;
            isJava15 = version.compareTo("1.4") > 0;
            isJava16 = version.compareTo("1.5") > 0;
        }
    }

    /**
     * Description of the Method
     *
     * @param imagej Description of the Parameter
     * @param theApplet Description of the Parameter
     */
    static void init(ImageJ imagej, Applet theApplet) {
        ij = imagej;
        applet = theApplet;
        progressBar = ij.getProgressBar();
    }


    /*
     * EU_HOU Add
     */
    /**
     * Gets the locale attribute of the IJ class
     *
     * @return The locale value
     */
    public static Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale attribute of the IJ class
     *
     * @param loc The new locale value
     */
    public static void setLocale(Locale loc) {
        locale = loc;
    }

    /**
     * Gets the bundle attribute of the IJ class
     *
     * @return The bundle value
     */
    public static ResourceBundle getBundle() {
        return menubun;
    }

    /**
     * Sets the bundle attribute of the IJ class
     *
     * @param table The new bundle value
     */
    public static void setBundle(ResourceBundle table) {
        menubun = table;
    }

    /**
     * Gets the pluginBundle attribute of the IJ class
     *
     * @return The pluginBundle value
     */
    public static ResourceBundle getPluginBundle() {
        return plgbun;
    }

    /**
     * Sets the pluginBundle attribute of the IJ class
     *
     * @param table The new pluginBundle value
     */
    public static void setPluginBundle(ResourceBundle table) {
        plgbun = table;
    }

    /**
     * Gets the opBundle attribute of the IJ class
     *
     * @return The opBundle value
     */
    public static ResourceBundle getOpBundle() {
        return blitbun;
    }

    /**
     * Sets the opBundle attribute of the IJ class
     *
     * @param table The new opBundle value
     */
    public static void setOpBundle(ResourceBundle table) {
        blitbun = table;
    }

    /**
     * Gets the colorBundle attribute of the IJ class
     *
     * @return The colorBundle value
     */
    public static ResourceBundle getColorBundle() {
        return colorbun;
    }

    /**
     * Sets the colorBundle attribute of the IJ class
     *
     * @param table The new colorBundle value
     */
    public static void setColorBundle(ResourceBundle table) {
        colorbun = table;
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    public static int execLevel() {
        return ExecLevel;
    }

    /**
     * Sets the execLevel attribute of the IJ class
     *
     * @param level The new execLevel value
     */
    static void setExecLevel(int level) {
        if ((level != LEVEL_EASY) || (level != LEVEL_TOUGH)
                || (level != LEVEL_TEACH) || (level != LEVEL_DEVEL)) {
            ExecLevel = LEVEL_INTER;
        } else {
            ExecLevel = level;
        }
    }


    /*
     * EU_HOU END
     */
    /**
     * Returns a reference to the "ImageJ" frame.
     *
     * @return The instance value
     */
    public static ImageJ getInstance() {
        return ij;
    }

    /**
     * Runs the macro contained in the string
     * <code>macro</code>. Returns any string value returned by the macro, or
     * null. The equivalent macro function is eval().
     *
     * @param macro Description of the Parameter
     * @return Description of the Return Value
     */
    public static String runMacro(String macro) {
        return runMacro(macro, "");
    }

    /**
     * Runs the macro contained in the string
     * <code>macro</code>. The optional string argument can be retrieved in the
     * called macro using the getArgument() macro function. Returns any string
     * value returned by the macro, or null.
     *
     * @param macro Description of the Parameter
     * @param arg Description of the Parameter
     * @return Description of the Return Value
     */
    public static String runMacro(String macro, String arg) {
        Macro_Runner mr = new Macro_Runner();

        return mr.runMacro(macro, arg);
    }

    /**
     * Runs the specified macro file. The file is assumed to be in the macros
     * folder unless
     * <code>name</code> is a full path. ".txt" is added if
     * <code>name</code> does not have an extension. The optional string
     * argument (
     * <code>arg</code>) can be retrieved in the called macro using the
     * getArgument() macro function. Returns any string value returned by the
     * macro or null. The equivalent macro function is runMacro().
     *
     * @param name Description of the Parameter
     * @param arg Description of the Parameter
     * @return Description of the Return Value
     */
    public static String runMacroFile(String name, String arg) {
        if (ij == null && Menus.getCommands() == null) {
            init();
        }
        Macro_Runner mr = new Macro_Runner();

        return mr.runMacroFile(name, arg);
    }

    /**
     * Runs the specified macro file.
     *
     * @param name Description of the Parameter
     * @return Description of the Return Value
     */
    public static String runMacroFile(String name) {
        return runMacroFile(name, null);
    }

    /**
     * Description of the Method
     *
     * @param className Description of the Parameter
     * @return Description of the Return Value
     */
    //EU_HOU Add
    public static Object runPlugIn(String className) {
        return runPlugIn(className, "");
    }

    /**
     * Runs the specified plugin and returns a reference to it.
     *
     * @param className Description of the Parameter
     * @param arg Description of the Parameter
     * @return Description of the Return Value
     */
    public static Object runPlugIn(String className, String arg) {
        return runPlugIn("", className, arg);
    }

    /**
     * Runs the specified plugin and returns a reference to it.
     *
     * @param commandName Description of the Parameter
     * @param className Description of the Parameter
     * @param arg Description of the Parameter
     * @return Description of the Return Value
     */
    static Object runPlugIn(String commandName, String className, String arg) {
        if (IJ.debugMode) {
            //EU_HOU Bundle
            IJ.log("runPlugin: " + className + " " + commandName + " " + arg);
        }
        // Use custom classloader if this is a user plugin
        // and we are not running as an applet
        if (!className.startsWith("ij") && applet == null) {
            boolean createNewClassLoader = altKeyDown();

            return runUserPlugIn(commandName, className, arg, createNewClassLoader);
        }
        Object thePlugIn = null;

        try {
            Class c = Class.forName(className);

            thePlugIn = c.newInstance();
            if (thePlugIn instanceof PlugIn) {
                ((PlugIn) thePlugIn).run(arg);
            } else {
                new PlugInFilterRunner(thePlugIn, commandName, arg);
            }
        } catch (ClassNotFoundException e) {
            if (IJ.getApplet() == null) {
                //EU_HOU Bundle
                log("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            }
        } catch (InstantiationException e) {
            //EU_HOU Bundle
            log("Unable to load plugin (ins)");
        } catch (IllegalAccessException e) {
            //EU_HOU Bundle
            log("Unable to load plugin, possibly \nbecause it is not public.");
        }
        redirectErrorMessages = false;
        return thePlugIn;
    }

    /**
     * Description of the Method
     *
     * @param commandName Description of the Parameter
     * @param className Description of the Parameter
     * @param arg Description of the Parameter
     * @param createNewLoader Description of the Parameter
     * @return Description of the Return Value
     */
    static Object runUserPlugIn(String commandName, String className, String arg, boolean createNewLoader) {
        ImagePlus imp;

        /*
         * EU_HOU CHANGES
         */
        //EU_HOU Bundle
        if (commandName.equals(IJ.getBundle().getString("Print")) || commandName.equals(IJ.getBundle().getString("Clear")) || commandName.equals(IJ.getBundle().getString("ClearOutside")) || commandName.equals(IJ.getBundle().getString("Fill")) || commandName.equals(IJ.getBundle().getString("Draw")) || commandName.equals(IJ.getBundle().getString("Crop")) || commandName.equals(IJ.getBundle().getString("Duplicate")) || commandName.equals(IJ.getBundle().getString("Rename")) || commandName.equals(IJ.getBundle().getString("Scale")) || commandName.equals(Prefs.getCommand("saveas01")) || commandName.equals(Prefs.getCommand("saveas02")) || commandName.equals(Prefs.getCommand("saveas03")) || commandName.equals(Prefs.getCommand("saveas04")) || commandName.equals(Prefs.getCommand("saveas05")) || commandName.equals(Prefs.getCommand("saveas06")) || commandName.equals(Prefs.getCommand("saveas07")) || commandName.equals(Prefs.getCommand("saveas08")) || commandName.equals(Prefs.getCommand("saveas09")) || commandName.equals(Prefs.getCommand("saveas10")) || commandName.equals(Prefs.getCommand("saveas11")) || commandName.equals(Prefs.getCommand("saveas12")) || commandName.equals(Prefs.getCommand("saveas13")) || commandName.equals(Prefs.getCommand("saveas14")) || commandName.equals(Prefs.getCommand("saveas15"))) {
            Object win = WindowManager.getFrontWindow();

            if (win instanceof ImageWindow) {
                imp = ((ImageWindow) win).getImagePlus();
            } else {
                imp = WindowManager.getCurrentImage();
            }
        } else {
            imp = WindowManager.getCurrentImage();
        }
        /*
         * EU_HOU END
         */
        if (applet != null) {
            return null;
        }
        if (notVerified) {
            // check for duplicate classes in the plugins folder
            IJ.runPlugIn("ij.plugin.ClassChecker", "");
            notVerified = false;
        }
        if (createNewLoader) {
            classLoader = null;
        }
        ClassLoader loader = getClassLoader();
        Object thePlugIn = null;

        try {
            thePlugIn = (loader.loadClass(className)).newInstance();
            if (thePlugIn instanceof PlugIn) {
                ((PlugIn) thePlugIn).run(arg);
            } else if (thePlugIn instanceof PlugInFilter) {
                new PlugInFilterRunner(thePlugIn, commandName, arg);
            }
        } catch (ClassNotFoundException e) {
            if (className.indexOf('_') != -1 && !suppressPluginNotFoundError) {
                //EU_HOU Bundle
                error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            }
        } catch (NoClassDefFoundError e) {
            int dotIndex = className.indexOf('.');

            if (dotIndex >= 0) {
                return runUserPlugIn(commandName, className.substring(dotIndex + 1), arg, createNewLoader);
            }
            if (className.indexOf('_') != -1 && !suppressPluginNotFoundError) {
                //EU_HOU Bundle
                error("Plugin or class not found: \"" + className + "\"\n(" + e + ")");
            }
        } catch (InstantiationException e) {
            //EU_HOU Bundle
            error("Unable to load plugin (ins)");
        } catch (IllegalAccessException e) {
            //EU_HOU Bundle
            error("Unable to load plugin, possibly \nbecause it is not public.");
        }
        redirectErrorMessages = false;
        suppressPluginNotFoundError = false;
        return thePlugIn;
    }

    /**
     * Description of the Method
     *
     * @param capabilities Description of the Parameter
     * @param cmd Description of the Parameter
     */
    static void wrongType(int capabilities, String cmd) {
        //EU_HOU Bundle
        String s = menubun.getString("TypeReqErr") + "\n \n  ";

        if ((capabilities & PlugInFilter.DOES_8G) != 0) {
            //EU_HOU Bundle
            s += menubun.getString("8-bit") + " " + menubun.getString("Grayscale") + "\n";
        }
        if ((capabilities & PlugInFilter.DOES_8C) != 0) {
            //EU_HOU Bundle
            s += menubun.getString("8-bitColor") + "\n";
        }
        if ((capabilities & PlugInFilter.DOES_16) != 0) {
            //EU_HOU Bundle
            s += menubun.getString("16-bit") + " " + menubun.getString("Grayscale") + "\n";
        }
        if ((capabilities & PlugInFilter.DOES_32) != 0) {
            //EU_HOU Bundle
            s += menubun.getString("32-bit") + " " + menubun.getString("Grayscale") + "\n";
        }
        if ((capabilities & PlugInFilter.DOES_RGB) != 0) {
            //EU_HOU Bundle
            s += menubun.getString("RGBColor") + "\n";
        }
        IJ.error(s);
    }

    /**
     * Starts executing a menu command in a separete thread and returns
     * immediately.
     *
     * @param command Description of the Parameter
     */
    public static void doCommand(String command) {
        if (ij != null) {
            ij.doCommand(command);
        }
    }

    /**
     * Runs an ImageJ command. Does not return until the command has finished
     * executing. To avoid "image locked", errors, plugins that call this method
     * should implement the PlugIn interface instead of PlugInFilter.
     *
     * @param command Description of the Parameter
     */
    public static void run(String command) {
        run(command, null);
    }

    /**
     * Runs an ImageJ command, with options that are passed to the GenericDialog
     * and OpenDialog classes. Does not return until the command has finished
     * executing.
     *
     * @param command Description of the Parameter
     * @param options Description of the Parameter
     */
    public static void run(String command, String options) {
        //IJ.log("run: "+command+" "+Thread.currentThread().getName());
        if (ij == null && Menus.getCommands() == null) {
            init();
        }
        Macro.abort = false;
        Macro.setOptions(options);

        Thread thread = Thread.currentThread();

        if (previousThread == null || thread != previousThread) {
            String name = thread.getName();

            if (!name.startsWith("Run$_")) {
                thread.setName("Run$_" + name);
            }
        }

        if (command.equals("New...")) {
            command = "Image...";
        } else if (command.equals("Threshold")) {
            command = "Make Binary";
        } else if (command.equals("Display...")) {
            command = "Appearance...";
        } else if (command.equals("Start Animation")) {
            command = "Start Animation [\\]";
        } else if (command.equals("Convert Images to Stack")) {
            command = "Images to Stack";
        } else if (command.equals("Convert Stack to Images")) {
            command = "Stack to Images";
        }

        previousThread = thread;
        macroRunning = true;

        Executer e = new Executer(command);

        e.run();
        macroRunning = false;
        Macro.setOptions(null);
        testAbort();
    }

    /**
     * Description of the Method
     */
    static void init() {
        Menus m = new Menus(null, null);

        Prefs.load(m, null);
        m.addMenuBar();
    }

    /**
     * A unit test for JUnit
     */
    private static void testAbort() {
        if (Macro.abort) {
            abort();
        }
    }

    /**
     * Returns true if either of the IJ.run() methods is executing.
     *
     * @return Description of the Return Value
     */
    public static boolean macroRunning() {
        return macroRunning;
    }

    /**
     * Returns the Applet that created this ImageJ or null if running as an
     * application.
     *
     * @return The applet value
     */
    public static java.applet.Applet getApplet() {
        return applet;
    }

    /**
     * Displays a message in the ImageJ status bar.
     *
     * @param s Description of the Parameter
     */
    public static void showStatus(String s) {
        if (ij != null) {
            ij.showStatus(s);
        }
        ImagePlus imp = WindowManager.getCurrentImage();
        ImageCanvas ic = imp != null ? imp.getCanvas() : null;

        if (ic != null) {
            ic.setShowCursorStatus(s.length() == 0 ? true : false);
        }
    }

    /**
     * Displays a line of text in the "Results" window. Writes to
     * System.out.println if the "ImageJ" frame is not present.
     *
     * @param s Description of the Parameter
     */
    public static void write(String s) {
        if (textPanel == null && ij != null) {
            showResults();
        }
        if (textPanel != null) {
            textPanel.append(s);
        } else {
            System.out.println(s);
        }
    }

    /**
     * Description of the Method
     */
    public static void showResults() {
        //EU_HOU Bundle
        TextWindow resultsWindow = new TextWindow("Results", "", 300, 200);

        textPanel = resultsWindow.getTextPanel();
        textPanel.setResultsTable(Analyzer.getResultsTable());
        if (ij != null) {
            textPanel.addKeyListener(ij);
        }
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    public static synchronized void log(String s) {
        System.out.println("IJ.log(" + s + "), logPanel=" + logPanel);
        if (s == null) {
            return;
        }
        if (logPanel == null && ij != null) {
            System.out.println("IJ.log 1, logPanel=" + logPanel);
            //EU_HOU Bundle
            TextWindow logWindow = new TextWindow("Log", "", 350, 250);
            System.out.println("IJ.log 1 2, logPanel=" + logPanel);
            logPanel = logWindow.getTextPanel();
            System.out.println("IJ.log 1 3, logPanel=" + logPanel);
            logPanel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            System.out.println("IJ.log 1 4, logPanel=" + logPanel);
        }
        if (logPanel != null) {
            if (s.startsWith("\\")) {
                System.out.println("IJ.log 2, logPanel=" + logPanel);
                handleLogCommand(s);
                System.out.println("IJ.log 2 2, logPanel=" + logPanel);
            } else {
                System.out.println("IJ.log 3, logPanel=" + logPanel);
                logPanel.append(s);
            }
        } else {
            System.out.println("IJ.log 4, logPanel=" + logPanel);
            System.out.println(s);
        }
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     */
    static void handleLogCommand(String s) {
        System.out.println("IJ.handleLogCommand 1, logPanel=" + logPanel);
        if (s.equals("\\Closed")) {
            System.out.println("IJ.handleLogCommand 2, logPanel=" + logPanel);
            logPanel = null;
        } else if (s.startsWith("\\Update:")) {
            System.out.println("IJ.handleLogCommand 3, logPanel=" + logPanel);
            int n = logPanel.getLineCount();
            String s2 = s.substring(8, s.length());

            if (n == 0) {
                System.out.println("IJ.handleLogCommand 4, logPanel=" + logPanel);
                logPanel.append(s2);
            } else {
                System.out.println("IJ.handleLogCommand 5, logPanel=" + logPanel);
                logPanel.setLine(n - 1, s2);
            }
        } else if (s.startsWith("\\Update")) {
            System.out.println("IJ.handleLogCommand 6, logPanel=" + logPanel);
            int cindex = s.indexOf(":");

            if (cindex == -1) {
                System.out.println("IJ.handleLogCommand 7, logPanel=" + logPanel);
                logPanel.append(s);
                return;
            }
            String nstr = s.substring(7, cindex);
            int line = (int) Tools.parseDouble(nstr, -1);

            if (line < 0 || line > 25) {
                System.out.println("IJ.handleLogCommand 8, logPanel=" + logPanel);
                logPanel.append(s);
                return;
            }
            int count = logPanel.getLineCount();

            while (line >= count) {
                log("");
                count++;
            }

            String s2 = s.substring(cindex + 1, s.length());

            logPanel.setLine(line, s2);
        } else if (s.equals("\\Clear")) {
            System.out.println("IJ.handleLogCommand 9, logPanel=" + logPanel);
            logPanel.clear();
        } else {
            System.out.println("IJ.handleLogCommand 10, logPanel=" + logPanel);
            logPanel.append(s);
        }
    }

    /**
     * Clears the "Results" window and sets the column headings to those in the
     * tab-delimited 'headings' String. Writes to System.out.println if the
     * "ImageJ" frame is not present.
     *
     * @param headings The new columnHeadings value
     */
    public static void setColumnHeadings(String headings) {
        if (textPanel == null && ij != null) {
            showResults();
        }
        if (textPanel != null) {
            textPanel.setColumnHeadings(headings);
        } else {
            System.out.println(headings);
        }
    }

    /**
     * Returns true if the "Results" window is open.
     *
     * @return The resultsWindow value
     */
    public static boolean isResultsWindow() {
        return textPanel != null;
    }

    /**
     * Returns a reference to the "Results" window TextPanel. Opens the
     * "Results" window if it is currently not open.
     *
     * @return The textPanel value
     */
    public static TextPanel getTextPanel() {
        if (textPanel == null) {
            showResults();
        }
        return textPanel;
    }

    /**
     * TextWindow calls this method with a null argument when the "Results"
     * window is closed.
     *
     * @param tp The new textPanel value
     */
    public static void setTextPanel(TextPanel tp) {
        textPanel = tp;
    }

    /**
     * Displays a "no images are open" dialog box.
     */
    public static void noImage() {
        //EU_HOU Bundle
        error(IJ.getBundle().getString("ErrorTitle"), IJ.getBundle().getString("NoImgErr"));

    }

    /**
     * Displays an "out of memory" message to the "Log" window.
     *
     * @param name Description of the Parameter
     */
    public static void outOfMemory(String name) {
        Undo.reset();
        System.gc();

        String tot = Runtime.getRuntime().totalMemory() / 1048576L + "MB";

        if (!memMessageDisplayed) {
            log(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }
        //EU_HOU Bundle
        log("<Out of memory>");
        if (!memMessageDisplayed) {
            //EU_HOU Bundle
            log("<All available memory (" + tot + ") has been>");
            log("<used. Instructions for making more>");
            log("<available can be found in the \"Memory\" >");
            log("<sections of the installation notes at>");
            log("<http://rsb.info.nih.gov/ij/docs/install/>");
            log(">>>>>>>>>>>>>>>>>>>>>>>>>>>");
            memMessageDisplayed = true;
        }
        Macro.abort();
    }

    /**
     * Updates the progress bar, where 0<=progress<=1.0. The progress bar is not
     * shown in BatchMode and erased if progress>=1.0. The progress bar is
     * updated only if more than 90 ms have passes since the last call. Does
     * nothing if the ImageJ window is not present.
     *
     * @param progress Description of the Parameter
     */
    public static void showProgress(double progress) {
        if (progressBar != null) {
            progressBar.show(progress, false);
        }
    }

    /**
     * Updates the progress bar, where the length of the bar is set to (
     * <code>currentValue+1)/finalValue</code> of the maximum bar length. The
     * bar is erased if
     * <code>currentValue&gt;=finalValue</code> . The bar is updated only if
     * more than 90 ms have passed since the last call. Does nothing if the
     * ImageJ window is not present.
     *
     * @param currentIndex Description of the Parameter
     * @param finalIndex Description of the Parameter
     */
    public static void showProgress(int currentIndex, int finalIndex) {
        if (progressBar != null) {
            progressBar.show(currentIndex, finalIndex);
        }
    }

    /**
     * Displays a message in a dialog box titled "Message". Writes the Java
     * console if ImageJ is not present.
     *
     * @param msg Description of the Parameter
     */
    public static void showMessage(String msg) {
        showMessage("Message", msg);
    }

    /**
     * Displays a message in a dialog box with the specified title. Writes the
     * Java console if ImageJ is not present.
     *
     * @param title Description of the Parameter
     * @param msg Description of the Parameter
     */
    public static void showMessage(String title, String msg) {
        if (redirectErrorMessages) {
            IJ.log(title + ": " + msg);
            redirectErrorMessages = false;
            return;
        }
        if (ij != null) {
            if (msg != null && msg.startsWith("<html>")) {
                new HTMLDialog(title, msg);
            } else {
                new MessageDialog(ij, title, msg);
            }
        } else {
            System.out.println(msg);
        }
    }

    /**
     * Displays a message in a dialog box titled "ImageJ". If a macro is
     * running, it is aborted. Writes to the Java console if the ImageJ window
     * is not present.
     *
     * @param msg Description of the Parameter
     */
    public static void error(String msg) {
        showMessage("SalsaJ", msg);
        Macro.abort();
    }

    /**
     * Displays a message in a dialog box with the specified title. If a macro
     * is running, it is aborted. Writes to the Java console if ImageJ is not
     * present.
     *
     * @param title Description of the Parameter
     * @param msg Description of the Parameter
     */
    public static synchronized void error(String title, String msg) {
        showMessage(title, msg);
        Macro.abort();
    }

    /**
     * Displays a message in a dialog box with the specified title. Returns
     * false if the user pressed "Cancel".
     *
     * @param title Description of the Parameter
     * @param msg Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean showMessageWithCancel(String title, String msg) {
        GenericDialog gd = new GenericDialog(title);

        gd.addMessage(msg);
        gd.showDialog();
        return !gd.wasCanceled();
    }
    /**
     * Description of the Field
     */
    public final static int CANCELED = Integer.MIN_VALUE;

    /**
     * Allows the user to enter a number in a dialog box. Returns the value
     * IJ.CANCELED (-2,147,483,648) if the user cancels the dialog box. Returns
     * 'defaultValue' if the user enters an invalid number.
     *
     * @param prompt Description of the Parameter
     * @param defaultValue Description of the Parameter
     * @return The number value
     */
    public static double getNumber(String prompt, double defaultValue) {
        GenericDialog gd = new GenericDialog("");
        int decimalPlaces = (int) defaultValue == defaultValue ? 0 : 2;

        gd.addNumericField(prompt, defaultValue, decimalPlaces);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return CANCELED;
        }
        double v = gd.getNextNumber();

        if (gd.invalidNumber()) {
            return defaultValue;
        } else {
            return v;
        }
    }

    /**
     * Allows the user to enter a string in a dialog box. Returns "" if the user
     * cancels the dialog box.
     *
     * @param prompt Description of the Parameter
     * @param defaultString Description of the Parameter
     * @return The string value
     */
    public static String getString(String prompt, String defaultString) {
        GenericDialog gd = new GenericDialog("");

        gd.addStringField(prompt, defaultString, 20);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return "";
        }
        return gd.getNextString();
    }

    /**
     * Delays 'msecs' milliseconds.
     *
     * @param msecs Description of the Parameter
     */
    public static void wait(int msecs) {
        try {
            Thread.sleep(msecs);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Emits an audio beep.
     */
    public static void beep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Runs the garbage collector and returns a string something like "64K of
     * 256MB (25%)" that shows how much of the available memory is in use. This
     * is the string displayed when the user clicks in the status bar.
     *
     * @return Description of the Return Value
     */
    public static String freeMemory() {
        System.gc();

        long inUse = currentMemory();
        String inUseStr = inUse < 10000 * 1024 ? inUse / 1024L + "K" : inUse / 1048576L + "MB";
        String maxStr = "";
        long max = maxMemory();

        if (max > 0L) {
            double percent = inUse * 100 / max;

            maxStr = " of " + max / 1048576L + "MB (" + (percent < 1.0 ? "<1" : d2s(percent, 0)) + "%)";
        }
        return inUseStr + maxStr;
    }

    /**
     * Returns the amount of memory currently being used by ImageJ.
     *
     * @return Description of the Return Value
     */
    public static long currentMemory() {
        long freeMem = Runtime.getRuntime().freeMemory();
        long totMem = Runtime.getRuntime().totalMemory();

        return totMem - freeMem;
    }

    /**
     * Returns the maximum amount of memory available to ImageJ or zero if
     * ImageJ is unable to determine this limit.
     *
     * @return Description of the Return Value
     */
    public static long maxMemory() {
        if (maxMemory == 0L) {
            Memory mem = new Memory();

            maxMemory = mem.getMemorySetting();
            if (maxMemory == 0L) {
                maxMemory = mem.maxMemory();
            }
        }
        return maxMemory;
    }

    /**
     * Description of the Method
     *
     * @param imp Description of the Parameter
     * @param start Description of the Parameter
     * @param str Description of the Parameter
     */
    public static void showTime(ImagePlus imp, long start, String str) {
        showTime(imp, start, str, 1);
    }

    /**
     * Description of the Method
     *
     * @param imp Description of the Parameter
     * @param start Description of the Parameter
     * @param str Description of the Parameter
     * @param nslices Description of the Parameter
     */
    public static void showTime(ImagePlus imp, long start, String str, int nslices) {
        if (Interpreter.isBatchMode()) {
            return;
        }
        long elapsedTime = System.currentTimeMillis() - start;
        double seconds = elapsedTime / 1000.0;
        long pixels = imp.getWidth() * imp.getHeight();
        int rate = (int) ((double) pixels * nslices / seconds);
        String str2;

        if (rate > 1000000000) {
            str2 = "";
        } else if (rate < 1000000) {
            str2 = ", " + rate + " pixels/second";
        } else {
            str2 = ", " + d2s(rate / 1000000.0, 1) + " million pixels/second";
        }
        showStatus(str + seconds + " seconds" + str2);
    }

    /**
     * Converts a number to a formatted string using 2 digits to the right of
     * the decimal point.
     *
     * @param n Description of the Parameter
     * @return Description of the Return Value
     */
    public static String d2s(double n) {
        return d2s(n, 2);
    }
    private static DecimalFormat df =
            new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    private static int dfDigits = 2;

    /**
     * Converts a number to a rounded formatted string. The 'decimalPlaces'
     * argument specifies the number of digits to the right of the decimal point
     * (0-9).
     *
     * @param n Description of the Parameter
     * @param decimalPlaces Description of the Parameter
     * @return Description of the Return Value
     */
    public static String d2s(double n, int decimalPlaces) {
        if (Double.isNaN(n)) {
            return "NaN";
        }
        if (n == Float.MAX_VALUE) {// divide by 0 in FloatProcessor
            return "3.4e38";
        }
        double np = n;
        boolean negative = n < 0.0;

        if (negative) {
            np = -n;
        }
        if ((np < 0.001 && np != 0.0 && np < 1.0 / Math.pow(10, decimalPlaces)) || np > 999999999999d) {
            return Float.toString((float) n);
        }// use scientific notation
        double whole = Math.round(np * Math.pow(10, decimalPlaces));
        double rounded = whole / Math.pow(10, decimalPlaces);

        if (negative) {
            rounded = -rounded;
        }
        if (decimalPlaces < 0) {
            decimalPlaces = 0;
        }
        if (decimalPlaces > 9) {
            decimalPlaces = 9;
        }
        if (decimalPlaces != dfDigits) {
            switch (decimalPlaces) {
                case 0:
                    df.applyPattern("0");
                    dfDigits = 0;
                    break;
                case 1:
                    df.applyPattern("0.0");
                    dfDigits = 1;
                    break;
                case 2:
                    df.applyPattern("0.00");
                    dfDigits = 2;
                    break;
                case 3:
                    df.applyPattern("0.000");
                    dfDigits = 3;
                    break;
                case 4:
                    df.applyPattern("0.0000");
                    dfDigits = 4;
                    break;
                case 5:
                    df.applyPattern("0.00000");
                    dfDigits = 5;
                    break;
                case 6:
                    df.applyPattern("0.000000");
                    dfDigits = 6;
                    break;
                case 7:
                    df.applyPattern("0.0000000");
                    dfDigits = 7;
                    break;
                case 8:
                    df.applyPattern("0.00000000");
                    dfDigits = 8;
                    break;
                case 9:
                    df.applyPattern("0.000000000");
                    dfDigits = 9;
                    break;
            }
        }
        String s = df.format(rounded);
        //if (s.length()>12) s = Float.toString((float)n); // use scientific notation
        return s;
    }

    /**
     * Adds the specified class to a Vector to keep it from being garbage
     * collected, which would cause the classes static fields to be reset.
     * Probably not needed with Java 1.2 or later.
     *
     * @param c Description of the Parameter
     */
    public static void register(Class c) {
        if (ij != null) {
            ij.register(c);
        }
    }

    /**
     * Returns true if the space bar is down.
     *
     * @return Description of the Return Value
     */
    public static boolean spaceBarDown() {
        return spaceDown;
    }

    /**
     * Returns true if the alt key is down.
     *
     * @return Description of the Return Value
     */
    public static boolean altKeyDown() {
        return altDown;
    }

    /**
     * Returns true if the shift key is down.
     *
     * @return Description of the Return Value
     */
    public static boolean shiftKeyDown() {
        return shiftDown;
    }

    /**
     * Sets the keyDown attribute of the IJ class
     *
     * @param key The new keyDown value
     */
    public static void setKeyDown(int key) {
        //IJ.showStatus("setKeyDown: "+key);
        switch (key) {
            case KeyEvent.VK_ALT:
                altDown = true;
                break;
            case KeyEvent.VK_SHIFT:
                shiftDown = true;
                if (debugMode) {
                    beep();
                }
                break;
            case KeyEvent.VK_SPACE: {
                spaceDown = true;

                ImageWindow win = WindowManager.getCurrentWindow();

                if (win != null) {
                    win.getCanvas().setCursor(-1, -1, -1, -1);
                }
                break;
            }
            case KeyEvent.VK_ESCAPE: {
                escapePressed = true;
                break;
            }
        }
    }

    /**
     * Sets the keyUp attribute of the IJ class
     *
     * @param key The new keyUp value
     */
    public static void setKeyUp(int key) {
        //IJ.showStatus("setKeyUp: "+key);
        switch (key) {
            case KeyEvent.VK_ALT:
                altDown = false;
                break;
            case KeyEvent.VK_SHIFT:
                shiftDown = false;
                if (debugMode) {
                    beep();
                }
                break;
            case KeyEvent.VK_SPACE: {
                spaceDown = false;

                ImageWindow win = WindowManager.getCurrentWindow();

                if (win != null) {
                    win.getCanvas().setCursor(-1, -1, -1, -1);
                }
                break;
            }
            case ALL_KEYS:
                altDown = shiftDown = spaceDown = false;
                break;
        }
    }

    /**
     * Sets the inputEvent attribute of the IJ class
     *
     * @param e The new inputEvent value
     */
    public static void setInputEvent(InputEvent e) {
        altDown = e.isAltDown();
        shiftDown = e.isShiftDown();
    }

    /**
     * Returns true if this machine is a Macintosh.
     *
     * @return The macintosh value
     */
    public static boolean isMacintosh() {
        return isMac;
    }

    /**
     * Returns true if this machine is a Macintosh running OS X.
     *
     * @return The macOSX value
     */
    public static boolean isMacOSX() {
        return isMacintosh();
    }

    /**
     * Returns true if this machine is running Windows.
     *
     * @return The windows value
     */
    public static boolean isWindows() {
        return isWin;
    }

    /**
     * Returns true if a macro is running, or if the run(), open() or newImage()
     * method is executing.
     */
    public static boolean isMacro() {
        return macroRunning || Interpreter.getInstance() != null;
    }

    /**
     * Always returns true.
     *
     * @return The java2 value
     */
    public static boolean isJava2() {
        return isJava2;
    }

    /**
     * Returns true if ImageJ is running on a Java 1.4 or greater JVM.
     *
     * @return The java14 value
     */
    public static boolean isJava14() {
        return isJava14;
    }

    /**
     * Returns true if ImageJ is running on a Java 1.5 or greater JVM.
     *
     * @return The java15 value
     */
    public static boolean isJava15() {
        return isJava15;
    }

    /**
     * Returns true if ImageJ is running on a Java 1.6 or greater JVM.
     *
     * @return The java16 value
     */
    public static boolean isJava16() {
        return isJava16;
    }

    /**
     * Returns true if ImageJ is running on Linux.
     *
     * @return The linux value
     */
    public static boolean isLinux() {
        return isLinux;
    }

    /**
     * Returns true if ImageJ is running on Windows Vista.
     *
     * @return The vista value
     */
    public static boolean isVista() {
        return isVista;
    }

    /**
     * Displays an error message and returns false if the ImageJ version is less
     * than the one specified.
     *
     * @param version Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean versionLessThan(String version) {
        boolean lessThan = ImageJ.VERSION.compareTo(version) < 0;

        if (lessThan) {
            //EU_HOU Bundle
            error("This plugin or macro requires ImageJ " + version + " or later.");
        }
        return lessThan;
    }

    /**
     * Displays a "Process all slices?" dialog. Returns
     * 'flags'+PlugInFilter.DOES_STACKS if the user selects "Yes", 'flags' if
     * the user selects "No" and PlugInFilter.DONE if the user selects "Cancel".
     *
     * @param imp Description of the Parameter
     * @param flags Description of the Parameter
     * @return Description of the Return Value
     */
    public static int setupDialog(ImagePlus imp, int flags) {
        if (imp == null || (ij != null && ij.hotkey) || hideProcessStackDialog) {
            return flags;
        }
        int stackSize = imp.getStackSize();

        if (stackSize > 1) {
            String macroOptions = Macro.getOptions();

            if (macroOptions != null) {
                if (macroOptions.indexOf("stack ") >= 0) {
                    return flags + PlugInFilter.DOES_STACKS;
                } else {
                    return flags;
                }
            }
            //EU_HOU Bundle
            YesNoCancelDialog d = new YesNoCancelDialog(getInstance(),
                    "Process Stack?", "Process all " + stackSize + " slices?  There is\n"
                    + "no Undo if you select \"Yes\".");

            if (d.cancelPressed()) {
                return PlugInFilter.DONE;
            } else if (d.yesPressed()) {
                if (Recorder.record) {
                    Recorder.recordOption("stack");
                }
                return flags + PlugInFilter.DOES_STACKS;
            }
            if (Recorder.record) {
                Recorder.recordOption("slice");
            }
        }
        return flags;
    }

    /**
     * Creates a rectangular selection. Removes any existing selection if width
     * or height are less than 1.
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param width Description of the Parameter
     * @param height Description of the Parameter
     */
    public static void makeRectangle(int x, int y, int width, int height) {
        if (width <= 0 || height < 0) {
            getImage().killRoi();
        } else {
            ImagePlus img = getImage();

            img.setRoi(x, y, width, height);
        }
    }

    /**
     * Creates an elliptical selection. Removes any existing selection if width
     * or height are less than 1.
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @param width Description of the Parameter
     * @param height Description of the Parameter
     */
    public static void makeOval(int x, int y, int width, int height) {
        if (width <= 0 || height < 0) {
            getImage().killRoi();
        } else {
            ImagePlus img = getImage();

            img.setRoi(new OvalRoi(x, y, width, height));
        }
    }

    /**
     * Creates a straight line selection.
     *
     * @param x1 Description of the Parameter
     * @param y1 Description of the Parameter
     * @param x2 Description of the Parameter
     * @param y2 Description of the Parameter
     */
    public static void makeLine(int x1, int y1, int x2, int y2) {
        getImage().setRoi(new Line(x1, y1, x2, y2));
    }

    /**
     * Creates a straight line selection using double coordinates.
     *
     * @param x1 Description of the Parameter
     * @param y1 Description of the Parameter
     * @param x2 Description of the Parameter
     * @param y2 Description of the Parameter
     */
    public static void makeLine(double x1, double y1, double x2, double y2) {
        getImage().setRoi(new Line(x1, y1, x2, y2));
    }

    /**
     * Sets the minimum and maximum displayed pixel values.
     *
     * @param min The new minAndMax value
     * @param max The new minAndMax value
     */
    public static void setMinAndMax(double min, double max) {
        ImagePlus img = getImage();

        if (img.getBitDepth() == 16) {
            Calibration cal = img.getCalibration();

            min = cal.getRawValue(min);
            max = cal.getRawValue(max);
        }
        img.getProcessor().setMinAndMax(min, max);
        img.updateAndDraw();
    }

    /**
     * Resets the minimum and maximum displayed pixel values to be the same as
     * the min and max pixel values.
     */
    public static void resetMinAndMax() {
        ImagePlus img = getImage();

        img.getProcessor().resetMinAndMax();
        img.updateAndDraw();
    }

    /**
     * Sets the lower and upper threshold levels and displays the image using
     * red to highlight thresholded pixels. May not work correctly on 16 and 32
     * bit images unless the display range has been reset using
     * IJ.resetMinAndMax().
     *
     * @param lowerThreshold The new threshold value
     * @param upperThresold The new threshold value
     */
    public static void setThreshold(double lowerThreshold, double upperThresold) {
        setThreshold(lowerThreshold, upperThresold, null);
    }

    /**
     * Sets the lower and upper threshold levels and displays the image using
     * the specified
     * <code>displayMode</code> ("Red", "Black & White", "Over/Under" or "No
     * Update").
     *
     * @param lowerThreshold The new threshold value
     * @param upperThreshold The new threshold value
     * @param displayMode The new threshold value
     */
    public static void setThreshold(double lowerThreshold, double upperThreshold, String displayMode) {
        int mode = ImageProcessor.RED_LUT;

        if (displayMode != null) {
            displayMode = displayMode.toLowerCase(Locale.US);
            if (displayMode.indexOf("black") != -1) {
                mode = ImageProcessor.BLACK_AND_WHITE_LUT;
            } else if (displayMode.indexOf("over") != -1) {
                mode = ImageProcessor.OVER_UNDER_LUT;
            } else if (displayMode.indexOf("no") != -1) {
                mode = ImageProcessor.NO_LUT_UPDATE;
            }
        }
        ImagePlus img = getImage();

        if (img.getBitDepth() == 16) {
            Calibration cal = img.getCalibration();

            lowerThreshold = cal.getRawValue(lowerThreshold);
            upperThreshold = cal.getRawValue(upperThreshold);
        }
        img.getProcessor().setThreshold(lowerThreshold, upperThreshold, mode);
        if (mode != ImageProcessor.NO_LUT_UPDATE) {
            img.getProcessor().setLutAnimation(true);
            img.updateAndDraw();
        }
    }

    /**
     * Disables thresholding.
     */
    public static void resetThreshold() {
        ImagePlus img = getImage();
        ImageProcessor ip = img.getProcessor();

        ip.resetThreshold();
        ip.setLutAnimation(true);
        img.updateAndDraw();
    }

    /**
     * For IDs less than zero, activates the image with the specified ID. For
     * IDs greater than zero, activates the Nth image.
     *
     * @param id Description of the Parameter
     */
    public static void selectWindow(int id) {
        if (id > 0) {
            id = WindowManager.getNthImageID(id);
        }
        ImagePlus imp = WindowManager.getImage(id);

        if (imp == null) {
            //EU_HOU Bundle
            error("Macro Error", "Image " + id + " not found or no images are open.");
        }
        if (Interpreter.isBatchMode()) {
            ImagePlus imp2 = WindowManager.getCurrentImage();

            if (imp2 != null && imp2 != imp) {
                imp2.saveRoi();
            }
            WindowManager.setTempCurrentImage(imp);
            WindowManager.setWindow(null);
        } else {
            ImageWindow win = imp.getWindow();

            win.toFront();
            WindowManager.setWindow(win);

            long start = System.currentTimeMillis();
            // timeout after 2 seconds unless current thread is event dispatch thread
            String thread = Thread.currentThread().getName();
            int timeout = thread != null && thread.indexOf("EventQueue") != -1 ? 0 : 2000;

            while (true) {
                wait(10);
                imp = WindowManager.getCurrentImage();
                if (imp != null && imp.getID() == id) {
                    return;
                }// specified image is now active
                if ((System.currentTimeMillis() - start) > timeout) {
                    WindowManager.setCurrentWindow(win);
                    return;
                }
            }
        }
    }

    /**
     * Activates the window with the specified title.
     *
     * @param title Description of the Parameter
     */
    public static void selectWindow(String title) {
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < 3000) {// 3 sec timeout
            Frame frame = WindowManager.getFrame(title);

            if (frame != null && !(frame instanceof ImageWindow)) {
                selectWindow(frame);
                return;
            }
            int[] wList = WindowManager.getIDList();
            int len = wList != null ? wList.length : 0;

            for (int i = 0; i < len; i++) {
                ImagePlus imp = WindowManager.getImage(wList[i]);

                if (imp != null) {
                    if (imp.getTitle().equals(title)) {
                        selectWindow(imp.getID());
                        return;
                    }
                }
            }
            wait(10);
        }
        //EU_HOU Bundle
        error("Macro Error", "No window with the title \"" + title + "\" found.");
    }

    /**
     * Description of the Method
     *
     * @param frame Description of the Parameter
     */
    static void selectWindow(Frame frame) {
        frame.toFront();

        long start = System.currentTimeMillis();

        while (true) {
            wait(10);
            if (WindowManager.getFrontWindow() == frame) {
                return;
            }// specified window is now in front
            if ((System.currentTimeMillis() - start) > 1000) {
                WindowManager.setWindow(frame);
                return;// 1 second timeout
            }
        }
    }

    /**
     * Sets the foreground color.
     *
     * @param red The new foregroundColor value
     * @param green The new foregroundColor value
     * @param blue The new foregroundColor value
     */
    public static void setForegroundColor(int red, int green, int blue) {
        setColor(red, green, blue, true);
    }

    /**
     * Sets the background color.
     *
     * @param red The new backgroundColor value
     * @param green The new backgroundColor value
     * @param blue The new backgroundColor value
     */
    public static void setBackgroundColor(int red, int green, int blue) {
        setColor(red, green, blue, false);
    }

    /**
     * Sets the color attribute of the IJ class
     *
     * @param red The new color value
     * @param green The new color value
     * @param blue The new color value
     * @param foreground The new color value
     */
    static void setColor(int red, int green, int blue, boolean foreground) {
        if (red < 0) {
            red = 0;
        }
        if (green < 0) {
            green = 0;
        }
        if (blue < 0) {
            blue = 0;
        }
        if (red > 255) {
            red = 255;
        }
        if (green > 255) {
            green = 255;
        }
        if (blue > 255) {
            blue = 255;
        }
        Color c = new Color(red, green, blue);

        if (foreground) {
            Toolbar.setForegroundColor(c);

            ImagePlus img = WindowManager.getCurrentImage();

            if (img != null) {
                img.getProcessor().setColor(c);
            }
        } else {
            Toolbar.setBackgroundColor(c);
        }
    }

    /**
     * Switches to the specified tool, where id = Toolbar.RECTANGLE (0),
     * Toolbar.OVAL (1), etc.
     *
     * @param id The new tool value
     */
    public static void setTool(int id) {
        Toolbar.getInstance().setTool(id);
    }

    /**
     * Equivalent to clicking on the current image at (x,y) with the wand tool.
     * Returns the number of points in the resulting ROI.
     *
     * @param x Description of the Parameter
     * @param y Description of the Parameter
     * @return Description of the Return Value
     */
    public static int doWand(int x, int y) {
        ImagePlus img = getImage();
        ImageProcessor ip = img.getProcessor();

        if ((img.getType() == ImagePlus.GRAY32) && Double.isNaN(ip.getPixelValue(x, y))) {
            return 0;
        }
        Wand w = new Wand(ip);
        double t1 = ip.getMinThreshold();

        if (t1 == ImageProcessor.NO_THRESHOLD) {
            w.autoOutline(x, y);
        } else {
            w.autoOutline(x, y, t1, ip.getMaxThreshold());
        }
        if (w.npoints > 0) {
            Roi previousRoi = img.getRoi();
            Roi roi = new PolygonRoi(w.xpoints, w.ypoints, w.npoints, Roi.TRACED_ROI);

            img.killRoi();
            img.setRoi(roi);
            // add/subtract this ROI to the previous one if the shift/alt key is down
            if (previousRoi != null) {
                roi.update(shiftKeyDown(), altKeyDown());
            }
        }
        return w.npoints;
    }

    /**
     * Sets the transfer mode used by the <i>Edit/Paste</i> command, where mode
     * is "Copy", "Blend", "Average", "Difference", "Transparent", "AND", "OR",
     * "XOR", "Add", "Subtract", "Multiply", or "Divide".
     *
     * @param mode The new pasteMode value
     */
    public static void setPasteMode(String mode) {
        mode = mode.toLowerCase(Locale.US);

        int m = Blitter.COPY;

        if (mode.startsWith("ble") || mode.startsWith("ave")) {
            m = Blitter.AVERAGE;
        } else if (mode.startsWith("diff")) {
            m = Blitter.DIFFERENCE;
        } else if (mode.startsWith("tran")) {
            m = Blitter.COPY_TRANSPARENT;
        } else if (mode.startsWith("and")) {
            m = Blitter.AND;
        } else if (mode.startsWith("or")) {
            m = Blitter.OR;
        } else if (mode.startsWith("xor")) {
            m = Blitter.XOR;
        } else if (mode.startsWith("sub")) {
            m = Blitter.SUBTRACT;
        } else if (mode.startsWith("add")) {
            m = Blitter.ADD;
        } else if (mode.startsWith("div")) {
            m = Blitter.DIVIDE;
        } else if (mode.startsWith("mul")) {
            m = Blitter.MULTIPLY;
        } else if (mode.startsWith("min")) {
            m = Blitter.MIN;
        } else if (mode.startsWith("max")) {
            m = Blitter.MAX;
        }
        Roi.setPasteMode(m);
    }

    /**
     * Returns a reference to the active image. Displays an error message and
     * aborts the macro if no images are open.
     *
     * @return The image value
     */
    public static ImagePlus getImage() {
        ImagePlus img = WindowManager.getCurrentImage();

        if (img == null) {
            IJ.noImage();
            abort();
        }
        return img;
    }

    /**
     * Switches to the specified stack slice, where 1<='slice'<=stack-size.
     *
     *
     *

     *
     * @param slice The new slice value
     */
    public static void setSlice(int slice) {
        getImage().setSlice(slice);
    }

    /**
     * Returns the ImageJ version number as a string.
     *
     * @return The version value
     */
    public static String getVersion() {
        return ImageJ.VERSION;
    }

    /**
     * Returns the path to the home ("user.home"), startup (ImageJ directory),
     * plugins, macros, temp, current or image directory if
     * <code>title</code> is "home", "startup", "plugins", "macros", "temp",
     * "current" or "image", otherwise, displays a dialog and returns the path
     * to the directory selected by the user. Returns null if the specified
     * directory is not found or the user cancels the dialog box. Also aborts
     * the macro if the user cancels the dialog box.
     *
     * @param title Description of the Parameter
     * @return The directory value
     */
    public static String getDirectory(String title) {
        title = title.toLowerCase();
        if (title.equals("plugins")) {
            return Menus.getPlugInsPath();
        } else if (title.equals("macros")) {
            return Menus.getMacrosPath();
        } else if (title.equals("luts")) {
            return Prefs.getHomeDir() + File.separator + "luts" + File.separator;
        } else if (title.equals("home")) {
            return System.getProperty("user.home") + File.separator;
        } else if (title.equals("startup") || title.equals("imagej")) {
            return Prefs.getHomeDir() + File.separator;
        } else if (title.equals("current")) {
            return OpenDialog.getDefaultDirectory();
        } else if (title.equals("temp")) {
            String dir = System.getProperty("java.io.tmpdir");

            if (dir != null && !dir.endsWith(File.separator)) {
                dir += File.separator;
            }
            return dir;
        } else if (title.equals("image")) {
            ImagePlus imp = WindowManager.getCurrentImage();
            FileInfo fi = imp != null ? imp.getOriginalFileInfo() : null;

            if (fi != null && fi.directory != null) {
                return fi.directory;
            } else {
                return null;
            }
        } else if (title.equals("salsaj")) {
            return getIJDir();
        } else {
            DirectoryChooser dc = new DirectoryChooser(title);
            String dir = dc.getDirectory();

            if (dir == null) {
                Macro.abort();
            } else if (Recorder.record) {
                Recorder.record("getDirectory", dir);
            }
            return dir;
        }
    }

    private static String getIJDir() {
        String path = Menus.getPlugInsPath();
        if (path == null) {
            return null;
        }
        String ijdir = (new File(path)).getParent();
        if (ijdir != null) {
            ijdir += File.separator;
        }
        return ijdir;
    }

    /**
     * Displays a file open dialog box and then opens the tiff, dicom, fits,
     * pgm, jpeg, bmp, gif, lut, roi, or text file selected by the user.
     * Displays an error message if the selected file is not in one of the
     * supported formats, or if it is not found.
     */
    public static void open() {
        open(null);
    }

    /**
     * Opens and displays a tiff, dicom, fits, pgm, jpeg, bmp, gif, lut, roi, or
     * text file. Displays an error message if the specified file is not in one
     * of the supported formats, or if it is not found.
     *
     * @param path Description of the Parameter
     */
    public static void open(String path) {
        if (ij == null && Menus.getCommands() == null) {
            init();
        }
        Opener o = new Opener();

        macroRunning = true;
        if (path == null || path.equals("")) {
            o.open();
        } else {
            o.open(path);
        }
        macroRunning = false;
    }

    /**
     * Opens a text file as a string. Displays a file open dialog if path is
     * null or blank. Returns null if the user cancels the file open dialog. If
     * there is an error, returns a message in the form "Error: message".
     */
    public static String openAsString(String path) {
        if (path == null || path.equals("")) {
            OpenDialog od = new OpenDialog("Open Text File", "");
            String directory = od.getDirectory();
            String name = od.getFileName();
            if (name == null) {
                return null;
            }
            path = directory + name;
        }
        String str = "";
        File file = new File(path);
        if (!file.exists()) {
            return "Error: file not found";
        }
        try {
            StringBuffer sb = new StringBuffer(5000);
            BufferedReader r = new BufferedReader(new FileReader(file));
            while (true) {
                String s = r.readLine();
                if (s == null) {
                    break;
                } else {
                    sb.append(s + "\n");
                }
            }
            r.close();
            str = new String(sb);
        } catch (Exception e) {
            str = "Error: " + e.getMessage();
        }
        return str;
    }

    /**
     * Opens a URL and returns the contents as a string. Returns "<Error:
     * message>" if there an error, including host or file not found.
     */
    public static String openUrlAsString(String url) {
        StringBuffer sb = null;
        url = url.replaceAll(" ", "%20");
        try {
            URL u = new URL(url);
            URLConnection uc = u.openConnection();
            long len = uc.getContentLength();
            if (len > 1048576L) {
                return "<Error: file is larger than 1MB>";
            }
            InputStream in = u.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
        } catch (Exception e) {
            return ("<Error: " + e + ">");
        }
        if (sb != null) {
            return new String(sb);
        } else {
            return "";
        }
    }

    /**
     * Open the specified file as a tiff, bmp, dicom, fits, pgm, gif or jpeg
     * image and returns an ImagePlus object if successful. Calls
     * HandleExtraFileTypes plugin if the file type is not recognised. Note that
     * 'path' can also be a URL.
     *
     * @param path Description of the Parameter
     * @return Description of the Return Value
     */
    public static ImagePlus openImage(String path) {
        return (new Opener()).openImage(path);
    }

    /**
     * Saves an image, lookup table, selection or text window to the specified
     * file path. The path must end in ".tif", ".jpg", ".gif", ".zip", ".raw",
     * ".avi", ".bmp", ".fits", ".pgm", ".png", ".lut", ".roi" or ".txt".
     *
     * @param path Description of the Parameter
     */
    public static void save(String path) {
        int dotLoc = path.lastIndexOf('.');

        if (dotLoc != -1) {
            saveAs(path.substring(dotLoc + 1), path);
        } else {
            //EU_HOU Bundle
            error("The save() macro function requires a file name extension.\n \n" + path);
        }
    }


    /*
     * Saves the active image, lookup table, selection, measurement results,
     * selection XY coordinates or text window to the specified file path. The
     * format argument must be "tiff", "jpeg", "gif", "zip", "raw", "avi",
     * "bmp", "fits", "pgm", "png", "text image", "lut", "selection",
     * "measurements", "xy Coordinates" or "text". If <code>path</code> is null
     * or an emply string, a file save dialog is displayed.
     */
    /**
     * Description of the Method
     *
     * @param format Description of the Parameter
     * @param path Description of the Parameter
     */
    public static void saveAs(String format, String path) {
        if (format == null) {
            return;
        }
        if (path != null && path.length() == 0) {
            path = null;
        }
        format = format.toLowerCase(Locale.US);
        if (format.indexOf("tif") != -1) {
            path = updateExtension(path, ".tif");
            format = "Tiff...";
        } else if (format.indexOf("jpeg") != -1 || format.indexOf("jpg") != -1) {
            path = updateExtension(path, ".jpg");
            format = "Jpeg...";
        } else if (format.indexOf("gif") != -1) {
            path = updateExtension(path, ".gif");
            format = "Gif...";
        } else if (format.indexOf("text image") != -1) {
            path = updateExtension(path, ".txt");
            format = "Text Image...";
        } else if (format.indexOf("text") != -1 || format.indexOf("txt") != -1) {
            if (path != null && !path.endsWith(".xls")) {
                path = updateExtension(path, ".txt");
            }
            format = "Text...";
        } else if (format.indexOf("zip") != -1) {
            path = updateExtension(path, ".zip");
            format = "ZIP...";
        } else if (format.indexOf("raw") != -1) {
            path = updateExtension(path, ".raw");
            format = "Raw Data...";
        } else if (format.indexOf("avi") != -1) {
            path = updateExtension(path, ".avi");
            format = "AVI... ";
        } else if (format.indexOf("bmp") != -1) {
            path = updateExtension(path, ".bmp");
            format = "BMP...";
        } else if (format.indexOf("fits") != -1) {
            path = updateExtension(path, ".fits");
            format = "FITS...";
        } else if (format.indexOf("png") != -1) {
            path = updateExtension(path, ".png");
            format = "PNG...";
        } else if (format.indexOf("pgm") != -1) {
            path = updateExtension(path, ".pgm");
            format = "PGM...";
        } else if (format.indexOf("lut") != -1) {
            path = updateExtension(path, ".lut");
            format = "LUT...";
        } else if (format.indexOf("measurements") != -1) {
            format = "Measurements...";
        } else if (format.indexOf("selection") != -1 || format.indexOf("roi") != -1) {
            path = updateExtension(path, ".roi");
            format = "Selection...";
        } else if (format.indexOf("xy") != -1 || format.indexOf("coordinates") != -1) {
            path = updateExtension(path, ".txt");
            format = "XY Coordinates...";
        } else {
            error("Unrecognized format: " + format);
        }
        if (path == null) {
            run(format);
        } else {
            run(format, "save=[" + path + "]");
        }
    }

    /**
     * Description of the Method
     *
     * @param path Description of the Parameter
     * @param extension Description of the Parameter
     * @return Description of the Return Value
     */
    static String updateExtension(String path, String extension) {
        if (path == null) {
            return null;
        }
        int dotIndex = path.lastIndexOf(".");

        if (dotIndex >= 0) {
            path = path.substring(0, dotIndex) + extension;
        } else {
            path += extension;
        }
        return path;
    }

    /**
     * Creates a new imagePlus.
     * <code>Type</code> should contain "8-bit", "16-bit", "32-bit" or "RGB". In
     * addition, it can contain "white", "black" or "ramp" (the default is
     * "white").
     * <code>Width</code> and
     * <code>height</code> specify the width and height of the image in pixels.
     * <code>Depth</code> specifies the number of stack slices.
     *
     * @param title Description of the Parameter
     * @param type Description of the Parameter
     * @param width Description of the Parameter
     * @param height Description of the Parameter
     * @param depth Description of the Parameter
     * @return Description of the Return Value
     */
    public static ImagePlus createImage(String title, String type, int width, int height, int depth) {
        type = type.toLowerCase(Locale.US);

        int bitDepth = 8;

        if (type.indexOf("16") != -1) {
            bitDepth = 16;
        }
        if (type.indexOf("24") != -1 || type.indexOf("rgb") != -1) {
            bitDepth = 24;
        }
        if (type.indexOf("32") != -1) {
            bitDepth = 32;
        }
        int options = NewImage.FILL_WHITE;

        if (bitDepth == 16 || bitDepth == 32) {
            options = NewImage.FILL_BLACK;
        }
        if (type.indexOf("white") != -1) {
            options = NewImage.FILL_WHITE;
        } else if (type.indexOf("black") != -1) {
            options = NewImage.FILL_BLACK;
        } else if (type.indexOf("ramp") != -1) {
            options = NewImage.FILL_RAMP;
        }
        options += NewImage.CHECK_AVAILABLE_MEMORY;
        return NewImage.createImage(title, width, height, depth, bitDepth, options);
    }

    /**
     * Opens a new image.
     * <code>Type</code> should contain "8-bit", "16-bit", "32-bit" or "RGB". In
     * addition, it can contain "white", "black" or "ramp" (the default is
     * "white").
     * <code>Width</code> and
     * <code>height</code> specify the width and height of the image in pixels.
     * <code>Depth</code> specifies the number of stack slices.
     *
     * @param title Description of the Parameter
     * @param type Description of the Parameter
     * @param width Description of the Parameter
     * @param height Description of the Parameter
     * @param depth Description of the Parameter
     */
    public static void newImage(String title, String type, int width, int height, int depth) {
        ImagePlus imp = createImage(title, type, width, height, depth);

        if (imp != null) {
            macroRunning = true;
            imp.show();
            macroRunning = false;
        }
    }

    /**
     * Returns true if the
     * <code>Esc</code> key was pressed since the last ImageJ command started to
     * execute or since resetEscape() was called.
     *
     * @return Description of the Return Value
     */
    public static boolean escapePressed() {
        return escapePressed;
    }

    /**
     * This method sets the
     * <code>Esc</code> key to the "up" position. The Executer class calls this
     * method when it runs an ImageJ command in a separate thread.
     */
    public static void resetEscape() {
        escapePressed = false;
    }

    /**
     * Causes IJ.error() and IJ.showMessage() output to be temporarily
     * redirected to the "Log" window.
     */
    public static void redirectErrorMessages() {
        redirectErrorMessages = true;
    }

    /**
     * Returns the state of the 'redirectErrorMessages' flag. The
     * File/Import/Image Sequence command sets this flag.
     *
     * @return Description of the Return Value
     */
    public static boolean redirectingErrorMessages() {
        return redirectErrorMessages;
    }

    /**
     * Temporarily suppress "plugin not found" errors.
     */
    public static void suppressPluginNotFoundError() {
        suppressPluginNotFoundError = true;
    }

    /**
     * Returns an instance of the class loader ImageJ uses to run plugins.
     *
     * @return The classLoader value
     */
    public static ClassLoader getClassLoader() {
        if (classLoader == null) {
            String pluginsDir = Menus.getPlugInsPath();

            if (pluginsDir == null) {
                String home = System.getProperty("plugins.dir");

                if (home != null) {
                    if (!home.endsWith(Prefs.separator)) {
                        home += Prefs.separator;
                    }
                    pluginsDir = home + "plugins" + Prefs.separator;
                    if (!(new File(pluginsDir)).isDirectory()) {
                        pluginsDir = home;
                    }
                }
            }
            if (pluginsDir == null) {
                return ClassLoader.getSystemClassLoader();
            } else {
                if (Menus.jnlp) {
                    classLoader = new PluginClassLoader(pluginsDir, true);
                } else {
                    classLoader = new PluginClassLoader(pluginsDir);
                }
            }
        }
        return classLoader;
    }

    /**
     * Returns the size, in pixels, of the primary display.
     *
     * @return The screenSize value
     */
    public static Dimension getScreenSize() {
        if (screenSize == null) {
            if (isWindows()) {// GraphicsEnvironment.getConfigurations is *very* slow on Windows
                screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                return screenSize;
            }
            if (GraphicsEnvironment.isHeadless()) {
                screenSize = new Dimension(0, 0);
            } else {
                // Can't use Toolkit.getScreenSize() on Linux because it returns
                // size of all displays rather than just the primary display.
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice[] gd = ge.getScreenDevices();
                GraphicsConfiguration[] gc = gd[0].getConfigurations();
                Rectangle bounds = gc[0].getBounds();

                if (bounds.x == 0 && bounds.y == 0) {
                    screenSize = new Dimension(bounds.width, bounds.height);
                } else {
                    screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                }
            }
        }
        return screenSize;
    }

    /**
     * Description of the Method
     */
    static void abort() {
        if (ij != null || Interpreter.isBatchMode()) {
            throw new RuntimeException(Macro.MACRO_CANCELED);
        }
    }

    public static void setToolBundle(ResourceBundle bun) {
        toolbun = bun;
    }

    public static ResourceBundle gettToolBundle() {
        return toolbun;
    }
}
