package ij.util;
import java.awt.Color;
import java.util.*;
import java.io.*;
/*
 *  EU_HOU CHANGES
 */
import ij.*;
/*
 *  EU_HOU END
 */
/**
 *  This class contains static utility methods.
 *
 *@author     Thomas
 *@created    26 novembre 2007
 */
public class Tools {
	/**
	 *  This array contains the 16 hex digits '0'-'F'.
	 */
	public final static char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


	/**
	 *  Converts a Color to an 7 byte hex string starting with '#'.
	 *
	 *@param  c  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static String c2hex(Color c) {
	int i = c.getRGB();
	char[] buf7 = new char[7];
		buf7[0] = '#';
		for (int pos = 6; pos >= 1; pos--) {
			buf7[pos] = hexDigits[i & 0xf];
			i >>>= 4;
		}
		return new String(buf7);
	}


	/**
	 *  Converts a float to an 9 byte hex string starting with '#'.
	 *
	 *@param  f  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static String f2hex(float f) {
	int i = Float.floatToIntBits(f);
	char[] buf9 = new char[9];
		buf9[0] = '#';
		for (int pos = 8; pos >= 1; pos--) {
			buf9[pos] = hexDigits[i & 0xf];
			i >>>= 4;
		}
		return new String(buf9);
	}


	/**
	 *  Gets the minMax attribute of the Tools class
	 *
	 *@param  a  Description of the Parameter
	 *@return    The minMax value
	 */
	public static double[] getMinMax(double[] a) {
	double min = Double.MAX_VALUE;
	double max = -Double.MAX_VALUE;
	double value;
		for (int i = 0; i < a.length; i++) {
			value = a[i];
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
		}
	double[] minAndMax = new double[2];
		minAndMax[0] = min;
		minAndMax[1] = max;
		return minAndMax;
	}


	/**
	 *  Gets the minMax attribute of the Tools class
	 *
	 *@param  a  Description of the Parameter
	 *@return    The minMax value
	 */
	public static double[] getMinMax(float[] a) {
	double min = Double.MAX_VALUE;
	double max = -Double.MAX_VALUE;
	double value;
		for (int i = 0; i < a.length; i++) {
			value = a[i];
			if (value < min) {
				min = value;
			}
			if (value > max) {
				max = value;
			}
		}
	double[] minAndMax = new double[2];
		minAndMax[0] = min;
		minAndMax[1] = max;
		return minAndMax;
	}


	/**
	 *  Converts the float array 'a' to a double array.
	 *
	 *@param  a  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static double[] toDouble(float[] a) {
	int len = a.length;
	double[] d = new double[len];
		for (int i = 0; i < len; i++) {
			d[i] = a[i];
		}
		return d;
	}


	/**
	 *  Converts the double array 'a' to a float array.
	 *
	 *@param  a  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static float[] toFloat(double[] a) {
	int len = a.length;
	float[] f = new float[len];
		for (int i = 0; i < len; i++) {
			f[i] = (float) a[i];
		}
		return f;
	}


	/**
	 *  Converts carriage returns to line feeds.
	 *
	 *@param  s  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static String fixNewLines(String s) {
	char[] chars = s.toCharArray();
		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\r') {
				chars[i] = '\n';
			}
		}
		return new String(chars);
	}


	/**
	 *  Returns a double containg the value represented by the specified <code>String</code>
	 *  .
	 *
	 *@param  s             the string to be parsed.
	 *@param  defaultValue  the value returned if <code>s</code> does not contain a
	 *      parsable double
	 *@return               The double value represented by the string argument or
	 *      <code>defaultValue</code> if the string does not contain a parsable
	 *      double
	 */
	public static double parseDouble(String s, double defaultValue) {
		if (s == null) {
			return defaultValue;
		}
		try {
		Double d = new Double(s);
			defaultValue = d.doubleValue();
		} catch (NumberFormatException e) {}
		return defaultValue;
	}


	/**
	 *  Returns a double containg the value represented by the specified <code>String</code>
	 *  .
	 *
	 *@param  s  the string to be parsed.
	 *@return    The double value represented by the string argument or Double.NaN
	 *      if the string does not contain a parsable double
	 */
	public static double parseDouble(String s) {
		return parseDouble(s, Double.NaN);
	}


