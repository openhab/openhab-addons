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
package org.openhab.binding.digitalstrom.internal.lib.serverconnection.impl;

import org.openhab.binding.digitalstrom.internal.lib.serverconnection.constants.JSONApiResponseKeysEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * The {@link JSONResponseHandler} checks a digitalSTROM-JSON response and can parse it to a {@link JsonObject}.
 *
 * @author Alexander Betker - Initial contribution
 * @author Alex Maier - Initial contribution
 * @author Michael Ochel - add Java-Doc, make methods static and change from SimpleJSON to GSON
 * @author Matthias Siegele - add Java-Doc, make methods static and change from SimpleJSON to GSON
 */
public class JSONResponseHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONResponseHandler.class);

    /**
     * Checks the digitalSTROM-JSON response and return true if it was successful, otherwise false.
     *
     * @param jsonResponse to check
     * @return true, if successful
     */
    public static boolean checkResponse(JsonObject jsonResponse) {
        if (jsonResponse == null) {
            return false;
        } else if (jsonResponse.get(JSONApiResponseKeysEnum.OK.getKey()) != null) {
            return jsonResponse.get(JSONApiResponseKeysEnum.OK.getKey()).getAsBoolean();
        } else {
            String message = "unknown message";
            if (jsonResponse.get(JSONApiResponseKeysEnum.MESSAGE.getKey()) != null) {
                message = jsonResponse.get(JSONApiResponseKeysEnum.MESSAGE.getKey()).getAsString();
            }
            LOGGER.error("JSONResponseHandler: error in json request. Error message : {}", message);
        }
        return false;
    }

    /**
     * Returns the {@link JsonObject} from the given digitalSTROM-JSON response {@link String} or null if the json
     * response was empty.
     *
     * @param jsonResponse to convert
     * @return jsonObject
     */
    public static JsonObject toJsonObject(String jsonResponse) {
        if (jsonResponse != null && !jsonResponse.trim().equals("")) {
            try {
                return (JsonObject) JsonParser.parseString(jsonResponse);
            } catch (JsonParseException e) {
                LOGGER.error("A JsonParseException occurred by parsing jsonRequest: {}", jsonResponse, e);
            }
        }
        return null;
    }

    /**
     * Returns the result {@link JsonObject} from the given digitalSTROM-JSON response {@link JsonObject}.
     *
     * @param jsonObject of response
     * @return json result object
     */
    public static JsonObject getResultJsonObject(JsonObject jsonObject) {
        if (jsonObject != null) {
            return jsonObject.get(JSONApiResponseKeysEnum.RESULT.getKey()).getAsJsonObject();
        }
        return null;
    }
}
