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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZappiSummary} is a DTO class used to represent a high level summary of a Zappi device. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class ZappiSummary extends BaseSummary {

    private final Logger logger = LoggerFactory.getLogger(ZappiSummary.class);

    // {"dat":"27-11-2020","tim":"16:02:06","ectp2":843,"ectt1":"Internal
    // Load","ectt2":"Grid","ectt3":"None","frq":50.12,"grd":841,"pha":1,"sno":17028110,"sta":1,"vol":2350,"pri":1,"cmt":254,"zmo":1,"tbk":5,"che":0.00,"pst":"A","mgl":50,"sbh":17,"sbk":5,"ectt4":"None","ectt5":"None","ectt6":"None","fwv":"3560S3.054","dst":1,"lck":16}

    @SerializedName("vol")
    public Float supplyVoltageInTenthVolt;

    @SerializedName("frq")
    public Float supplyFrequency;
    @SerializedName("pha")
    public Integer numberOfPhases;
    @SerializedName("lck") // Bit 0: Locked Now, Bit 1: Lock when plugged in, Bit 2: Lock when unplugged., Bit 3: Charge
                           // when locked., Bit 4: Charge Session Allowed (Even if locked)
    public Integer lockingMode;
    @SerializedName("zmo")
    public Integer chargingMode; // Zappi Mode - 1=Fast, 2=Eco, 3=Eco+, 4=Stop
    @SerializedName("sta")
    public Integer status; // 0 Starting, 1 Waiting for export, 2 DSR, 3 Diverting, 4 Boosting, 5 Charge Complete
    @SerializedName("pst")
    public String plugStatus; // Status A=EV Disconnected, B1=EV Connected, B2=Waiting for EV, C1=Charging, C2=Charging
                              // Max Power, F=Fault/Restart
    @SerializedName("cmt")
    public Integer commandTries; // 0-10 Trying, 253 Acked & Failed, 254 Acked & OK, 255 No command has ever been sent
    @SerializedName("pri")
    public Integer diverterPriority;
    @SerializedName("mgl")
    public Integer minimumGreenLevel;

    // Overall Measures
    @SerializedName("grd")
    public Integer gridPower; // Grid consumption
    @SerializedName("gen")
    public Integer generatedPower; // Generated Watts
    @SerializedName("div")
    public Integer divertedPower; // Diversion amount Watts
    @SerializedName("che")
    public Double chargeAdded; // Charge added in KWh

    // Smart Boost
    @SerializedName("sbh")
    public Integer smartBoostHour;
    @SerializedName("sbm")
    public Integer smartBoostMinute;
    @SerializedName("sbk")
    public Double smartBoostCharge;

    // Timed Boost
    @SerializedName("tbh")
    public Integer timedBoostHour;
    @SerializedName("tbm")
    public Integer timedBoostMinute;
    @SerializedName("tbk")
    public Double timedBoostCharge; // - Note charge remaining for boost = tbk-che

    // CT Clamps
    @SerializedName("ectt1")
    public String clampName1;
    @SerializedName("ectt2")
    public String clampName2;
    @SerializedName("ectt3")
    public String clampName3;
    @SerializedName("ectt4")
    public String clampName4;
    @SerializedName("ectt5")
    public String clampName5;
    @SerializedName("ectt6")
    public String clampName6;

    @SerializedName("ectp1")
    public Integer clampPower1; // in Watts
    @SerializedName("ectp2")
    public Integer clampPower2;
    @SerializedName("ectp3")
    public Integer clampPower3;
    @SerializedName("ectp4")
    public Integer clampPower4;
    @SerializedName("ectp5")
    public Integer clampPower5;
    @SerializedName("ectp6")
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

    public void toLogger() {
        logger.info("ZappiSummary:");
        logger.info("serialNumber={}", serialNumber);
        logger.info("date/time={} {}, dst={}", dat, tim, dst);
        logger.info("supplyVoltage={}, supplyFrequency={}, numberOfPhases={}", supplyVoltageInTenthVolt,
                supplyFrequency, numberOfPhases);
        logger.info("lockingMode={}", lockingMode);
        logger.info("chargingMode={}", chargingMode);
        logger.info("status={}", status);
        logger.info("plugStatus={}", plugStatus);
        logger.info("commandTries={}", commandTries);
        logger.info("diverterPriority={}", diverterPriority);
        logger.info("minimumGreenLevel={}", minimumGreenLevel);
        logger.info("gridPower={}, generatedPower={}, divertedPower={}", gridPower, generatedPower, divertedPower);
        logger.info("chargeAdded={}", chargeAdded);
        logger.info("smartBoostTime={}:{}, Charge={}", smartBoostHour, smartBoostMinute, smartBoostCharge);
        logger.info("timedBoostTime={}:{}, Charge={}", timedBoostHour, timedBoostMinute, timedBoostCharge);
        logger.info("clamp1={}, power={}", clampName1, clampPower1);
        logger.info("clamp2={}, power={}", clampName2, clampPower2);
        logger.info("clamp3={}, power={}", clampName3, clampPower3);
        logger.info("clamp4={}, power={}", clampName4, clampPower4);
        logger.info("clamp5={}, power={}", clampName5, clampPower5);
        logger.info("clamp6={}, power={}", clampName6, clampPower6);
        logger.info("firmwareVersion={}", firmwareVersion);
    }
}
