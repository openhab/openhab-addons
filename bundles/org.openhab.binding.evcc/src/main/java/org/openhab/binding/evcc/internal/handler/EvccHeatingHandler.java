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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.JSON_MEMBER_LOADPOINTS;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.JsonObject;

/**
 * The {@link EvccHeatingHandler} is responsible for fetching the data from the API response for Heating things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccHeatingHandler extends EvccLoadpointHandler {

    private static final HashMap<String, String> JSON_KEYS;

    static {
        JSON_KEYS = new HashMap<>();
        JSON_KEYS.put("effectiveLimitSoc", "effectiveLimitTemperature");
        JSON_KEYS.put("effectivePlanSoc", "effectivePlanTemperature");
        JSON_KEYS.put("limitSoc", "limitTemperature");
        JSON_KEYS.put("vehicleLimitSoc", "vehicleLimitTemperature");
        JSON_KEYS.put("vehicleSoc", "vehicleTemperature");
    }

    public EvccHeatingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String id = channelUID.getId();
        if (JSON_KEYS.containsValue(id)) {
            // Replace the generated key with the original one to get the correct
            String thingKey = getThingKey(id.replace("Temperature", "Soc"));
            channelUID = new ChannelUID(getThing().getUID(), thingKey);
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        updateJSON(state);
        super.updateFromEvccState(state);
    }

    protected void updateJSON(JsonObject state) {
        JsonObject heatingState = state.getAsJsonArray(JSON_MEMBER_LOADPOINTS).get(index).getAsJsonObject();
        renameJsonKeys(heatingState); // rename the json keys
        state.getAsJsonArray(JSON_MEMBER_LOADPOINTS).set(index, heatingState); // Update the keys in the original JSON
    }

    private static void renameJsonKeys(JsonObject json) {
        JSON_KEYS.forEach((oldKey, newKey) -> {
            if (json.has(oldKey)) {
                json.add(newKey, json.get(oldKey));
                json.remove(oldKey);
            }
        });
    }
}
