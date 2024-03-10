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
package org.openhab.binding.solax.internal.model.local;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.InverterType;

/**
 * The {@link LocalInverterData} Interface for the parsed inverter data in meaningful format
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface LocalInverterData {

    @Nullable
    String getWifiSerial();

    @Nullable
    String getWifiVersion();

    InverterType getInverterType();

    @Nullable
    String getRawData();

    default double getPV1Voltage() {
        return Short.MIN_VALUE;
    }

    default double getPV1Current() {
        return Short.MIN_VALUE;
    }

    default short getPV1Power() {
        return Short.MIN_VALUE;
    }

    default double getPV2Voltage() {
        return Short.MIN_VALUE;
    }

    default double getPV2Current() {
        return Short.MIN_VALUE;
    }

    default short getPV2Power() {
        return Short.MIN_VALUE;
    }

    default double getPVTotalPower() {
        return getPV1Power() + getPV2Power();
    }

    default double getPVTotalCurrent() {
        return getPV1Current() + getPV2Current();
    }

    default double getBatteryVoltage() {
        return Short.MIN_VALUE;
    }

    default double getBatteryCurrent() {
        return Short.MIN_VALUE;
    }

    default short getBatteryPower() {
        return Short.MIN_VALUE;
    }

    default short getBatteryTemperature() {
        return Short.MIN_VALUE;
    }

    default short getInverterTemperature1() {
        return Short.MIN_VALUE;
    }

    default short getInverterTemperature2() {
        return Short.MIN_VALUE;
    }

    default short getBatteryLevel() {
        return Short.MIN_VALUE;
    }

    default short getFeedInPower() {
        return Short.MIN_VALUE;
    }

    default short getPowerUsage() {
        return Short.MIN_VALUE;
    }

    default double getTotalEnergy() {
        return Short.MIN_VALUE;
    }

    default short getTotalBatteryDischargeEnergy() {
        return Short.MIN_VALUE;
    }

    default short getTotalBatteryChargeEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTotalPVEnergy() {
        return Short.MIN_VALUE;
    }

    default short getTotalFeedInEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTotalConsumption() {
        return Short.MIN_VALUE;
    }

    default double getTodayEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTodayFeedInEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTodayConsumption() {
        return Short.MIN_VALUE;
    }

    default double getTodayBatteryDischargeEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTodayBatteryChargeEnergy() {
        return Short.MIN_VALUE;
    }

    default double getInverterVoltage() {
        return Short.MIN_VALUE;
    }

    default double getInverterCurrent() {
        return Short.MIN_VALUE;
    }

    default short getInverterOutputPower() {
        return Short.MIN_VALUE;
    }

    default double getInverterFrequency() {
        return Short.MIN_VALUE;
    }

    default double getVoltagePhase1() {
        return Short.MIN_VALUE;
    }

    default double getVoltagePhase2() {
        return Short.MIN_VALUE;
    }

    default double getVoltagePhase3() {
        return Short.MIN_VALUE;
    }

    default double getCurrentPhase1() {
        return Short.MIN_VALUE;
    }

    default double getCurrentPhase2() {
        return Short.MIN_VALUE;
    }

    default double getCurrentPhase3() {
        return Short.MIN_VALUE;
    }

    default short getOutputPowerPhase1() {
        return Short.MIN_VALUE;
    }

    default short getOutputPowerPhase2() {
        return Short.MIN_VALUE;
    }

    default short getOutputPowerPhase3() {
        return Short.MIN_VALUE;
    }

    default short getTotalOutputPower() {
        return Short.MIN_VALUE;
    }

    default double getFrequencyPhase1() {
        return Short.MIN_VALUE;
    }

    default double getFrequencyPhase2() {
        return Short.MIN_VALUE;
    }

    default double getFrequencyPhase3() {
        return Short.MIN_VALUE;
    }

    default short getInverterWorkModeCode() {
        return Short.MIN_VALUE;
    }

    default String getInverterWorkMode() {
        return String.valueOf(getInverterWorkModeCode());
    }

    default String toStringDetailed() {
        return "WifiSerial = " + getWifiSerial() + ", WifiVersion = " + getWifiVersion() + ", InverterType = "
                + getInverterType() + ", BatteryPower = " + getBatteryPower() + "W, Battery SoC = " + getBatteryLevel()
                + "%, FeedIn Power = " + getFeedInPower() + "W, Total PV Power = " + (getPV1Power() + getPV2Power())
                + "W";
    }
}
