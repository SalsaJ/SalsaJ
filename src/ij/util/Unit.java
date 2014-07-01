/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.util;

import java.util.HashMap;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Unit {
// conversion 

    HashMap<String, Integer> units;

    public Unit() {
        // Hz
        units.put("Hz", 0);
        units.put("kHz", 3);
        units.put("MHz", 6);
        units.put("THz", 9);
        // wave
        units.put("m", 0);
        units.put("mm", -3);
        units.put("cm", -2);
        units.put("um", -6);
        units.put("nm", -9);
        units.put("A", -10);
        // speed
        units.put("ms", 0);
        units.put("kms", 3);

    }

    public double conversion(double val, String unit1, String unit2) {
        int u1 = units.get(unit1.substring(0, 1)).intValue();
        int u2 = units.get(unit1.substring(0, 1)).intValue();

        return val * Math.pow(10, u1 - u2);
    }
}
