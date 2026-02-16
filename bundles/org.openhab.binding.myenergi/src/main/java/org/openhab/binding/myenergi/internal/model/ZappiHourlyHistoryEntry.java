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
 * The {@link ZappiHourlyHistoryEntry} is a DTO class used to hold an hourly
 * slot of historic data. It's used to deserialize JSON API results.
 *
 * @author Rene Scherer - Initial contribution
 *
 */
@NonNullByDefault
public class ZappiHourlyHistoryEntry {

    private static final Integer SECONDS_PER_HOUR = 3600;

    // {"hr":13,"dow":"Sat","dom":28,"mon":11,"yr":2020,"imp":1760580,"exp":120,"gep":1203060}

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

    @Nullable
    public Integer getImportedWattHours() {
        return toHourValue(importedWattSeconds);
    };

    @Nullable
    public Integer getExportedWattHours() {
        return toHourValue(exportedWattSeconds);
    };

    @Nullable
    public Integer getGeneratedWattHours() {
        return toHourValue(generatedWattSeconds);
    };

    @Nullable
    public Integer getGeneratedNegativeWattHours() {
        return toHourValue(generatedNegativeWattSeconds);
    };

    @Nullable
    public Integer getZappiDivertedWattHours() {
        return toHourValue(zappiDivertedWattSeconds);
    };

    @Nullable
    public Integer getZappiImportedWattHours() {
        return toHourValue(zappiImportedWattSeconds);
    };

    @Override
    public String toString() {
        return "ZappiHourlyHistoryEntry [hour=" + hour + ", day=" + day + ", month=" + month + ", year=" + year
                + ", dayOfWeek=" + dayOfWeek + ", importedWattSeconds=" + importedWattSeconds + ", exportedWattSeconds="
                + exportedWattSeconds + ", generatedWattSeconds=" + generatedWattSeconds
                + ", generatedNegativeWattSeconds=" + generatedNegativeWattSeconds + ", zappiDivertedWattSeconds="
                + zappiDivertedWattSeconds + ", zappiImportedWattSeconds=" + zappiImportedWattSeconds + "]";
    }

    private @Nullable Integer toHourValue(@Nullable Integer secondValue) {
        if (secondValue == null) {
            return null;
        } else {
            return secondValue / SECONDS_PER_HOUR;
        }
    }
}
