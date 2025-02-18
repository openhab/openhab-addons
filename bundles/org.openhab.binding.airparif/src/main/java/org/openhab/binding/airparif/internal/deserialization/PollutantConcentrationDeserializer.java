/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollutantConcentration;
import org.openhab.binding.airparif.internal.api.Pollutant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Specialized deserializer for ColorMap class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class PollutantConcentrationDeserializer implements JsonDeserializer<PollutantConcentration> {
    private final Logger logger = LoggerFactory.getLogger(PollutantConcentrationDeserializer.class);

    @Override
    public @Nullable PollutantConcentration deserialize(JsonElement json, Type clazz,
            JsonDeserializationContext context) {
        PollutantConcentration result = null;
        JsonArray array = json.getAsJsonArray();

        if (array.size() == 3) {
            Pollutant pollutant = Pollutant.safeValueOf(array.get(0).getAsString());
            try {
                result = new PollutantConcentration(pollutant, array.get(1).getAsInt(), array.get(2).getAsInt());
            } catch (JsonSyntaxException ignore) {
                logger.debug("Error deserializing PollutantConcentration: {}", json.toString());
            }
        }
        return result;
    }
}
