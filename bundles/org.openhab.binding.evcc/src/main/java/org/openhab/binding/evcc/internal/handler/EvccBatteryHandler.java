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

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.handler.routing.FallbackExtraction;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.binding.evcc.internal.handler.routing.MatchingJsonObjectExtraction;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccBatteryHandler} is responsible for fetching the data from the API response for Battery things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBatteryHandler extends EvccBaseThingHandler {

    private final int index;

    public EvccBatteryHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        this.index = Integer.parseInt(getPropertyOrConfigValue(PROPERTY_INDEX));
        type = PROPERTY_TYPE_BATTERY;
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL();
            handler.register(this);

            // Register route for battery handler with indexed array extraction
            MessageRouter router = handler.getMessageRouter();
            router.registerRoute(
                    new HandlerRoute(JSON_KEY_BATTERY, new FallbackExtraction(new MatchingJsonObjectExtraction("power"),
                            new JsonPathExtraction("$.devices[" + index + "]")), this, JSON_KEY_BATTERY));
        });
    }

    @Override
    public Integer getIdentifier() {
        return (Integer) index;
    }

    @Override
    public void initializeThingFromLatestState(JsonObject state) {
        logger.debug("Battery handler initializing from state");
        state = getBatteryState(state);
        if (state.isEmpty()) {
            logger.debug("No battery state found for index {}", index);
            return;
        }
        createChannelsAndSetStatesFromApiResponse(state);
        logger.debug("Battery handler initialized successfully");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return getBatteryState(state);
    }

    @Override
    public void handleUpdate(String key, JsonElement value) {
        if (JSON_KEY_BATTERY.equals(key) && value.isJsonObject()) {
            updateOnlyPresentChannels(value.getAsJsonObject());
            updateStatus(ThingStatus.ONLINE);
            return;
        }
        super.handleUpdate(key, value);
    }

    private JsonObject getBatteryState(JsonObject state) {
        return state.has(JSON_KEY_BATTERY) ? extractBatteryState(state.get(JSON_KEY_BATTERY)) : new JsonObject();
    }

    private JsonObject extractBatteryState(JsonElement battery) {
        if (battery.isJsonNull()) {
            return new JsonObject();
        }
        JsonArray devices = battery.isJsonArray() ? battery.getAsJsonArray()
                : battery.getAsJsonObject().getAsJsonArray(JSON_KEY_DEVICES);
        return devices != null && index < devices.size() && devices.get(index).isJsonObject()
                ? devices.get(index).getAsJsonObject()
                : new JsonObject();
    }
}
