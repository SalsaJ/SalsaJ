package ij.plugin.frame;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.awt.List;
import java.util.zip.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;
import ij.plugin.filter.*;
import ij.util.*;
import ij.macro.*;
import ij.measure.*;

/**
 *  This plugin implements the Analyze/Tools/ROI Manager command.
 *
 *@author     Thomas
 *@created    20 novembre 2007
 */
public class RoiManager extends PlugInFrame implements ActionListener, ItemListener, MouseListener, MouseWheelListener {

    final static int BUTTONS = 10;
    final static int DRAW = 0, FILL = 1, LABEL = 2;
    final static int MENU = 0, COMMAND = 1, MULTI = 2;
    static int rows = 15;
    static boolean allowMultipleSelections = true;
    Panel panel;
    static Frame instance;
    java.awt.List list;
    Hashtable rois = new Hashtable();
    Roi roiCopy;
    boolean canceled;
    boolean macro;
    boolean ignoreInterrupts;
    PopupMenu pm;
    Button moreButton, colorButton;
    static boolean measureAll = true;
    static boolean onePerSlice = true;

    // EUHOU
    ResourceBundle bundle=IJ.gettToolBundle();

    /**
     *  Constructor for the RoiManager object
     */
    public RoiManager() {
        //EU_HOU Bundle
        super("ROI Manager");
        if (instance != null) {
            instance.toFront();
            return;
        }
        instance = this;
        list = new List(rows, allowMultipleSelections);
        showWindow();
    }

    /**
     *  Constructor for the RoiManager object
     *
     *@param  hideWindow  Description of the Parameter
     */
    public RoiManager(boolean hideWindow) {
        //EU_HOU Bundle
        super("ROI Manager");
        list = new List(rows, allowMultipleSelections);
    }

    /**
     *  Description of the Method
     */
    void showWindow() {
        ImageJ ij = IJ.getInstance();

        addKeyListener(ij);
        addMouseListener(this);
        addMouseWheelListener(this);
        WindowManager.addWindow(this);
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        list.add("012345678901234");
        list.addItemListener(this);
        list.addKeyListener(ij);
        list.addMouseListener(this);
        list.addMouseWheelListener(this);
        if (IJ.isLinux()) {
            list.setBackground(Color.white);
        }
        add(list);
        panel = new Panel();

        int nButtons = BUTTONS;

        panel.setLayout(new GridLayout(nButtons, 1, 5, 0));
        addButton("RM_Add");
        addButton("RM_Update");
        addButton("RM_Delete");
        addButton("RM_Rename");
        addButton("RM_Open");
        addButton("RM_Save");
        addButton("RM_Measure");
        addButton("RM_Deselect");
        addButton("RM_Show_All");
        // EUHOU
        //addButton("More >>");
        add(panel);
        addPopupMenu();
        pack();
        list.remove(0);
        GUI.center(this);
        show();
    }

    /**
     *  Adds a feature to the Button attribute of the RoiManager object
     *
     *@param  label  The feature to be added to the Button attribute
     */
    void addButton(String label) {
        Button b = new Button(bundle.getString(label));
        b.addActionListener(this);
        b.addKeyListener(IJ.getInstance());
        b.addMouseListener(this);
        //EU_HOU Bundle
        b.setActionCommand(label);
        if (label.equals("More >>")) {
            moreButton = b;
        }
        panel.add(b);
    }

    /**
     *  Adds a feature to the PopupMenu attribute of the RoiManager object
     */
    void addPopupMenu() {
        pm = new PopupMenu();
        //addPopupItem("Select All")
        //EU_HOU Bundle
        addPopupItem("RM_Draw");
        //EU_HOU Bundle
        addPopupItem("RM_Fill");
        //EU_HOU Bundle
        addPopupItem("RM_Label");

        pm.addSeparator();
        //EU_HOU Bundle
        addPopupItem("RM_Combine");
        //EU_HOU Bundle
        addPopupItem("RM_Split");
        //EU_HOU Bundle
        addPopupItem("RM_Add_Particles");
        //EU_HOU Bundle
        addPopupItem("RM_Multi_Measure");
        //EU_HOU Bundle
        addPopupItem("Sort");
        //EU_HOU Bundle
        addPopupItem("Specify...");
        //EU_HOU Bundle
        addPopupItem("RM_Remove_Slice_Info");
        //EU_HOU Bundle
        addPopupItem("RM_Help");
        //EU_HOU Bundle
        addPopupItem("RM_Options...");
        add(pm);
    }

