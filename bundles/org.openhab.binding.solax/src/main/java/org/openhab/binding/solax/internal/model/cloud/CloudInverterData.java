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
package org.openhab.binding.solax.internal.model.cloud;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.model.InverterType;

/**
 * The {@link CloudInverterData} Interface for the parsed inverter data in meaningful format. Currently the cloud
 * responds with the same response for all type of inverters, so it's modeled in a single interface.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public interface CloudInverterData {
    boolean isSuccess();

    String getOverallResult();

    int getCode();

    String getInverterSerialNumber();

    String getWifiSerialNumber();

    double getInverterOutputPower();

    double getYieldToday();

    double getYieldTotal();

    double getFeedInPower();

    double getFeedInEnergy();

    double getConsumeEnergy();

    double getFeedInPowerM2();

    double getEPSPowerR();

    double getEPSPowerS();

    double getEPSPowerT();

    InverterType getInverterType();

    double getBatteryLevel();

    double getBatteryPower();

    double getPowerPv1();

    double getPowerPv2();

    double getPowerPv3();

    double getPowerPv4();

    short getInverterWorkModeCode();

    default String getInverterWorkMode() {
        return String.valueOf(getInverterWorkModeCode());
    }

    /**
     * Undocumented in the API so currently it's only an int probably the code of the battery status
     *
     * @return battery status code as int
     */
    int getBatteryStatus();

    @Nullable
    String getRawData();

    double getPVTotalPower();

    ZonedDateTime getUploadTime(ZoneId zoneId);
}
