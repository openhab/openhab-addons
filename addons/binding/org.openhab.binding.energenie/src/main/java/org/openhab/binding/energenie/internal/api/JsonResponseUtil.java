/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.energenie.internal.api;

import java.util.Map.Entry;
import java.util.Set;

import org.openhab.binding.energenie.internal.api.constants.JsonResponseConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
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
public class JsonResponseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonResponseUtil.class);

    /**
     * Converts a given response String to a JSON object
     */
    public static JsonObject responseStringtoJsonObject(String jsonResponse) {
        if (jsonResponse != null && !jsonResponse.isEmpty()) {
            try {
                JsonParser parser = new JsonParser();
                return (JsonObject) parser.parse(jsonResponse);
            } catch (JsonParseException e) {
                LOGGER.error("An error occurred while trying to parse the JSON response {}: ", jsonResponse, e);
                return null;
            }
        }
        return null;
    }

    public static <T> T getObject(JsonObject jsonResponse, Class<T> type) {
        if (jsonResponse != null && jsonResponse.get(JsonResponseConstants.DATA_KEY) != null) {
            JsonElement data = jsonResponse.get(JsonResponseConstants.DATA_KEY);
            Gson gson = new Gson();
            return gson.fromJson(data, type);
        }
        return null;
    }

    /**
     * Returns the status of the server's response
     */
    public static String getResponseStatus(JsonObject jsonResponse) {
        if (jsonResponse != null) {
            JsonElement responseStatusElement = jsonResponse.get(JsonResponseConstants.RESPONSE_STATUS_KEY);
            if (responseStatusElement != null) {
                return responseStatusElement.getAsString();
            }
        }
        return null;
    }

    /**
     * Determines if the request was successful based on the server's response
     */
    public static boolean isRequestSuccessful(JsonObject jsonResponse) {
        if (jsonResponse != null && !jsonResponse.isJsonNull()) {
            String responseStatus = getResponseStatus(jsonResponse);
            return JsonResponseConstants.RESPONSE_SUCCESS.equals(responseStatus);
        }
        return false;
    }

    /**
     * Returns the error message of the data when the request was not
     * successful. Depending on the reason, it is saved either in the "message"
     * property or the "errors" property
     */
    public static String getErrorMessageFromResponse(JsonObject responseData) {
        JsonElement message = responseData.get(JsonResponseConstants.RESPONSE_MESSAGE_KEY);
        JsonElement error = responseData.get(JsonResponseConstants.RESPONSE_ERROR_KEY);
        if (message != null) {
            return message.getAsString();
        }
        if (error != null) {
            return getAllErrors(error);
        }
        return null;
    }

    private static String getAllErrors(JsonElement error) {
        StringBuilder allErrors = new StringBuilder();
        Set<Entry<String, JsonElement>> errorsEntrySet = error.getAsJsonObject().entrySet();
        for (Entry<String, JsonElement> entry : errorsEntrySet) {
            String errorKey = entry.getKey();
            JsonArray errorMessagesArray = entry.getValue().getAsJsonArray();
            for (JsonElement message : errorMessagesArray) {
                allErrors.append(errorKey);
                allErrors.append(message.getAsString());
                allErrors.append("\n");
            }
        }
        return allErrors.toString();
    }
}
