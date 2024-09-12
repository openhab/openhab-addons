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
package org.openhab.binding.sleepiq.internal.api.impl.typeadapters;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sleepiq.internal.api.dto.FoundationPosition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * The {@link FoundationPositionTypeAdapter} converts the hex string position from the
 * foundation status response into an integer value. The position is returned by the API
 * as a two character hex string (e.g. "3f"). The position can be between 0 and 100, inclusive.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class FoundationPositionTypeAdapter implements JsonDeserializer<FoundationPosition> {

    @Override
    public @Nullable FoundationPosition deserialize(JsonElement element, Type arg1, JsonDeserializationContext arg2)
            throws JsonParseException {
        try {
            return new FoundationPosition().withFoundationPosition(Integer.parseInt(element.getAsString(), 16));
        } catch (NumberFormatException e) {
            // If we can't parse it, just set to 0
            return new FoundationPosition().withFoundationPosition(Integer.valueOf(0));
        }
    }
}
