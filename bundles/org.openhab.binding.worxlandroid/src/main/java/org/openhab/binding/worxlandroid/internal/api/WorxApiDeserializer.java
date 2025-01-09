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
package org.openhab.binding.worxlandroid.internal.api;

import java.lang.reflect.Type;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link WorxApiDeserializer} is responsible to instantiate suitable Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = WorxApiDeserializer.class)
public class WorxApiDeserializer {
    private static final DateTimeFormatter WORX_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ssX");

    private final Gson gson;

    @Activate
    public WorxApiDeserializer(@Reference TimeZoneProvider timeZoneProvider) {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ZoneId.class,
                        (JsonDeserializer<ZoneId>) (json, type, context) -> ZoneId
                                .of(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, context) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString() + "Z", WORX_FORMATTER)
                                .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .registerTypeAdapter(Boolean.class, (JsonDeserializer<Boolean>) (json, type, context) -> {
                    String value = json.getAsJsonPrimitive().getAsString().toUpperCase();
                    return "1".equals(value);
                }).create();
    }

    public String toJson(Object object) {
        return gson.toJson(object);
    }

    public Map<String, String> toMap(Object object) {
        Map<String, String> fromObject = gson.fromJson(toJson(object), new TypeToken<HashMap<String, String>>() {
        }.getType());
        return fromObject != null ? Map.copyOf(fromObject) : Map.of();
    }

    public <T> T deserialize(Type typeToken, String json) throws WebApiException {
        try {
            @Nullable
            T result = gson.fromJson(json, typeToken);
            if (result != null) {
                return result;
            }
            throw new WebApiException("Deserialization of '%s' resulted in null value".formatted(json));
        } catch (JsonSyntaxException e) {
            throw new WebApiException("Unexpected error deserializing '%s' : %s".formatted(json, e.getMessage()));
        }
    }
}
