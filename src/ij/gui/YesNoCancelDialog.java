//EU_HOU
package ij.gui;
import ij.IJ;
import java.awt.*;
import java.awt.event.*;
/*
 *  EU_HOU CHANGES
 */
import java.util.ResourceBundle;
//just for visibility of 'getString()'
/*
 *  EU_HOU END
 */
/**
 *  A modal dialog box with a one line message and "Yes", "No" and "Cancel"
 *  buttons.
 *
 *@author     Thomas
 *@created    6 novembre 2007
 */
public class YesNoCancelDialog extends Dialog implements ActionListener, KeyListener {
	private Button yesB, noB, cancelB;
	private boolean cancelPressed, yesPressed;
	private boolean firstPaint = true;


	/**
	 *  Constructor for the YesNoCancelDialog object
	 *
	 *@param  parent  Description of the Parameter
	 *@param  title   Description of the Parameter
	 *@param  msg     Description of the Parameter
	 */
	public YesNoCancelDialog(Frame parent, String title, String msg) {
		super(parent, title, true);
		setLayout(new BorderLayout());

	Panel panel = new Panel();

		panel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));

	MultiLineLabel message = new MultiLineLabel(msg);

		message.setFont(new Font("Dialog", Font.PLAIN, 12));
		panel.add(message);
		add("North", panel);

		panel = new Panel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 8));
		if (IJ.isMacintosh() && msg.startsWith("Save")) {
			//EU_HOU Bundle
			yesB = new Button(IJ.getBundle().getString("YNCDialYes"));
			noB = new Button(IJ.getBundle().getString("YNCDialNo"));
			cancelB = new Button(IJ.getBundle().getString("YNCDialCan"));
		} else {
			//EU_HOU Bundle
			yesB = new Button(IJ.getBundle().getString("YNCDialYes"));
			noB = new Button(IJ.getBundle().getString("YNCDialNo"));
			cancelB = new Button(IJ.getBundle().getString("YNCDialCan"));
		}
		yesB.addActionListener(this);
		noB.addActionListener(this);
		cancelB.addActionListener(this);
		yesB.addKeyListener(this);
		noB.addKeyListener(this);
		cancelB.addKeyListener(this);
		if (IJ.isMacintosh()) {
			panel.add(noB);
			panel.add(cancelB);
			panel.add(yesB);
			setResizable(false);
		} else {
			panel.add(yesB);
			panel.add(noB);
			panel.add(cancelB);
		}
		add("South", panel);
		pack();
		GUI.center(this);
		show();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == cancelB) {
			cancelPressed = true;
		} else if (e.getSource() == yesB) {
			yesPressed = true;
		}
		closeDialog();
	}


	/**
	 *  Returns true if the user dismissed dialog by pressing "Cancel".
	 *
	 *@return    Description of the Return Value
	 */
	public boolean cancelPressed() {
		return cancelPressed;
	}


	/**
	 *  Returns true if the user dismissed dialog by pressing "Yes".
	 *
	 *@return    Description of the Return Value
	 */
	public boolean yesPressed() {
		return yesPressed;
	}


	/**
	 *  Description of the Method
	 */
	void closeDialog() {
		setVisible(false);
		dispose();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyPressed(KeyEvent e) {
	int keyCode = e.getKeyCode();

		IJ.setKeyDown(keyCode);
		if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_Y || keyCode == KeyEvent.VK_S) {
			yesPressed = true;
			closeDialog();
		} else if (keyCode == KeyEvent.VK_N || keyCode == KeyEvent.VK_D) {
			closeDialog();
		} else if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_C) {
			cancelPressed = true;
			closeDialog();
			IJ.resetEscape();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyReleased(KeyEvent e) {
	int keyCode = e.getKeyCode();

		IJ.setKeyUp(keyCode);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of the Parameter
	 */
	public void keyTyped(KeyEvent e) { }


	/**
	 *  Description of the Method
	 *
	 *@param  g  Description of the Parameter
	 */
	public void paint(Graphics g) {
		super.paint(g);
		if (firstPaint) {
			yesB.requestFocus();
			firstPaint = false;
		}
	}

}

