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
import org.openhab.binding.netatmo.internal.api.data.ModuleType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

/**
 * Specialized deserializer for ModuleType class
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class ModuleTypeDeserializer implements JsonDeserializer<ModuleType> {

    @Override
    public @Nullable ModuleType deserialize(JsonElement json, Type clazz, JsonDeserializationContext context) {
        String string = json.getAsString();
        return ModuleType.AS_SET.stream().filter(mt -> mt.apiName.equalsIgnoreCase(string)).findFirst()
                .orElse(ModuleType.UNKNOWN);
    }
}
