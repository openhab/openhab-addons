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
package org.openhab.binding.myenergi.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link ZappiMinuteHistoryEntry} is a DTO class used to hold a minute slot
 * of historic data. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class ZappiMinuteHistoryEntry {

    private static final Integer SECONDS_PER_MINUTE = 60;

    // {"min":2,"hr":12,"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":15720,"exp":480,"gep":45600,"v1":2385,"frq":4992,"pect1":16140,"nect1":480,"nect2":120,"nect3":1920}

    @SerializedName("min")
    @Nullable
    public Integer minute = 0;
    @SerializedName("hr")
    @Nullable
    public Integer hour = 0;
    @SerializedName("dom")
    @Nullable
    public Integer day;
    @SerializedName("mon")
    @Nullable
    public Integer month;
    @SerializedName("yr")
    @Nullable
    public Integer year;
    @SerializedName("dow")
    @Nullable
    public String dayOfWeek;

    @SerializedName("v1")
    @Nullable
    public Integer supplyVoltage = 0;
    @SerializedName("frq")
    @Nullable
    public Integer supplyFrequency = 0;

    @SerializedName("imp")
    @Nullable
    public Integer importedWattSeconds = 0;
    @SerializedName("exp")
    @Nullable
    public Integer exportedWattSeconds = 0;
    @SerializedName("gep")
    @Nullable
    public Integer generatedWattSeconds = 0;
    @SerializedName("gen")
    @Nullable
    public Integer generatedNegativeWattSeconds = 0;
    @SerializedName("h1d")
    @Nullable
    public Integer zappiDivertedWattSeconds = 0;
    @SerializedName("h1b")
    @Nullable
    public Integer zappiImportedWattSeconds = 0;

    @SerializedName("pect1")
    @Nullable
    public Integer positiveClampWattSeconds1 = 0;
    @SerializedName("nect1")
    @Nullable
    public Integer negativeClampWattSeconds1 = 0;
    @SerializedName("pect2")
    @Nullable
    public Integer positiveClampWattSeconds2 = 0;
    @SerializedName("nect2")
    @Nullable
    public Integer negativeClampWattSeconds2 = 0;
    @SerializedName("pect3")
    @Nullable
    public Integer positiveClampWattSeconds3 = 0;
    @SerializedName("nect3")
    @Nullable
    public Integer negativeClampWattSeconds3 = 0;

    public @Nullable Integer getImportedWattMinutes() {
        return toMinuteValue(importedWattSeconds);
    };

    public @Nullable Integer getExportedWattMinutes() {
        return toMinuteValue(exportedWattSeconds);
    };

    public @Nullable Integer getGeneratedWattMinutes() {
        return toMinuteValue(generatedWattSeconds);
    };

    public @Nullable Integer getGeneratedNegativeWattMinutes() {
        return toMinuteValue(generatedNegativeWattSeconds);
    };

    public @Nullable Integer getZappiDivertedWattMinutes() {
        return toMinuteValue(zappiDivertedWattSeconds);
    };

    public @Nullable Integer getZappiImportedWattMinutes() {
        return toMinuteValue(zappiImportedWattSeconds);
    };

    private @Nullable Integer toMinuteValue(@Nullable Integer secondValue) {
        if (secondValue == null) {
            return null;
        } else {
            return secondValue / SECONDS_PER_MINUTE;
        }
    }
}
