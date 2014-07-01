//EU_HOU
package ij.plugin;

import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.io.*;
import java.net.URL;
import java.awt.image.*;

/**
 *  This plugin implements the Help/About ImageJ command by opening the
 *  about.jpg in ij.jar, scaling it 400% and adding some text.
 *
 *@author     Thomas
 *@created    12 novembre 2007
 */
public class AboutBox implements PlugIn {

    final static int SMALL_FONT = 14, LARGE_FONT = 40;

    /**
     *  Main processing method for the AboutBox object
     *
     *@param  arg  Description of the Parameter
     */
    public void run(String arg) {
        int lines = 6;
        String[] text = new String[lines];
        //EU_HOU Bundle
        text[0] = "SalsaJ " + ImageJ.VERSION;
        //EU_HOU Bundle
        text[1] = IJ.getBundle().getString("About1");
        text[2] = "http://www.euhou.net";
        //EU_HOU Bundle
        text[3] = IJ.getBundle().getString("About2");
        //EU_HOU Bundle
        text[4] = IJ.getBundle().getString("About3");
        text[5] = "Java " + System.getProperty("java.version");

        ImageProcessor ip = null;
        ImageJ ij = IJ.getInstance();
        URL url = ij.getClass().getClassLoader().getResource("images/about2.jpg");
        if (url != null) {
            Image img = null;
            try {
                img = ij.createImage((ImageProducer) url.getContent());
            } catch (Exception e) {
            }
            if (img != null) {
                ImagePlus imp = new ImagePlus("", img);
                ip = imp.getProcessor();
            }
        }
        if (ip == null) {
            ip = new ColorProcessor(55, 45);
        }
        //ip = ip.resize(ip.getWidth() * 4, ip.getHeight() * 4);
        ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
        ip.setAntialiasedText(true);
        int[] widths = new int[lines];
        widths[0] = ip.getStringWidth(text[0]);
        ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
        for (int i = 1; i < lines - 1; i++) {
            widths[i] = ip.getStringWidth(text[i]);
        }
        int max = 0;
        for (int i = 0; i < lines - 1; i++) {
            if (widths[i] > max) {
                max = widths[i];
            }
        }
        //EU_HOU Changes
        ip.setColor(new Color(255, 255, 255));
        ip.setFont(new Font("SansSerif", Font.PLAIN, LARGE_FONT));
        int y = 40;
        ip.drawString(text[0], x(text[0], ip, max), y);
        ip.setFont(new Font("SansSerif", Font.PLAIN, SMALL_FONT));
        y += 100;
        ip.drawString(text[1], x(text[1], ip, max), y);
        y += 18;
        ip.drawString(text[2], x(text[2], ip, max), y);
        y += 25;
        ip.drawString(text[3], x(text[3], ip, max), y);
        y += 18;
        ip.drawString(text[4], x(text[4], ip, max), y);

        y += 18;
        ip.drawString(text[5], x(text[5], ip, max), y);

        //ip.drawString(text[6], ip.getWidth() - ip.getStringWidth(text[6]) - 10, ip.getHeight() - 3);
        ImageWindow.centerNextImage();
        //EU_HOU Bundle
        new ImagePlus(IJ.getBundle().getString("AboutIJ"), ip).show();
        //EU_HOU end
    }

    /**
     *  Description of the Method
     *
     *@param  text  Description of the Parameter
     *@param  ip    Description of the Parameter
     *@param  max   Description of the Parameter
     *@return       Description of the Return Value
     */
    int x(String text, ImageProcessor ip, int max) {
        //System.out.println("a=" + ip.getWidth() + " ,b=" + max + " ,c=" + ip.getStringWidth(text) + " ,d=" + ((ip.getWidth() - ip.getStringWidth(text)) / 2));
        return (ip.getWidth() - ip.getStringWidth(text)) / 2;
    }
}

