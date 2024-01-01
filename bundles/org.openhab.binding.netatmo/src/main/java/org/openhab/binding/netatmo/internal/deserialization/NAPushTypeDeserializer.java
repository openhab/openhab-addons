/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
        final String string = json.getAsString();
        final String[] elements = string.split("-");
        ModuleType moduleType = ModuleType.UNKNOWN;
        EventType eventType = EventType.UNKNOWN;

        if (elements.length == 2) {
            moduleType = fromNetatmoObject(elements[0]);
            eventType = fromEvent(elements[1]);
        } else if (elements.length == 1) {
            eventType = fromEvent(string);
            moduleType = eventType.getFirstModule();
        }

        if (moduleType.equals(ModuleType.UNKNOWN) || eventType.equals(EventType.UNKNOWN)) {
            logger.warn("Unknown module or event type: {}, deserialized to '{}-{}'", string, moduleType, eventType);
        }

        return new NAPushType(moduleType, eventType);
    }

    /**
     * @param apiName : Netatmo Object name (NSD, NACamera...)
     * @return moduletype value if found, or else Unknown
     */
    public static ModuleType fromNetatmoObject(String apiName) {
        return ModuleType.AS_SET.stream().filter(mt -> apiName.equals(mt.apiName)).findFirst()
                .orElse(ModuleType.UNKNOWN);
    }

    /**
     * @param apiName : Netatmo Event name (hush, off, on ...)
     * @return eventType value if found, or else Unknown
     */
    public static EventType fromEvent(String apiName) {
        return EventType.AS_SET.stream().filter(et -> apiName.equalsIgnoreCase(et.name())).findFirst()
                .orElse(EventType.UNKNOWN);
    }
}
