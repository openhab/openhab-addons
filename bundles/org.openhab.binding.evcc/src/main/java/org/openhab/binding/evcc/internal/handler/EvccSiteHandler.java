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

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.handler.routing.GridStateTransformer;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.binding.evcc.internal.handler.routing.ObjectFieldExtraction;
import org.openhab.binding.evcc.internal.handler.routing.StateTransformer;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * The {@link EvccSiteHandler} is responsible for fetching the data from the API response for Site things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccSiteHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccSiteHandler.class);

    private final StateTransformer gridTransformer = new GridStateTransformer();

    public EvccSiteHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        type = PROPERTY_TYPE_SITE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State state) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase(Locale.ROOT).replaceAll("co2|price",
                    "");
            String value;
            if (state instanceof OnOffType) {
                value = state == OnOffType.ON ? "true" : "false";
            } else {
                value = state.toString();
                if (value.contains(" ")) {
                    value = value.substring(0, state.toString().indexOf(" "));
                }
            }
            String url = endpoint + "/" + datapoint + "/" + value;
            logger.debug("Sending command to this url: {}", url);
            performApiRequest(url, POST, JsonNull.INSTANCE);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public String getIdentifier() {
        return "";
    }

    @Override
    public void initializeThingFromLatestState(JsonObject state) {
        logger.trace("Site handler initializing from state");
        // Set the smart cost type
        if (state.has(JSON_KEY_SMART_COST_TYPE) && !state.get(JSON_KEY_SMART_COST_TYPE).isJsonNull()) {
            smartCostType = state.get(JSON_KEY_SMART_COST_TYPE).getAsString();
        }

        if (state.has(JSON_KEY_GRID_CONFIGURED)) {
            JsonElement grid = state.get(JSON_KEY_GRID);
            if (grid != null && grid.isJsonObject()) {
                gridTransformer.transform(grid.getAsJsonObject()).entrySet()
                        .forEach(entry -> state.add(entry.getKey(), entry.getValue()));
            }
            state.remove(JSON_KEY_GRID);
            state.remove(JSON_KEY_GRID_CONFIGURED);
        }
        createChannelsAndSetStatesFromApiResponse(state);
        logger.trace("Site handler initialized successfully");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL();
            handler.register(this);
            MessageRouter router = handler.getMessageRouter();
            router.registerRoute(
                    new HandlerRoute(JSON_KEY_GRID, new JsonPathExtraction("$"), this, JSON_KEY_GRID, gridTransformer));
            router.registerRoute(new HandlerRoute(PROPERTY_TYPE_SITE, new ObjectFieldExtraction(JSON_KEY_GRID), this,
                    JSON_KEY_GRID, gridTransformer));
            router.registerRoute(
                    new HandlerRoute(JSON_KEY_HOME_POWER, new JsonPathExtraction("$"), this, JSON_KEY_HOME_POWER));
        });
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state;
    }
}
