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
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * The {@link EvccLoadpointHandler} is responsible for fetching the data from the API response for Loadpoint things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccLoadpointHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccLoadpointHandler.class);

    // JSON keys that need a special treatment, in example for backwards compatibility
    private static final Map<String, String> JSON_KEYS = Map.ofEntries(
            Map.entry(JSON_KEY_CHARGE_CURRENT, JSON_KEY_OFFERED_CURRENT),
            Map.entry(JSON_KEY_VEHICLE_PRESENT, JSON_KEY_CONNECTED),
            Map.entry(JSON_KEY_PHASES, JSON_KEY_PHASES_CONFIGURED), Map.entry(JSON_KEY_CHARGE_CURRENTS, ""),
            Map.entry(JSON_KEY_CHARGE_VOLTAGES, ""));
    protected final int index;

    public EvccLoadpointHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        this.index = Integer.parseInt(getPropertyOrConfigValue(PROPERTY_INDEX));
        type = PROPERTY_TYPE_LOADPOINT;
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = String.join("/", handler.getBaseURL(), API_PATH_LOADPOINTS, String.valueOf(index + 1));
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            if (this instanceof EvccHeatingHandler heating) {
                heating.updateJSON(stateOpt.getAsJsonObject());
            }

            JsonObject state = stateOpt.getAsJsonArray(JSON_KEY_LOADPOINTS).get(index).getAsJsonObject();

            modifyJSON(state);
            commonInitialize(state);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State state) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase();
            // Correct the datapoint for the API call
            if ("phasesconfigured".equals(datapoint)) {
                datapoint = JSON_KEY_PHASES;
            }
            // Special Handling for enable and disable endpoints
            if (datapoint.contains("enable")) {
                datapoint = "/enable/" + datapoint.replace("enable", "");
            } else if (datapoint.contains("disable")) {
                datapoint += "/disable/" + datapoint.replace("disable", "");
            }
            String value = "";
            if (state instanceof OnOffType) {
                value = state == OnOffType.ON ? "true" : "false";
            } else {
                value = state.toString();
                if (value.contains(" ")) {
                    value = value.substring(0, state.toString().indexOf(" "));
                }
            }
            String url = String.join("/", endpoint, datapoint, value);
            logger.debug("Sending command to this url: {}", url);
            if (sendCommand(url, JsonNull.INSTANCE)) {
                updateState(channelUID, state);
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        state = state.getAsJsonArray(JSON_KEY_LOADPOINTS).get(index).getAsJsonObject();
        modifyJSON(state);
        updateStatesFromApiResponse(state);
    }

    private void modifyJSON(JsonObject state) {
        JSON_KEYS.forEach((oldKey, newKey) -> {
            if (state.has(oldKey)) {
                if (oldKey.equals(JSON_KEY_CHARGE_CURRENTS)) {
                    addMeasurementDatapointToState(state, state.getAsJsonArray(oldKey), "Current");
                } else if (oldKey.equals(JSON_KEY_CHARGE_VOLTAGES)) {
                    addMeasurementDatapointToState(state, state.getAsJsonArray(oldKey), "Voltage");
                } else {
                    state.add(newKey, state.get(oldKey));
                }
                state.remove(oldKey);
            }
        });
    }

    protected void addMeasurementDatapointToState(JsonObject state, JsonArray values, String datapoint) {
        int phase = 1;
        for (JsonElement value : values) {
            state.add("charge" + datapoint + "L" + phase, value);
            phase++;
        }
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_KEY_LOADPOINTS) ? state.getAsJsonArray(JSON_KEY_LOADPOINTS).get(index).getAsJsonObject()
                : new JsonObject();
    }
}
