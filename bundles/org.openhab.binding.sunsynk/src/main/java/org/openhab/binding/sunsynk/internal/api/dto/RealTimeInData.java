/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
 * Use for solar status.
 * 
 * @author Lee Charlton - Initial contribution
 */

@NonNullByDefault
public class RealTimeInData {
    private int code;
    private String msg = "";
    private boolean success;
    private Data data = new Data();
    private double solarPower;

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
        private double ppv; // sum for all power
        private double todayPv;
        private String sn = "";
        private String time = "";
    }

    public RealTimeInData() {
    }

    class MPPTIV { // Empty; no solar panels
    }

    public double getetoday() {
        return this.data.etoday;
    }

    public double getetotal() {
        return this.data.etotal;
    }

    public String toString() {
        return "Content [code=" + code + ", msg=" + msg + "sucess=" + success + ", data=" + data + "]";
    }

    public void sumPVIV() {
        double solarPower = 0.0;
        for (PVIV x : this.data.pvIV) {
            this.solarPower = solarPower + x.ppv;
        }
    }

    public double getPVIV() {
        return this.solarPower;
    }
}
