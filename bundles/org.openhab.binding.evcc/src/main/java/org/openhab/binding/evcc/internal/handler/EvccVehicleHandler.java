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
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link EvccVehicleHandler} is responsible for creating the bridge and thing
 * handlers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccVehicleHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccVehicleHandler.class);

    @Nullable
    private final String vehicleId;

    private String endpoint = "";

    public EvccVehicleHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        vehicleId = thing.getProperties().get("id");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            String datapoint = channelUID.getId().replace("vehicle", "").toLowerCase();
            String value = command.toString();
            if (value.contains(" ")) {
                value = value.substring(0, command.toString().indexOf(" "));
            }
            String url = endpoint + "/" + vehicleId + "/" + datapoint + "/" + value;
            logger.debug("Sending command to this url: {}", url);
            sendCommand(url);
        }
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        root = root.getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        super.updateFromEvccState(root);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (bridgeHandler == null) {
            return;
        }
        endpoint = bridgeHandler.getBaseURL() + "/vehicles";
        Optional<JsonObject> stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.get().getAsJsonObject("vehicles").getAsJsonObject(vehicleId);
        commonInitialize(state);
    }
}
