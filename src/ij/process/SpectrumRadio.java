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
public class SpectrumRadio extends Spectrum {

    FitsDecoder fd = null;
    double Wave_ref = 589; // wavelength of reference

    public SpectrumRadio(double[] XdataRaw, double[] YdataRaw) {
        super(XdataRaw, YdataRaw);
        this.setOptique(false);
    }

    public void setFd(FitsDecoder fd) {
        this.fd = fd;
    }

    @Override
    public void computeDisplay() {

        double restfreq = fd.getRestfreq();
        double deltav;
        int length = XdataRaw.length;
        XdataDisplay = new double[length];

        if (restfreq == 0.0) {
            restfreq = fd.getCrval1();
        }
        if ((fd.getDeltav() == 0.0) & (display == Spectrum.DISPLAY_VELOCITY)) {
            deltav = -3.0E8 * fd.getCdelt1() / restfreq;
            System.out.println(deltav);
        } else {
            deltav = fd.getDeltav();
        }
        for (int i = 0; i < length; i++) {
            switch (display) {
                case Spectrum.DISPLAY_DEFAULT:
                    XdataDisplay[i] = (float) (i);
                    break;
                case Spectrum.DISPLAY_VELOCITY:
                    //xValues[length - 1 - i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001);
                    // thomas 19.09.06
                    XdataDisplay[i] = (float) (((i + 1 - length / 2) * deltav + fd.getVelolsr()) * 0.001) * -1.0f;
                    break;
                case Spectrum.DISPLAY_FREQ:
                    XdataDisplay[i] = (float) ((restfreq + fd.getCdelt1() * ((float) (i) - fd.getCrpix1())) * 0.000001);
                    break;
                case Spectrum.DISPLAY_WAVELENGTH:
                    ///////////////////////////////////////////////
                    //xValues[i] = (float) (fd.getCrval1() - (float) (i));
                    XdataDisplay[i] = (float) (3.0E+10 / (restfreq + fd.getCdelt1() * (fd.getCrpix1() - (float) (i))));

                    break;
                default:
                    XdataDisplay[i] = (float) (i);
                    break;
            }
        }

        YdataDisplay = new double[length];

        double bzero = fd.getBzero();
        double bscale = fd.getBscale();

//        double[] coeff = cal.getCoefficients();
//        if (coeff == null) {
//            coeff = new double[2];
//            coeff[0] = 0.0;
//            coeff[1] = 1.0;
//
//        }
//        bzero = bscale * coeff[0] + bzero;
//        //coeff[1]*fd.getBzero()+coeff[0];
//        bscale = coeff[1] * bscale;
        for (int i = 0; i < length; i++) {
            if (display == Spectrum.DISPLAY_DEFAULT) {
                YdataDisplay[i] = (float) (bscale * YdataRaw[i] + bzero);
            } else {
                YdataDisplay[length - 1 - i] = (float) (bscale * YdataRaw[i] + bzero);
            }
        }

    }

    private void computeDisplaySpecData() {
        /////////////////// COMPUTE VALUES
        double tmp_wave, tmp_freq, tmp_freq0;
        int length = XdataRaw.length;
        double[] xtemp = XdataRaw;
        double[] ytemp = YdataRaw;
        XdataDisplay = new double[length];
        YdataDisplay = new double[length];
        for (int i = 0; i < length; i++) {
            tmp_wave = xtemp[i] / 10; // A to nanometers
            tmp_freq = (float) ((3.0E8 / ((float) tmp_wave * 1.E-9))); // Hz
            //tmp_freq0 = (float) (3.e8 / 589.0e-9); // Hz; 589.6
            tmp_freq0 = (float) (3.e17 / (Wave_ref)); // Hz; 589.6
            switch (display) {
                case Spectrum.DISPLAY_DEFAULT:
                    //XdataDisplay[i] = (float) (i);
                    XdataDisplay[i] = xtemp[i];
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
        System.arraycopy(ytemp, 0, YdataDisplay, 0, length);
    }
}
