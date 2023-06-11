/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 *
 * The {@link UniFiTidyLowerCaseStringDeserializer} is an implementation of {@link JsonDeserializer} that deserializes
 * strings in a tidy lower case format.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiTidyLowerCaseStringDeserializer implements JsonDeserializer<String> {

    @Override
    public @Nullable String deserialize(final JsonElement json, final Type type,
            final JsonDeserializationContext context) throws JsonParseException {
        final String s = json.getAsJsonPrimitive().getAsString();
        return s.trim().toLowerCase();
    }
}
