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
package org.openhab.binding.homewizard.internal.devices.p1_meter;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homewizard.internal.devices.HomeWizardEnergyMeterMeasurementPayload;

import com.google.gson.annotations.SerializedName;

/**
 * Class that provides storage for the json objects obtained from HomeWizard devices.
 *
 * @author Gearrel Welvaart - Initial contribution
 *
 *
 */
@NonNullByDefault
public class HomeWizardP1MeterMeasurementPayload extends HomeWizardEnergyMeterMeasurementPayload {
    final String EXTERNAL_GAS_METER = "gas_meter";

    @SerializedName(value = "protocol_version", alternate = "smr_version")
    private int protocolVersion = 0;

    @SerializedName("meter_model")
    private String meterModel = "";

    @SerializedName("any_power_fail_count")
    private int anyPowerFailCount;

    @SerializedName("long_power_fail_count")
    private int longPowerFailCount;

    private double totalGasM3;
    private long gasTimestamp = 0;

    private ArrayList<HomeWizardP1ExternalDevicePayload> external = new ArrayList<HomeWizardP1ExternalDevicePayload>();

    /**
     * Getter for the smart meter version
     *
     * @return The most recent smart meter version obtained from the API
     */
    public int getProtocolVersion() {
        return protocolVersion;
    }

    /**
     * Getter for the meter model
     *
     * @return meter model
     */
    public String getMeterModel() {
        return meterModel;
    }

    /**
     * Getter for the count of any power failures
     *
     * @return count of any power failures
     */
    public int getAnyPowerFailCount() {
        return anyPowerFailCount;
    }

    /**
     * Getter for the count of long power failures
     *
     * @return count of long power failures
     */
    public int getLongPowerFailCount() {
        return longPowerFailCount;
    }

    /**
     * Getter for the total imported gas volume
     *
     * @return total imported gas volume
     */
    public double getTotalGasM3() {
        var gas = external.stream().filter(e -> e.getType().equals(EXTERNAL_GAS_METER)).findFirst();
        try {
            return Double.parseDouble(gas.get().getValue());
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Getter for the time stamp of the last gas update
     *
     * @param zoneId The time zone id for the return value, falls back to systemDefault() when null
     * @return time stamp of the last gas update as ZonedDateTime
     *
     */
    public @Nullable ZonedDateTime getGasTimestamp(@Nullable ZoneId zoneId) throws DateTimeException {
        ZoneId timeZoneId = zoneId == null ? ZoneId.systemDefault() : zoneId;
        var gas = external.stream().filter(e -> e.getType().equals(EXTERNAL_GAS_METER)).findFirst();
        ZonedDateTime zonedDateTime;
        long dtv;
        try {
            dtv = Long.parseLong(gas.get().getTimestamp());
            if (dtv < 1) {
                return null;
            }

            // 210119164000
            int seconds = (int) (dtv % 100);

            dtv /= 100;
            int minutes = (int) (dtv % 100);

            dtv /= 100;
            int hours = (int) (dtv % 100);

            dtv /= 100;
            int day = (int) (dtv % 100);

            dtv /= 100;
            int month = (int) (dtv % 100);

            dtv /= 100;
            int year = (int) (dtv + 2000);

            try {
                zonedDateTime = ZonedDateTime.of(year, month, day, hours, minutes, seconds, 0, timeZoneId);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            try {
                zonedDateTime = ZonedDateTime.of(
                        LocalDateTime.parse(gas.get().getTimestamp(), DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        timeZoneId);
            } catch (Exception ex) {
                return null;
            }
        }

        return zonedDateTime;
    }

    @Override
    public String toString() {
        return super.toString() + "  " + String.format("""
                Data [protocolVersion: %d meterModel: %s anyPowerFailCount: %d longPowerFailCount: %d"
                totalGasM3: %f gasTimestamp: %d"

                """, protocolVersion, meterModel, anyPowerFailCount, longPowerFailCount, totalGasM3, gasTimestamp);
    }
}
