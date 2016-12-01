package ij.text;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.filter.Analyzer;
import ij.measure.ResultsTable;
import ij.macro.Interpreter;
/*
 *  EU_HOU CHANGES
 */
import java.util.ResourceBundle;
import ij.process.Photometer;
/*
 *  EU_HOU END
 */

/**
 * Uses a TextPanel to displays text in a window.
 *
 * @author Thomas
 * @created 6 novembre 2007
 * @see TextPanel
 */
public class TextWindow extends Frame implements ActionListener, FocusListener, ItemListener {
    /*
     *  EU_HOU CHANGES
     */

    private boolean modifListen = false;
    private boolean ListenTextPanel = false;

    /*
     *  EU_HOU END
     */
    /**
     * Description of the Field
     */
    public final static String LOC_KEY = "results.loc";
    /**
     * Description of the Field
     */
    public final static String WIDTH_KEY = "results.width";
    /**
     * Description of the Field
     */
    public final static String HEIGHT_KEY = "results.height";
    final static String FONT_SIZE = "tw.font.size";
    final static String FONT_ANTI = "tw.font.anti";
    TextPanel textPanel;
    CheckboxMenuItem antialiased;
    int[] sizes = {9, 10, 11, 12, 13, 14, 16, 18, 20, 24, 36, 48, 60, 72};
    int fontSize = (int) Prefs.get(FONT_SIZE, 5);

    /**
     * Opens a new single-column text window.
     *
     * @param title the title of the window
     * @param width the width of the window in pixels
     * @param height the height of the window in pixels
     * @param data Description of the Parameter
     */
    public TextWindow(String title, String data, int width, int height) {
        this(title, "", data, width, height);
    }

