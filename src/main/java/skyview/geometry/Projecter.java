package skyview.geometry;

/** This class projects a point from the celestial sphere
 *  to a projection plane.
 */
public abstract class Projecter extends Transformer {
    
    /** Get the inverse */
    public abstract Deprojecter inverse();
    
    /** What is the output dimensionality of a projecter? */
    protected int getOutputDimension() {
	return 2;
    }
    
    /** What is the input dimensionality of a projecter? */
    protected int getInputDimension() {
	return 3;
    }
    
}
