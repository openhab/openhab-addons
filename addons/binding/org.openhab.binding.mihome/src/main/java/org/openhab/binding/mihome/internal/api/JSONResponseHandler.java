/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.internal.api;

import org.openhab.binding.mihome.internal.api.constants.JSONResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

/**
 * Contains some common methods which are used for processing the server's response
 *
 * @author Mihaela Memova - Initial contribution
 *
 */
public class JSONResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(JSONResponseHandler.class);

    /**
     * Converts a given response String to a JSON object
     */
    public static JsonObject responseStringtoJsonObject(String jsonResponse) throws JsonParseException {
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            try {
                JsonParser parser = new JsonParser();
                JsonObject resultObj = (JsonObject) parser.parse(jsonResponse);
                return resultObj;
            } catch (JsonParseException e) {
                logger.error("An JsonParseException occurred by parsing JSON response: " + jsonResponse, e);
                return null;
            }
        }
        return null;
    }

    /**
     * Returns the status of the server's response
     */
    public static String getResponseStatus(JsonObject jsonResponse) {
        String responseStatus = null;
        if (jsonResponse != null) {
            JsonElement responseStatusEl = jsonResponse.get(JSONResponseConstants.RESPONSE_STATUS_KEY);
            if (responseStatusEl != null) {
                responseStatus = responseStatusEl.getAsString();
            }
        }
        return responseStatus;
    }

    /**
     * Determines if the request was successful based on the server's response
     */
    public static boolean isRequestSuccessful(JsonObject jsonResponse) {
        if (jsonResponse != null && !jsonResponse.isJsonNull()) {
            String responseStatus = getResponseStatus(jsonResponse);
            return JSONResponseConstants.RESPONSE_SUCCESS.equals(responseStatus);
        }
        return false;
    }

    /**
     * Returns the error message of the data when the request was not successful.
     * Depending on the reason, it is saved either in the "message" property or the "errors"
     * property
     */
    public static String getErrorMessageFromResponse(JsonObject responseData) {
        JsonElement message = responseData.get(JSONResponseConstants.RESPONSE_MESSAGE_KEY);
        JsonElement error = responseData.get(JSONResponseConstants.RESPONSE_ERROR_KEY);
        if (message != null) {
            return message.toString();
        }
        if (error != null) {
            return error.toString();
        }
        return null;
    }
}
