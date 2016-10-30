package ij.process;

import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.gui.OvalRoi;
import ij.gui.Roi;

/**
 * Description of the Class
 *
 * @author     thomas
 * @created    18 juillet 2007
 */
public class IPhot {

    ImageWindow win = null;
    String title;
    Roi r;
    int x;
    int y;
    int inten;
    int sky;
    Photometer outer;

    public IPhot(String i, int x, int y, Roi r, int inten, int sky, Photometer outer) {
        this.outer = outer;
        this.title = new String(i);
        this.r = new OvalRoi(r.x(), r.y(), r.width(), r.height());
        this.x = x;
        this.y = y;
        this.inten = inten;
        this.sky = sky;
        win = WindowManager.getCurrentWindow();
    }

    public int getInten() {
        return inten;
    }

    public int getSky() {
        return sky;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    

    /**
     *  Description of the Method
     *
     *@return    Description of the Return Value
     */
    public String toString() {
        return new String(title + "\t" + x + "\t" + y + "\t" + inten + "\t" + r.width() / 2 + "\t" + sky + "\t" + r.height() / 2);
    }
}
