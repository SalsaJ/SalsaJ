package skyview.geometry.projecter;

import skyview.geometry.*;

import static java.lang.Double.NaN;
import static java.lang.Math.*;

import skyview.geometry.Projecter;
import skyview.geometry.Deprojecter;
import skyview.geometry.Transformer;


/** This class implements the Cartesian (rectangular)
 *  projection.  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */

public final class CarProjecter extends Projecter {
    
    /** Get the name of the compontent */
    public String getName() {
	return "CarProjecter";
    }
    
    /** Return a description of the component */
    public String getDescription() {
	return "Transform from the celestial sphere to the plane described by Lon/Lat directly";
    }
    
    /** Get the inverse transformation */
    public Deprojecter inverse() {
	return new  CarProjecter.CarDeprojecter();
    }
    
    /** Is this an inverse of some other transformation? */
    public boolean isInverse(Transformer t) {
	return t.getName().equals("CarDeprojecter");
    }
    
    /** Do the transfromation */
    public final void transform(double[] sphere, double[] plane) {
	
	if (Double.isNaN(sphere[2]) ) {
	    plane[0] = NaN;
	    plane[1] = NaN;
	} else {
	    plane[0] = atan2(sphere[1], sphere[0]);
	    plane[1] = atan2(sphere[2],
			     sqrt(sphere[0]*sphere[0]+sphere[1]*sphere[1]));
	}
    }
    
    
    public class CarDeprojecter extends Deprojecter {
        /** Get the name of the compontent */
        public String getName() {
	    return "CarDeprojecter";
        }
    
        /** Is this an inverse of some other transformation? */
        public boolean isInverse(Transformer t) {
	    return t.getName().equals("CarProjecter");
        }
	
        /** Return a description of the component */
        public String getDescription() {
	    return "Transform from the Lat/Lon to the corresponding unit vector.";
	}
	
	/** Get the inverse */
	public Projecter inverse() {
	    return CarProjecter.this;
	}
    
        /** Deproject a point from the plane to the sphere.
         *  @param plane a double[2] vector in the tangent plane.
         *  @param spehre a preallocated double[3] vector.
         */
        public final void transform(double[] plane, double[] sphere) {
	
	    if (Double.isNaN(plane[0])) {
	        sphere[0] = NaN;
	        sphere[1] = NaN;
	        sphere[2] = NaN;
	    
	    } else {
	     
	        double sr = sin(plane[0]);
	        double cr = cos(plane[0]);
	        double sd = sin(plane[1]);
	        double cd = cos(plane[1]);
	    
	        sphere[0] = cr*cd;
	        sphere[1] = sr*cd;
	        sphere[2] = sd;
	    }
	}
    }
}
