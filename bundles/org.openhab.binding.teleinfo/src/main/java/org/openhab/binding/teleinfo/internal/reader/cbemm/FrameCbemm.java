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
package org.openhab.binding.teleinfo.internal.reader.cbemm;

import org.openhab.binding.teleinfo.internal.reader.common.FrameAdco;
import org.openhab.binding.teleinfo.internal.reader.common.Ptec;

/**
 * The {@link FrameCbemm} class defines common attributes for CBEMM Teleinfo frames.
 *
 * @author Nicolas SIBERIL - Initial contribution
 */
public abstract class FrameCbemm extends FrameAdco {

    private static final long serialVersionUID = -8500010131430582841L;

    private int isousc;
    private int iinst; // ampères
    private Integer adps; // ampères
    private Integer imax; // ampères
    private Ptec ptec;
    private String motdetat;

    public FrameCbemm() {
        // default constructor
    }

    public int getIsousc() {
        return isousc;
    }

    public void setIsousc(int isousc) {
        this.isousc = isousc;
    }

    public int getIinst() {
        return iinst;
    }

    public void setIinst(int iinst) {
        this.iinst = iinst;
    }

    public Integer getAdps() {
        return adps;
    }

    public void setAdps(Integer adps) {
        this.adps = adps;
    }

    public Integer getImax() {
        return imax;
    }

    public void setImax(Integer imax) {
        this.imax = imax;
    }

    public Ptec getPtec() {
        return ptec;
    }

    public void setPtec(Ptec ptec) {
        this.ptec = ptec;
    }

    public String getMotdetat() {
        return motdetat;
    }

    public void setMotdetat(String motdetat) {
        this.motdetat = motdetat;
    }

}
