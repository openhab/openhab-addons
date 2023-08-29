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
        return -1;
    }

    default double getPV1Current() {
        return -1;
    }

    default short getPV1Power() {
        return -1;
    }

    default double getPV2Voltage() {
        return -1;
    }

    default double getPV2Current() {
        return -1;
    }

    default short getPV2Power() {
        return -1;
    }

    default short getPVTotalPower() {
        return (short) (getPV1Power() + getPV2Power());
    }

    default short getPVTotalCurrent() {
        return (short) (getPV1Current() + getPV2Current());
    }

    default double getBatteryVoltage() {
        return -1;
    };

    default double getBatteryCurrent() {
        return -1;
    };

    default short getBatteryPower() {
        return -1;
    }

    default short getBatteryTemperature() {
        return -1;
    }

    default short getBatteryLevel() {
        return -1;
    }

    default short getFeedInPower() {
        return -1;
    }

    //

    default short getPowerUsage() {
        return -1;
    }

    default double getTotalEnergy() {
        return -1;
    }

    default short getTotalBatteryDischargeEnergy() {
        return -1;
    }

    default short getTotalBatteryChargeEnergy() {
        return -1;
    }

    default double getTotalPVEnergy() {
        return -1;
    }

    default short getTotalFeedInEnergy() {
        return -1;
    }

    default double getTotalConsumption() {
        return -1;
    }

    default double getTodayEnergy() {
        return -1;
    }

    default double getTodayFeedInEnergy() {
        return -1;
    }

    default double getTodayConsumption() {
        return -1;
    }

    default double getTodayBatteryDischargeEnergy() {
        return -1;
    }

    default double getTodayBatteryChargeEnergy() {
        return -1;
    }

    default String toStringDetailed() {
        return "WifiSerial = " + getWifiSerial() + ", WifiVersion = " + getWifiVersion() + ", InverterType = "
                + getInverterType() + ", BatteryPower = " + getBatteryPower() + "W, Battery SoC = " + getBatteryLevel()
                + "%, FeedIn Power = " + getFeedInPower() + "W, Total PV Power = " + (getPV1Power() + getPV2Power())
                + "W";
    }
}
