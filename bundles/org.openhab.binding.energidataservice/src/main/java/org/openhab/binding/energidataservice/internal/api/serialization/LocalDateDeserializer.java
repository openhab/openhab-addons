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
package org.openhab.binding.energidataservice.internal.api.serialization;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link LocalDateDeserializer} converts a formatted string to {@link LocalDate}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

    @Override
    public @Nullable LocalDate deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        try {
            return LocalDate.parse(element.getAsString().substring(0, 10));
        } catch (DateTimeParseException e) {
            throw new JsonParseException("Could not parse as LocalDate: " + element.getAsString(), e);
        }
    }
}
