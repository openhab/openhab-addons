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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link EddiSummary} is a DTO class used to represent a high level summary
 * of an Eddi device. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class EddiSummary extends BaseSummary {

    // {"eddi":[{"sno":21164284,"dat":"12-11-2022","tim":"21:26:25","ectt1":"Internal
    // Load","ectt2":"None","ectt3":"None","bsm":0,"bst":0,
    // "cmt":253,"dst":1,"div":0,"frq":49.91,"fwv":"3202S4.097","grd":2969,"pha":1,"pri":1,"sta":1,"tz":0,"vol":2395,"che":1.65,
    // "hpri":1,"hno":1,"ht1":"Tank 1","ht2":"Tank
    // 2","r1a":0,"r2a":0,"r1b":0,"r2b":0,"rbc":1,"tp1":45,"tp2":127}]}

    // {"eddi":[{"sno":21164284,"dat":"13-11-2022","tim":"16:50:57","ectp1":502,"ectt1":"Internal
    // Load",
    // "ectt2":"None","ectt3":"None","bsm":1,"bst":0,"cmt":254,"dst":1,"div":502,"frq":49.92,
    // "fwv":"3202S4.097","grd":3302,"pha":1,"pri":1,"sta":4,"tz":0,"vol":2341,"che":3.05,
    // "hpri":1,"hno":1,"ht1":"Tank 1","ht2":"Tank 2",
    // "r1a":0,"r2a":0,"r1b":0,"r2b":0,"rbc":1,"rbt":1188,"tp1":56,"tp2":127}]}

    // Some fields do not appear in the API response when their value is 0. Those
    // fields are initiliased here.

    @SerializedName("bsm")
    @Nullable
    public String boostMode; // 0=off 1=on
    @SerializedName("bst")
    @Nullable
    public String boostTime; // ?
    @SerializedName("rbt")
    @Nullable
    public Integer boostRemaining = 0;
    @SerializedName("cmt")
    @Nullable
    public Integer commandTries; // Command Timer - counts 1 - 10 when command sent, then 254 - success, 253 -
    // failure,
    // 255 - never received any commands
    @SerializedName("che")
    @Nullable
    public Double energyTransferred; // in kWh
    @SerializedName("div")
    @Nullable
    public Integer divertedPower; // in W
    @SerializedName("ectp1")
    @Nullable
    public Integer clampPower1 = 0; // in W
    @SerializedName("ectp2")
    @Nullable
    public Integer clampPower2 = 0; // in W
    @SerializedName("ectp3")
    @Nullable
    public Integer clampPower3 = 0; // in W
    @SerializedName("ectt1")
    @Nullable
    public String clampName1;
    @SerializedName("ectt2")
    @Nullable
    public String clampName2;
    @SerializedName("ectt3")
    @Nullable
    public String clampName3;
    @SerializedName("frq")
    @Nullable
    public Float supplyFrequency; // in Hz
    @SerializedName("grd")
    @Nullable
    public Integer gridPower; // power imported from grid in W
    @SerializedName("gen")
    @Nullable
    public Integer generatedPower; // power generated (if available) in W
    @SerializedName("hno")
    @Nullable
    public Integer activeHeater;
    @SerializedName("hpri")
    @Nullable
    public Integer heaterPriority;
    @SerializedName("ht1")
    @Nullable
    public String heaterName1;
    @SerializedName("ht2")
    @Nullable
    public String heaterName2;
    @SerializedName("pha")
    @Nullable
    public Integer phase;
    @SerializedName("pri")
    @Nullable
    public Integer diverterPriority;
    @SerializedName("sta")
    @Nullable
    public Integer status;
    @SerializedName("r1a")
    @Nullable
    public Integer r1a; // relay?
    @SerializedName("r2a")
    @Nullable
    public Integer r2a;
    @SerializedName("r1b")
    @Nullable
    public Integer r1b;
    @SerializedName("r2b")
    @Nullable
    public Integer r2b;
    @Nullable
    @SerializedName("rbc")
    public Integer rbc;
    @SerializedName("tz")
    @Nullable
    public Integer tz; // time zone?
    @SerializedName("tp1")
    @Nullable
    public Float temperature1;
    @SerializedName("tp2")
    @Nullable
    public Float temperature2;
    @SerializedName("vol")
    @Nullable
    public Float supplyVoltageInTenthVolt;

    @Override
    public String toString() {
        return "EddiSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", firmwareVersion="
                + firmwareVersion + "]";
    }
}
