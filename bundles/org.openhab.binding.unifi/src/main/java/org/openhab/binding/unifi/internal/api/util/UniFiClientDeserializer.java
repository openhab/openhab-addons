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
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiUnknownClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWirelessClient;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 *
 * The {@link UniFiClientDeserializer} is an implementation of {@link JsonDeserializer} that deserializes
 * {@link UniFiClient} instances based on each client's <code>is_wired</code> property contained in the JSON output of
 * the controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiClientDeserializer implements JsonDeserializer<UniFiClient> {

    private static final String PROPERTY_IS_WIRED = "is_wired";

    @Override
    public @Nullable UniFiClient deserialize(final JsonElement json, final Type type,
            final JsonDeserializationContext context) throws JsonParseException {
        final JsonObject jsonObject = json.getAsJsonObject();
        final JsonElement wiredElement = jsonObject.get(PROPERTY_IS_WIRED);
        // mgb: if the "is_wired "property is missing, the client is unknown
        if (wiredElement == null) {
            return context.deserialize(json, UniFiUnknownClient.class);
        }
        if (wiredElement.getAsBoolean()) {
            return context.deserialize(json, UniFiWiredClient.class);
        }
        return context.deserialize(json, UniFiWirelessClient.class);
    }
}
