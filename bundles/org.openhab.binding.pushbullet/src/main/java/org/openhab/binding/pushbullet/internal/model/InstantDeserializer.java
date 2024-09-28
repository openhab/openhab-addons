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
package org.openhab.binding.pushbullet.internal.model;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * The {@link InstantDeserializer} deserializes a timestamp returned by the API.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InstantDeserializer implements JsonDeserializer<Instant> {

    @Override
    public @Nullable Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        BigDecimal timestamp = json.getAsBigDecimal();
        BigDecimal[] parts = timestamp.divideAndRemainder(BigDecimal.ONE);
        long seconds = parts[0].longValueExact();
        long nanos = parts[1].movePointRight(9).longValue();
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
