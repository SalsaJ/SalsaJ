/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ij.process;

import ij.plugin.FitsDecoder;

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
public class SpectrumOptical extends Spectrum {

    FitsDecoder fd;
    double Wave_ref=589; // wavelength of reference

    public SpectrumOptical(double[] XdataRaw, double[] YdataRaw) {
        super(XdataRaw, YdataRaw);
        this.setOptique(true);
    }

    @Override
     public void computeDisplay() {
        int length = XdataRaw.length;
        XdataDisplay = new double[length];
        YdataDisplay = new double[length];
        float tmp_wave;
        float tmp_freq, tmp_freq0;
        for (int i = 0; i < length; i++) {
            tmp_wave = (float) (fd.getCrval1() + fd.getCdelt1() * ((float) (i))) * 0.1f; // nanometers
            tmp_freq = (float) ((3.0E8 / ((float) tmp_wave * 1.E-9))); // Hz
            //tmp_freq0 = (float) (3.e8 / 589.0e-9); // Hz; 589.6
            tmp_freq0 = (float) (3.e17 / (Wave_ref)); // Hz; 589.6
            switch (display) {
                case Spectrum.DISPLAY_DEFAULT:
                    XdataDisplay[i] = (float) (i);
                    break;
                case Spectrum.DISPLAY_VELOCITY:
                    //xValues[length - 1 - i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001);
                    // thomas 19.09.06
                    XdataDisplay[i] = (float) ((3.e8 * (tmp_freq0 / tmp_freq - 1.0)) * 1e-3);
                    break;
                case Spectrum.DISPLAY_FREQ:
                    XdataDisplay[i] = (float) ((tmp_freq) * 1e-12); // THz
                    break;
                case Spectrum.DISPLAY_WAVELENGTH:
                    XdataDisplay[i] = (float) (tmp_wave);
                    break;
                default:
                    XdataDisplay[i] = (float) (i);
                    break;
            }
        }

        // Y VALUES
        float bzero = 32768;
        float bscale = 1;

        for (int i = 0; i < length; i++) {
            YdataDisplay[i] = bzero + bscale * YdataRaw[i];
        }

    }

    public void setFd(FitsDecoder fd) {
        this.fd = fd;
    }

    public double getWave_ref() {
        return Wave_ref;
    }

    public void setWave_ref(double Wave_ref) {
        this.Wave_ref = Wave_ref;
    }
}
