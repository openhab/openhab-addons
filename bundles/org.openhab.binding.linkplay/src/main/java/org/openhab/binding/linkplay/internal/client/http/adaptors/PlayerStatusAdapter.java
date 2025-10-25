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
package org.openhab.binding.linkplay.internal.client.http.adaptors;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linkplay.internal.client.http.dto.PlayerStatus;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Custom Gson deserializer for PlayerStatus. It converts the hex encoded
 * "Title", "Artist" and "Album" values into UTF-8 strings
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class PlayerStatusAdapter implements JsonDeserializer<PlayerStatus> {

    private static final Gson GSON_INTERNAL = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    @Override
    public @Nullable PlayerStatus deserialize(@Nullable JsonElement json, @Nullable Type typeOfT,
            @Nullable JsonDeserializationContext context) throws JsonParseException {
        if (json == null) {
            throw new JsonParseException("Expected JSON object for PlayerStatus");
        }
        @Nullable
        PlayerStatus ps = GSON_INTERNAL.fromJson(json, PlayerStatus.class);
        if (ps == null) {
            throw new JsonParseException("Failed to deserialize PlayerStatus");
        }

        ps.title = decodeHexSafe(ps.title);
        ps.artist = decodeHexSafe(ps.artist);
        ps.album = decodeHexSafe(ps.album);

        return ps;
    }

    private static String decodeHexSafe(String hex) {
        if (hex.length() % 2 != 0) {
            return hex;
        }
        try {
            int len = hex.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                        + Character.digit(hex.charAt(i + 1), 16));
            }
            return new String(data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return hex;
        }
    }
}
