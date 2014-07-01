package ij.plugin;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import ij.*;
import ij.gui.*;
import ij.macro.*;
import ij.text.*;
import ij.util.Tools;
import ij.io.*;
import ij.macro.MacroConstants;
import ij.plugin.frame.Recorder;
import java.util.*;

/**
 *  This plugin implements the Plugins/Macros/Install Macros command. It is also
 *  used by the Editor class to install macro in menus and by the ImageJ class
 *  to install macros at startup.
 *
 *@author     Thomas
 *@created    15 novembre 2007
 */
public class MacroInstaller implements PlugIn, MacroConstants, ActionListener {

	/**
	 *  Description of the Field
	 */
	public final static int MAX_SIZE = 28000, MAX_MACROS = 75, XINC = 10, YINC = 18;
	/**
	 *  Description of the Field
	 */
	public final static char commandPrefix = '^';
	final static String commandPrefixS = "^";

	private int[] macroStarts;
	private String[] macroNames;
	private MenuBar mb = new MenuBar();
	private int nMacros;
	private Program pgm;
	private boolean firstEvent = true;
	private String shortcutsInUse;
	private int inUseCount;
	private int nShortcuts;
	private int toolCount;
	private String text;
	private String anonymousName;
	private Menu macrosMenu;
	private int autoRunCount, autoRunAndHideCount;
	private boolean openingStartupMacrosInEditor;

	private static String defaultDir, fileName;
	private static MacroInstaller instance, listener;


	/**
	 *  Main processing method for the MacroInstaller object
	 *
	 *@param  path  Description of the Parameter
	 */
	public void run(String path) {
		if (path == null || path.equals("")) {
			path = showDialog();
		}
		if (path == null) {
			return;
		}
		openingStartupMacrosInEditor = path.indexOf("StartupMacros") != -1;

	String text = open(path);

		if (text != null) {
		String functions = Interpreter.getAdditionalFunctions();

			if (functions != null) {
				if (!(text.endsWith("\n") || functions.startsWith("\n"))) {
					text = text + "\n" + functions;
				} else {
					text = text + functions;
				}
			}
			install(text);
		}
	}


