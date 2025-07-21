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
package org.openhab.binding.ondilo.internal.dto;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link PoolConfiguration} DTO for representing Ondilo pool configurations.
 *
 * @author MikeTheTux - Initial contribution
 */
public class PoolConfiguration {
    /*
     * Example JSON representation:
     * {
     * "temperature_low": 10,
     * "temperature_high": 30,
     * "ph_low": 7.6,
     * "ph_high": 8.5,
     * "orp_low": 400,
     * "orp_high": 900,
     * "salt_low": 3000,
     * "salt_high": 5000,
     * "tds_low": 250,
     * "tds_high": 2000,
     * "pool_guy_number": "0123456789",
     * "maintenance_day": 2
     * }
     */
    @SerializedName("temperature_low")
    public int temperatureLow;

    @SerializedName("temperature_high")
    public int temperatureHigh;

    @SerializedName("ph_low")
    public double phLow;

    @SerializedName("ph_high")
    public double phHigh;

    @SerializedName("orp_low")
    public int orpLow;

    @SerializedName("orp_high")
    public int orpHigh;

    @SerializedName("salt_low")
    public int saltLow;

    @SerializedName("salt_high")
    public int saltHigh;

    @SerializedName("tds_low")
    public int tdsLow;

    @SerializedName("tds_high")
    public int tdsHigh;

    @SerializedName("pool_guy_number")
    public String poolGuyNumber;

    @SerializedName("maintenance_day")
    public int maintenanceDay;

    public String getMaintenanceDay(Locale locale) {
        DayOfWeek dayOfWeek = DayOfWeek.of((maintenanceDay % 7) + 1); // Change from week start Sunday to Monday
        return dayOfWeek.getDisplayName(TextStyle.FULL, locale);
    }
}
