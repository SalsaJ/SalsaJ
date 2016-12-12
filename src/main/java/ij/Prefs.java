//EU_HOU
package ij;
import ij.util.Java2;
import java.io.*;
import java.util.*;
import java.applet.*;
import java.net.URL;
import java.awt.*;
import java.applet.Applet;
import ij.io.*;
import ij.util.Tools;
import ij.gui.*;
import ij.plugin.filter.*;
import ij.process.ImageConverter;
import ij.plugin.Animator;
import ij.process.FloatBlitter;
//EU_HOU
//import ij.plugin.GelAnalyzer;
import ij.plugin.JpegWriter;
import ij.process.ColorProcessor;

/**
 *  This class contains the ImageJ preferences, which are loaded from the
 *  "IJ_Props.txt" and "IJ_Prefs.txt" files.
 *
 *@author     Thomas
 *@created    6 novembre 2007
 *@see        ij.ImageJ
 */
public class Prefs {

	/**
	 *  Description of the Field
	 */
	//EU_HOU Changes
	public final static String PROPS_NAME = "EUHOUProps";
	/**
	 *  Description of the Field
	 */
	//EU_HOU Changes
	public final static String PREFS_NAME = "EUHOUPrefs.txt";
	/**
	 *  Description of the Field
	 */
	public final static String DIR_IMAGE = "dir.image";
	/**
	 *  Description of the Field
	 */
	public final static String FCOLOR = "fcolor";
	/**
	 *  Description of the Field
	 */
	public final static String BCOLOR = "bcolor";
	/**
	 *  Description of the Field
	 */
	public final static String ROICOLOR = "roicolor";
	/**
	 *  Description of the Field
	 */
	public final static String SHOW_ALL_COLOR = "showcolor";
	/**
	 *  Description of the Field
	 */
	public final static String JPEG = "jpeg";
	/**
	 *  Description of the Field
	 */
	public final static String FPS = "fps";
	/**
	 *  Description of the Field
	 */
	public final static String DIV_BY_ZERO_VALUE = "div-by-zero";
	/**
	 *  Description of the Field
	 */
	public final static String NOISE_SD = "noise.sd";
	/**
	 *  Description of the Field
	 */
	public final static String MENU_SIZE = "menu.size";
	/**
	 *  Description of the Field
	 */
	public final static String THREADS = "threads";
	/**
	 *  Description of the Field
	 */
	public final static String KEY_PREFIX = ".";

	private final static int USE_POINTER = 1, ANTIALIASING = 2, INTERPOLATE = 4, ONE_HUNDRED_PERCENT = 8,
			BLACK_BACKGROUND = 16, JFILE_CHOOSER = 32, UNUSED = 64, BLACK_CANVAS = 128, WEIGHTED = 256,
			AUTO_MEASURE = 512, REQUIRE_CONTROL = 1024, USE_INVERTING_LUT = 2048, ANTIALIASED_TOOLS = 4096,
			INTEL_BYTE_ORDER = 8192, DOUBLE_BUFFER = 16384, NO_POINT_LABELS = 32768, NO_BORDER = 65536,
			SHOW_ALL_SLICE_ONLY = 131072;
	/**
	 *  Description of the Field
	 */
	public final static String OPTIONS = "prefs.options";

