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
import org.openhab.binding.solax.internal.connectivity.rawdata.RawDataBean;

/**
 * The {@link InverterData} interface should implement the interface that returns the parsed data in human readable code
 * and format.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface InverterData extends RawDataBean {
    @Nullable
    String getWifiSerial();

    @Nullable
    String getWifiVersion();

    InverterType getInverterType();

    short getInverterVoltage();

    short getInverterCurrent();

    short getInverterOutputPower();

    short getInverterFrequency();

    short getPV1Voltage();

    short getPV1Current();

    short getPV1Power();

    short getPV2Voltage();

    short getPV2Current();

    short getPV2Power();

    default short getPVTotalPower() {
        return (short) (getPV1Power() + getPV2Power());
    }

    default short getPVTotalCurrent() {
        return (short) (getPV1Current() + getPV2Current());
    }

    short getBatteryVoltage(); // V / 100

    short getBatteryCurrent(); // A / 100

    short getBatteryPower(); // W

    short getBatteryTemperature(); // temperature C

    short getBatterySoC(); // % battery SoC

    long getOnGridTotalYield(); // KWh total Yeld from the sun (to the grid?)

    short getOnGridDailyYield(); // KWh daily Yeld from the sun (to the grid?)

    long getTotalFeedInEnergy(); // KWh all times

    long getTotalConsumption(); // KWh all times

    short getFeedInPower();

    default String toStringDetailed() {
        return "WifiSerial = " + getWifiSerial() + ", WifiVersion = " + getWifiVersion() + ", InverterType = "
                + getInverterType() + ", InverterVoltage = " + getInverterVoltage() + "V, InverterCurrent = "
                + getInverterCurrent() + "A, InverterPower = " + getInverterOutputPower() + "W, BatteryPower = "
                + getBatteryPower() + "W, Battery SoC = " + getBatterySoC() + "%, FeedIn Power = " + getFeedInPower()
                + "W, Total PV Power = " + (getPV1Power() + getPV2Power()) + "W, Total Consumption = "
                + getTotalConsumption() + "kWh, Total Feed-in Energy = " + getTotalFeedInEnergy()
                + "kWh, Total On-Grid Yield = " + getOnGridTotalYield() + "kWh.";
    }
}
