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
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class Sites {
    public String siteid = "";
    public String nmi = "";

    private Sites() {
    }

    public static Sites parse(String response, String nem) {
        /* parse json string */
        JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
        Sites sites = new Sites();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            if (nem.equals(jsonObject.get("nmi").getAsString())) {
                sites.siteid = jsonObject.get("id").getAsString();
                sites.nmi = jsonObject.get("nmi").getAsString();
            }
        }
        if ((nem.isEmpty()) || (sites.siteid.isEmpty())) { // nem not specified, or not found so we take the first
                                                           // siteid
                                                           // found
            JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
            sites.siteid = jsonObject.get("id").getAsString();
            sites.nmi = jsonObject.get("nmi").getAsString();
        }
        return sites;
    }
}
