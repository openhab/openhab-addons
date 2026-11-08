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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Parses JSON responses from the Robonect module using Gson.
 * 
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - Shared JSON parser for endpoint responses
 */
public final class ModelParser {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MowerStatus.class, new MowerStatusDeserializer())
            .registerTypeAdapter(MowerMode.class, new MowerModeDeserializer())
            .registerTypeAdapter(Timer.TimerMode.class, new TimerModeDeserializer()).create();

    /**
     * Prevents creation of parser instances because parsing uses the shared, immutable Gson configuration.
     */
    private ModelParser() {
    }

    /**
     * Parses a JSON response into the specified type.
     * 
     * @param jsonString the JSON response
     * @param type the class of the expected result
     * @param <T> the result type
     * @return the parsed object, or {@code null} when the JSON represents {@code null}
     */
    public static <T> T parse(String jsonString, Class<T> type) {
        return GSON.fromJson(jsonString, type);
    }
}
