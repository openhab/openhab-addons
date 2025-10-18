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

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase();
            String value;
            if (command instanceof OnOffType) {
                value = command == OnOffType.ON ? "true" : "false";
            } else {
                value = command.toString();
                if (value.contains(" ")) {
                    value = value.substring(0, command.toString().indexOf(" "));
                }
            }
            String url = endpoint + "/" + datapoint + "/" + value;
            logger.debug("Sending command to this url: {}", url);
            sendCommand(url);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        if (state.has(JSON_KEY_GRID_CONFIGURED)) {
            modifyJSON(state);
        }
        updateStatesFromApiResponse(state);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL();
            JsonObject state = handler.getCachedEvccState().deepCopy();
            if (state.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            // Set the smart cost type
            if (state.has(JSON_KEY_SMART_COST_TYPE) && !state.get(JSON_KEY_SMART_COST_TYPE).isJsonNull()) {
                smartCostType = state.get(JSON_KEY_SMART_COST_TYPE).getAsString();
            }

            if (state.has(JSON_KEY_GRID_CONFIGURED)) {
                modifyJSON(state);
            }
            commonInitialize(state);
        });
    }

    private void modifyJSON(JsonObject state) {
        for (Map.Entry<String, JsonElement> entry : state.getAsJsonObject(JSON_KEY_GRID).entrySet()) {
            if ("currents".equals(entry.getKey())) {
                addMeasurementDatapointsToState(state, entry.getValue().getAsJsonArray(), "Current");
            } else if ("voltages".equals(entry.getKey())) {
                addMeasurementDatapointsToState(state, entry.getValue().getAsJsonArray(), "Voltage");
            } else {
                state.add(JSON_KEY_GRID + Utils.capitalizeFirstLetter(entry.getKey()), entry.getValue());
            }
        }
        state.remove(JSON_KEY_GRID);
        state.remove(JSON_KEY_GRID_CONFIGURED);
    }

    protected void addMeasurementDatapointsToState(JsonObject state, JsonArray values, String datapoint) {
        int phase = 1;
        for (JsonElement value : values) {
            state.add("grid" + datapoint + "L" + phase, value);
            phase++;
        }
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state;
    }
}
