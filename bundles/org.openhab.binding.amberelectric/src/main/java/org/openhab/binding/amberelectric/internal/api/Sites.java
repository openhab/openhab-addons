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
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class Sites {
    private static Logger LOGGER = LoggerFactory.getLogger(Sites.class);

    public String siteid;
    public String nmi;

    private Sites() {
    }

    public static Sites parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonArray jsonArray = JsonParser.parseString(response).getAsJsonArray();
        JsonObject jsonObject = jsonArray.get(0).getAsJsonObject();
        Sites sites = new Sites();
        sites.siteid = jsonObject.get("id").getAsString();
        sites.nmi = jsonObject.get("nmi").getAsString();
        return sites;
    }
}