	/**
	 *  file.separator system property
	 */
	public static String separator = System.getProperty("file.separator");
	/**
	 *  Use pointer cursor instead of cross
	 */
	public static boolean usePointerCursor;
	/**
	 *  No longer used
	 */
	public static boolean antialiasedText;
	/**
	 *  Display images scaled <100% using bilinear interpolation
	 */
	public static boolean interpolateScaledImages;
	/**
	 *  Open images at 100% magnification
	 */
	public static boolean open100Percent;
	/**
	 *  Backgound is black in binary images
	 */
	public static boolean blackBackground;
	/**
	 *  Use JFileChooser instead of FileDialog to open and save files.
	 */
	public static boolean useJFileChooser;
	/**
	 *  Color to grayscale conversion is weighted (0.299, 0.587, 0.114) if the
	 *  variable is true.
	 */
	public static boolean weightedColor;
	/**
	 *  Use black image border.
	 */
	public static boolean blackCanvas;
	/**
	 *  Point tool auto-measure mode.
	 */
	public static boolean pointAutoMeasure;
	/**
	 *  Point tool auto-next slice mode (not saved in IJ_Prefs).
	 */
	public static boolean pointAutoNextSlice;
	/**
	 *  Require control or command key for keybaord shortcuts.
	 */
	public static boolean requireControlKey;
	/**
	 *  Open 8-bit images with inverting LUT so 0 is white and 255 is black.
	 */
	public static boolean useInvertingLut;
	/**
	 *  Draw tool icons using antialiasing.
	 */
	public static boolean antialiasedTools;
	/**
	 *  Export Raw using little-endian byte order.
	 */
	public static boolean intelByteOrder;
	/**
	 *  Double buffer display of selections.
	 */
	public static boolean doubleBuffer;
	/**
	 *  Do not label multiple points created using point tool.
	 */
	public static boolean noPointLabels = true;
	/**
	 *  Disable Edit/Undo command.
	 */
	public static boolean disableUndo;
	/**
	 *  Do not draw black border around image.
	 */
	public static boolean noBorder;
	/**
	 *  Only show ROIs associated with current slice in Roi Manager "Show All"
	 *  mode.
	 */
	public static boolean showAllSliceOnly;

	static Properties ijPrefs = new Properties();
	static Properties props = new Properties(ijPrefs);
	static String prefsDir;
	static String imagesURL;
	static String homeDir;// ImageJ folder
	static int threads;
	//EU_HOU Changes
	static String pluginsDir;


	/**
	 *  Finds and loads the ImageJ configuration file, "IJ_Props.txt".
	 *
	 *@param  ij      Description of the Parameter
	 *@param  applet  Description of the Parameter
	 *@return         an error message if "IJ_Props.txt" not found.
	 */
	public static String load(Object ij, Applet applet) {
		/*
		    EU_HOU CHANGES
		    InputStream f = ij.getClass().getResourceAsStream("/"+PROPS_NAME);
		  */
		Locale lang = ((ImageJ) ij).getLocale();
		ResourceBundle table = null;

		try {
			table = ResourceBundle.getBundle("ij/" + PROPS_NAME, lang);
		}
		catch (Exception e) {
			//EU_HOU Bundle
			IJ.error("PROPS NOT LOADED");
		}
		if (applet != null) {
			/*
			    EU_HOU CHANGES
			    return loadAppletProps(f,applet);
			  */
			return loadAppletProps(table, applet);
		}
		/*
		    END EU_HOU
		  */
		if (homeDir == null) {
			homeDir = System.getProperty("user.dir");
		}
		String userHome = System.getProperty("user.home");
		String osName = System.getProperty("os.name");

		if (osName.indexOf("Windows", 0) > -1) {
			prefsDir = homeDir;//ImageJ folder on Windows
			if (prefsDir.endsWith("Desktop")) {
				prefsDir = userHome;
			}
		}
		else {
			prefsDir = userHome;// Mac Preferences folder or Unix home dir
			if (IJ.isMacOSX()) {
				prefsDir += "/Library/Preferences";
			}
		}
		if (table == null) {
			try {
				table = ResourceBundle.getBundle(homeDir + "/" + PROPS_NAME);
			}
			catch (Exception e) {
				table = null;
			}
		}
		if (table == null) {
			//EU_HOU Bundle
			return PROPS_NAME + " not found in ij.jar or in " + homeDir;
		}
		for (Enumeration e = table.getKeys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String val = table.getString(key);

			props.setProperty(key, val);
		}
		imagesURL = props.getProperty("images.location");
		loadPreferences();
		//History.setNumberOfFilesInHistory(Integer.decode(prefs.getProperty(KEY_NB_FILES_HISTORY, "10")).intValue());
		loadOptions();
		return null;


	}


