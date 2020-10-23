/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;

/**
 * Json model of senec home devices: This sub model provides the current power statistics by the inverter.
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomeGrid implements Serializable {

    private static final long serialVersionUID = -7479338321370375451L;

    /**
     * grid value indicating the current power draw (for values larger zero) or supply (for negative values)
     */
    public @SerializedName("P_TOTAL") String currentGridValue;

    /**
     * grid voltage for each phase
     */
    public @SerializedName("PM1OBJ1U_AC0") String currentGridVoltagePh1;
    public @SerializedName("PM1OBJ1U_AC1") String currentGridVoltagePh2;
    public @SerializedName("PM1OBJ1U_AC2") String currentGridVoltagePh3;

    /**
     * grid current for each phase, draw (for values larger zero) or supply (for negative values)
     */
    public @SerializedName("PM1OBJ1I_AC0") String currentGridCurrentPh1;
    public @SerializedName("PM1OBJ1I_AC1") String currentGridCurrentPh2;
    public @SerializedName("PM1OBJ1I_AC2") String currentGridCurrentPh3;

    /**
     * grid power for each phase, draw (for values larger zero) or supply (for negative values)
     */
    public @SerializedName("PM1OBJ1P_AC0") String currentGridPowerPh1;
    public @SerializedName("PM1OBJ1P_AC1") String currentGridPowerPh2;
    public @SerializedName("PM1OBJ1P_AC2") String currentGridPowerPh3;

    @Override
    public String toString() {
        return "SenecHomeGrid [currentGridValue=" + currentGridValue + "currentGridVoltagePh1=" + currentGridVoltagePh1
                + "currentGridVoltagePh2=" + currentGridVoltagePh2 + "currentGridVoltagePh3=" + currentGridVoltagePh3
                + "currentGridCurrentPh1=" + currentGridCurrentPh1 + "currentGridCurrentPh2=" + currentGridCurrentPh2
                + "currentGridCurrentPh3=" + currentGridCurrentPh3 + "currentGridPowerPh1=" + currentGridPowerPh1
                + "currentGridPowerPh2=" + currentGridPowerPh2 + "currentGridPowerPh3=" + currentGridPowerPh3 + "]";
    }
}
