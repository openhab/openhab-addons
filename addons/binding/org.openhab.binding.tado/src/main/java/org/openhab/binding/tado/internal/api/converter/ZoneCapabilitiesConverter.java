/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tado.internal.api.converter;

import java.lang.reflect.Type;

import org.openhab.binding.tado.internal.api.model.AirConditioningCapabilities;
import org.openhab.binding.tado.internal.api.model.GenericZoneCapabilities;
import org.openhab.binding.tado.internal.api.model.HeatingCapabilities;
import org.openhab.binding.tado.internal.api.model.HotWaterCapabilities;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson converter to handle type-hierarchy of {@link GenericZoneCapabilities}.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class ZoneCapabilitiesConverter
        implements JsonSerializer<GenericZoneCapabilities>, JsonDeserializer<GenericZoneCapabilities> {
    @Override
    public JsonElement serialize(GenericZoneCapabilities src, Type srcType, JsonSerializationContext context) {
        if (src instanceof HeatingCapabilities) {
            return context.serialize(src, HeatingCapabilities.class);
        } else if (src instanceof AirConditioningCapabilities) {
            return context.serialize(src, AirConditioningCapabilities.class);
        } else if (src instanceof HotWaterCapabilities) {
            return context.serialize(src, HotWaterCapabilities.class);
        }

        return new JsonObject();
    }

    @Override
    public GenericZoneCapabilities deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
        TadoSystemType settingType = TadoSystemType.valueOf(json.getAsJsonObject().get("type").getAsString());

        if (settingType == TadoSystemType.HEATING) {
            return context.deserialize(json, HeatingCapabilities.class);
        } else if (settingType == TadoSystemType.AIR_CONDITIONING) {
            return context.deserialize(json, AirConditioningCapabilities.class);
        } else if (settingType == TadoSystemType.HOT_WATER) {
            return context.deserialize(json, HotWaterCapabilities.class);
        }

        return null;
    }
}