	/*
	    static void dumpPrefs(String title) {
	    IJ.log("");
	    IJ.log(title);
	    Enumeration e = ijPrefs.keys();
	    while (e.hasMoreElements()) {
	    String key = (String) e.nextElement();
	    IJ.log(key+": "+ijPrefs.getProperty(key));
	    }
	    }
	  */
	/**
	 *  Description of the Method
	 *
	 *@param  f       Description of the Parameter
	 *@param  applet  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	/*
	    EU_HOU CHANGES
	  */
	static String loadAppletProps(ResourceBundle f, Applet applet) {
		if (f == null) {
			//EU_HOU Bundle
			return PROPS_NAME + " not found in SalsaJ.jar";
		}
		for (Enumeration e = f.getKeys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String val = f.getString(key);

			props.setProperty(key, val);
		}

		try {
			URL url = new URL(applet.getDocumentBase(), "images/");

			imagesURL = url.toString();
		}
		catch (Exception e) {}
		return null;
	}


	/*
	    EU_HOU END
	  */
	/**
	 *  Returns the URL of the directory that contains the ImageJ sample images.
	 *
	 *@return    The imagesURL value
	 */
	public static String getImagesURL() {
		return imagesURL;
	}


	/**
	 *  Sets the URL of the directory that contains the ImageJ sample images.
	 *
	 *@param  url  The new imagesURL value
	 */
	public static void setImagesURL(String url) {
		imagesURL = url;
	}


	/**
	 *  Returns the path to the ImageJ directory.
	 *
	 *@return    The homeDir value
	 */
	public static String getHomeDir() {
		return homeDir;
	}


	/**
	 *  Gets the path to the directory where the preferences file (IJPrefs.txt) is
	 *  saved.
	 *
	 *@return    The prefsDir value
	 */
	public static String getPrefsDir() {
		return prefsDir;
	}


	/**
	 *  Sets the path to the ImageJ directory.
	 *
	 *@param  path  The new homeDir value
	 */
	static void setHomeDir(String path) {
		if (path.endsWith(File.separator)) {
			path = path.substring(0, path.length() - 1);
		}
		homeDir = path;
	}


	/**
	 *  Finds an string in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key  Description of the Parameter
	 *@return      The string value
	 */
	public static String getString(String key) {
		//System.out.println("Prefs.getString key=" + key);
		return props.getProperty(key);
	}


	/**
	 *  Finds an string in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key            Description of the Parameter
	 *@param  defaultString  Description of the Parameter
	 *@return                The string value
	 */
	public static String getString(String key, String defaultString) {
		System.out.println("Prefs.getString key=" + key+" "+props);
		if (props == null) {
			return defaultString;
		}
		String s = props.getProperty(key);

		if (s == null) {
			return defaultString;
		}
		else {
			return s;
		}
	}


	/**
	 *  Finds a boolean in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               The boolean value
	 */
	public static boolean getBoolean(String key, boolean defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String s = props.getProperty(key);

		if (s == null) {
			return defaultValue;
		}
		else {
			return s.equals("true");
		}
	}


	/*
	    EU_HOU ADD
	  */
	/**
	 *  Gets the command attribute of the Prefs class
	 *
	 *@param  key  Description of the Parameter
	 *@return      The command value
	 */
	public static String getCommand(String key) {

		String res = getString(key);

		if (res == null) {
			return "null";
		}

		System.out.println("Prefs.getCommand key=" + key);
		System.out.println("Prefs.getCommand res=" + res);

		int ind0 = res.indexOf('"');
		int ind = res.indexOf(',');

		System.out.println("Prefs.getCommand ind0=" + ind0);
		System.out.println("Prefs.getCommand ind=" + ind);
		if (ind == -1) {
			System.out.println("Prefs.getCommand return1=" + (ind0 == -1 ? res : res.substring(1, ind - 1)));
			return (ind0 == -1 ? res : res.substring(1, ind - 1));
		}

		System.out.println("Prefs.getCommand return2=" + (ind0 == -1 ? res.substring(0, ind) : res.substring(1, ind - 1)));
		return (ind0 == -1 ? res.substring(0, ind) : res.substring(1, ind - 1));
	}


	/*
	    EU_HOU END
	  */
	/**
	 *  Finds an int in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               The int value
	 */
	public static int getInt(String key, int defaultValue) {
		if (props == null) {//workaround for Netscape JIT bug
			return defaultValue;
		}
		String s = props.getProperty(key);

		if (s != null) {
			try {
				return Integer.decode(s).intValue();
			}
			catch (NumberFormatException e) {
				IJ.write("" + e);
			}
		}
		return defaultValue;
	}


