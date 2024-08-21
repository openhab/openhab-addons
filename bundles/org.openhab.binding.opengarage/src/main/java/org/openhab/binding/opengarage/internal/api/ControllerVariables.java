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
package org.openhab.binding.opengarage.internal.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
public class ControllerVariables {
    private static Logger LOGGER = LoggerFactory.getLogger(ControllerVariables.class);

    public int dist;
    public int door;
    public int vehicle;
    public int rcnt;
    public int fwv;
    public String name;
    public String mac;
    public String cid;
    public int rssi;

    private ControllerVariables() {
    }

    public static ControllerVariables parse(String response) {
        LOGGER.debug("Parsing string: \"{}\"", response);
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        ControllerVariables info = new ControllerVariables();
        info.dist = jsonObject.get("dist").getAsInt();
        info.door = jsonObject.get("door").getAsInt();
        info.vehicle = jsonObject.get("vehicle").getAsInt();
        info.rcnt = jsonObject.get("rcnt").getAsInt();
        info.fwv = jsonObject.get("fwv").getAsInt();
        info.name = jsonObject.get("name").getAsString();
        info.mac = jsonObject.get("mac").getAsString();
        info.cid = jsonObject.get("cid").getAsString();
        info.rssi = jsonObject.get("rssi").getAsInt();
        return info;
    }
}
