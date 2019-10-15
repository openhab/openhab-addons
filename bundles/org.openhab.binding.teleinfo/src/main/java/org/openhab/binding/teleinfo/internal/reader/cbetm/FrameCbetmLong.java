/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.teleinfo.internal.reader.cbetm;

import org.openhab.binding.teleinfo.internal.reader.common.Ptec;

/**
 * The {@link FrameCbetmLong} class defines common attributes for CBETM Long Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class FrameCbetmLong extends FrameCbetm {

    private static final long serialVersionUID = -2527584397688316017L;

    private int isousc;
    private Integer imax1; // ampères
    private Integer imax2; // ampères
    private Integer imax3; // ampères
    private Ptec ptec;
    private int pmax; // W
    private int papp; // Volt.ampères
    private String motdetat;
    private String ppot;

    public FrameCbetmLong() {
        // default constructor
    }

    public int getIsousc() {
        return isousc;
    }

    public void setIsousc(int isousc) {
        this.isousc = isousc;
    }

    public Integer getImax1() {
        return imax1;
    }

    public void setImax1(Integer imax1) {
        this.imax1 = imax1;
    }

    public Integer getImax2() {
        return imax2;
    }

    public void setImax2(Integer imax2) {
        this.imax2 = imax2;
    }

    public Integer getImax3() {
        return imax3;
    }

    public void setImax3(Integer imax3) {
        this.imax3 = imax3;
    }

    public Ptec getPtec() {
        return ptec;
    }

    public void setPtec(Ptec ptec) {
        this.ptec = ptec;
    }

    public int getPmax() {
        return pmax;
    }

    public void setPmax(int pmax) {
        this.pmax = pmax;
    }

    public int getPapp() {
        return papp;
    }

    public void setPapp(int papp) {
        this.papp = papp;
    }

    public String getMotdetat() {
        return motdetat;
    }

    public void setMotdetat(String motdetat) {
        this.motdetat = motdetat;
    }

    public String getPpot() {
        return ppot;
    }

    public void setPpot(String ppot) {
        this.ppot = ppot;
    }

}
