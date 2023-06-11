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
package org.openhab.binding.icloud.internal.json.response;

/**
 * Serializable class to parse json response received from the Apple server.
 *
 * @author Patrik Gfeller - Initial Contribution
 *
 */
public class ICloudDeviceFeatures {
    private boolean CLK;

    private boolean CLT;

    private boolean CWP;

    private boolean KEY;

    private boolean KPD;

    private boolean LCK;

    private boolean LKL;

    private boolean LKM;

    private boolean LLC;

    private boolean LMG;

    private boolean LOC;

    private boolean LST;

    private boolean MCS;

    private boolean MSG;

    private boolean PIN;

    private boolean REM;

    private boolean SND;

    private boolean SVP;

    private boolean TEU;

    private boolean WIP;

    private boolean WMG;

    private boolean XRM;

    public boolean getCLK() {
        return this.CLK;
    }

    public boolean getCLT() {
        return this.CLT;
    }

    public boolean getCWP() {
        return this.CWP;
    }

    public boolean getKEY() {
        return this.KEY;
    }

    public boolean getKPD() {
        return this.KPD;
    }

    public boolean getLCK() {
        return this.LCK;
    }

    public boolean getLKL() {
        return this.LKL;
    }

    public boolean getLKM() {
        return this.LKM;
    }

    public boolean getLLC() {
        return this.LLC;
    }

    public boolean getLMG() {
        return this.LMG;
    }

    public boolean getLOC() {
        return this.LOC;
    }

    public boolean getLST() {
        return this.LST;
    }

    public boolean getMCS() {
        return this.MCS;
    }

    public boolean getMSG() {
        return this.MSG;
    }

    public boolean getPIN() {
        return this.PIN;
    }

    public boolean getREM() {
        return this.REM;
    }

    public boolean getSND() {
        return this.SND;
    }

    public boolean getSVP() {
        return this.SVP;
    }

    public boolean getTEU() {
        return this.TEU;
    }

    public boolean getWIP() {
        return this.WIP;
    }

    public boolean getWMG() {
        return this.WMG;
    }

    public boolean getXRM() {
        return this.XRM;
    }
}
