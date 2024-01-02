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
package org.openhab.binding.ecobee.internal.dto.thermostat;

import static org.openhab.binding.ecobee.internal.EcobeeBindingConstants.ECOBEE_DATETIME_FORMAT;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link LocalDateTimeDeserializer} is responsible for handling the local dates returned from
 * the Ecobee API service.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {

    @Override
    public @Nullable LocalDateTime deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(ECOBEE_DATETIME_FORMAT);
            return formatter.parse(element.getAsString(), LocalDateTime::from);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
