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
package org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * @author Danny Baumann - Initial contribution
 */
public class PortalIotCommandJsonResponse extends AbstractPortalIotCommandResponse {
    @SerializedName("resp")
    public final JsonElement response;

    public PortalIotCommandJsonResponse(String result, JsonElement response, int errorCode, Object errorObject) {
        super(result, errorCode, errorObject);
        this.response = response;
    }

    public <T> T getResponsePayloadAs(Gson gson, Class<T> clazz) throws DataParsingException {
        try {
            JsonElement payloadRaw = getResponsePayload(gson);
            @Nullable
            T payload = gson.fromJson(payloadRaw, clazz);
            if (payload == null) {
                throw new DataParsingException("Empty JSON payload");
            }
            return payload;
        } catch (JsonSyntaxException e) {
            throw new DataParsingException(e);
        }
    }

    public JsonElement getResponsePayload(Gson gson) throws DataParsingException {
        try {
            @Nullable
            JsonResponsePayloadWrapper wrapper = gson.fromJson(response, JsonResponsePayloadWrapper.class);
            if (wrapper == null) {
                throw new DataParsingException("Empty JSON payload");
            }
            return wrapper.body.payload;
        } catch (JsonSyntaxException e) {
            throw new DataParsingException(e);
        }
    }

    public static class JsonPayloadHeader {
        @SerializedName("pri")
        public int pri;
        @SerializedName("ts")
        public long timestamp;
        @SerializedName("tzm")
        public int tzm;
        @SerializedName("fwVer")
        public String firmwareVersion;
        @SerializedName("hwVer")
        public String hardwareVersion;
    }

    public static class JsonResponsePayloadWrapper {
        @SerializedName("header")
        public JsonPayloadHeader header;
        @SerializedName("body")
        public JsonResponsePayloadBody body;
    }

    public static class JsonResponsePayloadBody {
        @SerializedName("code")
        public int code;
        @SerializedName("msg")
        public String message;
        @SerializedName("data")
        public JsonElement payload;
    }
}
