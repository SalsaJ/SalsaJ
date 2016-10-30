package skyview.geometry.projecter;

import static java.lang.Math.sqrt;
import static java.lang.Math.abs;

import skyview.geometry.Projecter;
import skyview.geometry.Deprojecter;
import skyview.geometry.Transformer;

/** This class implements the AIT (Hammer-Aitov)
 *  projection.  This version uses only the Math.sqrt
 *  function without any calls to trigonometric functions.
 */

public class AitProjecter extends Projecter {
    
    /** The name of the Component */
    public String getName() {
	return "AitProjecter";
    }
    
    /** A description of the component */
    public String getDescription() {
	return "Project to an Hammer-Aitoff projection (often used for all sky data)";
    }
    
    /** Get the associated deprojecter */
    public Deprojecter inverse() {
	return new AitProjecter.AitDeprojecter();
    }
    
    /** Is this the inverse of another transformation? */
    public boolean isInverse(Transformer trans) {
	return trans.getName().equals("AitDeprojecter");
    }
    
    public final void transform(double[] sphere, double[] plane) {
	
	if ( Double.isNaN(sphere[2]) ) {
	    plane[0] = Double.NaN;
	    plane[1] = Double.NaN;
	} else {
	    
	    // Sphere[2] is just sin_b.
	    double cos_b = sqrt(1-sphere[2]*sphere[2]);
	    double cos_l = 0;
	    
	    if (1 - abs(sphere[2]) > 1.e-10) {
		// Not at a pole
		cos_l = sphere[0]/cos_b;
	    }
	    
	    // Use half angle formulae to get cos(l/2), sin(l/2)
	    // Be careful of roundoff errors.
	   
	    double cos_l2 = (0.5*(1+cos_l));
	    if (cos_l2 > 0) {
		cos_l2 = sqrt(cos_l2);
	    } else {
		cos_l2 = 0;
	    }
	    
	    double sin_l2 = (0.5*(1-cos_l));
	    if (sin_l2 > 0) {
		sin_l2 = sqrt(sin_l2);
	    } else {
		sin_l2 = 0;
	    }
	    
	    // Need to be careful to handle the sign of the
	    // half angle formulae.  We're treating this as a projection
	    // around 0,0.  So we're really looking not at 0 - 2PI for the
	    // range of L, but -PI to PI.  So if we have a negative
	    // Y value we want to use a negative value for sin(L/2)
	    // In this interval cos(L/2) is guaranteed to be positive.
	    
	    if (sphere[1] < 0) {
		sin_l2 = -sin_l2;
	    }
	    
	    // Now use Calabretta and Griesen formulae.
	    double gamma = sqrt( 2 / (1 + cos_b*cos_l2));
	    plane[0] = 2*gamma*cos_b*sin_l2;
	    plane[1] = gamma*sphere[2];
	}
	      
    }
    
    public class AitDeprojecter extends Deprojecter {
	
	/** Name of component */
	public String getName() {
	    return "AitDeprojecter";
	}
	
	/** Description of component */
	public String getDescription() {
	    return "Deproject from a Hammer-Aitoff ellipse back to the sphere.";
	}
	
	/** Get the inverse transformation */
	public Projecter inverse() {
	    return AitProjecter.this;
	}
    
        /** Is this the inverse of another transformation? */
        public boolean isInverse(Transformer trans) {
	    return trans.getName().equals("AitProjecter");
        }
	
        /** Deproject a point from the plane to the sphere.
         *  @param plane  The input position in the projection  plane.
         *  @param sphere A preallocated 3-vector to hold the unit vector result.
         */
        public final void transform(double[] plane, double[] sphere) {
	
	    if (plane[0]*plane[0]/8 + plane[1]*plane[1]/2 > 1 ||
	      
	        Double.isNaN(plane[0])) {
	        sphere[0] = Double.NaN;
	        sphere[1] = Double.NaN;
	        sphere[2] = Double.NaN;
	    
	    } else {
	    
	       // Use Calabretta and Greisen fomulae
                double z = (1 - plane[0]*plane[0]/16 - plane[1]*plane[1]/4 );
	        if (z > 0) {
		    z = sqrt(z);
	        } else {
		    z = 0;
	        }
	    
	        sphere[2]  = plane[1]*z;
	        double cos_b = sqrt(1-sphere[2]*sphere[2]);
	        if (abs(cos_b) > 1.e-12) {
		
		    // Use the double able formula to get form sin(l/2) to sin(l)
		    // C&G don't actually gives these values for the
		    // sin(l/2) and cos(l/2).  Rather they give
		    // L = 2*arg(2*z*z-1, z*x/2)
		    // This gives the sin(l/2), cos(l/2) to within
		    // a factor.  Empirically that seems to be 1/cos(B)
		    // Using the double angle formulae we only need to
		    // compute two square roots for the transformation.
	    
	            double sl2 = z*plane[0]/(2*cos_b);
	            double cl2 = (2*z*z-1)/cos_b;
	    
                    // Double angle formulae
	            double cl = 2*cl2*cl2-1;
	            double sl = 2*sl2*cl2;
		
		    sphere[0] = cl*cos_b;
		    sphere[1] = sl*cos_b;
		
	        } else {
		    sphere[0] = 0;
		    sphere[1] = 0;
	        }
	    }
        }
    }
}
