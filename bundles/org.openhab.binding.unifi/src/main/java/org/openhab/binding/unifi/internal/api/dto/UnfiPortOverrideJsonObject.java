/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.dto;

import com.google.gson.JsonObject;

/**
 * The {@link UnfiPortOverride} represents the data model of UniFi port override.
 * Using plain JsonObject to make sure any data in the object is not lost when writing the data back to the UniFi
 * device.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class UnfiPortOverrideJsonObject {

    private static final String PORT_IDX = "port_idx";
    private static final String PORT_CONF_ID = "port_conf_id";
    private static final String POE_MODE = "poe_mode";

    private final JsonObject jsonObject;

    public UnfiPortOverrideJsonObject(final JsonObject Object) {
        this.jsonObject = Object.getAsJsonObject();
    }

    public JsonObject getJsonObject() {
        return jsonObject;
    }

    public static boolean hasPortIdx(final JsonObject jsonObject) {
        return jsonObject.has(PORT_IDX);
    }

    public int getPortIdx() {
        return jsonObject.get(PORT_IDX).getAsInt();
    }

    public String getPortConfId() {
        return jsonObject.get(PORT_CONF_ID).getAsString();
    }

    public String getPoeMode() {
        return jsonObject.get(POE_MODE).getAsString();
    }

    public void setPoeMode(final String poeMode) {
        jsonObject.addProperty(POE_MODE, poeMode);
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
