package skyview.geometry.projecter;


/** This class implements the Zenithal Equidistant (Arc)
 *  projection.  This projection has equidistant
 *  parallels of latitude.
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */

import skyview.geometry.Projecter;
import skyview.geometry.Deprojecter;
import skyview.geometry.Transformer;

public class ArcProjecter extends Projecter {

    /** Get a name for the component */
    public String getName() {
	return "ArcProjecter";
    }
    
    /** Get a description for the component */
    public String getDescription() {
	return "Zenithal Equidistant (ARC) projecter";
    }
    
    /** Get this inverse of the transformation */
    public Deprojecter inverse() {
	return new ArcProjecter.ArcDeprojecter();
    }

    /** Is this an inverse of some other transformation? */
    public boolean isInverse(Transformer t) {
	return t.getName().equals("ArcDeprojecter");
    }
    
    /** Project a point from the sphere to the plane.
     *  @param sphere a double[3] unit vector
     *  @param plane  a double[2] preallocated vector.
     */
    public final void transform(double[] sphere, double[] plane) {
	
	if (Double.isNaN(sphere[2])) {
	    plane[0] = Double.NaN;
	    plane[1] = Double.NaN;
	} else {
            /*
	    double num = 2*(1-sphere[2]);
	    if (num < 0) {
		num = 0;
	    }
             */
	    double denom = sphere[0]*sphere[0] + sphere[1]*sphere[1];
	    if (denom == 0) {
		plane[0] = 0;
		plane[1] = 0;
	    } else {
	        double ratio = (Math.PI/2 - Math.asin(sphere[2]))/Math.sqrt(denom);
	        plane[0] = ratio * sphere[0];
	        plane[1] = ratio * sphere[1];
	    }
	    
	}
    }
    
    
    public class ArcDeprojecter extends Deprojecter {
	
	/** Get the name of the component */
	public String getName() {
	    return "ArcDeprojecter";
	}
	
	/** Get the description of the compontent */
	public String getDescription() {
	    return "Zenithal equal area (ARC) deprojecter";
	}
	
	/** Get the inverse transformation */
	public Projecter inverse() {
	    return ArcProjecter.this;
	}
	 
        /** Is this an inverse of some other transformation? */
        public boolean isInverse(Transformer t) {
	    return t.getName().equals("ArcProjecter");
        }
	
	
        /** Deproject a point from the plane to the sphere.
         *  @param plane a double[2] vector in the tangent plane.
         *  @param spehre a preallocated double[3] vector.
         */
        public final void transform(double[] plane, double[] sphere) {
	
	    double r = Math.sqrt(plane[0]*plane[0] + plane[1]*plane[1]);
	
	    if ( Double.isNaN(plane[0])) {
	        sphere[0] = Double.NaN;
	        sphere[1] = Double.NaN;
	        sphere[2] = Double.NaN;
	    
	    } else {
	        sphere[2]  = Math.cos(r);
                double pr_sinde=sphere[2];
                double pr_sq_cosde=1-pr_sinde*pr_sinde;
                double ratio=0;    
                if(r>0){
                    ratio = Math.sqrt(pr_sq_cosde)/r;    
                }
	        sphere[0] = ratio * plane[0];
	        sphere[1] = ratio * plane[1];
	    }
	}
    }
}
