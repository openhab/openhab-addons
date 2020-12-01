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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiVersionInfo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class is responsible for deserializing a {@link SupportedApiVersionInfo} string
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApiVersionInfoDeserializer implements JsonDeserializer<SupportedApiVersionInfo> {
    @Override
    public SupportedApiVersionInfo deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();
            if (!jo.has("version")) {
                throw new JsonParseException("version element not found and is required");
            }

            // authlevel is optional
            final String authLevel = jo.has("authLevel") ? jo.get("authLevel").getAsString() : null;

            // protocols is optional
            final Set<String> protocols = new HashSet<>();
            final JsonElement protElm = jo.get("protocols");
            if (jo.has("protocols")) {
                if (!protElm.isJsonArray()) {
                    throw new JsonParseException("protocols element is not an array");
                }

                for (final JsonElement elm : protElm.getAsJsonArray()) {
                    final String proto = elm.getAsString();
                    // ignore empty/null elements
                    if (proto != null && StringUtils.isNotEmpty(proto)) {
                        protocols.add(proto);
                    }
                }
            }

            final String version = jo.get("version").getAsString();
            if (version == null || StringUtils.isEmpty(version)) {
                throw new JsonParseException("version element is empty and is required");
            }

            return new SupportedApiVersionInfo(authLevel == null ? "" : authLevel, protocols, version);
        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
