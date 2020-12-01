/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiInfo;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiVersionInfo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class is responsible for deserializing a {@link SupportedApiInfo} string
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApiInfoDeserializer implements JsonDeserializer<SupportedApiInfo> {
    @Override
    public SupportedApiInfo deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();
            if (!jo.has("name")) {
                throw new JsonParseException("name element not found and is required");
            }

            if (!jo.has("versions")) {
                throw new JsonParseException("versions element not found and is required");
            }

            final String name = jo.get("name").getAsString();
            if (name == null || StringUtils.isEmpty(name)) {
                throw new JsonParseException("name element was empty and is required");
            }

            final JsonElement versElm = jo.get("versions");
            if (!versElm.isJsonArray()) {
                throw new JsonParseException("versions element is not an array");
            }

            final List<SupportedApiVersionInfo> versions = new ArrayList<>();
            for (final JsonElement elm : versElm.getAsJsonArray()) {
                versions.add(context.deserialize(elm, SupportedApiVersionInfo.class));
            }

            return new SupportedApiInfo(name, versions);

        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
