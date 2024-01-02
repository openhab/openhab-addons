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
 * Senec wallbox specific data from "WALLBOX" section.
 *
 * @author Erwin Guib - Initial Contribution
 */
public class SenecHomeWallbox implements Serializable {

    private static final long serialVersionUID = -664163242812451235L;

    /**
     * Encoded wallbox state.
     */
    public @SerializedName("STATE") String[] state;

    /**
     * L1 Charging current per wallbox (A).
     */
    public @SerializedName("L1_CHARGING_CURRENT") String[] l1ChargingCurrent;

    /**
     * L2 Charging current per wallbox (A).
     */
    public @SerializedName("L2_CHARGING_CURRENT") String[] l2ChargingCurrent;

    /**
     * L3 Charging current per wallbox (A).
     */
    public @SerializedName("L3_CHARGING_CURRENT") String[] l3ChargingCurrent;

    /**
     * Charging power per wallbox (W).
     */
    public @SerializedName("APPARENT_CHARGING_POWER") String[] chargingPower;

    @Override
    public String toString() {
        return "SenecWallbox{" + "l1ChargingCurrent=" + Arrays.toString(l1ChargingCurrent) + ", l2ChargingCurrent="
                + Arrays.toString(l2ChargingCurrent) + ", l3ChargingCurrent=" + Arrays.toString(l3ChargingCurrent)
                + '}';
    }
}
