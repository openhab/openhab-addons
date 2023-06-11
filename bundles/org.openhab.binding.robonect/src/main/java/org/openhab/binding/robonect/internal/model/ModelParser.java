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
package org.openhab.binding.robonect.internal.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * This class is responsible for parsing JSNON formatted answers from the Robonect module using the Gson library.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class ModelParser {

    private final Gson gson;

    /**
     * Creates a parser with containing a preconfigured Gson object capable of parsing the JSON answers from the
     * Robonect module.
     */
    public ModelParser() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(MowerStatus.class, new MowerStatusDeserializer());
        gsonBuilder.registerTypeAdapter(MowerMode.class, new MowerModeDeserializer());
        gsonBuilder.registerTypeAdapter(Timer.TimerMode.class, new TimerModeDeserializer());
        this.gson = gsonBuilder.create();
    }

    /**
     * Parses a jsonString to a Java Object of the specified type.
     * 
     * @param jsonString - the json string to parse
     * @param type - the class of the type of the expected object to be returned.
     * @param <T> - the type of expected return value.
     * @return
     */
    public <T> T parse(String jsonString, Class<T> type) {
        return gson.fromJson(jsonString, type);
    }
}
