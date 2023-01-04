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
package org.openhab.binding.unifi.internal.api.util;

import java.lang.reflect.Type;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * The {@link UniFiTimestampDeserializer} is an implementation of {@link JsonDeserializer} that deserializes timestamps
 * returned in the JSON responses of the UniFi controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiTimestampDeserializer implements JsonDeserializer<Instant> {

    @Override
    public @Nullable Instant deserialize(final JsonElement json, final Type type,
            final JsonDeserializationContext context) {
        final String text = json.getAsJsonPrimitive().getAsString();
        final long millis = Long.valueOf(text) * 1000;

        return Instant.ofEpochMilli(millis);
    }
}
