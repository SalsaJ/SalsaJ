//EU_HOU
package ij.gui;

import java.awt.*;
import java.awt.image.*;
import ij.*;

/**
 *  Freehand region of interest or freehand line of interest
 *
 *@author     Thomas
 *@created    6 novembre 2007
 */
public class FreehandRoi extends PolygonRoi {

	/**
	 *  Constructor for the FreehandRoi object
	 *
	 *@param  sx   Description of the Parameter
	 *@param  sy   Description of the Parameter
	 *@param  imp  Description of the Parameter
	 */
	public FreehandRoi(int sx, int sy, ImagePlus imp) {
		super(sx, sy, imp);
		/*
		 *  EU_HOU CHANGES
		 */
		/*
		 *  if (Toolbar.getToolId() == Toolbar.FREEROI) {
		 *  type = FREEROI;
		 *  } else {
		 *  type = FREELINE;
		 *  }
		 */
		/*
		 *  EU_HOU END
		 */
		type = FREELINE;
		if (nPoints == 2) {
			nPoints--;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  sx  Description of the Parameter
	 *@param  sy  Description of the Parameter
	 */
	protected void grow(int sx, int sy) {
	int ox = ic.offScreenX(sx);
	int oy = ic.offScreenY(sy);

		if (ox < 0) {
			ox = 0;
		}
		if (oy < 0) {
			oy = 0;
		}
		if (ox > xMax) {
			ox = xMax;
		}
		if (oy > yMax) {
			oy = yMax;
		}
		if (ox != xp[nPoints - 1] + x || oy != yp[nPoints - 1] + y) {
			xp[nPoints] = ox - x;
			yp[nPoints] = oy - y;
			nPoints++;
			if (nPoints == xp.length) {
				enlargeArrays();
			}
			drawLine();
		}
	}


	/**
	 *  Description of the Method
	 */
	void drawLine() {
	int x1 = xp[nPoints - 2] + x;
	int y1 = yp[nPoints - 2] + y;
	int x2 = xp[nPoints - 1] + x;
	int y2 = yp[nPoints - 1] + y;
	int xmin = Math.min(x1, x2);
	int xmax = Math.max(x1, x2);
	int ymin = Math.min(y1, y2);
	int ymax = Math.max(y1, y2);
	int margin = 4;

		if (ic != null) {
		double mag = ic.getMagnification();

			if (mag < 1.0) {
				margin = (int) (margin / mag);
			}
		}
		imp.draw(xmin - margin, ymin - margin, (xmax - xmin) + margin * 2, (ymax - ymin) + margin * 2);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  screenX  Description of the Parameter
	 *@param  screenY  Description of the Parameter
	 */
	protected void handleMouseUp(int screenX, int screenY) {
		if (state == CONSTRUCTING) {
			addOffset();
			finishPolygon();
		}
		state = NORMAL;
	}

}

