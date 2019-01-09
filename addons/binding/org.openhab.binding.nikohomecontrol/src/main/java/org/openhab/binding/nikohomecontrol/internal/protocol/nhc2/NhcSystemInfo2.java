/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
    class NhcSwVersion {
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
     * @return the lastConfig
     */
    public String getNhcVersion() {
        if (swVersions.size() > 0) {
            return swVersions.get(0).nhcVersion;
        } else {
            return "";
        }
    }

    /**
     * @return the lastConfig
     */
    public String getCocoImage() {
        if (swVersions.size() > 0) {
            return swVersions.get(0).cocoImage;
        } else {
            return "";
        }
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
