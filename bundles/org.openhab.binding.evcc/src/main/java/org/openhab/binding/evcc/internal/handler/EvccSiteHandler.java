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
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.evcc.internal.handler.routing.FixedValueExtraction;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.binding.evcc.internal.handler.routing.MessageRouter;
import org.openhab.binding.evcc.internal.handler.routing.ObjectFieldExtraction;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
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
        logger.debug("Site handler initializing from state");
        // Set the smart cost type
        if (state.has(JSON_KEY_SMART_COST_TYPE) && !state.get(JSON_KEY_SMART_COST_TYPE).isJsonNull()) {
            smartCostType = state.get(JSON_KEY_SMART_COST_TYPE).getAsString();
        }

        if (state.has(JSON_KEY_GRID_CONFIGURED)) {
            modifyJSON(state);
        }
        createChannelsAndSetStatesFromApiResponse(state);
        logger.debug("Site handler initialized successfully");
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL();
            handler.register(this);
            MessageRouter router = handler.getMessageRouter();
            router.registerRoute(new HandlerRoute(JSON_KEY_GRID, new JsonPathExtraction("$"), this, JSON_KEY_GRID));
            router.registerRoute(new HandlerRoute(PROPERTY_TYPE_SITE, new ObjectFieldExtraction(JSON_KEY_GRID), this,
                    JSON_KEY_GRID));
            router.registerRoute(new HandlerRoute("pvEnergy", new FixedValueExtraction(), this, "pvEnergy"));
            router.registerRoute(new HandlerRoute("pvPower", new FixedValueExtraction(), this, "pvPower"));
            router.registerRoute(new HandlerRoute("tariffGrid", new FixedValueExtraction(), this, "tariffGrid"));
        });
    }

    private void modifyJSON(JsonObject state) {
        for (Map.Entry<String, JsonElement> entry : state.get(JSON_KEY_GRID).getAsJsonObject().entrySet()) {
            switch (entry.getKey()) {
                case "currents":
                    addPhaseChannels(state, entry.getValue().getAsJsonArray(), "grid", "Current");
                    break;
                case "voltages":
                    addPhaseChannels(state, entry.getValue().getAsJsonArray(), "grid", "Voltage");
                    break;
                case "powers":
                    addPhaseChannels(state, entry.getValue().getAsJsonArray(), "grid", "Power");
                    break;
                default:
                    state.add(JSON_KEY_GRID + Utils.capitalizeFirstLetter(entry.getKey()), entry.getValue());
            }
        }
        state.remove(JSON_KEY_GRID);
        state.remove(JSON_KEY_GRID_CONFIGURED);
    }

    protected void addMeasurementDatapointsToState(JsonObject state, JsonArray values, String datapoint) {
        addPhaseChannels(state, values, "grid", datapoint);
    }

    @Override
    public void handleUpdate(String key, JsonElement value) {
        if (JSON_KEY_GRID.equals(key) && value.isJsonObject()) {
            JsonObject gridUpdate = value.getAsJsonObject();
            JsonObject updateState = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : gridUpdate.entrySet()) {
                switch (entry.getKey()) {
                    case "currents":
                        addMeasurementDatapointsToState(updateState, entry.getValue().getAsJsonArray(), "Current");
                        break;
                    case "voltages":
                        addMeasurementDatapointsToState(updateState, entry.getValue().getAsJsonArray(), "Voltage");
                        break;
                    case "powers":
                        addMeasurementDatapointsToState(updateState, entry.getValue().getAsJsonArray(), "Power");
                        break;
                    default:
                        updateState.add("grid" + Utils.capitalizeFirstLetter(entry.getKey()), entry.getValue());
                }
            }
            updateOnlyPresentChannels(updateState);
            updateStatus(ThingStatus.ONLINE);
            return;
        }
        super.handleUpdate(key, value);
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state;
    }
}
