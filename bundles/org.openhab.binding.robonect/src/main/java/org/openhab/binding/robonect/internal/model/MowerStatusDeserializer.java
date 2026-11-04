/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * This is a Gson deserializer to deserialize numeric mower status codes into enum values.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class MowerStatusDeserializer implements JsonDeserializer<MowerStatus> {
    /**
     * Converts a numeric JSON status code to the corresponding {@link MowerStatus}.
     *
     * @param jsonElement the JSON element containing the numeric status code
     * @param type the requested target type supplied by Gson
     * @param jsonDeserializationContext the deserialization context supplied by Gson
     * @return the status resolved by {@link MowerStatus#fromCode(int)}
     * @throws JsonParseException if Gson cannot deserialize the value
     */
    @Override
    public MowerStatus deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int code = jsonElement.getAsInt();
        return MowerStatus.fromCode(code);
    }
}
