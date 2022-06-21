/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * Specialized deserializer for push_type field
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class NAPushTypeDeserializer implements JsonDeserializer<NAPushType> {
    private final Logger logger = LoggerFactory.getLogger(NAPushTypeDeserializer.class);

    @Override
    public @Nullable NAPushType deserialize(JsonElement json, Type clazz, JsonDeserializationContext context) {
        ModuleType moduleType = ModuleType.UNKNOWN;
        EventType eventType = EventType.UNKNOWN;
        String string = json.getAsString();
        String[] elements = string.split("-");
        try {
            if (elements.length != 2) {
                throw new IllegalArgumentException();
            }
            moduleType = ModuleType.from(elements[0]);
            eventType = EventType.valueOf(elements[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Error deserializing push type: {}", string);
        }
        return new NAPushType(moduleType, eventType);
    }
}