    /**
     * Opens a new multi-column text window.
     *
     * @param title the title of the window
     * @param headings the tab-delimited column headings
     * @param data the text initially displayed in the window
     * @param width the width of the window in pixels
     * @param height the height of the window in pixels
     */
    public TextWindow(String title, String headings, String data, int width, int height) {
        super(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        if (IJ.isLinux()) {
            setBackground(ImageJ.backgroundColor);
        }
        textPanel = new TextPanel(title);
        textPanel.setTitle(title);
        add("Center", textPanel);
        textPanel.setColumnHeadings(headings);
        if (data != null && !data.equals("")) {
            textPanel.append(data);
        }
        addKeyListener(textPanel);
        ImageJ ij = IJ.getInstance();
        if (ij != null) {
            Image img = ij.getIconImage();
            if (img != null) {
                try {
                    setIconImage(img);
                } catch (Exception e) {
                }
            }
        }
        addFocusListener(this);
        addMenuBar();
        setFont();
        WindowManager.addWindow(this);
        Point loc = null;
        int w = 0;
        int h = 0;
        if (title.equals("Results")) {
            loc = Prefs.getLocation(LOC_KEY);
            w = (int) Prefs.get(WIDTH_KEY, 0.0);
            h = (int) Prefs.get(HEIGHT_KEY, 0.0);
        }
        if (loc != null && w > 0 && h > 0) {
            setSize(w, h);
            setLocation(loc);
        } else {
            setSize(width, height);
            GUI.center(this);
        }
        show();
    }

    public TextWindow(String title, String headings, String data, int width, int height, boolean hint) {

        super(title);
        enableEvents(AWTEvent.WINDOW_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK);
        setBackground(ImageJ.backgroundColor);
        textPanel = new TextPanel(title, hint);
        ListenTextPanel = true;
        textPanel.setTitle(title);
        add("Center", textPanel);
        textPanel.setColumnHeadings(headings);
        if (data != null) {
            textPanel.append(data);
        }
        addKeyListener(textPanel);
        ImageJ ij = IJ.getInstance();
        if (ij != null) {
            Image img = ij.getIconImage();
            if (img != null) {
                try {
                    setIconImage(img);
                } catch (Exception e) {
                }
            }
        }
        addFocusListener(this);
        addMenuBar();
        WindowManager.addWindow(this);
        setSize(width, height);
        GUI.center(this);
        show();
    }

    /**
     * Opens a new text window containing the contents of a text file.
     *
     * @param path the path to the text file
     * @param width the width of the window in pixels
     * @param height the height of the window in pixels
     */
    public TextWindow(String path, int width, int height) {
        super("");
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        textPanel = new TextPanel();
        add("Center", textPanel);
        if (openFile(path)) {
            WindowManager.addWindow(this);
            setSize(width, height);
            show();
        } else {
            dispose();
        }
    }

    public void setPhotometry(boolean phot) {
        textPanel.isPhotometry = true;
    }

    /**
     * Adds a feature to the MenuBar attribute of the TextWindow object
     */
    void addMenuBar() {
        MenuBar mb = new MenuBar();

        if (Menus.getFontSize() != 0) {
            mb.setFont(Menus.getFont());
        }
        //EU_HOU Bundle
        Menu m = new Menu(IJ.getBundle().getString("File"));
//EU_HOU Bundle
        m.add(new MenuItem(IJ.getBundle().getString("SaveAs"), new MenuShortcut(KeyEvent.VK_S)));
        if (getTitle().equals("Results")) {
            m.addSeparator();
            m.add(new MenuItem("Set File Extension..."));
        }
        m.addActionListener(this);
        mb.add(m);
        //EU_HOU Bundle
        m = new Menu(IJ.getBundle().getString("Edit"));
        m.add(new MenuItem(IJ.getBundle().getString("Cut"), new MenuShortcut(KeyEvent.VK_X)));
        m.add(new MenuItem(IJ.getBundle().getString("Copy"), new MenuShortcut(KeyEvent.VK_C)));
        m.add(new MenuItem(IJ.getBundle().getString("Clear")));
        m.add(new MenuItem(IJ.getBundle().getString("SelectAll"), new MenuShortcut(KeyEvent.VK_A)));
        if (getTitle().equals(IJ.getBundle().getString("Results"))) {
            m.addSeparator();
            //EU_HOU Bundle
            m.add(new MenuItem(IJ.getBundle().getString("ClearResults")));
            m.add(new MenuItem(IJ.getBundle().getString("Summarize")));
            m.add(new MenuItem(IJ.getBundle().getString("Distribution")));
            m.add(new MenuItem(IJ.getBundle().getString("SetMeasurements")));
            m.add(new MenuItem(IJ.getBundle().getString("Duplicate")));
        }
        m.addActionListener(this);
        mb.add(m);
        //EU_HOU Bundle
        m = new Menu("Font");
        m.add(new MenuItem("Make Text Smaller"));
        m.add(new MenuItem("Make Text Larger"));
        m.addSeparator();
        //EU_HOU Bundle
        antialiased = new CheckboxMenuItem("Antialiased", Prefs.get(FONT_ANTI, IJ.isMacOSX() ? true : false));
        antialiased.addItemListener(this);
        m.add(antialiased);
        //EU_HOU Bundle
        m.add(new MenuItem("Save Settings"));
        m.addActionListener(this);
        mb.add(m);
        setMenuBar(mb);
    }

    /**
     * Adds one or lines of text to the window.
     *
     * @param text The text to be appended. Multiple lines should be separated
     * by \n.
     */
    public void append(String text) {
        textPanel.append(text);
    }

    /**
     * Sets the font attribute of the TextWindow object
     */
    void setFont() {
        textPanel.setFont(new Font("SanSerif", Font.PLAIN, sizes[fontSize]), antialiased.getState());
    }

    /**
     * Description of the Method
     *
     * @param path Description of the Parameter
     * @return Description of the Return Value
     */
    boolean openFile(String path) {
        //EU_HOU Bundle
        OpenDialog od = new OpenDialog(IJ.getBundle().getString("OpenText"), path);
        String directory = od.getDirectory();
        String name = od.getFileName();

        if (name == null) {
            return false;
        }
        path = directory + name;
        //EU_HOU Bundle
        IJ.showStatus(IJ.getBundle().getString("Opening") + " " + path);
        try {
            BufferedReader r = new BufferedReader(new FileReader(directory + name));

            load(r);
            r.close();
        } catch (Exception e) {
            IJ.error(e.getMessage());
            return true;
        }
        textPanel.setTitle(name);
        setTitle(name);
        IJ.showStatus("");
        return true;
    }

    /**
     * Returns a reference to this TextWindow's TextPanel.
     *
     * @return The textPanel value
     */
    public TextPanel getTextPanel() {
        return textPanel;
    }

    /**
     * Appends the text in the specified file to the end of this TextWindow.
     *
     * @param in Description of the Parameter
     * @exception IOException Description of the Exception
     */
    public void load(BufferedReader in) throws IOException {
        int count = 0;

        while (true) {
            String s = in.readLine();

            if (s == null) {
                break;
            }
            textPanel.appendLine(s);
        }
    }

    /**
     * Description of the Method
     *
     * @param evt Description of the Parameter
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        String cmd = evt.getActionCommand();
//EU_HOU Bundle FIXME
        if (cmd.equals("Make Text Larger")) {
            changeFontSize(true);
        } else if (cmd.equals("Make Text Smaller")) {
            changeFontSize(false);
        } else if (cmd.equals("Save Settings")) {
            saveSettings();
        } else {
            textPanel.doCommand(cmd);
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);

        int id = e.getID();

        if (id == WindowEvent.WINDOW_CLOSING) {
            /*
             *  EU_HOU CHANGES
             */
            if ((modifListen) && (!Plot.RadioSpectra)) {
                EuHouToolbar.getInstance().photometerButtonAction();
            } //Photometer.getInstance().close();
            else {
                close();
            }

            /*
             *  EU_HOU END
             */
            //close();

        } else if (id == WindowEvent.WINDOW_ACTIVATED) {
            WindowManager.setWindow(this);
        }
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void itemStateChanged(ItemEvent e) {
        setFont();
    }

