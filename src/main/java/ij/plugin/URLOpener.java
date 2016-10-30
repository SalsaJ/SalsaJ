package ij.plugin;

import java.awt.*;
import java.io.*;
import java.net.URL;
import ij.*;
import ij.io.*;
import ij.gui.*;
import ij.plugin.frame.Editor;

/**
 *  Opens TIFFs, ZIP compressed TIFFs, DICOMs, GIFs and JPEGs using a URL. TIFF
 *  file names must end in ".tif", ZIP file names must end in ".zip" and DICOM
 *  file names must end in ".dcm". Opens a Web page in the default browser if
 *  the URL ends with "/".
 *
 *@author     Thomas
 *@created    3 decembre 2007
 */
public class URLOpener implements PlugIn {

    private static String url = "http://rsb.info.nih.gov/ij/images/clown.gif";

    /**
     *  If 'urlOrName' is a URL, opens the image at that URL. If it is a file name,
     *  opens the image with that name from the 'images.location' URL in
     *  IJ_Props.txt. If it is blank, prompts for an image URL and open the
     *  specified image.
     *
     *@param  urlOrName  Description of the Parameter
     */
    public void run(String urlOrName) {
        if (!urlOrName.equals("")) {
            if (urlOrName.endsWith("StartupMacros.txt")) {
                openTextFile(urlOrName, true);
            } else {
                String url = urlOrName.indexOf("://") > 0 ? urlOrName : Prefs.getImagesURL() + urlOrName;
                ImagePlus imp = new ImagePlus(url);
                if (imp.getType() == ImagePlus.COLOR_RGB) {
                    Opener.convertGrayJpegTo8Bits(imp);
                }
                WindowManager.checkForDuplicateName = true;
                FileInfo fi = imp.getOriginalFileInfo();
                if (fi != null && fi.fileType == FileInfo.RGB48) {
                    imp = new CompositeImage(imp, 3);
                }
                imp.show();
            }
            return;
        }
        //EU_HOU Bundle
        GenericDialog gd = new GenericDialog("Enter a URL");
        gd.addMessage("Enter URL of a TIFF, JPEG, GIF, PNG or DICOM image");
        gd.addStringField("URL:", url, 40);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        url = gd.getNextString();
        url = url.trim();
        if (url.indexOf("://") == -1) {
            url = "http://" + url;
        }
        if (url.endsWith("/")) {
            IJ.runPlugIn("ij.plugin.BrowserLauncher", url.substring(0, url.length() - 1));
        } else if (url.endsWith(".html") || url.endsWith(".htm") || url.indexOf(".html#") > 0) {
            IJ.runPlugIn("ij.plugin.BrowserLauncher", url);
        } else if (url.endsWith(".txt") || url.endsWith(".ijm")) {
            openTextFile(url, false);
        } else {
            //EU_HOU Bundle
            IJ.showStatus("Opening: " + url);
            ImagePlus imp = new ImagePlus(url);
            WindowManager.checkForDuplicateName = true;
            FileInfo fi = imp.getOriginalFileInfo();
            if (fi != null && fi.fileType == FileInfo.RGB48) {
                imp = new CompositeImage(imp, 3);
            }
            imp.show();
            IJ.showStatus("");
        }
        IJ.register(URLOpener.class);// keeps this class from being GC'd
    }

    /**
     *  Description of the Method
     *
     *@param  urlString  Description of the Parameter
     *@param  install    Description of the Parameter
     */
    void openTextFile(String urlString, boolean install) {
        StringBuffer sb = null;
        try {
            URL url = new URL(urlString);
            InputStream in = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            in.close();
        } catch (IOException e) {
            if (!(install && urlString.endsWith("StartupMacros.txt"))) {
                //EU_HOU Bundle
                IJ.error("URL Opener", "" + e);
            }
            sb = null;
        }
        if (sb != null) {
            if (install) {
                (new MacroInstaller()).install(new String(sb));
            } else {
                (new Editor()).create(urlString, new String(sb));
            }
        }
    }
}

