/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model.deser;

import java.lang.reflect.Type;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.gardena.internal.model.PropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Custom deserializer for Gardena complex property value type.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PropertyValueDeserializer implements JsonDeserializer<PropertyValue> {
    private final Logger logger = LoggerFactory.getLogger(PropertyValueDeserializer.class);

    private static final String PROPERTY_DURATION = "duration";
    private static final String PROPERTY_TYPE = "type";

    @Override
    public PropertyValue deserialize(JsonElement element, Type type, JsonDeserializationContext ctx)
            throws JsonParseException {
        if (element.isJsonObject()) {
            JsonObject jsonObj = element.getAsJsonObject();
            if (jsonObj.has(PROPERTY_DURATION)) {
                long duration = jsonObj.get(PROPERTY_DURATION).getAsLong();
                if (duration != 0) {
                    duration = Math.round(duration / 60.0);
                }
                return new PropertyValue(String.valueOf(duration));
            } else if (jsonObj.has(PROPERTY_TYPE)) {
                return new PropertyValue(jsonObj.get(PROPERTY_TYPE).getAsString());
            } else {
                logger.warn("Unsupported json value object, returning empty value");
                return new PropertyValue();
            }

        } else if (element.isJsonArray()) {
            JsonArray jsonArray = element.getAsJsonArray();
            return new PropertyValue(StringUtils.join(jsonArray.iterator(), ","));
        } else {
            return new PropertyValue(element.getAsString());
        }
    }

}
