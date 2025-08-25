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
 * The {@link ZappiMinuteHistoryEntry} is a DTO class used to hold a minute slot of historic data. It's used to
 * deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
public class ZappiMinuteHistoryEntry {

    private static final Integer SECONDS_PER_MINUTE = 60;

    // {"min":2,"hr":12,"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":15720,"exp":480,"gep":45600,"v1":2385,"frq":4992,"pect1":16140,"nect1":480,"nect2":120,"nect3":1920}

    @SerializedName("min")
    public Integer minute = 0;
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

    @SerializedName("v1")
    public Integer supplyVoltage = 0;
    @SerializedName("frq")
    public Integer supplyFrequency = 0;

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

    @SerializedName("pect1")
    public Integer positiveClampWattSeconds1 = 0;
    @SerializedName("nect1")
    public Integer negativeClampWattSeconds1 = 0;
    @SerializedName("pect2")
    public Integer positiveClampWattSeconds2 = 0;
    @SerializedName("nect2")
    public Integer negativeClampWattSeconds2 = 0;
    @SerializedName("pect3")
    public Integer positiveClampWattSeconds3 = 0;
    @SerializedName("nect3")
    public Integer negativeClampWattSeconds3 = 0;

    public Integer getImportedWattMinutes() {
        return importedWattSeconds / SECONDS_PER_MINUTE;
    };

    public Integer getExportedWattMinutes() {
        return exportedWattSeconds / SECONDS_PER_MINUTE;
    };

    public Integer getGeneratedWattMinutes() {
        return generatedWattSeconds / SECONDS_PER_MINUTE;
    };

    public Integer getGeneratedNegativeWattMinutes() {
        return generatedNegativeWattSeconds / SECONDS_PER_MINUTE;
    };

    public Integer getZappiDivertedWattMinutes() {
        return zappiDivertedWattSeconds / SECONDS_PER_MINUTE;
    };

    public Integer getZappiImportedWattMinutes() {
        return zappiImportedWattSeconds / SECONDS_PER_MINUTE;
    };
}
