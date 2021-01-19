/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.webhook;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.doc.EventType;
import org.openhab.binding.netatmo.internal.api.doc.ModuleType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Specialized deserializer for push_type field
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NAPushTypeDeserializer implements JsonDeserializer<NAPushType> {

    @Override
    public @Nullable NAPushType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        String string = json.getAsString();
        String[] elements = string.split("-");
        if (elements.length == 2) {
            ModuleType moduleType = ModuleType.valueOf(elements[0]);
            EventType eventType = EventType.valueOf(elements[1]);

            return new NAPushType(moduleType, eventType);
        }
        return null;
    }
}
