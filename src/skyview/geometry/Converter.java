package skyview.geometry;

/** A converter applies a succession of transformations on the data */
public class Converter extends Transformer implements skyview.Component {
    
    
    private java.util.ArrayList<Transformer> components = new java.util.ArrayList<Transformer>();
    private Transformer[] compArr;
    private double[][]    datArr;
    
    private double[] d2=new double[2], d3=new double[3];
    
    private boolean checked = false;
    private static boolean doDebug = false;
    
    public String getName() {
	return "Converter";
    }
    
    public String getDescription() {
	return "A compound set of transformations";
    }
    
    public void debug(boolean flag) {
	doDebug = flag;
    }
    
    /** Get the dimensionality of the input vectors.
     */
    public int getInputDimension() {
	if (components.size() > 0) {
	    return components.get(0).getInputDimension();
	} else {
	    return 0;
	}
    }
    
    /** Get the dimensionality of the output vectors.
     */
    public int getOutputDimension() {
	if (components.size() > 0) {
	    return components.get(components.size()-1).getOutputDimension();
	} else {
	    return 0;
	}
    }
    
    
    /** Add a component to the transformation */
    public void add(Transformer trans) throws TransformationException {
	
	// Check if this is a real transformation */
	if (trans == null) {
	    return;
	}
	
	if (components.size() > 0 && trans.getInputDimension() != getOutputDimension()) {
	    throw new TransformationException("Incompatible adjacent components: "+
					      components.get(components.size()-1).getName() + " -> "+
					      trans.getName()
					      );
	}
	
	/** If this is another aggregate, add in the individual elements */
	if (trans instanceof Converter) {
	    Converter c = (Converter) trans;
	    this.components.addAll(c.components);
	} else {
	    components.add(trans);
	}
	checked = false;
    }
    
    
    /** Transform a vector */
    public void transform(double[] in, double[] out) {
	
	if (!checked) {
	    check();
	}
	
	if (compArr.length == 0) {
	    if (in != out) {
		System.arraycopy(in, 0, out, 0, in.length);
	    }
	    return;
	}
	
	
	double[] from = in;
	double[] to;
	
	for (int i=0; i<compArr.length; i += 1) {
	    Transformer t = compArr[i];
	    if (doDebug) {
	        System.err.printf("Converter: %d %s: ", i, t);
		for (double x: from) {
		    System.err.print(x+" ");
		}
	        System.err.print(" -> ");
	    }
	    if (i == compArr.length-1) {
		to = out;
	    } else {
		to = datArr[i];
	    }
	    t.transform(from,to);
	    from = to;
	    if (doDebug) {
		for (double x: from) {
		    System.err.print(x+" ");
		}
		System.err.println();
	    }
	}
    }
    
    /** Transform an array of vectors */
    public void transform(double[][] in, double[][] out) {
	
	if (!checked) {
	    check();
	}
	if (components.size() == 0) {
	    if (in != out  && in.length > 0) {
		int len =  in[0].length;
		for (int i=0; i<in.length; i += 1) {
		    System.arraycopy(in[i], 0, out[i], 0, len);
		}
	    }
	}
	
	
	double[][] from = in;
	double[][] to;
	double[][] d2a = null;
	double[][] d3a = null;
	
	for (int i=0; i<components.size(); i += 1) {
	    Transformer t = components.get(i);
	    if (i == components.size()-1) {
		to = out;
	    } else {
		if (t.getOutputDimension() == 2) {
		    if (d2a == null) {
			d2a = new double[2][in[0].length];
		    }
		    to = d2a;
		} else {
		    if (d3a == null) {
			d3a = new double[3][in[0].length];
		    }
		    to = d3a;
		}
	    }
	    t.transform(from,to);
	    from = to;
	}
    }
    
    /** See if there are any optimizations we can do. */
    private void check() {
	
	
        // We restart the check at the beginning whenever
	// we delete anything, to handle cases like:
	// A * B * C where A*B is the inverse of C -- we
	// want to get rid of all three, and
	// C B A a b c  -> null
	// where a is A inverse, b is B inverse, c is C inverse
	
      deleterLoop:	
	while (components.size() > 0) {
	    
	    if (doDebug) {
		System.err.println("Converter: deleter: "+components.size());
	    }
	    
	    Transformer last = components.get(0);
	    int i = 1;
	    while (i < components.size()) {
                if (doDebug) {System.err.println("Check: compLoop"+i);}
	    
	        Transformer curr = components.get(i);
	    
	        // Check for inverses first so that
	        // we delete inverse rotaters and scalers rather
	        // than adding them.
	        if (last.isInverse(curr)) {
		    components.remove(i);
		    components.remove(i-1);
		    // Start again in case there is a string of inverses
		    // to remove.
		    if (doDebug) {System.err.println("inverse");}
		    continue deleterLoop;
		}
		
		
		
	        if (last instanceof Rotater && curr instanceof Rotater) {
		    Transformer comb = ((Rotater)last).add((Rotater)curr);
		    // Get rid of the two old transformations
		    components.remove(i);
		    components.remove(i-1);
		    components.add(i-1, comb);
		    if (doDebug) {System.err.println("rotation");}
		    continue deleterLoop;
	        }
	        if (last instanceof Scaler && curr instanceof Scaler) {
		    Transformer comb = ((Scaler)last).add((Scaler)curr);
		    // Get rid of the two old transformations
		    components.remove(i);
		    components.remove(i-1);
		    components.add(i-1, comb);
		    if (doDebug) {System.err.println("scalers");}
		    continue deleterLoop;
	        }
	        last = curr;
	        i += 1;
                if (doDebug) {System.err.println("Check: endcomploop");}
		
	    }
	    // If we get this far we are done!
	    break deleterLoop;
	}
	compArr = components.toArray(new Transformer[0]);
	datArr  = new double[compArr.length][];
	for (int i=0; i<datArr.length; i += 1) {
	    if (compArr[i].getOutputDimension() == 2) {
		datArr[i] = d2;
	    } else {
		datArr[i] = d3;
	    }
	}
        
	checked = true;
        if (doDebug) {System.err.println("Check: exit");}
    }
    
    /** Return the inverse of this series of transformations. */
    public Converter inverse() {
	Converter x = new Converter();
	for (int i=components.size()-1; i >= 0; i -= 1) {
	    try {
	        x.add(components.get(i).inverse());
	    } catch (TransformationException e) {
		return null;
	    }
	}
	return x;
    }
    
    /** Is this the inverse of another transformation. */
    public boolean isInverse(Transformer t) {
	
	// Need to collapse rotation cascades and such
	// to compare...
	check();
	
	// Two null transformations are inverses I suppose!
	if (t == null && components.size() == 0) {
	    return true;
	}
	
	// A non-converter can be an inverse if it exactly
	// inverts the single operation
	if (! (t instanceof Converter)) {
	    if (components.size() != 1) {
	        return false;
	    } else {
		return components.get(0).isInverse(t);
	    }
	}
	
	Converter c = (Converter) t;
	c.check();
	if (c.components.size() != components.size()) {
	    return false;
	}
	
	// Check component by component
	// Note that this assumes that 'c' is performed after
	// the transformations in 'this'.
	int n = components.size();
	for (int i=0; i<n; i += 1) {
	    if (!components.get(n-i).isInverse(c.components.get(i))) {
		return false;
	    }
	}
	return true;
    }
}
