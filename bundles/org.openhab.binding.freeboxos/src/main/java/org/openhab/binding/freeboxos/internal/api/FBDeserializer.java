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
package org.openhab.binding.freeboxos.internal.api;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.DEFAULT_CHARSET;

import java.time.Instant;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentProvider;
import org.eclipse.jetty.client.util.StringContentProvider;
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
 * The {@link FBDeserializer} is responsible to instantiate suitable Gson (de)serializer
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
@Component(service = FBDeserializer.class)
public class FBDeserializer {
    private final Gson gson;

    @Activate
    public FBDeserializer(@Reference TimeZoneProvider timeZoneProvider) {
        gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(ZonedDateTime.class,
                        (JsonDeserializer<ZonedDateTime>) (json, type, jsonDeserializationContext) -> {
                            long timestamp = json.getAsJsonPrimitive().getAsLong();
                            Instant i = Instant.ofEpochSecond(timestamp);
                            return ZonedDateTime.ofInstant(i, timeZoneProvider.getTimeZone());
                        })
                .create();
    }

    public <T> T deserialize(Class<T> clazz, String json) throws FreeboxException {
        try {
            @Nullable
            T result = gson.fromJson(json, clazz);
            if (result != null) {
                return result;
            }
            throw new FreeboxException("Deserialization of '%s' resulted in null value", json);
        } catch (JsonSyntaxException e) {
            throw new FreeboxException(e, "Unexpected error deserializing '%s'", json);
        }
    }

    public ContentProvider serialize(Object payload) {
        return new StringContentProvider(gson.toJson(payload), DEFAULT_CHARSET);
    }
}
