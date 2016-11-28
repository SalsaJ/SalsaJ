package ij.gui;
import ij.*;
import java.awt.*;
import javax.swing.*;
import ij.process.Photometer;

/**
 *@author     omar
 */
public class PhotometerParams extends JFrame {
	private static PhotometerParams instance = null;
        private ControlerPhotometerParams C;

	private CheckboxGroup coordcb, skycb, starcb;
	private JTextField xtf, ytf;
        private JTextField radtf, valtf, statf;

	/**
	 *  Constructor for the PhotometerParams object
	 */
	public PhotometerParams() {
		//EU_HOU Bundle
		super(IJ.getBundle().getString("PhotometrySettings"));
                Color gray = ImageJ.backgroundColor;

                GridBagLayout layout = new GridBagLayout();
                getContentPane().setLayout(layout);
                setResizable(false);



JCheckBox cb = new JCheckBox("all auto");





                //AUTO
                JPanel pAA = new JPanel();
                pAA.setLayout(new GridLayout(4,1));
                    JPanel pAA1 = new JPanel();
                    pAA1.add(new Label(IJ.getBundle().getString("PhotomCoord")));
                    Panel pAA2 = new Panel();
                    pAA2.add(new Label(IJ.getBundle().getString("StarRad")));
                    JPanel pAA3 = new JPanel();
                    pAA3.add(new Label(IJ.getBundle().getString("SkyParam")));
                    pAA.add(pAA1);
                    pAA.add(pAA2);
                    pAA.add(pAA3);
                JPanel pAB = new JPanel();
                pAB.setLayout(new GridLayout(4,1));
                    JPanel pAB1 = new JPanel();
                    coordcb = new CheckboxGroup();
                    pAB1.add(new Checkbox(IJ.getBundle().getString("Auto"), coordcb, true));
                    JPanel pAB2 = new JPanel();
                    starcb = new CheckboxGroup();
                    pAB2.add(new Checkbox(IJ.getBundle().getString("AutoFWHM"), starcb, true));
                    JPanel pAB3 = new JPanel();
                    skycb = new CheckboxGroup();
                    pAB3.add(new Checkbox(IJ.getBundle().getString("AutoSky"), skycb, true));
                    pAB.add(pAB1);
                    pAB.add(pAB2);
                    pAB.add(pAB3);
                //MANUEL
                JPanel pBA = new JPanel();
                pBA.setLayout(new GridLayout(4,1));
                    JPanel pBA1 = new JPanel();
                    pBA1.add(new Checkbox(IJ.getBundle().getString("Manual"), coordcb, false));
                    JPanel pBA2 = new JPanel();
                    pBA2.add(new Checkbox(IJ.getBundle().getString("ManStarRad"), starcb, false));
                    JPanel pBA3 = new JPanel();
                    pBA3.add(new Checkbox(IJ.getBundle().getString("ManSkyRad"), skycb, false));
                    JPanel pBA4 = new JPanel();
                    pBA4.add(new Checkbox(IJ.getBundle().getString("Skyval"), skycb, false));
                    pBA.add(pBA1).setBackground(gray);
                    pBA.add(pBA2).setBackground(gray);
                    pBA.add(pBA3).setBackground(gray);
                    pBA.add(pBA4).setBackground(gray);
                //--------------------JBoutton---------------------------------//
		Button bPlus=new Button(" + ");
		Button bMoin=new Button(" - ");
                C=new ControlerPhotometerParams();
                //-------------------setActionCommand-------------------------//
		bPlus.setActionCommand("bPlus");
		bMoin.setActionCommand("bMoin");
                //-------------------addActionListener------------------------//
		bPlus.addActionListener(C);
		bMoin.addActionListener(C);
                JPanel pBB = new JPanel();
                pBB.setLayout(new GridLayout(4,1));                    
                    JPanel pBB1 = new JPanel();
                    xtf = new JTextField(5);
                    ytf = new JTextField(5);
                    pBB1.add(new Label(" X:"));
                    pBB1.add(xtf);
                    pBB1.add(new Label(" Y:"));
                    pBB1.add(ytf);
                    JPanel pBB2 = new JPanel();
                    statf = new JTextField(5);
                    pBB2.add(bMoin);
                    pBB2.add(statf);
                    pBB2.add(bPlus);
                    JPanel pBB3 = new JPanel();
                    radtf = new JTextField(5);
                    pBB3.add(radtf);
                    JPanel pBB4 = new JPanel();
                    valtf = new JTextField(5);
                    pBB4.add(valtf);
                    pBB.add(pBB1).setBackground(gray);
                    pBB.add(pBB2).setBackground(gray);
                    pBB.add(pBB3).setBackground(gray);
                    pBB.add(pBB4).setBackground(gray);
                add(pAA);
                add(pAB);
                add(pBA);
                add(pBB);
		pack();


		instance = this;
		GUI.center(this);
		show();
		WindowManager.addWindow(this);
	}


