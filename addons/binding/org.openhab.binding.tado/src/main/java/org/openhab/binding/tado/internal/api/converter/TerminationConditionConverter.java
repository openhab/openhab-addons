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

import org.openhab.binding.tado.internal.api.model.ManualTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationCondition;
import org.openhab.binding.tado.internal.api.model.OverlayTerminationConditionType;
import org.openhab.binding.tado.internal.api.model.TadoModeTerminationCondition;
import org.openhab.binding.tado.internal.api.model.TimerTerminationCondition;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Gson converter to handle type-hierarchy of {@link OverlayTerminationCondition}.
 *
 * @author Dennis Frommknecht - Initial contribution
 */
public class TerminationConditionConverter
        implements JsonSerializer<OverlayTerminationCondition>, JsonDeserializer<OverlayTerminationCondition> {
    @Override
    public JsonElement serialize(OverlayTerminationCondition src, Type srcType, JsonSerializationContext context) {
        if (src instanceof ManualTerminationCondition) {
            return context.serialize(src, ManualTerminationCondition.class);
        } else if (src instanceof TadoModeTerminationCondition) {
            return context.serialize(src, TadoModeTerminationCondition.class);
        } else if (src instanceof TimerTerminationCondition) {
            return context.serialize(src, TimerTerminationCondition.class);
        }

        return new JsonObject();
    }

    @Override
    public OverlayTerminationCondition deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        OverlayTerminationConditionType terminationType = OverlayTerminationConditionType
                .valueOf(json.getAsJsonObject().get("type").getAsString());

        if (terminationType == OverlayTerminationConditionType.MANUAL) {
            return context.deserialize(json, ManualTerminationCondition.class);
        } else if (terminationType == OverlayTerminationConditionType.TADO_MODE) {
            return context.deserialize(json, TadoModeTerminationCondition.class);
        } else if (terminationType == OverlayTerminationConditionType.TIMER) {
            return context.deserialize(json, TimerTerminationCondition.class);
        }

        return null;
    }
}
