/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link EvccHeatingHandler} is responsible for fetching the data from the API response for Heating things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccHeatingHandler extends EvccLoadpointHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccHeatingHandler.class);

    private static final Map<String, String> JSON_KEYS = Map.ofEntries(
            Map.entry("effectiveLimitTemperature", JSON_KEY_EFFECTIVE_LIMIT_SOC),
            Map.entry("effectivePlanTemperature", JSON_KEY_EFFECTIVE_PLAN_SOC),
            Map.entry("limitTemperature", JSON_KEY_LIMIT_SOC),
            Map.entry("vehicleLimitTemperature", JSON_KEY_VEHICLE_LIMIT_SOC),
            Map.entry("vehicleTemperature", JSON_KEY_VEHICLE_SOC));

    public EvccHeatingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        type = PROPERTY_TYPE_HEATING;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String key = Utils.getKeyFromChannelUID(channelUID);
        if (JSON_KEYS.containsKey(key)) {
            // Replace the temperature key with the original one
            @Nullable
            String tmp = JSON_KEYS.get(key);
            if (null != tmp) {
                channelUID = new ChannelUID(getThing().getUID(), getThingKey(tmp));
            } else {
                logger.debug("Unknown key: {}", key);
                return;
            }
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        updateJSON(state);
        updateStatesFromApiResponse(state);
    }

    protected void updateJSON(JsonObject state) {
        JsonObject heatingState = state.getAsJsonArray(JSON_KEY_LOADPOINTS).get(index).getAsJsonObject();
        renameJsonKeys(heatingState); // rename the JSON keys
        state.getAsJsonArray(JSON_KEY_LOADPOINTS).set(index, heatingState); // Update the keys in the original JSON
    }

    private static void renameJsonKeys(JsonObject json) {
        JSON_KEYS.forEach((newKey, oldKey) -> {
            if (json.has(oldKey)) {
                json.add(newKey, json.get(oldKey));
                json.remove(oldKey);
            }
        });
    }
}
