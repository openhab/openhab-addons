/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link InverterData} Interface for the parsed inverter data in meaningful format
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface InverterData {

    @Nullable
    String getWifiSerial();

    @Nullable
    String getWifiVersion();

    InverterType getInverterType();

    @Nullable
    String getRawData();

    default double getPV1Voltage() {
        return Integer.MIN_VALUE;
    }

    default double getPV1Current() {
        return Integer.MIN_VALUE;
    }

    default short getPV1Power() {
        return Short.MIN_VALUE;
    }

    default double getPV2Voltage() {
        return Integer.MIN_VALUE;
    }

    default double getPV2Current() {
        return Integer.MIN_VALUE;
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
        return Integer.MIN_VALUE;
    };

    default double getBatteryCurrent() {
        return Integer.MIN_VALUE;
    };

    default short getBatteryPower() {
        return Short.MIN_VALUE;
    }

    default short getBatteryTemperature() {
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
        return Integer.MIN_VALUE;
    }

    default short getTotalBatteryDischargeEnergy() {
        return Short.MIN_VALUE;
    }

    default short getTotalBatteryChargeEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTotalPVEnergy() {
        return Integer.MIN_VALUE;
    }

    default short getTotalFeedInEnergy() {
        return Short.MIN_VALUE;
    }

    default double getTotalConsumption() {
        return Integer.MIN_VALUE;
    }

    default double getTodayEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayFeedInEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayConsumption() {
        return Integer.MIN_VALUE;
    }

    default double getTodayBatteryDischargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default double getTodayBatteryChargeEnergy() {
        return Integer.MIN_VALUE;
    }

    default String toStringDetailed() {
        return "WifiSerial = " + getWifiSerial() + ", WifiVersion = " + getWifiVersion() + ", InverterType = "
                + getInverterType() + ", BatteryPower = " + getBatteryPower() + "W, Battery SoC = " + getBatteryLevel()
                + "%, FeedIn Power = " + getFeedInPower() + "W, Total PV Power = " + (getPV1Power() + getPV2Power())
                + "W";
    }
}
