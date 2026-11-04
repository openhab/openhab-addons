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
 * This is a Gson desirializer capable of translating numeric modes into enum values.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class MowerModeDeserializer implements JsonDeserializer<MowerMode> {

    /**
     * Converts a numeric JSON mode to the corresponding {@link MowerMode}.
     *
     * @param jsonElement the JSON element containing the numeric mode
     * @param type the requested target type supplied by Gson
     * @param jsonDeserializationContext the deserialization context supplied by Gson
     * @return the mode resolved by {@link MowerMode#fromMode(int)}
     * @throws JsonParseException if Gson cannot deserialize the value
     */
    @Override
    public MowerMode deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int mode = jsonElement.getAsInt();
        return MowerMode.fromMode(mode);
    }
}
