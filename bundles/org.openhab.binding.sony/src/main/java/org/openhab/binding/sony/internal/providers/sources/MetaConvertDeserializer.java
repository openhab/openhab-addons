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
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Deserializer used to deserialize {@link MetaConvert} classes
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class MetaConvertDeserializer implements JsonDeserializer<MetaConvert> {
    @Override
    public MetaConvert deserialize(final @Nullable JsonElement je, final @Nullable Type type,
            final @Nullable JsonDeserializationContext context) throws JsonParseException {
        Objects.requireNonNull(je, "je cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(context, "context cannot be null");

        if (je instanceof JsonObject) {
            final JsonObject jo = je.getAsJsonObject();

            final JsonElement oldName = jo.get("oldName");
            if (oldName == null) {
                throw new JsonParseException("oldName must be specified");
            }

            final String pattern = oldName.getAsString();
            Pattern oldNamePattern;
            try {
                oldNamePattern = Pattern.compile(pattern);
            } catch (final PatternSyntaxException e) {
                throw new JsonParseException("oldName '" + pattern + "' was not a valid pattern", e);
            }

            final JsonElement newName = jo.get("newName");
            if (newName == null) {
                throw new JsonParseException("newName must be specified");
            }

            final String newNameStr = newName.getAsString();
            if (newNameStr == null || StringUtils.isEmpty(newNameStr)) {
                throw new JsonParseException("newName cannot be empty");
            }

            return new MetaConvert(oldNamePattern, newNameStr);
        }
        throw new JsonParseException("The json element isn't a JsonObject and cannot be deserialized");
    }
}
