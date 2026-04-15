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

import java.lang.reflect.Type;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ntfy.internal.models.BaseEvent;
import org.openhab.binding.ntfy.internal.models.MessageDeleteEvent;
import org.openhab.binding.ntfy.internal.models.MessageEvent;
import org.openhab.binding.ntfy.internal.models.OpenEvent;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Gson deserializer that converts incoming JSON into specific {@link BaseEvent}
 * implementations depending on the event type field.
 *
 * @author Christian Kittel - Initial contribution
 */
@NonNullByDefault
public class EventDeserializer implements JsonDeserializer<BaseEvent> {
    @Override
    public @Nullable BaseEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        JsonElement eventElement = jsonObject.get("event");

        if (eventElement != null) {
            String eventType = eventElement.getAsString();
            switch (eventType.toLowerCase(Locale.ROOT)) {
                case "message":
                    return context.deserialize(jsonObject, MessageEvent.class);
                case "open":
                    return context.deserialize(jsonObject, OpenEvent.class);
                case "message_delete":
                    return context.deserialize(jsonObject, MessageDeleteEvent.class);
            }
        }

        throw new JsonParseException("unknown event type");
    }
}
