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
package org.openhab.binding.jellyfin.internal.api.util;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Custom UUID deserializer for Jellyfin API responses.
 * 
 * Jellyfin servers return UUIDs as 32-character strings without hyphens,
 * but Jackson's default UUID deserializer expects the standard 36-character
 * format with hyphens. This deserializer handles both formats.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class UuidDeserializer extends JsonDeserializer<UUID> {

    @Override
    public @Nullable UUID deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isEmpty()) {
            return null;
        }

        // If the UUID string is 32 characters long (without hyphens), format it properly
        if (value.length() == 32 && !value.contains("-")) {
            // Format: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            // To: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
            String formattedUuid = value.substring(0, 8) + "-" + value.substring(8, 12) + "-" + value.substring(12, 16)
                    + "-" + value.substring(16, 20) + "-" + value.substring(20);
            return UUID.fromString(formattedUuid);
        } else {
            // Use standard UUID parsing for properly formatted UUIDs
            return UUID.fromString(value);
        }
    }
}
