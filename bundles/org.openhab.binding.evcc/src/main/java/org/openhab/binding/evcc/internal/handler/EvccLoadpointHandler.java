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

import com.google.gson.JsonObject;

/**
 * The {@link EvccLoadpointHandler} is responsible for fetching the data from the API response for Loadpoint things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccLoadpointHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccLoadpointHandler.class);
    protected final int index;
    private int[] version = {};

    public EvccLoadpointHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        Map<String, String> props = thing.getProperties();
        String indexString = props.getOrDefault(PROPERTY_INDEX, "0");
        index = Integer.parseInt(indexString);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL() + API_PATH_LOADPOINTS + "/" + (index + 1);
            JsonObject stateOpt = handler.getCachedEvccState();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            if (this instanceof EvccHeatingHandler heating) {
                heating.updateJSON(stateOpt.getAsJsonObject());
            }

            JsonObject state = stateOpt.getAsJsonArray(JSON_MEMBER_LOADPOINTS).get(index).getAsJsonObject();

            modifyJSON(state);
            commonInitialize(state);
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase();
            // Backwardscompatibility for phasesConfigured
            if ("configuredPhases".equals(datapoint) && version[0] == 0 && version[1] < 200) {
                datapoint = "phases";
            }
            // Special Handling for enable and disable endpoints
            if (datapoint.contains("enable")) {
                datapoint += "/enable/" + datapoint.replace("enable", "");
            } else if (datapoint.contains("disable")) {
                datapoint += "/disable/" + datapoint.replace("disable", "");
            }
            String value = "";
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
    public void updateFromEvccState(JsonObject state) {
        version = Utils.convertVersionStringToIntArray(state.get("version").getAsString().split(" ")[0]);
        state = state.getAsJsonArray(JSON_MEMBER_LOADPOINTS).get(index).getAsJsonObject();
        modifyJSON(state);
        super.updateFromEvccState(state);
    }

    private void modifyJSON(JsonObject state) {
        // This is for backward compatibility with older evcc versions
        if (state.has("chargeCurrent")) {
            state.addProperty("offeredCurrent", state.get("chargeCurrent").getAsDouble());
            state.remove("chargeCurrent");
        }
        if (state.has("vehiclePresent")) {
            state.add("connected", state.get("vehiclePresent"));
        }
        if (state.has("enabled")) {
            state.add("charging", state.get("enabled"));
        }
        if (state.has("phases")) {
            state.add("phasesConfigured", state.get("phases"));
        }
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_MEMBER_LOADPOINTS)
                ? state.getAsJsonArray(JSON_MEMBER_LOADPOINTS).get(index).getAsJsonObject()
                : new JsonObject();
    }
}
