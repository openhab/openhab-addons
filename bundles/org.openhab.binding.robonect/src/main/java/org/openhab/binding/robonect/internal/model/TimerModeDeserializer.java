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
 * Gson deserializer for deserializing timer mode values to the corresponding enum.
 *
 * @author Marco Meyer - Initial contribution
 */
public class TimerModeDeserializer implements JsonDeserializer<Timer.TimerMode> {

    /**
     * Converts a numeric JSON timer mode to a {@link Timer.TimerMode}.
     *
     * @param jsonElement the JSON value containing the timer mode code
     * @param type the requested result type, unused by this deserializer
     * @param jsonDeserializationContext the Gson context, unused by this deserializer
     * @return the matching timer mode, or {@link Timer.TimerMode#INACTIVE} for an unknown code
     * @throws JsonParseException if the JSON value cannot be deserialized
     */
    @Override
    public Timer.TimerMode deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        int code = jsonElement.getAsInt();
        return Timer.TimerMode.fromCode(code);
    }
}
