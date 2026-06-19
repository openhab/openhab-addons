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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.serializer;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homeconnectdirect.internal.service.websocket.model.Resource;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson adapter for {@link Resource} serialization and deserialization.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class ResourceAdapter implements JsonDeserializer<Resource>, JsonSerializer<Resource> {
    @Override
    public @Nullable Resource deserialize(JsonElement jsonElement, Type type,
            JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String resourceString = jsonElement.getAsString();
        String[] parts = resourceString.split("/", 3);
        String service = parts.length > 1 ? parts[1] : "";
        String endpoint = parts.length > 2 ? parts[2] : "";

        return new Resource(service, endpoint);
    }

    @Override
    public JsonElement serialize(Resource resource, Type type, JsonSerializationContext jsonSerializationContext) {
        String resourceString = "/" + resource.service() + "/" + resource.endpoint();
        return new JsonPrimitive(resourceString);
    }
}