	/**
	 *  Description of the Method
	 */
	void install() {
		if (text != null) {
		Tokenizer tok = new Tokenizer();

			pgm = tok.tokenize(text);
		}
		IJ.showStatus("");

	int[] code = pgm.getCode();
	Symbol[] symbolTable = pgm.getSymbolTable();
	int count = 0;
	int token;
	int nextToken;
	int address;
	String name;
	Symbol symbol;

		shortcutsInUse = null;
		inUseCount = 0;
		nShortcuts = 0;
		toolCount = 0;
		macroStarts = new int[MAX_MACROS];
		macroNames = new String[MAX_MACROS];

	int itemCount = macrosMenu.getItemCount();
	int baseCount = macrosMenu == Menus.getMacrosMenu() ? 5 : 5;

		if (itemCount > baseCount) {
			for (int i = itemCount - 1; i >= baseCount; i--) {
				macrosMenu.remove(i);
			}
		}
		if (pgm.hasVars() && pgm.macroCount() > 0 && pgm.getGlobals() == null) {
			new Interpreter().saveGlobals(pgm);
		}
		for (int i = 0; i < code.length; i++) {
			token = code[i] & TOK_MASK;
			if (token == MACRO) {
				nextToken = code[i + 1] & TOK_MASK;
				if (nextToken == STRING_CONSTANT) {
					if (count == MAX_MACROS) {
						if (macrosMenu == Menus.getMacrosMenu()) {
							//EU_HOU Bundle
							IJ.error("Macro Installer", "Macro sets are limited to " + MAX_MACROS + " macros.");
						}
						break;
					}
					address = code[i + 1] >> TOK_SHIFT;
					symbol = symbolTable[address];
					name = symbol.str;
					macroStarts[count] = i + 2;
					macroNames[count] = name;
					if (name.indexOf('-') != -1 && (name.indexOf("Tool") != -1 || name.indexOf("tool") != -1)) {
						Toolbar.getInstance().addMacroTool(name, this, toolCount);
						toolCount++;
					} else if (name.startsWith("AutoRun")) {
						if (autoRunCount == 0 && !openingStartupMacrosInEditor) {
							new MacroRunner(pgm, macroStarts[count], name, null);
							if (name.equals("AutoRunAndHide")) {
								autoRunAndHideCount++;
							}
						}
						autoRunCount++;
						count--;
					} else if (name.equals("Popup Menu")) {
						installPopupMenu(name, pgm);
					} else if (!name.endsWith("Tool Selected")) {
						addShortcut(name);
						macrosMenu.add(new MenuItem(name));
					}
					//IJ.log(count+" "+name+" "+macroStarts[count]);
					count++;
				}
			} else if (token == EOF) {
				break;
			}
		}
		nMacros = count;
		if (toolCount > 0) {
		Toolbar tb = Toolbar.getInstance();

			/*
			 *  EU_HOU CHANGES
			 */
			//if(Toolbar.getToolId()>=Toolbar.SPARE2)
			//	tb.setTool(Toolbar.RECTANGLE);
			/*
			 *  EU_HOU END
			 */
			tb.repaint();
		}
		this.instance = this;
		if (shortcutsInUse != null && text != null) {
			//EU_HOU Bundle
			IJ.showMessage("Install Macros", (inUseCount == 1 ? "This keyboard shortcut is" : "These keyboard shortcuts are")
					 + " already in use:" + shortcutsInUse);
		}
		if (nMacros == 0 && fileName != null) {
		int dotIndex = fileName.lastIndexOf('.');

			if (dotIndex > 0) {
				anonymousName = fileName.substring(0, dotIndex);
			} else {
				anonymousName = fileName;
			}
			macrosMenu.add(new MenuItem(anonymousName));
			nMacros = 1;
		}
	//EU_HOU Bundle
	String word = nMacros == 1 ? " macro" : " macros";
//EU_HOU Bundle
		IJ.showStatus(nMacros + word + " installed");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  text  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public int install(String text) {
		if (text == null && pgm == null) {
			return 0;
		}
		this.text = text;
		macrosMenu = Menus.getMacrosMenu();
		if (listener != null) {
			macrosMenu.removeActionListener(listener);
		}
		macrosMenu.addActionListener(this);
		listener = this;
		install();
		return nShortcuts;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  text  Description of the Parameter
	 *@param  menu  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public int install(String text, Menu menu) {
		this.text = text;
		macrosMenu = menu;
		install();
		return nShortcuts + toolCount;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  path  Description of the Parameter
	 */
	public void installFile(String path) {
		if (path != null) {
		String text = open(path);

			if (text != null) {
				install(text);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  path  Description of the Parameter
	 */
	public void installLibrary(String path) {
	String text = open(path);

		if (text != null) {
			Interpreter.setAdditionalFunctions(text);
		}
	}


	/**
	 *  Installs a macro set contained in ij.jar.
	 *
	 *@param  path  Description of the Parameter
	 */
	public void installFromIJJar(String path) {
	String text = openFromIJJar(path);

		if (text != null) {
			install(text);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 *@param  pgm   Description of the Parameter
	 */
	void installPopupMenu(String name, Program pgm) {
	Hashtable h = pgm.getMenus();

		if (h == null) {
			return;
		}
	String[] commands = (String[]) h.get(name);

		if (commands == null) {
			return;
		}
	PopupMenu popup = Menus.getPopupMenu();

		if (popup == null) {
			return;
		}
		popup.removeAll();
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].equals("-")) {
				popup.addSeparator();
			} else {
			MenuItem mi = new MenuItem(commands[i]);

				mi.addActionListener(this);
				popup.add(mi);
			}
		}
	}


	/**
	 *  Description of the Method
	 */
	void removeShortcuts() {
		Menus.getMacroShortcuts().clear();

	Hashtable shortcuts = Menus.getShortcuts();

		for (Enumeration en = shortcuts.keys(); en.hasMoreElements(); ) {
		Integer key = (Integer) en.nextElement();
		String value = (String) shortcuts.get(key);

			if (value.charAt(0) == commandPrefix) {
				shortcuts.remove(key);
			}
		}
	}


	/**
	 *  Adds a feature to the Shortcut attribute of the MacroInstaller object
	 *
	 *@param  name  The feature to be added to the Shortcut attribute
	 */
	void addShortcut(String name) {
	int index1 = name.indexOf('[');

		if (index1 == -1) {
			return;
		}
	int index2 = name.lastIndexOf(']');

		if (index2 <= (index1 + 1)) {
			return;
		}
	String shortcut = name.substring(index1 + 1, index2);
	int len = shortcut.length();

		if (len > 1) {
			shortcut = shortcut.toUpperCase(Locale.US);
		}
		;
		if (len > 3 || (len > 1 && shortcut.charAt(0) != 'F' && shortcut.charAt(0) != 'N')) {
			return;
		}
	int code = Menus.convertShortcutToCode(shortcut);

		if (code == 0) {
			return;
		}
		if (nShortcuts == 0) {
			removeShortcuts();
		}
		// One character shortcuts go in a separate hash table to
		// avoid conflicts with ImageJ menu shortcuts.
		if (len == 1) {
		Hashtable macroShortcuts = Menus.getMacroShortcuts();

			macroShortcuts.put(new Integer(code), commandPrefix + name);
			nShortcuts++;
			return;
		}
	Hashtable shortcuts = Menus.getShortcuts();

		if (shortcuts.get(new Integer(code)) != null) {
			if (shortcutsInUse == null) {
				shortcutsInUse = "\n \n";
			}
			shortcutsInUse += "	  " + name + "\n";
			inUseCount++;
			return;
		}
		shortcuts.put(new Integer(code), commandPrefix + name);
		nShortcuts++;
		//IJ.log("addShortcut3: "+name+"	  "+shortcut+"	  "+code);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Return Value
	 */
	String showDialog() {
		if (defaultDir == null) {
			defaultDir = Menus.getMacrosPath();
		}
	//EU_HOU Bundle
	OpenDialog od = new OpenDialog("Install Macros", defaultDir, fileName);
	String name = od.getFileName();

		if (name == null) {
			return null;
		}
	String dir = od.getDirectory();

		if (!(name.endsWith(".txt") || name.endsWith(".ijm"))) {
			//EU_HOU Bundle
			IJ.showMessage("Macro Installer", "File name must end with \".txt\" or \".ijm\" .");
			return null;
		}
		fileName = name;
		defaultDir = dir;
		return dir + name;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  path  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	String open(String path) {
		try {
		StringBuffer sb = new StringBuffer(5000);
		BufferedReader r = new BufferedReader(new FileReader(path));

			while (true) {
			String s = r.readLine();

				if (s == null) {
					break;
				} else {
					sb.append(s + "\n");
				}
			}
			r.close();
			return new String(sb);
		} catch (Exception e) {
			IJ.error(e.getMessage());
			return null;
		}
	}


	/**
	 *  Returns a text file contained in ij.jar.
	 *
	 *@param  path  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public String openFromIJJar(String path) {
	//ImageJ ij = IJ.getInstance();
	//if (ij==null) return null;
	String text = null;

		try {
		InputStream is = this.getClass().getResourceAsStream(path);
			//IJ.log(is+"	"+path);
			if (is == null) {
				return null;
			}
		InputStreamReader isr = new InputStreamReader(is);
		StringBuffer sb = new StringBuffer();
		char[] b = new char[8192];
		int n;

			while ((n = isr.read(b)) > 0) {
				sb.append(b, 0, n);
			}
			text = sb.toString();
		} catch (IOException e) {}
		return text;
	}

	//void runMacro() {
	// new MacroRunner(text);
	//}

	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public boolean runMacroTool(String name) {
		for (int i = 0; i < nMacros; i++) {
			if (macroNames[i].startsWith(name)) {
				new MacroRunner(pgm, macroStarts[i], name, null);
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name     Description of the Parameter
	 *@param  command  Description of the Parameter
	 *@return          Description of the Return Value
	 */
	public boolean runMenuTool(String name, String command) {
		for (int i = 0; i < nMacros; i++) {
			if (macroNames[i].startsWith(name)) {
				Recorder.recordInMacros = true;
				new MacroRunner(pgm, macroStarts[i], name, command);
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public static boolean runMacroCommand(String name) {
		if (instance == null) {
			return false;
		}
		if (name.startsWith(commandPrefixS)) {
			name = name.substring(1);
		}
		for (int i = 0; i < instance.nMacros; i++) {
			if (name.equals(instance.macroNames[i])) {
				new MacroRunner(instance.pgm, instance.macroStarts[i], name, null);
				return true;
			}
		}
		return false;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 */
	public static void runMacroShortcut(String name) {
		if (instance == null) {
			return;
		}
		if (name.startsWith(commandPrefixS)) {
			name = name.substring(1);
		}
		for (int i = 0; i < instance.nMacros; i++) {
			if (name.equals(instance.macroNames[i])) {
				(new MacroRunner()).runShortcut(instance.pgm, instance.macroStarts[i], name);
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  name  Description of the Parameter
	 */
	public void runMacro(String name) {
		if (anonymousName != null && name.equals(anonymousName)) {
			//IJ.log("runMacro: "+anonymousName);
			new MacroRunner(pgm, 0, anonymousName, null);
			return;
		}
		for (int i = 0; i < nMacros; i++) {
			if (name.equals(macroNames[i])) {
				new MacroRunner(pgm, macroStarts[i], name, null);
				return;
			}
		}
	}


	/**
	 *  Gets the macroCount attribute of the MacroInstaller object
	 *
	 *@return    The macroCount value
	 */
	public int getMacroCount() {
		return nMacros;
	}


	/**
	 *  Gets the program attribute of the MacroInstaller object
	 *
	 *@return    The program value
	 */
	public Program getProgram() {
		return pgm;
	}


	/**
	 *  Returns true if an "AutoRunAndHide" macro was run/installed.
	 *
	 *@return    The autoRunAndHide value
	 */
	public boolean isAutoRunAndHide() {
		return autoRunAndHideCount > 0;
	}


	/**
	 *  Sets the fileName attribute of the MacroInstaller object
	 *
	 *@param  fileName  The new fileName value
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
		//EU_HOU Bundle
		openingStartupMacrosInEditor = fileName.startsWith("StartupMacros");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  evt  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
	String cmd = evt.getActionCommand();
	MenuItem item = (MenuItem) evt.getSource();
	MenuContainer parent = item.getParent();

		if (parent instanceof PopupMenu) {
			for (int i = 0; i < nMacros; i++) {
				if (macroNames[i].equals("Popup Menu")) {
					new MacroRunner(pgm, macroStarts[i], "Popup Menu", cmd);
					return;
				}
			}
		} else {
			runMacro(cmd);
		}
	}

}


