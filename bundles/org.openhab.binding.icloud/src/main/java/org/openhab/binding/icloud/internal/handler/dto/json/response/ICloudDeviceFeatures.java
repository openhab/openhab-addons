/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.icloud.internal.handler.dto.json.response;

import com.google.gson.annotations.SerializedName;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudDeviceFeatures {

    @SerializedName("CLK")
    private boolean clk;

    @SerializedName("CLT")
    private boolean clt;

    @SerializedName("CWP")
    private boolean cwp;

    @SerializedName("KEY")
    private boolean key;

    @SerializedName("KPD")
    private boolean kpd;

    @SerializedName("LCK")
    private boolean lck;

    @SerializedName("LKL")
    private boolean lkl;

    @SerializedName("LKM")
    private boolean lkm;

    @SerializedName("LLC")
    private boolean llc;

    @SerializedName("LMG")
    private boolean lmg;

    @SerializedName("LOC")
    private boolean loc;

    @SerializedName("LST")
    private boolean lst;

    @SerializedName("MCS")
    private boolean mcs;

    @SerializedName("MSG")
    private boolean msg;

    @SerializedName("PIN")
    private boolean pin;

    @SerializedName("REM")
    private boolean rem;

    @SerializedName("SND")
    private boolean snd;

    @SerializedName("SVP")
    private boolean svp;

    @SerializedName("TEU")
    private boolean teu;

    @SerializedName("WIP")
    private boolean wip;

    @SerializedName("WMG")
    private boolean wmg;

    @SerializedName("XRM")
    private boolean xrm;

    @SerializedName("CLT")

    public boolean getCLK() {
        return this.clk;
    }

    public boolean getClt() {
        return this.clt;
    }

    public boolean getCwp() {
        return this.cwp;
    }

    public boolean getKey() {
        return this.key;
    }

    public boolean getKpd() {
        return this.kpd;
    }

    public boolean getLck() {
        return this.lck;
    }

    public boolean getLkl() {
        return this.lkl;
    }

    public boolean getLkm() {
        return this.lkm;
    }

    public boolean getLlc() {
        return this.llc;
    }

    public boolean getLmg() {
        return this.lmg;
    }

    public boolean getLoc() {
        return this.loc;
    }

    public boolean getLst() {
        return this.lst;
    }

    public boolean getMcs() {
        return this.mcs;
    }

    public boolean getMsg() {
        return this.msg;
    }

    public boolean getPin() {
        return this.pin;
    }

    public boolean getRem() {
        return this.rem;
    }

    public boolean getSnd() {
        return this.snd;
    }

    public boolean getSvp() {
        return this.svp;
    }

    public boolean getTeu() {
        return this.teu;
    }

    public boolean getWip() {
        return this.wip;
    }

    public boolean getWmg() {
        return this.wmg;
    }

    public boolean getXrm() {
        return this.xrm;
    }
}
