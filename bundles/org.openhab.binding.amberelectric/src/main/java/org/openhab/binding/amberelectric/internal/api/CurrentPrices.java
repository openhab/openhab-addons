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
package org.openhab.binding.amberelectric.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Container class for Current Pricing, related to amberelectric
 *
 * @author Paul Smedley <paul@smedley.id.au> - Initial contribution
 *
 */
public class CurrentPrices {
    private static Logger LOGGER = LoggerFactory.getLogger(CurrentPrices.class);

    public Double perKwh;
    public Double renewables;
    public Double spotPerKwh;
    public String spikeStatus;
    public String nemTime;

    private CurrentPrices() {
    }

    public static CurrentPrices parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        CurrentPrices currentprices = new CurrentPrices();
        currentprices.nemTime = jsonObject.get("nemTime").getAsString();
        currentprices.perKwh = jsonObject.get("perKwh").getAsDouble();
        currentprices.renewables = jsonObject.get("renewables").getAsDouble();
        currentprices.spotPerKwh = jsonObject.get("spotPerKwh").getAsDouble();
        currentprices.spikeStatus = jsonObject.get("spikeStatus").getAsString();

        return currentprices;
    }
}
