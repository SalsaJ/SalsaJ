package ij.util;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.BrowserLauncher;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import javax.swing.*;
import sun.awt.VerticalBagLayout;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 23 octobre 2007
 */
public class AboutSalsaJ extends JFrame {

    String name;
    String locale;

    /**
     * Constructor for the AboutWindow object
     *
     * @param na
     */
    public AboutSalsaJ(String na, String loc) {
        name = na;
        locale = loc;
        Container top = this.getContentPane();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        // text lic
        JLabel textLic = new JLabel("distributed under licence CreativeCommons3.0");
        textLic.setAlignmentX(Component.CENTER_ALIGNMENT);
        top.setBackground(Color.WHITE);
        top.add(version());
        top.add(textLic);
        top.add(licence());
        top.add(authors());
        top.add(institutions());
        top.add(contact());
    }

    /**
     * the version of the plugin
     *
     * @return the label with the version
     */
    private JLabel version() {
        JLabel label = new JLabel(name + ImageJ.VERSION);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    /**
     * creates the licence label
     *
     * @return the licence label
     */
    private JLabel licence() {
        JLabel lic = new JLabel("distributed under the Creative Commons licence");
        lic.setAlignmentX(Component.CENTER_ALIGNMENT);
        lic.setCursor(new Cursor(Cursor.HAND_CURSOR));


        JLabel CC;
        URL url = getClass().getResource("/images/CreativeCommons.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImageIcon icon = new ImageIcon(image);
        CC = new JLabel(icon, JLabel.CENTER);
        CC.setCursor(new Cursor(Cursor.HAND_CURSOR));
        CC.setAlignmentX(Component.CENTER_ALIGNMENT);

        CC.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                String url = "http://creativecommons.org/licenses/by-nc-nd/3.0/deed." + locale;
                try {
                    BrowserLauncher.openURL(url);
                } catch (IOException ioe) {
                    IJ.log("cannot open the url " + url + "\n" + ioe);
                }
            }
        });
        return CC;
    }

    /**
     * creates the authors label
     *
     * @return the authors label
     */
    private JLabel authors() {
        JLabel curie = new JLabel("A.-L. Melchior, T. Boudier, UPMC");
        curie.setAlignmentX(Component.CENTER_ALIGNMENT);
        return curie;
    }

    /**
     * creates the contact label
     *
     * @return the contact label
     */
    private JLabel contact() {
        JLabel cont = new JLabel("contact : euhoumtr@euhou.net");
        cont.setAlignmentX(Component.CENTER_ALIGNMENT);
        cont.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cont.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                try {
                    BrowserLauncher.openURL("http://www.euhou.net/index.php/what-is-eu-hou-mainmenu-3/contact-mainmenu-52?task=view&contact_id=1");
                } catch (IOException ioe) {
                    IJ.log("cannot open mailto\n" + ioe);
                }
            }
        });
        return cont;
    }

    /**
     * creates the institutions label
     *
     * @return the institutions label
     */
    private JPanel institutions() {
        JPanel inst = new JPanel();
//        inst.setLayout(new BoxLayout(inst, BoxLayout.X_AXIS));
//        URL url = getClass().getResource("/icons/institut_curie.gif");
//        Image image = Toolkit.getDefaultToolkit().getImage(url);
//        ImageIcon icon = new ImageIcon(image);
//        JLabel curie = new JLabel(icon, JLabel.CENTER);
//        curie.setCursor(new Cursor(Cursor.HAND_CURSOR));
//
//        curie.addMouseListener(
//                new MouseAdapter() {
//
//                    @Override
//                    public void mouseClicked(MouseEvent me) {
//                        try {
//                            BrowserLauncher.openURL("http://www.curie.fr");
//                        } catch (IOException ioe) {
//                            IJ.log("cannot open url http://www.curie.fr\n" + ioe);
//                        }
//                    }
//                });
        JLabel upmc;
        URL url = getClass().getResource("/images/upmc_bw.png");
        Image image = Toolkit.getDefaultToolkit().getImage(url);
        ImageIcon icon = new ImageIcon(image);
        upmc = new JLabel(icon, JLabel.CENTER);
        upmc.setCursor(new Cursor(Cursor.HAND_CURSOR));
        upmc.addMouseListener(
                new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent me) {
                try {
                    BrowserLauncher.openURL("http://www.upmc.fr");
                } catch (IOException ioe) {
                    IJ.log("cannot open url http://www.upmc.fr\n" + ioe);
                }
            }
        });


//        url = getClass().getResource("/icons/cnrs.gif");
//        image = Toolkit.getDefaultToolkit().getImage(url);
//        icon = new ImageIcon(image);
//        JLabel cnrs = new JLabel(icon, JLabel.CENTER);
//        cnrs.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        //JLabel cnrs = new JLabel(" CNRS ");
//        cnrs.addMouseListener(
//                new MouseAdapter() {
//
//                    @Override
//                    public void mouseClicked(MouseEvent me) {
//                        try {
//                            BrowserLauncher.openURL("http://www.cnrs.fr");
//                        } catch (IOException ioe) {
//                            IJ.log("cannot open url http://www.cnrs.fr\n" + ioe);
//                        }
//                    }
//                });

//        url = getClass().getResource("/icons/inserm.gif");
//        image = Toolkit.getDefaultToolkit().getImage(url);
//        icon = new ImageIcon(image);
//        JLabel inserm = new JLabel(icon, JLabel.CENTER);
//        inserm.setCursor(new Cursor(Cursor.HAND_CURSOR));
//        //JLabel cnrs = new JLabel(" CNRS ");
//        inserm.addMouseListener(
//                new MouseAdapter() {
//
//                    @Override
//                    public void mouseClicked(MouseEvent me) {
//                        try {
//                            BrowserLauncher.openURL("http://www.inserm.fr/fr/home.html");
//                        } catch (IOException ioe) {
//                            IJ.log("cannot open url http://www.inserm.fr\n" + ioe);
//                        }
//                    }
//                });
        //inst.add(curie);
        inst.add(upmc);
        //inst.add(cnrs);
        //inst.add(inserm);

        return inst;
    }

    /**
     * draw the window
     */
    public void drawAbout() {
        int sizeX = 400;
        Container top = this.getContentPane();
        int nbcomp = top.getComponentCount();
        for (int i = 0; i < nbcomp; i++) {
            Component tmp = top.getComponent(i);
            Dimension dim = tmp.getMinimumSize();
            tmp.setSize(sizeX, (int) dim.getHeight());
        }
        setSize(sizeX, 200);
        setResizable(false);
        setVisible(true);
    }
}