	/**
	 *  Returns the number of decimal places need to display two numbers.
	 *
	 *@param  n1  Description of the Parameter
	 *@param  n2  Description of the Parameter
	 *@return     The decimalPlaces value
	 */
	public static int getDecimalPlaces(double n1, double n2) {
		if (Math.round(n1) == n1 && Math.round(n2) == n2) {
			return 0;
		} else {
			n1 = Math.abs(n1);
			n2 = Math.abs(n2);
		double n = n1 < n2 && n1 > 0.0 ? n1 : n2;
		double diff = Math.abs(n2 - n1);
			if (diff > 0.0 && diff < n) {
				n = diff;
			}
		int digits = 2;
			if (n < 100.0) {
				digits = 3;
			}
			if (n < 0.1) {
				digits = 4;
			}
			if (n < 0.01) {
				digits = 5;
			}
			if (n < 0.001) {
				digits = 6;
			}
			if (n < 0.0001) {
				digits = 7;
			}
			return digits;
		}
	}


	/**
	 *  Splits a string into substrings using the default delimiter set, which is "
	 *  \t\n\r" (space, tab, newline and carriage-return).
	 *
	 *@param  str  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	public static String[] split(String str) {
		return split(str, " \t\n\r");
	}


	/**
	 *  Splits a string into substring using the characters contained in the second
	 *  argument as the delimiter set.
	 *
	 *@param  str    Description of the Parameter
	 *@param  delim  Description of the Parameter
	 *@return        Description of the Return Value
	 */
	public static String[] split(String str, String delim) {
		if (delim.equals("\n")) {
			return splitLines(str);
		}
	StringTokenizer t = new StringTokenizer(str, delim);
	int tokens = t.countTokens();
	String[] strings;
		if (tokens > 0) {
			strings = new String[tokens];
			for (int i = 0; i < tokens; i++) {
				strings[i] = t.nextToken();
			}
		} else {
			strings = new String[1];
			strings[0] = str;
			tokens = 1;
		}
		return strings;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  str  Description of the Parameter
	 *@return      Description of the Return Value
	 */
	static String[] splitLines(String str) {
	Vector v = new Vector();
		try {
		BufferedReader br = new BufferedReader(new StringReader(str));
		String line;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				v.addElement(line);
			}
			br.close();
		} catch (Exception e) {}
	String[] lines = new String[v.size()];
		v.copyInto((String[]) lines);
		return lines;
	}


	/*
	 *  EU_HOU CHANGES
	 */
	/**
	 *  Fit a polynomial line of the (x,y) values. The order is fixed by the length
	 *  of vector minus one.
	 *
	 *@param  xValues  the values fo the X axis.
	 *@param  yValues  the values fo the Y axis.
	 *@param  vector   the result of fitting line
	 */
	public static void fittingData(float[] xValues, float[] yValues, float[] vector) {
	int dim = vector.length;
	int n = xValues.length;
	float aux;
	float[] D = new float[dim];
	float[][] array = new float[dim][dim];
	float[][] arraytest = new float[dim][dim];
	float[][] result = new float[dim][dim];

		for (int i = 0; i < dim; i++) {
			D[i] = 0;
			vector[i] = 0;
			for (int j = 0; j < dim; j++) {
				array[i][j] = 0;
				arraytest[i][j] = 0;
				result[i][j] = 0;
			}
		}

		for (int i = 0; i < n; i++) {
			D[0] = 1;
			for (int j = 1; j < dim; j++) {
				D[j] = D[j - 1] * xValues[i];
			}
			///256.0;
			for (int j = 0; j < dim; j++) {
				vector[j] += yValues[i] * D[j];
				for (int l = j; l < dim; l++) {
					array[j][l] += D[j] * D[l];
					array[l][j] = array[j][l];
				}
			}
		}

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				arraytest[i][j] = array[i][j];
			}
		}
		for (int i = 0; i < dim; i++) {
			aux = (float) 1.0 / array[i][i];
			for (int j = 0; j < dim; j++) {
				array[i][j] = array[i][j] * aux;
			}
			array[i][i] = aux;
			for (int k = 0; k < i; k++) {
				aux = array[k][i];
				for (int j = 0; j < dim; j++) {
					array[k][j] -= aux * array[i][j];
				}
				array[k][i] = -aux * array[i][i];
			}
			for (int k = i + 1; k < dim; k++) {
				aux = array[k][i];
				for (int j = 0; j < dim; j++) {
					array[k][j] -= aux * array[i][j];
				}
				array[k][i] = -aux * array[i][i];
			}
		}

		for (int i = 0; i < dim; i++) {
			D[i] = vector[i];
			vector[i] = 0;
		}

		for (int i = 0; i < dim; i++) {
			for (int j = 0; j < dim; j++) {
				vector[i] += D[j] * array[i][j];
			}
		}
	}