    /**
     *  Adds a feature to the PopupItem attribute of the RoiManager object
     *
     *@param  s  The feature to be added to the PopupItem attribute
     */
    void addPopupItem(String s) {
        // EUHOU NOT IMPLEMENTED
        MenuItem mi = new MenuItem(s);
        mi.setActionCommand(s);

        mi.addActionListener(this);
        pm.add(mi);
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void actionPerformed(ActionEvent e) {
        int modifiers = e.getModifiers();
        boolean altKeyDown = (modifiers & ActionEvent.ALT_MASK) != 0 || IJ.altKeyDown();
        boolean shiftKeyDown = (modifiers & ActionEvent.SHIFT_MASK) != 0 || IJ.shiftKeyDown();

        IJ.setKeyUp(KeyEvent.VK_ALT);
        IJ.setKeyUp(KeyEvent.VK_SHIFT);

        String label = e.getActionCommand();

        if (label == null) {
            return;
        }
        String command = label;
        if (command.equals("RM_Add")) {
            add(shiftKeyDown, altKeyDown);
        } else if (command.equals("RM_Update")) {
            update();
        } else if (command.equals("RM_Delete")) {
            delete(false);
        } else if (command.equals("RM_Rename")) {
            rename(null);
        } else if (command.equals("RM_Open")) {
            open(null);
        } else if (command.equals("RM_Save")) {
            save();
        } else if (command.equals("RM_Measure")) {
            measure(MENU);
        } else if (command.equals("RM_Show_All")) {
            showAll();
        } else if (command.equals("RM_Draw")) {
            drawOrFill(DRAW);
        } else if (command.equals("RM_Fill")) {
            drawOrFill(FILL);
        } else if (command.equals("RM_Label")) {
            drawOrFill(LABEL);
        } else if (command.equals("RM_Deselect")) {
            select(-1);
        } else if (command.equals("RM_More >>")) {
            Point ploc = panel.getLocation();
            Point bloc = moreButton.getLocation();

            pm.show(this, ploc.x, bloc.y);
        } else if (command.equals("RM_Select_All")) {
            selectAll();
        } else if (command.equals("RM_Combine")) {
            combine();
        } else if (command.equals("RM_Split")) {
            split();
        } else if (command.equals("RM_Add_Particles")) {
            addParticles();
        } else if (command.equals("RM_Multi_Measure")) {
            multiMeasure();
        } else if (command.equals("RM_Sort")) {
            sort();
        } else if (command.equals("RM_Specify...")) {
            specify();
        } else if (command.equals("RM_Remove_Slice_Info")) {
            removeSliceInfo();
        } else if (command.equals("RM_Help")) {
            help();
        } else if (command.equals("RM_Options...")) {
            options();
        } else if (command.equals("RM_Set_Color...")) {
            setShowAllColor();
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void itemStateChanged(ItemEvent e) {
        //IJ.log("itemStateChanged: "+e.getItem().toString()+"  "+e+"  "+ignoreInterrupts);
        if (e.getStateChange() == ItemEvent.SELECTED && !ignoreInterrupts) {
            int index = 0;

            try {
                index = Integer.parseInt(e.getItem().toString());
            } catch (NumberFormatException ex) {
            }
            if (index < 0) {
                index = 0;
            }
            if (!IJ.shiftKeyDown() && !IJ.isMacintosh()) {
                int[] indexes = list.getSelectedIndexes();

                for (int i = 0; i < indexes.length; i++) {
                    if (indexes[i] != index) {
                        list.deselect(indexes[i]);
                    }
                }
            }
            if (WindowManager.getCurrentImage() != null) {
                restore(index, true);
                if (Recorder.record) {
                    Recorder.record("roiManager", "Select", index);
                }
            }
        }
    }

    /**
     *  Description of the Method
     *
     *@param  shiftKeyDown  Description of the Parameter
     *@param  altKeyDown    Description of the Parameter
     */
    void add(boolean shiftKeyDown, boolean altKeyDown) {
        if (shiftKeyDown) {
            addAndDraw(altKeyDown);
        } else if (altKeyDown) {
            add(true);
        } else {
            add(false);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  promptForName  Description of the Parameter
     *@return                Description of the Return Value
     */
    boolean add(boolean promptForName) {
        ImagePlus imp = getImage();

        if (imp == null) {
            return false;
        }
        Roi roi = imp.getRoi();

        if (roi == null) {
            //EU_HOU Bundle
            error("The active image does not have a selection.");
            return false;
        }
        int n = list.getItemCount();

        if (n > 0) {
            // check for duplicate
            String label = list.getItem(n - 1);
            Roi roi2 = (Roi) rois.get(label);

            if (roi2 != null) {
                int slice2 = getSliceNumber(label);

                if (roi.equals(roi2) && (slice2 == -1 || slice2 == imp.getCurrentSlice())) {
                    return false;
                }
            }
        }
        String name = roi.getName();

        if (isStandardName(name)) {
            name = null;
        }
        String label = name != null ? name : getLabel(imp, roi, -1);

        if (promptForName) {
            label = promptForName(label);
        } else {
            label = getUniqueName(label);
        }
        if (label == null) {
            return false;
        }
        list.add(label);
        roi.setName(label);
        roiCopy = (Roi) roi.clone();

        Calibration cal = imp.getCalibration();

        if (cal.xOrigin != 0.0 || cal.yOrigin != 0.0) {
            Rectangle r = roiCopy.getBounds();

            roiCopy.setLocation(r.x - (int) cal.xOrigin, r.y - (int) cal.yOrigin);
        }
        rois.put(label, roiCopy);
        updateShowAll();
        if (Recorder.record) {
            Recorder.record("roiManager", "Add");
        }
        return true;
    }

    /**
     *  Adds the specified ROI to the list. The third argument ('n') will be used
     *  form the first part of the ROI lable if it is >= 0.
     *
     *@param  imp  Description of the Parameter
     *@param  roi  Description of the Parameter
     *@param  n    Description of the Parameter
     */
    public void add(ImagePlus imp, Roi roi, int n) {
        if (roi == null) {
            return;
        }
        String label = getLabel(imp, roi, n);

        if (label == null) {
            return;
        }
        list.add(label);
        roi.setName(label);
        roiCopy = (Roi) roi.clone();

        Calibration cal = imp.getCalibration();

        if (cal.xOrigin != 0.0 || cal.yOrigin != 0.0) {
            Rectangle r = roiCopy.getBounds();

            roiCopy.setLocation(r.x - (int) cal.xOrigin, r.y - (int) cal.yOrigin);
        }
        rois.put(label, roiCopy);
    }

    /**
     *  Gets the standardName attribute of the RoiManager object
     *
     *@param  name  Description of the Parameter
     *@return       The standardName value
     */
    boolean isStandardName(String name) {
        if (name == null) {
            return false;
        }
        boolean isStandard = false;
        int len = name.length();

        if (len >= 14 && name.charAt(4) == '-' && name.charAt(9) == '-') {
            isStandard = true;
        } else if (len >= 9 && name.charAt(4) == '-') {
            isStandard = true;
        }
        return isStandard;
    }

    /**
     *  Gets the label attribute of the RoiManager object
     *
     *@param  imp  Description of the Parameter
     *@param  roi  Description of the Parameter
     *@param  n    Description of the Parameter
     *@return      The label value
     */
    String getLabel(ImagePlus imp, Roi roi, int n) {
        Rectangle r = roi.getBounds();
        int xc = r.x + r.width / 2;
        int yc = r.y + r.height / 2;

        if (n >= 0) {
            xc = yc;
            yc = n;
        }
        if (xc < 0) {
            xc = 0;
        }
        if (yc < 0) {
            yc = 0;
        }
        int digits = 4;
        String xs = "" + xc;

        if (xs.length() > digits) {
            digits = xs.length();
        }
        String ys = "" + yc;

        if (ys.length() > digits) {
            digits = ys.length();
        }
        xs = "000000" + xc;
        ys = "000000" + yc;

        String label = ys.substring(ys.length() - digits) + "-" + xs.substring(xs.length() - digits);

        if (imp.getStackSize() > 1) {
            String zs = "000000" + imp.getCurrentSlice();

            label = zs.substring(zs.length() - digits) + "-" + label;
        }
        return label;
    }

    /**
     *  Adds a feature to the AndDraw attribute of the RoiManager object
     *
     *@param  altKeyDown  The feature to be added to the AndDraw attribute
     */
    void addAndDraw(boolean altKeyDown) {
        if (altKeyDown) {
            if (!add(true)) {
                return;
            }
        } else if (!add(false)) {
            return;
        }
        ImagePlus imp = WindowManager.getCurrentImage();

        Undo.setup(Undo.COMPOUND_FILTER, imp);
        IJ.run("Draw");
        Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
        if (Recorder.record) {
            Recorder.record("roiManager", "Add & Draw");
        }
    }

    /**
     *  Description of the Method
     *
     *@param  replacing  Description of the Parameter
     *@return            Description of the Return Value
     */
    boolean delete(boolean replacing) {
        int count = list.getItemCount();

        if (count == 0) {
            //EU_HOU Bundle
            return error("The list is empty.");
        }
        int index[] = list.getSelectedIndexes();

        if (index.length == 0 || (replacing && count > 1)) {
            //EU_HOU Bundle
            String msg = "Delete all items on the list?";

            if (replacing) {
                //EU_HOU Bundle
                msg = "Replace items on the list?";
            }
            canceled = false;
            if (!IJ.macroRunning() && !macro) {
                //EU_HOU Bundle
                YesNoCancelDialog d = new YesNoCancelDialog(this, "ROI Manager", msg);

                if (d.cancelPressed()) {
                    canceled = true;
                    return false;
                }
                if (!d.yesPressed()) {
                    return false;
                }
            }
            index = getAllIndexes();
        }
        for (int i = count - 1; i >= 0; i--) {
            boolean delete = false;

            for (int j = 0; j < index.length; j++) {
                if (index[j] == i) {
                    delete = true;
                }
            }
            if (delete) {
                rois.remove(list.getItem(i));
                list.remove(i);
            }
        }
        updateShowAll();
        if (Recorder.record) {
            Recorder.record("roiManager", "Delete");
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    boolean update() {
        ImagePlus imp = getImage();

        if (imp == null) {
            return false;
        }
        ImageCanvas ic = imp.getCanvas();
        boolean showingAll = ic != null && ic.getShowAllROIs();
        Roi roi = imp.getRoi();

        if (roi == null) {
            //EU_HOU Bundle
            error("The active image does not have a selection.");
            return false;
        }
        int index = list.getSelectedIndex();

        if (index < 0 && !showingAll) {
            //EU_HOU Bundle
            return error("Exactly one item in the list must be selected.");
        }
        if (index >= 0) {
            String name = list.getItem(index);

            rois.remove(name);
            rois.put(name, roi);
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Update");
        }
        if (showingAll) {
            imp.draw();
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@param  name2  Description of the Parameter
     *@return        Description of the Return Value
     */
    boolean rename(String name2) {
        int index = list.getSelectedIndex();

        if (index < 0) {
            //EU_HOU Bundle
            return error("Exactly one item in the list must be selected.");
        }
        String name = list.getItem(index);

        if (name2 == null) {
            name2 = promptForName(name);
        }
        if (name2 == null) {
            return false;
        }
        Roi roi = (Roi) rois.get(name);

        rois.remove(name);
        roi.setName(name2);
        rois.put(name2, roi);
        list.replaceItem(name2, index);
        list.select(index);
        if (Recorder.record) {
            Recorder.record("roiManager", "Rename", name2);
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */
    String promptForName(String name) {
        //EU_HOU Bundle
        GenericDialog gd = new GenericDialog("ROI Manager");
//EU_HOU Bundle
        gd.addStringField("Rename As:", name, 20);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return null;
        }
        String name2 = gd.getNextString();

        name2 = getUniqueName(name2);
        return name2;
    }

    /**
     *  Description of the Method
     *
     *@param  index     Description of the Parameter
     *@param  setSlice  Description of the Parameter
     *@return           Description of the Return Value
     */
    boolean restore(int index, boolean setSlice) {
        String label = list.getItem(index);
        Roi roi = (Roi) rois.get(label);
        ImagePlus imp = getImage();

        if (imp == null || roi == null) {
            return false;
        }
        if (setSlice) {
            int slice = getSliceNumber(label);

            if (slice >= 1 && slice <= imp.getStackSize()) {
                imp.setSlice(slice);
            }
        }
        Roi roi2 = (Roi) roi.clone();
        Calibration cal = imp.getCalibration();
        Rectangle r = roi2.getBounds();

        if (cal.xOrigin != 0.0 || cal.yOrigin != 0.0) {
            roi2.setLocation(r.x + (int) cal.xOrigin, r.y + (int) cal.yOrigin);
        }
        int width = imp.getWidth();
        int height = imp.getHeight();

        if (r.x >= width || r.y >= height || (r.x + r.width) <= 0 || (r.y + r.height) <= 0) {
            roi2.setLocation((width - r.width) / 2, (height - r.height) / 2);
        }
        imp.setRoi(roi2);
        return true;
    }

    /**
     *  Gets the sliceNumber attribute of the RoiManager object
     *
     *@param  label  Description of the Parameter
     *@return        The sliceNumber value
     */
    int getSliceNumber(String label) {
        int slice = -1;

        if (label.length() > 4 && label.charAt(4) == '-' && label.length() >= 14) {
            slice = (int) Tools.parseDouble(label.substring(0, 4), -1);
        }
        return slice;
    }

    /**
     *  Description of the Method
     *
     *@param  path  Description of the Parameter
     */
    void open(String path) {
        Macro.setOptions(null);

        String name = null;

        if (path == null || path.equals("")) {
            //EU_HOU Bundle
            OpenDialog od = new OpenDialog("Open Selection(s)...", "");
            String directory = od.getDirectory();

            name = od.getFileName();
            if (name == null) {
                return;
            }
            path = directory + name;
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Open", path);
        }
        if (path.endsWith(".zip")) {
            openZip(path);
            return;
        }
        Opener o = new Opener();

        if (name == null) {
            name = o.getName(path);
        }
        Roi roi = o.openRoi(path);

        if (roi != null) {
            if (name.endsWith(".roi")) {
                name = name.substring(0, name.length() - 4);
            }
            name = getUniqueName(name);
            list.add(name);
            rois.put(name, roi);
        }
        updateShowAll();
    }

    // Modified on 2005/11/15 by Ulrik Stervbo to only read .roi files and to not empty the current list
    /**
     *  Description of the Method
     *
     *@param  path  Description of the Parameter
     */
    void openZip(String path) {
        ZipInputStream in = null;
        ByteArrayOutputStream out;
        int nRois = 0;

        try {
            in = new ZipInputStream(new FileInputStream(path));

            byte[] buf = new byte[1024];
            int len;
            ZipEntry entry = in.getNextEntry();

            while (entry != null) {
                String name = entry.getName();

                if (name.endsWith(".roi")) {
                    out = new ByteArrayOutputStream();
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();

                    byte[] bytes = out.toByteArray();
                    RoiDecoder rd = new RoiDecoder(bytes, name);
                    Roi roi = rd.getRoi();

                    if (roi != null) {
                        name = name.substring(0, name.length() - 4);
                        name = getUniqueName(name);
                        list.add(name);
                        rois.put(name, roi);
                        nRois++;
                    }
                }
                entry = in.getNextEntry();
            }
            in.close();
        } catch (IOException e) {
            error(e.toString());
        }
        if (nRois == 0) {
            //EU_HOU Bundle
            error("This ZIP archive does not appear to contain \".roi\" files");
        }
        updateShowAll();
    }

    /**
     *  Gets the uniqueName attribute of the RoiManager object
     *
     *@param  name  Description of the Parameter
     *@return       The uniqueName value
     */
    String getUniqueName(String name) {
        String name2 = name;
        int n = 1;
        Roi roi2 = (Roi) rois.get(name2);

        while (roi2 != null) {
            roi2 = (Roi) rois.get(name2);
            if (roi2 != null) {
                int lastDash = name2.lastIndexOf("-");

                if (lastDash != -1 && name2.length() - lastDash < 5) {
                    name2 = name2.substring(0, lastDash);
                }
                name2 = name2 + "-" + n;
                n++;
            }
            roi2 = (Roi) rois.get(name2);
        }
        return name2;
    }

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    boolean save() {
        if (list.getItemCount() == 0) {
            //EU_HOU Bundle
            return error("The selection list is empty.");
        }
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length > 1) {
            return saveMultiple(indexes, null);
        }
        String name = list.getItem(indexes[0]);

        Macro.setOptions(null);
//EU_HOU Bundle
        SaveDialog sd = new SaveDialog("Save Selection...", name, ".roi");
        String name2 = sd.getFileName();

        if (name2 == null) {
            return false;
        }
        String dir = sd.getDirectory();
        Roi roi = (Roi) rois.get(name);

        rois.remove(name);
        if (!name2.endsWith(".roi")) {
            name2 = name2 + ".roi";
        }
        String newName = name2.substring(0, name2.length() - 4);

        rois.put(newName, roi);
        roi.setName(newName);
        list.replaceItem(newName, indexes[0]);

        RoiEncoder re = new RoiEncoder(dir + name2);

        try {
            re.write(roi);
        } catch (IOException e) {
            //EU_HOU Bundle
            IJ.error("ROI Manager", e.getMessage());
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@param  indexes  Description of the Parameter
     *@param  path     Description of the Parameter
     *@return          Description of the Return Value
     */
    boolean saveMultiple(int[] indexes, String path) {
        Macro.setOptions(null);
        if (path == null) {
            //EU_HOU Bundle
            SaveDialog sd = new SaveDialog("Save ROIs...", "RoiSet", ".zip");
            String name = sd.getFileName();

            if (name == null) {
                return false;
            }
            if (!(name.endsWith(".zip") || name.endsWith(".ZIP"))) {
                name = name + ".zip";
            }
            String dir = sd.getDirectory();

            path = dir + name;
        }
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(zos));
            RoiEncoder re = new RoiEncoder(out);

            for (int i = 0; i < indexes.length; i++) {
                String label = list.getItem(indexes[i]);
                Roi roi = (Roi) rois.get(label);

                if (!label.endsWith(".roi")) {
                    label += ".roi";
                }
                zos.putNextEntry(new ZipEntry(label));
                re.write(roi);
                out.flush();
            }
            out.close();
        } catch (IOException e) {
            error("" + e);
            return false;
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Save", path);
        }
        return true;
    }

    /**
     *  Description of the Method
     *
     *@param  mode  Description of the Parameter
     *@return       Description of the Return Value
     */
    boolean measure(int mode) {
        ImagePlus imp = getImage();

        if (imp == null) {
            return false;
        }
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length == 0) {
            return false;
        }

        int nLines = 0;
        boolean allSliceOne = true;

        for (int i = 0; i < indexes.length; i++) {
            String label = list.getItem(indexes[i]);

            if (getSliceNumber(label) > 1) {
                allSliceOne = false;
            }
            Roi roi = (Roi) rois.get(label);

            if (roi.isLine()) {
                nLines++;
            }
        }
        if (nLines > 0 && nLines != indexes.length) {
            //EU_HOU Bundle
            error("All items must be areas or all must be lines.");
            return false;
        }

        int nSlices = 1;

        if (mode == MULTI) {
            nSlices = imp.getStackSize();
        }
        int measurements = Analyzer.getMeasurements();

        if (imp.getStackSize() > 1) {
            Analyzer.setMeasurements(measurements | Measurements.SLICE);
        }
        int currentSlice = imp.getCurrentSlice();

        for (int slice = 1; slice <= nSlices; slice++) {
            if (nSlices > 1) {
                imp.setSlice(slice);
            }
            for (int i = 0; i < indexes.length; i++) {
                if (restore(indexes[i], nSlices == 1 && !allSliceOne)) {
                    IJ.run("Measure");
                } else {
                    break;
                }
            }
        }
        imp.setSlice(currentSlice);
        Analyzer.setMeasurements(measurements);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Measure");
        }
        return true;
    }


    /*
     *  This method performs measurements for several ROI's in a stack
     *  and arranges the results with one line per slice.  By constast, the
     *  measure() method produces several lines per slice.  The results
     *  from multiMeasure() may be easier to import into a spreadsheet
     *  program for plotting or additional analysis. Based on the multi()
     *  method in Bob Dougherty's Multi_Measure plugin
     *  (http://www.optinav.com/Multi-Measure.htm).
     */
    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    boolean multiMeasure() {
        ImagePlus imp = getImage();

        if (imp == null) {
            return false;
        }
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        if (indexes.length == 0) {
            return false;
        }
        int measurements = Analyzer.getMeasurements();

        int nSlices = imp.getStackSize();
        //EU_HOU Bundle
        GenericDialog gd = new GenericDialog("Multi Measure");

        if (nSlices > 1) {
            //EU_HOU Bundle
            gd.addCheckbox("Measure All " + nSlices + " Slices", measureAll);
        }
        //EU_HOU Bundle
        gd.addCheckbox("One Row Per Slice", onePerSlice);

        int columns = getColumnCount(imp, measurements) * indexes.length;
        //EU_HOU Bundle
        String str = nSlices == 1 ? "this option" : "both options";

        gd.setInsets(10, 25, 0);
        gd.addMessage(
                //EU_HOU Bundle
                "Enabling " + str + " will result\n"
                + "in a table with " + columns + " columns.");
        gd.showDialog();
        if (gd.wasCanceled()) {
            return false;
        }
        if (nSlices > 1) {
            measureAll = gd.getNextBoolean();
        }
        onePerSlice = gd.getNextBoolean();
        if (!measureAll) {
            nSlices = 1;
        }
        int currentSlice = imp.getCurrentSlice();

        if (!onePerSlice) {
            return measure(MULTI);
        }

        Analyzer aSys = new Analyzer();//System Analyzer
        ResultsTable rtSys = Analyzer.getResultsTable();
        ResultsTable rtMulti = new ResultsTable();
        Analyzer aMulti = new Analyzer(imp, measurements, rtMulti);//Private Analyzer

        for (int slice = 1; slice <= nSlices; slice++) {
            int sliceUse = slice;

            if (nSlices == 1) {
                sliceUse = currentSlice;
            }
            imp.setSlice(sliceUse);
            rtMulti.incrementCounter();

            int roiIndex = 0;

            for (int i = 0; i < indexes.length; i++) {
                if (restore(indexes[i], false)) {
                    roiIndex++;

                    Roi roi = imp.getRoi();
                    ImageStatistics stats = imp.getStatistics(measurements);

                    aSys.saveResults(stats, roi);//Save measurements in system results table;
                    for (int j = 0; j <= rtSys.getLastColumn(); j++) {
                        float[] col = rtSys.getColumn(j);
                        String head = rtSys.getColumnHeading(j);

                        if (head != null && col != null && !head.equals("Slice")) {
                            rtMulti.addValue(head + roiIndex, rtSys.getValue(j, rtSys.getCounter() - 1));
                        }
                    }
                } else {
                    break;
                }
            }
            aMulti.displayResults();
            aMulti.updateHeadings();
        }

        imp.setSlice(currentSlice);
        if (indexes.length > 1) {
            IJ.run("Select None");
        }
        return true;
    }

    /**
     *  Gets the columnCount attribute of the RoiManager object
     *
     *@param  imp           Description of the Parameter
     *@param  measurements  Description of the Parameter
     *@return               The columnCount value
     */
    int getColumnCount(ImagePlus imp, int measurements) {
        ImageStatistics stats = imp.getStatistics(measurements);
        ResultsTable rt = new ResultsTable();
        Analyzer analyzer = new Analyzer(imp, measurements, rt);

        analyzer.saveResults(stats, null);

        int count = 0;

        for (int i = 0; i <= rt.getLastColumn(); i++) {
            float[] col = rt.getColumn(i);
            String head = rt.getColumnHeading(i);

            if (head != null && col != null) {
                count++;
            }
        }
        return count;
    }

    /**
     *  Description of the Method
     *
     *@param  mode  Description of the Parameter
     *@return       Description of the Return Value
     */
    boolean drawOrFill(int mode) {
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ImagePlus imp = WindowManager.getCurrentImage();

        imp.killRoi();

        ImageProcessor ip = imp.getProcessor();

        ip.setColor(Toolbar.getForegroundColor());
        ip.snapshot();
        Undo.setup(Undo.FILTER, imp);

        Filler filler = mode == LABEL ? new Filler() : null;
        int slice = imp.getCurrentSlice();

        for (int i = 0; i < indexes.length; i++) {
            String name = list.getItem(i);
            Roi roi = (Roi) rois.get(name);
            int type = roi.getType();

            if (roi == null) {
                continue;
            }
            if (mode == FILL && (type == Roi.POLYLINE || type == Roi.FREELINE || type == Roi.ANGLE)) {
                mode = DRAW;
            }
            int slice2 = getSliceNumber(name);

            if (slice2 >= 1 && slice2 <= imp.getStackSize()) {
                imp.setSlice(slice2);
                ip = imp.getProcessor();
                ip.setColor(Toolbar.getForegroundColor());
                if (slice2 != slice) {
                    Undo.reset();
                }
            }
            switch (mode) {
                case DRAW:
                    roi.drawPixels(ip);
                    break;
                case FILL:
                    ip.fillPolygon(roi.getPolygon());
                    break;
                case LABEL:
                    roi.drawPixels(ip);
                    filler.drawLabel(imp, ip, i + 1, roi.getBounds());
                    break;
            }
        }

        ImageCanvas ic = imp.getCanvas();

        if (ic != null) {
            ic.setShowAllROIs(false);
        }
        imp.updateAndDraw();

        String str = null;

        switch (mode) {
            case DRAW:
                //EU_HOU Bundle
                str = "Draw";
                break;
            case FILL:
                //EU_HOU Bundle
                str = "Fill";
                break;
            case LABEL:
                //EU_HOU Bundle
                str = "Label";
                imp.updateAndDraw();
                break;
        }
        if (Recorder.record) {
            Recorder.record("roiManager", str);
        }
        return true;
    }

    /**
     *  Description of the Method
     */
    void combine() {
        ImagePlus imp = getImage();

        if (imp == null) {
            return;
        }
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 1) {
            //EU_HOU Bundle
            error("More than one item must be selected, or none");
            return;
        }
        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        ShapeRoi s1 = null;
        ShapeRoi s2 = null;

        for (int i = 0; i < indexes.length; i++) {
            Roi roi = (Roi) rois.get(list.getItem(indexes[i]));

            if (roi.isLine() || roi.getType() == Roi.POINT) {
                continue;
            }
            Calibration cal = imp.getCalibration();

            if (cal.xOrigin != 0.0 || cal.yOrigin != 0.0) {
                roi = (Roi) roi.clone();

                Rectangle r = roi.getBounds();

                roi.setLocation(r.x + (int) cal.xOrigin, r.y + (int) cal.yOrigin);
            }
            if (s1 == null) {
                if (roi instanceof ShapeRoi) {
                    s1 = (ShapeRoi) roi;
                } else {
                    s1 = new ShapeRoi(roi);
                }
                if (s1 == null) {
                    return;
                }
            } else {
                if (roi instanceof ShapeRoi) {
                    s2 = (ShapeRoi) roi;
                } else {
                    s2 = new ShapeRoi(roi);
                }
                if (s2 == null) {
                    continue;
                }
                if (roi.isArea()) {
                    s1.or(s2);
                }
            }
        }
        if (s1 != null) {
            imp.setRoi(s1);
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Combine");
        }
    }

    /**
     *  Adds a feature to the Particles attribute of the RoiManager object
     */
    void addParticles() {
        String err = IJ.runMacroFile("ij.jar:AddParticles", null);

        if (err != null && err.length() > 0) {
            error(err);
        }
    }

    /**
     *  Description of the Method
     */
    void sort() {
        int n = rois.size();

        if (n == 0) {
            return;
        }
        String[] labels = new String[n];
        int index = 0;

        for (Enumeration en = rois.keys(); en.hasMoreElements();) {
            labels[index++] = (String) en.nextElement();
        }
        list.removeAll();
        StringSorter.sort(labels);
        for (int i = 0; i < labels.length; i++) {
            list.add(labels[i]);
        }
        if (Recorder.record) {
            Recorder.record("roiManager", "Sort");
        }
    }

    /**
     *  Description of the Method
     */
    void specify() {
        try {
            IJ.run("Specify...");
        } catch (Exception e) {
            return;
        }
        runCommand("add");
    }

    /**
     *  Description of the Method
     */
    void removeSliceInfo() {
        int[] indexes = list.getSelectedIndexes();

        if (indexes.length == 0) {
            indexes = getAllIndexes();
        }
        for (int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            String name = list.getItem(index);
            int n = getSliceNumber(name);

            if (n == -1) {
                continue;
            }
            String name2 = name.substring(5, name.length());

            name2 = getUniqueName(name2);

            Roi roi = (Roi) rois.get(name);

            rois.remove(name);
            roi.setName(name2);
            rois.put(name2, roi);
            list.replaceItem(name2, index);
        }
    }

    /**
     *  Description of the Method
     */
    void help() {
        String macro = "run('URL...', 'url=http://rsb.info.nih.gov/ij/docs/menus/analyze.html#manager');";

        new MacroRunner(macro);
    }

    /**
     *  Description of the Method
     */
    void options() {
        Color c = ImageCanvas.getShowAllColor();
        //EU_HOU Bundle
        GenericDialog gd = new GenericDialog("\"Show All\" Options");

        gd.addPanel(makeButtonPanel(gd), GridBagConstraints.CENTER, new Insets(5, 0, 0, 0));
        //EU_HOU Bundle
        gd.addCheckbox("Associate ROIs with Slices", Prefs.showAllSliceOnly);
        gd.showDialog();
        if (gd.wasCanceled()) {
            if (c != ImageCanvas.getShowAllColor()) {
                ImageCanvas.setShowAllColor(c);
            }
            return;
        }
        Prefs.showAllSliceOnly = gd.getNextBoolean();

        ImagePlus imp = WindowManager.getCurrentImage();

        if (imp != null) {
            imp.draw();
        }
    }

    /**
     *  Description of the Method
     *
     *@param  gd  Description of the Parameter
     *@return     Description of the Return Value
     */
    Panel makeButtonPanel(GenericDialog gd) {
        Panel panel = new Panel();
        //buttons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        //EU_HOU Bundle
        colorButton = new Button("Set Color...");
        colorButton.addActionListener(this);
        panel.add(colorButton);
        return panel;
    }

    /**
     *  Sets the showAllColor attribute of the RoiManager object
     */
    void setShowAllColor() {
        //EU_HOU Bundle
        ColorChooser cc = new ColorChooser("\"Show All\" Color", ImageCanvas.getShowAllColor(), false);

        ImageCanvas.setShowAllColor(cc.getColor());
    }

    /**
     *  Description of the Method
     */
    void split() {
        ImagePlus imp = getImage();

        if (imp == null) {
            return;
        }
        Roi roi = imp.getRoi();

        if (roi == null || roi.getType() != Roi.COMPOSITE) {
            //EU_HOU Bundle
            error("Image with composite selection required");
            return;
        }
        boolean record = Recorder.record;

        Recorder.record = false;

        Roi[] rois = ((ShapeRoi) roi).getRois();

        for (int i = 0; i < rois.length; i++) {
            imp.setRoi(rois[i]);
            add(false);
        }
        Recorder.record = record;
        if (Recorder.record) {
            //EU_HOU Bundle
            Recorder.record("roiManager", "Split");
        }
    }

    /**
     *  Description of the Method
     */
    void showAll() {
        ImagePlus imp = WindowManager.getCurrentImage();

        if (imp == null) {
            //EU_HOU Bundle
            error(IJ.getBundle().getString("NoImgErr"));
            return;
        }
        ImageCanvas ic = imp.getCanvas();

        if (ic == null) {
            return;
        }
        boolean showingROIs = ic.getShowAllROIs();

        ic.setShowAllROIs(!showingROIs);
        if (Recorder.record) {
            //EU_HOU Bundle
            Recorder.recordString("setOption(\"Show All\"," + (showingROIs ? "false" : "true") + ");\n");
        }
        imp.draw();
    }

    /**
     *  Description of the Method
     */
    void updateShowAll() {
        ImagePlus imp = WindowManager.getCurrentImage();

        if (imp == null) {
            return;
        }
        ImageCanvas ic = imp.getCanvas();

        if (ic != null && ic.getShowAllROIs()) {
            imp.draw();
        }
    }

    /**
     *  Gets the allIndexes attribute of the RoiManager object
     *
     *@return    The allIndexes value
     */
    int[] getAllIndexes() {
        int count = list.getItemCount();
        int[] indexes = new int[count];

        for (int i = 0; i < count; i++) {
            indexes[i] = i;
        }
        return indexes;
    }

    /**
     *  Gets the image attribute of the RoiManager object
     *
     *@return    The image value
     */
    ImagePlus getImage() {
        ImagePlus imp = WindowManager.getCurrentImage();

        if (imp == null) {
            //EU_HOU Bundle
            error(IJ.getBundle().getString("NoImgErr"));
            return null;
        } else {
            return imp;
        }
    }

    /**
     *  Description of the Method
     *
     *@param  msg  Description of the Parameter
     *@return      Description of the Return Value
     */
    boolean error(String msg) {
        //EU_HOU Bundle
        new MessageDialog(this, "ROI Manager", msg);
        Macro.abort();
        return false;
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            instance = null;
        }
        ignoreInterrupts = false;
    }

    /**
     *  Returns a reference to the ROI Manager or null if it is not open.
     *
     *@return    The instance value
     */
    public static RoiManager getInstance() {
        return (RoiManager) instance;
    }

    /**
     *  Returns the ROI Hashtable.
     *
     *@return    The rOIs value
     *@see       getCount
     *@see       getRoisAsArray
     */
    public Hashtable getROIs() {
        return rois;
    }

    /**
     *  Returns the selection list.
     *
     *@return    The list value
     *@see       getCount
     *@see       getRoisAsArray
     */
    public List getList() {
        return list;
    }

    /**
     *  Returns the ROI count.
     *
     *@return    The count value
     */
    public int getCount() {
        return list.getItemCount();
    }

    /**
     *  Returns the ROIs as an array.
     *
     *@return    The roisAsArray value
     */
    public Roi[] getRoisAsArray() {
        int n = list.getItemCount();
        Roi[] array = new Roi[n];

        for (int i = 0; i < n; i++) {
            String label = list.getItem(i);

            array[i] = (Roi) rois.get(label);
        }
        return array;
    }

    /**
     *  Returns the name of the selection with the specified index. Can be called
     *  from a macro using <pre>call("ij.plugin.frame.RoiManager.getName", index)</pre>
     *  Returns "null" if the Roi Manager is not open or index is out of range.
     *
     *@param  index  Description of the Parameter
     *@return        The name value
     */
    public static String getName(String index) {
        int i = (int) Tools.parseDouble(index, -1);
        RoiManager instance = getInstance();

        if (instance != null && i >= 0 && i < instance.list.getItemCount()) {
            return instance.list.getItem(i);
        } else {
            return "null";
        }
    }

    /**
     *  Executes the ROI Manager "Add", "Add & Draw", "Update", "Delete",
     *  "Measure", "Draw", "Fill", "Deselect", "Select All", "Combine", "Split" or
     *  "Sort" command. Returns false if <code>cmd</code> is not one of these
     *  strings.
     *
     *@param  cmd  Description of the Parameter
     *@return      Description of the Return Value
     */
    public boolean runCommand(String cmd) {
        cmd = cmd.toLowerCase();
        macro = true;

        boolean ok = true;

        if (cmd.equals("add")) {
            add(IJ.shiftKeyDown(), IJ.altKeyDown());
        } else if (cmd.equals("add & draw")) {
            addAndDraw(false);
        } else if (cmd.equals("update")) {
            update();
        } else if (cmd.equals("delete")) {
            delete(false);
        } else if (cmd.equals("measure")) {
            measure(COMMAND);
        } else if (cmd.equals("draw")) {
            drawOrFill(DRAW);
        } else if (cmd.equals("fill")) {
            drawOrFill(FILL);
        } else if (cmd.equals("label")) {
            drawOrFill(LABEL);
        } else if (cmd.equals("combine")) {
            combine();
        } else if (cmd.equals("split")) {
            split();
        } else if (cmd.equals("sort")) {
            sort();
        } else if (cmd.equals("deselect") || cmd.indexOf("all") != -1) {
            if (IJ.isMacOSX()) {
                ignoreInterrupts = true;
            }
            select(-1);
        } else if (cmd.equals("reset")) {
            list.removeAll();
            rois.clear();
        } else {
            ok = false;
        }
        macro = false;
        return ok;
    }

    /**
     *  Executes the ROI Manager "Open", "Save" or "Rename" command. Returns false
     *  if <code>cmd</code> is not "Open", "Save" or "Rename", or if an error
     *  occurs.
     *
     *@param  cmd   Description of the Parameter
     *@param  name  Description of the Parameter
     *@return       Description of the Return Value
     */
    public boolean runCommand(String cmd, String name) {
        cmd = cmd.toLowerCase();
        macro = true;
        if (cmd.equals("open")) {
            open(name);
            macro = false;
            return true;
        } else if (cmd.equals("save")) {
            if (!name.endsWith(".zip") && !name.equals("")) {
                //EU_HOU Bundle
                return error("Name must end with '.zip'");
            }
            if (list.getItemCount() == 0) {
                //EU_HOU Bundle
                return error("The selection list is empty.");
            }
            int[] indexes = getAllIndexes();
            boolean ok = false;

            if (name.equals("")) {
                ok = saveMultiple(indexes, null);
            } else {
                ok = saveMultiple(indexes, name);
            }
            macro = false;
            return ok;
        } else if (cmd.equals("rename")) {
            rename(name);
            macro = false;
            return true;
        }
        return false;
    }

    /**
     *  Description of the Method
     *
     *@param  index  Description of the Parameter
     */
    public void select(int index) {
        int n = list.getItemCount();

        if (index < 0) {
            for (int i = 0; i < n; i++) {
                if (list.isSelected(i)) {
                    list.deselect(i);
                }
            }
            return;
        }
        boolean mm = list.isMultipleMode();

        if (mm) {
            list.setMultipleMode(false);
        }
        if (index < n) {
            list.select(index);
            restore(index, true);
            if (!Interpreter.isBatchMode()) {
                IJ.wait(10);
            }
        }
        if (mm) {
            list.setMultipleMode(true);
        }
    }

    /**
     *  Description of the Method
     *
     *@param  index         Description of the Parameter
     *@param  shiftKeyDown  Description of the Parameter
     *@param  altKeyDown    Description of the Parameter
     */
    public void select(int index, boolean shiftKeyDown, boolean altKeyDown) {
        if (!(shiftKeyDown || altKeyDown)) {
            select(index);
        }
        ImagePlus imp = IJ.getImage();

        if (imp == null) {
            return;
        }
        Roi previousRoi = imp.getRoi();

        if (previousRoi == null) {
            select(index);
            return;
        }
        Roi.previousRoi = (Roi) previousRoi.clone();

        String label = list.getItem(index);
        Roi roi = (Roi) rois.get(label);

        if (roi != null) {
            roi.setImage(imp);
            roi.update(shiftKeyDown, altKeyDown);
        }
    }

    /**
     *  Description of the Method
     */
    void selectAll() {
        boolean allSelected = true;
        int count = list.getItemCount();

        for (int i = 0; i < count; i++) {
            if (!list.isIndexSelected(i)) {
                allSelected = false;
            }
        }
        if (allSelected) {
            select(-1);
        } else {
            for (int i = 0; i < count; i++) {
                if (!list.isSelected(i)) {
                    list.select(i);
                }
            }
        }
    }

    /**
     *  Overrides PlugInFrame.close().
     */
    public void close() {
        super.close();
        instance = null;
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
        }
    }

    /**
     *  Description of the Method
     *
     *@param  event  Description of the Parameter
     */
    public void mouseWheelMoved(MouseWheelEvent event) {
        synchronized (this) {
            int index = list.getSelectedIndex();
            int rot = event.getWheelRotation();

            if (rot < -1) {
                rot = -1;
            }
            if (rot > 1) {
                rot = 1;
            }
            index += rot;
            if (index < 0) {
                index = 0;
            }
            if (index >= list.getItemCount()) {
                index = list.getItemCount();
            }
            //IJ.log(index+"  "+rot);
            select(index);
            if (IJ.isWindows()) {
                list.requestFocusInWindow();
            }
        }
    }

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseReleased(MouseEvent e) {
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

    /**
     *  Description of the Method
     *
     *@param  e  Description of the Parameter
     */
    public void mouseExited(MouseEvent e) {
    }
}
