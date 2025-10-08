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
 * The {@link ZappiSummary} is a DTO class used to represent a high level
 * summary of a Zappi device. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class ZappiSummary extends BaseSummary {

    // {"dat":"27-11-2020","tim":"16:02:06","ectp2":843,"ectt1":"Internal
    // Load","ectt2":"Grid","ectt3":"None","frq":50.12,"grd":841,"pha":1,"sno":17028110,"sta":1,"vol":2350,"pri":1,"cmt":254,"zmo":1,"tbk":5,"che":0.00,"pst":"A","mgl":50,"sbh":17,"sbk":5,"ectt4":"None","ectt5":"None","ectt6":"None","fwv":"3560S3.054","dst":1,"lck":16}

    @SerializedName("vol")
    @Nullable
    public Float supplyVoltageInTenthVolt;

    @SerializedName("frq")
    @Nullable
    public Float supplyFrequency;
    @SerializedName("pha")
    @Nullable
    public Integer numberOfPhases;
    @SerializedName("lck") // Bit 0: Locked Now, Bit 1: Lock when plugged in, Bit 2: Lock when unplugged.,
                           // Bit 3: Charge
                           // when locked., Bit 4: Charge Session Allowed (Even if locked)
    @Nullable
    public Integer lockingMode;
    @SerializedName("zmo")
    @Nullable
    public Integer chargingMode; // Zappi Mode - 1=Fast, 2=Eco, 3=Eco+, 4=Stop
    @SerializedName("sta")
    @Nullable
    public Integer status; // 0 Starting, 1 Waiting for export, 2 DSR, 3 Diverting, 4 Boosting, 5 Charge
    // Complete
    @SerializedName("pst")
    @Nullable
    public String plugStatus; // Status A=EV Disconnected, B1=EV Connected, B2=Waiting for EV, C1=Charging,
    // C2=Charging
    // Max Power, F=Fault/Restart
    @SerializedName("cmt")
    @Nullable
    public Integer commandTries; // 0-10 Trying, 253 Acked & Failed, 254 Acked & OK, 255 No command has ever been
    // sent
    @SerializedName("pri")
    @Nullable
    public Integer diverterPriority;
    @SerializedName("mgl")
    @Nullable
    public Integer minimumGreenLevel;

    // Overall Measures
    @SerializedName("grd")
    @Nullable
    public Integer gridPower; // Grid consumption
    @SerializedName("gen")
    @Nullable
    public Integer generatedPower; // Generated Watts
    @SerializedName("div")
    @Nullable
    public Integer divertedPower; // Diversion amount Watts
    @SerializedName("che")
    @Nullable
    public Double chargeAdded; // Charge added in KWh

    // Smart Boost
    @SerializedName("sbh")
    @Nullable
    public Integer smartBoostHour;
    @SerializedName("sbm")
    @Nullable
    public Integer smartBoostMinute;
    @SerializedName("sbk")
    @Nullable
    public Double smartBoostCharge;

    // Timed Boost
    @SerializedName("tbh")
    @Nullable
    public Integer timedBoostHour;
    @SerializedName("tbm")
    @Nullable
    public Integer timedBoostMinute;
    @SerializedName("tbk")
    @Nullable
    public Double timedBoostCharge; // - Note charge remaining for boost = tbk-che

    // CT Clamps
    @SerializedName("ectt1")
    @Nullable
    public String clampName1;
    @SerializedName("ectt2")
    @Nullable
    public String clampName2;
    @SerializedName("ectt3")
    @Nullable
    public String clampName3;
    @SerializedName("ectt4")
    @Nullable
    public String clampName4;
    @SerializedName("ectt5")
    @Nullable
    public String clampName5;
    @SerializedName("ectt6")
    @Nullable
    public String clampName6;

    @SerializedName("ectp1")
    @Nullable
    public Integer clampPower1; // in Watts
    @SerializedName("ectp2")
    @Nullable
    public Integer clampPower2;
    @SerializedName("ectp3")
    @Nullable
    public Integer clampPower3;
    @SerializedName("ectp4")
    @Nullable
    public Integer clampPower4;
    @SerializedName("ectp5")
    @Nullable
    public Integer clampPower5;
    @SerializedName("ectp6")
    @Nullable
    public Integer clampPower6;

    public ZappiSummary(long serialNumber) {
        super(serialNumber);
    }

    @Override
    public String toString() {
        return "ZappiSummary [serialNumber=" + serialNumber + ", dat=" + dat + ", tim=" + tim + ", dst=" + dst
                + ", supplyVoltage=" + supplyVoltageInTenthVolt + ", supplyFrequency=" + supplyFrequency
                + ", numberOfPhases=" + numberOfPhases + ", lockingMode=" + lockingMode + ", chargingMode="
                + chargingMode + ", status=" + status + ", plugStatus=" + plugStatus + ", commandTries=" + commandTries
                + ", diverterPriority=" + diverterPriority + ", minimumGreenLevel=" + minimumGreenLevel + ", gridPower="
                + gridPower + ", generatedPower=" + generatedPower + ",  divertedPower=" + divertedPower
                + ", chargeAdded=" + chargeAdded + ", smartBoostHour=" + smartBoostHour + ", smartBoostMinute="
                + smartBoostMinute + ", smartBoostCharge=" + smartBoostCharge + ", timedBoostHour=" + timedBoostHour
                + ", timedBoostMinute=" + timedBoostMinute + ", timedBoostCharge=" + timedBoostCharge + ", clampName1="
                + clampName1 + ", clampName2=" + clampName2 + ", clampName3=" + clampName3 + ", clampName4="
                + clampName4 + ", clampName5=" + clampName5 + ", clampName6=" + clampName6 + ", clampPower1="
                + clampPower1 + ", clampPower2=" + clampPower2 + ", clampPower3=" + clampPower3 + ", clampPower4="
                + clampPower4 + ", clampPower5=" + clampPower5 + ", clampPower6=" + clampPower6 + ", firmwareVersion="
                + firmwareVersion + "]";
    }
}
