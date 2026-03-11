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
package org.openhab.binding.atmofrance.internal.deserialization;

import java.time.Instant;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atmofrance.internal.AtmoFranceException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AtmoFranceDeserializer} is responsible to instantiate suitable Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = AtmoFranceDeserializer.class)
public class AtmoFranceDeserializer {
    private final Gson gson;

    @Activate
    public AtmoFranceDeserializer() {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(Instant.class, (JsonDeserializer<Instant>) (json, type, context) -> {
                    String string = json.getAsJsonPrimitive().getAsString();
                    string += string.contains("+") ? "" : "T00:00:00+01:00";
                    return Instant.parse(string);
                }).registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, type, context) -> LocalDate
                        .parse(json.getAsJsonPrimitive().getAsString()))
                .create();
    }

    public <T> T deserialize(Class<T> clazz, String json) throws AtmoFranceException {
        try {
            T result = gson.fromJson(json, clazz);
            if (result == null) {
                throw new AtmoFranceException("Deserialization of '%s' resulted in null value", json);
            }

            return result;
        } catch (JsonSyntaxException e) {
            throw new AtmoFranceException(e, "Unexpected error deserializing '%s'", e.getMessage());
        }
    }

    public String serialize(Object object) {
        return gson.toJson(object);
    }
}
