/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.airparif.internal.deserialization;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.airparif.internal.api.PollenAlertLevel;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

/**
 * Specialized deserializer for ColorMap class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
class PollenAlertLevelDeserializer implements JsonDeserializer<PollenAlertLevel> {

    @Override
    public @Nullable PollenAlertLevel deserialize(JsonElement json, Type clazz, JsonDeserializationContext context) {
        int level;
        try {
            level = json.getAsInt();
        } catch (JsonSyntaxException ignore) {
            return PollenAlertLevel.UNKNOWN;
        }

        return PollenAlertLevel.AS_SET.stream().filter(s -> s.riskLevel == level).findFirst()
                .orElse(PollenAlertLevel.UNKNOWN);
    }
}
