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
package org.openhab.binding.teslapowerwall.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the battery soe.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class MeterAggregates {
    public double gridInstpower;
    public double batteryInstpower;
    public double homeInstpower;
    public double solarInstpower;
    public double gridEnergyexported;
    public double batteryEnergyexported;
    public double homeEnergyexported;
    public double solarEnergyexported;
    public double gridEnergyimported;
    public double batteryEnergyimported;
    public double homeEnergyimported;
    public double solarEnergyimported;

    private MeterAggregates() {
    }

    public static MeterAggregates parse(String response) {
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        MeterAggregates info = new MeterAggregates();

        JsonObject sitejson = jsonObject.get("site").getAsJsonObject();
        info.gridInstpower = sitejson.get("instant_power").getAsDouble() / 1000;
        info.gridEnergyexported = sitejson.get("energy_exported").getAsDouble() / 1000;
        info.gridEnergyimported = sitejson.get("energy_imported").getAsDouble() / 1000;

        JsonObject batteryjson = jsonObject.get("battery").getAsJsonObject();
        info.batteryInstpower = batteryjson.get("instant_power").getAsDouble() / 1000;
        info.batteryEnergyexported = batteryjson.get("energy_exported").getAsDouble() / 1000;
        info.batteryEnergyimported = batteryjson.get("energy_imported").getAsDouble() / 1000;

        JsonObject loadjson = jsonObject.get("load").getAsJsonObject();
        info.homeInstpower = loadjson.get("instant_power").getAsDouble() / 1000;
        info.homeEnergyexported = loadjson.get("energy_exported").getAsDouble() / 1000;
        info.homeEnergyimported = loadjson.get("energy_imported").getAsDouble() / 1000;

        JsonObject solarjson = jsonObject.get("solar").getAsJsonObject();
        info.solarInstpower = solarjson.get("instant_power").getAsDouble() / 1000;
        info.solarEnergyexported = solarjson.get("energy_exported").getAsDouble() / 1000;
        info.solarEnergyimported = solarjson.get("energy_imported").getAsDouble() / 1000;

        return info;
    }
}
