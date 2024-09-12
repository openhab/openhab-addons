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
package org.openhab.binding.boschindego.internal.dto.serialization;

import java.lang.reflect.Type;
import java.time.Instant;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link InstantDeserializer} converts a formatted UTC string to {@link Instant}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class InstantDeserializer implements JsonDeserializer<Instant> {

    @Override
    public @Nullable Instant deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        try {
            return Instant.parse(element.getAsString());
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Could not parse as Instant: " + element.getAsString(), e);
        }
    }
}
