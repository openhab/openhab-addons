/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for holding the set of parameters used to read the battery soe.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class MeterAggregates {
    private static Logger LOGGER = LoggerFactory.getLogger(MeterAggregates.class);

    public double grid_instpower;
    public double battery_instpower;
    public double home_instpower;
    public double solar_instpower;
    public double grid_energyexported;
    public double battery_energyexported;
    public double home_energyexported;
    public double solar_energyexported;
    public double grid_energyimported;
    public double battery_energyimported;
    public double home_energyimported;
    public double solar_energyimported;

    private MeterAggregates() {
    }

    public static MeterAggregates parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        MeterAggregates info = new MeterAggregates();

        JsonObject sitejson = jsonObject.get("site").getAsJsonObject();
        info.grid_instpower = sitejson.get("instant_power").getAsDouble()/1000;
        info.grid_energyexported = sitejson.get("energy_exported").getAsDouble()/1000;
        info.grid_energyimported = sitejson.get("energy_imported").getAsDouble()/1000;

        JsonObject batteryjson = jsonObject.get("battery").getAsJsonObject();
        info.battery_instpower = batteryjson.get("instant_power").getAsDouble()/1000;
        info.battery_energyexported = batteryjson.get("energy_exported").getAsDouble()/1000;
        info.battery_energyimported = batteryjson.get("energy_imported").getAsDouble()/1000;

        JsonObject loadjson = jsonObject.get("load").getAsJsonObject();
        info.home_instpower = loadjson.get("instant_power").getAsDouble()/1000;
        info.home_energyexported = loadjson.get("energy_exported").getAsDouble()/1000;
        info.home_energyimported = loadjson.get("energy_imported").getAsDouble()/1000;

        JsonObject solarjson = jsonObject.get("solar").getAsJsonObject();
        info.solar_instpower = solarjson.get("instant_power").getAsDouble()/1000;
        info.solar_energyexported = solarjson.get("energy_exported").getAsDouble()/1000;
        info.solar_energyimported = solarjson.get("energy_imported").getAsDouble()/1000;

        return info;
    }

}
