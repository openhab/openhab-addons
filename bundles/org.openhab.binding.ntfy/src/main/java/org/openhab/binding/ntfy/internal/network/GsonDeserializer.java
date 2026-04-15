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
package org.openhab.binding.ntfy.internal.network;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ntfy.internal.models.BaseEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Helper wrapper around Gson used to deserialize incoming ntfy JSON messages
 * into typed event objects.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class GsonDeserializer {

    private static final Gson GSON = new GsonBuilder().registerTypeAdapter(BaseEvent.class, new EventDeserializer())
            .registerTypeAdapter(Instant.class, new InstantDeserializer()).create();

    /**
     * Deserializes the provided JSON message into a {@link BaseEvent} instance.
     *
     * @param message the raw JSON message
     * @return the deserialized {@link BaseEvent} or {@code null} when parsing failed
     */
    public static @Nullable BaseEvent deserialize(String message) throws JsonSyntaxException {
        return GSON.fromJson(message, BaseEvent.class);
    }

    /**
     * Deserializes the provided JSON message into an instance of the given target
     * class.
     *
     * @param <T> the target type
     * @param message the raw JSON message
     * @param classOfT the target class to deserialize into
     * @return the deserialized instance or {@code null} when parsing failed
     */
    public static @Nullable <T> T deserialize(String message, Class<T> classOfT) {
        return GSON.fromJson(message, classOfT);
    }
}
