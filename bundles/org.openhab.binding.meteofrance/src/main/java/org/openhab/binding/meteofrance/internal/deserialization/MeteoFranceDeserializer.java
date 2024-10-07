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
package org.openhab.binding.meteofrance.internal.deserialization;

import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meteofrance.internal.MeteoFranceException;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Periods;
import org.openhab.binding.meteofrance.internal.dto.MeteoFrance.Timelaps;
import org.openhab.core.i18n.TimeZoneProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link MeteoFranceDeserializer} is responsible to instantiate suitable Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = MeteoFranceDeserializer.class)
public class MeteoFranceDeserializer {
    private final Gson gson;

    @Activate
    public MeteoFranceDeserializer(final @Reference TimeZoneProvider timeZoneProvider) {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapterFactory(new StrictEnumTypeAdapterFactory())
                .registerTypeAdapter(Periods.class, new PeriodsDeserializer())
                .registerTypeAdapter(Timelaps.class, new TimelapsDeserializer())
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, context) -> ZonedDateTime
                                .parse(json.getAsJsonPrimitive().getAsString())
                                .withZoneSameInstant(timeZoneProvider.getTimeZone()))
                .create();
    }

    public <T> T deserialize(Class<T> clazz, String json) throws MeteoFranceException {
        try {
            @Nullable
            T result = gson.fromJson(json, clazz);
            if (result != null) {
                return result;
            }
            throw new MeteoFranceException("Deserialization of '%s' resulted in null value", json);
        } catch (JsonSyntaxException e) {
            throw new MeteoFranceException(e, "Unexpected error deserializing '%s'", json);
        }
    }
}
