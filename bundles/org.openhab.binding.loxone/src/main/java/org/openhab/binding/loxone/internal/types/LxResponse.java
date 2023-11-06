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
package org.openhab.binding.loxone.internal.types;

import java.lang.reflect.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

/**
 * Response to a command sent to Miniserver's control.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxResponse {

    /**
     * A sub-response value structure that is received as a response to config API HTTP request sent to the Miniserver.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    public class LxResponseCfgApi {
        public String snr;
        public String version;
        public String key;
        public Integer httpsStatus;
    }

    /**
     * A sub-response structure that is part of a {@link LxResponse} class and contains a response to a command sent to
     * Miniserver's control.
     *
     * @author Pawel Pieczul - initial contribution
     *
     */
    private class LxSubResponse {
        @SerializedName("control")
        private String command;
        @SerializedName(value = "Code", alternate = { "code" })
        private Integer code;
        private JsonElement value;

        private boolean isSubResponseOk() {
            return (getResponseCode() == LxErrorCode.OK) && (command != null) && (value != null);
        }

        private LxErrorCode getResponseCode() {
            return LxErrorCode.getErrorCode(code);
        }
    }

    @SerializedName("LL")
    public LxSubResponse subResponse;
    private final Logger logger = LoggerFactory.getLogger(LxResponse.class);

    /**
     * Return true when response has correct syntax and return code was successful
     *
     * @return true when response is ok
     */
    public boolean isResponseOk() {
        return (subResponse != null && subResponse.isSubResponseOk());
    }

    /**
     * Gets command to which this response relates
     *
     * @return command name
     */
    public String getCommand() {
        return (subResponse != null ? subResponse.command : null);
    }

    /**
     * Gets error code from the response in numerical form or null if absent
     *
     * @return error code value
     */
    public Integer getResponseCodeNumber() {
        return (subResponse != null ? subResponse.code : null);
    }

    /**
     * Gets error code from the response as an enumerated value
     *
     * @return error code
     */
    public LxErrorCode getResponseCode() {
        return LxErrorCode.getErrorCode(getResponseCodeNumber());
    }

    /**
     * Gets response value as a string
     *
     * @return response value as string
     */
    public String getValueAsString() {
        return (subResponse != null && subResponse.value != null ? subResponse.value.getAsString() : null);
    }

    /**
     * Deserializes response value as a given type
     *
     * @param gson GSON object used for deserialization
     * @param <T> class to deserialize response to
     * @param type class type to deserialize response to
     * @return deserialized response
     */
    public <T> T getValueAs(Gson gson, Type type) {
        if (subResponse != null && subResponse.value != null) {
            try {
                return gson.fromJson(subResponse.value, type);
            } catch (NumberFormatException | JsonParseException e) {
                logger.debug("Error parsing response value as {}: {}", type, e.getMessage());
            }
        }
        return null;
    }
}
