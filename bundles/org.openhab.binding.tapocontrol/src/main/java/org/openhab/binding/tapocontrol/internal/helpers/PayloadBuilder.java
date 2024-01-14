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
package org.openhab.binding.tapocontrol.internal.helpers;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * PAYLOAD BUILDER
 * Generates payload for TapoHttp request
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class PayloadBuilder {
    public String method = "";
    private JsonObject parameters = new JsonObject();

    /**
     * Set Command
     *
     * @param command command (method) to send
     */
    public void setCommand(String command) {
        this.method = command;
    }

    /**
     * Add Parameter
     *
     * @param name parameter name
     * @param value parameter value (typeOf Bool,Number or String)
     */
    public void addParameter(String name, Object value) {
        if (value instanceof Boolean bool) {
            this.parameters.addProperty(name, bool);
        } else if (value instanceof Number number) {
            this.parameters.addProperty(name, number);
        } else {
            this.parameters.addProperty(name, value.toString());
        }
    }

    /**
     * Get JSON Payload (STRING)
     *
     * @return String JSON-Payload
     */
    public String getPayload() {
        Gson gson = new Gson();
        JsonObject payload = getJsonPayload();
        return gson.toJson(payload);
    }

    /**
     * Get JSON Payload (JSON-Object)
     *
     * @return JsonObject JSON-Payload
     */
    public JsonObject getJsonPayload() {
        JsonObject payload = new JsonObject();
        long timeMils = System.currentTimeMillis();// * 1000;

        payload.addProperty("method", this.method);
        if (this.parameters.size() > 0) {
            payload.add("params", this.parameters);
        }
        payload.addProperty("requestTimeMils", timeMils);

        return payload;
    }

    /**
     * Flush Parameters
     * remove all parameters
     */
    public void flushParameters(String command) {
        parameters = new JsonObject();
    }
}
