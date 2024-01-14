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
package org.openhab.binding.loxone.internal.types;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Unique identifier of an object on Loxone Miniserver.
 * <p>
 * It is defined by the Miniserver. UUID can represent a control, room, category, etc. and provides a unique ID space
 * across all objects residing on the Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
public class LxUuid {
    private final String uuid;
    private final String uuidOriginal;

    public static final JsonDeserializer<LxUuid> DESERIALIZER = new JsonDeserializer<>() {
        @Override
        public LxUuid deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            String uuid = json.getAsString();
            if (uuid != null) {
                return new LxUuid(uuid);
            }
            return null;
        }
    };

    /**
     * Create a new {@link LxUuid} object from an UUID on a Miniserver.
     *
     * @param uuid identifier retrieved from Loxone Miniserver
     */
    public LxUuid(String uuid) {
        uuidOriginal = uuid;
        this.uuid = init(uuid);
    }

    public LxUuid(byte[] data, int offset) {
        String id = String.format("%08x-%04x-%04x-%02x%02x%02x%02x%02x%02x%02x%02x",
                ByteBuffer.wrap(data, offset, 4).order(ByteOrder.LITTLE_ENDIAN).getInt(),
                ByteBuffer.wrap(data, offset + 4, 2).order(ByteOrder.LITTLE_ENDIAN).getShort(),
                ByteBuffer.wrap(data, offset + 6, 2).order(ByteOrder.LITTLE_ENDIAN).getShort(), data[offset + 8],
                data[offset + 9], data[offset + 10], data[offset + 11], data[offset + 12], data[offset + 13],
                data[offset + 14], data[offset + 15]);
        uuidOriginal = id;
        this.uuid = init(id);
    }

    private String init(String uuid) {
        return uuidOriginal.replaceAll("[^a-zA-Z0-9-]", "-").toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        LxUuid id = (LxUuid) o;
        return uuid.equals(id.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return uuid;
    }

    /**
     * Returns an original string that was used to create UUID.
     *
     * @return original string for the UUID
     */
    public String getOriginalString() {
        return uuidOriginal;
    }
}
