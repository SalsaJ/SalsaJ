package ij.text;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.*;
import java.awt.datatransfer.*;
import ij.*;
import ij.plugin.filter.Analyzer;
import ij.io.SaveDialog;
import ij.measure.ResultsTable;
import ij.util.Tools;
import ij.plugin.frame.Recorder;

import ij.process.Photometer;

/**
 *  This is an unlimited size text panel with tab-delimited, labeled and
 *  resizable columns. It is based on the hGrid class at
 *  http://www.lynx.ch/contacts/~/thomasm/Grid/index.html.
 *
 *@author     Thomas
 *@created    3 décembre 2007
 */
public class TextPanel extends Panel implements AdjustmentListener,
        MouseListener, MouseMotionListener, KeyListener, ClipboardOwner,
        ActionListener, MouseWheelListener, Runnable {

    final static int DOUBLE_CLICK_THRESHOLD = 650;
    // height / width
    int iGridWidth, iGridHeight;
    int iX, iY;
    // data
    String[] sColHead;
    Vector vData;
    int[] iColWidth;
    int iColCount, iRowCount;
    int iRowHeight, iFirstRow;
    // scrolling
    Scrollbar sbHoriz, sbVert;
    int iSbWidth, iSbHeight;
    boolean bDrag;
    int iXDrag, iColDrag;
    boolean headings = true;
    String title = "";
    String labels;
    KeyListener keyListener;
    Cursor resizeCursor = new Cursor(Cursor.E_RESIZE_CURSOR);
    Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
    int selStart = -1, selEnd = -1, selOrigin = -1, selLine = -1;
    TextCanvas tc;
    PopupMenu pm;
    boolean columnsManuallyAdjusted;
    long mouseDownTime;
    String filePath;
    ResultsTable rt;
    boolean unsavedLines;
    // EU_HOU
    static ResourceBundle etiq = IJ.getBundle();
    boolean modifListener = false;
    boolean ListenTextPanel = false;
    boolean isPhotometry = false;

    /**
     *  Constructs a new TextPanel.
     */
    public TextPanel() {
        tc = new TextCanvas(this);
        setLayout(new BorderLayout());
        add("Center", tc);
        sbHoriz = new Scrollbar(Scrollbar.HORIZONTAL);
        sbHoriz.addAdjustmentListener(this);
        sbHoriz.setFocusable(false);// prevents scroll bar from blinking on Windows
        add("South", sbHoriz);
        sbVert = new Scrollbar(Scrollbar.VERTICAL);
        sbVert.addAdjustmentListener(this);
        sbVert.setFocusable(false);
        ImageJ ij = IJ.getInstance();
        if (ij != null) {
            sbHoriz.addKeyListener(ij);
            sbVert.addKeyListener(ij);
        }
        add("East", sbVert);
        addPopupMenu();
    }

    /**
     *  Constructs a new TextPanel.
     *
     *@param  title  Description of the Parameter
     */
    public TextPanel(String title) {
        this();
        //EU_HOU Bundle
        if (title.equals(etiq.getString("Results"))) {
            pm.addSeparator();
            //EU_HOU Bundle
            //by oli addPopupItem(etiq.getString("Clear Results"));
            // by oli, would crash in English if not corrected
            addPopupItem(etiq.getString("ClearResults"));
            addPopupItem(etiq.getString("Summarize"));
            addPopupItem("Distribution");
            addPopupItem(etiq.getString("SetMeasurements"));
            addPopupItem("Duplicate...");
        }
    }

    public TextPanel(String title, boolean hint) {
        this(title);
        ListenTextPanel = true;
    }

    /**
     *  Adds a feature to the PopupMenu attribute of the TextPanel object
     */
    void addPopupMenu() {
        pm = new PopupMenu();
        //EU_HOU Bundle
        addPopupItem(etiq.getString("SaveAs"));
        pm.addSeparator();
        //EU_HOU Bundle
        addPopupItem(etiq.getString("Cut"));
        addPopupItem(etiq.getString("Copy"));
        addPopupItem(etiq.getString("Clear"));
        addPopupItem(etiq.getString("SelectAll"));
        add(pm);
    }

    /**
     *  Adds a feature to the PopupItem attribute of the TextPanel object
     *
     *@param  s  The feature to be added to the PopupItem attribute
     */
    void addPopupItem(String s) {
        MenuItem mi = new MenuItem(s);
        mi.addActionListener(this);
        pm.add(mi);
    }

    /**
     *  Clears this TextPanel and sets the column headings to those in the
     *  tab-delimited 'headings' String. Set 'headings' to "" to use a single
     *  column with no headings.
     *
     *@param  labels  The new columnHeadings value
     */
    public synchronized void setColumnHeadings(String labels) {
        boolean sameLabels = labels.equals(this.labels);
        this.labels = labels;
        if (labels.equals("")) {
            iColCount = 1;
            sColHead = new String[1];
            sColHead[0] = "";
        } /*else {
        sColHead = Tools.split(labels, "\t");
        iColCount = sColHead.length;
        }*/ else {
            StringTokenizer t = new StringTokenizer(labels, "\t");
            iColCount = t.countTokens();
            sColHead = new String[iColCount];
            for (int i = 0; i < iColCount; i++) {
                sColHead[i] = t.nextToken();
            }
        }



        flush();
        vData = new Vector();
        if (!(iColWidth != null && iColWidth.length == iColCount && sameLabels && iColCount != 1)) {
            iColWidth = new int[iColCount];
            columnsManuallyAdjusted = false;
        }
        iRowCount = 0;
        resetSelection();
        adjustHScroll();
        tc.repaint();
    }

    /**
     *  Returns the column headings as a tab-delimited string.
     *
     *@return    The columnHeadings value
     */
    public String getColumnHeadings() {
        return labels == null ? "" : labels;
    }

    /**
     *  Sets the font attribute of the TextPanel object
     *
     *@param  font         The new font value
     *@param  antialiased  The new font value
     */
    public void setFont(Font font, boolean antialiased) {
        tc.fFont = font;
        tc.iImage = null;
        tc.fMetrics = null;
        tc.antialiased = antialiased;
        iColWidth[0] = 0;
        if (isShowing()) {
            updateDisplay();
        }
    }

    /**
     *  Adds a single line to the end of this TextPanel.
     *
     *@param  data  Description of the Parameter
     */
    public void appendLine(String data) {
        if (vData == null) {
            setColumnHeadings("");
        }
        char[] chars = data.toCharArray();
        vData.addElement(chars);
        iRowCount++;
        if (isShowing()) {
            if (iColCount == 1 && tc.fMetrics != null) {
                iColWidth[0] = Math.max(iColWidth[0], tc.fMetrics.charsWidth(chars, 0, chars.length));
                adjustHScroll();
            }
            updateDisplay();
            unsavedLines = true;
        }
    }

    /**
     *  Adds one or more lines to the end of this TextPanel.
     *
     *@param  data  Description of the Parameter
     */
    public void append(String data) {
        if (data == null) {
            data = "null";
        }
        if (vData == null) {
            setColumnHeadings("");
        }
        while (true) {
            int p = data.indexOf('\n');
            if (p < 0) {
                appendWithoutUpdate(data);
                break;
            }
            appendWithoutUpdate(data.substring(0, p));
            data = data.substring(p + 1);
            if (data.equals("")) {
                break;
            }
        }
        if (isShowing()) {
            updateDisplay();
            unsavedLines = true;
        }
    }

    /**
     *  Adds a single line to the end of this TextPanel without updating the
     *  display.
     *
     *@param  data  Description of the Parameter
     */
    void appendWithoutUpdate(String data) {
        char[] chars = data.toCharArray();
        vData.addElement(chars);
        iRowCount++;
    }

    /**
     *  Description of the Method
     */
    void updateDisplay() {
        iY = iRowHeight * (iRowCount + 1);
        adjustVScroll();
        if (iColCount > 1 && iRowCount <= 10 && !columnsManuallyAdjusted) {
            iColWidth[0] = 0;
        }// forces column width calculation
        tc.repaint();
    }

    /**
     *  Gets the cell attribute of the TextPanel object
     *
     *@param  column  Description of the Parameter
     *@param  row     Description of the Parameter
     *@return         The cell value
     */
    String getCell(int column, int row) {
        if (column < 0 || column >= iColCount || row < 0 || row >= iRowCount) {
            return null;
        }
        return new String(tc.getChars(column, row));
    }

    /**
     *  Description of the Method
     */
    synchronized void adjustVScroll() {
        if (iRowHeight == 0) {
            return;
        }
        Dimension d = tc.getSize();
        int value = iY / iRowHeight;
        int visible = d.height / iRowHeight;
        int maximum = iRowCount + 1;
        if (visible < 0) {
            visible = 0;
        }
        if (visible > maximum) {
            visible = maximum;
        }
        if (value > (maximum - visible)) {
            value = maximum - visible;
        }
        sbVert.setValues(value, visible, 0, maximum);
        iY = iRowHeight * value;
    }

    /**
     *  Description of the Method
     */
    synchronized void adjustHScroll() {
        if (iRowHeight == 0) {
            return;
        }
        Dimension d = tc.getSize();
        int w = 0;
        for (int i = 0; i < iColCount; i++) {
            w += iColWidth[i];
        }
        iGridWidth = w;
        sbHoriz.setValues(iX, d.width, 0, iGridWidth);
        iX = sbHoriz.getValue();
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void adjustmentValueChanged(AdjustmentEvent e) {
        iX = sbHoriz.getValue();
        iY = iRowHeight * sbVert.getValue();
        tc.repaint();
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mousePressed(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (e.isPopupTrigger() || e.isMetaDown()) {
            pm.show(e.getComponent(), x, y);
        } else if (e.isShiftDown()) {
            extendSelection(x, y);
        } else {
            select(x, y);
            handleDoubleClick();
        }
    }

    /**
     *  Description of the Method
     */
    void handleDoubleClick() {
        if (selStart < 0 || selStart != selEnd || iColCount != 1) {
            return;
        }
        boolean doubleClick = System.currentTimeMillis() - mouseDownTime <= DOUBLE_CLICK_THRESHOLD;
        mouseDownTime = System.currentTimeMillis();
        if (doubleClick) {
            char[] chars = (char[]) (vData.elementAt(selStart));
            String s = new String(chars);
            int index = s.indexOf(": ");
            if (index > -1 && !s.endsWith(": ")) {
                s = s.substring(index + 2);
            }// remove sequence number added by ListFilesRecursively
            if (s.indexOf(File.separator) != -1 || s.indexOf(".") != -1) {
                filePath = s;
                Thread thread = new Thread(this, "Open");
                thread.setPriority(thread.getPriority() - 1);
                thread.start();
            }
        }
    }

    /**
     *  For better performance, open double-clicked files on separate thread
     *  instead of on event dispatch thread.
     */
    public void run() {
        if (filePath != null) {
            IJ.open(filePath);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseExited(MouseEvent e) {
        if (bDrag) {
            setCursor(defaultCursor);
            bDrag = false;
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseMoved(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        if (y <= iRowHeight) {
            int xb = x;
            x = x + iX - iGridWidth;
            int i = iColCount - 1;
            for (; i >= 0; i--) {
                if (x > -7 && x < 7) {
                    break;
                }
                x += iColWidth[i];
            }
            if (i >= 0) {
                if (!bDrag) {
                    setCursor(resizeCursor);
                    bDrag = true;
                    iXDrag = xb - iColWidth[i];
                    iColDrag = i;
                }
                return;
            }
        }
        if (bDrag) {
            setCursor(defaultCursor);
            bDrag = false;
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseDragged(MouseEvent e) {
        if (e.isPopupTrigger() || e.isMetaDown()) {
            return;
        }
        int x = e.getX();
        int y = e.getY();
        if (bDrag && x < tc.getSize().width) {
            int w = x - iXDrag;
            if (w < 0) {
                w = 0;
            }
            iColWidth[iColDrag] = w;
            columnsManuallyAdjusted = true;
            adjustHScroll();
            tc.repaint();
        } else {
            extendSelection(x, y);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseReleased(MouseEvent e) {
        if (ListenTextPanel) {
            String s = getLine();
            if (s == null) {
                return;
            }
            Photometer.getInstance().set(s);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseClicked(MouseEvent e) {
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseEntered(MouseEvent e) {
    }

    public String getLine() {
        if ((selLine == -1) || (selLine >= getLineCount())) {
            return null;
        }
        return getLine(selLine);
    }

    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (this) {
            int rot = event.getWheelRotation();
            sbVert.setValue(sbVert.getValue() + rot);
            iY = iRowHeight * sbVert.getValue();
            tc.repaint();
        }
    }

    /**
     *  Unused keyPressed and keyTyped events will be passed to 'listener'.
     *
     *@param  listener  The feature to be added to the KeyListener attribute
     */
    public void addKeyListener(KeyListener listener) {
        keyListener = listener;
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void keyPressed(KeyEvent e) {
        //boolean cutCopyOK = (e.isControlDown()||e.isMetaDown())
        //	&& selStart!=-1 && selEnd!=-1;
        //if (cutCopyOK && e.getKeyCode()==KeyEvent.VK_C)
        //	copySelection();
        //else if (cutCopyOK && e.getKeyCode()==KeyEvent.VK_X)
        //	{if (copySelection()>0) clearSelection();}
        //else if (cutCopyOK && e.getKeyCode()==KeyEvent.VK_A)
        //	selectAll();
        //else if (keyListener!=null)
        //	keyListener.keyPressed(e);
        int key = e.getKeyCode();
        if (keyListener != null && key != KeyEvent.VK_S && key != KeyEvent.VK_C && key != KeyEvent.VK_X && key != KeyEvent.VK_A) {
            keyListener.keyPressed(e);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void keyTyped(KeyEvent e) {
        if (keyListener != null) {
            keyListener.keyTyped(e);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        doCommand(cmd);
    }
    //EU_HOU Bundle

    /**
     *  Description of the Method
     *
     *@param  cmd  Description of the Parameter
     */
    void doCommand(String cmd) {
        // EU_HOU Bundle
        IJ.log(cmd);
        if (cmd == null) {
            return;
        }
        if (cmd.equals(etiq.getString("SaveAs"))) {
            saveAs("");
        } else if (cmd.equals(etiq.getString("Cut"))) {
            cutSelection();
        } else if (cmd.equals(etiq.getString("Copy"))) {
            copySelection();
        } else if (cmd.equals(etiq.getString("Clear"))) {
            clearSelection();
        } else if (cmd.equals(etiq.getString("SelectAll"))) {
            selectAll();
        } else if (cmd.equals(etiq.getString("Duplicate..."))) {
            duplicate();
        } else if (cmd.equals(etiq.getString("Summarize"))) {
            IJ.doCommand("Summarize");
        } else if (cmd.equals(etiq.getString("Distribution..."))) {
            IJ.doCommand("Distribution...");
        } else if (cmd.equals(etiq.getString("ClearResults"))) {
            IJ.doCommand("ClearResults");
        } else if (cmd.equals(etiq.getString("SetMeasurements"))) {
            IJ.doCommand("SetMeasurements");
        } else if (cmd.equals(etiq.getString("Set File Extension..."))) {
            IJ.doCommand("Input/Output...");
        }
    }

    /**
     *  Description of the Method
     *
     *@param  clip  Description of the Parameter
     *@param  cont  Description of the Parameter
     */
    public void lostOwnership(Clipboard clip, Transferable cont) {
    }

    /**
     *  Description of the Method
     */
    void duplicate() {
        if (rt == null) {
            return;
        }
        ResultsTable rt2 = (ResultsTable) rt.clone();
        //EU_HOU Bundle
        String title2 = IJ.getString("Title:", etiq.getString("Results2"));
        if (!title2.equals("")) {
            //EU_HOU Bundle
            if (title2.equals(etiq.getString("Results"))) {
                title2 = etiq.getString("Results") + "2";
            }
            rt2.show(title2);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  x  Description of the Parameter
     *@param  y  Description of the Parameter
     */
    void select(int x, int y) {
        Dimension d = tc.getSize();
        if (iRowHeight == 0 || x > d.width || y > d.height) {
            return;
        }
        int r = (y / iRowHeight) - 1 + iFirstRow;
        int lineWidth = iGridWidth;
        if (iColCount == 1 && tc.fMetrics != null && r >= 0 && r < iRowCount) {
            char[] chars = (char[]) vData.elementAt(r);
            lineWidth = Math.max(tc.fMetrics.charsWidth(chars, 0, chars.length), iGridWidth);
        }
        if (r >= 0 && r < iRowCount && x < lineWidth) {
            selOrigin = r;
            selStart = r;
            selEnd = r;
        } else {
            resetSelection();
            selOrigin = r;
            if (r >= iRowCount) {
                selOrigin = iRowCount - 1;
            }
            //System.out.println("select: "+selOrigin);
        }
        tc.repaint();
        selLine = r;
    }

    /**
     *  Description of the Method
     *
     *@param  x  Description of the Parameter
     *@param  y  Description of the Parameter
     */
    void extendSelection(int x, int y) {
        Dimension d = tc.getSize();
        if (iRowHeight == 0 || x > d.width || y > d.height) {
            return;
        }
        int r = (y / iRowHeight) - 1 + iFirstRow;
        //System.out.println(r+"  "+selOrigin);
        if (r >= 0 && r < iRowCount) {
            if (r < selOrigin) {
                selStart = r;
                selEnd = selOrigin;

            } else {
                selStart = selOrigin;
                selEnd = r;
            }
        }
        tc.repaint();
        selLine = r;
    }

    /**
     *  Copies the current selection to the system clipboard. Returns the number of
     *  characters copied.
     *
     *@return    Description of the Return Value
     */
    public int copySelection() {
        if (Recorder.record && title.equals(etiq.getString("Results"))) {
            Recorder.record("String.copyResults");
        }
        if (selStart == -1 || selEnd == -1) {
            return copyAll();
        }
        StringBuffer sb = new StringBuffer();
        for (int i = selStart; i <= selEnd; i++) {
            char[] chars = (char[]) (vData.elementAt(i));
            sb.append(chars);
            if (i < selEnd || selEnd > selStart) {
                sb.append('\n');
            }
        }
        String s = new String(sb);
        Clipboard clip = getToolkit().getSystemClipboard();
        if (clip == null) {
            return 0;
        }
        StringSelection cont = new StringSelection(s);
        clip.setContents(cont, this);
        if (s.length() > 0) {
            //EU_HOU Bundle
            IJ.showStatus((selEnd - selStart + 1) + " lines copied to clipboard");
            if (this.getParent() instanceof ImageJ) {
                Analyzer.setUnsavedMeasurements(false);
            }
        }
        return s.length();
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    int copyAll() {
        selectAll();
        int count = selEnd - selStart;
        if (count > 0) {
            copySelection();
        }
        resetSelection();
        unsavedLines = false;
        return count;
    }

    /**
     *  Description of the Method
     */
    void cutSelection() {
        if (selStart == -1 || selEnd == -1) {
            selectAll();
        }
        copySelection();
        clearSelection();
    }

    /**
     *  Deletes the selected lines.
     */
    public void clearSelection() {
        if (selStart == -1 || selEnd == -1) {
            if (getLineCount() > 0) {
                //EU_HOU Bundle
                IJ.error("Selection required");
            }
            return;
        }
        
        // FIXME remove also in Photometry if needed
        if (selStart == 0 && selEnd == (iRowCount - 1)) {
             // FIXME remove also in Photometry
            Photometer photo=Photometer.getInstance();
            photo.getPS().results.clear();
            vData.removeAllElements();
            iRowCount = 0;
            if (rt != null) {
                if (IJ.isResultsWindow() && IJ.getTextPanel() == this) {
                    Analyzer.setUnsavedMeasurements(false);
                    Analyzer.resetCounter();
                } else {
                    rt.reset();
                }
            }
        } else {
            int rowCount = iRowCount;
            boolean atEnd = rowCount - selEnd < 8;
            int count = selEnd - selStart + 1;
            for (int i = 0; i < count; i++) {
                vData.removeElementAt(selStart);
                iRowCount--;
            }
            if (rt != null && rowCount == rt.getCounter()) {
                for (int i = 0; i < count; i++) {
                    rt.deleteRow(selStart);
                }
                rt.show(title);
                if (!atEnd) {
                    iY = 0;
                    tc.repaint();
                }
            }
        }
        selStart = -1;
        selEnd = -1;
        selOrigin = -1;
        selLine = -1;
        adjustVScroll();
        tc.repaint();
    }

    /**
     *  Deletes all the lines.
     */
    public void clear() {
        if (vData == null) {
            return;
        }
        vData.removeAllElements();
        iRowCount = 0;
        selStart = -1;
        selEnd = -1;
        selOrigin = -1;
        selLine = -1;
        adjustVScroll();
        tc.repaint();
    }

    /**
     *  Selects all the lines in this TextPanel.
     */
    public void selectAll() {
        if (selStart == 0 && selEnd == iRowCount - 1) {
            resetSelection();
            return;
        }
        selStart = 0;
        selEnd = iRowCount - 1;
        selOrigin = 0;
        tc.repaint();
        selLine = -1;
    }

    /**
     *  Clears the selection, if any.
     */
    public void resetSelection() {
        selStart = -1;
        selEnd = -1;
        selOrigin = -1;
        selLine = -1;
        if (iRowCount > 0) {
            tc.repaint();
        }
    }

    /**
     *  Writes all the text in this TextPanel to a file.
     *
     *@param  pw  Description of the Parameter
     */
    public void save(PrintWriter pw) {
        resetSelection();
        if (labels != null && !labels.equals("")) {
            pw.println(labels);
        }
        for (int i = 0; i < iRowCount; i++) {
            char[] chars = (char[]) (vData.elementAt(i));
            pw.println(new String(chars));
        }
        unsavedLines = false;
    }

    /**
     *  Saves all the text in this TextPanel to a file. Set 'path' to "" to display
     *  a save as dialog. Returns 'false' if the user cancels the save as dialog.
     *
     *@param  path  Description of the Parameter
     *@return       Description of the Return Value
     */
    public boolean saveAs(String path) {
        boolean isResults = IJ.isResultsWindow() && IJ.getTextPanel() == this;
        if (path.equals("")) {
            IJ.wait(10);
            //EU_HOU Bundle
            String ext = ".txt";
            if (isResults || isPhotometry) {
                ext = ".xls";
            }
            //ext = isResults ? Prefs.get("options.ext", ".xls") : ".txt";
            //EU_HOU Bundle
            SaveDialog sd = new SaveDialog(etiq.getString("SaveAsText"), title, ext);
            String file = sd.getFileName();
            if (file == null) {
                return false;
            }
            path = sd.getDirectory() + file;
        }
        PrintWriter pw = null;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            pw = new PrintWriter(bos);
        } catch (IOException e) {
            //IJ.write("" + e);
            return true;
        }
        save(pw);
        pw.close();
        if (isResults) {
            Analyzer.setUnsavedMeasurements(false);
            if (Recorder.record) {
                Recorder.record("saveAs", "Measurements", path);
            }
        } else {
            if (Recorder.record) {
                Recorder.record("saveAs", "Text", path);
            }
        }
        IJ.showStatus("");
        return true;
    }

    /**
     *  Returns all the text as a string.
     *
     *@return    The text value
     */
    public String getText() {
        StringBuffer sb = new StringBuffer();
        if (labels != null && !labels.equals("")) {
            sb.append(labels);
            sb.append('\n');
        }
        for (int i = 0; i < iRowCount; i++) {
            char[] chars = (char[]) (vData.elementAt(i));
            sb.append(chars);
            sb.append('\n');
        }
        return new String(sb);
    }

    /**
     *  Sets the title attribute of the TextPanel object
     *
     *@param  title  The new title value
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *  Returns the number of lines of text in this TextPanel.
     *
     *@return    The lineCount value
     */
    public int getLineCount() {
        return iRowCount;
    }

    /**
     *  Returns the specified line as a string. The argument must be greater than
     *  or equal to zero and less than the value returned by getLineCount().
     *
     *@param  index  Description of the Parameter
     *@return        The line value
     */
    public String getLine(int index) {
        if (index < 0 || index >= iRowCount) {
            //EU_HOU Bundle
            throw new IllegalArgumentException("index out of range: " + index);
        }
        return new String((char[]) (vData.elementAt(index)));
    }

    /**
     *  Replaces the contents of the specified line, where 'index' must be greater
     *  than or equal to zero and less than the value returned by getLineCount().
     *
     *@param  index  The new line value
     *@param  s      The new line value
     */
    public void setLine(int index, String s) {
        if (index < 0 || index >= iRowCount) {
            //EU_HOU Bundle
            throw new IllegalArgumentException("index out of range: " + index);
        }
        if (vData != null) {
            vData.setElementAt(s.toCharArray(), index);
            tc.repaint();
        }
    }

    /**
     *  Returns the index of the first selected line, or -1 if there is no
     *  slection.
     *
     *@return    The selectionStart value
     */
    public int getSelectionStart() {
        return selStart;
    }

    /**
     *  Returns the index of the last selected line, or -1 if there is no slection.
     *
     *@return    The selectionEnd value
     */
    public int getSelectionEnd() {
        return selEnd;
    }

    /**
     *  Sets the ResultsTable associated with this TextPanel.
     *
     *@param  rt  The new resultsTable value
     */
    public void setResultsTable(ResultsTable rt) {
        this.rt = rt;
    }

    /**
     *  Description of the Method
     */
    void flush() {
        if (vData != null) {
            vData.removeAllElements();
        }
        vData = null;
    }
}
