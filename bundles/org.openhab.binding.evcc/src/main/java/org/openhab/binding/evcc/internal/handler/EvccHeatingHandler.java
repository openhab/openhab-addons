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

import java.util.Arrays;

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

    private static final String[] TEMPERATURE_CHANNEL_IDS = { "loadpoint-effective-limit-temperature",
            "loadpoint-limit-temperature", "loadpoint-vehicle-temperature" };

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
        if (Arrays.asList(TEMPERATURE_CHANNEL_IDS).contains(id)) {
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
        if (heatingState.has("vehicleLimitSoc")) {
            heatingState.addProperty("vehicleLimitTemperature", heatingState.get("vehicleLimitSoc").getAsDouble());
            heatingState.remove("vehicleLimitSoc");
        }
        if (heatingState.has("effectiveLimitSoc")) {
            heatingState.add("effectiveLimitTemperature", heatingState.get("effectiveLimitSoc"));
            heatingState.remove("effectiveLimitSoc");
        }
        if (heatingState.has("vehicleSoc")) {
            heatingState.add("vehicleTemperature", heatingState.get("vehicleSoc"));
            heatingState.remove("vehicleSoc");
        }
        state.getAsJsonArray(JSON_MEMBER_LOADPOINTS).set(index, heatingState); // Update the IDs in the original JSON
    }
}
