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
 * The {@link EvccVehicleHandler} is responsible for fetching the data from the API response for Vehicle things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccVehicleHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccVehicleHandler.class);

    private final @Nullable String vehicleId;

    private String endpoint = "";

    public EvccVehicleHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        vehicleId = thing.getProperties().get(PROPERTY_ID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase();
            String value = command.toString();
            if (value.contains(" ")) {
                value = value.substring(0, command.toString().indexOf(" "));
            }
            String url = endpoint + "/" + vehicleId + "/" + datapoint + "/" + value;
            logger.debug("Sending command to this url: {}", url);
            sendCommand(url);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        state = state.getAsJsonObject(JSON_MEMBER_VEHICLES).getAsJsonObject(vehicleId);
        super.updateFromEvccState(state);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = handler.getBaseURL() + API_PATH_VEHICLES;
            JsonObject stateOpt = handler.getCachedEvccState();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            JsonObject state = stateOpt.getAsJsonObject(JSON_MEMBER_VEHICLES).getAsJsonObject(vehicleId);
            commonInitialize(state);
        });
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_MEMBER_VEHICLES) ? state.getAsJsonObject(JSON_MEMBER_VEHICLES).getAsJsonObject(vehicleId)
                : new JsonObject();
    }
}
