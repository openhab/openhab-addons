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
package org.openhab.binding.senechome.internal.json;

import java.io.Serializable;
import java.util.Arrays;

import com.google.gson.annotations.SerializedName;

/**
 * Battery related data from section "BMS".
 *
 * @author Erwin Guib - Initial Contribution
 */
public class SenecHomeBattery implements Serializable {

    public static final long serialVersionUID = -2850415059107677832L;

    /**
     * Total charged energy per battery pack (mWh).
     */
    public @SerializedName("CHARGED_ENERGY") String[] chargedEnergy;

    /**
     * Total discharged energy per battery pack (mWh).
     */
    public @SerializedName("DISCHARGED_ENERGY") String[] dischargedEnergy;

    /**
     * Number of load cycles per battery pack.
     */
    public @SerializedName("CYCLES") String[] cycles;

    /**
     * Current per battery pack (A).
     */
    public @SerializedName("CURRENT") String[] current;

    /**
     * Voltage per battery pack (V).
     */
    public @SerializedName("VOLTAGE") String[] voltage;

    /**
     * Maximum cell voltage per battery pack (mV).
     */
    public @SerializedName("MAX_CELL_VOLTAGE") String[] maxCellVoltage;

    /**
     * Minimum cell voltage per battery pack (mV).
     */
    public @SerializedName("MIN_CELL_VOLTAGE") String[] minCellVoltage;

    @Override
    public String toString() {
        return "SenecHomeBattery{" + "chargedEnergy=" + Arrays.toString(chargedEnergy) + ", dischargedEnergy="
                + Arrays.toString(dischargedEnergy) + ", cycles=" + Arrays.toString(cycles) + ", current="
                + Arrays.toString(current) + ", voltage=" + Arrays.toString(voltage) + ", maxCellVoltage="
                + Arrays.toString(maxCellVoltage) + ", minCellVoltage=" + Arrays.toString(minCellVoltage) + '}';
    }
}