	/**
	 *  Description of the Method
	 */
	public void dispose() {
		WindowManager.removeWindow(this);
                this.setVisible(false);
		
	}


	/**
	 *  Gets the status attribute of the PhotometerParams object
	 *
	 *@return    The status value
	 */
	public short getStatus() {
	short res = 0;
		//EU_HOU Bundle
		if (!coordcb.getSelectedCheckbox().getLabel().equals(IJ.getBundle().getString("Auto"))) {
			res |= Photometer.COORD;
		}
		if (skycb.getSelectedCheckbox().getLabel().equals(IJ.getBundle().getString("ManSkyRad"))) {
			res |= Photometer.SKY_RAD;
		} else if (skycb.getSelectedCheckbox().getLabel().equals(IJ.getBundle().getString("Skyval"))) {
			res |= Photometer.SKY_VAL;
		} else if (skycb.getSelectedCheckbox().getLabel().equals(IJ.getBundle().getString("AutoSky"))) {
			res |= Photometer.SKY_STAR;
		}
		if (starcb.getSelectedCheckbox().getLabel().equals(IJ.getBundle().getString("ManStarRad"))) {
			res |= Photometer.STAR_RAD;
		}
		return res;
	}


	/**
	 *  Gets the val attribute of the PhotometerParams object
	 *
	 *@param  op                         Description of the Parameter
	 *@return                            The val value
	 *@exception  NumberFormatException  Description of the Exception
	 */
	public float getVal(short op) throws NumberFormatException {
	String s = null;
		if (op == Photometer.STAR_RAD) {
			s = statf.getText();
		} else if (op == Photometer.SKY_RAD) {
			s = radtf.getText();
		}
		if (s != null) {
		int i = Integer.parseInt(s);
			return (float) i;
		}
		if (op == Photometer.SKY_VAL) {
			s = valtf.getText();
		}
	float f = Float.parseFloat(s);
		return f;
	}


	/**
	 *  Gets the coord attribute of the PhotometerParams object
	 *
	 *@return                            The coord value
	 *@exception  NumberFormatException  Description of the Exception
	 */
	public int[] getCoord() throws NumberFormatException {
	int t[] = new int[2];
		t[0] = Integer.parseInt(xtf.getText());
		t[1] = Integer.parseInt(ytf.getText());
		return t;
	}


	/**
	 *  Gets the instance attribute of the PhotometerParams class
	 *
	 *@return    The instance value
	 */
	public static PhotometerParams getInstance() {
		return instance;
	}

        public void setX(int x) {
            xtf.setText(""+x);
	}

        public void setY(int y) {
            ytf.setText(""+y);
	}

        public int getstatf() {
            return Integer.parseInt(statf.getText());
	}

        public void setstatf(int a) {
            statf.setText(""+a);
	}
        
        public void setradtf(int a) {
            radtf.setText(""+a);
	}

        public void setvaltf(int a) {
            valtf.setText(""+a);
	}

        ImagePlus imp=null;
        public void setIMP(ImagePlus imp) {
            this.imp=imp;
	}
        public ImagePlus getIMP() {
            return imp;
	}

        




        


}