//===================================================================================================
	/**
	 *  Description of the Method
	 *
	 *@param  covar  Description of the Parameter
	 *@param  ma     Description of the Parameter
	 *@param  ia     Description of the Parameter
	 *@param  mfit   Description of the Parameter
	 */
	public static void covsrt(double[][] covar, int ma, int[] ia, int[] mfit) {
	int i;
	int j;
	int k;
	double swap;

		for (i = mfit[0]; i < ma; i++) {
			for (j = 0; j <= i; j++) {
				covar[i][j] = covar[j][i] = 0.0;
			}
		}
		k = mfit[0] - 1;
		for (j = ma - 1; j >= 0; j--) {
			if (ia[j] != 0) {
				if (k != j) {
					for (i = 0; i < ma; i++) {
						swap = covar[i][k];
						covar[i][k] = covar[i][j];
						covar[i][j] = swap;
					}
					for (i = 0; i < ma; i++) {
						swap = covar[k][i];
						covar[k][i] = covar[j][i];
						covar[j][i] = swap;
					}
				}
				k--;
			}
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  a  Description of the Parameter
	 *@param  n  Description of the Parameter
	 *@param  b  Description of the Parameter
	 *@param  m  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static int gaussj(double[][] a, int n, double[][] b, int m) {
	int[] indxc;
	int[] indxr;
	int[] ipiv;
	int i;
	int icol;
	int irow;
	int j;
	int k;
	int l;
	int ll;
	double big;
	double dum;
	double pivinv;
	double temp;
	double swap;

		indxc = new int[n];
		indxr = new int[n];
		ipiv = new int[n];
		irow = icol = 0;
		for (j = 0; j < n; j++) {
			ipiv[j] = 0;
		}
		for (i = 0; i < n; i++) {
			big = 0.0;
			for (j = 0; j < n; j++) {
				if (ipiv[j] != 1) {
					for (k = 0; k < n; k++) {
						if (ipiv[k] == 0) {
							if (java.lang.Math.abs(a[j][k]) >= big) {
								big = java.lang.Math.abs(a[j][k]);
								irow = j;
								icol = k;
							}
						} else if (ipiv[k] > 1) {
							//EU_HOU Bundle
							IJ.error(IJ.getPluginBundle().getString("error2"));
							return 0;
						}
					}
				}
			}
			++(ipiv[icol]);
			if (irow != icol) {
				for (l = 0; l < n; l++) {
					swap = a[irow][l];
					a[irow][l] = a[icol][l];
					a[icol][l] = swap;
				}
				for (l = 0; l < m; l++) {
					swap = b[irow][l];
					b[irow][l] = b[icol][l];
					b[icol][l] = swap;
				}
			}
			indxr[i] = irow;
			indxc[i] = icol;
			if (a[icol][icol] == 0.0) {
				//EU_HOU Bundle
				IJ.error(IJ.getPluginBundle().getString("error2"));
				return 0;
			}
			pivinv = 1.0 / a[icol][icol];
			a[icol][icol] = 1.0;
			for (l = 0; l < n; l++) {
				a[icol][l] *= pivinv;
			}
			for (l = 0; l < m; l++) {
				b[icol][l] *= pivinv;
			}
			for (ll = 0; ll < n; ll++) {
				if (ll != icol) {
					dum = a[ll][icol];
					a[ll][icol] = 0.0;
					for (l = 0; l < n; l++) {
						a[ll][l] -= a[icol][l] * dum;
					}
					for (l = 0; l < m; l++) {
						b[ll][l] -= b[icol][l] * dum;
					}
				}
			}
		}
		for (l = n - 1; l >= 0; l--) {
			if (indxr[l] != indxc[l]) {
				for (k = 0; k < n; k++) {
					swap = a[k][indxr[l]];
					a[k][indxr[l]] = a[k][indxc[l]];
					a[k][indxc[l]] = swap;
				}
			}
		}
		return 1;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x      Description of the Parameter
	 *@param  y      Description of the Parameter
	 *@param  sig    Description of the Parameter
	 *@param  ndata  Description of the Parameter
	 *@param  a      Description of the Parameter
	 *@param  ia     Description of the Parameter
	 *@param  ma     Description of the Parameter
	 *@param  alpha  Description of the Parameter
	 *@param  beta   Description of the Parameter
	 *@param  funcs  Description of the Parameter
	 *@param  chisq  Description of the Parameter
	 */
	public static void mrqcof(double[] x, double[] y, double[] sig, int ndata,
			double[] a, int[] ia, int ma,
			double[][] alpha, double[] beta,
			IFunctionTools funcs, double[] chisq) {
	int i;
	int j;
	int k;
	int l;
	int m;
	int mfit = 0;
	double ymod;
	double wt;
	double sig2i;
	double dy;
	double[] dyda = new double[ma];

		for (j = 0; j < ma; j++) {
			if (ia[j] != 0) {
				mfit++;
			}
		}
		for (j = 0; j < mfit; j++) {
			for (k = 0; k <= j; k++) {
				alpha[j][k] = 0.0;
			}
			beta[j] = 0.0;
		}

		chisq[0] = 0.0;
		for (i = 0; i < ndata; i++) {
			ymod = funcs.run(x[i], a, dyda, ma);
			sig2i = 1.0 / (sig[i] * sig[i]);
			dy = y[i] - ymod;
			for (j = -1, l = 0; l < ma; l++) {
				if (ia[l] != 0) {
					wt = dyda[l] * sig2i;
					for (j++, k = 0, m = 0; m <= l; m++) {
						if (ia[m] != 0) {
							alpha[j][k] += wt * dyda[m];
							k++;
						}
					}
					beta[j] += dy * wt;
				}
			}
			chisq[0] += dy * dy * sig2i;
		}
		for (j = 1; j < mfit; j++) {
			for (k = 0; k < j; k++) {
				alpha[k][j] = alpha[j][k];
			}
		}

	}


	static int[] mfit = new int[1];
	static double[] atry, beta, da;
	static double[][] oneda;


	/**
	 *  Description of the Method
	 *
	 *@param  x       Description of the Parameter
	 *@param  y       Description of the Parameter
	 *@param  sig     Description of the Parameter
	 *@param  ndata   Description of the Parameter
	 *@param  a       Description of the Parameter
	 *@param  ia      Description of the Parameter
	 *@param  ma      Description of the Parameter
	 *@param  covar   Description of the Parameter
	 *@param  alpha   Description of the Parameter
	 *@param  chisq   Description of the Parameter
	 *@param  ochisq  Description of the Parameter
	 *@param  alamda  Description of the Parameter
	 *@return         Description of the Return Value
	 */
	public static int mrqmin(double x[], double y[], double sig[], int ndata,
			double a[], int ia[], int ma,
			double[][] covar, double[][] alpha, double[] chisq, double[] ochisq,
			double[] alamda) {
	int i;
	int j;
	int k;
	int l;
	FgaussTools funcs = new FgaussTools();

		if (alamda[0] < 0.0) {
			atry = new double[ma];
			beta = new double[ma];
			da = new double[ma];

			mfit[0] = 0;
			for (j = 0; j < ma; j++) {
				if (ia[j] != 0) {
					mfit[0]++;
				}
			}
			oneda = new double[mfit[0]][1];

			alamda[0] = 0.001;
			mrqcof(x, y, sig, ndata, a, ia, ma, alpha, beta, funcs, chisq);
			ochisq[0] = chisq[0];
			for (j = 0; j < ma; j++) {
				atry[j] = a[j];
			}
		}
		for (j = 0; j < mfit[0]; j++) {
			for (k = 0; k < mfit[0]; k++) {
				covar[j][k] = alpha[j][k];
			}
			covar[j][j] = alpha[j][j] * (1.0 + alamda[0]);
			oneda[j][0] = beta[j];
		}
	int err;
		err = gaussj(covar, mfit[0], oneda, 1);
		if (err == 0) {
			return 0;
		}
		for (j = 0; j < mfit[0]; j++) {
			da[j] = oneda[j][0];
		}
		if (alamda[0] == 0.0) {
			covsrt(covar, ma, ia, mfit);
			return 1;
		}
		for (j = 0, l = 0; l < ma; l++) {
			if (ia[l] != 0) {
				atry[l] = a[l] + da[j++];
			}
		}
		mrqcof(x, y, sig, ndata, atry, ia, ma, covar, da, funcs, chisq);
		if (chisq[0] < ochisq[0]) {
			alamda[0] *= 0.1;
			ochisq[0] = chisq[0];
			for (j = 0; j < mfit[0]; j++) {
				for (k = 0; k < mfit[0]; k++) {
					alpha[j][k] = covar[j][k];
				}
				beta[j] = da[j];
			}
			for (l = 0; l < ma; l++) {
				a[l] = atry[l];
			}
		} else {
			alamda[0] *= 10.0;
			chisq[0] = ochisq[0];
		}
		return 1;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x       Description of the Parameter
	 *@param  result  Description of the Parameter
	 *@param  ma      Description of the Parameter
	 *@param  y       Description of the Parameter
	 */
	public static void Fgauss(float[] x, double[] result, int ma, float[] y) {
	FgaussTools funcs = new FgaussTools();
	double[] dy = new double[3];
	double[] a = new double[3];
		a[0] = result[3 * ma];
		a[1] = result[3 * ma + 1];
		a[2] = result[3 * ma + 2];
		for (int i = 0; i < y.length; i++) {
			y[i] = (float) funcs.run(x[i], a, dy, 3);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  x  Description of the Parameter
	 *@param  a  Description of the Parameter
	 *@return    Description of the Return Value
	 */
	public static float Fgauss(double x, double[] a) {
	FgaussTools funcs = new FgaussTools();
	double[] dy = new double[3];

		return (float) funcs.run(x, a, dy, 3);
	}

}

/**
 *  Description of the Interface
 *
 *@author     Thomas
 *@created    15 octobre 2007
 */
interface IFunctionTools {


	/**
	 *  Main processing method for the IFunctionTools object
	 *
	 *@param  x     Description of the Parameter
	 *@param  a     Description of the Parameter
	 *@param  dyda  Description of the Parameter
	 *@param  na    Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public double run(double x, double[] a, double[] dyda, int na);
}

/**
 *  Description of the Class
 *
 *@author     Thomas
 *@created    15 octobre 2007
 */
class FgaussTools implements IFunctionTools {


	/**
	 *  Main processing method for the FgaussTools object
	 *
	 *@param  x     Description of the Parameter
	 *@param  a     Description of the Parameter
	 *@param  dyda  Description of the Parameter
	 *@param  na    Description of the Parameter
	 *@return       Description of the Return Value
	 */
	public double run(double x, double[] a, double[] dyda, int na) {
	int i;
	double fac;
	double ex;
	double arg;
	double y;
	double W = 1.665109222;
		y = 0.0;
		for (i = 0; i < na; i += 3) {
			arg = W * (x - a[i + 1]) / a[i + 2];
			ex = java.lang.Math.exp(-arg * arg);
			fac = 2.0 * a[i] * ex * arg;
			y += a[i] * ex;
			dyda[i] = ex;
			dyda[i + 1] = W * fac / a[i + 2];
			dyda[i + 2] = fac * arg / a[i + 2];
		}
		return y;
	}
	/*
	 *  EU_HOU END
	 */
}

