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
package org.openhab.binding.salus.internal.aws.http;

import java.lang.reflect.Type;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class InstantDeserializer implements JsonDeserializer<@org.eclipse.jdt.annotation.Nullable Instant> {
    @Override
    @org.eclipse.jdt.annotation.Nullable
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Instant.ofEpochSecond(json.getAsLong());
    }
}
