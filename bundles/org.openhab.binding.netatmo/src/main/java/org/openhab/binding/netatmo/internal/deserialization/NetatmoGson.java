/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.deserialization;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

/**
 * The {@link NetatmoGson} is responsible to instantiate appropriate Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = NetatmoGson.class)
public class NetatmoGson {
    private final Gson gson;

    @Activate
    public NetatmoGson(@Reference TimeZoneProvider timeZoneProvider) {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory())
                .registerTypeAdapter(NAObjectMap.class, new NAObjectMapDeserializer())
                .registerTypeAdapter(NAThingMap.class, new NAThingMapDeserializer())
                .registerTypeAdapter(NAPushType.class, new NAPushTypeDeserializer())
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> {
                            long netatmoTS = json.getAsJsonPrimitive().getAsLong();
                            Instant i = Instant.ofEpochSecond(netatmoTS);
                            return ZonedDateTime.ofInstant(i, timeZoneProvider.getTimeZone());
                        })
                .registerTypeAdapter(OnOffType.class,
                        (JsonDeserializer<OnOffType>) (json, type, jsonDeserializationContext) -> OnOffType
                                .from(json.getAsJsonPrimitive().getAsString()))
                .registerTypeAdapter(OpenClosedType.class,
                        (JsonDeserializer<OpenClosedType>) (json, type, jsonDeserializationContext) -> {
                            String value = json.getAsJsonPrimitive().getAsString().toUpperCase();
                            return "TRUE".equals(value) || "1".equals(value) ? OpenClosedType.CLOSED
                                    : OpenClosedType.OPEN;
                        })
                .create();
    }

    public Gson getGson() {
        return gson;
    }
}
