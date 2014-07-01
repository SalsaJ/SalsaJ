/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.process;

import java.util.*;

/**
 *
 * @author omar
 */
public class PhotometerSave {

    private static PhotometerSave instance = null;
    public Hashtable results = new Hashtable();
    public String s1="";
    public String s2="";

    public PhotometerSave(){
    instance=this;
    }


public static PhotometerSave getInstance() {
		return instance;
	}


}



