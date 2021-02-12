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
package org.openhab.binding.netatmo.internal.api;

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.NADynamicObjectMap;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link NADynamicObjectMapDeserializer} is a specialized deserializer aimed to transform
 * a list of `NAObjects` into a map identified by the object's id.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NADynamicObjectMapDeserializer implements JsonDeserializer<NADynamicObjectMap> {
    private final Logger logger = LoggerFactory.getLogger(NADynamicObjectMapDeserializer.class);

    @Override
    public @Nullable NADynamicObjectMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        if (json instanceof JsonArray) {
            NADynamicObjectMap result = new NADynamicObjectMap();
            for (JsonElement item : (JsonArray) json) {
                JsonObject jsonO = item.getAsJsonObject();
                String thingType = jsonO.get("type").getAsString();
                ModuleType module = ModuleType.valueOf(thingType);
                if (module.dto != null) {
                    NAThing obj = context.deserialize(item, module.dto);
                    result.put(obj.getId(), obj);
                } else {
                    logger.warn("Unable to find appropriate dto for thing of type : {}", thingType);
                }
            }
            return result;
        }
        return null;
    }
}
