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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sony.internal.scalarweb.models.ScalarWebResult;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApi;
import org.openhab.binding.sony.internal.scalarweb.models.api.SupportedApiInfo;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * This class represents the deserializer to deserialize a json element to a {@link ScalarWebResult}
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SupportedApiDeserializer implements JsonDeserializer<SupportedApi> {
    @Override
    public SupportedApi deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();

            if (!jo.has("service")) {
                throw new JsonParseException("service element not found and is required");
            }

            if (!jo.has("protocols")) {
                throw new JsonParseException("protocols element not found and is required");
            }

            if (!jo.has("apis")) {
                throw new JsonParseException("apis element not found and is required");
            }

            final String service = jo.get("service").getAsString();
            if (service == null || StringUtils.isEmpty(service)) {
                throw new JsonParseException("service element was empty and is required");
            }

            final JsonElement protElm = jo.get("protocols");
            if (!protElm.isJsonArray()) {
                throw new JsonParseException("protocols element is not an array");
            }

            final Set<String> protocols = new HashSet<>();
            for (final JsonElement elm : protElm.getAsJsonArray()) {
                final String proto = elm.getAsString();
                // ignore empty/null elements
                if (proto != null && StringUtils.isNotEmpty(proto)) {
                    protocols.add(proto);
                }
            }

            final JsonElement apisElm = jo.get("apis");
            if (!apisElm.isJsonArray()) {
                throw new JsonParseException("apis element is not an array");
            }

            final List<SupportedApiInfo> apis = new ArrayList<>();
            for (final JsonElement elm : apisElm.getAsJsonArray()) {
                apis.add(context.deserialize(elm, SupportedApiInfo.class));
            }

            // notifications are optional
            final List<SupportedApiInfo> notifications = new ArrayList<>();
            if (jo.has("notifications")) {
                final JsonElement notElm = jo.get("notifications");
                if (!notElm.isJsonArray()) {
                    throw new JsonParseException("notifications element is not an array");
                }

                for (final JsonElement elm : notElm.getAsJsonArray()) {
                    notifications.add(context.deserialize(elm, SupportedApiInfo.class));
                }
            }

            return new SupportedApi(service, apis, notifications, protocols);
        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