    /**
     * Description of the Method
     */
    public void close() {
        if (Photometer.getInstance() != null) {
            Photometer.getInstance().resetPh();
        }
        //EU_HOU Bundle
        if (getTitle().equals(IJ.getBundle().getString("Results"))) {
            if (!Analyzer.resetCounter()) {
                return;
            }
            IJ.setTextPanel(null);
            Prefs.saveLocation(LOC_KEY, getLocation());

            Dimension d = getSize();

            Prefs.set(WIDTH_KEY, d.width);
            Prefs.set(HEIGHT_KEY, d.height);
        } else if (getTitle().equals("Log")) {
            System.out.println("TextWindow.close");
            IJ.debugMode = false;
            IJ.log("\\Closed");
        } else if (textPanel != null && textPanel.rt != null) {
            if (!saveContents()) {
                return;
            }
        } /*
         *  EU_HOU CHANGES
         //EU_HOU Bundle
         */ else if (getTitle().equals(IJ.getBundle().getString("GetPosition"))) {
            IJ.setTextPanel(null);
        }
        /*
         *  EU_HOU END
         */
        setVisible(false);
        dispose();
        IJ.setTool(Toolbar.LINE);
        WindowManager.removeWindow(this);
        textPanel.flush();
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    boolean saveContents() {
        int lineCount = textPanel.getLineCount();

        if (!textPanel.unsavedLines) {
            lineCount = 0;
        }
        ImageJ ij = IJ.getInstance();
        boolean macro = IJ.macroRunning() || Interpreter.isBatchMode();

        if (lineCount > 0 && !macro && ij != null && !ij.quitting()) {
            //EU_HOU Bundle
            YesNoCancelDialog d = new YesNoCancelDialog(this, getTitle(), "Save " + lineCount + " measurements?");

            if (d.cancelPressed()) {
                return false;
            } else if (d.yesPressed()) {
                if (!textPanel.saveAs("")) {
                    return false;
                }
            }
        }
        textPanel.rt.reset();
        return true;
    }

    /**
     * Description of the Method
     *
     * @param larger Description of the Parameter
     */
    void changeFontSize(boolean larger) {
        int in = fontSize;

        if (larger) {
            fontSize++;
            if (fontSize == sizes.length) {
                fontSize = sizes.length - 1;
            }
        } else {
            fontSize--;
            if (fontSize < 0) {
                fontSize = 0;
            }
        }
        IJ.showStatus(sizes[fontSize] + " point");
        setFont();
    }

    /**
     * Description of the Method
     */
    void saveSettings() {
        Prefs.set(FONT_SIZE, fontSize);
        Prefs.set(FONT_ANTI, antialiased.getState());
        //EU_HOU Bundle
        IJ.showStatus("Font settings saved (size=" + sizes[fontSize] + ", antialiased=" + antialiased.getState() + ")");
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void focusGained(FocusEvent e) {
        WindowManager.setWindow(this);
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public void focusLost(FocusEvent e) {
    }
}
