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
package org.openhab.binding.sony.internal.providers.sources;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializer used to deserialize {@link MetaInfo} classes
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class MetaInfoDeserializer implements JsonDeserializer<MetaInfo> {
    @Override
    public MetaInfo deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();

            boolean enabled = false;
            final List<Pattern> ignoreModelName = new ArrayList<>();
            final List<Pattern> ignoreChannelId = new ArrayList<>();
            final List<MetaConvert> modelNameConvert = new ArrayList<>();
            final List<MetaConvert> channelIdConvert = new ArrayList<>();

            final JsonElement enb = jo.get("enabled");
            if (enb != null) {
                enabled = Boolean.parseBoolean(enb.getAsString());
            }

            final JsonElement imnList = jo.get("ignoreModelName");
            if (imnList != null) {
                if (!imnList.isJsonArray()) {
                    throw new JsonParseException("ignoreModelName element is not an array");
                }

                for (final JsonElement elm : imnList.getAsJsonArray()) {
                    final String pattern = elm.getAsString();
                    try {
                        ignoreModelName.add(Pattern.compile(pattern));
                    } catch (PatternSyntaxException e) {
                        throw new JsonParseException("ignoreModelName '" + pattern + "' was not a valid pattern", e);
                    }
                }
            }

            final JsonElement iciList = jo.get("ignoreChannelId");
            if (iciList != null) {
                if (!iciList.isJsonArray()) {
                    throw new JsonParseException("ignoreChannelId element is not an array");
                }

                for (final JsonElement elm : iciList.getAsJsonArray()) {
                    final String pattern = elm.getAsString();
                    try {
                        ignoreChannelId.add(Pattern.compile(pattern));
                    } catch (PatternSyntaxException e) {
                        throw new JsonParseException("ignoreChannelId '" + pattern + "' was not a valid pattern", e);
                    }
                }
            }

            final JsonElement mncList = jo.get("modelNameConvert");
            if (mncList != null) {
                if (!mncList.isJsonArray()) {
                    throw new JsonParseException("modelNameConvert element is not an array");
                }

                for (final JsonElement elm : mncList.getAsJsonArray()) {
                    final MetaConvert conv = context.deserialize(elm, MetaConvert.class);
                    if (conv != null) {
                        modelNameConvert.add(conv);
                    }
                }
            }

            final JsonElement cicList = jo.get("channelIdConvert");
            if (cicList != null) {
                if (!cicList.isJsonArray()) {
                    throw new JsonParseException("channelIdConvert element is not an array");
                }

                for (final JsonElement elm : cicList.getAsJsonArray()) {
                    final MetaConvert conv = context.deserialize(elm, MetaConvert.class);
                    if (conv != null) {
                        channelIdConvert.add(conv);
                    }
                }
            }

            return new MetaInfo(enabled, ignoreModelName, ignoreChannelId, modelNameConvert, channelIdConvert);
        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
