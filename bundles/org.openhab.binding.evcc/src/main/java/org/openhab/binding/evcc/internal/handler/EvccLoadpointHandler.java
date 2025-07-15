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
 * The {@link EvccLoadpointHandler} is responsible for creating the bridge and thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccLoadpointHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccLoadpointHandler.class);
    private final int index;

    public EvccLoadpointHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        Map<String, String> props = thing.getProperties();
        String indexString = props.getOrDefault("index", "0");
        index = Integer.parseInt(indexString);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (bridgeHandler != null) {
            endpoint = bridgeHandler.getBaseURL() + "/loadpoints/" + (index + 1);
            Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            JsonObject state = stateOpt.get().getAsJsonArray("loadpoints").get(index).getAsJsonObject();

            // For backward compatibility add these elements if present
            if (state.has("vehiclePresent")) {
                state.add("connected", state.get("vehiclePresent"));
            }
            if (state.has("enabled")) {
                state.add("charging", state.get("enabled"));
            }
            if (state.has("phases")) {
                state.add("phasesConfigured", state.get("phases"));
            }
            commonInitialize(state);
        } else {
            return;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            String datapoint = channelUID.getId().replace("loadpoint", "").toLowerCase();
            // Special Handling for enbale and disable endpoints
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
            logger.debug("Sendig command to this url: {}", url);
            sendCommand(url);
        }
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        root = root.getAsJsonArray("loadpoints").get(index).getAsJsonObject();
        super.updateFromEvccState(root);
    }

    public void updateJSON(JsonObject state) {
        if (state.has("chargeCurrent")) {
            // This is for backward compatibility with older EVCC versions
            state.addProperty("offeredCurrent", state.get("chargeCurrent").getAsDouble());
            state.remove("chargeCurrent");
        }
    }
}
