/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EddiSummary} is a DTO class used to represent a high level summary of an Eddi device. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class EddiSummary extends BaseSummary {

    // {"eddi":[{"sno":21164284,"dat":"12-11-2022","tim":"21:26:25","ectt1":"Internal
    // Load","ectt2":"None","ectt3":"None","bsm":0,"bst":0,
    // "cmt":253,"dst":1,"div":0,"frq":49.91,"fwv":"3202S4.097","grd":2969,"pha":1,"pri":1,"sta":1,"tz":0,"vol":2395,"che":1.65,
    // "hpri":1,"hno":1,"ht1":"Tank 1","ht2":"Tank 2","r1a":0,"r2a":0,"r1b":0,"r2b":0,"rbc":1,"tp1":45,"tp2":127}]}

    // {"eddi":[{"sno":21164284,"dat":"13-11-2022","tim":"16:50:57","ectp1":502,"ectt1":"Internal Load",
    // "ectt2":"None","ectt3":"None","bsm":1,"bst":0,"cmt":254,"dst":1,"div":502,"frq":49.92,
    // "fwv":"3202S4.097","grd":3302,"pha":1,"pri":1,"sta":4,"tz":0,"vol":2341,"che":3.05,
    // "hpri":1,"hno":1,"ht1":"Tank 1","ht2":"Tank 2",
    // "r1a":0,"r2a":0,"r1b":0,"r2b":0,"rbc":1,"rbt":1188,"tp1":56,"tp2":127}]}

    // Some fields do not appear in the API response when their value is 0. Those fields are initiliased here.

    @SerializedName("bsm")
    public String boostMode; // 0=off 1=on
    @SerializedName("bst")
    public String boostTime; // ?
    @SerializedName("rbt")
    public Integer boostRemaining = 0;
    @SerializedName("cmt")
    public Integer commandTries; // Command Timer - counts 1 - 10 when command sent, then 254 - success, 253 - failure,
                                 // 255 - never received any commands
    @SerializedName("che")
    public Double energyTransferred; // in kWh
    @SerializedName("div")
    public Integer divertedPower; // in W
    @SerializedName("ectp1")
    public Integer clampPower1 = 0; // in W
    @SerializedName("ectp2")
    public Integer clampPower2 = 0; // in W
    @SerializedName("ectp3")
    public Integer clampPower3 = 0; // in W
    @SerializedName("ectt1")
    public String clampName1;
    @SerializedName("ectt2")
    public String clampName2;
    @SerializedName("ectt3")
    public String clampName3;
    @SerializedName("frq")
    public Float supplyFrequency; // in Hz
    @SerializedName("grd")
    public Integer gridPower; // power imported from grid in W
    @SerializedName("gen")
    public Integer generatedPower; // power generated (if available) in W
    @SerializedName("hno")
    public Integer activeHeater;
    @SerializedName("hpri")
    public Integer heaterPriority;
    @SerializedName("ht1")
    public String heaterName1;
    @SerializedName("ht2")
    public String heaterName2;
    @SerializedName("pha")
    public Integer phase;
    @SerializedName("pri")
    public Integer diverterPriority;
    @SerializedName("sta")
    public Integer status;
    @SerializedName("r1a")
    public Integer r1a; // relay?
    @SerializedName("r2a")
    public Integer r2a;
    @SerializedName("r1b")
    public Integer r1b;
    @SerializedName("r2b")
    public Integer r2b;
    @SerializedName("rbc")
    public Integer rbc;
    @SerializedName("tz")
    public Integer tz; // time zone?
    @SerializedName("tp1")
    public Float temperature1;
    @SerializedName("tp2")
    public Float temperature2;
    @SerializedName("vol")
    public Float supplyVoltageInTenthVolt;

    @Override
    public String toString() {
        return "EddiSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", firmwareVersion="
                + firmwareVersion + "]";
    }
}
