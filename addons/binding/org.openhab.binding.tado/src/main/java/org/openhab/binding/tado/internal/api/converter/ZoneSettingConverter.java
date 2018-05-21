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

import org.openhab.binding.tado.internal.api.model.CoolingZoneSetting;
import org.openhab.binding.tado.internal.api.model.GenericZoneSetting;
import org.openhab.binding.tado.internal.api.model.HeatingZoneSetting;
import org.openhab.binding.tado.internal.api.model.HotWaterZoneSetting;
import org.openhab.binding.tado.internal.api.model.TadoSystemType;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson converter to handle type-hierarchy of {@link GenericZoneSetting}.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class ZoneSettingConverter implements JsonSerializer<GenericZoneSetting>, JsonDeserializer<GenericZoneSetting> {
    @Override
    public JsonElement serialize(GenericZoneSetting src, Type srcType, JsonSerializationContext context) {
        if (src instanceof HeatingZoneSetting) {
            return context.serialize(src, HeatingZoneSetting.class);
        } else if (src instanceof CoolingZoneSetting) {
            return context.serialize(src, CoolingZoneSetting.class);
        } else if (src instanceof HotWaterZoneSetting) {
            return context.serialize(src, HotWaterZoneSetting.class);
        }

        return new JsonObject();
    }

    @Override
    public GenericZoneSetting deserialize(JsonElement json, Type type, JsonDeserializationContext context) {

        TadoSystemType settingType = TadoSystemType.valueOf(json.getAsJsonObject().get("type").getAsString());

        if (settingType == TadoSystemType.HEATING) {
            return context.deserialize(json, HeatingZoneSetting.class);
        } else if (settingType == TadoSystemType.HOT_WATER) {
            return context.deserialize(json, HotWaterZoneSetting.class);
        } else if (settingType == TadoSystemType.AIR_CONDITIONING) {
            return context.deserialize(json, CoolingZoneSetting.class);
        }

        return null;
    }
}
