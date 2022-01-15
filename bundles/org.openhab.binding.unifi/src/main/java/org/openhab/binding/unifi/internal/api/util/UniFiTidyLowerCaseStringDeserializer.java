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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 *
 * The {@link UniFiTidyLowerCaseStringDeserializer} is an implementation of {@link JsonDeserializer} that deserializes
 * strings in a tidy lower case format.
 *
 * @author Matthew Bowman - Initial contribution
 */
public class UniFiTidyLowerCaseStringDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        String s = json.getAsJsonPrimitive().getAsString();
        return s.trim().toLowerCase();
    }
}
