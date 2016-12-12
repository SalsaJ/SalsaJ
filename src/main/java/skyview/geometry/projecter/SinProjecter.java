package skyview.geometry.projecter;

/** This class implements the Sine (Orthographic)
 *  projection.  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */

import skyview.geometry.Projecter;
import skyview.geometry.Deprojecter;
import skyview.geometry.Transformer;

public final class SinProjecter extends Projecter {
    

    /** Get the name of the component */
    public String getName() {
	return "SinProjecter";
    }
    
    /** Get a description of the component */
    public String getDescription () {
	return "Project as if seeing the sphere from a great distance";
    }
    
    /** Get the inverse transformation */
    public Deprojecter inverse() {
	return new SinProjecter.SinDeprojecter();
    }
    
    /** Is this an inverse of some other transformation? */
    public boolean isInverse(Transformer t) {
	return t.getName().equals("SinDeprojecter");
    }
    
    /** Project a point from the sphere to the plane.
     *  @param sphere a double[3] unit vector
     *  @param plane  a double[2] preallocated vector.
     */
    public final void transform(double[] sphere, double[] plane) {
	
	if (Double.isNaN(sphere[2]) || sphere[2] <= 0) {
	    plane[0] = Double.NaN;
	    plane[1] = Double.NaN;
	} else {
	    plane[0] = sphere[0];
	    plane[1] = sphere[1];
	}
    }
    
    public class SinDeprojecter extends skyview.geometry.Deprojecter {
	
	/** Get the name of the component */
	public String getName() {
	    return "SinDeprojection";
	}
	
	/** Get a description of the component */
	public String getDescription() {
	    return "Invert the sine projection";
	}
	
	/** Get the inverse transformation */
	public Projecter inverse() {
	    return SinProjecter.this;
	}

        /** Is this an inverse of some other transformation? */
        public boolean isInverse(Transformer t) {
            return t.getName().equals("SinProjecter");
        }

    
        /** Deproject a point from the plane to the sphere.
         *  @param plane a double[2] vector in the tangent plane.
         *  @param spehre a preallocated double[3] vector.
         */
        public final void transform(double[] plane, double[] sphere) {
	
	    if (Double.isNaN(plane[0])) {
	        sphere[0] = Double.NaN;
	        sphere[1] = Double.NaN;
	        sphere[2] = Double.NaN;
	    
	    } else {
	        sphere[0] = plane[0];
	        sphere[1] = plane[1];
	        sphere[2] = Math.sqrt(1 - plane[0]*plane[0] - plane[1]*plane[1]);
	    }
        }
    }
}
