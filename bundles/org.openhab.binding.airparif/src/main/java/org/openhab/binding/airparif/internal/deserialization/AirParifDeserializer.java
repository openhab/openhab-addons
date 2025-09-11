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

import java.time.Instant;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.AirParifException;
import org.openhab.binding.airparif.internal.api.AirParifDto.PollutantConcentration;
import org.openhab.binding.airparif.internal.api.PollenAlertLevel;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AirParifDeserializer} is responsible to instantiate suitable Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = AirParifDeserializer.class)
public class AirParifDeserializer {
    private final Gson gson;

    @Activate
    public AirParifDeserializer() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
                .registerTypeAdapter(PollenAlertLevel.class, new PollenAlertLevelDeserializer())
                .registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory())
                .registerTypeAdapter(PollutantConcentration.class, new PollutantConcentrationDeserializer())
                .registerTypeAdapter(LocalDate.class,
                        (JsonDeserializer<LocalDate>) (json, type, context) -> LocalDate
                                .parse(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, context) -> {
                    String string = json.getAsJsonPrimitive().getAsString();
                    string += string.contains("+") ? "" : "Z";
                    return Instant.parse(string);
                }).create();
    }

    public <T> T deserialize(Class<T> clazz, String json) throws AirParifException {
        try {
            @Nullable
            T result = gson.fromJson(json, clazz);
            if (result != null) {
                return result;
            }
            throw new AirParifException("Deserialization of '%s' resulted in null value", json);
        } catch (JsonSyntaxException e) {
            throw new AirParifException(e, "Unexpected error deserializing '%s'", e.getMessage());
        }
    }
}
