/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sunsynk.internal.api.dto;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RealTimeInData} is the internal class for inverter real time information
 * from a Sun Synk Connect Account.
 * Use for solar string status.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class RealTimeInData {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();
    private double stringPower1;
    private double stringPower2;
    private double stringCurrent1;
    private double stringCurrent2;
    private double stringVoltage1;
    private double stringVoltage2;

    @SuppressWarnings("unused")
    class Data {
        private int pac;
        @SerializedName("grid_tip_power")
        private String gridTipPower = "";
        private double etoday;
        private double etotal;
        private List<PVIV> pvIV = new ArrayList<PVIV>();
        private List<MPPTIV> mpptIV = new ArrayList<MPPTIV>();
    }

    @SuppressWarnings("unused")
    private class PVIV {
        private String id = "";
        private int pvNo;
        private double vpv;
        private double ipv;
        private double ppv;
        private double todayPv;
        private String sn = "";
        private String time = "";
    }

    public RealTimeInData() {
    }

    class MPPTIV { // Empty; no mini inverters
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "success=" + success + ", data=" + data + "]";
    }

    public void stringEval() {
        for (PVIV x : this.data.pvIV) {
            if (x.pvNo == 1) {
                this.stringPower1 = x.ppv;
                this.stringCurrent1 = x.ipv;
                this.stringVoltage1 = x.vpv;
            }
            if (x.pvNo == 2) {
                this.stringPower2 = x.ppv;
                this.stringCurrent2 = x.ipv;
                this.stringVoltage2 = x.vpv;
            }

        }
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }

    public double getString1Power() {
        return this.stringPower1;
    }

    public double getString2Power() {
        return this.stringPower2;
    }

    public double getString1Current() {
        return this.stringCurrent1;
    }

    public double getString2Current() {
        return this.stringCurrent2;
    }

    public double getString1Voltage() {
        return this.stringVoltage1;
    }

    public double getString2Voltage() {
        return this.stringVoltage2;
    }
}
