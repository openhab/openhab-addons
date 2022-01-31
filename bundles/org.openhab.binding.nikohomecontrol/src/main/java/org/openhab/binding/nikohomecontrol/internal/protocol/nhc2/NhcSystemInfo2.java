/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * {@link NhcSystemInfo2} represents Niko Home Control II system info. It is used when parsing the systeminfo response
 * json.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcSystemInfo2 {
    static class NhcSwVersion {
        String nhcVersion = "";
        String cocoImage = "";
    }

    String lastConfig = "";
    String waterTariff = "";
    String electricityTariff = "";
    String gasTariff = "";
    String currency = "";
    String units = "";
    String language = "";
    @SerializedName(value = "SWversions")
    ArrayList<NhcSwVersion> swVersions = new ArrayList<>();

    /**
     * @return the NhcVersion
     */
    public String getNhcVersion() {
        return swVersions.stream().map(p -> p.nhcVersion).filter(v -> !v.isEmpty()).findFirst().orElse("");
    }

    /**
     * @return the CocoImage version
     */
    public String getCocoImage() {
        return swVersions.stream().map(p -> p.cocoImage).filter(v -> !v.isEmpty()).findFirst().orElse("");
    }

    /**
     * @return the lastConfig
     */
    public String getLastConfig() {
        return lastConfig;
    }

    /**
     * @return the waterTariff
     */
    public String getWaterTariff() {
        return waterTariff;
    }

    /**
     * @return the electricityTariff
     */
    public String getElectricityTariff() {
        return electricityTariff;
    }

    /**
     * @return the gasTariff
     */
    public String getGasTariff() {
        return gasTariff;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
}
