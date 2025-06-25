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
package org.openhab.binding.electroluxappliance.internal.dto;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * The {@link ActionDeserializer} class defines a deserializer required to support the capability information.
 *
 * @author David Goodyear - Initial contribution
 */
public class ActionDeserializer implements JsonDeserializer<ApplianceInfoDTO.Action> {

    @Override
    public ApplianceInfoDTO.Action deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
        ApplianceInfoDTO.Action action = new ApplianceInfoDTO.Action();
        JsonObject obj = json.getAsJsonObject();
        action.capabilities = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            ApplianceInfoDTO.Capability capability = context.deserialize(entry.getValue(),
                    ApplianceInfoDTO.Capability.class);
            action.capabilities.put(entry.getKey(), capability);
        }
        return action;
    }
}
