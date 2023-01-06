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
package org.openhab.binding.mycroft.internal.api;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Custom deserializer
 *
 * @author Gwendal Roulleau - Initial contribution
 */
@NonNullByDefault
public class MessageTypeConverter implements JsonDeserializer<MessageType>, JsonSerializer<MessageType> {
    @Override
    public @Nullable MessageType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        MessageType messageType = MessageType.fromString(json.getAsString());
        // for message of type non recognized :
        messageType.setMessageTypeName(json.getAsString());
        return messageType;
    }

    @Override
    public JsonElement serialize(MessageType src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.getMessageTypeName());
    }
}
