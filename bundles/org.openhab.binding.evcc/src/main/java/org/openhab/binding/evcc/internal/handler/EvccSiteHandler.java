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
 * The {@link EvccSiteHandler} is responsible for creating the bridge and thing
 * handlers.
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
            String datapoint = channelUID.getId().toLowerCase();
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
        if (root.has("gridConfigured")) {
            // If grid is configured, add gridPower to the root object
            // This is a workaround to avoid modifying the original JSON structure
            double gridPower = root.getAsJsonObject("grid").get("power").getAsDouble();
            root.addProperty("gridPower", gridPower);
        }
        super.updateFromEvccState(root);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (bridgeHandler == null) {
            return;
        }
        endpoint = bridgeHandler.getBaseURL();
        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get();
        if (state.has("gridConfigured")) {
            double gridPower = state.getAsJsonObject("grid").get("power").getAsDouble();
            state.addProperty("gridPower", gridPower);
        }
        commonInitialize(state);
    }
}
