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
package org.openhab.binding.amberelectric.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Container class for Current Pricing, related to amberelectric
 *
 * @author Paul Smedley <paul@smedley.id.au> - Initial contribution
 *
 */
@NonNullByDefault
public class CurrentPrices {
    public double elecPerKwh;
    public double clPerKwh;
    public double feedInPerKwh;
    public String elecStatus = "";
    public String clStatus = "";
    public String feedInStatus = "";
    public double renewables;
    public String spikeStatus = "";
    public String nemTime = "";

    private CurrentPrices() {
    }

    public static CurrentPrices parse(String response) {
        /* parse json string */
        JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        CurrentPrices currentprices = new CurrentPrices();
        currentprices.nemTime = jsonObject.get("nemTime").getAsString();
        currentprices.renewables = jsonObject.get("renewables").getAsDouble();
        currentprices.spikeStatus = jsonObject.get("spikeStatus").getAsString();
        for (int i = 0; i < jsonArray.size(); i++) {
            jsonObject = jsonArray.get(i).getAsJsonObject();
            if ("general".equals(jsonObject.get("channelType").getAsString())) {
                currentprices.elecPerKwh = jsonObject.get("perKwh").getAsDouble();
                currentprices.elecStatus = jsonObject.get("descriptor").getAsString();
            }
            if ("feedIn".equals(jsonObject.get("channelType").getAsString())) {
                // Multiple value from API by -1 to make the value match the app
                currentprices.feedInPerKwh = -1 * jsonObject.get("perKwh").getAsDouble();
                currentprices.feedInStatus = jsonObject.get("descriptor").getAsString();
            }
            if ("controlledLoad".equals(jsonObject.get("channelType").getAsString())) {
                currentprices.clPerKwh = jsonObject.get("perKwh").getAsDouble();
                currentprices.clStatus = jsonObject.get("descriptor").getAsString();
            }
        }
        return currentprices;
    }
}
