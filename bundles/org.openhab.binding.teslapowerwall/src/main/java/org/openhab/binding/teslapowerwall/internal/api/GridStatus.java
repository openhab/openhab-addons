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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the battery soe.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class GridStatus {
    private static Logger LOGGER = LoggerFactory.getLogger(GridStatus.class);

    public String grid_status = "";
    public Boolean grid_services = false;

    private GridStatus() {
    }

    public static GridStatus parse(String response) {
        LOGGER.trace("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        GridStatus info = new GridStatus();
        info.grid_status = jsonObject.get("grid_status").getAsString();
        info.grid_services = jsonObject.get("grid_services_active").getAsString().equalsIgnoreCase("true");
        return info;
    }
}