	/**
	 *  Looks up a real number in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               The double value
	 */
	public static double getDouble(String key, double defaultValue) {
		if (props == null) {
			return defaultValue;
		}
		String s = props.getProperty(key);
		Double d = null;

		if (s != null) {
			try {
				d = new Double(s);
			}
			catch (NumberFormatException e) {
				d = null;
			}
			if (d != null) {
				return (d.doubleValue());
			}
		}
		return defaultValue;
	}


	/**
	 *  Finds a color in IJ_Props or IJ_Prefs.txt.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultColor  Description of the Parameter
	 *@return               The color value
	 */
	public static Color getColor(String key, Color defaultColor) {
		int i = getInt(key, 0xaaa);

		if (i == 0xaaa) {
			return defaultColor;
		}
		return new Color((i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
	}


	/**
	 *  Returns the file.separator system property.
	 *
	 *@return    The fileSeparator value
	 */
	public static String getFileSeparator() {
		return separator;
	}


	/**
	 *  Opens the IJ_Prefs.txt file.
	 */
	static void loadPreferences() {
		String path = prefsDir + separator + PREFS_NAME;
		boolean ok = loadPrefs(path);

		if (!ok && IJ.isMacOSX()) {
			path = System.getProperty("user.home") + separator + PREFS_NAME;
			ok = loadPrefs(path);// look in home dir
			if (ok) {
				new File(path).delete();
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  path  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	static boolean loadPrefs(String path) {
		try {
			InputStream is = new BufferedInputStream(new FileInputStream(path));

			ijPrefs.load(is);
			is.close();
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}


	/**
	 *  Saves user preferences in the IJ_Prefs.txt properties file.
	 */
	public static void savePreferences() {
		try {
			Properties prefs = new Properties();
			String dir = OpenDialog.getDefaultDirectory();

			if (dir != null) {
				prefs.put(DIR_IMAGE, dir);
			}
			//prefs.put(KEY_NB_FILES_HISTORY, Integer.toString(History.getNumberOfFilesInHistory()));

			prefs.put(ROICOLOR, Tools.c2hex(Roi.getColor()));
			prefs.put(SHOW_ALL_COLOR, Tools.c2hex(ImageCanvas.getShowAllColor()));
			prefs.put(FCOLOR, Tools.c2hex(Toolbar.getForegroundColor()));
			prefs.put(BCOLOR, Tools.c2hex(Toolbar.getBackgroundColor()));
			//prefs.put(JPEG, Integer.toString(JpegWriter.getQuality()));
                        prefs.put(JPEG, Integer.toString(FileSaver.getJpegQuality()));
			prefs.put(FPS, Double.toString(Animator.getFrameRate()));
			prefs.put(DIV_BY_ZERO_VALUE, Double.toString(FloatBlitter.divideByZeroValue));
			prefs.put(NOISE_SD, Double.toString(Filters.getSD()));
			if (threads > 1) {
				prefs.put(THREADS, Integer.toString(threads));
			}
			if (IJ.isMacOSX()) {
				useJFileChooser = false;
			}
			saveOptions(prefs);
			savePluginPrefs(prefs);
			IJ.getInstance().savePreferences(prefs);
			Menus.savePreferences(prefs);
			ParticleAnalyzer.savePreferences(prefs);
			Analyzer.savePreferences(prefs);
			ImportDialog.savePreferences(prefs);
			PlotWindow.savePreferences(prefs);
			//EU_HOU Changes
			//GelAnalyzer.savePreferences(prefs);
			NewImage.savePreferences(prefs);

			String path = prefsDir + separator + PREFS_NAME;

			savePrefs(prefs, path);
		}
		catch (Throwable t) {
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);

			t.printStackTrace(pw);
			IJ.log(caw.toString());
			//EU_HOU Bundle
			IJ.log("<Unable to save preferences>");
			IJ.wait(3000);
		}
	}


	/**
	 *  Description of the Method
	 */
	static void loadOptions() {
		int options = getInt(OPTIONS, ANTIALIASING);

		usePointerCursor = (options & USE_POINTER) != 0;
		//antialiasedText = (options&ANTIALIASING)!=0;
		antialiasedText = false;
		interpolateScaledImages = (options & INTERPOLATE) != 0;
		open100Percent = (options & ONE_HUNDRED_PERCENT) != 0;
		open100Percent = (options & ONE_HUNDRED_PERCENT) != 0;
		blackBackground = (options & BLACK_BACKGROUND) != 0;
		useJFileChooser = (options & JFILE_CHOOSER) != 0;
		weightedColor = (options & WEIGHTED) != 0;
		if (weightedColor) {
			ColorProcessor.setWeightingFactors(0.299, 0.587, 0.114);
		}
		blackCanvas = (options & BLACK_CANVAS) != 0;
		pointAutoMeasure = (options & AUTO_MEASURE) != 0;
		requireControlKey = (options & REQUIRE_CONTROL) != 0;
		useInvertingLut = (options & USE_INVERTING_LUT) != 0;
		antialiasedTools = (options & ANTIALIASED_TOOLS) != 0;
		intelByteOrder = (options & INTEL_BYTE_ORDER) != 0;
		doubleBuffer = (options & DOUBLE_BUFFER) != 0;
		noPointLabels = (options & NO_POINT_LABELS) != 0;
		noBorder = (options & NO_BORDER) != 0;
		showAllSliceOnly = (options & SHOW_ALL_SLICE_ONLY) != 0;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  prefs  Description of the Parameter
	 */
	static void saveOptions(Properties prefs) {
		int options = (usePointerCursor ? USE_POINTER : 0) + (antialiasedText ? ANTIALIASING : 0)
				 + (interpolateScaledImages ? INTERPOLATE : 0) + (open100Percent ? ONE_HUNDRED_PERCENT : 0)
				 + (blackBackground ? BLACK_BACKGROUND : 0) + (useJFileChooser ? JFILE_CHOOSER : 0)
				 + (blackCanvas ? BLACK_CANVAS : 0) + (weightedColor ? WEIGHTED : 0)
				 + (pointAutoMeasure ? AUTO_MEASURE : 0) + (requireControlKey ? REQUIRE_CONTROL : 0)
				 + (useInvertingLut ? USE_INVERTING_LUT : 0) + (antialiasedTools ? ANTIALIASED_TOOLS : 0)
				 + (intelByteOrder ? INTEL_BYTE_ORDER : 0) + (doubleBuffer ? DOUBLE_BUFFER : 0)
				 + (noPointLabels ? NO_POINT_LABELS : 0) + (noBorder ? NO_BORDER : 0)
				 + (showAllSliceOnly ? SHOW_ALL_SLICE_ONLY : 0);

		prefs.put(OPTIONS, Integer.toString(options));
	}


	/**
	 *  Saves the value of the string <code>text</code> in the preferences file
	 *  using the keyword <code>key</code>. This string can be retrieved using the
	 *  appropriate <code>get()</code> method.
	 *
	 *@param  key   Description of the Parameter
	 *@param  text  Description of the Parameter
	 */
	public static void set(String key, String text) {
		if (key.indexOf('.') < 1) {
			//EU_HOU Bundle
			throw new IllegalArgumentException("Key must have a prefix");
		}
		ijPrefs.put(KEY_PREFIX + key, text);
	}


	/**
	 *  Saves <code>value</code> in the preferences file using the keyword <code>key</code>
	 *  . This value can be retrieved using the appropriate <code>getPref()</code>
	 *  method.
	 *
	 *@param  key    Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public static void set(String key, int value) {
		set(key, Integer.toString(value));
	}


	/**
	 *  Saves <code>value</code> in the preferences file using the keyword <code>key</code>
	 *  . This value can be retrieved using the appropriate <code>getPref()</code>
	 *  method.
	 *
	 *@param  key    Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public static void set(String key, double value) {
		set(key, "" + value);
	}


	/**
	 *  Saves the boolean variable <code>value</code> in the preferences file using
	 *  the keyword <code>key</code>. This value can be retrieved using the
	 *  appropriate <code>getPref()</code> method.
	 *
	 *@param  key    Description of the Parameter
	 *@param  value  Description of the Parameter
	 */
	public static void set(String key, boolean value) {
		set(key, "" + value);
	}


	/**
	 *  Uses the keyword <code>key</code> to retrieve a string from the preferences
	 *  file. Returns <code>defaultValue</code> if the key is not found.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               Description of the Return Value
	 */
	public static String get(String key, String defaultValue) {
		String value = ijPrefs.getProperty(KEY_PREFIX + key);

		if (value == null) {
			return defaultValue;
		}
		else {
			return value;
		}
	}


	/**
	 *  Uses the keyword <code>key</code> to retrieve a number from the preferences
	 *  file. Returns <code>defaultValue</code> if the key is not found.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               Description of the Return Value
	 */
	public static double get(String key, double defaultValue) {
		String s = ijPrefs.getProperty(KEY_PREFIX + key);
		Double d = null;

		if (s != null) {
			try {
				d = new Double(s);
			}
			catch (NumberFormatException e) {
				d = null;
			}
			if (d != null) {
				return (d.doubleValue());
			}
		}
		return defaultValue;
	}


	/**
	 *  Uses the keyword <code>key</code> to retrieve a boolean from the
	 *  preferences file. Returns <code>defaultValue</code> if the key is not
	 *  found.
	 *
	 *@param  key           Description of the Parameter
	 *@param  defaultValue  Description of the Parameter
	 *@return               Description of the Return Value
	 */
	public static boolean get(String key, boolean defaultValue) {
		String value = ijPrefs.getProperty(KEY_PREFIX + key);

		if (value == null) {
			return defaultValue;
		}
		else {
			return value.equals("true");
		}
	}


	/**
	 *  Saves the Point <code>loc</code> in the preferences file as a string using
	 *  the keyword <code>key</code>.
	 *
	 *@param  key  Description of the Parameter
	 *@param  loc  Description of the Parameter
	 */
	public static void saveLocation(String key, Point loc) {
		set(key, loc.x + "," + loc.y);
	}


	/**
	 *  Uses the keyword <code>key</code> to retrieve a location from the
	 *  preferences file. Returns null if the key is not found or the location is
	 *  not valid (e.g., offscreen).
	 *
	 *@param  key  Description of the Parameter
	 *@return      The location value
	 */
	public static Point getLocation(String key) {
		String value = ijPrefs.getProperty(KEY_PREFIX + key);

		if (value == null) {
			return null;
		}
		int index = value.indexOf(",");

		if (index == -1) {
			return null;
		}
		double xloc = Tools.parseDouble(value.substring(0, index));

		if (Double.isNaN(xloc) || index == value.length() - 1) {
			return null;
		}
		double yloc = Tools.parseDouble(value.substring(index + 1));

		if (Double.isNaN(yloc)) {
			return null;
		}
		Point p = new Point((int) xloc, (int) yloc);
		Dimension screen = IJ.getScreenSize();

		if (p.x > screen.width - 100 || p.y > screen.height - 40) {
			return null;
		}
		else {
			return p;
		}
	}


	/**
	 *  Save plugin preferences.
	 *
	 *@param  prefs  Description of the Parameter
	 */
	static void savePluginPrefs(Properties prefs) {
		Enumeration e = ijPrefs.keys();

		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();

			if (key.indexOf(KEY_PREFIX) == 0) {
				prefs.put(key, ijPrefs.getProperty(key));
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  prefs            Description of the Parameter
	 *@param  path             Description of the Parameter
	 *@exception  IOException  Description of the Exception
	 */
	public static void savePrefs(Properties prefs, String path)
		throws IOException {
		FileOutputStream fos = new FileOutputStream(path);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		//EU_HOU Bundle
		prefs.store(bos, "SalsaJ " + ImageJ.VERSION + " Preferences");
		bos.close();
	}


	/**
	 *  Returns the number of threads used by PlugInFilters to process stacks.
	 *
	 *@return    The threads value
	 */
	public static int getThreads() {
		if (threads == 0) {
			threads = getInt(THREADS, 0);

			int processors = Runtime.getRuntime().availableProcessors();

			if (threads < 1 || threads > processors) {
				threads = processors;
			}
		}
		return threads;
	}


	/**
	 *  Sets the number of threads (1-32) used by PlugInFilters to process stacks.
	 *
	 *@param  n  The new threads value
	 */
	public static void setThreads(int n) {
		if (n < 1) {
			n = 1;
		}
		if (n > 32) {
			n = 32;
		}
		threads = n;
	}

}

