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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.HeatingStateTransformer;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.binding.evcc.internal.handler.routing.StateTransformer;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * The {@link EvccHeatingHandler} is responsible for fetching the data from the API response for Heating things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccHeatingHandler extends EvccLoadpointHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccHeatingHandler.class);

    private final StateTransformer heatingTransformer = new HeatingStateTransformer();

    public EvccHeatingHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        type = PROPERTY_TYPE_HEATING;
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            MessageRouter router = handler.getMessageRouter();
            router.registerRoute(
                    new HandlerRoute(PROPERTY_TYPE_HEATING, new JsonPathExtraction("$"), this, PROPERTY_TYPE_HEATING));
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String key = Utils.getKeyFromChannelUID(channelUID);
        @Nullable
        String apiKey = HeatingStateTransformer.toApiKey(key);
        if (apiKey != null) {
            // Address the original temperature endpoint instead of the remapped SoC channel key
            channelUID = new ChannelUID(getThing().getUID(), getThingKey(apiKey));
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    public void initializeThingFromLatestState(JsonObject state) {
        logger.trace("Heating handler initializing from state");
        JsonArray loadpoints = state.getAsJsonArray(JSON_KEY_LOADPOINTS);
        if (loadpoints == null || index >= loadpoints.size() || !loadpoints.get(index).isJsonObject()) {
            logger.debug("Heating index {} out of bounds or invalid (size {})", index,
                    loadpoints != null ? loadpoints.size() : 0);
            return;
        }
        JsonObject normalized = heatingTransformer.transform(loadpoints.get(index).getAsJsonObject());
        loadpoints.set(index, normalized);
        createChannelsAndSetStatesFromApiResponse(normalized);
        logger.trace("Heating handler initialized successfully");
        updateStatus(ThingStatus.ONLINE);
    }
}
