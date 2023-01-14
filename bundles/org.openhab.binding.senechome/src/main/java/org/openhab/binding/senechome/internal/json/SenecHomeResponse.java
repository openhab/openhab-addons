/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Json model of senec home devices rebuilt by analyzing the api.
 *
 * @author Steven Schwarznau - Initial Contribution
 */
public class SenecHomeResponse implements Serializable {

    private static final long serialVersionUID = -2672622188872750438L;

    public @SerializedName("PV1") SenecHomePower power = new SenecHomePower();
    public @SerializedName("ENERGY") SenecHomeEnergy energy = new SenecHomeEnergy();
    public @SerializedName("PM1OBJ1") SenecHomeGrid grid = new SenecHomeGrid();
    public @SerializedName("STATISTIC") SenecHomeStatistics statistics = new SenecHomeStatistics();
    public @SerializedName("BMS") SenecHomeBattery battery = new SenecHomeBattery();
    public @SerializedName("TEMPMEASURE") SenecHomeTemperature temperature = new SenecHomeTemperature();
    public @SerializedName("WALLBOX") SenecHomeWallbox wallbox = new SenecHomeWallbox();

    @Override
    public String toString() {
        return "SenecHomeResponse [power=" + power + ", energy=" + energy + ", grid=" + grid + ", statistics="
                + statistics + "battery" + battery + "temperature" + temperature + "wallbox" + wallbox + "]";
    }
}
