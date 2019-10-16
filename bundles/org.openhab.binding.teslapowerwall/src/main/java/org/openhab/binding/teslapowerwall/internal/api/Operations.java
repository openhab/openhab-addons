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
 * Class for holding the set of parameters used to read the battery mode/reserver.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class Operations {
    private static Logger LOGGER = LoggerFactory.getLogger(Operations.class);

    public String mode;
    public double reserve;


    private Operations() {
    }

    public static Operations parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonObject jsonObject = new JsonParser().parse(response).getAsJsonObject();
        Operations info = new Operations();
        info.mode = jsonObject.get("real_mode").getAsString();
        info.reserve = jsonObject.get("backup_reserve_percent").getAsDouble();
        return info;
    }

}
