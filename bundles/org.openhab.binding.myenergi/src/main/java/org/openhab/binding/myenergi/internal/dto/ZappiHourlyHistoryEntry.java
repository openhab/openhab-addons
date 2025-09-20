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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZappiHourlyHistoryEntry} is a DTO class used to hold an hourly slot of historic data. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class ZappiHourlyHistoryEntry {

    private static final Integer SECONDS_PER_HOUR = 3600;

    // {"hr":13,"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":1760580,"exp":120,"gep":1203060}

    @SerializedName("hr")
    public Integer hour = 0;
    @SerializedName("dom")
    public Integer day;
    @SerializedName("mon")
    public Integer month;
    @SerializedName("yr")
    public Integer year;
    @SerializedName("dow")
    public String dayOfWeek;

    @SerializedName("imp")
    public Integer importedWattSeconds = 0;
    @SerializedName("exp")
    public Integer exportedWattSeconds = 0;
    @SerializedName("gep")
    public Integer generatedWattSeconds = 0;
    @SerializedName("gen")
    public Integer generatedNegativeWattSeconds = 0;
    @SerializedName("h1d")
    public Integer zappiDivertedWattSeconds = 0;
    @SerializedName("h1b")
    public Integer zappiImportedWattSeconds = 0;

    public Integer getImportedWattHours() {
        return importedWattSeconds / SECONDS_PER_HOUR;
    };

    public Integer getExportedWattHours() {
        return exportedWattSeconds / SECONDS_PER_HOUR;
    };

    public Integer getGeneratedWattHours() {
        return generatedWattSeconds / SECONDS_PER_HOUR;
    };

    public Integer getGeneratedNegativeWattHours() {
        return generatedNegativeWattSeconds / SECONDS_PER_HOUR;
    };

    public Integer getZappiDivertedWattHours() {
        return zappiDivertedWattSeconds / SECONDS_PER_HOUR;
    };

    public Integer getZappiImportedWattHours() {
        return zappiImportedWattSeconds / SECONDS_PER_HOUR;
    };

    @Override
    public String toString() {
        return "ZappiHourlyHistoryEntry [hour=" + hour + ", day=" + day + ", month=" + month + ", year=" + year
                + ", dayOfWeek=" + dayOfWeek + ", importedWattSeconds=" + importedWattSeconds + ", exportedWattSeconds="
                + exportedWattSeconds + ", generatedWattSeconds=" + generatedWattSeconds
                + ", generatedNegativeWattSeconds=" + generatedNegativeWattSeconds + ", zappiDivertedWattSeconds="
                + zappiDivertedWattSeconds + ", zappiImportedWattSeconds=" + zappiImportedWattSeconds + "]";
    }
}
